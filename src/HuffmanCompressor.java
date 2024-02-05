import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class HuffmanCompressor {
    private final String filePath;
    private final int n;
    private final long chunkSize;
    private int totalSize = 0;
    private String outputPath;
    private Map<String, Integer> frequencies;
    private final Map<String, String> huffmanCodes = new LinkedHashMap<>();

    public HuffmanCompressor(String filePath, int n) throws IOException {
        this.filePath = filePath;
        this.n = n;
        this.chunkSize = getChunkSize();
        String[] split;
        if (filePath.contains("\\")) {
            split = filePath.split("\\\\");
            split[split.length - 1] = "20011982." + n + "." + split[split.length - 1] + ".hc";
            outputPath = String.join("\\", split);
        } else {
            split = filePath.split("/");
            split[split.length - 1] = "20011982." + n + "." + split[split.length - 1] + ".hc";
            outputPath = String.join("/", split);
        }
        getBytesFrequencies();
        buildHuffmanTree();
    }

    private void getBytesFrequencies() throws IOException {
        frequencies = new LinkedHashMap<>();                                                        // LinkedHashMap to preserve the order of insertion


        FileInputStream fis = new FileInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        while (true) {
            byte[] chunk = new byte[(int) chunkSize];
            int numOfBytesRead = bis.read(chunk, 0, (int) chunkSize);
            if (numOfBytesRead <= 0) {
                break;
            }
            totalSize += numOfBytesRead;
            int index = 0;                                                                                           // index of the current byte in the chunk
            while (index < numOfBytesRead) {
                int bufferSize = Math.min(n, numOfBytesRead - index);
                byte[] buffer = new byte[bufferSize];
                System.arraycopy(chunk, index, buffer, 0, bufferSize);
                StringBuilder stringBuilder = new StringBuilder();
                for (byte b : buffer) {
                    stringBuilder.append((char) (b & 0xFF));                                        // convert byte to char of ISO-8859-1 all +VEs
                }
                String str = stringBuilder.toString();
                frequencies.put(str, frequencies.getOrDefault(str, 0) + 1);
                index += bufferSize;
            }
        }
        bis.close();
        fis.close();
    }

    private void buildHuffmanTree() {
        PriorityQueue<HuffmanNode> PQ = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            PQ.add(new HuffmanLeaf(entry.getValue(), entry.getKey()));
        }
        buildHuffmanTreeHelper(PQ);
    }

    private void buildHuffmanTreeHelper(PriorityQueue<HuffmanNode> PQ) {
        while (PQ.size() > 1) {
            HuffmanNode leftNode = PQ.poll();
            HuffmanNode rightNode = PQ.poll();
            PQ.add(new HuffmanNode(leftNode, Objects.requireNonNull(rightNode)));
        }
        HuffmanNode root = PQ.poll();
        generateHuffmanCodes(root, "");
    }

    private void generateHuffmanCodes(HuffmanNode root, String code) {
        if (root instanceof HuffmanLeaf leaf) {
            huffmanCodes.put(leaf.getValue(), code);
        } else {
            generateHuffmanCodes(root.getLeftNode(), code + "0");
            generateHuffmanCodes(root.getRightNode(), code + "1");
        }
    }

    public void compress() throws IOException {

        FileOutputStream fos = new FileOutputStream(outputPath);

        fos.write((totalSize >> 24) & 0xFF);
        fos.write((totalSize >> 16) & 0xFF);
        fos.write((totalSize >> 8) & 0xFF);
        fos.write(totalSize & 0xFF);

        fos.write((n >> 24) & 0xFF);
        fos.write((n >> 16) & 0xFF);
        fos.write((n >> 8) & 0xFF);
        fos.write(n & 0xFF);

        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(frequencies);

        FileInputStream fis = new FileInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(fis);

        StringBuilder encodedString = new StringBuilder();
        StringBuilder tempEncodedString = new StringBuilder();

        int numOfBytesRead;
        while (true) {
            byte[] chunk = new byte[(int) chunkSize];                                                                // chunk of bytes  LOOO MULTI OF 8 M4 HAN7TAG ELI T7T DAH
            numOfBytesRead = bis.read(chunk, 0, (int) chunkSize);
            if (numOfBytesRead <= 0) {
                break;
            }
            int index = 0;                                                                                           // index of the current byte in the chunk
            while (index < numOfBytesRead) {
                int bufferSize = Math.min(n, numOfBytesRead - index);
                byte[] buffer = new byte[bufferSize];
                System.arraycopy(chunk, index, buffer, 0, bufferSize);
                StringBuilder stringBuilder = new StringBuilder();
                for (byte b : buffer) {
                    stringBuilder.append((char) (b & 0xFF));                                        // convert byte to char of ISO-8859-1 all +VEs
                }
                String str = stringBuilder.toString();
                encodedString.append(huffmanCodes.get(str));
                index += bufferSize;
            }
            // get the max multiple of 8 and save the rest for the next iteration
            if (encodedString.length() > 8) {
                CharSequence charSequence = encodedString.subSequence(encodedString.length() - (encodedString.length() % 8), encodedString.length());
                tempEncodedString = new StringBuilder(charSequence.toString());
                encodedString.delete(encodedString.length() - (encodedString.length() % 8), encodedString.length());
            }
            byte[] encodedBytes = convertToBytes(encodedString.toString());
            fos.write(encodedBytes);
            encodedString = tempEncodedString;
        }
        if (!(encodedString.isEmpty())) {
            byte[] encodedBytes = convertToBytes(encodedString.toString());
            fos.write(encodedBytes);
        }
        bis.close();
        fis.close();

        int compressedSize = (int) new File(outputPath).length();
        System.out.println("Compression Ratio " + (double)compressedSize / (double)totalSize);
    }

    private byte[] convertToBytes(String encodedStringChunk) {
        int length = encodedStringChunk.length();
        int padding = (Byte.SIZE - (length % Byte.SIZE)) % Byte.SIZE;                   // Calculate padding where Byte.size = 8
        int totalBits = length + padding;                                                                   // Calculate total bits to be a multiple of 8
        int totalBytes = (totalBits + Byte.SIZE - 1) / Byte.SIZE;                             // Calculate total bytes
        byte[] bytes = new byte[totalBytes];
        for (int i = 0; i < length; i++) {
            if ((encodedStringChunk.charAt(i)) == '1') {
                bytes[i / Byte.SIZE] = (byte) (bytes[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));      // 0x80 = 10000000
            }
        }
        return bytes;
    }

    private long getChunkSize() {
        if (n <= 10) {
            return 102400L * n;
        } else if (n <= 20) {
            return 51200L * n;
        } else if (n <= 50) {
            return 25600L * n;
        } else if (n <= 100) {
            return 12800L * n;
        } else if (n <= 200) {
            return 6400L * n;
        } else if (n <= 300) {
            return 3200L * n;
        } else if (n <= 600) {
            return 1600L * n;
        } else if (n <= 1000) {
            return 800L * n;
        } else if (n <= 2000) {
            return 400L * n;
        } else if (n <= 5000) {
            return 200L * n;
        } else if (n <= 10000) {
            return 100L * n;
        } else {
            return  n;
        }
    }
}