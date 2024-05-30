package org.dicio.skill.standard2.capture

data class StringRangeCapture(
    override val name: String,
    val start: Int,
    val end: Int, // exclusive
) : NamedCapture
