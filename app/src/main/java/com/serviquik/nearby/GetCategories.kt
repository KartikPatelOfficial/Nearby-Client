package com.serviquik.nearby

import com.google.firebase.firestore.CollectionReference

class GetCategories(val db: CollectionReference, val id: String) {

    var listener: CategoryListner? = null

    fun getCategory() {
        db.whereEqualTo("Id", id).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (!it.result.isEmpty) {
                    val arrayList = ArrayList<Category>()
                    for (doc in it.result) {
                        arrayList.add(Category(doc.getString("Name")!!, doc.id))
                    }
                    listener!!.onGetCategory(arrayList)
                } else {
                    listener!!.onGetCategory(null)
                }
            } else {
                listener!!.onGetCategory(null)
            }
        }
    }

    interface CategoryListner {
        fun onGetCategory(categories: ArrayList<Category>?)
    }


}