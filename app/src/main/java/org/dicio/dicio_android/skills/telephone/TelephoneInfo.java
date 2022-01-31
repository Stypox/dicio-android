package org.dicio.dicio_android.skills.telephone;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.Sections.isSectionAvailable;
import static org.dicio.dicio_android.SectionsGenerated.telephone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.dicio_android.R;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

import java.util.Arrays;
import java.util.List;

public class TelephoneInfo extends SkillInfo {

    public TelephoneInfo() {
        super("open", R.string.skill_name_telephone, R.string.skill_sentence_example_telephone,
                R.drawable.ic_open_in_new_white, false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return isSectionAvailable(telephone);
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder(this)
                .recognize(new StandardRecognizer(getSection(telephone)))
                .output(new TelephoneOutput());
    }

    @Nullable
    @Override
    public Fragment getPreferenceFragment() {
        return null;
    }

    @NonNull
    @Override
    public List<String> getNeededPermissions() {
        return Arrays.asList(READ_CONTACTS, CALL_PHONE);
    }
}
