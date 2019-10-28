package com.example.finalyearproject


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class StudentViewCompletedPeerReviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_student_view_completed_peer_review, container, false)
        val mStudentViewCompletedReviewRecyclerView: RecyclerView = view.findViewById(R.id.student_view_completed_peer_review_recycler_view)
        val db = FirebaseFirestore.getInstance()
        val student = LoginActivity.appUser as Student
        val subjectName = arguments!!.getString(SUBJECT_NAME)
        val assignmentId = arguments!!.getString(ASSIGNMENT_ID)
        val groupId = arguments!!.getString(GROUP_ID)
        val reviewTarget = student.student_email
        val peerReviewResults = mutableListOf<PeerReviewResult>()

        mStudentViewCompletedReviewRecyclerView.layoutManager = LinearLayoutManager(activity)

        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(groupId.toString()).get().addOnSuccessListener { documentSnapshot ->
            val peerReviewGrouping = documentSnapshot.toObject(PeerReviewGrouping::class.java)
            var preventOverwriteTrigger = 0
            peerReviewGrouping!!.student_email_list!!.forEach { email ->
                db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(
                    groupId.toString()).collection(email).whereEqualTo("review_target", reviewTarget).get().addOnSuccessListener { querySnapshot ->
                    for (doc in querySnapshot) {
                        if (doc.exists()) {
                            val peerReviewResult = doc.toObject(PeerReviewResult::class.java)
                            peerReviewResults.add(peerReviewResult)
                        }
                    }
                    preventOverwriteTrigger += 1
                    if (preventOverwriteTrigger == peerReviewGrouping.student_email_list!!.size) {
                        val mViewStudentResultAdapter = StudentViewResultsAdapter(activity, peerReviewResults)
                        mStudentViewCompletedReviewRecyclerView.adapter = mViewStudentResultAdapter
                    }
                }
            }

        }

        return view
    }

    companion object {
        const val SUBJECT_NAME = "student view completed peer review subject name"
        const val ASSIGNMENT_ID = "student view completed peer review assignment id"
        const val GROUP_ID = "student view completed peer review group id"

        fun newInstance(): StudentViewCompletedPeerReviewFragment = StudentViewCompletedPeerReviewFragment()

        fun getNewPeerReviewResultInstance(subjectName: String, assignmentId: String, groupId: String): StudentViewCompletedPeerReviewFragment {
            val fragment = StudentViewCompletedPeerReviewFragment()
            val args = Bundle()
            args.putString(SUBJECT_NAME, subjectName)
            args.putString(ASSIGNMENT_ID, assignmentId)
            args.putString(GROUP_ID, groupId)
            fragment.arguments = args
            return fragment
        }
    }

    class StudentViewResultsAdapter(private val context: FragmentActivity?, private val peerReviewResults: List<PeerReviewResult>): RecyclerView.Adapter<StudentViewResultsAdapter.StudentViewResultsHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): StudentViewResultsHolder {
            return StudentViewResultsHolder(LayoutInflater.from(context).inflate(R.layout.list_item_student_view_completed_peer_review, parent, false))
        }

        override fun getItemCount(): Int {
            return peerReviewResults.size
        }

        override fun onBindViewHolder(holder: StudentViewResultsHolder, position: Int) {
            val peerReviewResult = peerReviewResults[position]
            holder.bind(peerReviewResult, position)
        }

        class StudentViewResultsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind (peerReviewResult: PeerReviewResult, position: Int) = with(itemView) {
                val mStudentViewCompletedReviewStudentNumber: TextView = itemView.findViewById(R.id.student_view_completed_peer_review_student_number_placeholder)
                val mStudentViewCompletedReviewStudentResults: TextView = itemView.findViewById(R.id.student_view_completed_peer_review_results_placeholder)
                val mStudentViewCompletedReviewAdditionalComments: TextView = itemView.findViewById(R.id.student_view_completed_peer_review_additional_comments_placeholder)

                val studentNumberString = resources.getString(R.string.student_view_results_student_number, position + 1)
                mStudentViewCompletedReviewStudentNumber.text = studentNumberString
                val resultsInPercentage = peerReviewResult.review_marks!!.toDouble() * 100 / peerReviewResult.available_marks!!.toDouble()
                val studentResultsString = resources.getString(R.string.student_view_results_actual_results, peerReviewResult.review_marks, peerReviewResult.available_marks, resultsInPercentage)
                mStudentViewCompletedReviewStudentResults.text = studentResultsString
                val additionalCommentString = resources.getString(R.string.student_view_results_additional_comments, peerReviewResult.additional_comment)
                mStudentViewCompletedReviewAdditionalComments.text = additionalCommentString
            }
        }
    }


}
