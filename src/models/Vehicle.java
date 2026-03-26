package models;

public abstract class Vehicle {
    private int id;
    private String name;
    private String numberPlate;
    private double rentalPricePerDay;
    private int availableCount;
    private double securityDeposit;
    private int totalKmsRun;        // tracks total km for servicing
    private boolean isRented;

    public Vehicle(int id, String name, String numberPlate,
                   double rentalPricePerDay, int availableCount, double securityDeposit) {
        this.id = id;
        this.name = name;
        this.numberPlate = numberPlate;
        this.rentalPricePerDay = rentalPricePerDay;
        this.availableCount = availableCount;
        this.securityDeposit = securityDeposit;
        this.totalKmsRun = 0;
        this.isRented = false;
    }

    // Abstract method — each subclass defines its service limit
    public abstract int getServiceIntervalKms();

    public boolean needsService() {
        return totalKmsRun >= getServiceIntervalKms();
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getNumberPlate() { return numberPlate; }
    public double getRentalPricePerDay() { return rentalPricePerDay; }
    public int getAvailableCount() { return availableCount; }
    public double getSecurityDeposit() { return securityDeposit; }
    public int getTotalKmsRun() { return totalKmsRun; }
    public boolean isRented() { return isRented; }

    public void setAvailableCount(int count) { this.availableCount = count; }
    public void setSecurityDeposit(double deposit) { this.securityDeposit = deposit; }
    public void setRented(boolean rented) { this.isRented = rented; }
    public void addKms(int kms) { this.totalKmsRun += kms; }
    public void resetKmsAfterService() { this.totalKmsRun = 0; }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | Plate: " + numberPlate
             + " | Rs." + rentalPricePerDay + "/day"
             + " | Available: " + availableCount
             + " | KMs: " + totalKmsRun;
    }
}