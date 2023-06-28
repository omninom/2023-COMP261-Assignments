/**
 * A new instance of HuffmanCoding is created for every run. The constructor is
 * passed the full text to be encoded or decoded, so this is a good place to
 * construct the tree. You should store this tree in a field and then use it in
 * the encode and decode methods.
 */

import java.util.*;
public class HuffmanCoding {

	private Node root;
	/**
	 * This would be a good place to compute and store the tree.
	 */
	public HuffmanCoding(String text) {
		// Calculate character frequencies
		Map<Character, Integer> frequencyMap = new HashMap<>();
		for (char c : text.toCharArray()) {
			frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
		}

		// Create initial priority queue of nodes
		PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt((Node n) -> n.frequency)
				.thenComparingInt(n -> Character.compare(n.character, n.character)));

		for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
			pq.offer(new Node(entry.getValue(), entry.getKey(), null, null));
		}

		// Build the Huffman tree by merging nodes
		while (pq.size() > 1) {
			Node left = pq.poll();
			Node right = pq.poll();
			Node parent = new Node(left.frequency + right.frequency, '\0', left, right);
			pq.offer(parent);
		}

		// Return the root node of the Huffman tree
		root = pq.poll();
	}


	/**
	 * Take an input string, text, and encode it with the stored tree. Should
	 * return the encoded text as a binary string, that is, a string containing
	 * only 1 and 0.
	 */
	public String encode(String text) {
		Map<Character, String> encodingMap = new HashMap<>();
		buildEncodingMap(root, "", encodingMap);

		StringBuilder encodedText = new StringBuilder();
		for (char c : text.toCharArray()) {
			encodedText.append(encodingMap.get(c));
		}
		//print out the encoding map
		for (Map.Entry<Character, String> entry : encodingMap.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		return encodedText.toString();
	}

	/**
	 * Build the encoding map by traversing the tree and storing the path to each
	 * leaf node.
	 */
	private void buildEncodingMap(Node node, String s, Map<Character, String> encodingMap) {
		if (node.left == null && node.right == null) {
			encodingMap.put(node.character, s);    //if it is a leaf node, put the character and its encoding into the map
			return;
		}
		//if it is not a leaf node, recursively call the method on its left and right child
		buildEncodingMap(node.left, s + "0", encodingMap);
		buildEncodingMap(node.right, s + "1", encodingMap);
	}


	/**
	 * Take encoded input as a binary string, decode it using the stored tree,
	 * and return the decoded text as a text string.
	 */
	public String decode(String encoded) {
		StringBuilder decodedText = new StringBuilder();
		Node current = root;
		//traverse the tree according to the encoded string and append the character to the decoded text when it reaches a leaf node
		for (char bit : encoded.toCharArray()) {
			if (bit == '0') {
				current = current.left;
			} else if (bit == '1') {
				current = current.right;
			}

			if (current.left == null && current.right == null) {
				decodedText.append(current.character);
				current = root;
			}
		}
		return decodedText.toString();
	}


	/**
	 * The getInformation method is here for your convenience, you don't need to
	 * fill it in if you don't wan to. It is called on every run and its return
	 * value is displayed on-screen. You could use this, for example, to print
	 * out the encoding tree.
	 */
	public String getInformation() {
		return "";
	}
}

/**
 * A node in a Huffman tree. Leaves contain a character from the text and its
 * frequency in that text. Nodes contain null characters and the sum of the
 * frequencies of all the leaves in its subtree.
 */
class Node{
	int frequency;
	char character;
	Node left;
	Node right;

	public Node(int frequency, char character, Node left, Node right){
		this.frequency = frequency;
		this.character = character;
		this.left = left;
		this.right = right;
	}
}
