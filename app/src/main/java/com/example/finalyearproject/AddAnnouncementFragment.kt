package com.example.finalyearproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class AddAnnouncementFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_add_announcement, container, false)

        val mAddAnnouncementTitle: EditText = view.findViewById(R.id.add_announcement_title_label)
        val mAddAnnouncementContent: EditText = view.findViewById(R.id.add_announcement_content)
        val mAddAnnouncementAnnouncerLabel: TextView = view.findViewById(R.id.add_announcement_announcer_label)
        val mAddAnnouncementSubmit: Button = view.findViewById(R.id.add_announcement_submit_button)
        val mAddAnnouncementCancel: Button = view.findViewById(R.id.add_announcement_cancel_button)

        val auth = FirebaseAuth.getInstance()
        val user = LoginActivity.appUser as Lecturer

        val announcerLabelText = String.format(resources.getString(R.string.add_announcement_announcer_placeholder), user.lecturer_name)

        mAddAnnouncementAnnouncerLabel.text = announcerLabelText

        mAddAnnouncementCancel.setOnClickListener {
            fragmentManager!!.popBackStackImmediate()
        }

        mAddAnnouncementSubmit.setOnClickListener {
            val announcement = Announcement(
                announcement_title = mAddAnnouncementTitle.text.toString(),
                announcement_content = mAddAnnouncementContent.text.toString(),
                announcer_name = user.lecturer_name,
                announcement_date = Date()
            )
            Announcement.writeToDatabase(announcement)
            val transaction = fragmentManager!!.beginTransaction()
            transaction.replace(R.id.main_container, LecturerHomeFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    companion object {
        fun newInstance(): AddAnnouncementFragment = AddAnnouncementFragment()

    }
}
