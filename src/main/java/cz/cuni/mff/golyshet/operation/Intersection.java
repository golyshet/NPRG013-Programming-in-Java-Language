package cz.cuni.mff.golyshet.operation;

import cz.cuni.mff.golyshet.automaton.Automaton;
import cz.cuni.mff.golyshet.automaton.NFA;
import cz.cuni.mff.golyshet.automaton.Transition;
import cz.cuni.mff.golyshet.fileReader.Reader;

import java.util.*;

/**
 * Class that represents the intersection operation.
 * Implements MultipleAutomatonOperation interface.
 * We add the states, alphabet, initial states, final states and transitions to the result automaton.
 * The result automaton is then minimized and determinized.
 * The intersection of two automata is the automaton that accepts the language that is the intersection of the languages accepted by the two automata.
 */
public class Intersection implements MultipleAutomatonOperation {
    @Override
    public Automaton perform(List<String> paths) {
        Automaton automaton1 = new Reader().read(paths.get(0));
        automaton1 = new Determinization().determinize(automaton1);
        Automaton automaton2 = new Reader().read(paths.get(1));
        automaton2 = new Determinization().determinize(automaton2);

        return new Minimization().minimize(new Determinization().determinize(intersection(automaton1, automaton2)));
    }

    /**
     * Intersects two automata
     * @param automaton1 first automaton
     * @param automaton2 second automaton
     * @return intersected automaton
     */
    private Automaton intersection(Automaton automaton1, Automaton automaton2) {

        // Create new initial state that is a pair of the initial states of the two automata
        Pair newInitialStates = new Pair(automaton1.getInitialStates().first(), automaton2.getInitialStates().first());

        // Map the pairs of states to unique state IDs
        Map<Pair, Integer> mappedStates = new HashMap<>();
        int currentState = 0;

        // Map the state IDs to the set of transitions leaving the state
        Map<Integer, Set<Transition>> newTransitionTable = new HashMap<>();
        // Queue for BFS
        Queue<Pair> queue = new LinkedList<>();
        // Add the initial state to the queue
        queue.add(newInitialStates);

        Set<Integer> finalStates = new HashSet<>();
        Set<Integer> states = new HashSet<>();

        // Map the initial state and add it to the set of states
        mappedStates.put(newInitialStates, currentState);
        states.add(currentState);

        // Check if the initial state is a final state
        if (automaton1.getFinalStates().contains(newInitialStates.first) && automaton2.getFinalStates().contains(newInitialStates.second))
            finalStates.add(currentState);
        currentState++;

        // BFS to create the new transition table
        while (!queue.isEmpty()) {
            // Get the current state
            Pair currentSet = queue.poll();
            // For each symbol in the alphabet
            for (String symbol : automaton1.getAlphabet()) {
                Pair newState = new Pair(-1, -1);
                // Find the state in the first automaton that can be reached from the current state with the given symbol
                for (Transition transition : automaton1.getTransitionTable().getOrDefault(currentSet.first, Collections.emptySet())) {
                    if (transition.getSymbol().equals(symbol)) {
                        newState.first = transition.getTo_state();
                        break;
                    }
                }
                // Find the state in the second automaton that can be reached from the current state with the given symbol
                if (newState.first != -1) {
                    for (Transition transition : automaton2.getTransitionTable().getOrDefault(currentSet.second, Collections.emptySet())) {
                        if (transition.getSymbol().equals(symbol)) {
                            newState.second = transition.getTo_state();
                            break;
                        }
                    }

                    // If both states are not -1, add the state to the queue and map it to a unique state ID
                    if (newState.second != -1) {
                        if (!mappedStates.containsKey(newState)) {
                            mappedStates.put(newState, currentState);

                            states.add(currentState);

                            // Check if the state is a final state
                            if (automaton1.getFinalStates().contains(newState.first) && automaton2.getFinalStates().contains(newState.second))
                                finalStates.add(currentState);
                            currentState++;

                            queue.add(newState);
                        }
                        // Add the transition to the transition table
                        newTransitionTable.computeIfAbsent(mappedStates.get(currentSet), k -> new HashSet<>())
                                .add(new Transition(symbol, mappedStates.get(newState)));
                    }
                }
            }
        }
        // Create the new automaton
        Automaton result = new NFA();
        // Add the alphabet, initial state, final states, states and transition table
        result.getAlphabet().addAll(automaton1.getAlphabet());
        result.getAlphabet().addAll(automaton2.getAlphabet());
        result.getInitialStates().add(mappedStates.get(newInitialStates));
        result.getFinalStates().addAll(finalStates);
        result.getStates().addAll(states);
        result.getTransitionTable().putAll(newTransitionTable);
        return result;
    }

    /**
     * Class that represents a pair of states
     */
    class Pair {
        public int first;
        public int second;

        /**
         * Constructor
         * @param first state
         * @param second state
         */
        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return first == pair.first && second == pair.second;
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
