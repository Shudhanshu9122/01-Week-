import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {
    class SimpleTokenBucket {
        int maxTokens;
        AtomicInteger currentTokens;
        long lastResetTime;
        long resetIntervalMillis;

        public SimpleTokenBucket(int maxTokens, long resetIntervalMillis) {
            this.maxTokens = maxTokens;
            this.currentTokens = new AtomicInteger(maxTokens);
            this.resetIntervalMillis = resetIntervalMillis;
            this.lastResetTime = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - lastResetTime > resetIntervalMillis) {
                currentTokens.set(maxTokens);
                lastResetTime = now;
            }

            if (currentTokens.get() > 0) {
                currentTokens.decrementAndGet();
                return true;
            }
            return false;
        }

        public int getUsed() {
            return maxTokens - currentTokens.get();
        }
        
        public long getResetTime() {
            return lastResetTime + resetIntervalMillis;
        }
    }

    private Map<String, SimpleTokenBucket> clientBuckets;

    public RateLimiter() {
        clientBuckets = new ConcurrentHashMap<>();
    }

    public String checkRateLimit(String clientId) {
        SimpleTokenBucket bucket = clientBuckets.computeIfAbsent(clientId, 
            k -> new SimpleTokenBucket(1000, 3600 * 1000));
            
        if (bucket.tryConsume()) {
            return "Allowed (" + bucket.currentTokens.get() + " requests remaining)";
        } else {
            long secondsToWait = (bucket.getResetTime() - System.currentTimeMillis()) / 1000;
            return "Denied (0 requests remaining, retry after " + secondsToWait + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        SimpleTokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) return "No data for client";
        return "{used: " + bucket.getUsed() + ", limit: " + bucket.maxTokens + ", reset: " + bucket.getResetTime() + "}";
    }
    
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();
        System.out.println("checkRateLimit(\"abc123\") -> " + limiter.checkRateLimit("abc123"));
        System.out.println("checkRateLimit(\"abc123\") -> " + limiter.checkRateLimit("abc123"));
        
        SimpleTokenBucket bucket = limiter.clientBuckets.get("abc123");
        bucket.currentTokens.set(0);
        
        System.out.println("checkRateLimit(\"abc123\") -> " + limiter.checkRateLimit("abc123"));
        System.out.println("getRateLimitStatus(\"abc123\") -> " + limiter.getRateLimitStatus("abc123"));
    }
}
