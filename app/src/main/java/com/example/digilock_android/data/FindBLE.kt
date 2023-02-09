package com.example.digilock_android.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context

class FindBLE {
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanning = false
    private lateinit var context: Context
    private lateinit var log: LogView
    private val handler = android.os.Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    fun build(context: Context, log: LogView) {
        this.context = context
        this.log = log
        bluetoothLeScanner = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner
    }
    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }
}

private val leDeviceListAdapter = LeDeviceListAdapter()

class LeDeviceListAdapter {
    fun notifyDataSetChanged() {
        TODO("Not yet implemented")
    }

    fun addDevice(bluetoothDevice: BluetoothDevice?) {
        TODO("Not yet implemented")
    }

}

// Device scan callback.
private val leScanCallback: ScanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        TODO("Not yet implemented")
    }
}



