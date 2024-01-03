package be.heh.projet_mastock.Activity.Fragments

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import be.heh.projet_mastock.Activity.WebActivity
import be.heh.projet_mastock.DB.ProductRecord
import be.heh.projet_mastock.R

// Custom ArrayAdapter for displaying product items in a ListView
class ProductAdapter(context: Context, products: List<ProductRecord>) : ArrayAdapter<ProductRecord>(context, R.layout.list_product, products) {

    // ViewHolder pattern for efficient ListView item reuse
    private class ViewHolder {
        lateinit var imageView: ImageView
        lateinit var brandModel: TextView
        lateinit var refNumber: TextView
        lateinit var webSite: TextView
        lateinit var isBorrow: ImageView
    }

    // Overrides the getView method to customize each item in the ListView
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Retrieve the product at the specified position
        var product: ProductRecord? = getItem(position)
        var viewHolder: ViewHolder

        var currentView = convertView

        // If the currentView is null, create a new ViewHolder and inflate the layout
        if (currentView == null) {
            viewHolder = ViewHolder()
            currentView = LayoutInflater.from(context).inflate(R.layout.list_product, parent, false)

            // Initialize ViewHolder components and associate them with layout elements
            viewHolder.imageView = currentView.findViewById(R.id.lp_iv_type)
            viewHolder.brandModel = currentView.findViewById(R.id.lp_tv_brandModel)
            viewHolder.refNumber = currentView.findViewById(R.id.lp_tv_refNumber)
            viewHolder.webSite = currentView.findViewById(R.id.lp_tv_webSite)
            viewHolder.isBorrow = currentView.findViewById(R.id.lp_iv_isBorrow)

            // Set the ViewHolder as a tag on the currentView
            currentView.tag = viewHolder
        } else {
            // If the currentView is not null, retrieve the ViewHolder from the tag
            viewHolder = currentView.tag as ViewHolder
        }

        // Populate the ViewHolder components with data from the current product
        product?.let {
            // Set the appropriate image for the product type
            if (it.type == "Smartphone") {
                viewHolder.imageView.setImageResource(R.drawable.baseline_smartphone_24)
            } else {
                viewHolder.imageView.setImageResource(R.drawable.baseline_tablet_24)
            }

            // Set the brand and model text
            viewHolder.brandModel.text = it.brandModel

            // Set the reference number text
            viewHolder.refNumber.text = it.refNumber

            // Set the website text
            viewHolder.webSite.text = it.webSite

            // Set the borrow indicator image based on whether the product is borrowed
            if (it.isBorrow) {
                viewHolder.isBorrow.setImageResource(R.drawable.package_ui_web_svgrepo_com)
            } else {
                viewHolder.isBorrow.setImageResource(R.drawable.package_box_ui_2_svgrepo_com)
            }

            // Set a click listener on the website text to open the WebActivity
            viewHolder.webSite.setOnClickListener {
                val iWebSite = Intent(context, WebActivity::class.java)
                iWebSite.putExtra("url", viewHolder.webSite.text)
                context.startActivity(iWebSite)
            }
        }

        // Return the populated currentView
        return currentView!!
    }
}
