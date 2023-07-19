package cz.cuni.mff.golyshet.operation;

import cz.cuni.mff.golyshet.automaton.Automaton;
import cz.cuni.mff.golyshet.automaton.DFA;
import cz.cuni.mff.golyshet.automaton.NFA;
import cz.cuni.mff.golyshet.automaton.Transition;
import cz.cuni.mff.golyshet.fileReader.Reader;

import java.util.*;

/**
 * Class that represents the determinization operation.
 * Implements the SingleAutomatonOperation interface.
 * For determinization of the automaton we simplify the initial states of the automaton.
 * Then we remove all the epsilon transitions from the automaton.
 * Determinization is done by creating a transition table for the automaton and normalize result.
 */
public class Determinization implements SingleAutomatonOperation {
    @Override
    public Automaton perform(List<String> paths) {
        if (paths.size() != 1) {
            throw new RuntimeException("Determinization operation requires exactly one input file.");
        }
        Reader reader = new Reader();
        Automaton automaton = reader.read(paths.get(0));
        if (automaton.getType() == Automaton.Type.DFA) {
            return automaton;
        }
        return determinize(automaton);
    }

    /**
     * Determinizes the automaton
     * @param automaton to be determinized
     * @return determinized automaton
     */

    protected Automaton determinize(Automaton automaton) {
        // Make single initial state and remove epsilon transitions
        Automaton result = removeEpsilonTransitions(simplifyInitialStates(automaton));

        Map<Key, Set<Integer>> transitionMap = new HashMap<>();

        Queue<Set<Integer>> queue = new LinkedList<>();
        queue.add(result.getInitialStates());
        Set<Set<Integer>> explored = new HashSet<>();
        explored.add(result.getInitialStates());

        // BFS to create the transition table
        while (!queue.isEmpty()) {
            Set<Integer> currentStates = queue.poll();
            Map<Key, Set<Integer>> tableRow = new HashMap<>();

            for (Integer state : currentStates) {
                Set<Transition> transitions = result.getTransitionTable().getOrDefault(state, Collections.emptySet());
                for (Transition transition : transitions) {
                    tableRow.computeIfAbsent(
                            new Key(currentStates, transition.getSymbol()), k -> new HashSet<>())
                            .add(transition.getTo_state());
                }
            }

            // Add the new states to the queue
            for (Map.Entry<Key, Set<Integer>> entry : tableRow.entrySet()) {
                Set<Integer> value = entry.getValue();
                if (!explored.contains(value)) {
                    queue.add(value);
                    explored.add(value);
                }
            }
            transitionMap.putAll(tableRow);
        }
        // Renames the states of the automaton to be consecutive numbers starting from 0
        return normalize(result, transitionMap);
    }


    /**
     * Renames all the states of the automaton to be consecutive numbers starting from 0
     * @param automaton automaton for which the states should be renamed
     * @param transitionMap transition map of the automaton
     * @return normalized automaton
     */
    private Automaton normalize(Automaton automaton, Map<Key, Set<Integer>> transitionMap) {
        Set<String> newAlphabet = new HashSet<>(automaton.getAlphabet());
        Set<Integer> newStates = new HashSet<>();
        Set<Integer> newFinalStates = new HashSet<>();
        Set<Integer> newInitialStates = new HashSet<>();
        Map<Integer, Set<Transition>> newTransitionTable = new HashMap<>();

        Map<Set<Integer>, Integer> mappedStates = new HashMap<>();

        int stateCounter = 0;
        Queue<Set<Integer>> queue = new LinkedList<>();
        queue.add(automaton.getInitialStates());
        newInitialStates.add(stateCounter);
        mappedStates.put(automaton.getInitialStates(), stateCounter++);

        while (!queue.isEmpty()) {
            Set<Integer> currentStates = queue.poll();
            for (String symbol : automaton.getAlphabet()) {
                Set<Integer> state = transitionMap.getOrDefault(new Key(currentStates, symbol), Collections.emptySet());
                if (state.isEmpty())
                    continue;
                if (!mappedStates.containsKey(state)) {
                    mappedStates.put(state, stateCounter++);
                    queue.add(state);
                }
                newTransitionTable.computeIfAbsent(mappedStates.get(currentStates), k -> new HashSet<>())
                        .add(new Transition(symbol, mappedStates.get(state)));
            }
            newStates.add(mappedStates.get(currentStates));
            for (Integer finalState : automaton.getFinalStates()) {
                if (currentStates.contains(finalState)) {
                    newFinalStates.add(mappedStates.get(currentStates));
                    break;
                }
            }
        }

        DFA result = new DFA();
        result.clear();

        result.getAlphabet().addAll(newAlphabet);
        result.getStates().addAll(newStates);
        result.getFinalStates().addAll(newFinalStates);
        result.getInitialStates().addAll(newInitialStates);
        result.getTransitionTable().putAll(newTransitionTable);

        return result;
    }

    /**
     * Removes epsilon transitions from the automaton
     * @param automaton for which the epsilon transitions should be removed
     * @return automaton without epsilon transitions
     */
    private Automaton removeEpsilonTransitions(Automaton automaton) {

        // Construct new transition table with epsilon transitions removed
        Map<Integer, Set<Transition>> transition_table = new HashMap<>();
        for (Integer state : automaton.getStates()) {
            Set<Transition> new_transitions = new HashSet<>();
            Set<Transition> transitions = automaton.getTransitionTable().getOrDefault(state, Collections.emptySet());
            for (Transition transition : transitions) {
                if (!transition.getSymbol().equals("*")) {
                    new_transitions.add(transition);
                } else {
                    // Add transitions from epsilon closure
                    Set<Integer> visited = new HashSet<>();
                    Queue<Integer> queue = new LinkedList<>();
                    queue.add(transition.getTo_state());
                    while (!queue.isEmpty()) {
                        int current_state = queue.poll();
                        if (visited.contains(current_state)) {
                            continue;
                        }
                        visited.add(current_state);
                        // Add all transitions from the current state except epsilon transitions
                        Set<Transition> current_transitions = automaton.getTransitionTable().getOrDefault(current_state, Collections.emptySet());
                        for (Transition current_transition : current_transitions) {
                            if (current_transition.getSymbol().equals("*")) {
                                queue.add(current_transition.getTo_state());
                            } else {
                                new_transitions.add(current_transition);
                            }
                        }
                    }
                    // Add final states
                    if (automaton.getFinalStates().stream().anyMatch(visited::contains))
                        automaton.getFinalStates().add(state);
                }
            }
            transition_table.put(state, new_transitions);
        }

        // Construct new automaton
        NFA result = new NFA();
        result.clear();
        SortedSet<String> alphabet = new TreeSet<>(automaton.getAlphabet());
        alphabet.remove("*");
        SortedSet<Integer> states = new TreeSet<>(automaton.getStates());
        SortedSet<Integer> final_states = new TreeSet<>(automaton.getFinalStates());
        SortedSet<Integer> initial_states = new TreeSet<>(automaton.getInitialStates());
        result.getAlphabet().addAll(alphabet);
        result.getStates().addAll(states);
        result.getFinalStates().addAll(final_states);
        result.getInitialStates().addAll(initial_states);
        result.getTransitionTable().putAll(transition_table);

        return result;
    }

    /**
     * Simplifies the automaton by joining all the initial states into one
     * @param automaton which should be simplified by joining initial states
     * @return simplified automaton with one initial state
     */
    private Automaton simplifyInitialStates(Automaton automaton) {
        if (automaton.getInitialStates().size() == 1) {
            return automaton;
        }

        SortedSet<String> alphabet = new TreeSet<>(automaton.getAlphabet());
        SortedSet<Integer> states = new TreeSet<>(automaton.getStates());
        SortedSet<Integer> final_states = new TreeSet<>(automaton.getFinalStates());
        Map<Integer, Set<Transition>> transition_table = new HashMap<>(automaton.getTransitionTable());

        // Create a new initial state
        int new_initial_state = Collections.max(automaton.getStates()) + 1;
        states.add(new_initial_state);

        // Add epsilon transitions from new initial state to old initial states
        for (int initial_state : automaton.getInitialStates()) {
            transition_table.computeIfAbsent(new_initial_state, k -> new HashSet<>())
                    .add(new Transition("*", initial_state));
        }

        // Add new initial state to final states if any of the initial states is final
        if (automaton.getInitialStates().stream().anyMatch(automaton.getFinalStates()::contains))
            final_states.add(new_initial_state);

        // Create new automaton
        NFA result = new NFA();
        result.getAlphabet().addAll(alphabet);
        result.getStates().addAll(states);
        result.getFinalStates().addAll(final_states);
        result.getInitialStates().add(new_initial_state);
        result.getTransitionTable().putAll(transition_table);

        return result;
    }


    /**
     * Helper class for mapping a set of states and a symbol to a set of states
     */
    class Key {
        public Set<Integer> states;
        public String symbol;

        /**
         * Constructor
         * @param states set of states
         * @param symbol symbol for the transition
         */
        public Key(Set<Integer> states, String symbol) {
            this.states = states;
            this.symbol = symbol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(states, key.states) && Objects.equals(symbol, key.symbol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(states, symbol);
        }
    }
}
