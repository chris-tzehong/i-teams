package com.example.finalyearproject.model

import android.util.Log
import com.example.finalyearproject.MainActivity
import com.google.firebase.firestore.FirebaseFirestore

data class PeerReviewResult(
    var reviewer_email: String? = null,
    var review_target: String? = null,
    var review_results: HashMap<String, String>? = null,
    var additional_comment: String? = null,
    var isDone: Boolean? = null,
    var review_marks: Int? = null,
    var available_marks: Int? = null
) {

    companion object {

        fun writeToDatabase(subject_name: String, peerReviewGrouping: PeerReviewGrouping, peerReviewResult: PeerReviewResult) {
            val db = FirebaseFirestore.getInstance()
            db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subject_name).collection(
                peerReviewGrouping.assignment_id.toString()).document(peerReviewGrouping.group_id.toString()).collection(
                peerReviewResult.reviewer_email.toString()).document(peerReviewResult.review_target.toString()).set(peerReviewResult).addOnSuccessListener {
                Log.d(MainActivity.TAG, "DocumentSnapshot successfully written!")
            }.addOnFailureListener { e ->
                Log.d(MainActivity.TAG, "Fail to write.", e)
            }
        }

        fun readSingleFileFromDatabase(peerReview: PeerReview, peerReviewGrouping: PeerReviewGrouping, reviewer_email: String?, review_target: String?): PeerReviewResult? {
            val db = FirebaseFirestore.getInstance()
            var peerReviewResult: PeerReviewResult? = null
            db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(peerReview.subject_name.toString()).collection(
                peerReviewGrouping.assignment_id.toString()).document(peerReviewGrouping.group_id.toString()).collection(
                reviewer_email.toString()).document(review_target.toString()).get().addOnSuccessListener { documentSnapshot ->
                peerReviewResult = documentSnapshot.toObject(PeerReviewResult::class.java)
            }
            return peerReviewResult
        }

        fun readAllFromDatabase(peerReview: PeerReview, peerReviewGrouping: PeerReviewGrouping, reviewer_email: String?): MutableList<PeerReviewResult>? {
            val db = FirebaseFirestore.getInstance()
            var peerReviewResults = mutableListOf<PeerReviewResult>()
            db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(peerReview.subject_name.toString()).collection(
                peerReviewGrouping.assignment_id.toString()).document(peerReviewGrouping.group_id.toString()).collection(
                reviewer_email.toString()).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        val peerReviewResult = doc.toObject(PeerReviewResult::class.java)
                        peerReviewResults.add(peerReviewResult)
                    }
                }
            }
            return peerReviewResults
        }
    }
}