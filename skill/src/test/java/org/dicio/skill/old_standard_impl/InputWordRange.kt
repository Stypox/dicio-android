package org.dicio.skill.old_standard_impl

class InputWordRange {
    private var from: Int
    private var to: Int

    /**
     * Deep copy constructor
     */
    internal constructor(other: InputWordRange) {
        from = other.from
        to = other.to
    }

    /**
     * @param from the index of the first element of the range
     * @param to the index of one past the last element of the range
     */
    constructor(from: Int, to: Int) {
        this.from = from
        this.to = to
    }

    override fun toString(): String {
        return "[$from,$to)"
    }

    /**
     * @return the index of the first element of the range
     */
    fun from(): Int {
        return from
    }

    /**
     * @return the index of one past the last element of the range
     */
    fun to(): Int {
        return to
    }
}
