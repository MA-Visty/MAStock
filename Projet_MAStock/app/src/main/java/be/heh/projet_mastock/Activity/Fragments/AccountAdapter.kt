package be.heh.projet_mastock.Activity.Fragments

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import be.heh.projet_mastock.DB.MyDB
import be.heh.projet_mastock.DB.UserRecord
import be.heh.projet_mastock.R
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Custom ArrayAdapter for managing user accounts in a ListView
class AccountAdapter(context: Context, products: List<UserRecord>) : ArrayAdapter<UserRecord>(context, R.layout.list_user, products) {

    private lateinit var db : MyDB

    // ViewHolder pattern for efficient item view recycling
    private class ViewHolder {
        lateinit var linearLayoutType: LinearLayout
        lateinit var type: MaterialSwitch
        lateinit var email: TextView
        lateinit var delete: ImageView
    }

    // Override the getView method to provide custom item views for the ListView
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        db = MyDB.getDB(context)
        var user: UserRecord? = getItem(position)
        var viewHolder: ViewHolder

        var currentView = convertView

        // Implement the ViewHolder pattern for efficient item view recycling
        if (currentView == null) {
            viewHolder = ViewHolder()
            currentView = LayoutInflater.from(context).inflate(R.layout.list_user, parent, false)

            viewHolder.linearLayoutType = currentView.findViewById(R.id.lu_ll_type)
            viewHolder.type = currentView.findViewById(R.id.lu_ms_type)
            viewHolder.email = currentView.findViewById(R.id.lu_tv_email)
            viewHolder.delete = currentView.findViewById(R.id.lu_iv_delete)

            currentView.tag = viewHolder
        } else {
            viewHolder = currentView.tag as ViewHolder
        }

        // Populate the item view with data from the UserRecord
        user?.let {
            if(it.isAdmin) {
                viewHolder.linearLayoutType.visibility = View.GONE
                viewHolder.delete.visibility = View.GONE
            }
            viewHolder.type.isChecked = it.isEnable
            viewHolder.email.text = it.email

            // Set click listeners for the switch and delete icon
            viewHolder.type.setOnClickListener {
                user.isEnable = viewHolder.type.isChecked
                updateAccounts(user)
            }
            viewHolder.delete.setOnClickListener {
                showConfirmationDialog(user)
            }
        }

        return currentView!!
    }

    // Update the account status in the database and display a toast message
    private fun updateAccounts(userRecord: UserRecord) {
        GlobalScope.launch {
            db.userDao().updateUser(userRecord)
        }
        Toast.makeText(context, "Account ${userRecord.email} is updated.", Toast.LENGTH_SHORT).show()
    }

    // Display a confirmation dialog for deleting an account
    private fun showConfirmationDialog(userRecord: UserRecord) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Confirmation")
        alertDialogBuilder.setMessage("Do you really want to delete this item?")
        alertDialogBuilder.setPositiveButton("Confirm") { dialog, which ->
            deleteAccounts(userRecord)
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // Delete the account from the database and update the ListView
    private fun deleteAccounts(userRecord: UserRecord) {
        GlobalScope.launch {
            db.userDao().deleteUser(userRecord)

            (context as AppCompatActivity).runOnUiThread {
                remove(userRecord)
                notifyDataSetChanged()
            }
        }
        Toast.makeText(context, "Account ${userRecord.email} is deleted.", Toast.LENGTH_SHORT).show()
    }
}
