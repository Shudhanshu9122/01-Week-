import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {
    // HashMap<productId, stockCount>
    private Map<String, AtomicInteger> inventory;
    
    // LinkedHashMap for waiting list (FIFO)
    private Map<String, LinkedHashMap<Integer, Boolean>> waitingLists;

    public FlashSaleInventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingLists = new ConcurrentHashMap<>();
    }

    public void addProduct(String productId, int initialStock) {
        inventory.put(productId, new AtomicInteger(initialStock));
        waitingLists.put(productId, new LinkedHashMap<>());
    }
    
    // Check Stock in O(1) time
    public int checkStock(String productId) {
        AtomicInteger stock = inventory.get(productId);
        return (stock != null) ? stock.get() : 0;
    }

    // Process purchase request in O(1) time (Synchronized operation)
    public synchronized String purchaseItem(String productId, int userId) {
        AtomicInteger stock = inventory.get(productId);
        
        if (stock == null) {
            return "Product not found";
        }
        
        if (stock.get() > 0) {
            int currentStock = stock.decrementAndGet();
            if (currentStock >= 0) {
                return "Success, " + currentStock + " units remaining";
            } else {
                stock.incrementAndGet(); // fallback
            }
        }
        
        // Stock runs out, add to waiting list
        LinkedHashMap<Integer, Boolean> waitList = waitingLists.get(productId);
        waitList.put(userId, true);
        return "Added to waiting list, position #" + waitList.size();
    }
    
    public static void main(String[] args) {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();
        manager.addProduct("IPHONE15_256GB", 100);
        
        System.out.println("checkStock(\"IPHONE15_256GB\") -> " + manager.checkStock("IPHONE15_256GB") + " units available");
        System.out.println("purchaseItem(\"IPHONE15_256GB\", 12345) -> " + manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println("purchaseItem(\"IPHONE15_256GB\", 67890) -> " + manager.purchaseItem("IPHONE15_256GB", 67890));
        
        // Simulate remaining purchases
        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", i);
        }
        
        System.out.println("purchaseItem(\"IPHONE15_256GB\", 99999) -> " + manager.purchaseItem("IPHONE15_256GB", 99999));
    }
}
