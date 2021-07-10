package de.rki.covpass.checkapp.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log


public interface ScanReceiver {
    public fun scanResult(result: String)
}

public class HardwareScanner(public val receiver: ScanReceiver) {

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when {
                intent.hasExtra("com.symbol.datawedge.data_string") -> {
                    // Zebra DataWedge
                    intent.getStringExtra("com.symbol.datawedge.data_string")?.let { receiver.scanResult(it) }
                }
                intent.hasExtra("SCAN_BARCODE1") -> {
                    // NewLand
                    val barcode = intent.getStringExtra("SCAN_BARCODE1")?.trim()
                    barcode?.let { receiver.scanResult(it) }
                }
                intent.hasExtra("EXTRA_BARCODE_DECODING_DATA") -> {
                    // Bluebird
                    val barcode = intent.getByteArrayExtra("EXTRA_BARCODE_DECODING_DATA")?.let { String(it).trim() }
                    barcode?.let { receiver.scanResult(it) }
                }
                intent.hasExtra("barocode") -> {
                    // Intent receiver for LECOM-manufactured hardware scanners
                    val barcode = intent?.getByteArrayExtra("barocode") // sic!
                    val barocodelen = intent?.getIntExtra("length", 0)
                    val barcodeStr = barcode?.let { String(it, 0, barocodelen) }
                    barcodeStr?.let { receiver.scanResult(it) }
                }
            }
        }
    }

    public fun start(ctx: Context) {
        val filter = IntentFilter()
        filter.addAction("scan.rcv.message")  // LECOM
        filter.addAction("eu.pretix.SCAN")  // Zebra DataWedge
        filter.addAction("kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_DECODING_DATA")  // Bluebird
        filter.addAction("nlscan.action.SCANNER_RESULT")  // NewLand
        ctx.registerReceiver(scanReceiver, filter)
    }

    public fun stop(ctx: Context) {
        try {
            ctx.unregisterReceiver(scanReceiver)
        } catch (exception: Exception) {
            // Scanner has probably been already stopped elsewhere.
        }
    }
}

public fun hasHardwareScanner(): Boolean {
    Log.i("HardwareScanner", "Detecting brand='${Build.BRAND}' model='${Build.MODEL}'")
    return when (Build.BRAND) {
        "Zebra" -> Build.MODEL.startsWith("TC") || Build.MODEL.startsWith("M") || Build.MODEL.startsWith("CC6")
        "Bluebird" -> Build.MODEL.startsWith("EF")
        "NewLand" -> Build.MODEL.startsWith("NQ")
        else -> false
    }
}
