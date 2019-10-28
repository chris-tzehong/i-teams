package com.example.finalyearproject

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

data class Lecturer(
    var lecturer_id: String? = null,
    var lecturer_name: String? = null,
    var lecturer_email: String? = null,
    var lecturer_department: String? = null,
    var lecturer_dob: Date? = null,
    var lecturer_contact: Int? = null
) {
    companion object {
        private const val LECTURER_COLLECTION = "lecturer"
        private const val WRITE = "lecturer write"

        fun writeToDatabase(lecturer: Lecturer) {
            val db = FirebaseFirestore.getInstance()
            db.collection(LECTURER_COLLECTION).document(lecturer.lecturer_email.toString()).set(lecturer).addOnSuccessListener {
                Log.d(WRITE, "DocumentSnapshot successfully written!")
            }.addOnFailureListener { e ->
                Log.w(WRITE, "Error adding document", e)
            }
        }

        fun readSingleFileFromDatabase(lecturer_email: String?): Lecturer? {
            val db = FirebaseFirestore.getInstance()
            var lecturer: Lecturer? = null
            db.collection(LECTURER_COLLECTION).document(lecturer_email.toString()).get().addOnSuccessListener { documentSnapshot ->
                lecturer = documentSnapshot.toObject(Lecturer::class.java)
            }
            return lecturer
        }

        fun readAllFromDatabase(): MutableList<Lecturer> {

            val db = FirebaseFirestore.getInstance()
            val lecturers = mutableListOf<Lecturer>()
            db.collection(LECTURER_COLLECTION).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        val lecturer = doc.toObject(Lecturer::class.java)
                        lecturers.add(lecturer)
                    }
                }
            }
            return lecturers
        }
    }
}