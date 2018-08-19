package com.serviquik.nearby.review

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.serviquik.nearby.R
import com.willy.ratingbar.ScaleRatingBar

class ReviewAdapter(private val reviews: ArrayList<Review>) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.card_review, p0, false))
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val review = reviews[p1]
        p0.nameTV.text = review.Name
        p0.reviewTV.text = review.Review
        p0.ratingBar.rating = review.Star.toFloat()
    }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val nameTV:TextView = view.findViewById(R.id.reviewCardName)
    val reviewTV:TextView = view.findViewById(R.id.reviewCardreview)
    val ratingBar:ScaleRatingBar = view.findViewById(R.id.reviewCardRatingBar)
}