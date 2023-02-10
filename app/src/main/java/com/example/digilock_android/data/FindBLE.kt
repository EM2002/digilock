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

    private val SCAN_PERIOD: Long = 60000

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result !== null) {
                val ssid = result.device.address.toString()
                val rssi = result.rssi

                try {
                    log.logPrepend( "$ssid | $rssi", "BLE Scan")
                    if (rssi > -60) return
                }
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
        while (scanning) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                log.logPrepend("[!!] BLUETOOTH DISABLED", "BLE")
                return
            }

            val filter: ScanFilter = ScanFilter.Builder()
                .setDeviceAddress("7F:D3:C0:27:EF:EE")
                .build()
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

            bleScanner.startScan(MutableList(1) {filter}, scanSettings, scanCallback)
        }
        bleScanner.stopScan(scanCallback)
    }

    fun stopScanning() {
        this.scanning = false
    }
}

// Device scan callback.



