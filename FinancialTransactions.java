import java.util.*;

public class FinancialTransactions {
    static class Transaction {
        int id;
        int amount;
        String merchant;
        long timestamp;
        String accountId;

        public Transaction(int id, int amount, String merchant, long timestamp, String accountId) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.timestamp = timestamp;
            this.accountId = accountId;
        }
    }

    private List<Transaction> transactions;

    public FinancialTransactions() {
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public List<int[]> findTwoSum(int target) {
        Map<Integer, Integer> complementMap = new HashMap<>();
        List<int[]> result = new ArrayList<>();
        
        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (complementMap.containsKey(complement)) {
                result.add(new int[]{complementMap.get(complement), t.id});
            }
            complementMap.put(t.amount, t.id);
        }
        return result;
    }

    public List<int[]> findTwoSumWithinTimeWindow(int target, long windowMillis) {
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort(Comparator.comparingLong(t -> t.timestamp));
        
        List<int[]> result = new ArrayList<>();
        Map<Integer, Integer> activeWindow = new HashMap<>();
        int left = 0;
        
        for (int right = 0; right < sorted.size(); right++) {
            Transaction current = sorted.get(right);
            
            while (current.timestamp - sorted.get(left).timestamp > windowMillis) {
                if (activeWindow.getOrDefault(sorted.get(left).amount, -1) == sorted.get(left).id) {
                    activeWindow.remove(sorted.get(left).amount);
                }
                left++;
            }
            
            int complement = target - current.amount;
            if (activeWindow.containsKey(complement)) {
                result.add(new int[]{activeWindow.get(complement), current.id});
            }
            activeWindow.put(current.amount, current.id);
        }
        return result;
    }

    public List<String> detectDuplicates() {
        Map<String, List<Transaction>> map = new HashMap<>();
        List<String> duplicates = new ArrayList<>();
        
        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        
        for (Map.Entry<String, List<Transaction>> entry : map.entrySet()) {
            List<Transaction> list = entry.getValue();
            if (list.size() > 1) {
                Set<String> accounts = new HashSet<>();
                for (Transaction t : list) {
                    accounts.add(t.accountId);
                }
                if (accounts.size() > 1) {
                    duplicates.add("{amount:" + list.get(0).amount + ", merchant:\"" + list.get(0).merchant + "\", accounts:" + accounts + "}");
                }
            }
        }
        return duplicates;
    }

    public List<List<Integer>> findKSum(int k, int target) {
        Collections.sort(transactions, Comparator.comparingInt(t -> t.amount));
        return kSumHelper(transactions, target, k, 0);
    }
    
    private List<List<Integer>> kSumHelper(List<Transaction> list, int target, int k, int index) {
        List<List<Integer>> res = new ArrayList<>();
        if (index == list.size()) return res;
        
        int n = list.size();
        if (k == 2) {
            int left = index, right = n - 1;
            while (left < right) {
                int sum = list.get(left).amount + list.get(right).amount;
                if (sum == target) {
                    List<Integer> temp = new ArrayList<>();
                    temp.add(list.get(left).id);
                    temp.add(list.get(right).id);
                    res.add(temp);
                    
                    while (left < right && list.get(left).amount == list.get(left+1).amount) left++;
                    while (left < right && list.get(right).amount == list.get(right-1).amount) right--;
                    left++; right--;
                } else if (sum < target) {
                    left++;
                } else {
                    right--;
                }
            }
        } else {
            for (int i = index; i < n - k + 1; i++) {
                if (i > index && list.get(i).amount == list.get(i-1).amount) continue;
                List<List<Integer>> subRes = kSumHelper(list, target - list.get(i).amount, k - 1, i + 1);
                if (subRes != null) {
                    for (List<Integer> sub : subRes) {
                        sub.add(0, list.get(i).id);
                        res.add(sub);
                    }
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        FinancialTransactions ft = new FinancialTransactions();
        ft.addTransaction(new Transaction(1, 500, "Store A", 1000000, "acc1"));
        ft.addTransaction(new Transaction(2, 300, "Store B", 1000000 + 15 * 60 * 1000, "acc1"));
        ft.addTransaction(new Transaction(3, 200, "Store C", 1000000 + 30 * 60 * 1000, "acc1"));
        ft.addTransaction(new Transaction(4, 500, "Store A", 1000000 + 5 * 60 * 1000, "acc2"));
        
        List<int[]> twoSum = ft.findTwoSum(500);
        System.out.print("findTwoSum(target=500) -> [");
        for (int[] pair : twoSum) System.out.print("(id:" + pair[0] + ", id:" + pair[1] + ") ");
        System.out.println("]");
        
        System.out.println("detectDuplicates() -> " + ft.detectDuplicates());
        
        List<List<Integer>> kSum = ft.findKSum(3, 1000);
        System.out.println("findKSum(k=3, target=1000) -> " + kSum);
    }
}
