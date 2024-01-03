package be.heh.projet_mastock.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import be.heh.projet_mastock.Activity.Fragments.AccountsFragment
import be.heh.projet_mastock.Activity.Fragments.AddFragment
import be.heh.projet_mastock.Activity.Fragments.FragmentChangeListener
import be.heh.projet_mastock.Activity.Fragments.HomeFragment
import be.heh.projet_mastock.Activity.Fragments.ScanFragment
import be.heh.projet_mastock.R
import be.heh.projet_mastock.databinding.ActivityProductsBinding

class ProductsActivity : AppCompatActivity(), FragmentChangeListener {

    // View binding instance for the activity
    lateinit var binding: ActivityProductsBinding

    // Reference to the activity context
    private lateinit var activityContext: AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize view binding
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assign the activity context
        activityContext = this

        // Set the initial fragment to HomeFragment
        replaceFragment(HomeFragment())

        // Adjust visibility of menu items based on user roles
        val sharedPreferences = getSharedPreferences("accountPref", Context.MODE_PRIVATE)
        binding.bottomNavigationView.menu.findItem(R.id.bnm_i_add).isVisible = sharedPreferences.getBoolean("isEnable", false)
        binding.bottomNavigationView.menu.findItem(R.id.bnm_i_accounts).isVisible = sharedPreferences.getBoolean("isAdmin", false)

        // Handle bottom navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bnm_i_home -> {
                    // Replace the fragment with HomeFragment
                    replaceFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.bnm_i_add -> {
                    // Replace the fragment with AddFragment
                    replaceFragment(AddFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.bnm_i_scan -> {
                    // Replace the fragment with ScanFragment
                    replaceFragment(ScanFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.bnm_i_accounts -> {
                    // Replace the fragment with AccountsFragment
                    replaceFragment(AccountsFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.bnm_i_logout -> {
                    // Navigate to MainActivity for logout
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    return@setOnItemSelectedListener true
                }
                else -> return@setOnItemSelectedListener false
            }
        }
    }

    // Callback method when a product is updated in a fragment
    override fun onProductUpdate() {
        // Retrieve HomeFragment and trigger its product loading
        val homeFragment = supportFragmentManager.findFragmentByTag("HomeFragment") as? HomeFragment
        homeFragment?.loadProducts()
    }

    // Update the selected item in the bottom navigation
    override fun updateSelectedItem(itemId: Int) {
        binding.bottomNavigationView.selectedItemId = itemId
    }

    // Replace the current fragment with a new one
    override fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
