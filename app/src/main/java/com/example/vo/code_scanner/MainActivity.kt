package com.example.vo.code_scanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
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

    private lateinit var svBarcode: SurfaceView
    private lateinit var tvBarcode: TextView
    private lateinit var btnOpenBrowser: Button

    // from vision SDK
    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    private lateinit var scanedResult: String

    private lateinit var mTopToolbar: Toolbar
    private var isFragmentScannerLoaded = true;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTopToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mTopToolbar)

        svBarcode = findViewById(R.id.sv_barcode)
        tvBarcode = findViewById(R.id.tv_barcode)
        btnOpenBrowser = findViewById(R.id.btn_openBrowser)
        btnOpenBrowser.visibility = View.GONE
        scanedResult = ""

        detector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        // callbacks
        detector.setProcessor(object : Detector.Processor<Barcode>{
            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                processorHandler(detections)
            }
            override fun release() {}
        })

        cameraSource = CameraSource.Builder(this, detector).setRequestedPreviewSize(1024,768).setRequestedFps(25f).setAutoFocusEnabled(true).build()
        svBarcode.holder.addCallback(object  : SurfaceHolder.Callback2{
            override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {}

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraSource.stop()
            }


            override fun surfaceCreated(holder: SurfaceHolder?) {
                if(ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraSource.start(holder)
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.CAMERA), 111)
                }

            }

        })

        btnOpenBrowser.setOnClickListener {
            if (!scanedResult.isEmpty()) {
                openBrowser(scanedResult)
            }
        }
        showScannerFragment()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraSource.start(svBarcode.holder)
            } else {
                Toast.makeText(this, "Scanner won't work without permission.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        cameraSource.stop()
        cameraSource.release()
    }
    private fun openBrowser(url:String){
        val uri = Uri.parse(url)
        val intents = Intent(Intent.ACTION_VIEW, uri)
        val b = Bundle()
        b.putBoolean("new_window", true)
        intents.putExtras(b)
        startActivity(intents)
    }
    private fun processorHandler(detections: Detector.Detections<Barcode>?){
        var barcodes = detections?.detectedItems
        if(barcodes!!.size() > 0) {
            scanedResult = barcodes.valueAt(0).displayValue
            tvBarcode.post {
                Log.d("Scanned result", scanedResult)
                tvBarcode!!.text = scanedResult

            }
            if(URLUtil.isValidUrl(scanedResult)){
                btnOpenBrowser.post {
                    btnOpenBrowser.visibility = View.VISIBLE
                }
            } else {
                btnOpenBrowser.post {
                    btnOpenBrowser.visibility = View.GONE
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var id = item?.itemId

        when(id) {
            R.id.action_scanner -> {
                Toast.makeText(this, "action_scanner", Toast.LENGTH_LONG).show();
                showScannerFragment()
                return true;
            }
            R.id.action_history -> {
                Toast.makeText(this, "action_history", Toast.LENGTH_LONG).show();
                showHistoryFragment()
                return true;
            }
        }

        return super.onOptionsItemSelected(item)
    }

    val manager = supportFragmentManager
    //TODO: add Fragments
    fun showScannerFragment(){
        val transaction = manager.beginTransaction()
        val fragment = ScannerFragment()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        isFragmentScannerLoaded = true
    }

    fun showHistoryFragment(){
        val transaction = manager.beginTransaction()
        val fragment = HistoryFragment()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        isFragmentScannerLoaded = false
    }
}


