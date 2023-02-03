package com.example.digilock_android

import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.ToggleButton
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

    private lateinit var bleAdvertiser: BLEAdvertiser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
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

        val biometricLoginButton =
            view.findViewById<ImageButton>(R.id.fingerprint)
        biometricLoginButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        bleAdvertiser = BLEAdvertiser(this.requireContext(), this.log)

        view.findViewById<RadioButton>(R.id.balanced).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.low_power).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.tx_ultra_low).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.tx_low).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.tx_medium).setOnClickListener(this)
        view.findViewById<RadioButton>(R.id.tx_high).setOnClickListener(this)

        view.findViewById<ToggleButton>(R.id.toggleButton).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                log.logPrepend("enabled", "button")
                bleAdvertiser.starAdvertising(null, null)
            }
            else {
                log.logPrepend("disabled", "button")
                bleAdvertiser.stopAdvertising()
            }
        }

        return view
    }

    override fun onClick(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.balanced ->
                    if (checked) {
                        bleAdvertiser.stopAdvertising()
                        log.logPrepend("Radio balanced pressed successfully", "Radio")
                        bleAdvertiser.starAdvertising(null, AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    }
                R.id.low_power ->
                    if (checked) {
                        log.logPrepend("Radio power pressed successfully", "radio")
                        bleAdvertiser.starAdvertising(null, AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                    }
                R.id.tx_high ->
                    if (checked) {
                        bleAdvertiser.stopAdvertising()
                        log.logPrepend("Radio tx_high pressed successfully", "radio")
                        bleAdvertiser.starAdvertising(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH, null)
                    }
                R.id.tx_medium ->
                    if (checked) {
                        bleAdvertiser.stopAdvertising()
                        log.logPrepend( "Radio tx_medium pressed successfully", "radio")
                        bleAdvertiser.starAdvertising(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM, null)
                    }
                R.id.tx_low ->
                    if (checked) {
                        bleAdvertiser.stopAdvertising()
                        log.logPrepend("Radio tx_low pressed successfully", "radio")
                        bleAdvertiser.starAdvertising(AdvertiseSettings.ADVERTISE_TX_POWER_LOW, null)
                    }
                R.id.tx_ultra_low ->
                    if (checked) {
                        bleAdvertiser.stopAdvertising()
                        log.logPrepend("Radio tx_ultra_low pressed successfully", "radio")
                        bleAdvertiser.starAdvertising(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW, null)
                    }
            }
        }
    }
}