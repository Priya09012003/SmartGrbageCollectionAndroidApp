package com.example.smartgrbagecollectionandroidapp

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var storeVerificationId: String? = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        val GetOtp: Button = findViewById(R.id.getotp)
        val Verify: Button = findViewById(R.id.verifybtn)
        val PhoneNumber: EditText = findViewById(R.id.phonenumber)
        val VerifyNumber: EditText = findViewById(R.id.EnterOtp)

        auth = FirebaseAuth.getInstance()

        GetOtp.setOnClickListener {
            startPhoneNumberVerification(PhoneNumber.text.toString())
        }

        Verify.setOnClickListener {
            verifyPhoneNumberWithCode(storeVerificationId, VerifyNumber.text.toString())
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: $credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Handle invalid credentials
                } else if (e is FirebaseTooManyRequestsException) {
                    // Handle too many requests
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential: Success")
                    val user = task.result?.user
                    Toast.makeText(this, "Welcome: ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    updateUI(user)
                } else {
                    Log.w(TAG, "SignInWithCredential: Failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // Handle invalid credentials
                    }
                }
            }
    }

    private fun updateUI(user: FirebaseUser? = auth.currentUser) {
        // Implement UI updates based on the user's authentication status
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
