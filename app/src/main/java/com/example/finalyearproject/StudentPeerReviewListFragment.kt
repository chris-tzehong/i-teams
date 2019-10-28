package com.example.finalyearproject


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject


class StudentPeerReviewListFragment : Fragment() {

    private var fireStoreListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_student_peer_review_list, container, false)

        val mPendingStudentPeerReviewRecyclerView: RecyclerView = view.findViewById(R.id.pending_student_peer_review_recycler_view)
        mPendingStudentPeerReviewRecyclerView.layoutManager = LinearLayoutManager(activity)
        val mCompletedStudentPeerReviewRecyclerView: RecyclerView = view.findViewById(R.id.completed_student_peer_review_recycler_view)
        mCompletedStudentPeerReviewRecyclerView.layoutManager = LinearLayoutManager(activity)
        val db = FirebaseFirestore.getInstance()
        val student = LoginActivity.appUser as Student

        val mInitialPeerReviews: MutableList<PeerReview> = mutableListOf()
        val mInitialPeerReviewGrouping = mutableListOf<PeerReviewGrouping>()
        val mPendingPeerReviews = mutableListOf<PeerReviewGrouping>()
        val mCompletedPeerReviews = mutableListOf<PeerReviewGrouping>()
        var trigger = 0


        db.collection(PeerReview.PEER_REVIEW_COLLECTION).get().addOnSuccessListener { documentSnapshot ->

                for (doc in documentSnapshot!!) {
                    val peerReview = doc.toObject(PeerReview::class.java)
                    mInitialPeerReviews.add(peerReview)

                }

                mInitialPeerReviews.forEach { peerReview ->
                    for (id in peerReview.assignment_id!!) {
                        fireStoreListener = db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(peerReview.subject_name.toString()).collection(id).whereArrayContains("student_email_list", student.student_email.toString()).addSnapshotListener(
                            EventListener { documentSnapshot, e ->
                            if (e != null) {
                                Log.d(MainActivity.TAG, "Listen", e)
                                return@EventListener
                            }

                                var peerReviewGrouping: PeerReviewGrouping? = null
                                var peerReviewResults = mutableListOf<PeerReviewResult>()


                            for (doc in documentSnapshot!!) {
                                peerReviewGrouping = doc.toObject(PeerReviewGrouping::class.java)
                                mInitialPeerReviewGrouping.add(peerReviewGrouping)
                            }
                                Log.d("test2", mInitialPeerReviewGrouping.toString())

                                val mPendingAdapter = PendingStudentPeerReviewAdapter(activity, mPendingPeerReviews)
                                mPendingStudentPeerReviewRecyclerView.adapter = mPendingAdapter
                                val mCompletedAdapter = CompletedStudentPeerReviewAdapter(activity, mCompletedPeerReviews)
                                mCompletedStudentPeerReviewRecyclerView.adapter = mCompletedAdapter

                            trigger += 1

                                if (trigger == mInitialPeerReviews.size) {
                                    mInitialPeerReviewGrouping.forEach { review ->
                                        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(review.subject_name.toString()).collection(review.assignment_id.toString()).document(
                                            review.group_id.toString()).collection(student.student_email.toString()).get().addOnSuccessListener { querySnapshot ->
                                            for (doc in querySnapshot) {
                                                val peerReviewResult = doc.toObject(PeerReviewResult::class.java)
                                                peerReviewResults.add(peerReviewResult)
                                            }

                                            if (peerReviewResults.size == review.student_email_list!!.size - 1) {
                                                mCompletedPeerReviews.add(review)
                                                mCompletedAdapter.notifyDataSetChanged()
                                            } else {
                                                mPendingPeerReviews.add(review)
                                                mPendingAdapter.notifyDataSetChanged()
                                            }

                                            peerReviewResults = mutableListOf()

                                        }
                                    }


                                }

                        })
                    }
                }

            }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        fireStoreListener!!.remove()
    }

    companion object {
        fun newInstance(): StudentPeerReviewListFragment = StudentPeerReviewListFragment()
    }

    class PendingStudentPeerReviewAdapter(private val context: FragmentActivity?, private val studentPeerReviewGroupings: List<PeerReviewGrouping>):
        RecyclerView.Adapter<PendingStudentPeerReviewAdapter.PendingStudentPeerReviewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingStudentPeerReviewHolder {
            return PendingStudentPeerReviewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_student_peer_review, parent, false))
        }

        override fun getItemCount(): Int {
            return studentPeerReviewGroupings.size
        }

        override fun onBindViewHolder(holderPendingStudent: PendingStudentPeerReviewHolder, position: Int) {
            val studentPeerReviewGrouping = studentPeerReviewGroupings[position]
            holderPendingStudent.bind(studentPeerReviewGrouping, context!!.supportFragmentManager)
        }

        class PendingStudentPeerReviewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind (peerReviewGrouping: PeerReviewGrouping, fragmentManager: FragmentManager) = with(itemView) {
                val mPeerReviewSubjectName: TextView = itemView.findViewById(R.id.student_peer_review_subject_name_placeholder)
                val mPeerReviewAssignmentIdAndGroupId: TextView = itemView.findViewById(R.id.lecturer_peer_review_assignment_id_placeholder)

                mPeerReviewSubjectName.text = peerReviewGrouping.subject_name
                val textToPlace: String = String.format(resources.getString(R.string.student_peer_review_assignment_id_group_id_placeholder), peerReviewGrouping.assignment_id, peerReviewGrouping.group_id)
                mPeerReviewAssignmentIdAndGroupId.text = textToPlace

                itemView.setOnClickListener {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(R.id.main_container, StudentAnswerPeerReviewFragment.getPeerReviewGroupingNewInstance(peerReviewGrouping.subject_name.toString(),
                        peerReviewGrouping.assignment_id.toString(), peerReviewGrouping.group_id.toString()))
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
    }

    class CompletedStudentPeerReviewAdapter(private val context: FragmentActivity?, private val studentPeerReviewGroupings: List<PeerReviewGrouping>): RecyclerView.Adapter<CompletedStudentPeerReviewAdapter.CompletedStudentPeerReviewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CompletedStudentPeerReviewHolder {
            return CompletedStudentPeerReviewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_student_peer_review, parent, false))
        }

        override fun getItemCount(): Int {
            return studentPeerReviewGroupings.size
        }

        override fun onBindViewHolder(holder: CompletedStudentPeerReviewHolder, position: Int) {
            val peerReviewGrouping = studentPeerReviewGroupings[position]
            holder.bind(peerReviewGrouping, context!!.supportFragmentManager)
        }

        class CompletedStudentPeerReviewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(peerReviewGrouping: PeerReviewGrouping, fragmentManager: FragmentManager) = with(itemView) {
                val mPeerReviewSubjectName: TextView = itemView.findViewById(R.id.student_peer_review_subject_name_placeholder)
                val mPeerReviewAssignmentIdAndGroupId: TextView = itemView.findViewById(R.id.lecturer_peer_review_assignment_id_placeholder)

                mPeerReviewSubjectName.text = peerReviewGrouping.subject_name
                val textToPlace: String = String.format(resources.getString(R.string.student_peer_review_assignment_id_group_id_placeholder), peerReviewGrouping.assignment_id, peerReviewGrouping.group_id)
                mPeerReviewAssignmentIdAndGroupId.text = textToPlace

                itemView.setOnClickListener {
                    if (peerReviewGrouping.isDone == false) {
                        Toast.makeText(context, R.string.student_peer_review_not_released_error, Toast.LENGTH_SHORT).show()
                    } else {
                        val transaction = fragmentManager.beginTransaction()
                        transaction.replace(R.id.main_container, StudentViewCompletedPeerReviewFragment.getNewPeerReviewResultInstance(peerReviewGrouping.subject_name.toString(),
                            peerReviewGrouping.assignment_id.toString(), peerReviewGrouping.group_id.toString()))
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }
            }
        }
    }


}
