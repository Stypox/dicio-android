package org.dicio.dicio_android.skills.telephone;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.Sections.isSectionAvailable;
import static org.dicio.dicio_android.SectionsGenerated.telephone;
import static org.dicio.dicio_android.SectionsGenerated.util_yes_no;

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
                R.drawable.ic_call_white, false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return isSectionAvailable(telephone) && isSectionAvailable(util_yes_no);
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder(context, this)
                .recognize(new StandardRecognizer(context, this, getSection(telephone)))
                .output(new TelephoneOutput(context, this));
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
