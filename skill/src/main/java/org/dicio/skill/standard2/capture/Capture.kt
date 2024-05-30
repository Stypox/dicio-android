package org.dicio.skill.standard2.capture

data class Capture(
    override val name: String,
    val value: Any,
) : NamedCapture
