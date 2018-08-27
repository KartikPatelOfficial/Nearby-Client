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
import java.io.File
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

    private lateinit var progressDialog :ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(context,ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

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

        val mColorAccent = BaseColor(0, 153, 204, 255)
        val mHeadingFontSize = 20.0f

        val mOrderDetailsTitleChunk = Chunk("Order Details")
        val mOrderDetailsTitleParagraph = Paragraph(mOrderDetailsTitleChunk)
        mOrderDetailsTitleParagraph.alignment = Element.ALIGN_CENTER
        document.add(mOrderDetailsTitleParagraph)

        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = BaseColor(0, 0, 0, 68)

        val mOrderIdChunk = Chunk("Order No:")
        val mOrderIdParagraph = Paragraph(mOrderIdChunk)
        document.add(mOrderIdParagraph)

        document.add(Paragraph(""))
        document.add(Chunk(lineSeparator))
        document.add(Paragraph(""))

        val table = PdfPTable(4)
        var isFirst = true

        document.add(Paragraph(""))
        document.add(Paragraph(""))

        for (cell in currentProducts) {

            if (isFirst) {
                table.addCell(getCell("Name"))
                table.addCell(getCell("Price/product"))
                table.addCell(getCell("Quantity"))
                table.addCell(getCell("Price"))
                table.addCell(getCell(""))
                table.addCell(getCell(""))
                table.addCell(getCell(""))
                table.addCell(getCell(""))
                isFirst = false
            }

            table.addCell(getCell(cell.productName!!))
            table.addCell(getCell(cell.productPrice.toString()))
            table.addCell(getCell(cell.quantity.toString()))
            table.addCell(getCell((cell.quantity!! * cell.productPrice!!).toString()))
        }

        document.add(Paragraph(""))
        document.add(Chunk(lineSeparator))
        document.add(Paragraph(""))

        val totalChunk = Chunk(total.toString())
        val totalParagraph = Paragraph(totalChunk)
        totalParagraph.alignment = Element.ALIGN_RIGHT
        document.add(totalParagraph)

        document.add(Chunk(lineSeparator))
        document.add(Paragraph(""))

        val grandTotalChunk = Chunk((total * .5).toString())
        val grandTotalParagraph = Paragraph(grandTotalChunk)
        grandTotalParagraph.alignment = Element.ALIGN_RIGHT
        document.add(grandTotalParagraph)

        document.add(table)
        document.close()
    }

    private fun getCell(string: String): PdfPCell {
        val cell = PdfPCell(Phrase(string))
        cell.border = Rectangle.NO_BORDER
        return cell
    }

}
