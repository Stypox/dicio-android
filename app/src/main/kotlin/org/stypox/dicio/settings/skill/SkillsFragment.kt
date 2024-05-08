package org.stypox.dicio.settings.skill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.stypox.dicio.R
import org.stypox.dicio.skills.SkillHandler
import org.stypox.dicio.util.ShareUtils
import org.stypox.dicio.util.ShareUtils.openUrlInBrowser

class SkillsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.pref_skills, container, false)
        val skillRecyclerView = view.findViewById<RecyclerView>(R.id.skillRecyclerView)
        val numberLibraryTextView = view.findViewById<TextView>(R.id.numberLibraryTextView)
        skillRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        skillRecyclerView.adapter = SkillsAdapter(
            this, inflater, SkillHandler.allSkillInfoList
        )

        // the skill context should always already have been initialized
        val skillContext = SkillHandler.skillContext
        numberLibraryTextView.isVisible = skillContext.numberParserFormatter == null

        numberLibraryTextView.setOnClickListener {
            openUrlInBrowser(requireContext(), DICIO_NUMBERS_URL)
        }
        numberLibraryTextView.setOnLongClickListener {
            ShareUtils.copyToClipboard(requireContext(), DICIO_NUMBERS_URL)
            true
        }
        return view
    }

    companion object {
        private const val DICIO_NUMBERS_URL = "https://github.com/Stypox/dicio-numbers"
    }
}