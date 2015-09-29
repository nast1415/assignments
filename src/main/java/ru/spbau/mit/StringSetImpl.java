package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class StringSetImpl implements StreamSerializable, StringSet {
    final private static int MAX_NUMBER_OF_LETTERS = 60;
    final private static int SIZE_OF_ALPHABET = 26;
    final private static int NO_TRANSITION = 0;

    List<Node> arrayOfNodes = new ArrayList<>(); // Create ArrayList of nodes
    private Node root = new Node(); // Create a root
    private int lastIndex = 1;

    public StringSetImpl() {
        arrayOfNodes.add(root);
    }

    public static class Node {
        private int[] transits = new int[MAX_NUMBER_OF_LETTERS]; // Array of transitions on letters
        private boolean isTerminal;
        private int numberOfTerminal;

        public Node() { // Node constructor
            for (int i = 0; i < MAX_NUMBER_OF_LETTERS; i++) {
                transits[i] = NO_TRANSITION;
            }
            setIsTerminal(false);
            numberOfTerminal = 0;
        }

        public boolean isTerminal() {
            return isTerminal;
        }

        private void setIsTerminal(boolean isTerminal) {
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
                    vertex = arrayOfNodes.get(transitPass);
                } else {
                    vertex.transits[value] = lastIndex;
                    vertex = new Node();
                    arrayOfNodes.add(vertex);
                    lastIndex++;
                }
                vertex.numberOfTerminal++;
            }
        }
        vertex.setIsTerminal(true);
        return true;
    }

    public Node getVertex(String element) {
        Node vertex = root;
        for (char c : element.toCharArray()) {
            int value = getElementNumber(c);
            int transitPass = vertex.transits[value];

            if (transitPass != NO_TRANSITION) {
                vertex = arrayOfNodes.get(transitPass);
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
            for (char c : element.toCharArray()) {
                int value = getElementNumber(c);
                int transitPass = vertex.transits[value];
                vertex = arrayOfNodes.get(transitPass);
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
        byte[] value;
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
            for (Node vertex: arrayOfNodes) {
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
        arrayOfNodes.clear();
        lastIndex = 0;
        int i;
        try {
            //int arraySize = readInt(in); // Read array size
            while (in.available() != 0){
                Node vertex = new Node();
                for (int j = 0; j < MAX_NUMBER_OF_LETTERS; j++) {
                    vertex.transits[j] = readInt(in);
                }
                vertex.numberOfTerminal = readInt(in);
                int isTerm = readInt(in);
                vertex.isTerminal = isTerm == 1;
                arrayOfNodes.add(vertex);
                lastIndex++;
            }
            root = arrayOfNodes.get(0);

        } catch (IOException e) {
            throw new SerializationException();
        }
    }
}
