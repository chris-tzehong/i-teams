package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                if (LoginActivity.isLecturer) {
                    val homeFragment = LecturerHomeFragment.newInstance()
                    openFragment(homeFragment)
                } else {
                    val homeFragment = StudentHomeFragment.newInstance()
                    openFragment(homeFragment)
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_lookup -> {
                val lookupFragment = LookupFragment.newInstance()
                openFragment(lookupFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_peer_review -> {
                if (LoginActivity.isLecturer) {
                    val peerReviewFragment = LecturerPeerReviewListFragment.newInstance()
                    openFragment(peerReviewFragment)
                } else {
                    val peerReviewFragment = StudentPeerReviewListFragment.newInstance()
                    openFragment(peerReviewFragment)
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                val profileFragment = ProfileFragment.newInstance()
                openFragment(profileFragment)
                return@OnNavigationItemSelectedListener true
            }

        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        var toolbar: ActionBar = supportActionBar!!

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mBottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation_bar)
        if (LoginActivity.isLecturer) {
            val homeFragment = LecturerHomeFragment.newInstance()
            openFragment(homeFragment)
        } else {
            val homeFragment = StudentHomeFragment.newInstance()
            openFragment(homeFragment)
        }
        mBottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        val anything = mutableListOf<String>()
//        anything.add("Assignment 1")
//
//        val peerReview = PeerReview("Object-Oriented Programming Fundamentals", "woanningl@sunway.edu.my", "Lim Woan Ning", anything)
//        PeerReview.writeToDatabase(peerReview)
//
//        val studentList = mutableListOf<String>()
//        studentList.add("13054842@imail.sunway.edu.my")
//        studentList.add("16027625@imail.sunway.edu.my")
//        studentList.add("14075236@imail.sunway.edu.my")
//        val peerReviewGrouping = PeerReviewGrouping("Assignment 1", "Group 1", studentList, "default", false, peerReview.subject_name, true)
//        PeerReviewGrouping.writeToDatabase(peerReview, peerReviewGrouping)
//
//        val results = hashMapOf<String, Int>()
//        results["1"] = 5
//        val peerReviewResult = PeerReviewResult("16027625@imail.sunway.edu.my", "13054842@imail.sunway.edu.my", results, "No comment.", true)
//        PeerReviewResult.writeToDatabase(peerReview, peerReviewGrouping, peerReviewResult)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.toolbar_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        const val MAIN_ACTIVITY = "Main Activity"
        const val TAG = "debuggable"
    }
}
