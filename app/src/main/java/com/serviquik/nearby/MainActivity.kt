package com.serviquik.nearby

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TextView
import com.serviquik.nearby.manageProduct.ManageProductsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.support.v4.content.ContextCompat
import android.view.WindowManager


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val titles = ArrayList<String>()
//    lateinit var toolbarTitle: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
//        toolbarTitle = findViewById(R.id.toolbar_title)

        titles.add(getString(R.string.order_list))
        titles.add(getString(R.string.manage_customer))
        titles.add(getString(R.string.manage_Product))
        titles.add(getString(R.string.review))
        titles.add(getString(R.string.offer))

        val window = window

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
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
            }
            R.id.drawerManageCustomer -> {
                toolbar.title = titles[1]
                changeFragment(ManageCustomerFragment())
            }
            R.id.drawerManageProduct -> {
                toolbar.title = titles[2]
                changeFragment(ManageProductsFragment())
            }
            R.id.drawerReviews -> {
                toolbar.title = titles[3]
            }
            R.id.drawerOffer -> {
                toolbar.title = titles[4]
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
