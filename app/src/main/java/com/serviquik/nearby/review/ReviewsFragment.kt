package com.serviquik.nearby.review


import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.MainActivity
import com.serviquik.nearby.R
import com.serviquik.nearby.auth.LoginActivity
import com.serviquik.nearby.manageProduct.Product

class ReviewsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val reviews = ArrayList<Review>()

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)
        MainActivity.changeToolbarTitle("Reviews")
        progressDialog.show()
        val notFoundTV: TextView = view.findViewById(R.id.manageReviewNotFoundTV)
        notFoundTV.visibility = View.INVISIBLE

        val adapter = ReviewAdapter(reviews)
        val recyclerView: RecyclerView = view.findViewById(R.id.manageReviewRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        recyclerView.adapter = adapter

        val product = Product(
                arguments!!["Description"] as String,
                arguments!!["Title"] as String,
                arguments!!.getLong("Price"),
                arguments!!.getStringArrayList("Images"),
                arguments!!["ParantID"] as String,
                arguments!!["Rating"] as String?,
                arguments!!["ParentCategory"] as String,
                Timestamp.now()
        )

        db.collection("Products").document(product.parentID).collection("Reviews").get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (doc in it.result) {
                    val review = Review(doc.getString("ID")!!, doc.getString("Name")!!, doc.getString("Review")!!, doc.getString("Star")!!, doc.getTimestamp("Time")!!)
                    reviews.add(review)
                }
                adapter.notifyDataSetChanged()
            } else {
                notFoundTV.visibility = View.VISIBLE
            }
            progressDialog.dismiss()
        }
        return view
    }


}
