package com.example.finalyearproject.home


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.R
import com.example.finalyearproject.model.Announcement
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class LecturerHomeFragment : Fragment() {

    private var fireStoreListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_lecturer_home, container, false)

        val mAnnouncementRecyclerView: RecyclerView = view.findViewById(R.id.announcement_list_student)
        mAnnouncementRecyclerView.layoutManager = LinearLayoutManager(activity)

        val db = FirebaseFirestore.getInstance()
        val mAddAnnouncementButton: FloatingActionButton = view.findViewById(R.id.add_announcement_button)

        mAddAnnouncementButton.setOnClickListener {
            val transaction = fragmentManager!!.beginTransaction()
            transaction.replace(R.id.main_container, AddAnnouncementFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val mInitialAnnouncements: MutableList<Announcement> = Announcement.readAllFromDatabase()

        val mInitialAdapter =
            AnnouncementAdapter(
                activity,
                mInitialAnnouncements
            )
        mAnnouncementRecyclerView.adapter = mInitialAdapter

        fireStoreListener = db.collection(Announcement.ANNOUNCEMENT_COLLECTION).addSnapshotListener(
            EventListener { documentSnapshots, e ->
                if (e != null) {
                    Log.e(HOME_FRAGMENT, "LISTEN", e)
                    return@EventListener
                }

                val mAnnouncements: MutableList<Announcement> = mutableListOf()

                for (doc in documentSnapshots!!) {
                    val announcement = doc.toObject(Announcement::class.java)
                    announcement.announcement_id = doc.id
                    db.collection(Announcement.ANNOUNCEMENT_COLLECTION).document(doc.id).update("announcement_id", doc.id)
                    mAnnouncements.add(announcement)
                }
                val mAdapter =
                    AnnouncementAdapter(
                        activity,
                        mAnnouncements
                    )
                mAnnouncementRecyclerView.adapter = mAdapter
            })

        return view

    }

    override fun onDestroy() {
        super.onDestroy()
        fireStoreListener!!.remove()
    }

    companion object {
        fun newInstance(): LecturerHomeFragment =
            LecturerHomeFragment()
        const val HOME_FRAGMENT = "Home Fragment"
    }

    class AnnouncementAdapter(private val context: FragmentActivity?, private val announcements: List<Announcement>): RecyclerView.Adapter<AnnouncementAdapter.AnnouncementHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementHolder {
            return AnnouncementHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.list_item_announcement,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return announcements.size
        }

        override fun onBindViewHolder(holder: AnnouncementHolder, position: Int) {
            val announcement = announcements[position]
            holder.bind(announcement)
        }

        class AnnouncementHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(announcement: Announcement) = with(itemView) {
                val mAnnouncementTitle: TextView = itemView.findViewById(R.id.announcement_title)
                mAnnouncementTitle.text = announcement.announcement_title
                val mAnnouncementDate: TextView = itemView.findViewById(R.id.announcement_date)
                mAnnouncementDate.text = announcement.announcement_date.toString()
                val mAnnouncementContent: TextView = itemView.findViewById(R.id.announcement_content)
                mAnnouncementContent.text = announcement.announcement_content
                val mAnnouncerName: TextView = itemView.findViewById(R.id.announcer_name)
                mAnnouncerName.text = announcement.announcer_name
            }
        }
    }


}
