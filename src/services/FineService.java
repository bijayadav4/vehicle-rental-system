package services;

import models.Bike;
import models.Booking;
import models.Borrower;
import models.Car;
import models.Vehicle;
import utils.InputHelper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineService {

    private RentalService rentalService;
    private VehicleService vehicleService;

    public FineService(RentalService rentalService, VehicleService vehicleService) {
        this.rentalService  = rentalService;
        this.vehicleService = vehicleService;
    }

    // ─────────────────────────────────────────
    //  MAIN RETURN MENU
    // ─────────────────────────────────────────
    public void returnMenu(Borrower borrower) {
        System.out.println("\n--- Active Rentals ---");
        rentalService.viewMyActiveRentals(borrower);

        if (borrower.getRentedCar() == null && borrower.getRentedBike() == null) {
            System.out.println("  ❌ You have no active rentals.");
            return;
        }

        System.out.println("\n  Select vehicle to act on:");
        System.out.println("  1. Car");
        System.out.println("  2. Bike");
        System.out.println("  0. Cancel");
        int slot = InputHelper.getInt("  Choose: ");

        Vehicle vehicle = switch (slot) {
            case 1 -> borrower.getRentedCar();
            case 2 -> borrower.getRentedBike();
            default -> null;
        };

        if (vehicle == null) {
            System.out.println("  ❌ No vehicle in that slot or cancelled.");
            return;
        }

        Booking booking = rentalService.findActiveBooking(borrower, vehicle);
        if (booking == null) {
            System.out.println("  ❌ No active booking found.");
            return;
        }

        System.out.println("\n  Action:");
        System.out.println("  1. Return Vehicle");
        System.out.println("  2. Extend Tenure");
        System.out.println("  3. Exchange Vehicle");
        System.out.println("  4. Mark as Lost");
        System.out.println("  0. Cancel");
        int action = InputHelper.getInt("  Choose: ");

        switch (action) {
            case 1 -> returnVehicle(borrower, booking, vehicle, slot);
            case 2 -> extendTenure(borrower, booking, vehicle);
            case 3 -> exchangeVehicle(borrower, booking, vehicle, slot);
            case 4 -> markAsLost(borrower, booking, vehicle, slot);
            case 0 -> System.out.println("  Cancelled.");
            default -> System.out.println("  ❌ Invalid action.");
        }
    }

    // ─────────────────────────────────────────
    //  1. RETURN VEHICLE
    // ─────────────────────────────────────────
    private void returnVehicle(Borrower borrower, Booking booking,
                               Vehicle vehicle, int slot) {
        System.out.println("\n--- Return: " + vehicle.getName() + " ---");

        // KMs driven today
        int kms = InputHelper.getInt("  KMs driven today: ");
        vehicle.addKms(kms);

        // Base rent calculation
        LocalDate today     = LocalDate.now();
        long days           = ChronoUnit.DAYS.between(booking.getRentDate(), today);
        if (days < 1) days  = 1; // minimum 1 day charge
        double baseRent     = days * vehicle.getRentalPricePerDay();
        double totalCharge  = baseRent;

        System.out.println("\n  --- Billing Summary ---");
        System.out.printf("  Days rented    : %d day(s)%n", days);
        System.out.printf("  Base rent      : Rs.%.0f%n", baseRent);

        // Extra km charge — more than 500 km/day
        double kmCharge = 0;
        if (kms > 500) {
            kmCharge    = baseRent * 0.15;
            totalCharge += kmCharge;
            System.out.printf("  Extra km charge: Rs.%.0f (15%% — exceeded 500 km)%n",
                    kmCharge);
        }

        // Damage fine — only for Cars
        double damageFine = 0;
        if (vehicle instanceof Car) {
            System.out.println("\n  Any damage? (none / LOW / MEDIUM / HIGH):");
            String damage = InputHelper.getString("  Damage level: ").toUpperCase();
            damageFine = switch (damage) {
                case "LOW"    -> baseRent * 0.20;
                case "MEDIUM" -> baseRent * 0.50;
                case "HIGH"   -> baseRent * 0.75;
                default       -> 0;
            };
            if (damageFine > 0) {
                totalCharge += damageFine;
                System.out.printf("  Damage fine    : Rs.%.0f (%s)%n",
                        damageFine, damage);
            }
        }

        System.out.printf("  ─────────────────────────────%n");
        System.out.printf("  TOTAL CHARGE   : Rs.%.0f%n", totalCharge);
        System.out.printf("  Your balance   : Rs.%.0f%n",
                borrower.getSecurityBalance());

        // Payment method
        double remaining = collectPayment(borrower, totalCharge);

        // Finalise booking
        booking.setReturnDate(today);
        booking.setTotalRent(totalCharge);
        booking.markReturned();

        // Restore vehicle
        vehicle.setAvailableCount(vehicle.getAvailableCount() + 1);
        vehicle.setRented(false);

        // Clear borrower slot
        clearSlot(borrower, slot);
        borrower.resetExtension();

        System.out.printf("%n  ✅ Vehicle returned successfully.%n");
        System.out.printf("  Remaining security balance: Rs.%.0f%n", remaining);

        // Service warning after adding kms
        if (vehicle.needsService()) {
            System.out.println("  ⚠️  This vehicle is now due for service.");
        }
    }

    // ─────────────────────────────────────────
    //  2. EXTEND TENURE
    // ─────────────────────────────────────────
    private void extendTenure(Borrower borrower, Booking booking, Vehicle vehicle) {
        System.out.println("\n--- Extend Tenure: " + vehicle.getName() + " ---");

        // Rule: maximum 2 consecutive extensions
        if (borrower.getExtensionCount() >= 2) {
            System.out.println("  ❌ Maximum 2 extensions already used.");
            System.out.println("     You must return or exchange the vehicle.");
            return;
        }

        // Extension must be a consecutive day
        LocalDate nextDay = booking.getReturnDate().plusDays(1);
        System.out.println("  Current return date : " + booking.getReturnDate());
        System.out.println("  Extension will be to: " + nextDay);

        String confirm = InputHelper.getString("  Confirm extension? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("  Extension cancelled.");
            return;
        }

        booking.setReturnDate(nextDay);
        double extraRent = vehicle.getRentalPricePerDay();
        booking.setTotalRent(booking.getTotalRent() + extraRent);
        borrower.incrementExtension();

        System.out.printf("  ✅ Extended to %s%n", nextDay);
        System.out.printf("  Extra rent Rs.%.0f added to booking.%n", extraRent);
        System.out.printf("  Extensions used: %d / 2%n",
                borrower.getExtensionCount());
    }

    // ─────────────────────────────────────────
    //  3. EXCHANGE VEHICLE
    // ─────────────────────────────────────────
    private void exchangeVehicle(Borrower borrower, Booking booking,
                                 Vehicle oldVehicle, int slot) {
        System.out.println("\n--- Exchange: " + oldVehicle.getName() + " ---");

        // Show catalogue of same type
        System.out.println("  Available replacements:");
        boolean anyFound = false;
        for (Vehicle v : vehicleService.getAvailableForRent()) {
            boolean sameType = (oldVehicle instanceof Car  && v instanceof Car)
                            || (oldVehicle instanceof Bike && v instanceof Bike);
            if (sameType && v.getId() != oldVehicle.getId()) {
                System.out.println("    " + v);
                anyFound = true;
            }
        }

        if (!anyFound) {
            System.out.println("  ❌ No replacement vehicle of the same type available.");
            return;
        }

        int newId     = InputHelper.getInt("  Enter ID of new vehicle: ");
        Vehicle newV  = vehicleService.findById(newId);

        if (newV == null || newV.getId() == oldVehicle.getId()) {
            System.out.println("  ❌ Invalid selection.");
            return;
        }

        if (newV.needsService() || newV.getAvailableCount() <= 0) {
            System.out.println("  ❌ Selected vehicle is not available.");
            return;
        }

        // Collect any dues on old vehicle before swapping
        int kms = InputHelper.getInt("  KMs driven on old vehicle today: ");
        oldVehicle.addKms(kms);

        double daysUsed   = ChronoUnit.DAYS.between(
                booking.getRentDate(), LocalDate.now());
        if (daysUsed < 1) daysUsed = 1;
        double partialRent = daysUsed * oldVehicle.getRentalPricePerDay();

        System.out.printf("  Partial charge for old vehicle: Rs.%.0f%n", partialRent);
        collectPayment(borrower, partialRent);

        // Restore old vehicle
        oldVehicle.setAvailableCount(oldVehicle.getAvailableCount() + 1);
        oldVehicle.setRented(false);
        booking.markReturned();

        // Create new booking for new vehicle
        Booking newBooking = new Booking(borrower, newV, LocalDate.now());
        newBooking.setTotalRent(newV.getRentalPricePerDay());
        rentalService.getAllBookings().add(newBooking);

        // Assign new vehicle to borrower
        newV.setAvailableCount(newV.getAvailableCount() - 1);
        newV.setRented(true);
        borrower.resetExtension();

        if (slot == 1) borrower.setRentedCar(newV);
        else           borrower.setRentedBike(newV);

        System.out.println("  ✅ Exchanged to: " + newV.getName());
        System.out.println("     New Booking ID #" + newBooking.getBookingId());
    }

    // ─────────────────────────────────────────
    //  4. MARK AS LOST
    // ─────────────────────────────────────────
    private void markAsLost(Borrower borrower, Booking booking,
                            Vehicle vehicle, int slot) {
        System.out.println("\n--- Mark as Lost: " + vehicle.getName() + " ---");
        System.out.println("  ⚠️  Full security deposit will be forfeited.");
        System.out.printf("  Your balance: Rs.%.0f%n", borrower.getSecurityBalance());

        String confirm = InputHelper.getString(
                "  Are you sure you want to report this as lost? (yes/no): ");
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("  Cancelled.");
            return;
        }

        // Forfeit entire security balance as penalty
        double forfeited = borrower.getSecurityBalance();
        borrower.deductBalance(forfeited);

        // Mark booking as returned (closed)
        booking.setReturnDate(LocalDate.now());
        booking.setTotalRent(forfeited);
        booking.markReturned();

        // Remove vehicle from inventory permanently
        vehicleService.getAllVehicles().remove(vehicle);

        // Clear borrower slot
        clearSlot(borrower, slot);
        borrower.resetExtension();

        System.out.printf("  ✅ Vehicle marked as lost.%n");
        System.out.printf("  Rs.%.0f forfeited from security deposit.%n", forfeited);
        System.out.printf("  Remaining balance: Rs.%.0f%n",
                borrower.getSecurityBalance());
    }

    // ─────────────────────────────────────────
    //  PAYMENT COLLECTION (cash or deposit)
    // ─────────────────────────────────────────
    private double collectPayment(Borrower borrower, double amount) {
        System.out.println("\n  Pay via:");
        System.out.println("  1. Cash");
        System.out.println("  2. Deduct from Security Deposit");
        int method = InputHelper.getInt("  Choose: ");

        if (method == 1) {
            System.out.printf("  ✅ Rs.%.0f collected as cash. Deposit unchanged.%n",
                    amount);
        } else {
            if (borrower.getSecurityBalance() < amount) {
                double shortfall = amount - borrower.getSecurityBalance();
                System.out.printf("  ⚠️  Deposit insufficient. " +
                        "Rs.%.0f must be paid in cash.%n", shortfall);
                borrower.deductBalance(borrower.getSecurityBalance());
            } else {
                borrower.deductBalance(amount);
                System.out.printf("  ✅ Rs.%.0f deducted from security deposit.%n",
                        amount);
            }
        }

        return borrower.getSecurityBalance();
    }

    // ─────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────
    private void clearSlot(Borrower borrower, int slot) {
        if (slot == 1) borrower.setRentedCar(null);
        else           borrower.setRentedBike(null);
    }
}