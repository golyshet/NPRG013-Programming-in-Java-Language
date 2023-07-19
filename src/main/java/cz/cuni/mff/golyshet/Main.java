package cz.cuni.mff.golyshet;

import cz.cuni.mff.golyshet.automaton.Automaton;
import cz.cuni.mff.golyshet.input_parser.CommandLineParser;
import cz.cuni.mff.golyshet.operation.Operation;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class of the program
 * It parses the command line arguments and performs the operations
 * It also asks the user for the name of the file to save the result of the operation
 * If the user does not enter anything, the result is printed in stdout
 * @see CommandLineParser
 */

public class Main {
    /**
     * Main method of the program
     * @param args command line arguments
     */
    public static void main(String[] args) {
        CommandLineParser parser = new CommandLineParser();
        Map<Operation, List<String>> operation_list = parser.parse(args);
        Automaton automaton = null;
        // Perform the operations and save the result in the file or print it in stdout
        for (Map.Entry<Operation, List<String>> entry: operation_list.entrySet()) {
            System.out.println("Enter the name of file to save result of operation or press enter to print in stdout:");
            Scanner scanner = new Scanner(System.in);
            String filename = scanner.nextLine();
            filename = filename.strip();
            automaton = entry.getKey().perform(entry.getValue());
            automaton.save(String.valueOf(filename));
        }
    }
}
