package com.serviquik.nearby.manageProduct

import android.annotation.SuppressLint
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.serviquik.nearby.R
import com.squareup.picasso.Picasso
import com.willy.ratingbar.ScaleRatingBar

class ProductsAdapter(private val products: ArrayList<Product>) : RecyclerView.Adapter<Viewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        return Viewholder(LayoutInflater.from(parent.context).inflate(R.layout.card_products, parent, false))
    }

    override fun getItemCount(): Int {
        return products.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val product = products[position]

        holder.descriptionTV.text = product.description
        holder.titleTV.text = product.title
        holder.priceTV.text = "₹ "+product.price
        try {
            Picasso.get().load(Uri.parse(product.images!![0])).into(holder.imageView)
        }catch (e : NullPointerException){

        }
        if(product.rating!=null) {
            holder.ratingBar.rating = product.rating.toFloat()
        }else{
            holder.ratingBar.rating = 2.5f
        }

        holder.editBtn.setOnClickListener {
           Log.d("----->",product.parentID)
        }
    }

}

class Viewholder(view: View) : RecyclerView.ViewHolder(view) {
    val titleTV:TextView = view.findViewById(R.id.productCardTitle)
    val descriptionTV:TextView = view.findViewById(R.id.productCardDescription)
    val priceTV:TextView = view.findViewById(R.id.productCardPrice)
    val imageView:ImageView = view.findViewById(R.id.productCardImage)
    val editBtn:Button = view.findViewById(R.id.productCardEdit)
    val ratingBar:ScaleRatingBar = view.findViewById(R.id.productsCardRatingBar)
}