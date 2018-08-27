package com.serviquik.nearby.manageProduct

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.card.MaterialCardView
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.R
import com.serviquik.nearby.review.ReviewsFragment
import com.squareup.picasso.Picasso
import com.willy.ratingbar.ScaleRatingBar
import java.util.*
import kotlin.collections.HashMap

class ProductsAdapter(private val products: ArrayList<Product>, private val fragmentManager: FragmentManager, val context: Context) : RecyclerView.Adapter<Viewholder>() {

    private val colors = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        colors.add("#FF9800")
        colors.add("#FF5722")
        colors.add("#009688")
        colors.add("#03A9F4")
        colors.add("#673AB7")
        colors.add("#F44336")
        colors.add("#3F51B5")
        return Viewholder(LayoutInflater.from(parent.context).inflate(R.layout.card_products, parent, false))

    }

    override fun getItemCount(): Int {
        return products.size
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val product = products[position]

        val index = Random().nextInt(colors.size)
        holder.background.setCardBackgroundColor(Color.parseColor(colors[index]))
        holder.descriptionTV.text = product.description
        holder.titleTV.text = product.title
        holder.priceTV.text = "₹ " + product.price
        try {
            Picasso.get().load(Uri.parse(product.images!![0])).into(holder.imageView)
        } catch (e: NullPointerException) {
        } catch (e: IndexOutOfBoundsException) {
        }
        if (product.rating != null) {
            holder.ratingBar.rating = product.rating.toFloat()
        } else {
            holder.ratingBar.rating = 2.5f
        }

        holder.background.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("Description", product.description)
            bundle.putString("Title", product.title)
            bundle.putLong("Price", product.price)
            bundle.putStringArrayList("Images", product.images)
            bundle.putString("ParantID", product.parentID)
            bundle.putString("Rating", product.rating)
            bundle.putString("ParentCategory", product.parantCategory)

            val ft = fragmentManager.beginTransaction()
            val fragment = ReviewsFragment()
            fragment.arguments = bundle
            ft.replace(R.id.container, fragment, "ReviewFragment")
            ft.addToBackStack("ProductFragment")
            ft.commit()
        }

        holder.editBtn.setOnClickListener { _ ->
            val inflater1 = LayoutInflater.from(context)
            val dialog = inflater1.inflate(R.layout.product_add, null)
            val dialogeBuilder = AlertDialog.Builder(context)
            dialogeBuilder.setView(dialog)
            dialogeBuilder.setTitle("Edit Data")

            dialog.findViewById<Button>(R.id.productAddSelectImage).visibility = View.GONE
            dialog.findViewById<Spinner>(R.id.productAddSpinner).visibility = View.GONE

            val nameEt = dialog.findViewById<EditText>(R.id.productAddName)
            val descriptionEt = dialog.findViewById<EditText>(R.id.productAddDescription)
            val priceEt = dialog.findViewById<EditText>(R.id.productAddPrice)

            nameEt.text = SpannableStringBuilder(product.title)
            priceEt.text = SpannableStringBuilder(product.price.toString())
            descriptionEt.text = SpannableStringBuilder(product.description)

            dialogeBuilder.setPositiveButton("Update") { _, _ ->
                val description = descriptionEt.text.toString()
                val name = nameEt.text.toString()
                val price = Integer.parseInt(priceEt.text.toString())

                val data = HashMap<String, Any>()
                data["Name"] = name
                data["Description"] = description
                data["Price"] = price
                FirebaseFirestore.getInstance().collection("Products").document(product.parentID).update(data).addOnCompleteListener {
                    holder.descriptionTV.text = description
                    holder.titleTV.text = name
                    holder.priceTV.text = "₹ $price"
                }
            }

            dialogeBuilder.show()
        }
    }

}

class Viewholder(view: View) : RecyclerView.ViewHolder(view) {
    val background: MaterialCardView = view.findViewById(R.id.productCardBackground)
    val titleTV: TextView = view.findViewById(R.id.productCardTitle)
    val descriptionTV: TextView = view.findViewById(R.id.productCardDescription)
    val priceTV: TextView = view.findViewById(R.id.productCardPrice)
    val imageView: ImageView = view.findViewById(R.id.productCardImage)
    val editBtn: Button = view.findViewById(R.id.productCardEdit)
    val ratingBar: ScaleRatingBar = view.findViewById(R.id.productsCardRatingBar)
}