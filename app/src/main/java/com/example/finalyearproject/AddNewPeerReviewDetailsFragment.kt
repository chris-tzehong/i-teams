package com.example.finalyearproject


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_lecturer_peer_review_list.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder

/**
 * A simple [Fragment] subclass.
 */
class AddNewPeerReviewDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_add_new_peer_review_details, container, false)

        val mAddNewPeerReviewSubjectNameEditText: EditText = view.findViewById(R.id.add_new_peer_review_subject_name)
        val mAddNewPeerReviewAssignmentIdEditText: EditText = view.findViewById(R.id.add_new_peer_review_assignment_id)
        val mAddNewPeerReviewUploadListButton: Button = view.findViewById(R.id.add_new_peer_review_upload_button)
        val mAddNewPeerReviewProceedButton: Button = view.findViewById(R.id.add_new_peer_review_proceed_button)
        val db = FirebaseFirestore.getInstance()
        val lecturer = LoginActivity.appUser as Lecturer
        temporaryPeerReviewGroupingsHolder = mutableListOf()

        mAddNewPeerReviewUploadListButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/csv"
            startActivityForResult(intent, OPEN_REQUEST_CODE)
        }

        mAddNewPeerReviewProceedButton.setOnClickListener {
            when {
                mAddNewPeerReviewSubjectNameEditText.text.isEmpty() -> Toast.makeText(activity, R.string.add_new_peer_review_details_empty_subject_name_error, Toast.LENGTH_SHORT).show()
                mAddNewPeerReviewAssignmentIdEditText.text.isEmpty() -> Toast.makeText(activity, R.string.add_new_peer_review_details_empty_assignment_id_error, Toast.LENGTH_SHORT).show()
                temporaryPeerReviewGroupingsHolder.isEmpty() -> Toast.makeText(activity, R.string.add_new_peer_review_details_empty_student_list_error, Toast.LENGTH_SHORT).show()
                else -> {
                    val peerReview = PeerReview(mAddNewPeerReviewSubjectNameEditText.text.toString(), lecturer.lecturer_email, lecturer.lecturer_name)
                    db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(mAddNewPeerReviewSubjectNameEditText.text.toString()).set(peerReview).addOnSuccessListener {
                        temporaryPeerReviewGroupingsHolder.forEach { item ->
                            item.subject_name = mAddNewPeerReviewSubjectNameEditText.text.toString()
                            item.assignment_id = mAddNewPeerReviewAssignmentIdEditText.text.toString()
                            item.isDone = false
                            item.isReleased = false
                            item.question_set_name = "default"
                            db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(mAddNewPeerReviewSubjectNameEditText.text.toString()).collection(mAddNewPeerReviewAssignmentIdEditText.text.toString()).document(item.group_id.toString()).set(item).addOnSuccessListener {
                                Log.d(MainActivity.TAG, "Item successfully written!")
                                db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(mAddNewPeerReviewSubjectNameEditText.text.toString()).update("lecturer_name", lecturer.lecturer_name).addOnSuccessListener {
                                    Log.d(MainActivity.TAG, "Lecturer name updated!")
                                }
                                db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(mAddNewPeerReviewSubjectNameEditText.text.toString()).update("lecturer_email", lecturer.lecturer_email)
                                db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(mAddNewPeerReviewSubjectNameEditText.text.toString()).update("assignment_id", FieldValue.arrayUnion(mAddNewPeerReviewAssignmentIdEditText.text.toString()))
                                db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(mAddNewPeerReviewSubjectNameEditText.text.toString()).update("subject_name", mAddNewPeerReviewSubjectNameEditText.text.toString())
                            }.addOnFailureListener {
                                Log.d(MainActivity.TAG, "Fail to write into database.")
                                Toast.makeText(activity, R.string.add_new_peer_review_details_fail_to_create_error, Toast.LENGTH_SHORT).show()
                            }
                        }
                        val transaction = fragmentManager!!.beginTransaction()
                        transaction.replace(R.id.main_container, AddNewPeerReviewQuestionSetFragment.getNewAddPeerReviewQuestionSetInstance(mAddNewPeerReviewSubjectNameEditText.text.toString(), mAddNewPeerReviewAssignmentIdEditText.text.toString()))
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }


                }
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        var currentUri: Uri? = null
        var studentEmailList = mutableListOf<String>()
        val peerReviewGroupings = mutableListOf<PeerReviewGrouping>()

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_REQUEST_CODE) {
                data?.let {
                    currentUri = it.data

                    try {
                        val content = readCsvFile(currentUri as Uri)
                        for (i in content.indices) {
                            if (i != content.size - 1) {
                                if (content[i].group_id == content[i+1].group_id) {
                                    studentEmailList.add(content[i].email.toString())
                                } else {
                                    studentEmailList.add(content[i].email.toString())
                                    val peerReviewGrouping = PeerReviewGrouping(group_id = content[i].group_id.toString(), student_email_list = studentEmailList)
                                    peerReviewGroupings.add(peerReviewGrouping)
                                    studentEmailList = mutableListOf()
                                }
                            } else {
                                studentEmailList.add(content[i].email.toString())
                                val peerReviewGrouping = PeerReviewGrouping(group_id = content[i].group_id.toString(), student_email_list = studentEmailList)
                                peerReviewGroupings.add(peerReviewGrouping)
                            }
                        }
                        temporaryPeerReviewGroupingsHolder = peerReviewGroupings
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun readCsvFile(uri: Uri): List<GroupHolder> {
        val inputStream = context!!.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val groupHolders = mutableListOf<GroupHolder>()

        var currentLine = reader.readLine()

        while (currentLine != null) {
            val tokens = currentLine.split(",")
            val groupHolder = GroupHolder(tokens[0], tokens[1])
            groupHolders.add(groupHolder)
            currentLine = reader.readLine()
        }
        inputStream!!.close()
        return groupHolders

    }

    companion object {
        private val OPEN_REQUEST_CODE = 42
        private var temporaryPeerReviewGroupingsHolder = mutableListOf<PeerReviewGrouping>()

        fun newInstance(): AddNewPeerReviewDetailsFragment = AddNewPeerReviewDetailsFragment()
    }

    data class GroupHolder(var group_id: String? = null, var email: String? = null)




}
