package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityLoginBinding

    //Progress
    private lateinit var progressDialog: ProgressDialog

    //Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //Init progress dialog, will show while login user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!")
        progressDialog.setCanceledOnTouchOutside(false)

        //Handler click, not have account , goto register screen
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))

        }

        //Handler click, begin login
        binding.loginBtn.setOnClickListener {
            /*Steps
          * 1) Input Data
          * 2) Validate Data
          * 3) Login - Firebase auth
          * 4) Check User type - Firebase Auth
          * If user - Move to user dashboard
          * If admin - Move to admin dashboard
             */
            validateData()

        }


    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        // 2) Validate Data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }

    }


    private fun loginUser() {
        //3) Login - Firebase auth

        //Show progress
        progressDialog.setMessage("Logging in...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()

            }.addOnFailureListener { e ->
                // failed login
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()


            }
    }

    private fun checkUser() {
        // If user - Move to user dashboard
        // If admin - Move to admin dashboard
        progressDialog.setMessage("Checking user...")
        val firebaseUser = firebaseAuth.currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    //Get user type e.g user or admin
                    val userType = snapshot.child("userType").value
                    if (userType == "user") {
                        //its simple user, open user dashboard
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    } else if (userType == "admin") {
                        //its simple user, open user dashboard
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                DashboardAdminActivity::class.java
                            )
                        )
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }
}
