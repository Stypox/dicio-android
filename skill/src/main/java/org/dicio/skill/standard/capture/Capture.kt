package org.dicio.skill.standard.capture

data class Capture(
    override val name: String,
    val value: Any,
) : NamedCapture
