import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class UsernameAvailabilityChecker {
    private Map<String, Integer> users;
    private Map<String, Integer> attemptFrequency;
    
    private String mostAttemptedUsername;
    private int maxAttempts;

    public UsernameAvailabilityChecker() {
        users = new HashMap<>();
        attemptFrequency = new HashMap<>();
        mostAttemptedUsername = null;
        maxAttempts = 0;
    }

    public boolean checkAvailability(String username) {
        int attempts = attemptFrequency.getOrDefault(username, 0) + 1;
        attemptFrequency.put(username, attempts);
        
        if (attempts > maxAttempts) {
            maxAttempts = attempts;
            mostAttemptedUsername = username;
        }

        return !users.containsKey(username);
    }

    public boolean registerUser(String username, int userId) {
        if (!checkAvailability(username)) {
            return false;
        }
        users.put(username, userId);
        return true;
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        if (!users.containsKey(username)) {
            suggestions.add(username);
            return suggestions;
        }

        int count = 1;
        while (suggestions.size() < 2) {
            String suggestion = username + count;
            if (!users.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
            count++;
        }
        
        if (!username.contains(".")) {
            int mid = username.length() / 2;
            String suggestionWithDot = username.substring(0, mid) + "." + username.substring(mid);
            if (!users.containsKey(suggestionWithDot)) {
                suggestions.add(suggestionWithDot);
            }
        }

        return suggestions;
    }

    public String getMostAttempted() {
        if (mostAttemptedUsername == null) {
            return "No attempts yet";
        }
        return mostAttemptedUsername + " (" + maxAttempts + " attempts)";
    }

    public static void main(String[] args) {
        UsernameAvailabilityChecker checker = new UsernameAvailabilityChecker();
        checker.registerUser("john_doe", 1);
        System.out.println("checkAvailability(\"john_doe\") -> " + checker.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") -> " + checker.checkAvailability("jane_smith"));
        System.out.println("suggestAlternatives(\"john_doe\") -> " + checker.suggestAlternatives("john_doe"));
        System.out.println("getMostAttempted() -> " + checker.getMostAttempted());
    }
}
