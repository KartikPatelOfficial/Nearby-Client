package com.serviquik.nearby.bill

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.serviquik.nearby.R
import com.serviquik.nearby.manageProduct.Product

@SuppressLint("SetTextI18n")
class BillAdapter(private val products: ArrayList<Product>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    private val productName = ArrayList<String>()

    lateinit var adapter: ArrayAdapter<String>

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        for (name in products) {
            productName.add(name.title)
        }
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, productName)

        return ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.card_bill_add, p0, false))
    }

    override fun getItemCount(): Int {
        return BillFragment.currentProducts.size + 1
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.spinner.adapter = adapter
        p0.addBtn.setOnClickListener {
            val price = products[p0.spinner.selectedItemPosition].price
            val quantityText = p0.quantityET.text.toString()
            if (TextUtils.isEmpty(quantityText)) {
                p0.quantityET.error = "please enter quantity"
                return@setOnClickListener
            }
            val quantity = Integer.parseInt(quantityText)
            BillFragment.currentProducts.add(Bill(products[p0.spinner.selectedItemPosition].title, price, quantity))
            BillFragment.count += 1
            BillFragment.adapter!!.notifyItemChanged(p1 + 1)
            BillFragment.total = BillFragment.makeTotal(price, quantity)
            BillFragment.totalText.text = "₹ ${BillFragment.total}"
            BillFragment.sendBtn.isEnabled = true
        }
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val spinner: Spinner = view.findViewById(R.id.billCardSpinner)
    val quantityET: EditText = view.findViewById(R.id.billCardET)
    val addBtn: Button = view.findViewById(R.id.billCardAddBtn)
}