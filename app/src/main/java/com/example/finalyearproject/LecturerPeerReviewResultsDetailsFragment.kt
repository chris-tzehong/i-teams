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

class LecturerPeerReviewResultsDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_lecturer_peer_review_results_details, container, false)
        val mLecturerPeerReviewDetailsReviewerEmailLabel: TextView = view.findViewById(R.id.lecturer_peer_review_results_details_reviewer_label)
        val mLecturerPeerReviewDetailsReviewTargetEmailLabel: TextView = view.findViewById(R.id.lecturer_peer_review_results_details_reviewed_label)
        val mLecturerPeerReviewDetailsRecyclerView: RecyclerView = view.findViewById(R.id.lecturer_peer_review_results_details_recycler_view)
        val mLecturerPeerReviewDetailsAdditionalCommentLabel: TextView = view.findViewById(R.id.lecturer_peer_review_results_details_comments_label)
        val subjectName = arguments!!.getString(SUBJECT_NAME)
        val assignmentId = arguments!!.getString(ASSIGNMENT_ID)
        val groupId = arguments!!.getString(GROUP_ID)
        val reviewerEmail = arguments!!.getString(REVIEWER_EMAIL)
        val reviewTarget = arguments!!.getString(REVIEW_TARGET)
        val questionSetName = arguments!!.getString(QUESTION_SET_NAME)
        val db = FirebaseFirestore.getInstance()
        val questionAnswers = mutableListOf<QuestionAnswer>()

        mLecturerPeerReviewDetailsRecyclerView.layoutManager = LinearLayoutManager(activity)
        val reviewerString = resources.getString(R.string.lecturer_peer_review_results_details_reviewer_label, reviewerEmail.toString())
        mLecturerPeerReviewDetailsReviewerEmailLabel.text = reviewerString
        val reviewTargetString = resources.getString(R.string.lecturer_peer_review_results_details_reviewed_label, reviewTarget.toString())
        mLecturerPeerReviewDetailsReviewTargetEmailLabel.text = reviewTargetString
        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName!!).collection(assignmentId!!).document(groupId!!).collection(reviewerEmail!!).document(reviewTarget!!).get().addOnSuccessListener { documentSnapshot ->
            val peerReviewResult = documentSnapshot.toObject(PeerReviewResult::class.java)
            val commentString = resources.getString(R.string.lecturer_peer_review_results_details_additional_comments_label, peerReviewResult!!.additional_comment)
            mLecturerPeerReviewDetailsAdditionalCommentLabel.text = commentString
            db.collection(PeerReviewQuestionSet.PEER_REVIEW_QUESTION_SET_COLLECTION).document(questionSetName!!).get().addOnSuccessListener { documentSnapshot ->
                val questionSet = documentSnapshot.toObject(PeerReviewQuestionSet::class.java)
                for (i in questionSet!!.question_list!!.indices) {
                    val questionAnswer = QuestionAnswer(questionSet!!.question_list!![i], peerReviewResult.review_results!!.get(i.toString()))
                    questionAnswers.add(questionAnswer)
                }
                val mViewResultsAdapter = ViewResultsAdapter(activity, questionAnswers)
                mLecturerPeerReviewDetailsRecyclerView.adapter = mViewResultsAdapter
            }
        }



        return view
    }

    companion object {
        const val SUBJECT_NAME = "lecturer peer review results details subject name"
        const val ASSIGNMENT_ID = "lecturer peer review results details assignment id"
        const val GROUP_ID = "lecturer peer review results details group id"
        const val REVIEWER_EMAIL = "lecturer peer review results details review email"
        const val REVIEW_TARGET = "lecturer peer review results details review target"
        const val QUESTION_SET_NAME = "lecturer peer review results details question set name"

        fun newInstance(): LecturerPeerReviewResultsDetailsFragment = LecturerPeerReviewResultsDetailsFragment()

        fun getResultDetailsInstance(subject_name: String, assignment_id: String, group_id: String, reviewer_email: String, review_target: String, question_set_name: String): LecturerPeerReviewResultsDetailsFragment {
            val fragment = LecturerPeerReviewResultsDetailsFragment()
            val args = Bundle()
            args.putString(SUBJECT_NAME, subject_name)
            args.putString(ASSIGNMENT_ID, assignment_id)
            args.putString(GROUP_ID, group_id)
            args.putString(REVIEWER_EMAIL, reviewer_email)
            args.putString(REVIEW_TARGET, review_target)
            args.putString(QUESTION_SET_NAME, question_set_name)
            fragment.arguments = args
            return fragment
        }
    }

    class ViewResultsAdapter(private val context: FragmentActivity?, private val questionAnswers: List<QuestionAnswer>): RecyclerView.Adapter<ViewResultsAdapter.ViewResultsHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewResultsHolder {
            return ViewResultsHolder(LayoutInflater.from(context).inflate(R.layout.list_item_lecturer_peer_review_details_question_answer, parent, false))
        }

        override fun getItemCount(): Int {
            return questionAnswers.size
        }

        override fun onBindViewHolder(holder: ViewResultsHolder, position: Int) {
            val questionAnswer = questionAnswers[position]
            holder.bind(questionAnswer)
        }

        class ViewResultsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(questionAnswer: QuestionAnswer) = with(itemView) {
                val mLecturerPeerReviewResultsDetailsQuestionLabel: TextView = itemView.findViewById(R.id.lecturer_peer_review_results_details_questions_placeholder)
                val mLecturerPeerReviewResultsDetailsAnswerLabel: TextView = itemView.findViewById(R.id.lecturer_peer_review_results_details_answer_placeholder)

                mLecturerPeerReviewResultsDetailsQuestionLabel.text = questionAnswer.question
                val stringValue = resources.getString(R.string.lecturer_peer_review_results_details_answer_placeholder, questionAnswer.answer)
                mLecturerPeerReviewResultsDetailsAnswerLabel.text = stringValue
            }
        }
    }

    data class QuestionAnswer(var question: String? = null, var answer: String? = null)


}
