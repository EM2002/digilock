package com.example.digilock_android.data

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class BLEAdvertiser() {
    private lateinit var context: Context
    private lateinit var log: LogView
    private lateinit var callback: AdvertiseCallback
    private lateinit var advertiser: BluetoothLeAdvertiser
    private var powerMode: Int = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
    private var txPower: Int = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
    private var status: Boolean = false

    fun build(context: Context, log: LogView) {
        this.context = context
        this.log = log
        advertiser = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeAdvertiser
    }
    fun starAdvertising(shouldLog: Boolean = true) {
        val settings = (AdvertiseSettings.Builder()
            .setTxPowerLevel(this.txPower)
            .setAdvertiseMode(this.powerMode)).build()
        val data: AdvertiseData = (AdvertiseData.Builder()).setIncludeDeviceName(true).build()
        this.callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                if (shouldLog) log.logPrepend("Started BLE Advertising with tx_power $txPower and power_mode $powerMode", "BLE")
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            log.logPrepend("[!!] BLUETOOTH DISABLED", "BLE")
            return
        }

        advertiser.startAdvertising(settings, data, callback)
        status = true
    }

    fun stopAdvertising(shouldLog: Boolean = true) {
        if (ActivityCompat.checkSelfPermission(
                this.context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            log.logPrepend("[!!] BLUETOOTH DISABLED", "BLE")
            return
        }
        advertiser.stopAdvertising(this.callback)
        if (shouldLog) log.logPrepend("Bluetooth advertising stopped", "BLE")
        status = false
    }
    fun setPowerMode(powerMode: Int) {
        this.powerMode = powerMode
        if (this.status) this.stopAdvertising(false)
        this.starAdvertising()
    }
    fun setTXPower(txPower: Int) {
        this.txPower = txPower
        if (this.status) this.stopAdvertising(false)
        this.starAdvertising()
    }

    fun isAdvertising(): Boolean {
        return status
    }
}