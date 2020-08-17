package com.example.finalyearproject.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

data class PeerReviewQuestionSet(
    var set_name: String? = null,
    var question_list: List<String>? = null
    ) {

    companion object {
        const val PEER_REVIEW_QUESTION_SET_COLLECTION = "peer_review_question_set"
        private const val WRITE = "peer review write"

        fun writeToDatabase(peerReviewQuestionSet: PeerReviewQuestionSet) {
            val db = FirebaseFirestore.getInstance()
            db.collection(PEER_REVIEW_QUESTION_SET_COLLECTION).document(peerReviewQuestionSet.set_name.toString()).set(peerReviewQuestionSet).addOnSuccessListener {
                Log.d(WRITE, "DocumentSnapshot successfully written")
            }.addOnFailureListener { e ->
                Log.d(WRITE, "Error writing", e)
            }
        }

        fun readSingleFileFromDatabase(set_name: String?): PeerReviewQuestionSet? {
            val db = FirebaseFirestore.getInstance()
            var peerReviewQuestionSet: PeerReviewQuestionSet? = null
            db.collection(PEER_REVIEW_QUESTION_SET_COLLECTION).document(set_name.toString()).get().addOnSuccessListener { documentSnapshot ->
                peerReviewQuestionSet = documentSnapshot.toObject(PeerReviewQuestionSet::class.java)
            }
            return peerReviewQuestionSet
        }
    }
}