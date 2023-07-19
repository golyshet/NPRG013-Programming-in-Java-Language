package cz.cuni.mff.golyshet.fileReader;

import cz.cuni.mff.golyshet.automaton.Automaton;
import cz.cuni.mff.golyshet.automaton.DFA;
import cz.cuni.mff.golyshet.automaton.NFA;
import cz.cuni.mff.golyshet.automaton.Transition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * This class is used to read the input file and create an Automaton object from it.
 * Reader must be in the package cz.cuni.mff.golyshet.fileReader
 */
public class Reader {

    /**
     * This method takes a path to a file as input and returns an Automaton object.
     * @param path to the file
     * @return an Automaton object
     */
    public Automaton read(String path) {
        try {
            // Create a reader for the file
            FileReader reader = new FileReader(path);
            Automaton automaton = null;
            BufferedReader br = new BufferedReader(reader);
            // Read the first line of the file, which contains the type of automaton and the alphabet.
            String str = br.readLine();
            if (str == null || str.isEmpty()) {
                System.out.println("File is empty");
                System.exit(1);
            }
            // Get the type of automaton and its alphabet from the first line
            String[] typeAndAlphabet = str.split("\\s+");
            if (typeAndAlphabet.length < 2) {
                System.out.println("Wrong format type and alphabet of file");
                System.exit(1);
            }
            // Determine the type of automaton based on the first word in the first line of the file.
            if (typeAndAlphabet[0].equals("DFA")) {
                automaton = new DFA();
            } else if (typeAndAlphabet[0].equals("NFA")) {
                automaton = new NFA();
            } else {
                System.out.println("Wrong format of file");
                System.exit(1);
            }
            // Add the alphabet to the automaton
            List<String> alphabet = new ArrayList<>();
            for (int i = 1; i < typeAndAlphabet.length; i++) {
                automaton.getAlphabet().add(typeAndAlphabet[i]);
                alphabet.add(typeAndAlphabet[i]);
            }
            if (alphabet.size() != automaton.getAlphabet().size()) {
                System.exit(1);
            }
            // Read the rest of the file line by line
            str = br.readLine();
            while (str != null) {
                str = str.trim();
                int transitionBegin = 2;
                String[] line = str.split("\\s+");
                int state;
                // Check if the line is a final state, an initial state, or none of them
                if (Objects.equals(line[0], ">")) {
                    automaton.getInitialStates().add(Integer.parseInt(line[1]));
                    automaton.getStates().add(Integer.parseInt(line[1]));
                    state = Integer.parseInt(line[1]);
                } else if (Objects.equals(line[0], "<")) {
                    automaton.getFinalStates().add(Integer.parseInt(line[1]));
                    automaton.getStates().add(Integer.parseInt(line[1]));
                    state = Integer.parseInt(line[1]);
                } else if (Objects.equals(line[0], "<>") || Objects.equals(line[0], "><")) {
                    automaton.getFinalStates().add(Integer.parseInt(line[1]));
                    automaton.getInitialStates().add(Integer.parseInt(line[1]));
                    automaton.getStates().add(Integer.parseInt(line[1]));
                    state = Integer.parseInt(line[1]);
                } else {
                    transitionBegin = 1;
                    state = Integer.parseInt(line[0]);
                    automaton.getStates().add(Integer.parseInt(line[0]));
                }

                // Parse the transitions for the current state
                int currentSymbol = 0;
                for (int i = transitionBegin; i < line.length; i++) {
                    for (String cstate: line[i].split("\\|")) {
                        if (Objects.equals(cstate, "-"))
                            break;
                        int to_state = Integer.parseInt(cstate);
                        automaton.getTransitionTable().computeIfAbsent(state, k -> new HashSet<>())
                                .add(new Transition(alphabet.get(currentSymbol), to_state));
                        automaton.getStates().add(to_state);
                    }
                    currentSymbol++;
                }
                // Read the next line
                str = br.readLine();
            }
            return automaton;
        } catch (Exception e) {
            System.out.println("Error while reading file or file does not exist");
            System.exit(1);
        }
        return null;
    }
}
