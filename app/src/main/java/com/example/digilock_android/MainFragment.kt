package com.example.digilock_android

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.digilock_android.data.BLEAdvertiser
import com.example.digilock_android.data.LogView
import java.util.concurrent.Executor

class MainFragment : Fragment(R.layout.fragment_main), View.OnClickListener {
    private lateinit var log: LogView
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val handler = android.os.Handler(Looper.getMainLooper())
    private val DEVICE_MAC: String = "72:7A:11:41:9E:1C"
    private var unlocked: Boolean = false

    private val permissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String, Boolean> ->
            run {
                isGranted.forEach { (s, b) ->
                    if (b) Log.i("permissions", "Granted $s")
                    else Log.w("permissions", "Denied $s")
                }
            }
        }

    private var bleAdvertiser: BLEAdvertiser = BLEAdvertiser()
    private lateinit var bleScanner: BluetoothLeScanner
    private var scanning = false
    private var runThread = true
    private var testing = false

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result !== null) {
                val ssid = result.device.address.toString()
                val rssi = result.rssi

                try {
                    log.logPrepend( "$ssid | $rssi", "BLE Scan")
                    if (rssi > -60 && !testing) {
                        if (!unlocked) biometricPrompt.authenticate(promptInfo)
                        return
                    }
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

    private fun checkPermissions() {
        Log.i("location", "checkPermission")
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) -> {
                Log.i("permission", "Granted")
                return
            }
            else -> {
                Log.i("permission", "Not granted")
                permissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
                return
            }
        }
    }
    private fun scanLeDevice() {
        if (!runThread) return
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            if (ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                log.logPrepend("[!!] BLUETOOTH DISABLED", "BLE")
                return
            }

            /*handler.postDelayed({
                scanning = false
                bleScanner.stopScan(scanCallback)
            }, 10000)*/
            scanning = true

            val filter: ScanFilter = ScanFilter.Builder()
                .setDeviceAddress(DEVICE_MAC)
                .build()
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                .build()

            if(!testing) bleScanner.startScan(MutableList(1) {filter} , scanSettings, scanCallback)
            else bleScanner.startScan(scanCallback)
        } else {
            scanning = false
            bleScanner.stopScan(scanCallback)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                // TODO: Dummy daten einfügen
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when(errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            view?.setBackgroundColor(Color.parseColor("#FF4CAF50"))
                            unlocked = true
                        }
                        else -> {
                            log.logPrepend("Authentication errored with code $errorCode", "Auth")
                            view?.setBackgroundColor(Color.parseColor("#000000"))
                        }
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    log.logPrepend("Authentication successful", "Auth")
                }

                override fun onAuthenticationFailed() {
                    // TODO: Dummy daten einfügen
                    super.onAuthenticationFailed()
                    log.logPrepend("Authentication failed successfully", "Auth")
                    view?.setBackgroundColor(Color.parseColor("#FFF44336"))
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Identification")
            .setNegativeButtonText("Positive")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val logView = view.findViewById<TextView>(R.id.logView)
        logView.movementMethod = ScrollingMovementMethod()
        this.log = LogView(logView)
        bleAdvertiser.build(this.requireContext(), this.log)
        bleScanner = (this.requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner

        val biometricLoginButton =
            view.findViewById<ImageButton>(R.id.fingerprint)
        biometricLoginButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        view.findViewById<Button>(R.id.testing).setOnClickListener {
            testing = !testing
            log.logPrepend("Testing: $testing", "State")
        }

        view.findViewById<RadioButton>(R.id.balanced).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.low_power).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.tx_ultra_low).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.tx_low).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.tx_medium).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.tx_high).setOnClickListener(this)

        view.findViewById<ToggleButton>(R.id.toggleButton).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkPermissions()
                log.logPrepend("started scanning", "button")
                if (runThread) thread.start()
                else runThread = true
            } else {
                checkPermissions()
                log.logPrepend("stopped scanning", "button")
                //if (bleAdvertiser.isAdvertising()) bleAdvertiser.stopAdvertising()
                runThread = false
            }
        }
        return view
    }

    override fun onClick(view: View) {
        checkPermissions()
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) -> run {
                    when (view.getId()) {
                        R.id.balanced ->
                            if (checked) {
                                if (bleAdvertiser.isAdvertising()) bleAdvertiser.stopAdvertising()
                                log.logPrepend("Radio balanced pressed successfully", "Radio")
                                bleAdvertiser.setPowerMode( AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                            }
                        R.id.low_power ->
                            if (checked) {
                                if (bleAdvertiser.isAdvertising()) bleAdvertiser.stopAdvertising()
                                log.logPrepend("Radio power pressed successfully", "radio")
                                bleAdvertiser.setPowerMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                            }
                        R.id.tx_high ->
                            if (checked) {
                                if (bleAdvertiser.isAdvertising()) bleAdvertiser.stopAdvertising()
                                log.logPrepend("Radio tx_high pressed successfully", "radio")
                                bleAdvertiser.setTXPower(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                            }
                        R.id.tx_medium ->
                            if (checked) {
                                if (bleAdvertiser.isAdvertising()) bleAdvertiser.stopAdvertising()
                                log.logPrepend("Radio tx_medium pressed successfully", "radio")
                                bleAdvertiser.setTXPower(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                            }
                        R.id.tx_low ->
                            if (checked) {
                                if (bleAdvertiser.isAdvertising()) bleAdvertiser.stopAdvertising()
                                log.logPrepend("Radio tx_low pressed successfully", "radio")
                                bleAdvertiser.setTXPower(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                            }
                        R.id.tx_ultra_low ->
                            if (checked) {
                                if (bleAdvertiser.isAdvertising()) bleAdvertiser.stopAdvertising()
                                log.logPrepend("Radio tx_ultra_low pressed successfully", "radio")
                                bleAdvertiser.setTXPower(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
                            }
                    }
                }
            }
        }
    }

    private val thread: Thread = Thread {
        run {
            try {
                while(true) {
                    Thread.sleep(2000)
                    if(runThread) handler.post { scanLeDevice() }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}