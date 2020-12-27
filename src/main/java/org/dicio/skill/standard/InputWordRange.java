package org.dicio.skill.standard;

public class InputWordRange {
    private int from;
    private int to;

    /**
     * Deep copy constructor
     */
    InputWordRange(final InputWordRange other) {
        from = other.from;
        to = other.to;
    }

    /**
     * @param from the index of the first element of the range
     * @param to the index of one past the last element of the range
     */
    public InputWordRange(final int from, final int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "[" + from + "," + to + ")";
    }

    /**
     * @return the index of the first element of the range
     */
    public int from() {
        return from;
    }

    /**
     * @return the index of one past the last element of the range
     */
    public int to() {
        return to;
    }
}
