package org.dicio.skill.standard.capture

data class StringRangeCapture(
    override val name: String,
    val start: Int,
    val end: Int, // exclusive
) : NamedCapture
