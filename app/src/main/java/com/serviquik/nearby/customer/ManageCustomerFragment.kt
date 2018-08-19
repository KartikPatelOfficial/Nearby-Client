package com.serviquik.nearby.customer


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.R


class ManageCustomerFragment : Fragment() {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_customer, container, false)

        view.findViewById<FloatingActionButton>(R.id.manageCustomerRecyclerViewFAB).setOnClickListener {

        }

        return view
    }


}
