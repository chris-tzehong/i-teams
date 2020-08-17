package com.example.finalyearproject.studentreview


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.LoginActivity
import com.example.finalyearproject.R
import com.example.finalyearproject.model.*
import com.google.firebase.firestore.FirebaseFirestore

class StudentAnswerPeerReviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_answer_peer_review, container, false)

        val mStudentPeerReviewQuestionsRecyclerView: RecyclerView = view.findViewById(
            R.id.student_peer_review_questions_recycler_view
        )
        mStudentPeerReviewQuestionsRecyclerView.layoutManager = LinearLayoutManager(activity)
        val mStudentPeerReviewQuestionsTargetSpinner: Spinner = view.findViewById(R.id.student_peer_review_target_spinner)
        val mStudentPeerReviewQuestionSubmitButton: Button = view.findViewById(R.id.student_peer_review_submit_button)
        val mStudentPeerReviewQuestionsAdditionalComments: EditText = view.findViewById(
            R.id.student_peer_review_additional_comments
        )
        val mStudentPeerReviewQuestionsTargetReviewedLabel: TextView = view.findViewById(
            R.id.student_peer_review_target_reviewed_error_label
        )
        var peerReviewGrouping: PeerReviewGrouping? = null
        var peerReviewQuestionSet: PeerReviewQuestionSet? = null
        var peerReviewResult: PeerReviewResult? = null
        val db = FirebaseFirestore.getInstance()
        val student = LoginActivity.appUser as Student
        var mReviewedTarget: String? = null
        var mQuestionAdapter: QuestionAdapter? = null


        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(arguments!!.getString(
            SUBJECT_NAME
        ).toString()).collection(arguments!!.getString(
            ASSIGNMENT_ID
        ).toString()).document(arguments!!.getString(GROUP_ID).toString()).get().addOnSuccessListener { documentSnapshot ->

            peerReviewGrouping = documentSnapshot.toObject(PeerReviewGrouping::class.java)

            val targetList = mutableListOf<String>()
            targetList.add("-")
            peerReviewGrouping!!.student_email_list!!.forEach { email ->
                targetList.add(email)
            }
            targetList.remove(student.student_email)

            val mTargetsAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, targetList)
            mTargetsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            mStudentPeerReviewQuestionsTargetSpinner.adapter = mTargetsAdapter
            if (mStudentPeerReviewQuestionsTargetSpinner.selectedItem.toString() == "-") {
                mStudentPeerReviewQuestionsTargetReviewedLabel.text = resources.getString(
                    R.string.student_peer_review_invalid_review_target_error
                )
            }


            db.collection(PeerReviewQuestionSet.PEER_REVIEW_QUESTION_SET_COLLECTION).document(peerReviewGrouping!!.question_set_name.toString()).get().addOnSuccessListener {
                peerReviewQuestionSet = it.toObject(PeerReviewQuestionSet::class.java)
                val mQuestionSet = peerReviewQuestionSet!!.question_list
                mQuestionAdapter =
                    QuestionAdapter(
                        activity,
                        mQuestionSet!!
                    )
                mStudentPeerReviewQuestionsRecyclerView.adapter = mQuestionAdapter
                mStudentPeerReviewQuestionsTargetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        mReviewedTarget = parent!!.getItemAtPosition(position).toString()
                        review_results.clear()
                        reset_target = true
                        mQuestionAdapter!!.notifyDataSetChanged()
                        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(peerReviewGrouping!!.subject_name.toString()).collection(
                            peerReviewGrouping!!.assignment_id.toString()).document(peerReviewGrouping!!.group_id.toString()).collection(
                            student.student_email.toString()).document(mReviewedTarget.toString()).get().addOnSuccessListener { documentSnapshot ->
                            mStudentPeerReviewQuestionsTargetReviewedLabel.isVisible = documentSnapshot.exists()
                            mStudentPeerReviewQuestionsTargetReviewedLabel.text = resources.getString(
                                R.string.student_peer_review_target_reviewed_error
                            )
                            mStudentPeerReviewQuestionSubmitButton.isEnabled = !documentSnapshot.exists()
                        }

                    }

                }
            }



        }


        mStudentPeerReviewQuestionSubmitButton.setOnClickListener {
            if (mStudentPeerReviewQuestionsTargetSpinner.selectedItem.toString() == "-") {
                Toast.makeText(activity,
                    R.string.student_peer_review_invalid_review_target_error, Toast.LENGTH_SHORT).show()
            } else if (mStudentPeerReviewQuestionsAdditionalComments.text.isEmpty()) {
                Toast.makeText(activity,
                    R.string.student_peer_review_empty_additional_comments_error, Toast.LENGTH_SHORT).show()
            } else if (review_results.size != peerReviewQuestionSet!!.question_list!!.size || review_results.containsValue("-")) {
                Toast.makeText(activity,
                    R.string.student_peer_review_incomplete_questions_error, Toast.LENGTH_SHORT).show()
            } else {
                var reviewMarks = 0
                review_results.forEach { entry ->
                    reviewMarks += when (entry.value) {
                        "1 - Strongly disagree" -> 1
                        "2 - Disagree" -> 2
                        "3 - Neutral" -> 3
                        "4 - Agree" -> 4
                        "5 - Strongly agree" -> 5
                        else -> 0
                    }
                }
                peerReviewResult =
                    PeerReviewResult(
                        student.student_email,
                        mStudentPeerReviewQuestionsTargetSpinner.selectedItem.toString(),
                        review_results,
                        mStudentPeerReviewQuestionsAdditionalComments.text.toString(),
                        true,
                        reviewMarks,
                        peerReviewQuestionSet!!.question_list!!.size * 5
                    )
                db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(peerReviewGrouping!!.subject_name.toString()).collection(
                    peerReviewGrouping!!.assignment_id.toString()).document(peerReviewGrouping!!.group_id.toString()).collection(
                    student.student_email.toString()).document(peerReviewResult!!.review_target.toString()).set(peerReviewResult!!).addOnSuccessListener {
                    val reviewedList = mutableListOf<PeerReviewResult>()
                    db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(peerReviewGrouping!!.subject_name.toString()).collection(
                        peerReviewGrouping!!.assignment_id.toString()).document(peerReviewGrouping!!.group_id.toString()).collection(
                        student.student_email.toString()).get().addOnCompleteListener {task ->
                        if (task.isSuccessful) {
                            for (doc in task.result!!) {
                                val prResult = doc.toObject(PeerReviewResult::class.java)
                                reviewedList.add(prResult)
                            }
                        }
                    }
                    if (reviewedList.size == peerReviewGrouping!!.student_email_list!!.size - 1) {
                        peerReviewGrouping!!.isDone = true
                    }
                }
            }
        }





        return view
    }

    companion object {
        const val SUBJECT_NAME = "student answer peer review fragment subject name"
        const val ASSIGNMENT_ID = "student answer peer review fragment assignment id"
        const val GROUP_ID = "student answer peer review fragment group id"
        var review_results = hashMapOf<String, String>()
        var reset_target = false

        fun newInstance(): StudentAnswerPeerReviewFragment =
            StudentAnswerPeerReviewFragment()

        fun getPeerReviewGroupingNewInstance(subject_name: String, assignment_id: String, group_id: String): StudentAnswerPeerReviewFragment {
            val fragment =
                StudentAnswerPeerReviewFragment()
            val args = Bundle()
            args.putString(SUBJECT_NAME, subject_name)
            args.putString(ASSIGNMENT_ID, assignment_id)
            args.putString(GROUP_ID, group_id)
            fragment.arguments = args
            return fragment
        }
    }

    class QuestionAdapter(private val context: FragmentActivity?, private val questions: List<String>): RecyclerView.Adapter<QuestionAdapter.QuestionHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionHolder {
            return QuestionHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_peer_review_question,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return questions.size
        }

        override fun onBindViewHolder(holder: QuestionHolder, position: Int) {
            val question = questions[position]
            holder.bind(question, position)
        }

        class QuestionHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(question: String, mapPosition: Int) = with(itemView) {
                val mStudentPeerReviewQuestionsLabel: TextView = itemView.findViewById(
                    R.id.student_peer_review_questions_label
                )
                val mChoicesSpinner: Spinner = itemView.findViewById(R.id.student_peer_review_questions_choices_spinner)
                mStudentPeerReviewQuestionsLabel.text = question

                val mChoicesAdapter = ArrayAdapter.createFromResource(context,
                    R.array.student_peer_review_questions_spinner_choices, android.R.layout.simple_spinner_item)
                mChoicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                mChoicesSpinner.adapter = mChoicesAdapter
                if (reset_target) {
                    val blankItemPosition = mChoicesAdapter.getPosition("-")
                    mChoicesSpinner.setSelection(blankItemPosition)
                    reset_target = false
                }
                mChoicesSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        review_results[mapPosition.toString()] =
                            parent!!.getItemAtPosition(position).toString()
                    }

                }

            }
        }
    }


}
