package org.dicio.dicio_android.settings.skill;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.dicio.dicio_android.skills.SkillHandler;
import org.dicio.skill.SkillInfo;

import java.util.List;

public class SkillsAdapter extends RecyclerView.Adapter<SkillItemHolder> {

    final Fragment fragment;
    @NonNull final LayoutInflater inflater;
    final List<SkillInfo> skillInfoList;

    public SkillsAdapter(final Fragment fragment,
                         @NonNull final LayoutInflater inflater,
                         final List<SkillInfo> skillInfoList) {
        this.fragment = fragment;
        this.inflater = inflater;
        this.skillInfoList = skillInfoList;
    }

    @NonNull
    @Override
    public SkillItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new SkillItemHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final SkillItemHolder holder, final int position) {
        holder.bind(fragment, skillInfoList.get(position));
    }

    @Override
    public void onViewRecycled(@NonNull final SkillItemHolder holder) {
        holder.unbind(fragment);
    }

    @Override
    public int getItemCount() {
        return skillInfoList.size();
    }
}
