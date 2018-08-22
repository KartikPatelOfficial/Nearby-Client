package com.serviquik.nearby.bill

import android.annotation.SuppressLint
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
import android.util.Log
import com.google.firebase.Timestamp
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.draw.LineSeparator
import com.itextpdf.text.Paragraph
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat


@SuppressLint("StaticFieldLeak")
class BillFragment : Fragment() {

    val name = ManageCustomerFragment.name
    val number = ManageCustomerFragment.number
    val email = ManageCustomerFragment.email


    companion object {
        var total: Long = 0
        var count = 0
        val currentProducts = ArrayList<Bill>()
        var adapter: BillAdapter? = null

        lateinit var totalText: TextView

        fun makeTotal(price: Long, quantity: Int): Long {
            return total + (price * quantity)
        }
    }

    private val products = ArrayList<Product>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bill, container, false)
        adapter = BillAdapter(products, context!!)
        val recyclerView: RecyclerView = view.findViewById(R.id.billRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        recyclerView.adapter = adapter

        val text = "₹" + makeTotal(0, 0)
        totalText = view.findViewById(R.id.billTotalTV)
        totalText.text = text

        view.findViewById<Button>(R.id.billSendBillBtn).setOnClickListener {
            sendToDatabase()
        }

        db.collection("Products").whereEqualTo("VendorID", auth.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result) {
                    products.add(
                            Product(document["Description"] as String, document["Name"] as String, document.getLong("Price")!!, null, document.id, null, document.getString("ParentCategory")!!, document.getTimestamp("Time"))
                    )
                }
                adapter!!.notifyDataSetChanged()
            }
        }

        return view
    }

    private fun sendToDatabase() {
        val data = HashMap<String, Any>()
        data["Price"] = total
        data["Time"] = Timestamp.now()

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
            createPdf()
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createPdf() {


        val pdfFolder = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "pdfdemo")
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir()
        }

        val date = Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(date)

        val myFile = File("$pdfFolder$timeStamp.pdf")

        val output = FileOutputStream(myFile)

        val document = Document()

        PdfWriter.getInstance(document, output)

        document.open()

        document.add(Paragraph("Hey there"))
        document.add(Paragraph("Base camp"))

        document.close()

    }

}
