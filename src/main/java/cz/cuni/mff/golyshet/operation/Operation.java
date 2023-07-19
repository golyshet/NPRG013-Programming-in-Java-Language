package cz.cuni.mff.golyshet.operation;

import cz.cuni.mff.golyshet.automaton.Automaton;

import java.util.List;

/**
 * Interface for operations
 * All operations must implement this interface
 * All operations must be in the package cz.cuni.mff.golyshet.operation
 */

public interface Operation {

    /**
     * Performs the operation on the given automaton
     * @param paths to the files needed for the operation
     * @return automaton after the operation
     */
    public Automaton perform(List<String> paths);

}
