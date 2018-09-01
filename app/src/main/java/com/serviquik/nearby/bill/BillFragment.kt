package com.serviquik.nearby.bill

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.serviquik.nearby.R
import com.serviquik.nearby.customer.ManageCustomerFragment
import com.serviquik.nearby.manageProduct.Product
import android.widget.Toast
import android.content.Intent
import android.os.Environment
import com.google.firebase.Timestamp
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap
import com.itextpdf.text.pdf.draw.LineSeparator
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.serviquik.nearby.MainActivity
import java.io.File
import java.text.SimpleDateFormat


@SuppressLint("StaticFieldLeak")
class BillFragment : Fragment() {

    val name = ManageCustomerFragment.name
    val number = ManageCustomerFragment.number
    val email = ManageCustomerFragment.email
    private val cid = ManageCustomerFragment.cid

    companion object {
        var total: Long = 0
        var count = 0
        val currentProducts = ArrayList<Bill>()
        var adapter: BillAdapter? = null

        lateinit var totalText: TextView
        lateinit var sendBtn: Button

        fun makeTotal(price: Long, quantity: Int): Long {
            return total + (price * quantity)
        }
    }

    private val products = ArrayList<Product>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MainActivity.changeToolbarTitle("Bill")
        val view = inflater.inflate(R.layout.fragment_bill, container, false)
        adapter = BillAdapter(products, context!!)
        val recyclerView: RecyclerView = view.findViewById(R.id.billRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        recyclerView.adapter = adapter

        val text = "₹" + makeTotal(0, 0)
        totalText = view.findViewById(R.id.billTotalTV)
        totalText.text = text

        sendBtn = view.findViewById(R.id.billSendBillBtn)
        sendBtn.isEnabled = false

        sendBtn.setOnClickListener {
            sendToDatabase()
        }

        progressDialog.show()

        db.collection("Products").whereEqualTo("VendorID", auth.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result) {
                    products.add(
                            Product(document["Description"] as String, document["Name"] as String, document.getLong("Price")!!, null, document.id, null, document.getString("ParentCategory")!!, document.getTimestamp("Time"))
                    )
                }
                progressDialog.dismiss()
                adapter!!.notifyDataSetChanged()
            }
        }

        return view
    }

    private fun sendToDatabase() {
        progressDialog.show()
        val data = HashMap<String, Any>()
        data["Price"] = total
        data["Time"] = Timestamp.now()
        data["Name"] = name
        data["Email"] = email
        data["Number"] = number
        data["Status"] = 5
        data["CID"] = cid

        val orderRef = db.collection("Vendors").document(auth.uid!!).collection("OrderList").document()

        orderRef.set(data).addOnSuccessListener { _ ->
            for (bill in currentProducts) {
                val items = HashMap<String, Any>()
                items["Name"] = bill.productName!!
                items["Quantity"] = bill.quantity!!
                items["Price"] = bill.productPrice!!
                orderRef.collection("Items").document().set(items)
            }
            sendMail()
        }
    }

    private fun sendMail() {
        //Todo add shop name
        val shopName = "Temp"
        var invocation = ""

        progressDialog.dismiss()

        for (bill in currentProducts) {
            invocation += "${bill.productName}\t\t\t\t\t ${bill.quantity}\t\t\t\t\t ${bill.productPrice}\n"
        }

        val textMail = "Your order from $shopName \n\n $invocation \n\n Grand total : $total"
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        i.putExtra(Intent.EXTRA_SUBJECT, "Receipt")
        i.putExtra(Intent.EXTRA_TEXT, textMail)
        try {
            startActivity(Intent.createChooser(i, "Send mail..."))
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

}
