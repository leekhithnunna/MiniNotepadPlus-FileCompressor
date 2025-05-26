import java.io.*;
import java.util.*;

class BitOutputStream {
    private FileOutputStream fos;
    private int buffer;
    private int bufferSize;

    public BitOutputStream(String fileName) throws IOException {
        fos = new FileOutputStream(fileName);
        buffer = 0;
        bufferSize = 0;
    }

    public void writeBit(int bit) throws IOException {
        buffer <<= 1;
        buffer |= bit & 1;
        bufferSize++;

        if (bufferSize == 8) {
            flush();
        }
    }

    public void flush() throws IOException {
        fos.write(buffer);
        buffer = 0;
        bufferSize = 0;
    }

    public void close() throws IOException {
        flush();
        fos.close();
    }
}

class HuffmanNode implements Comparable<HuffmanNode> {
    int frequency;
    char data;
    HuffmanNode left, right;

    public HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        left = right = null;
    }

    public int compareTo(HuffmanNode node) {
        return frequency - node.frequency;
    }
}

class HuffmanCompressor {
    public static void compressFile(String inputFileName, String outputFileName) throws IOException {
        FileInputStream fis = new FileInputStream(inputFileName);
        BitOutputStream bos = new BitOutputStream(outputFileName);

        int[] frequencies = new int[256]; // Assuming ASCII characters

        int character;
        while ((character = fis.read()) != -1) {
            frequencies[character]++;
        }

        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                priorityQueue.add(new HuffmanNode((char) i, frequencies[i]));
            }
        }

        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();

            HuffmanNode mergedNode = new HuffmanNode('\0', left.frequency + right.frequency);
            mergedNode.left = left;
            mergedNode.right = right;
            priorityQueue.add(mergedNode);
        }

        HuffmanNode root = priorityQueue.peek();

        Map<Character, String> huffmanCodes = new HashMap<>();
        generateCodes(root, "", huffmanCodes);

        // Write Huffman codes to the output file
        for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
            char characterKey = entry.getKey();
            String codeValue = entry.getValue();
            bos.writeBit(1); // Indicate a Huffman code is being written
            bos.writeBit(characterKey); // Write the character
            for (char bit : codeValue.toCharArray()) {
                bos.writeBit(bit - '0'); // Write Huffman code bits
            }
            bos.writeBit(0); // Indicate end of Huffman code for this character
        }

        // Write separator between codes and compressed data
        bos.writeBit(1);
        bos.writeBit(0);

        // Write compressed data bit by bit
        fis.close();
        fis = new FileInputStream(inputFileName);
        while ((character = fis.read()) != -1) {
            String code = huffmanCodes.get((char) character);
            for (char bit : code.toCharArray()) {
                bos.writeBit(bit - '0');
            }
        }

        fis.close();
        bos.close();
    }

    private static void generateCodes(HuffmanNode node, String code, Map<Character, String> huffmanCodes) {
        if (node == null) return;

        if (node.data != '\0') {
            huffmanCodes.put(node.data, code);
        }

        generateCodes(node.left, code + "0", huffmanCodes);
        generateCodes(node.right, code + "1", huffmanCodes);
    }
}



