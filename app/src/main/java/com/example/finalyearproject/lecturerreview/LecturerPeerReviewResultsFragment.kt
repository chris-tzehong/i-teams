package com.example.finalyearproject.lecturerreview


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.R
import com.example.finalyearproject.model.PeerReview
import com.example.finalyearproject.model.PeerReviewGrouping
import com.example.finalyearproject.model.PeerReviewResult
import com.example.finalyearproject.model.Student
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 */
class LecturerPeerReviewResultsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_lecturer_peer_review_results, container, false)
        val mLecturerPeerReviewResultsRecyclerView: RecyclerView = view.findViewById(
            R.id.lecturer_peer_review_results_recycler_view
        )
        val mLecturerPeerReviewResultsNotDoneLabel: TextView = view.findViewById(R.id.lecturer_peer_review_not_done_label)
        val mLecturerPeerReviewResultsNotDoneRecyclerView: RecyclerView = view.findViewById(
            R.id.lecturer_peer_review_results_not_done_students_recycler_view
        )
        mLecturerPeerReviewResultsNotDoneLabel.isVisible = false
        mLecturerPeerReviewResultsRecyclerView.layoutManager = LinearLayoutManager(activity)
        mLecturerPeerReviewResultsNotDoneRecyclerView.layoutManager = LinearLayoutManager(activity)
        mLecturerPeerReviewResultsNotDoneRecyclerView.isVisible = false
        val db = FirebaseFirestore.getInstance()
        val subjectName = arguments!!.getString(SUBJECT_NAME)
        val assignmentId = arguments!!.getString(ASSIGNMENT_ID)
        val groupId = arguments!!.getString(GROUP_ID)
        val reviewTarget = arguments!!.getString(REVIEW_TARGET)
        val peerReviewResults = mutableListOf<PeerReviewResult>()
        val checkList = mutableListOf<String>()
        val peerReviewNotDoneStudents = mutableListOf<String>()
        val groupRef = db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(groupId.toString())

        groupRef.get().addOnSuccessListener {documentSnapshot ->
            val peerReviewGrouping = documentSnapshot.toObject(PeerReviewGrouping::class.java)
            val temporaryResultsHolders = mutableListOf<TemporaryResultsHolder>()
            val mViewStudentResultsAdapter =
                ViewStudentResultsAdapter(
                    activity,
                    temporaryResultsHolders
                )
            var preventOverwriteTrigger = 0
            peerReviewGrouping!!.student_email_list!!.forEach { email ->
                groupRef.collection(email).whereEqualTo("review_target", reviewTarget).get().addOnSuccessListener { querySnapshot ->
                    for (doc in querySnapshot!!) {
                        if (doc.exists()) {
                            val peerReviewResult = doc.toObject(PeerReviewResult::class.java)
                            peerReviewResults.add(peerReviewResult)
                        }
                    }
                    preventOverwriteTrigger += 1
                    if (preventOverwriteTrigger == peerReviewGrouping.student_email_list!!.size) {
                        peerReviewResults.forEach { item ->
                            checkList.add(item.reviewer_email.toString())
                        }
                        peerReviewGrouping.student_email_list!!.forEach { email ->
                            if (email !in checkList) {
                                if (email != reviewTarget) {
                                    peerReviewNotDoneStudents.add(email)
                                }

                            }
                        }
                        if (peerReviewNotDoneStudents.isNotEmpty()) {
                            val mNotDoneStudentsAdapter =
                                NotDoneStudentsAdapter(
                                    activity,
                                    peerReviewNotDoneStudents
                                )
                            mLecturerPeerReviewResultsNotDoneRecyclerView.adapter = mNotDoneStudentsAdapter
                            mLecturerPeerReviewResultsNotDoneLabel.isVisible = true
                            mLecturerPeerReviewResultsNotDoneRecyclerView.isVisible = true
                        }
                        peerReviewResults.forEach { item ->
                            db.collection(Student.STUDENT_COLLECTION).document(item.reviewer_email.toString()).get().addOnSuccessListener { documentSnapshot ->
                                val student = documentSnapshot.toObject(Student::class.java)
                                val temporaryResultsHolder =
                                    TemporaryResultsHolder(
                                        peerReviewGrouping.subject_name,
                                        peerReviewGrouping.assignment_id,
                                        peerReviewGrouping.group_id,
                                        student!!.student_name,
                                        item.reviewer_email,
                                        item.available_marks,
                                        item.review_marks,
                                        peerReviewGrouping.question_set_name,
                                        reviewTarget
                                    )
                                temporaryResultsHolders.add(temporaryResultsHolder)
                                mViewStudentResultsAdapter.notifyDataSetChanged()
                            }
                        }
                    }


                }

            }
            mLecturerPeerReviewResultsRecyclerView.adapter = mViewStudentResultsAdapter
        }

        return view
    }

    companion object {
        const val SUBJECT_NAME = "lecturer peer review results subject name"
        const val ASSIGNMENT_ID = "lecturer peer review results assignment id"
        const val GROUP_ID = "lecturer peer review results group id"
        const val REVIEW_TARGET = "lecturer peer review results review target"

        fun newInstance(): LecturerPeerReviewResultsFragment =
            LecturerPeerReviewResultsFragment()

        fun getResultsInstance(subject_name: String?, assignment_id: String?, group_id: String?, review_target: String?): LecturerPeerReviewResultsFragment {
            val fragment =
                LecturerPeerReviewResultsFragment()
            val args = Bundle()
            args.putString(SUBJECT_NAME, subject_name)
            args.putString(ASSIGNMENT_ID, assignment_id)
            args.putString(GROUP_ID, group_id)
            args.putString(REVIEW_TARGET, review_target)
            fragment.arguments = args
            return fragment
        }
    }

    class ViewStudentResultsAdapter(private val context: FragmentActivity?, private val temporaryResultsHolders: List<TemporaryResultsHolder>): RecyclerView.Adapter<ViewStudentResultsAdapter.ViewStudentResultsHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewStudentResultsHolder {
            return ViewStudentResultsHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_peer_review_results,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return temporaryResultsHolders.size
        }

        override fun onBindViewHolder(holder: ViewStudentResultsHolder, position: Int) {
            val temporaryResultsHolder = temporaryResultsHolders[position]
            holder.bind(temporaryResultsHolder, context!!.supportFragmentManager)
        }

        class ViewStudentResultsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(temporaryResultsHolder: TemporaryResultsHolder, fragmentManager: FragmentManager) = with(itemView) {
                val mLecturerPeerReviewResultsStudentNameLabel: TextView = itemView.findViewById(
                    R.id.lecturer_peer_review_results_student_name_label
                )
                val mLecturerPeerReviewResultsStudentEmailLabel: TextView = itemView.findViewById(
                    R.id.lecturer_peer_review_results_student_email_label
                )
                val mLecturerPeerReviewResultsStudentResultsLabel: TextView = itemView.findViewById(
                    R.id.lecturer_peer_review_results_student_results_label
                )
                val mLecturerPeerReviewResultsViewDetailsButton: Button = itemView.findViewById(
                    R.id.lecturer_peer_review_results_view_details_button
                )

                val nameString = resources.getString(R.string.lecturer_peer_review_results_student_name_placeholder, temporaryResultsHolder.reviewer_name)
                mLecturerPeerReviewResultsStudentNameLabel.text = nameString
                mLecturerPeerReviewResultsStudentEmailLabel.text = temporaryResultsHolder.reviewer_email
                val resultsInPercentage = temporaryResultsHolder.received_results!! * 100 / temporaryResultsHolder.available_results!!.toDouble()
                val resultsString = resources.getString(
                    R.string.lecturer_peer_review_results_student_results_placeholder, temporaryResultsHolder.received_results,
                    temporaryResultsHolder.available_results, resultsInPercentage)
                mLecturerPeerReviewResultsStudentResultsLabel.text = resultsString

                mLecturerPeerReviewResultsViewDetailsButton.setOnClickListener {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(
                        R.id.main_container, LecturerPeerReviewResultsDetailsFragment.getResultDetailsInstance(temporaryResultsHolder.subject_name.toString(),
                        temporaryResultsHolder.assignment_id.toString(), temporaryResultsHolder.group_id.toString(), temporaryResultsHolder.reviewer_email.toString(),
                        temporaryResultsHolder.review_target.toString(), temporaryResultsHolder.question_set.toString()))
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
    }

    class NotDoneStudentsAdapter(private val context: FragmentActivity?, private val emails: List<String>): RecyclerView.Adapter<NotDoneStudentsAdapter.NotDoneStudentsHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotDoneStudentsHolder {
            return NotDoneStudentsHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_not_done_students,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return emails.size
        }

        override fun onBindViewHolder(holder: NotDoneStudentsHolder, position: Int) {
            val email = emails[position]
            holder.bind(email)
        }

        class NotDoneStudentsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(email: String) = with(itemView) {
                val mLecturerPeerReviewResultsNotDoneStudentLabel: TextView = itemView.findViewById(
                    R.id.lecturer_peer_review_results_not_done_student_name_placeholder
                )
                mLecturerPeerReviewResultsNotDoneStudentLabel.text = email
            }
        }
    }

    data class TemporaryResultsHolder(var subject_name: String? = null,
                                      var assignment_id: String? = null,
                                      var group_id: String? = null,
                                      var reviewer_name: String? = null,
                                      var reviewer_email: String? = null,
                                      var available_results: Int? = null,
                                      var received_results: Int? = null,
                                      var question_set: String? = null,
                                      var review_target: String? = null)
}
