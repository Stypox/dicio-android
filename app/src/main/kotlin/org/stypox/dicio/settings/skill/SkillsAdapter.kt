package org.stypox.dicio.settings.skill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.dicio.skill.SkillInfo

class SkillsAdapter(
    private val fragment: Fragment,
    private val inflater: LayoutInflater,
    private val skillInfoList: List<SkillInfo>
) : RecyclerView.Adapter<SkillItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillItemHolder {
        return SkillItemHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: SkillItemHolder, position: Int) {
        holder.bind(fragment, skillInfoList[position])
    }

    override fun onViewRecycled(holder: SkillItemHolder) {
        holder.unbind(fragment)
    }

    override fun getItemCount(): Int {
        return skillInfoList.size
    }
}