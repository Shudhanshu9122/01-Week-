import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DNSCache {
    class DNSEntry {
        String domain;
        String ipAddress;
        long timestamp;
        long expiryTime;

        public DNSEntry(String domain, String ipAddress, long ttlMillis) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.timestamp = System.currentTimeMillis();
            this.expiryTime = this.timestamp + ttlMillis;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private Map<String, DNSEntry> cache;
    private AtomicInteger hits;
    private AtomicInteger misses;
    private AtomicInteger totalLookupTime;

    public DNSCache() {
        cache = new ConcurrentHashMap<>();
        hits = new AtomicInteger(0);
        misses = new AtomicInteger(0);
        totalLookupTime = new AtomicInteger(0);

        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    cache.values().removeIf(DNSEntry::isExpired);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleaner.setDaemon(true);
        cleaner.start();
    }

    public String resolve(String domain) {
        long startTime = System.currentTimeMillis();
        
        DNSEntry entry = cache.get(domain);
        String result;
        
        if (entry != null && !entry.isExpired()) {
            hits.incrementAndGet();
            long duration = System.currentTimeMillis() - startTime;
            result = "Cache HIT -> " + entry.ipAddress;
            totalLookupTime.addAndGet((int)duration);
        } else {
            misses.incrementAndGet();
            String ip = fetchFromUpstream(domain);
            cache.put(domain, new DNSEntry(domain, ip, 300 * 1000));
            long duration = System.currentTimeMillis() - startTime;
            result = "Cache MISS -> Query upstream -> " + ip + " (TTL: 300s)";
            totalLookupTime.addAndGet((int)duration);
        }
        
        return result;
    }

    private String fetchFromUpstream(String domain) {
        return "172.217.14.206";
    }

    public String getCacheStats() {
        int totalRequests = hits.get() + misses.get();
        if (totalRequests == 0) return "No requests yet.";
        
        double hitRate = (hits.get() * 100.0) / totalRequests;
        double avgLookupTime = (double)totalLookupTime.get() / totalRequests;
        
        return String.format("Hit Rate: %.1f%%, Avg Lookup Time: %.1fms", hitRate, avgLookupTime);
    }
    
    public static void main(String[] args) {
        DNSCache dnsCache = new DNSCache();
        System.out.println("resolve(\"google.com\") -> " + dnsCache.resolve("google.com"));
        System.out.println("resolve(\"google.com\") -> " + dnsCache.resolve("google.com"));
        
        dnsCache.cache.get("google.com").expiryTime = System.currentTimeMillis() - 1000;
        System.out.println("... after 301 seconds ...");
        System.out.println("resolve(\"google.com\") -> " + dnsCache.resolve("google.com"));
        
        System.out.println("getCacheStats() -> " + dnsCache.getCacheStats());
    }
}
