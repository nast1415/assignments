package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StringSetImpl implements StreamSerializable, StringSet {

    final private static int MAX_N = 239017;

    Node[] arrayOfNodes = new Node[MAX_N]; // Create array of nodes
    Node root = new Node(); // Create a root
    int lastIndex = 0;
    private String prefix;

    StringSetImpl() {
        arrayOfNodes[0] = root;  // Add root as a 0-th element of this array
        lastIndex++;
    }

    public class Node {
        int[] transits = new int[60]; // Array of transitions on letters
        private boolean isTerminal;
        int numberOfTerminal;

        Node() { // Node constructor
            for (int i = 0; i < 60; i++) {
                transits[i] = 0;
            } // 0 shows us that there is no transition on the letter i
            setIsTerminal(false);
            numberOfTerminal = 0;
        }

        public boolean isTerminal() {
            return isTerminal;
        }

        public void setIsTerminal(boolean isTerminal) {
            this.isTerminal = isTerminal;
        }
    }

    public int setElementNumber(int value) {
        int letterNumber;
        if (value <= 'z' && value >= 'a') {
            letterNumber = value - 'a' + 26;
        } else {
            letterNumber = value - 'A';
        }
        return letterNumber;
    }

    public boolean add(String element) {
        Node v = root;
        boolean isContains;
        int letterNumber;
        int value;

        for (int i = 0; i < element.length(); i++) {
            value = setElementNumber(element.charAt(i));
            letterNumber = v.transits[value];

            if (letterNumber != 0) {
                v = arrayOfNodes[letterNumber];
            } else {
                v.transits[value] = lastIndex;
                v = new Node();
                arrayOfNodes[lastIndex] = v;
                lastIndex++;
            }
        }
        isContains = v.isTerminal();
        v.isTerminal = true;
        if (!isContains) {
            v = root;
            v.numberOfTerminal++;
            for (int i = 0; i < element.length(); i++) {
                value = setElementNumber(element.charAt(i));
                letterNumber = v.transits[value];
                v = arrayOfNodes[letterNumber];
                v.numberOfTerminal++;
            }
        }
        return !isContains;
    }

    public boolean contains(String element) {
        Node v = root;

        for (int i = 0; i < element.length(); i++) {
            int value = setElementNumber(element.charAt(i));
            int letterNumber = v.transits[value];

            if (letterNumber != 0) {
                v = arrayOfNodes[letterNumber];
            } else {
                return false;
            }
        }
        return v.isTerminal;
    }

    public boolean remove(String element) {
        Node v = root;

        if (!contains(element)) {
            return false;
        } else {
            for (int i = 0; i < element.length(); i++) {
                int value = setElementNumber(element.charAt(i));
                int letterNumber = v.transits[value];
                v = arrayOfNodes[letterNumber];
                v.numberOfTerminal--;
            }

            v.isTerminal = false;

            v = root;
            v.numberOfTerminal--;

        }
        return true;
    }

    public int size() {
        return root.numberOfTerminal;
    }

    public int howManyStartsWithPrefix(String prefix) {
        Node v = root;

        for (int i = 0; i < prefix.length(); i++) {
            int value = setElementNumber(prefix.charAt(i));
            int letterNumber = v.transits[value];

            if (letterNumber != 0) {
                v = arrayOfNodes[letterNumber];
            } else {
                return 0;
            }
        }
        return v.numberOfTerminal;
    }

    public void serialize(OutputStream out) throws SerializationException {
        try {
            out.write(lastIndex); // ArraySize
            for (int i = 0; i < lastIndex; i++) {
                Node v = arrayOfNodes[i];
                for (int j = 0; j < 60; j++) {
                    out.write(v.transits[j]);
                } // Write transits array of v
                out.write(v.numberOfTerminal); // Write number of terminal vertices in the v-tree
                if (v.isTerminal()) {
                    out.write(1);
                } else {
                    out.write(0);
                }
            }
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    public void deserialize(InputStream in) throws SerializationException {
        for (int i = 0; i < lastIndex; i++) {
            arrayOfNodes[i] = null;
        }
        lastIndex = 0;
        try {
            int arraySize = in.read(); // Read array size
            for (int i = 0; i < arraySize; i++) {
                Node v = new Node();
                for (int j = 0; j < 60; j++) {
                    v.transits[j] = in.read();
                }
                v.numberOfTerminal = in.read();
                int isTerm = in.read();
                v.isTerminal = isTerm == 1;
                arrayOfNodes[lastIndex] = v;
                lastIndex++;
            }
            root = arrayOfNodes[0];

        } catch (IOException e) {
            throw new SerializationException();
        }
    }
}
