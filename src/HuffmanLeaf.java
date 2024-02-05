public class HuffmanLeaf extends HuffmanNode {
    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    public HuffmanLeaf(int frequency, String value) {
        super(frequency);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
