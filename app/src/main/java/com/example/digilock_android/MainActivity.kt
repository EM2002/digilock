package com.example.digilock_android

import android.annotation.SuppressLint
import androidx.biometric.BiometricPrompt;
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.material.internal.ContextUtils.getActivity
import java.util.concurrent.Executor

class MainActivity : ComponentActivity() {
    private lateinit var logView: TextView
    private var logLineCounter = 0
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        logView = findViewById(R.id.textView2)
        logView.movementMethod = ScrollingMovementMethod()
    }
    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.balanced ->
                    if (checked) {
                        logLineCounter = logText(logView, "Radio balanced pressed successfully\n", "radio")
                    }
                R.id.low_power ->
                    if (checked) {
                        logLineCounter = logText(logView, "Radio power pressed successfully\n", "radio")
                    }
                R.id.tx_high ->
                    if (checked) {
                        logLineCounter = logText(logView, "Radio tx_high pressed successfully\n", "radio")
                    }
                R.id.tx_medium ->
                    if (checked) {
                        logLineCounter = logText(logView, "Radio tx_medium pressed successfully\n", "radio")
                    }
                R.id.tx_low ->
                    if (checked) {
                        logLineCounter = logText(logView, "Radio tx_low pressed successfully\n", "radio")
                    }
                R.id.tx_ultra_low ->
                    if (checked) {
                        logLineCounter = logText(logView, "Radio tx_ultra_low pressed successfully\n", "radio")
                    }
            }
        }
    }
    fun onClickBiometrics(view: View) {
        BiometricAuth.showBiometricPrompt(
            activity = getActivity(applicationContext),
            listener = this,
            cryptoObject = null,
            allowDeviceCredential = true
        )
    }

    fun onBiometricAuthenticationError(errorCode: Int, toString: String) {

    }

    fun onBiometricAuthenticationSuccess(result: BiometricPrompt.AuthenticationResult) {

    }
}

@SuppressLint("SetTextI18n")
fun logText(logView: TextView, text: String, tag: String, line: Int = 0): Int {
    Log.d(tag, text)
    logView.text = line.toString() + ": " + text + logView.text
    return line + 1
}

