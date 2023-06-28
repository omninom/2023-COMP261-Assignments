/**
 * A new KMP instance is created for every substring search performed. Both the
 * pattern and the text are passed to the constructor and the search method. You
 * could, for example, use the constructor to create the match table and the
 * search method to perform the search itself.
 */
public class KMP {

	/**
	 * Perform KMP substring search on the given text with the given pattern.
	 * 
	 * This should return the starting index of the first substring match if it
	 * exists, or -1 if it doesn't.
	 */
	public static int search(String pattern, String text) {
		int[] matchTable = buildTable(pattern);
		int k = 0; //start of current match in text
		int i = 0; //position of current character in pattern
		while(k+i < text.length()){
			if(pattern.charAt(i) == text.charAt(k+i)){	//if we have a match
				i++;
				if(i == pattern.length()){	//if we have a complete match
					return k;
				}
			}
			else if(matchTable[i] == -1){	//if we have no match
				k = k + i + 1;	//move the start of the match to the next character
				i = 0;	//reset the position of the current character in the pattern
			}
			else{	//if we have a partial match
				k = k + i - matchTable[i];		//move the start of the match to the next character
				i = matchTable[i];	//reset the position of the current character in the pattern
			}
		}
		return -1; //no match found
	}

	/**
	 * Build the KMP match table for the given pattern.
	 *
	 * This should return an array whose values represent the length of the
	 * longest prefix of the substring ending at that index that is also a
	 * suffix of the substring starting at index 0.
	 */
	public static int[] buildTable(String pattern) {
		//initialize the table
		int[] table = new int[pattern.length()];
		//initialize the first two values
		table[0] = -1;
		if(pattern.length() > 1){	//if the pattern is longer than 1 character
			table[1] = 0;
		}
		int j = 0; //index of the prefix
		int pos = 2; //curent position we are computing in table
		while(pos < pattern.length()){
			if(pattern.charAt(pos-1) == pattern.charAt(j)){
				table[pos] = j+1;
				pos++;
				j++;
			}
			else if(j > 0){		//if we can't match, we go back to the previous prefix
				j = table[j];
			}
			else{				//we have run out of candidates to try
				table[pos] = 0;
				pos++;
			}
		}
		return table;
	}
}
