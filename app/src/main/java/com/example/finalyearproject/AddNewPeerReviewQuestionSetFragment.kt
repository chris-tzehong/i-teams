package com.example.finalyearproject


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_add_new_peer_review_question_set.*

class AddNewPeerReviewQuestionSetFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_add_new_peer_review_question_set, container, false)
        val mAddNewPeerReviewQuestionSetRadioGroup: RadioGroup = view.findViewById(R.id.add_new_peer_review_question_set_radio_group)
        val mAddNewPeerReviewQuestionDefaultSetRadioButton: RadioButton = view.findViewById(R.id.add_new_peer_review_default_question_set_radio_button)
        val mAddNewPeerReviewQuestionCustomSetRadioButton: RadioButton = view.findViewById(R.id.add_new_peer_review_custom_question_set_radio_button)
        val mAddNewPeerReviewQuestionSetName: EditText = view.findViewById(R.id.add_new_peer_review_custom_question_set_name)
        val mAddNewPeerReviewQuestionSetAddCustomQuestionButton: Button = view.findViewById(R.id.add_new_peer_review_add_custom_question_button)
        val mAddNewPeerReviewQuestionCreateSessionButton: Button = view.findViewById(R.id.add_new_peer_review_create_session_button)
        val mAddNewPeerReviewQuestionDefaultSetRecyclerView: RecyclerView = view.findViewById(R.id.add_new_peer_review_default_question_set_recycler_view)
        val mAddNewPeerReviewQuestionCustomSetRecyclerView: RecyclerView = view.findViewById(R.id.add_new_peer_review_custom_question_set_recycler_view)
        val mAddNewPeerReviewQuestionSetNameLabel: TextView = view.findViewById(R.id.add_new_peer_review_custom_question_set_name_label)
        val mAddNewPeerReviewRootLayout: FrameLayout = view.findViewById(R.id.add_new_custom_question_root_layout)
        val questions = mutableListOf<String>()
        var questionSetName: String? = null
        val db = FirebaseFirestore.getInstance()
        val subjectName = arguments!!.getString(SUBJECT_NAME)
        val assignmentId = arguments!!.getString(ASSIGNMENT_ID)
        Log.d("wtf", assignmentId)
        val peerReviewGroupings = mutableListOf<PeerReviewGrouping>()

        mAddNewPeerReviewQuestionDefaultSetRecyclerView.layoutManager = LinearLayoutManager(activity)
        mAddNewPeerReviewQuestionCustomSetRecyclerView.layoutManager = LinearLayoutManager(activity)
        val mCustomQuestionAdapter = QuestionAdapter(activity, questions)
        mAddNewPeerReviewQuestionCustomSetRecyclerView.adapter = mCustomQuestionAdapter

        mAddNewPeerReviewRootLayout.foreground.alpha = 0

        mAddNewPeerReviewQuestionSetRadioGroup.check(R.id.add_new_peer_review_default_question_set_radio_button)
        mAddNewPeerReviewQuestionDefaultSetRecyclerView.isVisible = true
        mAddNewPeerReviewQuestionCustomSetRecyclerView.isVisible = false
        mAddNewPeerReviewQuestionSetAddCustomQuestionButton.isVisible = false
        mAddNewPeerReviewQuestionSetNameLabel.isVisible = false
        mAddNewPeerReviewQuestionSetName.isVisible = false
        mAddNewPeerReviewQuestionSetRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.add_new_peer_review_default_question_set_radio_button -> {
                    mAddNewPeerReviewQuestionDefaultSetRecyclerView.isVisible = true
                    mAddNewPeerReviewQuestionCustomSetRecyclerView.isVisible = false
                    mAddNewPeerReviewQuestionSetAddCustomQuestionButton.isVisible = false
                    mAddNewPeerReviewQuestionSetNameLabel.isVisible = false
                    mAddNewPeerReviewQuestionSetName.isVisible = false
                }
                R.id.add_new_peer_review_custom_question_set_radio_button -> {
                    mAddNewPeerReviewQuestionDefaultSetRecyclerView.isVisible = false
                    mAddNewPeerReviewQuestionCustomSetRecyclerView.isVisible = true
                    mAddNewPeerReviewQuestionSetAddCustomQuestionButton.isVisible = true
                    mAddNewPeerReviewQuestionSetNameLabel.isVisible = true
                    mAddNewPeerReviewQuestionSetName.isVisible = true
                }
            }
        }

        db.collection(PeerReviewQuestionSet.PEER_REVIEW_QUESTION_SET_COLLECTION).document("default").get().addOnSuccessListener { documentSnapshot ->
            val questionSet = documentSnapshot.toObject(PeerReviewQuestionSet::class.java)
            val mQuestionAdapter = QuestionAdapter(activity, questionSet!!.question_list as List<String>)
            mAddNewPeerReviewQuestionDefaultSetRecyclerView.adapter = mQuestionAdapter
        }

        mAddNewPeerReviewQuestionSetAddCustomQuestionButton.setOnClickListener {
            val inflater: LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.popup_add_question, null)
            val popupWindow = PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            popupWindow.isFocusable = true
            popupWindow.update()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut
            }

            val mAddNewCustomQuestion: EditText = view.findViewById(R.id.popup_add_question_question)
            val mConfirmAddNewCustomQuestionButton: Button = view.findViewById(R.id.popup_add_question_confirm_button)
            val mCancelAddCustomQuestionButton: Button = view.findViewById(R.id.popup_add_question_cancel_button)

            mCancelAddCustomQuestionButton.setOnClickListener {
                mAddNewPeerReviewRootLayout.foreground.alpha = 0
                popupWindow.dismiss()
            }

            mConfirmAddNewCustomQuestionButton.setOnClickListener {
                if (mAddNewCustomQuestion.text.isEmpty()) {
                    Toast.makeText(activity, R.string.add_new_peer_review_empty_new_question_error, Toast.LENGTH_SHORT).show()
                } else {
                    questions.add(mAddNewCustomQuestion.text.toString())
                    mCustomQuestionAdapter.notifyDataSetChanged()
                    mAddNewPeerReviewRootLayout.foreground.alpha = 0
                    popupWindow.dismiss()
                }
            }

            TransitionManager.beginDelayedTransition(add_new_custom_question_root_layout)
            popupWindow.showAtLocation(add_new_custom_question_root_layout, Gravity.CENTER, 0, 0)
            mAddNewPeerReviewRootLayout.foreground.alpha = 200
        }

        mAddNewPeerReviewQuestionCreateSessionButton.setOnClickListener {
            if (mAddNewPeerReviewQuestionSetRadioGroup.checkedRadioButtonId == R.id.add_new_peer_review_default_question_set_radio_button) {
                questionSetName = "default"
            } else {
                questionSetName = mAddNewPeerReviewQuestionSetName.text.toString()
                val questionSet = PeerReviewQuestionSet(questionSetName, questions)
                db.collection(PeerReviewQuestionSet.PEER_REVIEW_QUESTION_SET_COLLECTION).document(questionSetName.toString()).set(questionSet).addOnSuccessListener {
                    Log.d(MainActivity.TAG, "Successfully written into database!")
                }
            }
            db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        val peerReviewGrouping = doc.toObject(PeerReviewGrouping::class.java)
                        peerReviewGroupings.add(peerReviewGrouping)
                    }
                    peerReviewGroupings.forEach { item ->
                        db.collection(PeerReview.PEER_REVIEW_COLLECTION).document(subjectName.toString()).collection(assignmentId.toString()).document(item.group_id.toString()).update("question_set_name", questionSetName).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(MainActivity.TAG, "Successfully updated!")
                            } else {
                                Log.d(MainActivity.TAG, "Failure in updating documents.")
                            }
                        }
                    }
                }
            }
            val transaction = fragmentManager!!.beginTransaction()
            transaction.replace(R.id.main_container, LecturerPeerReviewListFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    companion object {
        const val SUBJECT_NAME = "add new peer review question set subject name"
        const val ASSIGNMENT_ID = "add new peer review question set assignment id"

        fun newInstance(): AddNewPeerReviewDetailsFragment = AddNewPeerReviewDetailsFragment()

        fun getNewAddPeerReviewQuestionSetInstance(subjectName: String, assignmentId: String): AddNewPeerReviewQuestionSetFragment {
            val fragment = AddNewPeerReviewQuestionSetFragment()
            val args = Bundle()
            args.putString(SUBJECT_NAME, subjectName)
            args.putString(ASSIGNMENT_ID, assignmentId)
            fragment.arguments = args
            return fragment
        }

    }

    class QuestionAdapter(private val context: FragmentActivity?, private val questions: List<String>): RecyclerView.Adapter<QuestionAdapter.QuestionHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionHolder {
            return QuestionHolder(LayoutInflater.from(context).inflate(R.layout.list_item_add_new_peer_review_question, parent, false))
        }

        override fun getItemCount(): Int {
            return questions.size
        }

        override fun onBindViewHolder(holder: QuestionHolder, position: Int) {
            val question = questions[position]
            holder.bind(question)
        }

        class QuestionHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(question: String) = with(itemView) {
                val mAddNewPeerReviewQuestionLabel: TextView = itemView.findViewById(R.id.add_new_peer_review_questions_label)
                mAddNewPeerReviewQuestionLabel.text = question
            }
        }
    }


}
