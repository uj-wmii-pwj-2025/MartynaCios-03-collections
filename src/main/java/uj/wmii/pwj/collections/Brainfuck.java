package uj.wmii.pwj.collections;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public interface Brainfuck {

    /**
     * Executes uploaded program.
     */
    void execute();

    /**
     * Creates a new instance of Brainfuck interpreter with given program, using standard IO and stack of 1024 size.
     * @param program brainfuck program to interpret
     * @return new instance of the interpreter
     * @throws IllegalArgumentException if program is null or empty
     */
    static Brainfuck createInstance(String program) {
        return createInstance(program, System.out, System.in, 1024);
    }

    /**
     * Creates a new instance of Brainfuck interpreter with given parameters.
     * @param program brainfuck program to interpret
     * @param out output stream to be used by interpreter implementation
     * @param in input stream to be used by interpreter implementation
     * @param stackSize maximum stack size, that is allowed for this interpreter
     * @return new instance of the interpreter
     * @throws IllegalArgumentException if: program is null or empty, OR out is null, OR in is null, OR stackSize is below 1.
     */
    static Brainfuck createInstance(String program, PrintStream out, InputStream in, int stackSize) {
        return new BrainfuckImpl(program, out, in, stackSize);
    }
}

class BrainfuckImpl implements Brainfuck {
    private String code;
    private PrintStream output;
    private InputStream input;
    private byte[] storage;
    private int pointer;

    public BrainfuckImpl(String program, PrintStream out, InputStream in, int stackSize) {
        if (program == null || program.isEmpty()) {
            throw new IllegalArgumentException("Program cannot be null or empty");
        }
        if (out == null) {
            throw new IllegalArgumentException("Output stream must not be null");
        }
        if (in == null) {
            throw new IllegalArgumentException("Input stream must not be null");
        }
        if (stackSize < 1) {
            throw new IllegalArgumentException("Stack size must be at least 1");
        }

        validateBrackets(program);

        code = program;
        output = out;
        input = in;
        storage = new byte[stackSize];
        pointer = 0;
    }

    private void validateBrackets(String program) {
        List<Integer> stack = new ArrayList<>();

        for (int i = 0; i < program.length(); i++) {
            char c = program.charAt(i);

            if (c == '[') {
                stack.add(i);
            } else if (c == ']') {
                if (stack.size() == 0) {
                    throw new IllegalArgumentException(
                            "Closing bracket without matching opening at position " + i);
                }
                stack.remove(stack.size() - 1);
            }
        }

        if (stack.size() > 0) {
            throw new IllegalArgumentException(
                    "Opening bracket without matching closing at position " +
                            stack.get(stack.size() - 1));
        }
    }

    @Override
    public void execute() {
        int position = 0;

        while (position < code.length()) {
            char instruction = code.charAt(position);

            if (instruction == '>') {
                pointer++;
                if (pointer >= storage.length) {
                    throw new IndexOutOfBoundsException("Data pointer exceeded memory size");
                }
            } else if (instruction == '<') {
                pointer--;
                if (pointer < 0) {
                    throw new IndexOutOfBoundsException("Data pointer went below zero");
                }
            } else if (instruction == '+') {
                storage[pointer] = (byte)(storage[pointer] + 1);
            } else if (instruction == '-') {
                storage[pointer] = (byte)(storage[pointer] - 1);
            } else if (instruction == '.') {
                output.write(storage[pointer] & 0xFF);
            } else if (instruction == ',') {
                try {
                    int readValue = input.read();
                    if (readValue != -1) {
                        storage[pointer] = (byte) readValue;
                    }
                } catch (Exception e) {
                }
            } else if (instruction == '[') {
                if (storage[pointer] == 0) {
                    position = locateMatchingBracket(position, true);
                }
            } else if (instruction == ']') {
                if (storage[pointer] != 0) {
                    position = locateMatchingBracket(position, false);
                }
            }

            position++;
        }

        output.flush();
    }

    private int locateMatchingBracket(int start, boolean forward) {
        char lookingFor;
        char skipOver;

        if (forward) {
            lookingFor = ']';
            skipOver = '[';
        } else {
            lookingFor = '[';
            skipOver = ']';
        }

        int depth = 1;
        int pos = start + (forward ? 1 : -1);

        for (; pos >= 0 && pos < code.length(); pos += (forward ? 1 : -1)) {
            char ch = code.charAt(pos);

            if (ch == skipOver) {
                depth++;
                continue;
            }

            if (ch == lookingFor) {
                depth--;
                if (depth != 0) {
                    continue;
                }

                if (forward) {
                    return pos;
                } else {
                    return pos - 1;
                }
            }
        }

        throw new IllegalArgumentException("Unmatched bracket at position: " + start);
    }
}