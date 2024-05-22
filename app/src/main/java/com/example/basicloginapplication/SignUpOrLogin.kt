package com.example.basicloginapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback


class SignUpOrLogin : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_sign_up_or_login,container,false)
        val btnSignUp=view.findViewById<Button>(R.id.fragmentSignUpButton)
        val btnLogin=view.findViewById<Button>(R.id.fragmentLoginButton)
        btnSignUp.setOnClickListener(View.OnClickListener { Utils.switchToFragment(activity?.supportFragmentManager!!,SignUp(),true) })
        btnLogin.setOnClickListener(View.OnClickListener { Utils.switchToFragment(activity?.supportFragmentManager!!,Login(),true) })
        activity?.let {
            activity?.onBackPressedDispatcher?.addCallback(it, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            })
        }
        return view
    }

}