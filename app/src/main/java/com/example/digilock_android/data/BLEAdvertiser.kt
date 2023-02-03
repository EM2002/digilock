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

class BLEAdvertiser(private val context: Context, private val log: LogView) {
    private lateinit var callback: AdvertiseCallback
    private val advertiser: BluetoothLeAdvertiser = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeAdvertiser
    private var txPower: Int = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
    private var advertiseMode: Int = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM

    fun starAdvertising(txPower: Int?, advertiseMode: Int?) {
        if (advertiseMode !== null) this.advertiseMode = advertiseMode
        if (txPower !== null) this.txPower = txPower

        val settings = (AdvertiseSettings.Builder()
            .setTxPowerLevel(this.txPower)
            .setAdvertiseMode(this.advertiseMode)).build()
        val data: AdvertiseData = (AdvertiseData.Builder()).setIncludeDeviceName(true).build()
        this.callback = object: AdvertiseCallback(){
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                log.logPrepend("Started BLE Advertising", "BLE")
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            log.logPrepend("[!!] BLUETOOTH DISABLED", "BLE")
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        advertiser.startAdvertising(settings, data, callback)
    }

    fun stopAdvertising() {
        if (ActivityCompat.checkSelfPermission(
                this.context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            log.logPrepend("[!!] BLUETOOTH DISABLED", "BLE")
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        advertiser.stopAdvertising(this.callback)
        log.logPrepend("Bluetooth advertising stopped", "BLE")
    }

}