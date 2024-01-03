package be.heh.projet_mastock.Activity.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import be.heh.projet_mastock.Activity.WebActivity
import be.heh.projet_mastock.DB.MyDB
import be.heh.projet_mastock.DB.Product
import be.heh.projet_mastock.DB.ProductRecord
import be.heh.projet_mastock.R
import be.heh.projet_mastock.databinding.FragmentAddBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class AddFragment : Fragment() {

    // Variable to track whether it's an update or a new product
    private var isUpadte: Boolean = false

    // Database instance
    private lateinit var db: MyDB

    // View Binding
    private lateinit var binding: FragmentAddBinding

    // Array to store product types
    private lateinit var types: Array<String>

    // Current product being edited
    private var product: ProductRecord = ProductRecord(0, "", "", "", "", false)

    // Listener to communicate with the parent activity
    private lateinit var fragmentChangeListener: FragmentChangeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Attach the FragmentChangeListener to the parent activity
        if (context is FragmentChangeListener) {
            fragmentChangeListener = context
        } else {
            throw ClassCastException("$context must implement FragmentChangeListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentAddBinding.inflate(inflater, container, false)

        // Initialize the database
        db = MyDB.getDB(requireContext())

        // Load product types from resources and set up an ArrayAdapter
        types = resources.getStringArray(R.array.type)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.list_type, types)
        binding.faActvType.setAdapter(arrayAdapter)

        // Check if the Fragment is being used to edit an existing product
        val arguments = arguments
        if (arguments != null && arguments.containsKey("product")) {
            // If editing, retrieve the product details from arguments
            val initialProduct = arguments.getSerializable("product") as Product
            // Initialize the product with the existing data
            product = ProductRecord(
                initialProduct.id,
                initialProduct.type,
                initialProduct.brandModel,
                initialProduct.refNumber,
                initialProduct.webSite,
                initialProduct.isBorrow
            )

            // Configure UI based on user permissions
            configureUIBasedOnPermissions()

            // Set up UI elements and generate QR code
            setZone()
            generateQR()
        } else {
            // If creating a new product, hide the delete button
            binding.faIvDelete.visibility = View.GONE
        }

        // Set up listeners for various UI elements
        setUpListeners()

        return binding.root
    }

    // Function to configure UI elements based on user permissions
    private fun configureUIBasedOnPermissions() {
        val sharedPreferences = requireActivity().getSharedPreferences("accountPref", Context.MODE_PRIVATE)

        // If user has permission to update, configure UI elements accordingly
        if (sharedPreferences.getBoolean("isEnable", false)) {
            binding.faBpAdd.text = "Update"
            isUpadte = true

            // Toggle borrow status when clicked on borrow icon
            binding.faIvIsBorrow.setOnClickListener {
                if (product.isBorrow) {
                    product.isBorrow = !product.isBorrow
                    binding.faIvIsBorrow.setImageResource(R.drawable.package_box_ui_2_svgrepo_com)
                } else {
                    product.isBorrow = !product.isBorrow
                    binding.faIvIsBorrow.setImageResource(R.drawable.package_ui_web_svgrepo_com)
                }
            }
        } else {
            // If user doesn't have update permission, disable certain UI elements
            binding.faTilType.isEnabled = false
            binding.faTilBrandModel.isEnabled = false
            binding.faTilRefNumber.isEnabled = false
            binding.faTietWebSite.isFocusable = false
            binding.faTietWebSite.isTextInputLayoutFocusedRectEnabled = false
            binding.faLlAction.visibility = View.GONE

            // Open web activity when website field is clicked
            binding.faTietWebSite.setOnClickListener {
                val iWebSite = Intent(requireContext(), WebActivity::class.java)
                iWebSite.putExtra("url", product.webSite)
                startActivity(iWebSite)
            }
        }

        // Show borrow icon and set its initial state based on product's borrow status
        binding.faIvIsBorrow.visibility = View.VISIBLE
        if (product.isBorrow) {
            binding.faIvIsBorrow.setImageResource(R.drawable.package_ui_web_svgrepo_com)
        } else {
            binding.faIvIsBorrow.setImageResource(R.drawable.package_box_ui_2_svgrepo_com)
        }
    }

    // Function to set up listeners for various UI elements
    private fun setUpListeners() {
        // Set listener for QR code icon to initiate scanning
        binding.faIvQrCode.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("accountPref", Context.MODE_PRIVATE)

            // If user has permission to update, configure UI elements accordingly
            if (sharedPreferences.getBoolean("isEnable", false)) {
                scanCode()
            }
        }

        // Set long click listener for QR code icon to share the QR code image
        binding.faIvQrCode.setOnLongClickListener {
            // Get the QR code image as Bitmap from ImageView
            val qrCodeBitmap = (binding.faIvQrCode.drawable as? BitmapDrawable)?.bitmap

            // Check if the Bitmap is not null
            if (qrCodeBitmap != null) {
                // Create an intent with the ACTION_SEND action
                val shareIntent = Intent(Intent.ACTION_SEND)

                // Set the content type of the intent
                shareIntent.type = "image/*"

                // Add the Bitmap to the intent using a Uri
                val imageUri: Uri = getImageUri(qrCodeBitmap)
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)

                // Add optional text to share with the image
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Share this QRCode!")

                // Launch the sharing activity with the intent
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
            true
        }

        // Set item click listener for product type AutoCompleteTextView
        binding.faActvType.setOnItemClickListener(AdapterView.OnItemClickListener { _, _, position, _ ->
            product.type = types[position]
            if (types[position] == "Smartphone") {
                binding.faTilType.setStartIconDrawable(R.drawable.baseline_smartphone_24)
            } else if (types[position] == "Tablet") {
                binding.faTilType.setStartIconDrawable(R.drawable.baseline_tablet_24)
            } else {
                binding.faTilType.setStartIconDrawable(R.drawable.baseline_question_mark_24)
            }
            binding.faTilType.error = ""
            generateQR()
        })

        // Set text change listeners for various text input fields
        binding.faTietBrandModel.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                product.brandModel = binding.faTietBrandModel.text.toString()
                binding.faTilBrandModel.error = ""
                generateQR()
            }
            override fun afterTextChanged(editable: Editable?) {}
        })

        binding.faTietRefNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                product.refNumber = binding.faTietRefNumber.text.toString()
                binding.faTilRefNumber.error = ""
                generateQR()
            }
            override fun afterTextChanged(editable: Editable?) {}
        })

        binding.faTietWebSite.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                product.webSite = binding.faTietWebSite.text.toString()
                binding.faTilWebSite.error = ""
                generateQR()
            }
            override fun afterTextChanged(editable: Editable?) {}
        })

        // Set click listener for delete button
        binding.faIvDelete.setOnClickListener {
            showConfirmationDialog()
        }

        // Set click listener for add/update button
        binding.faBpAdd.setOnClickListener {
            if (checkValideProduct()) {
                if (isUpadte) {
                    updateProduct()
                } else {
                    lifecycleScope.launch {
                        if (isNewProduct()) {
                            saveProduct()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Product ${product.refNumber} is already exist.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    // Function to share the QR code image
    private fun getImageUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            "QRCode",
            null
        )
        return Uri.parse(path)
    }

    // Function to set UI fields based on the product
    private fun setZone() {
        // Get the index of the selected type
        val selectedTypeIndex = types.indexOf(product.type)
        if (selectedTypeIndex != -1) {
            // Set type, brandModel, refNumber, and webSite
            binding.faActvType.setText(product.type, false)
            binding.faActvType.setSelection(selectedTypeIndex)
            if (product.type == "Smartphone") {
                binding.faTilType.setStartIconDrawable(R.drawable.baseline_smartphone_24)
            } else if (product.type == "Tablet") {
                binding.faTilType.setStartIconDrawable(R.drawable.baseline_tablet_24)
            } else {
                binding.faTilType.setStartIconDrawable(R.drawable.baseline_question_mark_24)
            }
        }
        binding.faTietBrandModel.setText(product.brandModel)
        binding.faTietRefNumber.setText(product.refNumber)
        binding.faTietWebSite.setText(product.webSite)
    }

    // Function to initiate barcode scanning
    private fun scanCode() {
        var scanOptions: ScanOptions = ScanOptions()
        scanOptions.setPrompt("Volume up to flash 'ON' \nVolume down to flash 'OFF'")
        scanOptions.setBeepEnabled(true)
        scanOptions.setOrientationLocked(true)
        scanOptions.setCaptureActivity(CaptureAct::class.java)
        barLauncher.launch(scanOptions)
    }

    // Result launcher for barcode scanning
    var barLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // Parse the scanned QR code content
            val lines = result.contents.lines()
            if (lines.size >= 3) {
                product.type = lines[0].trim()
                product.brandModel = lines[1].trim()
                product.refNumber = lines[2].trim()
                product.webSite = lines[3].trim()

                // Update UI with scanned data
                setZone()
            } else {
                // Show an error dialog for an invalid QR code
                var builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Error")
                builder.setMessage("QR code is not valid!")
                builder.setPositiveButton("OK") { dialogInterface, i -> dialogInterface.dismiss() }
                builder.show()
            }
        }
    }

    // Function to show a confirmation dialog for deleting a product
    private fun showConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Confirmation")
        alertDialogBuilder.setMessage("Do you really want to delete this item?")
        alertDialogBuilder.setPositiveButton("Confirm") { dialog, which ->
            // Call deleteProduct() if user confirms deletion
            deleteProduct()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // Function to check if the product is valid (non-empty fields)
    private fun checkValideProduct(): Boolean {
        val types = resources.getStringArray(R.array.type)
        if (!product.type.isNullOrBlank() && types.contains(product.type) &&
            !product.brandModel.isNullOrBlank() && !product.refNumber.isNullOrBlank() && !product.webSite.isNullOrBlank()
        ) {
            return true
        } else {
            // Set error messages for invalid fields
            binding.faTilType.error = "Invalid type!"
            binding.faTilBrandModel.error = "Invalid brand and model!"
            binding.faTilRefNumber.error = "Invalid reference number!"
            binding.faTilWebSite.error = "Invalid web site!"
            return false
        }
    }

    // Function to check if the product is new (not in the database)
    private suspend fun isNewProduct(): Boolean {
        return withContext(Dispatchers.IO) {
            val existingProduct = db.productDao().getProduct(product.refNumber)
            existingProduct == null
        }
    }

    // Function to save a new product to the database
    private fun saveProduct() {
        GlobalScope.launch {
            db.productDao().insertProduct(product)
            activity?.runOnUiThread {
                // Display success message, update UI, and redirect to home
                Toast.makeText(requireContext(), "Product ${product.refNumber} is added.", Toast.LENGTH_LONG).show()
                (activity as? FragmentChangeListener)?.onProductUpdate()
                redirect()
            }
        }
    }

    // Function to update an existing product in the database
    private fun updateProduct() {
        GlobalScope.launch {
            db.productDao().updateProduct(product)
            activity?.runOnUiThread {
                // Display success message, update UI, and redirect to home
                Toast.makeText(requireContext(), "Product ${product.refNumber} is updated.", Toast.LENGTH_LONG).show()
                (activity as? FragmentChangeListener)?.onProductUpdate()
                redirect()
            }
        }
    }

    // Function to delete a product from the database
    private fun deleteProduct() {
        GlobalScope.launch {
            db.productDao().deleteProduct(product)
            activity?.runOnUiThread {
                // Display success message, update UI, and redirect to home
                Toast.makeText(requireContext(), "Product ${product.refNumber} is deleted.", Toast.LENGTH_LONG).show()
                (activity as? FragmentChangeListener)?.onProductUpdate()
                redirect()
            }
        }
    }

    // Function to redirect to the home fragment
    private fun redirect() {
        fragmentChangeListener?.updateSelectedItem(R.id.bnm_i_home)
        fragmentChangeListener?.replaceFragment(HomeFragment())
    }

    // Function to generate a QR code from the product details and set it to the ImageView
    private fun generateQR() {
        val text: String = "${product.type}\n${product.brandModel}\n${product.refNumber}\n${product.webSite}"
        var writer: MultiFormatWriter = MultiFormatWriter()
        try {
            var matrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 100, 100)
            var encoder: BarcodeEncoder = BarcodeEncoder()
            var bitmap: Bitmap = encoder.createBitmap(matrix)
            binding.faIvQrCode.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}