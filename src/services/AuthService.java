package services;

import models.Borrower;
import models.User;
import utils.InputHelper;

import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private List<User> users = new ArrayList<>();

    public AuthService() {
        // Pre-load a default Admin account
        users.add(new User("admin@rental.com", "admin123", "ADMIN"));

        // Pre-load a sample Borrower
        users.add(new Borrower("user@rental.com", "user123"));
    }

    // Returns logged-in User or null if failed
    public User login() {
        System.out.println("\n========== LOGIN ==========");
        String email    = InputHelper.getString("  Email    : ");
        String password = InputHelper.getString("  Password : ");

        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)
                    && u.getPassword().equals(password)) {
                System.out.println("  ✅ Welcome, " + u.getEmail()
                        + " (" + u.getRole() + ")");
                return u;
            }
        }

        System.out.println("  ❌ Invalid email or password.");
        return null;
    }

    // Only Borrowers can self-register
    public void signup() {
        System.out.println("\n========== SIGN UP ==========");
        String email = InputHelper.getString("  Enter Email    : ");

        // Check if email already exists
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                System.out.println("  ❌ Email already registered.");
                return;
            }
        }

        String password = InputHelper.getString("  Enter Password : ");
        Borrower newBorrower = new Borrower(email, password);
        users.add(newBorrower);
        System.out.println("  ✅ Account created! Security balance: Rs.30,000");
        System.out.println("     You can now login.");
    }

    public List<User> getUsers() { return users; }
}