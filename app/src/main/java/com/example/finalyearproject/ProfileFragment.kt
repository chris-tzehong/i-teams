package com.example.finalyearproject


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.Serializable
import java.util.*

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        val mReputationScoreLabel: TextView = view.findViewById(R.id.profile_reputation_score)
        val mProfileNameLabel: TextView = view.findViewById(R.id.profile_name)
        val mProfileIdLabel: TextView = view.findViewById(R.id.profile_id)
        val mProfileEmailLabel: TextView = view.findViewById(R.id.profile_email)
        val mProfileCourseOrDepartmentLabel: TextView = view.findViewById(R.id.profile_course_or_department)
        val mProfileDobLabel: TextView = view.findViewById(R.id.profile_dob)
        val mProfileContactLabel: TextView = view.findViewById(R.id.profile_contact)
        val mProfileChangePasswordButton: Button = view.findViewById(R.id.profile_change_password_button)
        val mRootLayout: FrameLayout = view.findViewById(R.id.root_layout_popup)

        val auth = FirebaseAuth.getInstance()
        mRootLayout.foreground.alpha = 0

        if (arguments != null) {
            val student = arguments!!.getSerializable(OTHER_PROFILE) as Student
            mReputationScoreLabel.isVisible = true
            val reputation = getString(R.string.profile_reputation_score_placeholder, student.student_reputation)
            mReputationScoreLabel.text = reputation
            mProfileNameLabel.text = student.student_name
            mProfileIdLabel.text = student.student_id.toString()
            mProfileEmailLabel.text = student.student_email
            mProfileCourseOrDepartmentLabel.text = student.student_course
            mProfileDobLabel.isVisible = false
            mProfileContactLabel.isVisible = false
            mProfileChangePasswordButton.isVisible = false
        } else {
            if (LoginActivity.isLecturer) {
                var lecturer = LoginActivity.appUser as Lecturer
                mReputationScoreLabel.isVisible = false
                mProfileNameLabel.text = lecturer.lecturer_name
                mProfileIdLabel.text = lecturer.lecturer_id
                mProfileEmailLabel.text = lecturer.lecturer_email
                mProfileCourseOrDepartmentLabel.text = lecturer.lecturer_department
                mProfileDobLabel.text = convertDob(lecturer.lecturer_dob)
                val contact = getString(R.string.profile_contact_placeholder, lecturer.lecturer_contact)
                mProfileContactLabel.text = contact
            } else {
                var student = LoginActivity.appUser as Student
                mReputationScoreLabel.isVisible = true
                val reputation = getString(R.string.profile_reputation_score_placeholder, student.student_reputation)
                mReputationScoreLabel.text = reputation
                mProfileNameLabel.text = student.student_name
                mProfileIdLabel.text = student.student_id.toString()
                mProfileEmailLabel.text = student.student_email
                mProfileCourseOrDepartmentLabel.text = student.student_course
                mProfileDobLabel.text = convertDob(student.student_dob)
                val contact = getString(R.string.profile_contact_placeholder, student.student_contact)
                mProfileContactLabel.text = contact
            }
        }

        mProfileChangePasswordButton.setOnClickListener {
            val inflater: LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.popup_change_password, null)
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

            val mNewPasswordEditText: EditText = view.findViewById(R.id.profile_new_password)
            val mConfirmPasswordEditText: EditText = view.findViewById(R.id.profile_confirm_password)
            val mCancelChangePassword: Button = view.findViewById(R.id.profile_cancel_change_password_button)
            val mConfirmChangePassword: Button = view.findViewById(R.id.profile_confirm_change_password_button)

            mCancelChangePassword.setOnClickListener {
                mRootLayout.foreground.alpha = 0
                popupWindow.dismiss()
            }

            mConfirmChangePassword.setOnClickListener {
                val newPassword = mNewPasswordEditText.text.toString()
                val confirmPassword = mConfirmPasswordEditText.text.toString()
                when {
                    newPassword.length < 6 -> Toast.makeText(context, R.string.profile_password_too_short_error, Toast.LENGTH_SHORT).show()
                    confirmPassword != newPassword -> Toast.makeText(context, R.string.profile_password_not_match_error, Toast.LENGTH_SHORT).show()
                    else -> {
                        val currentUser = auth.currentUser
                        currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(PROFILE_FRAGMENT, "User password updated.")
                                Toast.makeText(context, R.string.profile_successful_update_password, Toast.LENGTH_SHORT).show()
                                mRootLayout.foreground.alpha = 0
                                popupWindow.dismiss()
                            } else {
                                Log.d(PROFILE_FRAGMENT, "User password failed to update.")
                                Toast.makeText(context, R.string.profile_failure_update_password, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            TransitionManager.beginDelayedTransition(root_layout_popup)
            popupWindow.showAtLocation(root_layout_popup, Gravity.CENTER, 0, 0)
            mRootLayout.foreground.alpha = 200
        }




        return view
    }

    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
        fun newOtherProfileInstance(serializable: Serializable): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putSerializable(OTHER_PROFILE, serializable)
            fragment.arguments = args
            return fragment
        }

        private const val PROFILE_FRAGMENT = "Profile Fragment"
        const val OTHER_PROFILE = "student profile"
    }

    fun convertDob(date: Date?): String {
        val calendar = GregorianCalendar()
        calendar.time = date
        return getString(R.string.date_placeholder, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }


}
