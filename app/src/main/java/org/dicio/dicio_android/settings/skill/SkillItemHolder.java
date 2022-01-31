package org.dicio.dicio_android.settings.skill;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import org.dicio.dicio_android.MainActivity;
import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.SkillHandler;
import org.dicio.dicio_android.util.PermissionUtils;
import org.dicio.skill.SkillInfo;

public class SkillItemHolder extends RecyclerView.ViewHolder {

    final AppCompatImageView iconImageView;
    final AppCompatCheckBox checkBox;
    final AppCompatImageView expandImageView;
    final AppCompatImageView permissionsNoticeImageView;
    final TextView permissionsTextView;
    final TextView grantPermissionsTextView;
    final TextView notAvailableTextView;
    final FrameLayout fragmentHolder;

    String currentSkillInfoId;
    boolean expanded;
    boolean grantButtonHidden;

    public SkillItemHolder(@NonNull final LayoutInflater inflater,
                           @NonNull final ViewGroup parent) {
        super(inflater.inflate(R.layout.pref_skill_item, parent, false));
        iconImageView = itemView.findViewById(R.id.skillIconImageView);
        checkBox = itemView.findViewById(R.id.skillCheckBox);
        expandImageView = itemView.findViewById(R.id.expandImageView);
        permissionsNoticeImageView = itemView.findViewById(R.id.permissionsNoticeImageView);
        notAvailableTextView = itemView.findViewById(R.id.notAvailableTextView);
        permissionsTextView = itemView.findViewById(R.id.permissionsTextView);
        grantPermissionsTextView = itemView.findViewById(R.id.grantPermissionsTextView);
        fragmentHolder = itemView.findViewById(R.id.fragmentHolder);
    }

    public void bind(final Fragment fragment, final SkillInfo skillInfo) {
        final Context context = fragment.requireContext();
        currentSkillInfoId = skillInfo.getId();
        expanded = false;

        checkBox.setText(skillInfo.getNameResource());
        // the correct tint is set in the xml
        iconImageView.setImageResource(SkillHandler.getSkillIconResource(skillInfo));
        hidePermissionsViews();
        fragmentHolder.setVisibility(View.GONE);

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
                = PreferenceManager.getDefaultSharedPreferences(context);
        final String preferenceKey = SkillHandler.getIsEnabledPreferenceKey(skillInfo.getId());
        checkBox.setChecked(sharedPreferences.getBoolean(preferenceKey, true));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(preferenceKey, isChecked).apply());

        grantButtonHidden = allPermissionsGranted(context, skillInfo);
        permissionsNoticeImageView.setVisibility(grantButtonHidden ? View.GONE : View.VISIBLE);
        if (!skillInfo.getNeededPermissions().isEmpty()) {
            grantPermissionsTextView.setOnClickListener(view -> {
                // the results of the permissions request will be ignored; just hide the button here
                ActivityCompat.requestPermissions(fragment.requireActivity(),
                        PermissionUtils.permissionsArrayFromSkillInfo(skillInfo),
                        MainActivity.SETTINGS_PERMISSIONS_REQUEST_CODE);

                grantButtonHidden = true;
                permissionsNoticeImageView.setVisibility(View.GONE);
                grantPermissionsTextView.setVisibility(View.GONE);
            });
        }

        if (skillInfo.hasPreferences() || !skillInfo.getNeededPermissions().isEmpty()) {
            showExpandButton(fragment, skillInfo);
        } else {
            expandImageView.setVisibility(View.GONE);
        }
    }

    public void unbind(final Fragment fragment) {
        itemView.setOnClickListener(null);
        removeFragmentInHolderIfPresent(fragment);
        checkBox.setOnCheckedChangeListener(null);
        grantPermissionsTextView.setOnClickListener(null);
    }


    private void showExpandButton(final Fragment fragment, final SkillInfo skillInfo) {
        expandImageView.setRotation(0);
        expandImageView.setVisibility(View.VISIBLE);
        itemView.setOnClickListener(v -> {
            expanded = !expanded;
            if (expanded) {
                fragmentHolder.setVisibility(View.VISIBLE);
                animateExpandImageRotation(180);
                showPermissionsViewsIfNeeded(fragment.requireContext(), skillInfo);
                showFragmentInHolderIfNeeded(fragment, skillInfo);
            } else {
                fragmentHolder.setVisibility(View.GONE);
                animateExpandImageRotation(0);
                hidePermissionsViews();
                removeFragmentInHolderIfPresent(fragment);
            }
        });
    }

    private boolean allPermissionsGranted(final Context context, final SkillInfo skillInfo) {
        return PermissionUtils.checkPermissions(context,
                PermissionUtils.permissionsArrayFromSkillInfo(skillInfo));
    }

    private void hidePermissionsViews() {
        permissionsTextView.setVisibility(View.GONE);
        grantPermissionsTextView.setVisibility(View.GONE);
    }

    private void showPermissionsViewsIfNeeded(final Context context, final SkillInfo skillInfo) {
        if (skillInfo.getNeededPermissions().isEmpty()) {
            return; // nothing to show
        }

        permissionsTextView.setText(context.getString(R.string.pref_skill_missing_permissions,
                PermissionUtils.getCommaJoinedPermissions(context, skillInfo)));
        permissionsTextView.setVisibility(View.VISIBLE);
        grantPermissionsTextView.setVisibility(grantButtonHidden ? View.GONE : View.VISIBLE);
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

    private void showFragmentInHolderIfNeeded(final Fragment fragment, final SkillInfo skillInfo) {
        if (!skillInfo.hasPreferences()) {
            return;
        }
        final Fragment skillFragment = skillInfo.getPreferenceFragment();
        assert skillFragment != null;

        final FrameLayout frame = new FrameLayout(fragment.requireContext());
        frame.setId(View.generateViewId());
        fragmentHolder.addView(frame, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

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
