package com.example.digilock_android.data

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat

class FindBLE {
    private lateinit var bleScanner: BluetoothLeScanner
    private var scanning = false
    private lateinit var context: Context
    private lateinit var log: LogView
    private val handler = android.os.Handler(Looper.getMainLooper())

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result !== null) {
                val ssid = result.device.address.toString()
                val rssi = result.rssi

                if (rssi < -60) return

                try { Log.i("BLE scan", "$ssid | $rssi") }
                catch (e: SecurityException) {Log.e("Exception", "Missing Permissions")}
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            try { results?.forEach { result -> Log.i("BLE scan", result.device.address.toString()) } }
            catch (e: SecurityException) {Log.e("Exception", "Missing Permissions")}
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLE Scan", "Scan failed")
        }
    }

    fun build(context: Context, log: LogView) {
        this.context = context
        this.log = log
        bleScanner = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner
    }

    fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    log.logPrepend("[!!] BLUETOOTH DISABLED", "BLE")
                    return@postDelayed
                }
                bleScanner.stopScan(scanCallback)
            }, SCAN_PERIOD)
            scanning = true

            val filter: ScanFilter = ScanFilter.Builder()
                .setDeviceAddress("A8:87:B3:B9:1C:61")
                .build()
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

            bleScanner.startScan(scanCallback)
        } else {
            scanning = false
            bleScanner.stopScan(scanCallback)
        }
    }
}

// Device scan callback.



