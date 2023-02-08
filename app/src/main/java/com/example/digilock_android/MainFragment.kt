package com.example.digilock_android

import android.bluetooth.le.AdvertiseSettings
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
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


    private val bleAdvertiser: BLEAdvertiser = BLEAdvertiser()

    private fun checkPermissions() {
        Log.i("location", "checkPermission")
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT) -> {
                Log.i("permission", "Granted")
                return
            }
            else -> {
                Log.i("permission", "Not granted")
                permissions.launch(arrayOf(
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADVERTISE,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_ADMIN))
                return
            }
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
                    log.logPrepend("Authentication errored successfully", "Auth")
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
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Identification")
            .setNegativeButtonText("Cancel")
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

        val biometricLoginButton =
            view.findViewById<ImageButton>(R.id.fingerprint)
        biometricLoginButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
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
                log.logPrepend("enabled", "button")
                bleAdvertiser.starAdvertising()
            }
            else {
                checkPermissions()
                log.logPrepend("disabled", "button")
                if (bleAdvertiser.isAdvertising()) bleAdvertiser.stopAdvertising()
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
                    android.Manifest.permission.BLUETOOTH_ADVERTISE
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
}