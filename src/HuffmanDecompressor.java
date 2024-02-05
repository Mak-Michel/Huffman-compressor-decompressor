
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HuffmanDecompressor {
    private final String compressedFilePath;
    private Map<String, Integer> frequencies = new LinkedHashMap<>();
    private HuffmanNode root;
    private String outputPath;
    private int n;

    public HuffmanDecompressor(String compressedFilePath) throws IOException, ClassNotFoundException {
        this.compressedFilePath = compressedFilePath;

        String[] split;

        if(compressedFilePath.contains("/")) {
            split = compressedFilePath.split("/");
            split[split.length - 1] = "extracted." + split[split.length - 1].substring(0, split[split.length - 1].length() - 3);
            outputPath = String.join("/", split);
        }
        else {
            split = compressedFilePath.split("\\\\");
            split[split.length - 1] = "extracted." + split[split.length - 1].substring(0, split[split.length - 1].length() - 3);
            outputPath = String.join("\\", split);
        }
        readCompressedFile();
    }

    private void readCompressedFile() throws IOException, ClassNotFoundException {
        frequencies = new LinkedHashMap<>();


        FileInputStream fis = new FileInputStream(compressedFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);

        byte[] buffer = new byte[4];
        bis.read(buffer);
        int totalSize = (buffer[0] & 0xFF) << 24 | (buffer[1] & 0xFF) << 16 |
                (buffer[2] & 0xFF) << 8 | (buffer[3] & 0xFF);

        bis.read(buffer);
        n = (buffer[0] & 0xFF) << 24 | (buffer[1] & 0xFF) << 16 |
                (buffer[2] & 0xFF) << 8 | (buffer[3] & 0xFF);

        ObjectInputStream ois = new ObjectInputStream(bis);                                 // bis bot fis 34an pointers btslm b3dha...
        frequencies = (Map<String, Integer>) ois.readObject();

        buildHuffmanTree();

        int len;
        long maxChunkRead = getChunkSize();
        byte[] chunk = new byte[(int) maxChunkRead];
        HuffmanNode currentCode = root;
        FileOutputStream fos = new FileOutputStream(outputPath);
        while (totalSize > 0) {
            len = bis.read(chunk, 0, (int) maxChunkRead);
            if (len <= 0) {
                break;
            }
            String encodedString = byteToBitsString(chunk);

            for (int i = 0; i < encodedString.length() && totalSize > 0; i++) {
                currentCode = (encodedString.charAt(i) == '0') ? currentCode.getLeftNode() : currentCode.getRightNode();
                if (currentCode instanceof HuffmanLeaf leaf) {
                    fos.write(leaf.getValue().getBytes(StandardCharsets.ISO_8859_1));
                    currentCode = root;
                    totalSize -= n;
                }
            }
        }
        fis.close();
        ois.close();
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
        root = PQ.poll();
    }

    private String byteToBitsString(byte[] bytes) {
        StringBuilder bits = new StringBuilder();
        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                bits.append((b & (1 << i)) != 0 ? '1' : '0');
            }
        }
        return bits.toString();
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
