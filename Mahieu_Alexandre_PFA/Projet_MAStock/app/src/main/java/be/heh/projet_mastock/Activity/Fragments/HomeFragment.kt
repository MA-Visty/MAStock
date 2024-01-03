package be.heh.projet_mastock.Activity.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import be.heh.projet_mastock.DB.MyDB
import be.heh.projet_mastock.DB.Product
import be.heh.projet_mastock.DB.ProductRecord
import be.heh.projet_mastock.R
import be.heh.projet_mastock.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Fragment displaying a list of products
class HomeFragment : Fragment() {

    // Data list for products
    private var data: MutableList<ProductRecord> = mutableListOf()

    // View binding for the fragment
    private lateinit var binding: FragmentHomeBinding

    // Database instance
    private lateinit var db : MyDB

    // Adapter for managing the ListView of products
    private lateinit var adapter: ProductAdapter

    // Listener for handling fragment changes
    private lateinit var fragmentChangeListener: FragmentChangeListener

    // Fragment lifecycle method called when attaching to the context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure the context implements FragmentChangeListener
        try {
            fragmentChangeListener = context as FragmentChangeListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement FragmentChangeListener")
        }
    }

    // Fragment lifecycle method called when creating the view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize the database instance
        db = MyDB.getDB(requireContext())

        // Initialize the adapter for the ListView
        adapter = ProductAdapter(requireContext(), data)
        binding.fhLv.adapter = adapter

        // Set item click listener for ListView items
        binding.fhLv.setOnItemClickListener { parent, view, position, id ->
            // Notify the listener of the selected item
            fragmentChangeListener?.updateSelectedItem(R.id.bnm_i_add)
            // Retrieve the selected product
            val selectedItem = data[position]
            // Create a new AddFragment with product details
            val newFragment = AddFragment()
            val args = Bundle()
            args.putSerializable("product", Product(selectedItem.id, selectedItem.type, selectedItem.brandModel, selectedItem.refNumber, selectedItem.webSite, selectedItem.isBorrow))
            newFragment.arguments = args
            // Replace the current fragment with the new AddFragment
            fragmentChangeListener?.replaceFragment(newFragment)
        }

        // Load products from the database
        loadProducts()

        return binding.root
    }

    // Function to load products from the database
    fun loadProducts() {
        GlobalScope.launch {
            // Clear the adapter and add all products from the database
            adapter.clear()
            adapter.addAll(db.productDao().get())

            // Notify the adapter of data changes on the main thread
            withContext(Dispatchers.Main) {
                adapter.notifyDataSetChanged()
            }
        }
    }
}
