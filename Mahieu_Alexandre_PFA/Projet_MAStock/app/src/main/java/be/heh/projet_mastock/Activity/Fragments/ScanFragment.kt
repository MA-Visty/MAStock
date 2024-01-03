package be.heh.projet_mastock.Activity.Fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import be.heh.projet_mastock.DB.MyDB
import be.heh.projet_mastock.DB.Product
import be.heh.projet_mastock.DB.ProductRecord
import be.heh.projet_mastock.R
import be.heh.projet_mastock.databinding.FragmentScanBinding
import com.google.android.material.button.MaterialButton
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Fragment for scanning QR codes and performing actions based on the scanned data
class ScanFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var db: MyDB
    private var state: String = ""
    private lateinit var fragmentChangeListener: FragmentChangeListener

    // Attaches the FragmentChangeListener to the context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            fragmentChangeListener = context as FragmentChangeListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement FragmentChangeListener")
        }
    }

    // Inflates the layout and sets up click listeners for different actions
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanBinding.inflate(inflater, container, false)
        val view = binding.root

        db = MyDB.getDB(requireContext())

        // Set up click listeners for different actions (Search, Deposit, Borrow)
        var bpSearch: MaterialButton = view?.findViewById(R.id.fs_mb_search)!!
        bpSearch.setOnClickListener {
            state = "Search"
            scanCode()
        }
        var bpDiscount: MaterialButton = view?.findViewById(R.id.fs_mb_deposit)!!
        bpDiscount.setOnClickListener {
            state = "Deposit"
            scanCode()
        }
        var bpBorrow: MaterialButton = view?.findViewById(R.id.fs_mb_borrow)!!
        bpBorrow.setOnClickListener {
            state = "Borrow"
            scanCode()
        }

        return view
    }

    // Initiates the barcode scanner with specified options
    private fun scanCode() {
        var scanOptions: ScanOptions = ScanOptions()
        scanOptions.setPrompt("Volume up to flash 'ON' \nVolume down to flash 'OFF'")
        scanOptions.setBeepEnabled(true)
        scanOptions.setOrientationLocked(true)
        scanOptions.setCaptureActivity(CaptureAct::class.java)
        barLauncher.launch(scanOptions)
    }

    // Retrieves product information based on the scanned QR code
    private fun getProduct(refNumber: String) {
        GlobalScope.launch {
            var product = db.productDao().getProduct(refNumber)
            activity?.runOnUiThread {
                if (product != null && state.equals("Search")) {
                    // If in Search state, navigate to AddFragment with product details
                    fragmentChangeListener?.updateSelectedItem(R.id.bnm_i_add)
                    val newFragment = AddFragment()
                    val args = Bundle()
                    args.putSerializable(
                        "product",
                        Product(
                            product.id,
                            product.type,
                            product.brandModel,
                            product.refNumber,
                            product.webSite,
                            product.isBorrow
                        )
                    )
                    newFragment.arguments = args
                    fragmentChangeListener?.replaceFragment(newFragment)
                } else if (product != null) {
                    // If in Deposit or Borrow state, update product's borrow status
                    if (state.equals("Deposit")) {
                        product.isBorrow = false
                    } else {
                        product.isBorrow = true
                    }
                    updateBorrow(product)
                } else {
                    // Display an error dialog for invalid QR code
                    var builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Error")
                    builder.setMessage("QR code is not valid!")
                    builder.setPositiveButton("OK") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
                    builder.show()
                }
            }
        }
    }

    // Updates the borrow status of the product in the database
    private fun updateBorrow(product: ProductRecord) {
        GlobalScope.launch {
            db.productDao().updateProduct(product)
            activity?.runOnUiThread {
                Toast.makeText(
                    context,
                    "The product ${product.brandModel} is borrowed ${product.isBorrow}",
                    Toast.LENGTH_SHORT
                ).show()
                // Update the product list and navigate to the home screen
                (activity as? FragmentChangeListener)?.onProductUpdate()
                fragmentChangeListener?.updateSelectedItem(R.id.bnm_i_home)
                fragmentChangeListener?.replaceFragment(HomeFragment())
            }
        }
    }

    // Activity result launcher for handling barcode scanner results
    var barLauncher: ActivityResultLauncher<ScanOptions> =
        registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val lines = result.contents.lines()
                if (lines.size >= 3) {
                    // Extract the reference number from the scanned data
                    val refNumber = lines[2].trim()
                    getProduct(refNumber)
                } else {
                    // Display an error dialog for invalid QR code
                    var builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Error")
                    builder.setMessage("QR code is not valid!")
                    builder.setPositiveButton("OK") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
                    builder.show()
                }
            }
        }
}
