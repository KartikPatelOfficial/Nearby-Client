package com.serviquik.nearby.offer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.serviquik.nearby.R
import com.serviquik.nearby.manageProduct.Viewholder
import com.squareup.picasso.Picasso

class OfferAdapter(private val offers: ArrayList<Offer>) : RecyclerView.Adapter<OfferViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): OfferViewHolder {
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

    }

}

class OfferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val imageView = view.findViewById<ImageView>(R.id.cardOfferImageView)!!
    val textView = view.findViewById<TextView>(R.id.cardOfferOffer)!!
}