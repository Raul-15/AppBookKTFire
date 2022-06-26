package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityRegisterBinding

    //Progress
    private lateinit var progressDialog: ProgressDialog

    //Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //Init progress dialog, will show while creating account | register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!")
        progressDialog.setCanceledOnTouchOutside(false)
        //handle back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //Handle click, begin register
        binding.registerBtn.setOnClickListener {
            /*Steps
            * 1) Input Data
            * 2) Validate Data
            * 3) Create account - Firebase auth
            * 4) Save User Info - Firebase Real Time Database
            * */
            validatedata()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validatedata() {
        // 1) Input Data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cpasswordEt.text.toString().trim()
        // 2) Validate Data
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern", Toast.LENGTH_SHORT).show()

        } else if (password.isEmpty()) {
            Toast.makeText(this, "Confirm password", Toast.LENGTH_SHORT).show()

        } else if (cPassword.isEmpty()) {
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show()

        } else {
            createUserAccount()
        }


    }

    private fun createUserAccount() {
//Show Progress
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        //Create user in firebae auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserinfo()
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed creating account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun updateUserinfo() {
        // 4)Save User Info - Firebase Realtime Database
        progressDialog.setMessage("Saving user info...")
        //Timestamp
        val timestamp = System.currentTimeMillis()

        //get current user uid, since user is registered so we can get it now
        val uid = firebaseAuth.uid

        //Setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // add empty, will do in profile edit
        hashMap["userType"] =
            "user" // possible values are user/admin, will change value to admin manually on firebase d
        hashMap["timestamp"] = timestamp

        // set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //User info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "Account created...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                //Failed adding data to db
                    e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed creating account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }
}