import java.util.*;

public class RealTimeAnalyticsDashboard {
    private Map<String, Integer> pageViews;
    private Map<String, Set<String>> uniqueVisitors;
    private Map<String, Integer> trafficSources;

    public RealTimeAnalyticsDashboard() {
        pageViews = new HashMap<>();
        uniqueVisitors = new HashMap<>();
        trafficSources = new HashMap<>();
    }

    public synchronized void processEvent(String url, String userId, String source) {
        pageViews.put(url, pageViews.getOrDefault(url, 0) + 1);
        
        uniqueVisitors.computeIfAbsent(url, k -> new HashSet<>()).add(userId);
        
        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
    }

    public synchronized void getDashboard() {
        System.out.println("Top Pages:");
        
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
            (a, b) -> b.getValue().compareTo(a.getValue())
        );
        pq.addAll(pageViews.entrySet());
        
        int count = 1;
        while (!pq.isEmpty() && count <= 10) {
            Map.Entry<String, Integer> entry = pq.poll();
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, new HashSet<>()).size();
            System.out.printf("%d. %s - %d views (%d unique)\n", count++, url, views, unique);
        }

        System.out.println("\nTraffic Sources:");
        int totalSources = trafficSources.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalSources > 0) {
            List<String> sourceStats = new ArrayList<>();
            for (Map.Entry<String, Integer> source : trafficSources.entrySet()) {
                double percentage = (source.getValue() * 100.0) / totalSources;
                sourceStats.add(String.format("%s: %.0f%%", source.getKey(), percentage));
            }
            System.out.println(String.join(", ", sourceStats));
        } else {
            System.out.println("No traffic yet.");
        }
    }

    public static void main(String[] args) {
        RealTimeAnalyticsDashboard dashboard = new RealTimeAnalyticsDashboard();
        
        dashboard.processEvent("/article/breaking-news", "user_123", "Google");
        dashboard.processEvent("/article/breaking-news", "user_456", "Facebook");
        dashboard.processEvent("/sports/championship", "user_123", "Direct");
        dashboard.processEvent("/sports/championship", "user_789", "Google");
        dashboard.processEvent("/article/breaking-news", "user_123", "Direct");
        
        dashboard.getDashboard();
    }
}
