package cz.cuni.mff.golyshet.automaton;

/**
 * Represents a transition in a finite automaton.
 * Each transition is represented by a Transition object, which contains the symbol and the destination state.
 * All transitions must be in the package cz.cuni.mff.golyshet.automaton
 */

public class Transition {
    String symbol;
    int to_state;

    /**
     * Creates a new Transition object with the given symbol and destination state.
     * @param s the symbol on which the transition is taken
     * @param to_state the state that the transition leads to
     */
    public Transition(String s, int to_state) {
        this.symbol = s;
        this.to_state = to_state;
    }

    /**
     * @return the symbol associated with this transition
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return the state that this transition leads to
     */
    public int getTo_state() {
        return to_state;
    }
}
