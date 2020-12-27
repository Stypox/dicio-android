package org.dicio.dicio_android.skills.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.component.standard.StandardRecognizer;
import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.Skill;
import org.dicio.dicio_android.skills.SkillInfo;
import org.dicio.dicio_android.skills.ChainSkill;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.SectionsGenerated.search;

public class SearchInfo implements SkillInfo {

    @Override
    public Skill build(final Context context, final SharedPreferences preferences) {
        final ChainSkill.Builder builder = new ChainSkill.Builder()
                .recognize(new StandardRecognizer(getSection(search)));

        final String searchEngine = preferences.getString(
                context.getString(R.string.pref_key_search_engine),
                context.getString(R.string.pref_val_search_engine_qwant));
        if (searchEngine.equals(context.getString(R.string.pref_val_search_engine_duckduckgo))) {
            builder.process(new DuckDuckGoProcessor());
        } else {
            builder.process(new QwantProcessor());
        }

        return builder.output(new SearchOutput());
    }

    @Override
    public boolean hasPreferences() {
        return true;
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return new PreferenceFragmentCompat() {
            @Override
            public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
                addPreferencesFromResource(R.xml.pref_search);
            }
        };
    }
}
