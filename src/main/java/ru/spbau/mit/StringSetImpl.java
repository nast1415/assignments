package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class StringSetImpl implements StreamSerializable, StringSet {

    final private static int MAX_N = 239017;
    final private static int MAX_NUMBER_OF_LETTERS = 60;
    final private static int SIZE_OF_ALPHABET = 26;

    Node[] arrayOfNodes = new Node[MAX_N]; // Create array of nodes
    Node root = new Node(); // Create a root
    int lastIndex = 0;

    public StringSetImpl() {
        arrayOfNodes[0] = root;  // Add root as a 0-th element of this array
        lastIndex++;
    }

    public class Node {
        int[] transits = new int[MAX_NUMBER_OF_LETTERS]; // Array of transitions on letters
        private boolean isTerminal;
        int numberOfTerminal;

        Node() { // Node constructor
            for (int i = 0; i < MAX_NUMBER_OF_LETTERS; i++) {
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
        int transitPass;
        if (value <= 'z' && value >= 'a') {
            transitPass = value - 'a' + SIZE_OF_ALPHABET;
        } else {
            transitPass = value - 'A';
        }
        return transitPass;
    }

    public boolean add(String element) {
        Node v = root;
        boolean isContains;
        int transitPass;
        int value;

        for (int i = 0; i < element.length(); i++) {
            value = setElementNumber(element.charAt(i));
            transitPass = v.transits[value];

            if (transitPass != 0) {
                v = arrayOfNodes[transitPass];
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
                transitPass = v.transits[value];
                v = arrayOfNodes[transitPass];
                v.numberOfTerminal++;
            }
        }
        return !isContains;
    }

    public Node getVertice(String element) {
        Node v = root;
        for (int i = 0; i < element.length(); i++) {
            int value = setElementNumber(element.charAt(i));
            int transitPass = v.transits[value];

            if (transitPass != 0) {
                v = arrayOfNodes[transitPass];
            } else {
                return null;
            }
        }
        return v;
    }

    public boolean contains(String element) {
        Node v = getVertice(element);
        return v != null && v.isTerminal;
    }

    public boolean remove(String element) {
        Node v = root;

        if (!contains(element)) {
            return false;
        } else {
            for (int i = 0; i < element.length(); i++) {
                int value = setElementNumber(element.charAt(i));
                int transitPass = v.transits[value];
                v = arrayOfNodes[transitPass];
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

        Node v = getVertice(prefix);
        if (v == null) {
            return 0;
        }
        return v.numberOfTerminal;
    }

    private int readInt(InputStream in) throws IOException, SerializationException {
        byte [] value;
        value = new byte[4];
        int number = in.read(value, 0, 4);
        if (number != 4) {
            throw new SerializationException();
        }
        return ByteBuffer.wrap(value).getInt();
    }

    private void printInt(OutputStream out, int element) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(element).array());
    }

    public void serialize(OutputStream out) throws SerializationException {
        try {
            printInt(out, lastIndex); // ArraySize
            for (int i = 0; i < lastIndex; i++) {
                Node v = arrayOfNodes[i];
                for (int j = 0; j < MAX_NUMBER_OF_LETTERS; j++) {
                    printInt(out, v.transits[j]);
                } // Write transits array of v
                printInt(out, v.numberOfTerminal); // Write number of terminal vertices in the v-tree
                if (v.isTerminal()) {
                    printInt(out, 1);
                } else {
                    printInt(out, 0);
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
            int arraySize = readInt(in); // Read array size
            for (int i = 0; i < arraySize; i++) {
                Node v = new Node();
                for (int j = 0; j < MAX_NUMBER_OF_LETTERS; j++) {
                    v.transits[j] = readInt(in);
                }
                v.numberOfTerminal = readInt(in);
                int isTerm = readInt(in);
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
