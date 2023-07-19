package cz.cuni.mff.golyshet.input_parser;

import cz.cuni.mff.golyshet.operation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that parses the command-line arguments and returns a map of operations and their arguments.
 * Implements Parser interface
 * All parsers must be in the package cz.cuni.mff.golyshet.input_parser
 */
public class CommandLineParser implements Parser {
    // Create a map to store the operation and its arguments
    Map<Operation, List<String>> operation_list = new LinkedHashMap<>();

    /**
     * Parses the command-line arguments and returns a map of operations and their arguments
     * @param args the command-line arguments
     * @return a map of operations and their arguments
     */
    public Map<Operation, List<String>> parse(String[] args) {
        // Check if there are any arguments
        if (args.length == 0) {
            System.out.println("No args");
            System.exit(1);
        }

        // Parse the arguments
        int position = 0;
        while (position < args.length) {
            // Check if the argument is an operation
            switch (args[position]) {
                // If it's intersection, add an Intersection operation and its arguments to the map
                case "--intersection" -> {
                    if (position + 2 >= args.length) {
                        System.out.println("Error path for intersection");
                        System.exit(1);
                    }
                    operation_list.put(new Intersection(), List.of(args[position + 1], args[position + 2]));
                    position += 3;
                }
                // If it's union, add a Union operation and its arguments to the map
                case "--union" -> {
                    if (position + 2 >= args.length) {
                        System.out.println("Error path for union");
                        System.exit(1);
                    }
                    operation_list.put(new Union(), List.of(args[position + 1], args[position + 2]));
                    position += 3;
                }
                // If it's concatenation, add a Concatenation operation and its arguments to the map
                case "--concatenation" -> {
                    if (position + 2 >= args.length) {
                        System.out.println("Error path for concatenation");
                        System.exit(1);
                    }
                    operation_list.put(new Concatenation(), List.of(args[position + 1], args[position + 2]));
                    position += 3;
                }
                // If it's determinization, add a Determinization operation and its arguments to the map
                case "--determinization" -> {
                    if (position + 1 >= args.length) {
                        System.out.println("Error path for determinization");
                        System.exit(1);
                    }
                    operation_list.put(new Determinization(), List.of(args[position + 1]));
                    position += 2;
                }
                // If it's minimization, add a Minimization operation and its argument to the map
                case "--minimization" -> {
                    if (position + 1 >= args.length) {
                        System.out.println("Error path for minimization");
                        System.exit(1);
                    }
                    operation_list.put(new Minimization(), List.of(args[position + 1]));
                    position += 2;
                }
                // If it's an unknown operation, print an error message and exit the program
                default -> {
                    System.out.println("Unknown operation");
                    System.exit(1);
                }
            }
        }
        // Return the map of operations and their arguments
        return operation_list;
    }
}
