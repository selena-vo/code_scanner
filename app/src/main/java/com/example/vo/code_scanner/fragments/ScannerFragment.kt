package com.example.vo.code_scanner.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vo.code_scanner.R

class ScannerFragment : Fragment(){
    val TAG = "Scanner Fragment"



    companion object {
        fun newInstance() = ScannerFragment()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_scanner, container,false)
    }
}
