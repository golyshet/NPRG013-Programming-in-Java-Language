package cz.cuni.mff.golyshet.operation;

import cz.cuni.mff.golyshet.automaton.Automaton;
import cz.cuni.mff.golyshet.automaton.DFA;
import cz.cuni.mff.golyshet.automaton.Transition;
import cz.cuni.mff.golyshet.fileReader.Reader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that represents minimization operation.
 * Implements SingleAutomatonOperation interface.
 * Minimization is performed by removing unreachable and useless states and then merging equivalent states.
 * Unreachable state is a state that cannot be reached from the initial state.
 * Useless state is a state that cannot reach any final state.
 * Equivalent states are states that have the same set of transitions to other states.
 */
public class Minimization implements SingleAutomatonOperation {
    @Override
    public Automaton perform(List<String> paths) {
        Automaton automaton = new Reader().read(paths.get(0));
        return minimize(new Determinization().determinize(automaton));
    }

    /**
     * @param automaton for minimization
     * @return map of states that can reach each state
     */
    protected Automaton minimize(Automaton automaton) {
        return mergeEquivalentStates(removeUselessStates(removeUnreachableStates(automaton)));
    }

    /**
     * Removes any state that is unreachable from the initial state
     * @param automaton for removing unreachable states
     * @return automaton without unreachable states
     */
    private Automaton removeUnreachableStates(Automaton automaton) {
        DFA result = new DFA();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(automaton.getInitialStates().first());
        Set<Integer> explored = new HashSet<>();

        // BFS from initial state
        while (!queue.isEmpty()) {
            Integer state = queue.poll();
            explored.add(state);

            // Add all states reachable from current state
            Set<Transition> transitions = automaton.getTransitionTable().getOrDefault(state, Collections.emptySet());
            for (Transition transition : transitions) {
                if (!explored.contains(transition.getTo_state())) {
                    queue.add(transition.getTo_state());
                }
            }
        }

        // Create new automaton
        Set<String> newAlphabet = new HashSet<>(automaton.getAlphabet());
        Set<Integer> newStates = new HashSet<>(explored);
        // Only keep final states that are reachable
        Set<Integer> newFinalStates = automaton.getFinalStates().stream().filter(explored::contains).collect(Collectors.toSet());
        Set<Integer> newInitialStates = new HashSet<>(automaton.getInitialStates());
        // Only keep transitions that are from reachable states
        Map<Integer, Set<Transition>> newTransitionTable = automaton.getTransitionTable().entrySet().stream()
                .filter(entry -> explored.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Create new automaton
        result.getAlphabet().addAll(newAlphabet);
        result.getStates().addAll(newStates);
        result.getFinalStates().addAll(newFinalStates);
        result.getInitialStates().addAll(newInitialStates);
        result.getTransitionTable().putAll(newTransitionTable);

        return result;
    }

    /**
     * Removes any state that cannot reach any final state
     * @param automaton for removing useless states
     * @return automaton without useless states
     */
    private Automaton removeUselessStates(Automaton automaton) {
        DFA result = new DFA();
        Queue<Integer> queue = new LinkedList<>(automaton.getFinalStates());
        Set<Integer> explored = new HashSet<>(automaton.getFinalStates());

        Map<Integer, Set<Integer>> incoming = getIncomingStates(automaton);

        // BFS from final states
        while (!queue.isEmpty()) {
            Integer state = queue.poll();
            // Get all states that lead to the current state
            Set<Integer> incomingStates = incoming.getOrDefault(state, Collections.emptySet());
                // Add all states that lead to the current state and have not been explored yet
            for (Integer incomingState : incomingStates) {
                if (!explored.contains(incomingState)) {
                    queue.add(incomingState);
                    explored.add(incomingState);
                }
            }
        }

        // Create new automaton
        Set<String> newAlphabet = new HashSet<>(automaton.getAlphabet());
        Set<Integer> newStates = new HashSet<>(explored);
        Set<Integer> newFinalStates = new HashSet<>(automaton.getFinalStates());
        // Only keep initial states that are not useless
        Set<Integer> newInitialStates = automaton.getInitialStates().stream().filter(explored::contains).collect(Collectors.toSet());
        // Only keep transitions that are not from or to useless states
        Map<Integer, Set<Transition>> newTransitionTable = automaton.getTransitionTable().entrySet().stream()
                .filter(entry -> explored.contains(entry.getKey()))
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().stream()
                        .filter(transition -> explored.contains(transition.getTo_state()))
                        .collect(Collectors.toSet())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // If there are no final or initial states, the automaton accepts empty language
        if (newFinalStates.isEmpty() || newInitialStates.isEmpty()) {
            newFinalStates.clear();
            newInitialStates.clear();
            newStates.clear();
            newTransitionTable.clear();

            newStates.add(0);
            newInitialStates.add(0);
        }

        // Create new automaton
        result.getAlphabet().addAll(newAlphabet);
        result.getStates().addAll(newStates);
        result.getFinalStates().addAll(newFinalStates);
        result.getInitialStates().addAll(newInitialStates);
        result.getTransitionTable().putAll(newTransitionTable);

        return result;
    }

    /**
     * Merges equivalent states
     * @param automaton for merging equivalent states
     * @return automaton with merged equivalent states
     */
    private Automaton mergeEquivalentStates(Automaton automaton) {
        DFA result = new DFA();
        Set<String> newAlphabet = new HashSet<>(automaton.getAlphabet());
        Set<Integer> newFinalStates = new HashSet<>();
        Set<Integer> newInitialStates = new HashSet<>();
        Map<Integer, Set<Transition>> newTransitionTable = new HashMap<>();

        automaton.rename(0);

        // Create equivalent table
        boolean[][] equivalentTable = new boolean[automaton.getStates().size()][automaton.getStates().size()];
        // Initialize table to true
        Arrays.stream(equivalentTable).forEach(row -> Arrays.fill(row, true));

        int equivalentCount = equivalentTable.length * equivalentTable.length;

        // Final states are not equivalent to non-final states
        for (Integer finalState: automaton.getFinalStates()) {
            for (Integer nonFinalState : automaton.getStates()) {
                if (!automaton.getFinalStates().contains(nonFinalState)) {
                    equivalentTable[finalState][nonFinalState] = false;
                    equivalentTable[nonFinalState][finalState] = false;
                    equivalentCount -= 2;
                }
            }
        }

        int previousEquivalentCount = 0;

        // While there are still changes being made to the equivalent table
        while (previousEquivalentCount != equivalentCount) {
            // Update previous equivalent count
            previousEquivalentCount = equivalentCount;
            // Iterate through the triangle of the equivalent table
            for (int i = 0; i < equivalentTable.length; i++) {
                for (int j = 0; j < i; j++) {
                    // If states i and j are marked as equivalent
                    if (equivalentTable[i][j]) {
                        Set<Transition> transitions1 = automaton.getTransitionTable().getOrDefault(i, Collections.emptySet());
                        Set<Transition> transitions2 = automaton.getTransitionTable().getOrDefault(j, Collections.emptySet());
                        // If the sets of transitions are of different size, mark i and j as not equivalent
                        if (transitions1.size() != transitions2.size()) {
                            equivalentTable[i][j] = false;
                            equivalentTable[j][i] = false;
                            equivalentCount -= 2;
                        } else {
                            // Iterate through the transitions for state i
                            for (Transition transition1 : transitions1) {
                                // Find the corresponding transition for state j
                                Transition transition2 = transitions2.stream().filter(t -> t.getSymbol().equals(transition1.getSymbol())).findFirst().orElse(null);
                                // If there is no corresponding transition, mark i and j as not equivalent
                                if (transition2 == null) {
                                    equivalentTable[i][j] = false;
                                    equivalentTable[j][i] = false;
                                    equivalentCount -= 2;
                                    break;
                                } else {
                                    // If the corresponding states for the transitions are not marked as equivalent, mark i and j as not equivalent
                                    if (!equivalentTable[transition1.getTo_state()][transition2.getTo_state()]) {
                                        equivalentTable[i][j] = false;
                                        equivalentTable[j][i] = false;
                                        equivalentCount -= 2;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Set<Set<Integer>> equivalentStates = new HashSet<>();
        // Loop through all pairs of states in triangle of the equivalent table
        for (int i = 0; i < equivalentTable.length; i++) {
            for (int j = 0; j < i; j++) {
                // If the pair of states is marked as equivalent
                if (equivalentTable[i][j]) {
                    boolean isAdded = false;
                    // Loop through the sets of equivalent states and add the current pair of states to an existing set
                    // if one of the states is already in that set
                    for (Set<Integer> equivalentState : equivalentStates) {
                        if (equivalentState.contains(i)) {
                            equivalentState.add(j);
                            isAdded = true;
                            break;
                        } else if (equivalentState.contains(j)) {
                            equivalentState.add(i);
                            isAdded = true;
                            break;
                        }
                    }
                    // If the pair of states is not added to an existing set, create a new set for them
                    if (!isAdded)
                        equivalentStates.add(new HashSet<>(Arrays.asList(i, j)));
                }
            }
        }

        // Create a map to store the mapping of old states to new states
        Map<Integer, Integer> equivalentMap = new HashMap<>();

        // Loop through the sets of equivalent states and map states to the representative state of the set
        for (Set<Integer> equivalentState : equivalentStates) {
            Integer representative = equivalentState.stream().min(Integer::compareTo).orElse(null);
            for (Integer state : equivalentState) {
                equivalentMap.put(state, representative);
            }
        }

        // Loop through all states and add them to the map if they are not already in it
        for (Integer state : automaton.getStates()) {
            if (!equivalentMap.containsKey(state)) {
                equivalentMap.put(state, state);
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        queue.add(automaton.getInitialStates().first());
        Set<Integer> explored = new HashSet<>();
        explored.add(equivalentMap.get(automaton.getInitialStates().first()));

        // BFS to find all reachable states from the initial state
        while (!queue.isEmpty()) {
            Integer state = queue.poll();
            for (Transition transition : automaton.getTransitionTable().getOrDefault(state, Collections.emptySet())) {
                // Add the equivalent state to the new transition table if it does not already exist
                newTransitionTable.putIfAbsent(equivalentMap.get(equivalentMap.get(state)), new HashSet<>());
                // Add new transition to the new transition table
                newTransitionTable.get(equivalentMap.get(equivalentMap.get(state))).add(
                        new Transition(transition.getSymbol(), equivalentMap.get(transition.getTo_state())));
                // Add the state to the explored set if it is not already in it
                if (!explored.contains(equivalentMap.get(transition.getTo_state()))) {
                    queue.add(transition.getTo_state());
                    explored.add(equivalentMap.get(transition.getTo_state()));
                }
            }
        }

        Set<Integer> newStates = new HashSet<>(explored);

        // Add the states, alphabet, initial states, final states and transitions to the result automaton

        for (Integer state : automaton.getFinalStates())
            if (explored.contains(equivalentMap.get(state)))
                newFinalStates.add(equivalentMap.get(state));
        newInitialStates.add(equivalentMap.get(automaton.getInitialStates().first()));

        result.clear();
        result.getAlphabet().addAll(newAlphabet);
        result.getStates().addAll(newStates);
        result.getFinalStates().addAll(newFinalStates);
        result.getInitialStates().addAll(newInitialStates);
        result.getTransitionTable().putAll(newTransitionTable);

        result.rename(0);

        return result;
    }


    /**
     * Returns a map of incoming states for each state in the automaton
     * @param automaton from which to get the incoming states
     * @return a map of incoming states for each state in the automaton
     */
    Map<Integer, Set<Integer>> getIncomingStates(Automaton automaton) {
        Map<Integer, Set<Integer>> incoming = new HashMap<>();
        for (Map.Entry<Integer, Set<Transition>> entry : automaton.getTransitionTable().entrySet()) {
            Set<Transition> value = entry.getValue();
            for (Transition transition : value) {
                incoming.putIfAbsent(transition.getTo_state(), new HashSet<>());
                incoming.get(transition.getTo_state()).add(entry.getKey());
            }
        }
        return incoming;
    }
}
