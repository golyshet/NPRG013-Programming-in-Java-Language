package cz.cuni.mff.golyshet.automaton;

import java.io.FileDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class representing a deterministic finite automaton (DFA).
 * Implements the Automaton interface.
 */
public class DFA implements Automaton {

    // DFA fields
    private final SortedSet<String> alphabet; // The alphabet of the DFA
    private final SortedSet<Integer> states; // The set of states in the DFA
    private final SortedSet<Integer> final_states; // The set of final states in the DFA
    private final SortedSet<Integer> initial_states; // The set of initial states in the DFA
    private final Map<Integer, Set<Transition>> transition_table; // The transition table of the DFA

    /**
     * Constructs an empty DFA.
     */
    public DFA() {
        // Initialize all fields as empty sets or maps
        alphabet = new TreeSet<>();
        states = new TreeSet<>();
        final_states = new TreeSet<>();
        initial_states = new TreeSet<>();
        transition_table = new HashMap<>();
    }

    @Override
    public Type getType() {
        return Type.DFA;
    }

    @Override
    public void rename(int start) {
        // Create a mapping of old state IDs to new state IDs
        Map<Integer, Integer> mappedStates = new HashMap<>();

        int stateCounter = start;
        Queue<Integer> queue = new LinkedList<>();
        queue.add(initial_states.first());
        mappedStates.put(initial_states.first(), stateCounter++);

        // BFS to traverse all reachable states from the initial state
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

        // Map any remaining states to new state IDs
        for (Integer state : states) {
            if (!mappedStates.containsKey(state)) {
                mappedStates.put(state, stateCounter++);
            }
        }

        // Create new sets and map all transitions to new state IDs
        Set<Integer> newStates = new HashSet<>(mappedStates.values());
        Set<String> newAlphabet = new HashSet<>(alphabet);
        Set<Integer> newInitialStates = initial_states.stream().map(mappedStates::get).collect(Collectors.toSet());
        Set<Integer> newFinalStates = final_states.stream().map(mappedStates::get).collect(Collectors.toSet());
        Map<Integer, Set<Transition>> newTransitionTable = transition_table.entrySet().stream()
                .collect(Collectors.toMap(entry -> mappedStates.get(entry.getKey()),
                        entry -> entry.getValue().stream()
                                .map(transition -> new Transition(transition.getSymbol(), mappedStates.get(transition.getTo_state())))
                                .collect(Collectors.toSet())));

        // Clear the DFA's old fields and replace them with the new ones
        clear();
        alphabet.addAll(newAlphabet);
        states.addAll(newStates);
        initial_states.addAll(newInitialStates);
        final_states.addAll(newFinalStates);
        transition_table.putAll(newTransitionTable);
    }

    @Override
    public void clear() {
        // Clear all fields of the DFA
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
        try {
            java.io.FileWriter fileWriter;
            // Create a FileWriter object using the given filename or the standard output if filename is empty
            if (filename.isEmpty())
                fileWriter = new java.io.FileWriter(FileDescriptor.out);
            else
                fileWriter = new java.io.FileWriter(filename);
            // Write the header line with "DFA" followed by the alphabet symbols separated by spaces
            fileWriter.write("DFA ");
            for (String symbol : alphabet)
                fileWriter.write(symbol + " ");
            fileWriter.write("\n");
            // Write the lines for each state with its type (initial, final, both or none), ID, and transitions
            for (Integer state : states) {
                // Determine the state type and write the appropriate character(s) before the state ID
                if (final_states.contains(state) && initial_states.contains(state))
                    fileWriter.write("<>");
                else if (initial_states.contains(state))
                    fileWriter.write(" >");
                else if (final_states.contains(state))
                    fileWriter.write(" <");
                else
                    fileWriter.write("  ");
                fileWriter.write(" ");
                // Write the state ID followed by a space
                fileWriter.write(state.toString());
                fileWriter.write(" ");
                // Write the transitions for each alphabet symbol separated by spaces
                for (String symbol : alphabet) {
                    // Find the transition with the current symbol for the current state
                    Transition transition = transition_table.getOrDefault(state, Collections.emptySet()).stream().filter(t -> t.getSymbol().equals(symbol)).findFirst().orElse(null);
                    if (transition == null)
                        fileWriter.write("- ");
                    // Write the ID of the destination state if there is a transition with the current symbol
                    else
                        fileWriter.write(transition.getTo_state() + " ");
                }
                fileWriter.write("\n");
            }
            fileWriter.flush();
            // Close the writer if a filename was provided
            if (!filename.isEmpty())
                fileWriter.close();
        } catch (java.io.IOException e) {
            // Print an error message if there was an error writing to the file
            System.out.println("Error while saving DFA to file.");
        }
    }
}
