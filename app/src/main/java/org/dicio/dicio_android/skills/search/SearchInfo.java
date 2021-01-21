package org.dicio.dicio_android.skills.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

import java.util.Locale;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.SectionsGenerated.search;

public class SearchInfo extends SkillInfo {

    public SearchInfo() {
        super("search", R.string.skill_name_search, true);
    }

    @Override
    public Skill build(final Context context,
                       final SharedPreferences preferences,
                       final Locale locale) {

        final ChainSkill.Builder builder = new ChainSkill.Builder()
                .recognize(new StandardRecognizer(getSection(search)));

        final String searchEngine = preferences.getString(
                context.getString(R.string.pref_key_search_engine), "");
        if (searchEngine.equals(context.getString(R.string.pref_val_search_engine_duckduckgo))) {
            builder.process(new DuckDuckGoProcessor());
        } else {
            builder.process(new QwantProcessor());
        }

        return builder.output(new SearchOutput());
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return new Preferences();
    }

    public static class Preferences extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
            addPreferencesFromResource(R.xml.pref_search);
        }
    }
}
