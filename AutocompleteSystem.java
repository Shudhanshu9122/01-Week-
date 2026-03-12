import java.util.*;

public class AutocompleteSystem {
    class TrieNode {
        Map<Character, TrieNode> children;
        Map<String, Integer> prefixPopularity;

        public TrieNode() {
            children = new HashMap<>();
            prefixPopularity = new HashMap<>();
        }
    }

    private TrieNode root;
    private Map<String, Integer> globalFrequencies;

    public AutocompleteSystem() {
        root = new TrieNode();
        globalFrequencies = new HashMap<>();
    }

    public void updateFrequency(String query) {
        int freq = globalFrequencies.getOrDefault(query, 0) + 1;
        globalFrequencies.put(query, freq);
        
        TrieNode curr = root;
        for (char c : query.toCharArray()) {
            curr.children.putIfAbsent(c, new TrieNode());
            curr = curr.children.get(c);
            curr.prefixPopularity.put(query, freq);
        }
        System.out.println("updateFrequency(\"" + query + "\") -> Frequency: " + (freq - 1) + " -> " + freq + " (trending)");
    }

    public List<String> search(String prefix) {
        TrieNode curr = root;
        for (char c : prefix.toCharArray()) {
            if (!curr.children.containsKey(c)) {
                return new ArrayList<>();
            }
            curr = curr.children.get(c);
        }
        
        PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>(
            (a, b) -> a.getValue().compareTo(b.getValue())
        );
        
        for (Map.Entry<String, Integer> entry : curr.prefixPopularity.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }
        
        List<String> results = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            Map.Entry<String, Integer> entry = minHeap.poll();
            results.add(String.format("\"%s\" (%d searches)", entry.getKey(), entry.getValue()));
        }
        Collections.reverse(results);
        
        System.out.println("search(\"" + prefix + "\") ->");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }
        return results;
    }

    public static void main(String[] args) {
        AutocompleteSystem autocomplete = new AutocompleteSystem();
        autocomplete.updateFrequency("java tutorial");
        autocomplete.updateFrequency("javascript");
        autocomplete.updateFrequency("java download");
        
        // Mock large insert
        autocomplete.globalFrequencies.put("java tutorial", 1234567);
        autocomplete.globalFrequencies.put("javascript", 987654);
        autocomplete.globalFrequencies.put("java download", 456789);
        
        // update prefixes manually for mock
        TrieNode root = autocomplete.root;
        for (String word : Arrays.asList("java tutorial", "javascript", "java download")) {
            TrieNode curr = root;
            int freq = autocomplete.globalFrequencies.get(word);
            for (char c : word.toCharArray()) {
                curr.children.putIfAbsent(c, new TrieNode());
                curr = curr.children.get(c);
                curr.prefixPopularity.put(word, freq);
            }
        }

        autocomplete.search("jav");
        
        autocomplete.updateFrequency("java 21 features");
        autocomplete.updateFrequency("java 21 features");
        autocomplete.updateFrequency("java 21 features");
    }
}
