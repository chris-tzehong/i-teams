package com.example.finalyearproject

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

data class Announcement (
    var announcement_title: String? = null,
    var announcement_content: String? = null,
    var announcement_date: Date? = null,
    var announcement_id: String? = null,
    var announcer_name: String? = null) {

    companion object {
        const val ANNOUNCEMENT_COLLECTION = "announcement"
        private const val WRITE = "write"

        fun writeToDatabase(announcement: Announcement) {

            val db = FirebaseFirestore.getInstance()
            db.collection(ANNOUNCEMENT_COLLECTION).add(announcement).addOnSuccessListener { documentReference ->
                Log.d(WRITE, "DocumentSnapshot written with ID: {$documentReference}")
            }.addOnFailureListener { e ->
                Log.w(WRITE, "Error adding document", e)
            }
        }

        fun readAllFromDatabase(): MutableList<Announcement> {

            val db = FirebaseFirestore.getInstance()
            val announcements = mutableListOf<Announcement>()
            db.collection(ANNOUNCEMENT_COLLECTION).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        val announcement = doc.toObject(Announcement::class.java)
                        announcement.announcement_id = doc.id
                        db.collection(ANNOUNCEMENT_COLLECTION).document(doc.id).update("announcement_id", doc.id)
                        announcements.add(announcement)
                    }
                }
            }
            return announcements
        }
    }
}
