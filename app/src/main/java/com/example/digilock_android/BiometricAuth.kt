package com.example.digilock_android

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.util.Log
import androidx.core.content.ContextCompat


class BiometricAuth {
    fun hasBiometricCapability(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate()
    }
    fun isBiometricReady(context: Context) =
        hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS
    fun setBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String,
        allowDeviceCredential: Boolean
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        // Use Device Credentials if allowed, otherwise show Cancel Button
        builder.apply {
            if (allowDeviceCredential) setDeviceCredentialAllowed(true)
            else setNegativeButtonText("Cancel")
        }

        return builder.build()
    }

    fun initBiometricPrompt(
        activity: MainActivity,
        listener: MainActivity
    ): BiometricPrompt {
        // 1
        val executor = ContextCompat.getMainExecutor(activity)

        // 2
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticationError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(this.javaClass.simpleName, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticationSuccess(result)
            }
        }

        // 3
        return BiometricPrompt(activity, executor, callback)
    }

    companion object {
        fun showBiometricPrompt(
            biometricAuth: BiometricAuth, title: String = "Biometric Authentication",
            subtitle: String = "Enter biometric credentials to proceed.",
            description: String = "Input your Fingerprint or FaceID to ensure it's you!",
            activity: MainActivity,
            listener: MainActivity,
            cryptoObject: BiometricPrompt.CryptoObject? = null,
            allowDeviceCredential: Boolean = false
        ) {
            // 1
            val promptInfo = biometricAuth.setBiometricPromptInfo(
                title,
                subtitle,
                description,
                allowDeviceCredential
            )

            // 2
            val biometricPrompt = biometricAuth.initBiometricPrompt(activity, listener)

            // 3
            biometricPrompt.apply {
                if (cryptoObject == null) authenticate(promptInfo)
                else authenticate(promptInfo, cryptoObject)
            }
        }
    }

}