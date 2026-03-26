package services;

import models.Bike;
import models.Booking;
import models.Borrower;
import models.Car;
import models.User;
import models.Vehicle;
import utils.InputHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReportService {

    private VehicleService vehicleService;
    private RentalService  rentalService;
    private AuthService    authService;

    public ReportService(VehicleService vehicleService,
                         RentalService  rentalService,
                         AuthService    authService) {
        this.vehicleService = vehicleService;
        this.rentalService  = rentalService;
        this.authService    = authService;
    }

    // ═════════════════════════════════════════
    //  ADMIN REPORTS MENU
    // ═════════════════════════════════════════
    public void adminReportsMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n====== ADMIN REPORTS ======");
            System.out.println("  1. Vehicles Due for Service");
            System.out.println("  2. All Vehicles Sorted by Rental Price");
            System.out.println("  3. Search & Filter Vehicles");
            System.out.println("  4. Rented vs Never-Rented Vehicles");
            System.out.println("  0. Back");
            System.out.println("===========================");

            int choice = InputHelper.getInt("  Choose: ");
            switch (choice) {
                case 1 -> reportServiceDue();
                case 2 -> reportSortedByPrice();
                case 3 -> reportSearchAndFilter();
                case 4 -> reportRentedVsNeverRented();
                case 0 -> running = false;
                default -> System.out.println("  ❌ Invalid choice.");
            }
        }
    }

    // ─────────────────────────────────────────
    //  REPORT 1 — Vehicles due for service
    // ─────────────────────────────────────────
    private void reportServiceDue() {
        System.out.println("\n--- Report: Vehicles Due for Service ---");

        List<Vehicle> due = new ArrayList<>();
        for (Vehicle v : vehicleService.getAllVehicles()) {
            if (v.needsService()) due.add(v);
        }

        if (due.isEmpty()) {
            System.out.println("  ✅ All vehicles are within service limits.");
            return;
        }

        System.out.printf("  %-4s %-6s %-18s %-14s %-10s %-12s%n",
                "ID", "Type", "Name", "Plate", "KMs Run", "Limit");
        System.out.println("  " + "-".repeat(68));

        for (Vehicle v : due) {
            String type  = (v instanceof Car) ? "CAR" : "BIKE";
            int    limit = v.getServiceIntervalKms();
            System.out.printf("  %-4d %-6s %-18s %-14s %-10d %-12d%n",
                    v.getId(), type, v.getName(),
                    v.getNumberPlate(), v.getTotalKmsRun(), limit);
        }

        System.out.printf("%n  Total vehicles due: %d%n", due.size());
    }

    // ─────────────────────────────────────────
    //  REPORT 2 — All vehicles sorted by price
    // ─────────────────────────────────────────
    private void reportSortedByPrice() {
        System.out.println("\n--- Report: Vehicles by Rental Price ---");
        System.out.println("  Order: 1. Low to High   2. High to Low");
        int order = InputHelper.getInt("  Choose: ");

        List<Vehicle> sorted = new ArrayList<>(vehicleService.getAllVehicles());
        if (order == 2) {
            sorted.sort(Comparator.comparingDouble(
                    Vehicle::getRentalPricePerDay).reversed());
        } else {
            sorted.sort(Comparator.comparingDouble(
                    Vehicle::getRentalPricePerDay));
        }

        System.out.printf("%n  %-4s %-6s %-18s %-14s %-12s %-10s %-8s%n",
                "ID", "Type", "Name", "Plate", "Price/Day", "Deposit", "Status");
        System.out.println("  " + "-".repeat(76));

        for (Vehicle v : sorted) {
            String type   = (v instanceof Car)  ? "CAR"    : "BIKE";
            String status = v.isRented()         ? "Rented" : "Free";
            String svc    = v.needsService()     ? " ⚠ Svc" : "";
            System.out.printf("  %-4d %-6s %-18s %-14s %-12.0f %-10.0f %-8s%n",
                    v.getId(), type, v.getName(), v.getNumberPlate(),
                    v.getRentalPricePerDay(), v.getSecurityDeposit(),
                    status + svc);
        }
    }

    // ─────────────────────────────────────────
    //  REPORT 3 — Search & filter vehicles
    // ─────────────────────────────────────────
    private void reportSearchAndFilter() {
        System.out.println("\n--- Report: Search & Filter Vehicles ---");

        String keyword = InputHelper.getString(
                "  Search by name (press Enter to skip): ").toLowerCase();

        System.out.println("  Filter by type: 1. All   2. Cars only   3. Bikes only");
        int filter = InputHelper.getInt("  Choose: ");

        List<Vehicle> result = new ArrayList<>();

        for (Vehicle v : vehicleService.getAllVehicles()) {

            // Apply name filter
            boolean nameMatch = keyword.isEmpty()
                    || v.getName().toLowerCase().contains(keyword);

            // Apply type filter
            boolean typeMatch = switch (filter) {
                case 2  -> v instanceof Car;
                case 3  -> v instanceof Bike;
                default -> true;
            };

            if (nameMatch && typeMatch) result.add(v);
        }

        if (result.isEmpty()) {
            System.out.println("  ❌ No vehicles match the criteria.");
            return;
        }

        System.out.printf("%n  %-4s %-6s %-18s %-14s %-12s %-10s%n",
                "ID", "Type", "Name", "Plate", "Price/Day", "Available");
        System.out.println("  " + "-".repeat(68));

        for (Vehicle v : result) {
            String type = (v instanceof Car) ? "CAR" : "BIKE";
            System.out.printf("  %-4d %-6s %-18s %-14s %-12.0f %-10d%n",
                    v.getId(), type, v.getName(), v.getNumberPlate(),
                    v.getRentalPricePerDay(), v.getAvailableCount());
        }

        System.out.printf("%n  Results found: %d%n", result.size());
    }

    // ─────────────────────────────────────────
    //  REPORT 4 — Rented vs never rented
    // ─────────────────────────────────────────
    private void reportRentedVsNeverRented() {
        System.out.println("\n--- Report: Rented vs Never-Rented ---");

        List<Vehicle> currentlyRented = new ArrayList<>();
        List<Vehicle> neverRented     = new ArrayList<>();

        // Collect all vehicle IDs that appear in any booking
        List<Integer> bookedIds = new ArrayList<>();
        for (Booking b : rentalService.getAllBookings()) {
            int vid = b.getVehicle().getId();
            if (!bookedIds.contains(vid)) bookedIds.add(vid);
        }

        for (Vehicle v : vehicleService.getAllVehicles()) {
            if (v.isRented()) {
                currentlyRented.add(v);
            } else if (!bookedIds.contains(v.getId())) {
                neverRented.add(v);
            }
        }

        // Currently rented out
        System.out.println("\n  [ Currently Rented Out ]");
        if (currentlyRented.isEmpty()) {
            System.out.println("  None.");
        } else {
            for (Vehicle v : currentlyRented) {
                String type = (v instanceof Car) ? "CAR" : "BIKE";
                // Find borrower name from active booking
                String borrowerEmail = "Unknown";
                for (Booking b : rentalService.getAllBookings()) {
                    if (b.getVehicle().getId() == v.getId() && !b.isReturned()) {
                        borrowerEmail = b.getBorrower().getEmail();
                        break;
                    }
                }
                System.out.printf("  [%s] %-18s | Plate: %-14s | Rented by: %s%n",
                        type, v.getName(), v.getNumberPlate(), borrowerEmail);
            }
        }

        // Never rented at all
        System.out.println("\n  [ Never Been Rented ]");
        if (neverRented.isEmpty()) {
            System.out.println("  All vehicles have been rented at least once.");
        } else {
            for (Vehicle v : neverRented) {
                String type = (v instanceof Car) ? "CAR" : "BIKE";
                System.out.printf("  [%s] %-18s | Plate: %-14s | Price: Rs.%.0f/day%n",
                        type, v.getName(), v.getNumberPlate(),
                        v.getRentalPricePerDay());
            }
        }

        System.out.printf("%n  Summary — Currently rented: %d | Never rented: %d%n",
                currentlyRented.size(), neverRented.size());
    }

    // ═════════════════════════════════════════
    //  BORROWER REPORT — Full rental history
    // ═════════════════════════════════════════
    public void borrowerHistory(Borrower borrower) {
        System.out.println("\n====== MY RENTAL HISTORY ======");

        List<Booking> history = new ArrayList<>();
        for (Booking b : rentalService.getAllBookings()) {
            if (b.getBorrower().getEmail().equals(borrower.getEmail())) {
                history.add(b);
            }
        }

        if (history.isEmpty()) {
            System.out.println("  No rental history found.");
            return;
        }

        // Summary counts
        long active   = history.stream().filter(b -> !b.isReturned()).count();
        long returned = history.stream().filter(Booking::isReturned).count();
        double totalSpent = history.stream()
                .filter(Booking::isReturned)
                .mapToDouble(Booking::getTotalRent)
                .sum();

        System.out.printf("  Total bookings : %d%n", history.size());
        System.out.printf("  Active         : %d%n", active);
        System.out.printf("  Returned       : %d%n", returned);
        System.out.printf("  Total spent    : Rs.%.0f%n", totalSpent);
        System.out.printf("  Security balance: Rs.%.0f%n",
                borrower.getSecurityBalance());

        System.out.println("\n  --- Booking Details ---");
        System.out.printf("  %-6s %-18s %-12s %-12s %-10s %-10s%n",
                "Bk#", "Vehicle", "Rent Date", "Return Date", "Amount", "Status");
        System.out.println("  " + "-".repeat(72));

        for (Booking b : history) {
            String status = b.isReturned() ? "Returned" : "Active";
            System.out.printf("  %-6d %-18s %-12s %-12s %-10.0f %-10s%n",
                    b.getBookingId(),
                    b.getVehicle().getName(),
                    b.getRentDate().toString(),
                    b.getReturnDate().toString(),
                    b.getTotalRent(),
                    status);
        }
    }
}