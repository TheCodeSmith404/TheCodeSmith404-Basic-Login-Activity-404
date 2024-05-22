package com.example.basicloginapplication


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class Login : Fragment() {
    lateinit var enterEmail:EditText
    lateinit var enterPassword:EditText
    lateinit var forgotPassword:TextView
    lateinit var loginButton: Button
    lateinit var auth: FirebaseAuth
    lateinit var loginGoogle:ImageButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view:View=inflater.inflate(R.layout.fragment_login,container,false)
        val textView=view.findViewById<TextView>(R.id.textViewLoginToRegister)
        val imageButton=view.findViewById<ImageButton>(R.id.imageButtonBackLogin)

        textView.setOnClickListener(View.OnClickListener { Utils.switchToFragment(activity?.supportFragmentManager!!,SignUp()) })
        imageButton.setOnClickListener(View.OnClickListener { Utils.switchToFragment(activity?.supportFragmentManager!!,SignUpOrLogin()) })
        enterEmail=view.findViewById(R.id.editTextEmailLogin)
        enterPassword=view.findViewById(R.id.editTextPasswordLogin)
        forgotPassword=view.findViewById(R.id.textForgetPassword)
        loginButton=view.findViewById(R.id.buttonLoginLogin)
        loginGoogle=view.findViewById(R.id.imageButtonGoogle)
        auth=FirebaseAuth.getInstance()

        setupGoogleSignIn()

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }

        forgotPassword.setOnClickListener(View.OnClickListener {
            Toast.makeText(context,"Not Implemented Yet",Toast.LENGTH_SHORT).show()
        })

        loginButton.setOnClickListener(View.OnClickListener {
            if (enterEmail.text.toString().isNotEmpty() && enterPassword.text.toString().isNotEmpty()) {
                auth.signInWithEmailAndPassword(enterEmail.text.toString(), enterPassword.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateUI(user =auth.currentUser)
                        } else {
                            val exception = task.exception
                            when (exception) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    Toast.makeText(context, "Invalid credentials. Please check your email and password.", Toast.LENGTH_SHORT).show()
                                }
                                is FirebaseAuthInvalidUserException -> {
                                    Toast.makeText(context, "No account found with this email. Please sign up first.", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(context, "Login failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
            } else {
                Toast.makeText(context, "Email and Password must not be empty", Toast.LENGTH_SHORT).show()
            }

        })
        loginGoogle.setOnClickListener(View.OnClickListener {
            signInWithGoogle()

        })
        return view
    }
    private fun setupGoogleSignIn() {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Register the ActivityResultLauncher

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(requireContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
}