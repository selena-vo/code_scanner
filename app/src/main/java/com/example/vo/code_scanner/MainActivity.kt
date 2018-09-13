package com.example.vo.code_scanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.vo.code_scanner.fragments.HistoryFragment
import com.example.vo.code_scanner.fragments.ScannerFragment
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector


class MainActivity : AppCompatActivity() {

    private lateinit var mTopToolbar: Toolbar
    private val manager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTopToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mTopToolbar)
        val scannerFragment = ScannerFragment.newInstance()
        replaceFragment(scannerFragment, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var id = item?.itemId

        when(id) {
            R.id.action_scanner -> {
                val scannerFragment = ScannerFragment.newInstance()
                replaceFragment(scannerFragment, true)
                return true;
            }
            R.id.action_history -> {
                val historyFagment = HistoryFragment.newInstance()
                replaceFragment(historyFagment, false)
                return true;
            }
        }

        return super.onOptionsItemSelected(item)
    }



    private fun replaceFragment(fragment: Fragment, isFristFragment : Boolean) {
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_holder, fragment)
        if(!isFristFragment){
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }


}


