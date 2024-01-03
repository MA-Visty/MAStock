package be.heh.projet_mastock.Activity.Fragments

import androidx.fragment.app.Fragment

// Interface to communicate changes between fragments and the hosting activity
interface FragmentChangeListener {

    // Method to replace the current fragment with a new one
    fun replaceFragment(fragment: Fragment)

    // Method to update the selected item in the bottom navigation of the hosting activity
    fun updateSelectedItem(itemId: Int)

    // Callback method triggered when a product is updated in a fragment
    fun onProductUpdate()
}
