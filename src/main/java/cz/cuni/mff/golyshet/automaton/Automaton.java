package cz.cuni.mff.golyshet.automaton;

import java.util.*;

/**
 * Interface for an automaton
 * Represents a nondeterministic finite automaton (NFA) or a deterministic finite automaton (DFA).
 * The automaton is represented by a set of states, a set of symbols in the alphabet, a set of final states, a set of initial states, and a transition table.
 * The transition table is a map that associates each state with a set of transitions.
 * Each transition is represented by a Transition object, which contains the symbol and the destination state.
 * The automaton can be saved to a file or printed to the standard output.
 * All automata must be in the package cz.cuni.mff.golyshet.automaton
 */

public interface Automaton {

    /**
     * Enum to represent the type of the automaton (either NFA or DFA)
     */
    enum Type {NFA, DFA;};

    /**
     * @return the type of the automaton
     */
    public Type getType();

    /**
     * Renames the states of the automaton so that they are numbered from start to start+n-1, where n is the number of states.
     * @param start the new ID of the first state
     */
    public void rename(int start);

    /**
     * Clears all the data in the automaton
     */
    public void clear();

    /**
     * @return a sorted set of all the states in the automaton
     */
    public SortedSet<Integer> getStates();

    /**
     * @return a sorted set of all the symbols in the alphabet used by the automaton
     */
    public SortedSet<String> getAlphabet();


    /**
     * @return a sorted set of all the final (accepting) states in the automaton
     */
    public SortedSet<Integer> getFinalStates();

    /**
     * @return a sorted set of all the initial (starting) states in the automaton
     */
    public SortedSet<Integer> getInitialStates();

    /**
     * Each transition is represented by a Transition object, which contains the symbol and the destination state.
     * @return a map that associates each state with a set of transitions.
     */
    public Map<Integer, Set<Transition>> getTransitionTable();

    /**
     * Saves the automaton to a file with the given filename, or prints it to the standard output if the filename is empty.
     * @param filename
     */
    void save(String filename);
}
