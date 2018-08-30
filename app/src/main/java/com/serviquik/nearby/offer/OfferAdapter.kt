package com.serviquik.nearby.offer

import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.R
import com.serviquik.nearby.manageProduct.Viewholder
import com.squareup.picasso.Picasso

class OfferAdapter(private val offers: ArrayList<Offer>, private val context: Context) : RecyclerView.Adapter<OfferViewHolder>() {

    private lateinit var progressDialog: ProgressDialog

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): OfferViewHolder {
        progressDialog = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        return OfferViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.card_offer, p0, false))
    }

    override fun getItemCount(): Int {
        return offers.size
    }

    override fun onBindViewHolder(p0: OfferViewHolder, p1: Int) {
        val offer = offers[p1]

        if (offer.offer != null) {
            p0.textView.text = offer.offer
        } else {
            p0.textView.visibility = View.GONE
        }

        if (offer.picture != null) {
            Picasso.get().load(offer.picture).fit().into(p0.imageView)
        } else {
            p0.imageView.visibility = View.GONE
        }

        p0.deleteBtn.setOnClickListener { _ ->
            AlertDialog.Builder(context)
                    .setTitle("Warning")
                    .setMessage("Are you sure ou want to delete offer?")
                    .setPositiveButton("Yes") { _, _ ->
                        progressDialog.show()
                        FirebaseFirestore.getInstance()
                                .collection("Vendor")
                                .document(FirebaseAuth.getInstance().uid!!)
                                .collection("Offers").document(offer.id!!).delete().addOnCompleteListener { vendorIt ->
                                    if (vendorIt.isSuccessful) {
                                        FirebaseFirestore.getInstance()
                                                .collection("Products")
                                                .document(offer.product.parentID)
                                                .delete().addOnCompleteListener {
                                                    if (!it.isSuccessful) {
                                                        AlertDialog.Builder(context).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
                                                    }
                                                    progressDialog.dismiss()
                                                }
                                    } else {
                                        progressDialog.dismiss()
                                        AlertDialog.Builder(context).setTitle("Error").setMessage(vendorIt.exception!!.localizedMessage).show()
                                    }
                                }
                    }
                    .show()
        }

    }

}

class OfferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val imageView = view.findViewById<ImageView>(R.id.cardOfferImageView)!!
    val textView = view.findViewById<TextView>(R.id.cardOfferOffer)!!
    val deleteBtn = view.findViewById<ImageButton>(R.id.cardOfferDelete)!!
}