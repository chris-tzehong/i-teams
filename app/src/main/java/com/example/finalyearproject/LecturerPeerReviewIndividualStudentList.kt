package com.example.finalyearproject


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class LecturerPeerReviewIndividualStudentList : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_lecturer_peer_review_individual_student_list, container, false)
        val mLecturerPeerReviewIndividualStudentListRecyclerView: RecyclerView = view.findViewById(R.id.lecturer_peer_review_individual_list_recycler_view)
        mLecturerPeerReviewIndividualStudentListRecyclerView.layoutManager = LinearLayoutManager(activity)
        val db = FirebaseFirestore.getInstance()
        val subjectName = arguments!!.getString(SUBJECT_NAME)
        val assignmentId = arguments!!.getString(ASSIGNMENT_ID)
        val groupId = arguments!!.getString(GROUP_ID)

        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName!!).collection(assignmentId!!).document(groupId!!).get().addOnSuccessListener { documentSnapshot ->
            val lecturerPeerReviewIndividualListGrouping = documentSnapshot.toObject(PeerReviewGrouping::class.java)
            val temporaryIndividualHolders = mutableListOf<TemporaryIndividualHolder>()
            val mLecturerPeerReviewIndividualListAdapter = LecturerIndividualAdapter(activity, temporaryIndividualHolders)
            lecturerPeerReviewIndividualListGrouping!!.student_email_list!!.forEach { email ->
                db.collection(Student.STUDENT_COLLECTION).document(email).get().addOnSuccessListener { documentSnapshot ->
                    val student = documentSnapshot.toObject(Student::class.java)
                    val temporaryIndividualHolder = TemporaryIndividualHolder(lecturerPeerReviewIndividualListGrouping.subject_name, lecturerPeerReviewIndividualListGrouping.assignment_id,
                        lecturerPeerReviewIndividualListGrouping.group_id, email, student!!.student_name)
                    temporaryIndividualHolders.add(temporaryIndividualHolder)
                    mLecturerPeerReviewIndividualListAdapter.notifyDataSetChanged()
                }
            }
            mLecturerPeerReviewIndividualStudentListRecyclerView.adapter = mLecturerPeerReviewIndividualListAdapter
        }

        return view
    }

    companion object {
        const val SUBJECT_NAME = "lecturer peer review individual student list subject name"
        const val ASSIGNMENT_ID = "lecturer peer review individual student list assignment id"
        const val GROUP_ID = "lecturer peer review individual student list group id"

        fun newInstance(): LecturerPeerReviewIndividualStudentList = LecturerPeerReviewIndividualStudentList()

        fun getNewIndividualListInstance(subject_name: String?, assignment_id: String?, group_id: String?): LecturerPeerReviewIndividualStudentList {
            val fragment = LecturerPeerReviewIndividualStudentList()
            val args = Bundle()
            args.putString(SUBJECT_NAME, subject_name)
            args.putString(ASSIGNMENT_ID, assignment_id)
            args.putString(GROUP_ID, group_id)
            fragment.arguments = args
            return fragment
        }
    }

    class LecturerIndividualAdapter(private val context: FragmentActivity?, private val temporaryIndividualHolders: List<TemporaryIndividualHolder>): RecyclerView.Adapter<LecturerIndividualAdapter.LecturerIndividualHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): LecturerIndividualHolder {
            return LecturerIndividualHolder(LayoutInflater.from(context).inflate(R.layout.list_item_lecturer_individual_peer_review, parent, false))
        }

        override fun getItemCount(): Int {
            return temporaryIndividualHolders.size
        }

        override fun onBindViewHolder(holder: LecturerIndividualHolder, position: Int) {
            val temporaryIndividualHolder = temporaryIndividualHolders[position]
            holder.bind(temporaryIndividualHolder, context!!.supportFragmentManager)
        }

        class LecturerIndividualHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(temporaryIndividualHolder: TemporaryIndividualHolder, fragmentManager: FragmentManager) = with(itemView) {
                val mLecturerPeerReviewIndividualListEmail: TextView = itemView.findViewById(R.id.lecturer_peer_review_individual_list_email_label)
                val mLecturerPeerReviewIndividualListName: TextView = itemView.findViewById(R.id.lecturer_peer_review_individual_list_student_name_label)

                mLecturerPeerReviewIndividualListEmail.text = temporaryIndividualHolder.reviewer_email
                mLecturerPeerReviewIndividualListName.text = temporaryIndividualHolder.reviewer_name

                itemView.setOnClickListener {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(R.id.main_container, LecturerPeerReviewResultsFragment.getResultsInstance(temporaryIndividualHolder.subject_name.toString(),
                        temporaryIndividualHolder.assignment_id.toString(), temporaryIndividualHolder.group_id.toString(), temporaryIndividualHolder.reviewer_email.toString()))
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
    }



    data class TemporaryIndividualHolder(var subject_name: String? = null, var assignment_id: String? = null, var group_id: String? = null, var reviewer_email: String? = null, var reviewer_name: String? = null)
}
