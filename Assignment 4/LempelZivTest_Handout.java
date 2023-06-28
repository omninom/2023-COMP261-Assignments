public class LempelZivTest_Handout {
    public static void main(String[] args) {
        // Test cases
        String text1 = "ABABABA,,,...";
        
        // Compress the texts
        String compressedText1 = LempelZiv.compress(text1);
        
        
        // Print results
        System.out.println("Text 1: " + text1);
        System.out.println("Tuples of Compressed Text 1: " + compressedText1);
        
        // Decompress the compressed texts
        String decompressedText1 = LempelZiv.decompress(compressedText1);
        System.out.println("Decompressed Text 1: " + decompressedText1);
        
        // Check if decompressed texts match the original texts
        boolean isDecompressedCorrect1 = decompressedText1.equals(text1);
        
        // Print correctness results
        if (isDecompressedCorrect1) {
            System.out.println("Decompressed Text 1 matches the original Text 1.");
        } else {
            System.out.println("Decompressed Text 1 does not match the original Text 1.");
        }


        String text2 = "AAAAAAAAasdasdqwasdsdfgvasdfgasdasdfAAasdasdqwasdsdfgvasdfgasdasdfAAasdasdqwasdsdfgvasdfgasdasdfAAasdasdqwasdsdfgvasdfgasdasdfAAasdasdqwasdsdfgvasdfgasdasdf";

        // Compress the texts
        String compressedText2 = LempelZiv.compress(text2);


        // Print results
        System.out.println("\nText 2: " + text2);
        System.out.println("Tuples of Compressed Text 2: " + compressedText2);

        // Decompress the compressed texts
        String decompressedText2 = LempelZiv.decompress(compressedText2);
        System.out.println("Decompressed Text 2: " + decompressedText2);

        // Check if decompressed texts match the original texts
        boolean isDecompressedCorrect2 = decompressedText2.equals(text2);

        // Print correctness results
        if (isDecompressedCorrect2) {
            System.out.println("Decompressed Text 2 matches the original Text 2.");
        } else {
            System.out.println("Decompressed Text 2 does not match the original Text 2.");
        }


    }
}