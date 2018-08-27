package com.serviquik.nearby

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.serviquik.nearby.manageProduct.ManageProductsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import tourguide.tourguide.TourGuide
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import com.serviquik.nearby.customer.ManageCustomerFragment
import com.serviquik.nearby.offer.OfferFragment
import com.serviquik.nearby.orderList.OrderListFragment
import com.serviquik.nearby.profile.ProfileFragment


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val titles = ArrayList<String>()

    private lateinit var tip: TourGuide

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    @Suppress("PrivatePropertyName")
    private val PREFS_NAME = "MyPrefsFile"

    private var settings: SharedPreferences? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var toolbarStatic: Toolbar? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var profilePictureTv: ImageView

        fun changeToolbarTitle(title: String) {
            toolbarStatic!!.title = title
        }

        fun changeNavBarProfilePicture(path: String) {
            Picasso.get().load(path).into(profilePictureTv)
        }

    }

    private lateinit var progressDialog: ProgressDialog


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbarStatic = toolbar
        supportActionBar!!.title = "Order List"

        settings = getSharedPreferences(PREFS_NAME, 0)

        progressDialog = ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        addDataInDrawer()

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1);
        }

        titles.add(getString(R.string.order_list))
        titles.add(getString(R.string.manage_customer))
        titles.add(getString(R.string.manage_Product))
        titles.add(getString(R.string.review))
        titles.add(getString(R.string.offer))

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(p0: Int) {}

            override fun onDrawerSlide(p0: View, p1: Float) {}

            override fun onDrawerClosed(p0: View) {}

            override fun onDrawerOpened(p0: View) {

                if (settings!!.getBoolean("my_first_time", true)) {

                    tip = TourGuide.create(this@MainActivity) {
                        toolTip {
                            title { "Profile" }
                            description { "Edit/Update your profile.." }
                        }
                        overlay {
                            backgroundColor { Color.parseColor("#AA2196F3") }
                            disableClick(false)
                        }
                    }.playOn(profilePictureTv)

                    tip.cleanUp()

                    settings!!.edit().putBoolean("my_first_time", false).apply()
                }

            }

        })

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        changeFragment(OrderListFragment())

    }

    private fun addDataInDrawer() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        val view = navigationView.getHeaderView(0)

        profilePictureTv = view.findViewById(R.id.navbarProfilePicture)
        val nameTV: TextView = view.findViewById(R.id.navbarName)
        val emailTV: TextView = view.findViewById(R.id.navbarEmail)

        profilePictureTv.setOnClickListener {
            changeFragment(ProfileFragment())
            drawer_layout.closeDrawers()
        }

        db.collection("Vendors").document(auth.currentUser!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val document = it.result

                val profilePicURL = document["ProfilePicture"]

                if (profilePicURL != null) {
                    Picasso.get().load(Uri.parse(profilePicURL as String)).into(profilePictureTv)
                } else {
                    val authURL: Uri? = auth!!.currentUser!!.photoUrl
                    if (authURL != null) {
                        Picasso.get().load(authURL).into(profilePictureTv)
                    }
                }

                val name = document.getString("Name")
                val email = document.getString("Email")

                runOnUiThread {
                    nameTV.text = name
                    emailTV.text = email
                }
            }
            progressDialog.dismiss()
        }

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawerOrderList -> {
                toolbar.title = titles[0]
                changeFragment(OrderListFragment())
                drawer_layout.closeDrawers()
            }
            R.id.drawerManageCustomer -> {
                toolbar.title = titles[1]
                changeFragment(ManageCustomerFragment())
                drawer_layout.closeDrawers()
            }
            R.id.drawerManageProduct -> {
                toolbar.title = titles[2]
                changeFragment(ManageProductsFragment())
                drawer_layout.closeDrawers()
            }
            R.id.drawerOffer -> {
                toolbar.title = titles[4]
                changeFragment(OfferFragment())
                drawer_layout.closeDrawers()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun changeFragment(fragment: Fragment) {
        val ft = supportFragmentManager!!.beginTransaction()
        ft.replace(R.id.container, fragment)
        ft.commit()

    }

}
