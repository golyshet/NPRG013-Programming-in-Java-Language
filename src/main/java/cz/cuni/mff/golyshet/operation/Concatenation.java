package cz.cuni.mff.golyshet.operation;

import cz.cuni.mff.golyshet.automaton.Automaton;
import cz.cuni.mff.golyshet.automaton.NFA;
import cz.cuni.mff.golyshet.automaton.Transition;
import cz.cuni.mff.golyshet.fileReader.Reader;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Class that represents the concatenation operation.
 * Implements MultipleAutomatonOperation interface.
 * To concatenate two automata we need to rename the states of the second automaton.
 * Then we add the states, alphabet, initial states, final states and transitions to the result automaton.
 * We add epsilon transitions from the final states of the first automaton to the initial states of the second automaton.
 * The result automaton is then minimized and determinized.
 */
public class Concatenation implements MultipleAutomatonOperation {
    @Override
    public Automaton perform(List<String> paths) {
        Automaton automaton1 = new Reader().read(paths.get(0));
        Automaton automaton2 = new Reader().read(paths.get(1));
        return new Minimization().minimize(new Determinization().determinize(concatenation(automaton1, automaton2)));
    }

    /**
     * Concatenates two automata
     * @param automaton1 first automaton
     * @param automaton2 second automaton
     * @return concatenated automaton
     */
    private Automaton concatenation(Automaton automaton1, Automaton automaton2) {
        // Rename the states of the second automaton
        automaton2.rename(Collections.max(automaton1.getStates()) + 1);
        NFA result = new NFA();

        // Add the states, alphabet, initial states, final states and transitions to the result automaton
        // Exclude the initial states of the second automaton and the final states of the first automaton
        result.getAlphabet().addAll(automaton1.getAlphabet());
        result.getAlphabet().addAll(automaton2.getAlphabet());
        result.getInitialStates().addAll(automaton1.getInitialStates());
        result.getFinalStates().addAll(automaton2.getFinalStates());
        result.getStates().addAll(automaton1.getStates());
        result.getStates().addAll(automaton2.getStates());
        result.getTransitionTable().putAll(automaton1.getTransitionTable());
        result.getTransitionTable().putAll(automaton2.getTransitionTable());

        // Add epsilon transitions from the final states of the first automaton to the initial states of the second automaton
        for (Integer finals: automaton1.getFinalStates()) {
            for (Integer initial: automaton2.getInitialStates())
                result.getTransitionTable().computeIfAbsent(finals, k -> new HashSet<>())
                        .add(new Transition("*", initial));
        }

        return result;
    }
}
