package com.example.finalyearproject

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

data class PeerReviewGrouping(
    var assignment_id: String? = null,
    var group_id: String? = null,
    var student_email_list: List<String>? = null,
    var question_set_name: String? = null,
    var isReleased: Boolean? = null,
    var subject_name: String? = null,
    var isDone: Boolean? = null
) {

    companion object {
        const val PEER_REVIEW_GROUPING_SUB_COLLECTION = "peer_review_grouping"

        fun writeToDatabase(peerReview: PeerReview, peerReviewGrouping: PeerReviewGrouping) {
            val db = FirebaseFirestore.getInstance()
            db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(peerReview.subject_name.toString()).collection(
                peerReviewGrouping.assignment_id.toString()).document(peerReviewGrouping.group_id.toString()).set(peerReviewGrouping).addOnSuccessListener {
                Log.d(MainActivity.TAG, "DocumentSnapshot successfully written!")
            }.addOnFailureListener { e ->
                Log.d(MainActivity.TAG, "Fail to write", e)
            }
        }

        fun readSingleFileFromDatabase(subject_name: String?, assignment_id: String?, group_id: String?): PeerReviewGrouping? {
            val db = FirebaseFirestore.getInstance()
            var peerReviewGrouping: PeerReviewGrouping? = null
            db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subject_name.toString()).collection(
                assignment_id.toString()).document(group_id.toString()).get().addOnSuccessListener { documentSnapshot ->
                peerReviewGrouping = documentSnapshot.toObject(PeerReviewGrouping::class.java)
            }.addOnFailureListener { e ->
                Log.d(MainActivity.TAG, "Fail to read.", e)
            }
            return peerReviewGrouping
        }

        fun readAllFromDatabase(peerReview: PeerReview, assignment_id: String?): MutableList<PeerReviewGrouping> {
            val db = FirebaseFirestore.getInstance()
            var peerReviewGroupings = mutableListOf<PeerReviewGrouping>()
            db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(peerReview.subject_name.toString()).collection(assignment_id.toString()).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        val peerReviewGrouping = doc.toObject(PeerReviewGrouping::class.java)
                        peerReviewGroupings.add(peerReviewGrouping)
                    }
                }
            }
            return peerReviewGroupings
        }
    }
}