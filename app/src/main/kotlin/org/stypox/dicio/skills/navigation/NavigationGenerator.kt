package org.stypox.dicio.skills.navigation

import android.content.Intent
import android.net.Uri
import org.dicio.skill.chain.CaptureEverythingRecognizer
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.output.graphical.GraphicalOutputUtils
import java.util.Locale

class NavigationGenerator : OutputGenerator<String?>() {
    override fun generate(data: String?): SkillOutput {
        if (data.isNullOrBlank()) {
            val uriGeoSimple = String.format(Locale.ENGLISH, "geo:0,0?q=%s", data)
            val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriGeoSimple))
            ctx().android!!.startActivity(launchIntent)
        }

        return NavigationOutput(ctx().android!!, data)
    }
}
