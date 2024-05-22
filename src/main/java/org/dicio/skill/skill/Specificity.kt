package org.dicio.skill.skill

/**
 * Represents how specific a skill is when matching user input. If a skill with a high specificity
 * yields a high score, it will be preferred over a skill with a lower specificity and roughly the
 * same score. This is because if the user input matched both a highly-specific skill and a lower
 * specificity one, then what the user most likely wanted was the highly-specific skill. For
 * example, if the user says "what is the weather", the weather skill should be chosen, even if the
 * search skill interprets it with a high score as searching for "the weather".
 */
enum class Specificity {
    /**
     * For highly specific things (e.g. weather)
     */
    HIGH,

    /**
     * For not-too-specific things (e.g. calculator that parses numbers)
     */
    MEDIUM,

    /**
     * For broad things (e.g. omniscient API)
     */
    LOW,
}
