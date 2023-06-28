
import java.util.*;
import java.util.regex.*;


public class LempelZiv {
    /**
     * Take uncompressed input as a text string, compress it, and return it as a
     * text string.
     *
     * e.g (For the text "ABABABA,,,..."), the compressed text is: [0|0|A][0|0|B][2|2|A][4|2|,][1|1|,][0|0|.][1|1|.]
     */
    public static String compress(String input) {
        StringBuilder compressed = new StringBuilder();
        int cursor = 1;
        int windowSize = 100;
        int lookaheadLimit = 8; // Lookahead limit of 8 characters

        compressed.append("[0|0|" + input.charAt(0) + "]"); // First character is always of this format

        while (cursor < input.length()) {
            int length = 0;     //the length of the match
            int prevMatch = 0; //the index of the previous match
            int match;  //the index of the current match

            // Set the lookahead limit based on the remaining characters or the specified limit
            int limit = Math.min(lookaheadLimit, input.length() - cursor);

            while (true) {
                int endOfWindow = cursor - 1;   //the window always ends behind the cursor
                int startOfWindow = Math.max(0, cursor - windowSize);   //the start of the window is either 0 or the cursor - window size
                String previousSubstring = input.substring(startOfWindow, endOfWindow + 1); //the previous substring is the substring of the window
                String currentSubstring = input.substring(cursor, cursor + length + 1); //the current substring is the substring of the window + the length, we keep increasing the length until we find a match
                match = previousSubstring.indexOf(currentSubstring); //find the index of the current substring in the previous substring

                if (match != -1 && (cursor + length) <= input.length() - limit) { //if we find a match and the cursor + length is within the lookahead limit
                    prevMatch = match;  //set the previous match to the current match
                    length++;   //increase the length keep looking for a matches until we reach the lookahead limit
                } else {
                    //the first value of the tuple is 0 if we don't find a match, otherwise it's the calculation of the offset.
                    compressed.append("[" + (length == 0 ? 0 : cursor - (startOfWindow + prevMatch)) + "|" + length + "|" + input.charAt(cursor + length) + "]");
                    cursor = cursor + length + 1;   //move the cursor to the next character
                    break;
                }
            }
        }
        return compressed.toString();
    }




    /**
     * Take compressed input as a text string, decompress it, and return it as a
     * text string.
     */
    public static String decompress(String compressed) {
        StringBuilder decompressed = new StringBuilder();
        int cursor = 0;
        while (cursor < compressed.length()) {
            char currentChar = compressed.charAt(cursor);   //get the current character at the cursor
            if (currentChar == '[') {
                int closingBracketIndex = compressed.indexOf(']', cursor + 1); //find the closing bracket
                String tuple = compressed.substring(cursor + 1, closingBracketIndex);   //get the tuple using the closing bracket index
                String[] parts = tuple.split("\\|");    //split the tuple into the offset, length and character
                int offset = Integer.parseInt(parts[0]);    //parse the offset
                int length = Integer.parseInt(parts[1]);    //parse the length
                char ch = parts[2].charAt(0);   //get the character

                if (offset != 0 || length != 0) {   //if the offset and length are not 0, we need to decompress
                    int startIndex = decompressed.length() - offset;    //get the start index of the decompressed string
                    for (int j = 0; j < length; j++) {  //loop through the length of the match substring
                        char previousChar = decompressed.charAt(startIndex + j);    //get the previous character
                        decompressed.append(previousChar);  //append the previous character to the decompressed string
                    }
                }
                decompressed.append(ch);    //append the character to the decompressed string
                cursor = closingBracketIndex + 1;   //move the cursor to the next character
            }
        }
        return decompressed.toString(); //return the decompressed string

    }


    /**
     * The getInformation method is here for your convenience, you don't need to
     * fill it in if you don't want to. It is called on every run and its return
     * value is displayed on-screen. You can use this to print out any relevant
     * information from your compression.
     */
    public String getInformation() {
        return "";
    }
}
