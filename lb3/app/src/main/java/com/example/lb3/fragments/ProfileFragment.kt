package com.example.lb3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.lb3.R
import com.example.lb3.activities.LoginActivity
import com.example.lb3.data.PreferencesManager

class ProfileFragment : Fragment() {

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        val user = prefs.getCurrentUser()
        if (user == null) {
            requireActivity().finish()
            return
        }

        view.findViewById<TextView>(R.id.user_name).text = user.name
        view.findViewById<TextView>(R.id.user_email).text = user.email
        view.findViewById<TextView>(R.id.avatar_text).text = user.name.take(1)

        val enrolled = prefs.getEnrolledCount(user.id)
        val completed = prefs.getCompletedCount(user.id)
        
        view.findViewById<TextView>(R.id.progress_stats).text = 
            getString(R.string.progress_format, completed, enrolled)

        val progress = if (enrolled > 0) (completed * 100) / enrolled else 0
        view.findViewById<ProgressBar>(R.id.overall_progress).progress = progress

        view.findViewById<Button>(R.id.logout_button).setOnClickListener {
            prefs.logout()
            startActivity(LoginActivity.intent(requireContext()))
            requireActivity().finish()
        }
    }
}
