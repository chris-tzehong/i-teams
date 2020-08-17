package com.example.finalyearproject.lecturerreview


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.MainActivity
import com.example.finalyearproject.R
import com.example.finalyearproject.model.PeerReview
import com.example.finalyearproject.model.PeerReviewGrouping
import com.example.finalyearproject.model.PeerReviewResult
import com.example.finalyearproject.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat


class LecturerPeerReviewGroupingListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_lecturer_peer_review_grouping_list, container, false)
        val mLecturerPeerReviewGroupingsRecyclerView: RecyclerView = view.findViewById(
            R.id.lecturer_peer_review_groupings_recycler_view
        )
        mLecturerPeerReviewGroupingsRecyclerView.layoutManager = LinearLayoutManager(activity)
        val lecturerPeerReviewGroupings = mutableListOf<PeerReviewGrouping>()
        val mLecturerPeerReviewGroupingsDownloadResultsButton: Button = view.findViewById(
            R.id.lecturer_peer_review_groupings_download_results_button
        )
        val mLecturerPeerReviewGroupingsReleaseResultsButton: Button = view.findViewById(
            R.id.lecturer_peer_review_groupings_release_result_button
        )
        var isDone = true
        var isReleased = false
        val db = FirebaseFirestore.getInstance()
        val subjectName = arguments!!.getString(SUBJECT_NAME)
        val assignmentId = arguments!!.getString(ASSIGNMENT_ID)

        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (doc in task.result!!) {
                    val lecturerPeerReviewGrouping = doc.toObject(PeerReviewGrouping::class.java)
                    lecturerPeerReviewGroupings.add(lecturerPeerReviewGrouping)
                    isReleased = lecturerPeerReviewGrouping.isReleased as Boolean
                    mLecturerPeerReviewGroupingsReleaseResultsButton.isEnabled = !isReleased
                }

                val mLecturerPeerReviewGroupingAdapter =
                    LecturerPeerReviewGroupingAdapter(
                        activity,
                        lecturerPeerReviewGroupings
                    )
                mLecturerPeerReviewGroupingsRecyclerView.adapter = mLecturerPeerReviewGroupingAdapter
            }
        }

        mLecturerPeerReviewGroupingsDownloadResultsButton.setOnClickListener {
            val stringBuilder = StringBuilder()
            stringBuilder.append("group_id,reviewer_mail,peer_mail,results")
            lecturerPeerReviewGroupings.forEach { item ->
                val groupId = item.group_id.toString()
                item.student_email_list!!.forEach { email ->
                    db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(groupId).collection(email).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (doc in task.result!!) {
                                val peerReviewResult = doc.toObject(PeerReviewResult::class.java)
                                val marksInPercentage = peerReviewResult.review_marks!! * 100 / peerReviewResult.available_marks!!.toDouble()
                                val df = DecimalFormat("#.##")
                                stringBuilder.append("\n${groupId},${peerReviewResult.reviewer_email.toString()},${peerReviewResult.review_target.toString()},${df.format(marksInPercentage)}")
                            }
                            try {
                                val out = FileOutputStream("${context!!.filesDir.path}/${subjectName}_${assignmentId}.csv")
                                out.write((stringBuilder.toString()).toByteArray())
                                out.close()

                                val file = File(context!!.filesDir, "${subjectName}_${assignmentId}.csv")
                                val path = FileProvider.getUriForFile(context as Context, "com.example.finalyearproject.fileprovider", file)
                                val fileIntent = Intent(Intent.ACTION_SEND)
                                fileIntent.type = "text/csv"
                                fileIntent.putExtra(Intent.EXTRA_SUBJECT, "$subjectName - $assignmentId Results")
                                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                fileIntent.putExtra(Intent.EXTRA_STREAM, path)
                                startActivity(Intent.createChooser(fileIntent, "Send to..."))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        val peerReviewGroupingList = mutableListOf<PeerReviewGrouping>()
        val peerReviewResultList = mutableListOf<PeerReviewResult>()
        var peerReviewGroupingTrigger = 0
        var peerReviewPredictedNumber = 0
        var peerReviewGroupingPredictedNumber = 0

        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).get().addOnSuccessListener { querySnapshot ->
            for (doc in querySnapshot) {
                val peerReviewGrouping = doc.toObject(PeerReviewGrouping::class.java)
                peerReviewGroupingList.add(peerReviewGrouping)
            }
            peerReviewGroupingList.forEach { item ->
                peerReviewGroupingPredictedNumber += item.student_email_list!!.size
            }
            peerReviewGroupingList.forEach { item ->
                peerReviewPredictedNumber += (item.student_email_list!!.size * (item.student_email_list!!.size - 1))
                item.student_email_list!!.forEach { email ->
                    db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(item.group_id.toString()).collection(email).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (doc in task.result!!) {
                                val peerReviewResult = doc.toObject(PeerReviewResult::class.java)
                                peerReviewResultList.add(peerReviewResult)
                            }
                            peerReviewGroupingTrigger += 1
                            if (peerReviewGroupingTrigger == peerReviewGroupingPredictedNumber) {
                                if (peerReviewResultList.size == peerReviewPredictedNumber) {
                                    peerReviewGroupingList.forEach { item ->
                                        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(item.group_id.toString()).update("done", true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d(MainActivity.TAG, "Successfully updated!")
                                                isDone = true
                                            } else {
                                                Log.d(MainActivity.TAG, "Fail to write into database.")
                                            }
                                        }
                                    }
                                } else {
                                    isDone = false
                                }
                            }
                        }
                    }
                }
            }
        }

        var changeReputationTrigger = 0
        var changeReputationPredictedTrigger = 0
        var temporaryPeerResultList = mutableListOf<PeerReviewResult>()
        mLecturerPeerReviewGroupingsReleaseResultsButton.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.lecturer_peer_review_groupings_release_results_dialog_title)
            if (isDone) {
                builder.setMessage(R.string.lecturer_peer_review_groupings_able_release_results_dialog_description)
                builder.setPositiveButton(R.string.lecturer_peer_review_groupings_able_release_confirm_button) { dialog, which ->
                    peerReviewGroupingList.forEach { item ->
                        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(item.group_id.toString()).update("released", true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                isReleased = true
                                Toast.makeText(activity,
                                    R.string.lecturer_peer_review_groupings_successful_release_results_description, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    peerReviewGroupingList.forEach { group ->
                        changeReputationPredictedTrigger += group.student_email_list!!.size * group.student_email_list!!.size
                        group.student_email_list!!.forEach { target ->
                            group.student_email_list!!.forEach { email ->
                                db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(
                                    group.group_id.toString()).collection(email).whereEqualTo("review_target", target).get().addOnSuccessListener { querySnapshot ->
                                    for (doc in querySnapshot) {
                                        val peerReviewResult = doc.toObject(PeerReviewResult::class.java)
                                        temporaryPeerResultList.add(peerReviewResult)
                                    }

                                    changeReputationTrigger += 1
                                    if (changeReputationTrigger == changeReputationPredictedTrigger) {
                                        val filteredMap = temporaryPeerResultList.groupBy { it.review_target }
                                        filteredMap.keys.forEach { key ->
                                            val temporaryList = filteredMap[key]
                                            var totalAffectionScore = 0.0
                                            temporaryList!!.forEach { item ->
                                                // deducted reputation mark will be calculated based on lost mark of received review
                                                val reputationAffectionScore = (item.available_marks!! - item.review_marks!!.toDouble()) / item.available_marks!!.toDouble()
                                                totalAffectionScore += reputationAffectionScore
                                            }
                                            totalAffectionScore /= temporaryList.size
                                            val totalAffectionTwoPoints = BigDecimal(totalAffectionScore).setScale(2, RoundingMode.UP)
                                            totalAffectionScore = totalAffectionTwoPoints.toDouble()
                                            db.collection(Student.STUDENT_COLLECTION).document(key.toString()).get().addOnSuccessListener { documentSnapshot ->
                                                val student = documentSnapshot.toObject(
                                                    Student::class.java)
                                                val changedReputationScore = student!!.student_reputation!! - totalAffectionScore
                                                db.collection(Student.STUDENT_COLLECTION).document(key.toString()).update("student_reputation", changedReputationScore).addOnSuccessListener {
                                                    Log.d(MainActivity.TAG, "Score successfully updated!")
                                                }.addOnFailureListener {
                                                    Log.d(MainActivity.TAG, "Fail to update reputation score.")
                                                }
                                            }

                                        }
                                        changeReputationTrigger = 0
                                        changeReputationPredictedTrigger = 0
                                    }

                                }

                                }
                            }
                        }

                }
                builder.setNegativeButton(R.string.lecturer_peer_review_groupings_able_release_cancel_button) { dialog, which ->  }
            } else {
                builder.setMessage(R.string.lecturer_peer_review_groupings_unable_release_results_dialog_description)
                builder.setNeutralButton(R.string.lecturer_peer_review_groupings_unable_release_results_cancel_button) { dialog, which ->  }
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        return view
    }

    companion object {
        const val SUBJECT_NAME = "lecturer peer review grouping list subject name"
        const val ASSIGNMENT_ID = "lecturer peer review grouping list assignment id"

        fun newInstance(): LecturerPeerReviewGroupingListFragment =
            LecturerPeerReviewGroupingListFragment()

        fun getPeerReviewGroupingsNewInstance(subject_name: String, assignment_id: String): LecturerPeerReviewGroupingListFragment {
            val fragment =
                LecturerPeerReviewGroupingListFragment()
            val args = Bundle()
            args.putString(SUBJECT_NAME, subject_name)
            args.putString(ASSIGNMENT_ID, assignment_id)
            fragment.arguments = args
            return fragment
        }
    }

    class LecturerPeerReviewGroupingAdapter(private val context: FragmentActivity?, private val lecturerPeerReviewGroupings: List<PeerReviewGrouping>): RecyclerView.Adapter<LecturerPeerReviewGroupingAdapter.LecturerPeerReviewGroupingHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LecturerPeerReviewGroupingHolder {
            return LecturerPeerReviewGroupingHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_lecturer_peer_review_grouping,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return lecturerPeerReviewGroupings.size
        }

        override fun onBindViewHolder(holder: LecturerPeerReviewGroupingHolder, position: Int) {
            val lecturerPeerReviewGrouping = lecturerPeerReviewGroupings[position]
            holder.bind(lecturerPeerReviewGrouping, context!!.supportFragmentManager)
        }

        class LecturerPeerReviewGroupingHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(lecturerPeerReviewGrouping: PeerReviewGrouping, fragmentManager: FragmentManager) = with(itemView) {
                val mLecturerPeerReviewGroupingGroupId: TextView = itemView.findViewById(
                    R.id.lecturer_peer_review_grouping_group_id
                )
                val mLecturerPeerReviewGroupingNumberOfStudents: TextView = itemView.findViewById(
                    R.id.lecturer_peer_review_grouping_number_of_students
                )
                val mLecturerPeerReviewGroupingIsDoneImage: ImageView = itemView.findViewById(
                    R.id.lecturer_peer_review_grouping_is_done_image
                )

                mLecturerPeerReviewGroupingGroupId.text = lecturerPeerReviewGrouping.group_id
                val stringToPlace = resources.getString(R.string.lecturer_peer_review_groupings_number_of_students_placeholder, lecturerPeerReviewGrouping.student_email_list!!.size)
                mLecturerPeerReviewGroupingNumberOfStudents.text = stringToPlace
                mLecturerPeerReviewGroupingIsDoneImage.isVisible = lecturerPeerReviewGrouping.isDone as Boolean

                itemView.setOnClickListener {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(
                        R.id.main_container,
                        LecturerPeerReviewIndividualStudentList.getNewIndividualListInstance(
                            lecturerPeerReviewGrouping.subject_name,
                            lecturerPeerReviewGrouping.assignment_id,
                            lecturerPeerReviewGrouping.group_id
                        )
                    )
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
    }


}
