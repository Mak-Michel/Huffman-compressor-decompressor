public class HuffmanNode implements Comparable<HuffmanNode> {
    private final int frequency;
    private HuffmanNode leftNode, rightNode;

    public HuffmanNode(int frequency) {
        this.frequency = frequency;
    }

    public HuffmanNode(HuffmanNode leftNode, HuffmanNode rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.frequency = leftNode.getFrequency() + rightNode.getFrequency();
    }

    public int getFrequency() {
        return frequency;
    }

    public HuffmanNode getLeftNode() {
        return leftNode;
    }

    public HuffmanNode getRightNode() {
        return rightNode;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        return Integer.compare(frequency, node.getFrequency());
    }
}
