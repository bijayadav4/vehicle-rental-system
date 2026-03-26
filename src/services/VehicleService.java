package services;

import models.Bike;
import models.Car;
import models.Vehicle;
import utils.InputHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VehicleService {

    private List<Vehicle> vehicles = new ArrayList<>();
    private int idCounter = 1;

    public VehicleService() {
        // Pre-loaded sample vehicles
        vehicles.add(new Car(idCounter++,  "Honda City",   "TN01AB1234", 1500, 3, 10000));
        vehicles.add(new Car(idCounter++,  "Maruti Swift",  "TN02CD5678", 1200, 2, 10000));
        vehicles.add(new Bike(idCounter++, "Royal Enfield", "TN03EF9012", 600,  4, 3000));
        vehicles.add(new Bike(idCounter++, "Honda Activa",  "TN04GH3456", 400,  5, 3000));
    }

    // ─────────────────────────────────────────
    //  ADD
    // ─────────────────────────────────────────
    public void addVehicle() {
        System.out.println("\n--- Add Vehicle ---");
        System.out.println("  Type: 1. Car   2. Bike");
        int type = InputHelper.getInt("  Choose: ");

        String name        = InputHelper.getString("  Vehicle Name   : ");
        String plate       = InputHelper.getString("  Number Plate   : ");
        double price       = InputHelper.getDouble("  Rent per Day (Rs.): ");
        int count          = InputHelper.getInt(   "  Available Count: ");
        double deposit     = InputHelper.getDouble("  Security Deposit (Rs.): ");

        Vehicle v;
        if (type == 1) {
            v = new Car(idCounter++, name, plate, price, count, deposit);
        } else if (type == 2) {
            v = new Bike(idCounter++, name, plate, price, count, deposit);
        } else {
            System.out.println("  ❌ Invalid type.");
            return;
        }

        vehicles.add(v);
        System.out.println("  ✅ Vehicle added: " + v.getName());
    }

    // ─────────────────────────────────────────
    //  MODIFY
    // ─────────────────────────────────────────
    public void modifyVehicle() {
        System.out.println("\n--- Modify Vehicle ---");
        listAllVehicles();

        int id = InputHelper.getInt("  Enter Vehicle ID to modify: ");
        Vehicle v = findById(id);
        if (v == null) { System.out.println("  ❌ Vehicle not found."); return; }

        System.out.println("  Editing: " + v.getName());
        System.out.println("  1. Update Available Count");
        System.out.println("  2. Update Security Deposit");
        System.out.println("  3. Update Rental Price");
        int choice = InputHelper.getInt("  Choose: ");

        switch (choice) {
            case 1 -> {
                int count = InputHelper.getInt("  New Available Count: ");
                v.setAvailableCount(count);
                System.out.println("  ✅ Count updated.");
            }
            case 2 -> {
                double dep = InputHelper.getDouble("  New Security Deposit (Rs.): ");
                v.setSecurityDeposit(dep);
                System.out.println("  ✅ Deposit updated.");
            }
            case 3 -> {
                // We need to update price — add setter to Vehicle
                System.out.println("  ⚠️  Price update not yet wired. Coming soon.");
            }
            default -> System.out.println("  ❌ Invalid choice.");
        }
    }

    // ─────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────
    public void deleteVehicle() {
        System.out.println("\n--- Delete Vehicle ---");
        listAllVehicles();

        int id = InputHelper.getInt("  Enter Vehicle ID to delete: ");
        Vehicle v = findById(id);

        if (v == null) { System.out.println("  ❌ Vehicle not found."); return; }
        if (v.isRented()) { System.out.println("  ❌ Cannot delete a rented vehicle."); return; }

        vehicles.remove(v);
        System.out.println("  ✅ Vehicle '" + v.getName() + "' deleted.");
    }

    // ─────────────────────────────────────────
    //  CHANGE SECURITY DEPOSIT (Admin override)
    // ─────────────────────────────────────────
    public void changeSecurityDeposit() {
        System.out.println("\n--- Change Security Deposit ---");
        listAllVehicles();

        int id = InputHelper.getInt("  Enter Vehicle ID: ");
        Vehicle v = findById(id);
        if (v == null) { System.out.println("  ❌ Vehicle not found."); return; }

        double newDeposit = InputHelper.getDouble("  New Deposit Amount (Rs.): ");
        v.setSecurityDeposit(newDeposit);
        System.out.println("  ✅ Security deposit updated to Rs." + newDeposit);
    }

    // ─────────────────────────────────────────
    //  LIST ALL (with sort options)
    // ─────────────────────────────────────────
    public void listAllVehicles() {
        if (vehicles.isEmpty()) {
            System.out.println("  No vehicles in inventory.");
            return;
        }

        System.out.println("\n--- All Vehicles ---");
        System.out.println("  Sort by: 1. Name   2. Available Count   3. No sort");
        int sort = InputHelper.getInt("  Choose: ");

        List<Vehicle> sorted = new ArrayList<>(vehicles);

        switch (sort) {
            case 1 -> sorted.sort(Comparator.comparing(Vehicle::getName));
            case 2 -> sorted.sort(Comparator.comparingInt(Vehicle::getAvailableCount).reversed());
            default -> {} // no sort
        }

        System.out.println();
        System.out.printf("  %-4s %-6s %-18s %-14s %-10s %-8s %-10s %-8s%n",
                "ID", "Type", "Name", "Plate", "Price/Day", "Count", "Deposit", "Service");
        System.out.println("  " + "-".repeat(82));

        for (Vehicle v : sorted) {
            String type    = (v instanceof Car) ? "CAR" : "BIKE";
            String service = v.needsService() ? "⚠ DUE" : "OK";
            System.out.printf("  %-4d %-6s %-18s %-14s %-10.0f %-8d %-10.0f %-8s%n",
                    v.getId(), type, v.getName(), v.getNumberPlate(),
                    v.getRentalPricePerDay(), v.getAvailableCount(),
                    v.getSecurityDeposit(), service);
        }
    }

    // ─────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────
    public void searchVehicle() {
        System.out.println("\n--- Search Vehicle ---");
        System.out.println("  Search by: 1. Name   2. Number Plate");
        int choice = InputHelper.getInt("  Choose: ");
        String keyword = InputHelper.getString("  Enter search term: ").toLowerCase();

        boolean found = false;
        for (Vehicle v : vehicles) {
            boolean match = switch (choice) {
                case 1 -> v.getName().toLowerCase().contains(keyword);
                case 2 -> v.getNumberPlate().toLowerCase().contains(keyword);
                default -> false;
            };
            if (match) {
                System.out.println("  >> " + v);
                found = true;
            }
        }

        if (!found) System.out.println("  ❌ No vehicle found for: " + keyword);
    }

    // ─────────────────────────────────────────
    //  HELPERS (used by other services too)
    // ─────────────────────────────────────────
    public Vehicle findById(int id) {
        for (Vehicle v : vehicles) {
            if (v.getId() == id) return v;
        }
        return null;
    }

    public Vehicle findByName(String name) {
        for (Vehicle v : vehicles) {
            if (v.getName().equalsIgnoreCase(name)) return v;
        }
        return null;
    }

    // Returns only vehicles fit for catalogue (not needing service, count > 0)
    public List<Vehicle> getAvailableForRent() {
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (!v.needsService() && v.getAvailableCount() > 0) {
                result.add(v);
            }
        }
        return result;
    }

    public List<Vehicle> getAllVehicles() { return vehicles; }
}