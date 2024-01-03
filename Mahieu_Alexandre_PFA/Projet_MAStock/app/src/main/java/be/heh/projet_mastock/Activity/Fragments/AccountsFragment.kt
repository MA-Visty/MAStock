package be.heh.projet_mastock.Activity.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import be.heh.projet_mastock.DB.MyDB
import be.heh.projet_mastock.DB.UserRecord
import be.heh.projet_mastock.databinding.FragmentAccountsBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Fragment displaying a list of user accounts
class AccountsFragment : Fragment() {

    // Data list for user accounts
    private var data: MutableList<UserRecord> = mutableListOf()

    // View binding for the fragment
    private lateinit var binding: FragmentAccountsBinding

    // Database instance
    private lateinit var db : MyDB

    // Adapter for managing the ListView of user accounts
    private lateinit var adapter: AccountAdapter

    // Fragment lifecycle method called when creating the view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        binding = FragmentAccountsBinding.inflate(inflater, container, false)

        // Initialize the database instance
        db = MyDB.getDB(requireContext())

        // Initialize the adapter for the ListView
        adapter = AccountAdapter(requireContext(), data)
        binding.faLv.adapter = adapter

        // Load user accounts from the database
        loadAccounts()

        return binding.root
    }

    // Function to load user accounts from the database
    private fun loadAccounts() {
        GlobalScope.launch {
            // Clear the adapter and add all user accounts from the database
            adapter.clear()
            adapter.addAll(db.userDao().get())
        }
    }
}
