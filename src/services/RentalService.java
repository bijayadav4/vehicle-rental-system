package services;

import models.Bike;
import models.Booking;
import models.Borrower;
import models.Car;
import models.Vehicle;
import utils.InputHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalService {

    private VehicleService vehicleService;
    private List<Booking> allBookings = new ArrayList<>();

    // Cart holds at most 1 car + 1 bike
    private Vehicle cartCar  = null;
    private Vehicle cartBike = null;

    public RentalService(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // ─────────────────────────────────────────
    //  SHOW CATALOGUE (service-ready vehicles only)
    // ─────────────────────────────────────────
    public void showCatalogue() {
        List<Vehicle> available = vehicleService.getAvailableForRent();

        if (available.isEmpty()) {
            System.out.println("  ❌ No vehicles available for rent right now.");
            return;
        }

        System.out.println("\n========== VEHICLE CATALOGUE ==========");
        System.out.printf("  %-4s %-6s %-18s %-14s %-12s %-10s%n",
                "ID", "Type", "Name", "Plate", "Price/Day", "Deposit");
        System.out.println("  " + "-".repeat(68));

        for (Vehicle v : available) {
            String type = (v instanceof Car) ? "CAR" : "BIKE";
            System.out.printf("  %-4d %-6s %-18s %-14s %-12.0f %-10.0f%n",
                    v.getId(), type, v.getName(),
                    v.getNumberPlate(),
                    v.getRentalPricePerDay(),
                    v.getSecurityDeposit());
        }
        System.out.println("  " + "=".repeat(68));
    }

    // ─────────────────────────────────────────
    //  ADD TO CART
    // ─────────────────────────────────────────
    public void addToCart(Borrower borrower) {
        showCatalogue();

        System.out.println("\n--- Add to Cart ---");
        System.out.println("  Search by: 1. Vehicle ID   2. Vehicle Name");
        int choice = InputHelper.getInt("  Choose: ");

        Vehicle v = null;
        if (choice == 1) {
            int id = InputHelper.getInt("  Enter Vehicle ID: ");
            v = vehicleService.findById(id);
        } else if (choice == 2) {
            String name = InputHelper.getString("  Enter Vehicle Name: ");
            v = vehicleService.findByName(name);
        } else {
            System.out.println("  ❌ Invalid choice.");
            return;
        }

        if (v == null) {
            System.out.println("  ❌ Vehicle not found.");
            return;
        }

        if (v.needsService()) {
            System.out.println("  ❌ This vehicle is due for service and cannot be rented.");
            return;
        }

        if (v.getAvailableCount() <= 0) {
            System.out.println("  ❌ No units available for this vehicle.");
            return;
        }

        // Rule: max one car and one bike at a time
        if (v instanceof Car) {
            if (borrower.getRentedCar() != null || cartCar != null) {
                System.out.println("  ❌ You already have a Car rented or in cart.");
                return;
            }
            // Check minimum security deposit for car
            if (borrower.getSecurityBalance() < 10000) {
                System.out.println("  ❌ Insufficient security deposit.");
                System.out.println("     Minimum Rs.10,000 required for a Car.");
                System.out.println("     Your balance: Rs." + borrower.getSecurityBalance());
                return;
            }
            cartCar = v;
            System.out.println("  ✅ " + v.getName() + " added to cart (CAR slot).");

        } else if (v instanceof Bike) {
            if (borrower.getRentedBike() != null || cartBike != null) {
                System.out.println("  ❌ You already have a Bike rented or in cart.");
                return;
            }
            // Check minimum security deposit for bike
            if (borrower.getSecurityBalance() < 3000) {
                System.out.println("  ❌ Insufficient security deposit.");
                System.out.println("     Minimum Rs.3,000 required for a Bike.");
                System.out.println("     Your balance: Rs." + borrower.getSecurityBalance());
                return;
            }
            cartBike = v;
            System.out.println("  ✅ " + v.getName() + " added to cart (BIKE slot).");
        }
    }

    // ─────────────────────────────────────────
    //  REMOVE FROM CART
    // ─────────────────────────────────────────
    public void removeFromCart() {
        System.out.println("\n--- Remove from Cart ---");
        viewCart();

        if (cartCar == null && cartBike == null) return;

        System.out.println("  Remove: 1. Car   2. Bike   0. Cancel");
        int choice = InputHelper.getInt("  Choose: ");

        switch (choice) {
            case 1 -> {
                if (cartCar == null) { System.out.println("  No car in cart."); return; }
                System.out.println("  ✅ " + cartCar.getName() + " removed from cart.");
                cartCar = null;
            }
            case 2 -> {
                if (cartBike == null) { System.out.println("  No bike in cart."); return; }
                System.out.println("  ✅ " + cartBike.getName() + " removed from cart.");
                cartBike = null;
            }
            case 0 -> {}
            default -> System.out.println("  ❌ Invalid choice.");
        }
    }

    // ─────────────────────────────────────────
    //  VIEW CART
    // ─────────────────────────────────────────
    public void viewCart() {
        System.out.println("\n--- Your Cart ---");
        if (cartCar == null && cartBike == null) {
            System.out.println("  Cart is empty.");
            return;
        }
        if (cartCar  != null)
            System.out.println("  CAR  : " + cartCar.getName()
                    + " | Rs." + cartCar.getRentalPricePerDay() + "/day");
        if (cartBike != null)
            System.out.println("  BIKE : " + cartBike.getName()
                    + " | Rs." + cartBike.getRentalPricePerDay() + "/day");
    }

    // ─────────────────────────────────────────
    //  CHECKOUT
    // ─────────────────────────────────────────
    public void checkout(Borrower borrower) {
        viewCart();

        if (cartCar == null && cartBike == null) {
            System.out.println("  ❌ Your cart is empty. Add a vehicle first.");
            return;
        }

        System.out.println("\n--- Confirm Checkout ---");
        double totalToday = 0;
        if (cartCar  != null) totalToday += cartCar.getRentalPricePerDay();
        if (cartBike != null) totalToday += cartBike.getRentalPricePerDay();

        System.out.printf("  Total rent for today : Rs.%.0f%n", totalToday);
        System.out.printf("  Your security balance: Rs.%.0f%n", borrower.getSecurityBalance());

        String confirm = InputHelper.getString("  Confirm checkout? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("  Checkout cancelled.");
            return;
        }

        LocalDate today = LocalDate.now();

        // Process car booking
        if (cartCar != null) {
            Booking b = new Booking(borrower, cartCar, today);
            b.setTotalRent(cartCar.getRentalPricePerDay());
            allBookings.add(b);
            borrower.setRentedCar(cartCar);
            cartCar.setAvailableCount(cartCar.getAvailableCount() - 1);
            cartCar.setRented(true);
            System.out.println("  ✅ Car booked!  Booking ID #" + b.getBookingId());
            cartCar = null;
        }

        // Process bike booking
        if (cartBike != null) {
            Booking b = new Booking(borrower, cartBike, today);
            b.setTotalRent(cartBike.getRentalPricePerDay());
            allBookings.add(b);
            borrower.setRentedBike(cartBike);
            cartBike.setAvailableCount(cartBike.getAvailableCount() - 1);
            cartBike.setRented(true);
            System.out.println("  ✅ Bike booked! Booking ID #" + b.getBookingId());
            cartBike = null;
        }

        System.out.println("\n  Remember: Return the vehicle by end of today.");
        System.out.println("  Security balance after return: Rs."
                + borrower.getSecurityBalance());
    }

    // ─────────────────────────────────────────
    //  VIEW ACTIVE RENTALS for a borrower
    // ─────────────────────────────────────────
    public void viewMyActiveRentals(Borrower borrower) {
        System.out.println("\n--- Your Active Rentals ---");
        boolean found = false;
        for (Booking b : allBookings) {
            if (b.getBorrower().getEmail().equals(borrower.getEmail())
                    && !b.isReturned()) {
                System.out.println("  " + b);
                found = true;
            }
        }
        if (!found) System.out.println("  No active rentals.");
    }

    // ─────────────────────────────────────────
    //  VIEW RENTAL HISTORY for a borrower
    // ─────────────────────────────────────────
    public void viewMyHistory(Borrower borrower) {
        System.out.println("\n--- Your Rental History ---");
        boolean found = false;
        for (Booking b : allBookings) {
            if (b.getBorrower().getEmail().equals(borrower.getEmail())) {
                String status = b.isReturned() ? "Returned" : "Active";
                System.out.println("  [" + status + "] " + b);
                found = true;
            }
        }
        if (!found) System.out.println("  No rental history found.");
    }

    // ─────────────────────────────────────────
    //  HELPERS used by FineService
    // ─────────────────────────────────────────
    public Booking findActiveBooking(Borrower borrower, Vehicle vehicle) {
        for (Booking b : allBookings) {
            if (b.getBorrower().getEmail().equals(borrower.getEmail())
                    && b.getVehicle().getId() == vehicle.getId()
                    && !b.isReturned()) {
                return b;
            }
        }
        return null;
    }

    public List<Booking> getAllBookings() { return allBookings; }
}