package org.stypox.dicio.skills.navigation

import android.content.Intent
import android.net.Uri
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import java.util.Locale

class NavigationGenerator : OutputGenerator<String?>() {
    override fun generate(data: String?): SkillOutput {
        if (data.isNullOrBlank()) {
            val uriGeoSimple = String.format(Locale.ENGLISH, "geo:0,0?q=%s", data)
            val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriGeoSimple))
            ctx().android!!.startActivity(launchIntent)
        }

        return NavigationOutput(data)
    }
}
