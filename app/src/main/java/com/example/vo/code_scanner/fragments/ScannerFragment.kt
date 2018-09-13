package com.example.vo.code_scanner.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.vo.code_scanner.R
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import org.json.JSONArray

class ScannerFragment : Fragment(){
    val TAG = "Scanner Fragment"
    private lateinit var svBarcode: SurfaceView
    private lateinit var tvBarcode: TextView
    private lateinit var btnOpenBrowser: Button

    // from vision SDK
    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    private lateinit var scanedResult: String


    companion object {
        fun newInstance() = ScannerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detector = BarcodeDetector.Builder(activity).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        // callbacks
        detector.setProcessor(object : Detector.Processor<Barcode>{
            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                processorHandler(detections)
            }
            override fun release() {}
        })

        cameraSource = CameraSource.Builder(context, detector).setRequestedPreviewSize(1024,768).setRequestedFps(25f).setAutoFocusEnabled(true).build()

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater?.inflate(R.layout.fragment_scanner, container,false)

        // init surface
        svBarcode = view.findViewById(R.id.sv_barcode)
        tvBarcode = view.findViewById(R.id.tv_barcode)
        svBarcode.holder.addCallback(object  : SurfaceHolder.Callback2{
            override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {}

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraSource.stop()
            }


            override fun surfaceCreated(holder: SurfaceHolder?) {
                if(ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraSource.start(holder)
                } else {
                    requestPermissions( arrayOf(android.Manifest.permission.CAMERA), 111)
                }

            }

        })

        // init opening browser button
        btnOpenBrowser = view.findViewById(R.id.btn_openBrowser)
        btnOpenBrowser.visibility = View.GONE
        btnOpenBrowser.setOnClickListener {
            if (!scanedResult.isEmpty()) {
                openBrowser(scanedResult)
            }
        }

        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        scanedResult = ""

    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraSource.start(svBarcode.holder)
            } else {
                Toast.makeText(activity, "Scanner won't work without permission.", Toast.LENGTH_LONG).show()
            }
        }
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
            Log.d("Scanned result", scanedResult)

            tvBarcode.post {

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

            saveScannedValue(scanedResult)
        }
    }

    private fun saveScannedValue(info : String){
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val jsonArray = JSONArray(sharedPreferences?.getString("SCANNED_INFO_LIST", "[]"))


        val arrayList : ArrayList<String> = ArrayList()

        for (i in 0 until jsonArray.length()) {
            if(jsonArray.get(i).toString().equals(info) ||
                    i > 50){
                return
            }
            arrayList.add(jsonArray.get(i) as String)
        }
        arrayList.add(0,info)

        var sharedPreffEditor = sharedPreferences?.edit()
        val new_json = JSONArray(arrayList)
        sharedPreffEditor?.putString("SCANNED_INFO_LIST",new_json.toString())
        sharedPreffEditor?.apply()

    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        cameraSource.stop()
        cameraSource.release()
    }


}
