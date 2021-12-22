package org.dicio.dicio_android.settings.skill;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.SkillHandler;
import org.dicio.skill.SkillInfo;

public class SkillItemHolder extends RecyclerView.ViewHolder {

    final AppCompatImageView iconImageView;
    final AppCompatCheckBox checkBox;
    final AppCompatImageView expandImageView;
    final TextView notAvailableTextView;
    final FrameLayout fragmentHolder;

    String currentSkillInfoId;
    boolean expanded;

    public SkillItemHolder(@NonNull final LayoutInflater inflater,
                           @NonNull final ViewGroup parent) {
        super(inflater.inflate(R.layout.pref_skill_item, parent, false));
        iconImageView = itemView.findViewById(R.id.skillIconImageView);
        checkBox = itemView.findViewById(R.id.skillCheckBox);
        expandImageView = itemView.findViewById(R.id.expandImageView);
        notAvailableTextView = itemView.findViewById(R.id.notAvailableTextView);
        fragmentHolder = itemView.findViewById(R.id.fragmentHolder);
    }

    public void bind(final Fragment fragment, final SkillInfo skillInfo) {
        currentSkillInfoId = skillInfo.getId();
        expanded = false;

        checkBox.setText(skillInfo.getNameResource());
        // the correct tint is set in the xml
        iconImageView.setImageResource(SkillHandler.getSkillIconResource(skillInfo));

        if (skillInfo.isAvailable(SkillHandler.getSkillContext())) {
            checkBox.setEnabled(true);
            notAvailableTextView.setVisibility(View.GONE);
        } else {
            checkBox.setEnabled(false);
            checkBox.setChecked(false);
            notAvailableTextView.setVisibility(View.VISIBLE);
            expandImageView.setVisibility(View.GONE);
            return; // this skill is not available, so it shouldn't be customizable
        }

        final SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext());
        final String preferenceKey = SkillHandler.getIsEnabledPreferenceKey(skillInfo.getId());
        checkBox.setChecked(sharedPreferences.getBoolean(preferenceKey, true));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(preferenceKey, isChecked).apply());

        if (skillInfo.hasPreferences()) {
            expandImageView.setRotation(0);
            expandImageView.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(v -> {
                expanded = !expanded;
                if (expanded) {
                    fragmentHolder.setVisibility(View.VISIBLE);
                    animateExpandImageRotation(180);
                    showFragmentInHolder(fragment, skillInfo);
                } else {
                    fragmentHolder.setVisibility(View.GONE);
                    animateExpandImageRotation(0);
                    removeFragmentInHolderIfPresent(fragment);
                }
            });
        } else {
            expandImageView.setVisibility(View.GONE);
        }
    }

    public void unbind(final Fragment fragment) {
        itemView.setOnClickListener(null);
        removeFragmentInHolderIfPresent(fragment);
        checkBox.setOnCheckedChangeListener(null);
    }


    private void removeFragmentInHolderIfPresent(final Fragment fragment) {
        final FragmentManager fragmentManager = fragment.getChildFragmentManager();
        final Fragment skillFragment = fragmentManager.findFragmentByTag(currentFragmentTag());
        fragmentHolder.removeAllViews();

        if (skillFragment != null) {
            fragment.getChildFragmentManager()
                    .beginTransaction()
                    .remove(skillFragment)
                    .commit();
        }
    }

    private void showFragmentInHolder(final Fragment fragment, final SkillInfo skillInfo) {
        final FrameLayout frame = new FrameLayout(fragment.requireContext());
        frame.setId(View.generateViewId());
        fragmentHolder.addView(frame, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        final Fragment skillFragment = skillInfo.getPreferenceFragment();
        assert skillFragment != null;
        fragment.getChildFragmentManager()
                .beginTransaction()
                .add(frame.getId(), skillFragment, currentSkillInfoId)
                .commit();
    }

    private String currentFragmentTag() {
        return currentSkillInfoId + "_skill_settings_tag";
    }

    private void animateExpandImageRotation(final float rotation) {
        expandImageView.animate()
                .rotation(rotation)
                .setDuration(100)
                .start();
    }
}
