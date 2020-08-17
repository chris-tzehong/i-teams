package com.example.finalyearproject.lookup


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.profile.ProfileFragment
import com.example.finalyearproject.R
import com.example.finalyearproject.model.Student
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class LookupFragment : Fragment() {

    private var fireStoreListener: ListenerRegistration? = null
    private var allAccessList: MutableList<Student>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_lookup, container, false)

        val mStudentRecyclerView: RecyclerView = view.findViewById(R.id.search_results_recycler_view)
        mStudentRecyclerView.layoutManager = LinearLayoutManager(activity)

        val db = FirebaseFirestore.getInstance()

        var mInitialStudents: MutableList<Student> = Student.readAllFromDatabase()
        val mInitialAdapter =
            StudentAdapter(
                activity,
                mInitialStudents
            )
        val mSearchResultsLabel: TextView = view.findViewById(R.id.search_results_label)
        mStudentRecyclerView.adapter = mInitialAdapter
        mStudentRecyclerView.isVisible = false
        mSearchResultsLabel.isVisible = false

        fireStoreListener = db.collection(Student.STUDENT_COLLECTION).addSnapshotListener(
            EventListener { documentSnapshots, e ->
                if (e != null) {
                    Log.e(LOOKUP_FRAGMENT,"LISTEN", e)
                    return@EventListener
                }

                val mStudents = mutableListOf<Student>()
                for (doc in documentSnapshots!!) {
                    val student = doc.toObject(Student::class.java)
                    mStudents.add(student)
                }
                mInitialStudents = mStudents
                allAccessList = mStudents
                val mAdapter =
                    StudentAdapter(
                        activity,
                        mStudents
                    )
                mStudentRecyclerView.adapter = mAdapter

            })

        val mSearchBar: EditText = view.findViewById(R.id.lookup_search_bar)
        mSearchBar.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var filteredResults = filterResults(mInitialStudents, s.toString())
                val mFilteredAdapter =
                    StudentAdapter(
                        activity,
                        filteredResults
                    )
                mStudentRecyclerView.adapter = mFilteredAdapter
                mStudentRecyclerView.isVisible = true
                mSearchResultsLabel.isVisible = true
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    mStudentRecyclerView.isVisible = false
                    mSearchResultsLabel.isVisible = false
                }
            }

        })

        return view
    }

    companion object {
        fun newInstance(): LookupFragment =
            LookupFragment()
        private const val LOOKUP_FRAGMENT = "lookup fragment"
    }

    override fun onDestroy() {
        super.onDestroy()
        fireStoreListener!!.remove()
    }

    class StudentAdapter(private val context: FragmentActivity?, private val students: List<Student>):
        RecyclerView.Adapter<StudentAdapter.StudentHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
            return StudentHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_student,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return students.size
        }

        override fun onBindViewHolder(holder: StudentHolder, position: Int) {
            val student = students[position]
            holder.bind(student, context!!.supportFragmentManager)
        }

        class StudentHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            fun bind(student: Student, fm: FragmentManager) = with(itemView) {
                val mStudentNameLabel: TextView = findViewById(R.id.lookup_student_name_label)
                val mStudentEmailLabel: TextView = findViewById(R.id.lookup_student_email_label)
                mStudentNameLabel.text = student.student_name
                mStudentEmailLabel.text = student.student_email
                itemView.setOnClickListener {
                    val transaction = fm.beginTransaction()
                    transaction.replace(
                        R.id.main_container,
                        ProfileFragment.newOtherProfileInstance(
                            student
                        )
                    )
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

            }

        }
    }

    fun filterResults(students: List<Student>, searchResult: String): List<Student> {
        val filteredStudents = mutableListOf<Student>()
        for (student in students) {
            if (student.student_name!!.toLowerCase().contains(searchResult.toLowerCase())) {
                filteredStudents.add(student)
            }
        }
        return filteredStudents
    }

    override fun onResume() {
        super.onResume()
        val mSearchResultsLabel: TextView = view!!.findViewById(R.id.search_results_label)
        val mLookupRecyclerView: RecyclerView = view!!.findViewById(R.id.search_results_recycler_view)
        val mSearchBar: EditText = view!!.findViewById(R.id.lookup_search_bar)
        mSearchResultsLabel.isVisible = false
        mLookupRecyclerView.isVisible = false
        mSearchBar.setText("")

    }


}
