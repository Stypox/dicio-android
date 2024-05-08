package org.dicio.skill.standard

import org.dicio.skill.chain.InputRecognizer.Specificity

open class StandardRecognizerData(val specificity: Specificity, vararg val sentences: Sentence)
