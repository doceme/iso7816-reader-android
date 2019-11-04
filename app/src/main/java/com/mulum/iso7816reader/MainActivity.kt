package com.mulum.iso7816reader

import android.annotation.SuppressLint
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private var timer: TimerTask? = null
    private lateinit var idleStatus: String
    private lateinit var readingStatus: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        idleStatus = getString(R.string.idle)
        readingStatus = getString(R.string.reading)

        read.setOnClickListener {
            enableReaderMode()
        }

        cancel.setOnClickListener {
            disableReaderMode()
        }
    }

    private fun enableReaderMode() {
        nfcAdapter?.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null)

        status.text = readingStatus

        cancel.visibility = View.VISIBLE
        read.visibility = View.GONE

        timer = Timer(readingStatus, false).schedule(60000) {
            if (nfcAdapter?.isEnabled == true) {
                runOnUiThread {
                    disableReaderMode()
                }
            }
        }
    }

    private fun disableReaderMode(text: String = getString(R.string.idle)) {
        read.visibility = View.VISIBLE
        cancel.visibility = View.GONE

        timer?.cancel()
        timer = null
        nfcAdapter?.disableReaderMode(this)
        status.text = text
    }

    override fun onResume() {
        super.onResume()
        disableReaderMode()
    }

    public override fun onPause() {
        super.onPause()
        disableReaderMode()
    }

    @SuppressLint("SetTextI18n")
    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)

        timer?.cancel()
        timer = null

        isoDep.connect()

        val response = isoDep.transceive(Utils.hexStringToByteArray("00A4040C07F0010203040506"))

        isoDep.close()

        runOnUiThread {
            disableReaderMode("Response: ${Utils.toHex(response)}")
        }
    }

}
