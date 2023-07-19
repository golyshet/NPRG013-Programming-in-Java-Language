# NPRG013-Programming-in-Java-Language

## Run
The program works for several operations: 
•	Determinozation, Minimization for one automaton.
•	Intersection, Concatenation, Union for two automata.
To perform the operation, you need to pass the name of the operation 
(--intersection or --union or  --concatenation or –determinization or --minimization) and the path to the required number of files as program arguments.
You can perform more operations at once, for each you need to specify the required number of paths (one for single automaton operation, two for multiple) to files. Output is either to a file or to standard output. After starting the program with the necessary arguments, the output format will be offered: when you enter a file name, it will be saved to a file, when you press Enter, it will be saved to standard output. And so for each operation.
The file means the automation file. The final result of the operation is always a deterministic minimal automaton.

Example of correct program arguments: 
--intersection tests/test_1.txt tests/test_11.txt --minimization tests/nfa.txt

## Requirements to automaton file
The automaton file is expected to contain the following information:
The type of automaton (either "DFA" or "NFA") and its alphabet on the first line, separated by whitespace, epsilon will be denoted in the alphabet as "*".
The states, transitions, initial states, and final states of the automaton on subsequent lines, using the following format:
A line starting with ">" indicates an initial state.
A line starting with "<" indicates a final state.
A line starting with "<>" or "><" indicates a state that is both initial and final.
A line starting or following the state type (initial, final or both) with a state number (integer) indicates a transition from that state to other states, with the symbols in the alphabet as labels. The more transitions at one symbol are separated by "|", absence of transition is "-" .

Example: 
We have this automaton file:
NFA a b *
> 0 2 0 -
< 1 1 1 1
<> 2 - - 1|2|3
3 0 - -
It is means that we have NFA automaton with {a, b} alphabet. 
State 0: This is the initial state. It has transitions to 2 on "a" and itself on "b". It also has not an epsilon transition.
State 1: This is the finial state. It has transitions to itself on "a", "b" and epsilon.
State 2: This is the initial and final state. It has a transition to state 1, 2 and 3 on epsilon.
State 3: This is not initial and not final state. It has a transition to 0 on "a".

## Test
To test operations that require two automata, you can use paired files from the tests folder, files test_x and test_xx.  Instead of x, you should put the numbers 1, 2, 3 or 4, respectively. To test operations that require one automaton, you can use any file from the tests folder. You can create a new automaton(s) in accordance with the requirements for automaton fie.


