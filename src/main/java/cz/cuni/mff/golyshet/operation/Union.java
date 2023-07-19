package cz.cuni.mff.golyshet.operation;

import cz.cuni.mff.golyshet.automaton.Automaton;
import cz.cuni.mff.golyshet.automaton.NFA;
import cz.cuni.mff.golyshet.automaton.Transition;
import cz.cuni.mff.golyshet.fileReader.Reader;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Class representing the union operation on two automata.
 * Implements the MultipleAutomatonOperation interface.
 * For union, we need to rename the states of the second automaton.
 * Then we add the states, alphabet, initial states, final states and transitions to the result automaton.
 * The result automaton is then minimized and determinized.
 * Create a new initial state and add epsilon transitions from it to the initial states of both automata.
 */
public class Union implements MultipleAutomatonOperation {
    @Override
    public Automaton perform(List<String> paths) {
        Automaton automaton1 = new Reader().read(paths.get(0));
        Automaton automaton2 = new Reader().read(paths.get(1));
        return new Minimization().minimize(new Determinization().determinize(union(automaton1, automaton2)));
    }

    /**
     * Performs the union operation on two automata
     * @param automaton1 first automaton
     * @param automaton2 second automaton
     * @return the result of the union operation
     */
    private Automaton union(Automaton automaton1, Automaton automaton2) {
        // Rename the states of the second automaton
        automaton2.rename(Collections.max(automaton1.getStates()) + 1);
        NFA result = new NFA();
        // Add the alphabet, final states, states and transitions of both automata to the result
        result.getAlphabet().addAll(automaton1.getAlphabet());
        result.getAlphabet().addAll(automaton2.getAlphabet());
        result.getFinalStates().addAll(automaton1.getFinalStates());
        result.getFinalStates().addAll(automaton2.getFinalStates());
        result.getStates().addAll(automaton1.getStates());
        result.getStates().addAll(automaton2.getStates());
        result.getTransitionTable().putAll(automaton1.getTransitionTable());
        result.getTransitionTable().putAll(automaton2.getTransitionTable());
        // Create a new initial state for the result and add epsilon transitions to the initial states of both automatons
        int newInitialState = Collections.max(result.getStates()) + 1;

        for (Integer initial: automaton1.getInitialStates()) {
            result.getTransitionTable().computeIfAbsent(newInitialState, k -> new HashSet<>())
                    .add(new Transition("*", initial));
        }

        for (Integer initial: automaton2.getInitialStates()) {
            result.getTransitionTable().computeIfAbsent(newInitialState, k -> new HashSet<>())
                    .add(new Transition("*", initial));
        }

        // Check if the initial state is a final state
        if (automaton1.getInitialStates().stream().anyMatch(automaton1.getFinalStates()::contains))
            result.getFinalStates().add(newInitialState);

        if (automaton2.getInitialStates().stream().anyMatch(automaton2.getFinalStates()::contains))
            result.getFinalStates().add(newInitialState);

        // Add the new initial state to the set of initial states and states
        result.getInitialStates().add(newInitialState);
        result.getStates().add(newInitialState);

        return result;
    }
}
