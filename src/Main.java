import models.Borrower;
import models.User;
import services.AuthService;
import services.FineService;
import services.RentalService;
import services.ReportService;
import services.VehicleService;
import utils.InputHelper;

public class Main {

    static AuthService    authService    = new AuthService();
    static VehicleService vehicleService = new VehicleService();
    static RentalService  rentalService  = new RentalService(vehicleService);
    static FineService    fineService    = new FineService(rentalService, vehicleService);
    static ReportService  reportService  = new ReportService(
            vehicleService, rentalService, authService);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║     VEHICLE RENTAL SYSTEM        ║");
        System.out.println("╚══════════════════════════════════╝");
        while (true) showStartMenu();
    }

    // ── START MENU ──────────────────────────
    static void showStartMenu() {
        System.out.println("\n---------- MAIN MENU ----------");
        System.out.println("  1. Login");
        System.out.println("  2. Sign Up  (New Borrower)");
        System.out.println("  3. Exit");
        System.out.println("-------------------------------");
        int choice = InputHelper.getInt("  Choose: ");
        switch (choice) {
            case 1 -> {
                User user = authService.login();
                if (user != null) {
                    if (user.getRole().equals("ADMIN")) showAdminMenu(user);
                    else                                showBorrowerMenu(user);
                }
            }
            case 2 -> authService.signup();
            case 3 -> { System.out.println("\n  Goodbye! 👋"); System.exit(0); }
            default -> System.out.println("  ❌ Invalid choice.");
        }
    }

    // ── ADMIN MENU ──────────────────────────
    static void showAdminMenu(User admin) {
        System.out.println("\n✅ Logged in as ADMIN: " + admin.getEmail());
        boolean running = true;
        while (running) {
            System.out.println("\n====== ADMIN MENU ======");
            System.out.println("  1. View All Vehicles");
            System.out.println("  2. Add Vehicle");
            System.out.println("  3. Modify Vehicle");
            System.out.println("  4. Delete Vehicle");
            System.out.println("  5. Search Vehicle");
            System.out.println("  6. Change Security Deposit");
            System.out.println("  7. Reports");
            System.out.println("  0. Logout");
            System.out.println("========================");
            int choice = InputHelper.getInt("  Choose: ");
            switch (choice) {
                case 1 -> vehicleService.listAllVehicles();
                case 2 -> vehicleService.addVehicle();
                case 3 -> vehicleService.modifyVehicle();
                case 4 -> vehicleService.deleteVehicle();
                case 5 -> vehicleService.searchVehicle();
                case 6 -> vehicleService.changeSecurityDeposit();
                case 7 -> reportService.adminReportsMenu();
                case 0 -> { System.out.println("  Logged out."); running = false; }
                default -> System.out.println("  ❌ Invalid choice.");
            }
        }
    }

    // ── BORROWER MENU ───────────────────────
    static void showBorrowerMenu(User user) {
        Borrower borrower = (Borrower) user;
        System.out.println("\n✅ Logged in as BORROWER: " + borrower.getEmail());
        System.out.printf("   Security Balance : Rs.%.0f%n",
                borrower.getSecurityBalance());
        boolean running = true;
        while (running) {
            System.out.println("\n====== BORROWER MENU ======");
            System.out.println("  1. Browse Vehicles");
            System.out.println("  2. Add to Cart");
            System.out.println("  3. Remove from Cart");
            System.out.println("  4. View Cart");
            System.out.println("  5. Checkout");
            System.out.println("  6. My Active Rentals");
            System.out.println("  7. Return / Extend / Exchange / Lost");
            System.out.println("  8. My Rental History");
            System.out.println("  0. Logout");
            System.out.println("===========================");
            int choice = InputHelper.getInt("  Choose: ");
            switch (choice) {
                case 1 -> rentalService.showCatalogue();
                case 2 -> rentalService.addToCart(borrower);
                case 3 -> rentalService.removeFromCart();
                case 4 -> rentalService.viewCart();
                case 5 -> rentalService.checkout(borrower);
                case 6 -> rentalService.viewMyActiveRentals(borrower);
                case 7 -> fineService.returnMenu(borrower);
                case 8 -> reportService.borrowerHistory(borrower);
                case 0 -> { System.out.println("  Logged out."); running = false; }
                default -> System.out.println("  ❌ Invalid choice.");
            }
        }
    }
}