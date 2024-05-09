package org.stypox.dicio.skills.telephone

import android.content.ContentResolver
import android.content.Context
import android.util.Pair
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import org.dicio.skill.Skill
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.standard.StandardResult
import org.stypox.dicio.R
import org.stypox.dicio.Sentences_en.telephone
import org.stypox.dicio.output.graphical.GraphicalOutputUtils
import java.util.stream.Collectors

class TelephoneOutput : OutputGenerator<StandardResult>() {
    override fun generate(data: StandardResult) {
        val contentResolver: ContentResolver = ctx().android!!.contentResolver
        val userContactName: String = data.getCapturingGroup(telephone.who)!!.trim { it <= ' ' }
        val contacts: List<Contact> =
            Contact.getFilteredSortedContacts(contentResolver, userContactName)
        val validContacts: MutableList<Pair<String, List<String>>> = ArrayList()
        val output = GraphicalOutputUtils.buildVerticalLinearLayout(
            ctx().android!!,
            ResourcesCompat.getDrawable(
                ctx().android!!.resources,
                R.drawable.divider_items, null
            )
        )

        var i = 0
        while (validContacts.size < 5 && i < contacts.size) {
            val contact = contacts[i]
            val numbers = contact.getNumbers(contentResolver)
            if (numbers.isEmpty()) {
                ++i
                continue
            }
            if (validContacts.isEmpty()
                && contact.distance < 3
                && numbers.size == 1 // it has just one number
                && (contacts.size <= i + 1 // the next contact has a distance higher by 3+
                        || contacts[i + 1].distance - 2 > contact.distance)
            ) {
                // very close match with just one number and without distance ties: call it directly
                ConfirmCallOutput.callAfterConfirmation(
                    this,
                    contact.name,
                    numbers[0]
                )
                return
            }
            validContacts.add(Pair(contact.name, numbers))
            addNumbersToOutput(contact, numbers, output, ctx().android!!)
            ++i
        }

        // this point will not be reached if a very close match was found
        if (validContacts.isEmpty()) {
            ctx().speechOutputDevice!!.speak(
                ctx().android!!.getString(
                    R.string.skill_telephone_unknown_contact
                )
            )
        } else {
            ctx().speechOutputDevice!!.speak(
                ctx().android!!.getString(
                    R.string.skill_telephone_found_contacts, validContacts.size
                )
            )
            ctx().graphicalOutputDevice!!.display(output)
            if (validContacts.size == 1 // there is exactly one valid contact and ...
                // ... either it has exactly one number, or we would be forced to use
                // RecognizeNameIO in any case, which only uses the first number anyway
                && (validContacts[0].second.size == 1 || ctx().numberParserFormatter == null)
            ) {
                // not a good enough match, but since we have only this, call it directly
                val contact = validContacts[0]
                ConfirmCallOutput.callAfterConfirmation(
                    this,
                    contact.first,
                    contact.second[0]
                )
            } else {
                // ask the user which one to call (assumes items in validContacts are in the same
                // order as those displayed by addNumbersToOutput)
                val nextSkills: MutableList<Skill> = ArrayList()
                val recognizeNameContacts =
                    validContacts.stream()
                        // when saying the name, there is no way to distinguish between
                        // different numbers, so just use the first one
                        .map { NameNumberPair(it.first, it.second[0]) }
                        .collect(Collectors.toList())
                nextSkills.add(ContactChooserName(recognizeNameContacts))
                if (ctx().numberParserFormatter != null) {
                    val recognizeIndexContacts = validContacts.stream()
                        .flatMap {
                            it.second.stream().map { number -> NameNumberPair(it.first, number) }
                        }
                        .collect(Collectors.toList())
                    nextSkills.add(ContactChooserIndex(recognizeIndexContacts))
                }
                setNextSkills(nextSkills)
            }
        }
    }

    private fun addNumbersToOutput(
        contact: Contact,
        numbers: List<String?>,
        output: LinearLayout,
        context: Context
    ) {
        for (j in numbers.indices) {
            val view: View
            if (j == 0) {
                view = GraphicalOutputUtils.inflate(context, R.layout.skill_telephone_contact)
                view.findViewById<TextView>(R.id.contact_name).text = contact.name
            } else {
                // a contact can have multiple associated numbers
                view = GraphicalOutputUtils.inflate(
                    context,
                    R.layout.skill_telephone_contact_secondary_number
                )
            }
            val number = numbers[j]
            view.findViewById<TextView>(R.id.contact_number).text = number
            view.setOnClickListener {
                ConfirmCallOutput.call(
                    context,
                    number
                )
            }
            output.addView(view)
        }
    }
}
