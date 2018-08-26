package com.serviquik.nearby.customer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.serviquik.nearby.R
import de.hdodenhof.circleimageview.CircleImageView
import android.net.Uri
import android.support.design.card.MaterialCardView
import com.squareup.picasso.Picasso
import java.util.*


class ManageCustomerAdapter(private val customers: ArrayList<Customer>, private val context: Context) : RecyclerView.Adapter<ManageCustomerViewHolder>() {

    private val colors = java.util.ArrayList<String>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ManageCustomerViewHolder {
        colors.add("#FF9800")
        colors.add("#FF5722")
        colors.add("#009688")
        colors.add("#03A9F4")
        colors.add("#673AB7")
        colors.add("#F44336")
        colors.add("#3F51B5")
        return ManageCustomerViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.card_my_customer, p0, false))
    }

    override fun getItemCount(): Int {
        return customers.size
    }

    override fun onBindViewHolder(p0: ManageCustomerViewHolder, p1: Int) {
        val customer = customers[p1]
        val index = Random().nextInt(colors.size)
        p0.backgroud.setCardBackgroundColor(Color.parseColor(colors[index]))
        p0.nameTV.text = customer.name
        p0.typeTV.text = getTpe(customer.isLocal)
        p0.moreBtn.setOnClickListener { _ ->
            val popupMenu = PopupMenu(context, p0.moreBtn)
            popupMenu.inflate(R.menu.customer_more)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.moreEmail -> makeEmail(customer.email)
                    R.id.morePhone -> makePhone(customer.phoneNumber)
                    else -> return@setOnMenuItemClickListener false
                }
            }
            popupMenu.show()
        }
        if (customer.profileURL != null) {
            Picasso.Builder(context).build().load(customer.profileURL).into(p0.profileIV)
        }
    }

    private fun makePhone(phoneNumber: String): Boolean {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:$phoneNumber")
        if (dialIntent.resolveActivity(context.packageManager) != null) {
            startActivity(context, dialIntent, null)
        } else {
            Toast.makeText(context, "Can't resolve app for ACTION_DIAL Intent.", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun makeEmail(email: String): Boolean {

        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        try {
            startActivity(context, Intent.createChooser(i, "Send mail..."), null)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun getTpe(local: Boolean): CharSequence? {
        return if (local) {
            "Local"
        } else {
            "Online"
        }
    }

}

class ManageCustomerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val profileIV = view.findViewById<CircleImageView>(R.id.cardCustomerImage)!!
    val nameTV = view.findViewById<TextView>(R.id.cardCustomerName)!!
    val typeTV = view.findViewById<TextView>(R.id.cardCustomerType)!!
    val moreBtn = view.findViewById<ImageButton>(R.id.cardCustomerMore)!!
    val backgroud = view.findViewById<MaterialCardView>(R.id.cardCustomerBack)
}