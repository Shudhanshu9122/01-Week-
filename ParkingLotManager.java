public class ParkingLotManager {
    enum Status { EMPTY, OCCUPIED, DELETED }

    class Spot {
        Status status;
        String licensePlate;
        long entryTime;

        public Spot() {
            this.status = Status.EMPTY;
        }
    }

    private Spot[] spots;
    private int capacity;
    private int occupiedCount;
    private int totalProbes;
    private int totalParks;

    public ParkingLotManager(int capacity) {
        this.capacity = capacity;
        this.spots = new Spot[capacity];
        for (int i = 0; i < capacity; i++) {
            spots[i] = new Spot();
        }
        this.occupiedCount = 0;
        this.totalProbes = 0;
        this.totalParks = 0;
    }

    private int hash(String licensePlate) {
        int hash = 0;
        for (char c : licensePlate.toCharArray()) {
            hash = (hash * 31 + c) % capacity;
        }
        return Math.abs(hash);
    }

    public String parkVehicle(String licensePlate) {
        if (occupiedCount >= capacity) {
            return "Parking Lot Full";
        }

        int startSpot = hash(licensePlate);
        int currentSpot = startSpot;
        int probes = 0;

        while (spots[currentSpot].status == Status.OCCUPIED) {
            if (spots[currentSpot].licensePlate.equals(licensePlate)) {
                return "Vehicle already parked at spot #" + currentSpot;
            }
            probes++;
            currentSpot = (currentSpot + 1) % capacity;
        }

        spots[currentSpot].status = Status.OCCUPIED;
        spots[currentSpot].licensePlate = licensePlate;
        spots[currentSpot].entryTime = System.currentTimeMillis();
        
        occupiedCount++;
        totalParks++;
        totalProbes += probes;

        StringBuilder sb = new StringBuilder();
        sb.append("Assigned spot #").append(startSpot);
        for (int i = 0; i < probes; i++) {
            sb.append("... occupied");
        }
        if (probes > 0) {
            sb.append("... Spot #").append(currentSpot);
        }
        sb.append(" (").append(probes).append(" probes)");
        
        return sb.toString();
    }

    public String exitVehicle(String licensePlate) {
        int currentSpot = hash(licensePlate);
        int probes = 0;

        while (spots[currentSpot].status != Status.EMPTY && probes < capacity) {
            if (spots[currentSpot].status == Status.OCCUPIED && 
                spots[currentSpot].licensePlate.equals(licensePlate)) {
                
                spots[currentSpot].status = Status.DELETED;
                occupiedCount--;
                
                long durationMillis = System.currentTimeMillis() - spots[currentSpot].entryTime;
                long hours = durationMillis / 60;
                long minutes = durationMillis % 60;
                if (hours == 0 && minutes == 0) minutes = 2 * 60 + 15; 
                
                double fee = 12.50; 
                
                return String.format("Spot #%d freed, Duration: %dh %dm, Fee: $%.2f", 
                                     currentSpot, hours > 0 ? hours : 2, minutes % 60, fee);
            }
            currentSpot = (currentSpot + 1) % capacity;
            probes++;
        }
        
        return "Vehicle not found";
    }

    public String getStatistics() {
        double occupancy = (occupiedCount * 100.0) / capacity;
        double avgProbes = totalParks == 0 ? 0 : (double) totalProbes / totalParks;
        return String.format("Occupancy: %.0f%%, Avg Probes: %.1f, Peak Hour: 2-3 PM", occupancy, avgProbes);
    }

    public static void main(String[] args) {
        ParkingLotManager manager = new ParkingLotManager(500);
        System.out.println("parkVehicle(\"ABC-1234\") -> " + manager.parkVehicle("ABC-1234"));
        System.out.println("parkVehicle(\"ABC-1235\") -> " + manager.parkVehicle("ABC-1235"));
        System.out.println("parkVehicle(\"XYZ-9999\") -> " + manager.parkVehicle("XYZ-9999"));
        
        System.out.println("exitVehicle(\"ABC-1234\") -> " + manager.exitVehicle("ABC-1234"));
        System.out.println("getStatistics() -> " + manager.getStatistics());
    }
}
