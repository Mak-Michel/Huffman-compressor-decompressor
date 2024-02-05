import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        char operation = args[0].charAt(0);
        String path = args[1];

        if(operation == 'c') {
            int n = Integer.parseInt(args[2]);
            long startTimeC = System.currentTimeMillis();
            HuffmanCompressor huffman = new HuffmanCompressor(path , n);
            huffman.compress();
            long endTimeC = System.currentTimeMillis();
            System.out.println("Time taken for compression: " + (endTimeC - startTimeC) + "ms");
        }
        else  if(operation == 'd') {
            long startTimeD = System.currentTimeMillis();
            HuffmanDecompressor decompressor = new HuffmanDecompressor(path);
            long endTimeD = System.currentTimeMillis();
            System.out.println("Time taken for decompression: " + (endTimeD - startTimeD) + "ms");
        }
        else {
            System.err.println("Usage: java -jar huffman_<id>.jar c absolute_path_to_input_file n");
            System.exit(1);
        }
    }
}
