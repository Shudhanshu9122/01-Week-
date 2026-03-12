import java.util.*;

public class PlagiarismDetector {
    private Map<String, Set<String>> ngramDatabase;
    private static final int N = 5;

    public PlagiarismDetector() {
        ngramDatabase = new HashMap<>();
    }

    public void addDocument(String documentId, String content) {
        List<String> ngrams = extractNGrams(content, N);
        for (String ngram : ngrams) {
            ngramDatabase.computeIfAbsent(ngram, k -> new HashSet<>()).add(documentId);
        }
    }

    public void analyzeDocument(String newDocumentId, String content) {
        List<String> ngrams = extractNGrams(content, N);
        System.out.println("-> Extracted " + ngrams.size() + " n-grams");
        
        Map<String, Integer> matchCounts = new HashMap<>();
        
        for (String ngram : ngrams) {
            Set<String> docs = ngramDatabase.get(ngram);
            if (docs != null) {
                for (String docId : docs) {
                    matchCounts.put(docId, matchCounts.getOrDefault(docId, 0) + 1);
                }
            }
        }
        
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String existingDoc = entry.getKey();
            int matches = entry.getValue();
            
            double similarity = (matches * 100.0) / ngrams.size();
            
            System.out.printf("-> Found %d matching n-grams with \"%s\"\n", matches, existingDoc);
            if (similarity > 50.0) {
                System.out.printf("-> Similarity: %.1f%% (PLAGIARISM DETECTED)\n", similarity);
            } else {
                System.out.printf("-> Similarity: %.1f%% (suspicious)\n", similarity);
            }
        }
    }

    private List<String> extractNGrams(String text, int n) {
        List<String> ngrams = new ArrayList<>();
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+");
        
        if (words.length < n) return ngrams;
        
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(words[i + j]).append(j < n - 1 ? "" : " ");
            }
            ngrams.add(sb.toString().trim());
        }
        return ngrams;
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();
        detector.addDocument("essay_089.txt", "The quick brown fox jumps over the lazy dog and runs away fast");
        detector.addDocument("essay_092.txt", "In this essay we will discuss how the quick brown fox jumps over the lazy dog repeatedly");
        
        System.out.println("analyzeDocument(\"essay_123.txt\")");
        detector.analyzeDocument("essay_123.txt", "Let me write about how the quick brown fox jumps over the lazy dog and runs away fast");
    }
}
