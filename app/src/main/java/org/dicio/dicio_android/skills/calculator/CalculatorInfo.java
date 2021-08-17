package org.dicio.dicio_android.skills.calculator;

import static org.dicio.dicio_android.Sections.isSectionAvailable;
import static org.dicio.dicio_android.SectionsGenerated.calculator;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.Sections;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

public class CalculatorInfo extends SkillInfo {

    public CalculatorInfo() {
        super("calculator", R.string.skill_name_calculator,
                R.string.skill_sentence_example_calculator, R.drawable.ic_calculate_white, false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return isSectionAvailable(calculator) && context.getNumberParserFormatter() != null;
    }

    @Override
    public Skill build(SkillContext context) {
        return new ChainSkill.Builder()
                .recognize(new StandardRecognizer(Sections.getSection(calculator)))
                .process(new CalculatorProcessor())
                .output(new CalculatorOutput());
    }

    @Nullable
    @Override
    public Fragment getPreferenceFragment() {
        return null;
    }
}
