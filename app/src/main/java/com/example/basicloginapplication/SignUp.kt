package com.example.basicloginapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider


class SignUp : Fragment() {
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_sign_up, container, false)
        val imageButton=view.findViewById<ImageButton>(R.id.imageButtonBackSignup)
        val textView=view.findViewById<TextView>(R.id.textViewLoginToRegister)
        val email=view.findViewById<TextView>(R.id.editTextEmailLogin)
        val password=view.findViewById<TextView>(R.id.editTextPasswordLogin)
        val name=view.findViewById<TextView>(R.id.editTextName)
        val signUpButton=view.findViewById<Button>(R.id.buttonLoginLogin)
        val signUpGoogle=view.findViewById<ImageButton>(R.id.imageButtonGoogle)
        imageButton.setOnClickListener(View.OnClickListener { Utils.switchToFragment(activity?.supportFragmentManager!!,SignUpOrLogin()) })
        textView.setOnClickListener(View.OnClickListener { Utils.switchToFragment(activity?.supportFragmentManager!!,Login()) })
        firebaseAuth=FirebaseAuth.getInstance()
        firestore=FirebaseFirestore.getInstance()

        signUpButton.setOnClickListener(View.OnClickListener {
            if(email.text.toString().isNotEmpty()&&
                password.text.toString().isNotEmpty()&&
                name.text.toString().isNotEmpty()) {
                val emailText = email.text.toString()
                val passwordText = password.text.toString()
                val nameText = name.text.toString()
                registerUser(emailText,passwordText,nameText)
                Utils.hideKeyboard(requireActivity())
            }
        })
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {val statusCode = e.statusCode
                val statusMessage = GoogleSignInStatusCodes.getStatusCodeString(statusCode)
                Log.d("Google Sign In", "Error: $statusMessage ($statusCode)")
                // Google Sign In failed, update UI appropriately
                Toast.makeText(requireContext(), "Google Sign-In failed: $statusMessage", Toast.LENGTH_SHORT).show()
            }
        }
        signUpGoogle.setOnClickListener(View.OnClickListener {
            signInLauncher.launch(mGoogleSignInClient.signInIntent)
        })

        return view
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user=firebaseAuth.currentUser
                    val email=user?.email.toString()
                    val name=user?.displayName.toString()
                    saveUserToFirestore(name,firebaseAuth.currentUser!!.uid,email,"")
                    val intent= Intent(requireContext(),MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    // Handle signed-in user
                } else {
                    Toast.makeText(requireContext(), "Google authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    fun registerUser(emailText: String, passwordText: String,name:String) {
        firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    saveUserToFirestore(name,firebaseAuth.currentUser!!.uid,emailText,passwordText)
                    val intent= Intent(requireContext(),MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    // Registration failed
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(requireContext(), "Invalid credentials: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthWeakPasswordException -> {
                            Toast.makeText(requireContext(), "Weak password: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(requireContext(), "User already exists: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Registration Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }
    private fun saveUserToFirestore(name:String,uid: String, email: String,passwordText: String) {
        val user = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "password" to passwordText
        )

        firestore.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "User saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
//                Toast.makeText(requireContext(), "Failed to save user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
