package com.example.finalyearproject.lecturerreview


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.LoginActivity
import com.example.finalyearproject.R
import com.example.finalyearproject.model.Lecturer
import com.example.finalyearproject.model.PeerReview
import com.example.finalyearproject.model.PeerReviewGrouping
import com.google.firebase.firestore.FirebaseFirestore

class LecturerPeerReviewListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_lecturer_peer_review_list, container, false)
        val mLecturerPeerReviewListRecyclerView: RecyclerView = view.findViewById(R.id.lecturer_peer_review_list_recycler_view)
        val mLecturerAddPeerReviewButton: Button = view.findViewById(R.id.lecturer_add_new_review_session_button)
        val db = FirebaseFirestore.getInstance()
        val lecturer = LoginActivity.appUser as Lecturer
        val lecturerPeerReviewGroupings = mutableListOf<PeerReviewGrouping>()
        val temporaryPeerReviewHolders = mutableListOf<TemporaryPeerReviewHolder>()

        mLecturerPeerReviewListRecyclerView.layoutManager = LinearLayoutManager(activity)
        val mLecturerPeerReviewAdapter =
            LecturerPeerReviewAdapter(
                activity,
                temporaryPeerReviewHolders
            )
        mLecturerPeerReviewListRecyclerView.adapter = mLecturerPeerReviewAdapter
        db.collection(PeerReview.PEER_REVIEW_COLLECTION).whereEqualTo("lecturer_email", lecturer.lecturer_email.toString()).get().addOnSuccessListener { documentSnapshot ->
            for (doc in documentSnapshot!!) {
                val lecturerPeerReview = doc.toObject(PeerReview::class.java)
                Log.d("wtf", lecturerPeerReview.toString())
                for (assignment in lecturerPeerReview.assignment_id!!) {
                    val temporaryPeerReviewHolder =
                        TemporaryPeerReviewHolder(
                            lecturerPeerReview.subject_name,
                            assignment
                        )
                    temporaryPeerReviewHolders.add(temporaryPeerReviewHolder)
                }
                mLecturerPeerReviewAdapter.notifyDataSetChanged()

            }
        }

        mLecturerAddPeerReviewButton.setOnClickListener {
            val transaction = fragmentManager!!.beginTransaction()
            transaction.replace(R.id.main_container, AddNewPeerReviewDetailsFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }



        return view
    }

    companion object {
        fun newInstance(): LecturerPeerReviewListFragment =
            LecturerPeerReviewListFragment()
    }

    class LecturerPeerReviewAdapter(private val context: FragmentActivity?, private val lecturePeerReviewHolders: List<TemporaryPeerReviewHolder>): RecyclerView.Adapter<LecturerPeerReviewAdapter.LecturerPeerReviewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LecturerPeerReviewHolder {
            return LecturerPeerReviewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_lecturer_peer_review,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return lecturePeerReviewHolders.size
        }

        override fun onBindViewHolder(holderLecturer: LecturerPeerReviewHolder, position: Int) {
            val lecturePeerReviewHolder = lecturePeerReviewHolders[position]
            holderLecturer.bind(lecturePeerReviewHolder, context!!.supportFragmentManager)
        }

        class LecturerPeerReviewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(temporaryPeerReviewHolder: TemporaryPeerReviewHolder, fragmentManager: FragmentManager) = with(itemView) {
                val mLecturerPeerReviewSubjectNameHolder: TextView = itemView.findViewById(
                    R.id.lecturer_peer_review_subject_name_placeholder
                )
                val mLecturerPeerReviewAssignmentIdHolder: TextView = itemView.findViewById(
                    R.id.lecturer_peer_review_assignment_id_placeholder
                )

                mLecturerPeerReviewSubjectNameHolder.text = temporaryPeerReviewHolder.subject_name
                mLecturerPeerReviewAssignmentIdHolder.text = temporaryPeerReviewHolder.assignment_id

                itemView.setOnClickListener {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(R.id.main_container, LecturerPeerReviewGroupingListFragment.getPeerReviewGroupingsNewInstance(temporaryPeerReviewHolder.subject_name.toString(), temporaryPeerReviewHolder.assignment_id.toString()))
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
    }

    data class TemporaryPeerReviewHolder(var subject_name: String? = null, var assignment_id: String? = null)


}
