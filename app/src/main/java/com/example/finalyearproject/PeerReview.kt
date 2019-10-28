package com.example.finalyearproject

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

data class PeerReview(
    var subject_name: String? = null,
    var lecturer_email: String? = null,
    var lecturer_name: String? = null,
    var assignment_id: List<String>? = null
) {

    companion object {
        const val PEER_REVIEW_COLLECTION = "peer_review"
        private const val PEER_REVIEW_WRITE = "peer review write"

        fun writeToDatabase(peerReview: PeerReview) {
            val db = FirebaseFirestore.getInstance()
            db.collection(PEER_REVIEW_COLLECTION).document(peerReview.subject_name.toString()).set(peerReview).addOnSuccessListener {
                Log.d(PEER_REVIEW_WRITE, "DocumentSnapshot successfully written!")
            }.addOnFailureListener { e ->
                Log.d(PEER_REVIEW_WRITE, "Fail to write.", e)
            }
        }

        fun readSingleFileFromDatabase(subject_name: String?): PeerReview? {
            val db = FirebaseFirestore.getInstance()
            var peerReview: PeerReview? = null
            db.collection(PEER_REVIEW_COLLECTION).document(subject_name.toString()).get().addOnSuccessListener { documentSnapshot ->
                documentSnapshot.toObject(PeerReview::class.java)
            }.addOnFailureListener { e ->
                Log.d(MainActivity.TAG, "Fail to read.", e)
            }
            return peerReview
        }

        fun readAllFromDatabase(): MutableList<PeerReview> {
            val db = FirebaseFirestore.getInstance()
            var peerReviews = mutableListOf<PeerReview>()
            db.collection(PEER_REVIEW_COLLECTION).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        val peerReview = doc.toObject(PeerReview::class.java)
                        peerReviews.add(peerReview)
                    }
                }
            }
            return peerReviews
        }
    }
}