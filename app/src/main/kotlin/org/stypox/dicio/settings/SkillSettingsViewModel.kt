package org.stypox.dicio.settings

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.settings.datastore.UserSettings
import org.stypox.dicio.eval.SkillHandler2
import javax.inject.Inject


@HiltViewModel
class SkillSettingsViewModel @Inject constructor(
    application: Application,
    private val dataStore: DataStore<UserSettings>,
    val skillContext: SkillContext,
    private val skillHandler: SkillHandler2,
) : AndroidViewModel(application) {
    val skills: List<SkillInfo> get() = skillHandler.allSkillInfoList
    val enabledSkills = dataStore.data.map { it.enabledSkillsMap }

    fun setSkillEnabled(id: String, state: Boolean) {
        viewModelScope.launch {
            dataStore.updateData {
                it.toBuilder()
                    .putEnabledSkills(id, state)
                    .build()
            }
        }
    }
}
