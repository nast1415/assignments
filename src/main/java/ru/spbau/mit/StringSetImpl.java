package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class StringSetImpl implements StreamSerializable, StringSet {

    final private static int MAX_N = 239017;
    final private static int MAX_NUMBER_OF_LETTERS = 60;
    final private static int SIZE_OF_ALPHABET = 26;
    final private static int NO_TRANSITION = 0;

    private Node[] arrayOfNodes = new Node[MAX_N]; // Create array of nodes
    private Node root = new Node(); // Create a root
    private int lastIndex = 0;

    public StringSetImpl() {
        arrayOfNodes[0] = root;
        lastIndex++;
    }

    public static class Node {
        private int[] transits = new int[MAX_NUMBER_OF_LETTERS]; // Array of transitions on letters
        private boolean isTerminal;
        private int numberOfTerminal;

        private Node() { // Node constructor
            for (int i = 0; i < MAX_NUMBER_OF_LETTERS; i++) {
                transits[i] = NO_TRANSITION;
            }
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

    public int getElementNumber(int value) {
        int transitPass;
        if (value <= 'z' && value >= 'a') {
            transitPass = value - 'a' + SIZE_OF_ALPHABET;
        } else {
            transitPass = value - 'A';
        }
        return transitPass;
    }

    public boolean add(String element) {
        Node vertex = root;
        int transitPass;
        int value;

        if (contains(element))
            return false;
        else {
            vertex.numberOfTerminal++;
            for (char c : element.toCharArray()) {
                value = getElementNumber(c);
                transitPass = vertex.transits[value];

                if (transitPass != NO_TRANSITION) {
                    vertex = arrayOfNodes[transitPass];
                    vertex.numberOfTerminal++;
                } else {
                    vertex.transits[value] = lastIndex;
                    vertex = new Node();
                    vertex.numberOfTerminal++;
                    arrayOfNodes[lastIndex] = vertex;
                    lastIndex++;
                }
            }
        }
        vertex.setIsTerminal(true);
        return true;
    }

    public Node getVertex(String element) {
        Node vertex = root;
        for (int i = 0; i < element.length(); i++) {
            int value = getElementNumber(element.charAt(i));
            int transitPass = vertex.transits[value];

            if (transitPass != NO_TRANSITION) {
                vertex = arrayOfNodes[transitPass];
            } else {
                return null;
            }
        }
        return vertex;
    }

    public boolean contains(String element) {
        Node vertex = getVertex(element);
        return vertex != null && vertex.isTerminal;
    }

    public boolean remove(String element) {
        Node vertex = root;

        if (!contains(element)) {
            return false;
        } else {
            for (int i = 0; i < element.length(); i++) {
                int value = getElementNumber(element.charAt(i));
                int transitPass = vertex.transits[value];
                vertex = arrayOfNodes[transitPass];
                vertex.numberOfTerminal--;
            }

            vertex.setIsTerminal(false);

            vertex = root;
            vertex.numberOfTerminal--;

        }
        return true;
    }

    public int size() {
        return root.numberOfTerminal;
    }

    public int howManyStartsWithPrefix(String prefix) {

        Node vertex = getVertex(prefix);
        if (vertex == null) {
            return 0;
        }
        return vertex.numberOfTerminal;
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
                Node vertex = arrayOfNodes[i];
                for (int j = 0; j < MAX_NUMBER_OF_LETTERS; j++) {
                    printInt(out, vertex.transits[j]);
                } // Write transits array of vertex
                printInt(out, vertex.numberOfTerminal); // Write number of terminal vertices in the vertex-tree
                if (vertex.isTerminal()) {
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
                Node vertex = new Node();
                for (int j = 0; j < MAX_NUMBER_OF_LETTERS; j++) {
                    vertex.transits[j] = readInt(in);
                }
                vertex.numberOfTerminal = readInt(in);
                int isTerm = readInt(in);
                vertex.isTerminal = isTerm == 1;
                arrayOfNodes[lastIndex] = vertex;
                lastIndex++;
            }
            root = arrayOfNodes[0];

        } catch (IOException e) {
            throw new SerializationException();
        }
    }
}
