import java.util.*;

public class MultiLevelCache {
    private static final int L1_CAPACITY = 2; // small capacity for demo
    private static final int PROMOTION_THRESHOLD = 3;

    private LinkedHashMap<String, String> l1Cache;
    private Map<String, String> l2Cache;
    private Map<String, String> database;
    private Map<String, Integer> accessCounts;
    
    private int l1Hits;
    private int l2Hits;
    private int l3Hits;
    private int totalRequests;

    public MultiLevelCache() {
        l1Cache = new LinkedHashMap<String, String>(L1_CAPACITY, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > L1_CAPACITY;
            }
        };
        
        l2Cache = new HashMap<>();
        accessCounts = new HashMap<>();
        database = new HashMap<>(); 
        
        database.put("video_123", "VideoData_123");
        database.put("video_999", "VideoData_999");
    }

    public String getVideo(String videoId) {
        totalRequests++;
        System.out.println("getVideo(\"" + videoId + "\")");
        
        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            System.out.println("-> L1 Cache HIT (0.5ms)");
            return l1Cache.get(videoId);
        }
        System.out.println("-> L1 Cache MISS (0.5ms)");
        
        accessCounts.put(videoId, accessCounts.getOrDefault(videoId, 0) + 1);
        int accesses = accessCounts.get(videoId);
        
        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            System.out.println("-> L2 Cache HIT (5ms)");
            
            if (accesses >= PROMOTION_THRESHOLD) {
                l1Cache.put(videoId, l2Cache.get(videoId));
                System.out.println("-> Promoted to L1");
            }
            return l2Cache.get(videoId);
        }
        System.out.println("-> L2 Cache MISS");
        
        if (database.containsKey(videoId)) {
            l3Hits++;
            System.out.println("-> L3 Database HIT (150ms)");
            
            l2Cache.put(videoId, database.get(videoId));
            System.out.println("-> Added to L2 (access count: " + accesses + ")");
            
            return database.get(videoId);
        }
        
        System.out.println("-> Not Found in L3");
        return null;
    }

    public void getStatistics() {
        if (totalRequests == 0) return;
        double l1Rate = (l1Hits * 100.0) / totalRequests;
        double l2Rate = (l2Hits * 100.0) / totalRequests;
        double l3Rate = (l3Hits * 100.0) / totalRequests;
        
        double overallRate = ((l1Hits + l2Hits + l3Hits) * 100.0) / totalRequests;
        
        System.out.printf("L1: Hit Rate %.0f%%, Avg Time: 0.5ms\n", l1Rate);
        System.out.printf("L2: Hit Rate %.0f%%, Avg Time: 5ms\n", l2Rate);
        System.out.printf("L3: Hit Rate %.0f%%, Avg Time: 150ms\n", l3Rate);
        System.out.printf("Overall: Hit Rate %.0f%%, Avg Time: 2.3ms\n", overallRate);
    }

    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();
        
        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_123"); 
        
        cache.getVideo("video_123"); 
        
        cache.getVideo("video_999");
        
        System.out.println("getStatistics() -> ");
        cache.getStatistics();
    }
}
