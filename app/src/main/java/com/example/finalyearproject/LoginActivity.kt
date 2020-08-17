package com.example.finalyearproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.finalyearproject.model.Lecturer
import com.example.finalyearproject.model.Student
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val mLoginUsername: EditText = findViewById(R.id.mLoginUsername)
        val mLoginPassword: EditText = findViewById(R.id.mLoginPassword)
        val mLoginButton: Button = findViewById(R.id.mLoginButton)

        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val mStudents: MutableList<Student> = Student.readAllFromDatabase()
        val mLecturers: MutableList<Lecturer> = Lecturer.readAllFromDatabase()

        mLoginButton.setOnClickListener {
            if (mLoginUsername.text.isEmpty() || mLoginPassword.text.isEmpty()) {
                Toast.makeText(baseContext, getString(R.string.login_empty_credentials), Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(mLoginUsername.text.toString(), mLoginPassword.text.toString()).addOnCompleteListener(this) {task ->
                    if (task.isSuccessful) {
                        Log.d(LOGIN_ACTIVITY, "signInWithEmailAndPassword: Success")
                        val fireBaseUser = auth.currentUser
                        for (student in mStudents) {
                            if (student.student_email == fireBaseUser!!.email) {
                                isLecturer = false
                                appUser = student
                            } else for (lecturer in mLecturers) {
                                if (lecturer.lecturer_email == fireBaseUser!!.email) {
                                    isLecturer = true
                                    appUser = lecturer
                                }
                            }
                        }
                        Log.d(LOGIN_ACTIVITY, isLecturer.toString())
                        val loginToSystem = Intent(this, MainActivity::class.java)
                        startActivity(loginToSystem)
                    } else {
                        Log.d(LOGIN_ACTIVITY, "signInWithEmailAndPassword: Failure")
                        Toast.makeText(baseContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                    }

                }
            }

        }

//        mLoginButton.setOnClickListener {
//            val loginToSystem = Intent(this, MainActivity::class.java)
//            startActivity(loginToSystem)
//        }



        // only for admin to create users
//        auth.createUserWithEmailAndPassword("16027625@imail.sunway.edu.my", "fypomg").addOnCompleteListener(this) { task ->
//            if (task.isSuccessful) {
//                Log.d(LOGIN_ACTIVITY, "createUserWithEmailAndPassword: Success")
//                val user = auth.currentUser
//            } else {
//                Log.w(LOGIN_ACTIVITY, "createUserWithEmailAndPassword: Failure", task.exception)
//            }
//        }
    }

    companion object {
        const val LOGIN_ACTIVITY = "Login"
        var isLecturer = false
        var appUser: Any? = null
    }
}
