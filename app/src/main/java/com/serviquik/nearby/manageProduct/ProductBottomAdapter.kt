package com.serviquik.nearby.manageProduct

import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.serviquik.nearby.R
import com.squareup.picasso.Picasso

class ProductBottomAdapter(private val imagePaths: ArrayList<String>) : RecyclerView.Adapter<ProductBottomViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ProductBottomViewHolder {
        return ProductBottomViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.image_layout, p0, false))
    }

    override fun getItemCount(): Int {
        return imagePaths.size
    }

    override fun onBindViewHolder(p0: ProductBottomViewHolder, p1: Int) {
        p0.imageView.setImageDrawable(Drawable.createFromPath(imagePaths[p1]))
    }

}

class ProductBottomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val imageView = view.findViewById<ImageView>(R.id.imageLayoutImage)!!
}