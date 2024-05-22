package com.example.basicloginapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    lateinit var auth:FirebaseAuth
    lateinit var firestore:FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        firestore=FirebaseFirestore.getInstance()
        val text=findViewById<TextView>(R.id.setUserId)
        val nameView=findViewById<TextView>(R.id.textViewNameMain)
        val uid=auth.currentUser?.uid
        firestore.collection("users").document(uid!!).get().addOnSuccessListener {
            val name=it.get("name")
            nameView.text=name.toString()
        }
        text.text=uid
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        val logoutButton=findViewById<Button>(R.id.buttonLogOut)
        logoutButton.setOnClickListener(View.OnClickListener {
            auth.signOut()
            finish()
        })

    }
}