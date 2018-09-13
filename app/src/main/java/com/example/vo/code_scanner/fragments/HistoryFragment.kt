package com.example.vo.code_scanner.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.vo.code_scanner.R
import org.json.JSONArray

class HistoryFragment : Fragment() {
    val TAG = "History Fragment"
    private lateinit var lvHistory: ListView

    companion object {
        fun newInstance() = HistoryFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_history,container,false)

        lvHistory = view.findViewById(R.id.lv_history)
        val dataArray = loadScannedInfoList()
        val adapter   = ArrayAdapter(activity,android.R.layout.simple_list_item_1, dataArray)
        lvHistory.adapter = adapter

        lvHistory.setOnItemClickListener{ _,view,_,_ ->
            val textView = view.findViewById<TextView>(android.R.id.text1)
            val value = textView.text.toString()
            if (URLUtil.isValidUrl(value)) {
                openBrowser(value)
            } else {
                copyToClipboard(value)
                Toast.makeText(context, "Copied ${textView.text} to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    fun loadScannedInfoList() : ArrayList<String> {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val jsonArray = JSONArray(sharedPreferences?.getString("SCANNED_INFO_LIST", "[]"))
        val arrayList : ArrayList<String> = ArrayList()

        for (i in 0 until jsonArray.length()) {
            arrayList.add(jsonArray.get(i) as String)
        }

        return arrayList
    }

    private fun openBrowser(url:String){
        val uri = Uri.parse(url)
        val intents = Intent(Intent.ACTION_VIEW, uri)
        val b = Bundle()
        b.putBoolean("new_window", true)
        intents.putExtras(b)
        startActivity(intents)
    }

    private fun copyToClipboard(value:String){
        var mManager:ClipboardManager =
                activity?.application?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        mManager.primaryClip = ClipData.newPlainText("value", value)
    }
}
