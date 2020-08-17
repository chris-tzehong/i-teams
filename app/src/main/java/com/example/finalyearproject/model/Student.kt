package com.example.finalyearproject.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable
import java.util.*

data class Student(
    var student_id: Int? = null,
    var student_name: String? = null,
    var student_email: String? = null,
    var student_dob: Date? = null,
    var student_course: String? = null,
    var student_reputation: Double? = null,
    var student_contact: Int? = null): Serializable {



    companion object {
        const val STUDENT_COLLECTION = "student"
        private const val WRITE = "student write"

        fun writeToDatabase(student: Student) {
            val db = FirebaseFirestore.getInstance()
            db.collection(STUDENT_COLLECTION).document(student.student_email.toString()).set(student).addOnSuccessListener {
                Log.d(WRITE, "DocumentSnapshot successfully written!")
            }.addOnFailureListener { e ->
                Log.w(WRITE, "Error adding document", e)
            }
        }

        fun readSingleFileFromDatabase(student_email: String?): Student? {
            val db = FirebaseFirestore.getInstance()
            var student: Student? = null
            db.collection(STUDENT_COLLECTION).document(student_email.toString()).get().addOnSuccessListener { documentSnapshot ->
                student = documentSnapshot.toObject(Student::class.java)
            }
            return student
        }

        fun readAllFromDatabase(): MutableList<Student> {

            val db = FirebaseFirestore.getInstance()
            val students = mutableListOf<Student>()
            db.collection(STUDENT_COLLECTION).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        val student = doc.toObject(Student::class.java)
                        students.add(student)
                    }
                }
            }
            return students
        }
    }
}