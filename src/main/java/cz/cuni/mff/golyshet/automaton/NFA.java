package cz.cuni.mff.golyshet.automaton;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a nondeterministic finite automaton (NFA).
 * Implements the Automaton interface.
 */

public class NFA implements Automaton {

    private final SortedSet<String> alphabet;
    private final SortedSet<Integer> states;
    private final SortedSet<Integer> final_states;
    private final SortedSet<Integer> initial_states;
    private final Map<Integer, Set<Transition>> transition_table;

    /**
     * Constructs an empty NFA.
     */
    public NFA() {
        alphabet = new TreeSet<>();
        states = new TreeSet<>();
        final_states = new TreeSet<>();
        initial_states = new TreeSet<>();
        transition_table = new HashMap<>();
    }

    @Override
    public Type getType() {
        return Type.NFA;
    }

    @Override
    public void rename(int start) {
        // Create a mapping from old states to new states
        Map<Integer, Integer> mappedStates = new HashMap<>();

        // Add initial states to the queue
        int stateCounter = start;
        Queue<Integer> queue = new LinkedList<>();
        queue.add(initial_states.first());
        mappedStates.put(initial_states.first(), stateCounter++);

        // BFS to find all reachable states and map them to new states
        while (!queue.isEmpty()) {
            Integer currentState = queue.poll();
            Set<Transition> transitions = transition_table.getOrDefault(currentState, Collections.emptySet());
            for (Transition transition : transitions) {
                if (!mappedStates.containsKey(transition.getTo_state())) {
                    mappedStates.put(transition.getTo_state(), stateCounter++);
                    queue.add(transition.getTo_state());
                }
            }
        }

        // Map all leftover states to new states
        for (Integer state : states) {
            if (!mappedStates.containsKey(state)) {
                mappedStates.put(state, stateCounter++);
            }
        }

        // Create new sets and maps with mapped states
        Set<Integer> newStates = new HashSet<>(mappedStates.values());
        Set<String> newAlphabet = new HashSet<>(alphabet);
        Set<Integer> newInitialStates = initial_states.stream().map(mappedStates::get).collect(Collectors.toSet());
        Set<Integer> newFinalStates = final_states.stream().map(mappedStates::get).collect(Collectors.toSet());
        Map<Integer, Set<Transition>> newTransitionTable = transition_table.entrySet().stream()
                .collect(Collectors.toMap(entry -> mappedStates.get(entry.getKey()),
                        entry -> entry.getValue().stream()
                                .map(transition -> new Transition(transition.getSymbol(), mappedStates.get(transition.getTo_state())))
                                .collect(Collectors.toSet())));

        // Replace old sets and maps with new ones
        clear();
        alphabet.addAll(newAlphabet);
        states.addAll(newStates);
        initial_states.addAll(newInitialStates);
        final_states.addAll(newFinalStates);
        transition_table.putAll(newTransitionTable);
    }

    @Override
    public void clear() {
        alphabet.clear();
        states.clear();
        final_states.clear();
        initial_states.clear();
        transition_table.clear();
    }

    @Override
    public SortedSet<Integer> getStates() {
        return states;
    }

    @Override
    public SortedSet<String> getAlphabet() {
        return alphabet;
    }

    @Override
    public SortedSet<Integer> getFinalStates() {
        return final_states;
    }

    @Override
    public SortedSet<Integer> getInitialStates() {
        return initial_states;
    }

    @Override
    public Map<Integer, Set<Transition>> getTransitionTable() {
        return transition_table;
    }

    @Override
    public void save(String filename) {

    }
}
