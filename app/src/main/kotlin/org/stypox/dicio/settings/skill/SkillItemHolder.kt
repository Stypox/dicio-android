package org.stypox.dicio.settings.skill

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.MainActivity
import org.stypox.dicio.R
import org.stypox.dicio.skills.SkillHandler
import org.stypox.dicio.util.PermissionUtils

class SkillItemHolder(
    inflater: LayoutInflater,
    parent: ViewGroup
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.pref_skill_item, parent, false)) {
    private val iconImageView: AppCompatImageView = itemView.findViewById(R.id.skillIconImageView)
    private val checkBox: AppCompatCheckBox = itemView.findViewById(R.id.skillCheckBox)
    private val expandImageView: AppCompatImageView = itemView.findViewById(R.id.expandImageView)
    private val permissionsNoticeImageView: AppCompatImageView =
        itemView.findViewById(R.id.permissionsNoticeImageView)
    private val permissionsTextView: TextView = itemView.findViewById(R.id.permissionsTextView)
    private val grantPermissionsTextView: TextView =
        itemView.findViewById(R.id.grantPermissionsTextView)
    private val notAvailableTextView: TextView = itemView.findViewById(R.id.notAvailableTextView)
    private val fragmentHolder: FrameLayout = itemView.findViewById(R.id.fragmentHolder)
    private var currentSkillInfoId: String? = null
    private var expanded = false
    private var grantButtonHidden = false

    fun bind(fragment: Fragment, skillInfo: SkillInfo) {
        val context = fragment.requireContext()
        currentSkillInfoId = skillInfo.id
        expanded = false
        checkBox.setText(skillInfo.nameResource)
        // the correct tint is set in the xml
        iconImageView.setImageResource(SkillHandler.getSkillIconResource(skillInfo))
        hidePermissionsViews()
        fragmentHolder.visibility = View.GONE
        if (skillInfo.isAvailable(SkillHandler.skillContext)) {
            checkBox.isEnabled = true
            notAvailableTextView.visibility = View.GONE
        } else {
            checkBox.isEnabled = false
            checkBox.isChecked = false
            notAvailableTextView.visibility = View.VISIBLE
            expandImageView.visibility = View.GONE
            permissionsNoticeImageView.visibility = View.GONE
            return  // this skill is not available, so it shouldn't be customizable
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val preferenceKey = SkillHandler.getIsEnabledPreferenceKey(skillInfo.id)
        checkBox.isChecked = sharedPreferences.getBoolean(preferenceKey, true)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(preferenceKey, isChecked).apply()
        }
        grantButtonHidden = allPermissionsGranted(context, skillInfo)
        permissionsNoticeImageView.visibility = if (grantButtonHidden) View.GONE else View.VISIBLE
        if (skillInfo.neededPermissions.isNotEmpty()) {
            grantPermissionsTextView.setOnClickListener {
                // the results of the permissions request will be ignored; just hide the button here
                ActivityCompat.requestPermissions(
                    fragment.requireActivity(),
                    skillInfo.neededPermissions.toTypedArray(),
                    MainActivity.SETTINGS_PERMISSIONS_REQUEST_CODE
                )
                grantButtonHidden = true
                permissionsNoticeImageView.visibility = View.GONE
                grantPermissionsTextView.visibility = View.GONE
            }
        }
        if (skillInfo.hasPreferences || skillInfo.neededPermissions.isNotEmpty()) {
            showExpandButton(fragment, skillInfo)
        } else {
            expandImageView.visibility = View.GONE
        }
    }

    fun unbind(fragment: Fragment) {
        expandImageView.setOnClickListener(null)
        removeFragmentInHolderIfPresent(fragment)
        checkBox.setOnCheckedChangeListener(null)
        grantPermissionsTextView.setOnClickListener(null)
    }

    private fun showExpandButton(fragment: Fragment, skillInfo: SkillInfo) {
        expandImageView.rotation = 0f
        expandImageView.visibility = View.VISIBLE
        expandImageView.setOnClickListener {
            expanded = !expanded
            if (expanded) {
                fragmentHolder.visibility = View.VISIBLE
                animateExpandImageRotation(180f)
                showPermissionsViewsIfNeeded(fragment.requireContext(), skillInfo)
                showFragmentInHolderIfNeeded(fragment, skillInfo)
            } else {
                fragmentHolder.visibility = View.GONE
                animateExpandImageRotation(0f)
                hidePermissionsViews()
                removeFragmentInHolderIfPresent(fragment)
            }
        }
    }

    private fun allPermissionsGranted(context: Context, skillInfo: SkillInfo): Boolean {
        return PermissionUtils.checkPermissions(
            context,
            *skillInfo.neededPermissions.toTypedArray()
        )
    }

    private fun hidePermissionsViews() {
        permissionsTextView.visibility = View.GONE
        grantPermissionsTextView.visibility = View.GONE
    }

    private fun showPermissionsViewsIfNeeded(context: Context, skillInfo: SkillInfo) {
        if (skillInfo.neededPermissions.isEmpty()) {
            return  // nothing to show
        }
        permissionsTextView.text = context.getString(
            R.string.pref_skill_missing_permissions,
            PermissionUtils.getCommaJoinedPermissions(context, skillInfo)
        )
        permissionsTextView.visibility = View.VISIBLE
        grantPermissionsTextView.visibility = if (grantButtonHidden) View.GONE else View.VISIBLE
    }

    private fun removeFragmentInHolderIfPresent(fragment: Fragment) {
        val fragmentManager = fragment.childFragmentManager
        val skillFragment = fragmentManager.findFragmentByTag(currentFragmentTag())
        fragmentHolder.removeAllViews()
        if (skillFragment != null) {
            fragment.childFragmentManager
                .beginTransaction()
                .remove(skillFragment)
                .commit()
        }
    }

    private fun showFragmentInHolderIfNeeded(fragment: Fragment, skillInfo: SkillInfo) {
        if (!skillInfo.hasPreferences) {
            return
        }
        val skillFragment = skillInfo.preferenceFragment ?: return

        val frame = FrameLayout(fragment.requireContext())
        frame.id = View.generateViewId()
        fragmentHolder.addView(
            frame, FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        fragment.childFragmentManager
            .beginTransaction()
            .add(frame.id, skillFragment, currentSkillInfoId)
            .commit()
    }

    private fun currentFragmentTag(): String {
        return currentSkillInfoId + "_skill_settings_tag"
    }

    private fun animateExpandImageRotation(rotation: Float) {
        expandImageView.animate()
            .rotation(rotation)
            .setDuration(100)
            .start()
    }
}
