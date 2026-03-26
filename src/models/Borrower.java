package models;

public class Borrower extends User {
    private double securityBalance; // starts at 30000
    private Vehicle rentedCar;
    private Vehicle rentedBike;
    private int extensionCount;     // max 2 extensions

    public Borrower(String email, String password) {
        super(email, password, "BORROWER");
        this.securityBalance = 30000.0;
        this.rentedCar = null;
        this.rentedBike = null;
        this.extensionCount = 0;
    }

    public double getSecurityBalance() { return securityBalance; }
    public Vehicle getRentedCar() { return rentedCar; }
    public Vehicle getRentedBike() { return rentedBike; }
    public int getExtensionCount() { return extensionCount; }

    public void deductBalance(double amount) { securityBalance -= amount; }
    public void setRentedCar(Vehicle car) { this.rentedCar = car; }
    public void setRentedBike(Vehicle bike) { this.rentedBike = bike; }
    public void incrementExtension() { extensionCount++; }
    public void resetExtension() { extensionCount = 0; }
}