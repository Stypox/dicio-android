package org.stypox.dicio.skills.navigation

import android.content.Intent
import android.net.Uri
import org.dicio.skill.chain.CaptureEverythingRecognizer
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.output.graphical.GraphicalOutputUtils
import java.util.Locale

class NavigationOutput : OutputGenerator<String?>() {
    override fun generate(data: String?) {
        val message = if (data.isNullOrEmpty()) {
            // try again
            setNextSkills(
                listOf(
                    ChainSkill.Builder(
                            StandardRecognizer(
                                Sections.getSection(SectionsGenerated.navigation)
                            )
                        )
                        .process(NavigationProcessor())
                        .output(NavigationOutput()),
                    ChainSkill.Builder(CaptureEverythingRecognizer())
                        .process(NavigationProcessor())
                        .output(NavigationOutput())
                )
            )

            ctx().android!!.getString(R.string.skill_navigation_specify_where)

        } else {
            val uriGeoSimple = String.format(Locale.ENGLISH, "geo:0,0?q=%s", data)
            val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriGeoSimple))
            ctx().android!!.startActivity(launchIntent)

            ctx().android!!.getString(R.string.skill_navigation_navigating_to, data)
        }

        ctx().speechOutputDevice!!.speak(message)
        ctx().graphicalOutputDevice!!.display(
            GraphicalOutputUtils.buildSubHeader(
                ctx().android!!, message
            )
        )
    }
}
