package org.dicio.dicio_android.settings.skill;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.dicio.dicio_android.skills.SkillHandler;

public class SkillsAdapter extends RecyclerView.Adapter<SkillItemHolder> {

    final Fragment fragment;
    @NonNull final LayoutInflater inflater;

    public SkillsAdapter(final Fragment fragment, @NonNull final LayoutInflater inflater) {
        this.fragment = fragment;
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public SkillItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new SkillItemHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final SkillItemHolder holder, final int position) {
        holder.bind(fragment, SkillHandler.getSkillInfoList().get(position));
    }

    @Override
    public void onViewRecycled(@NonNull final SkillItemHolder holder) {
        holder.unbind(fragment);
    }

    @Override
    public int getItemCount() {
        return SkillHandler.getSkillInfoList().size();
    }
}
