package models;

public class Bike extends Vehicle {
    public Bike(int id, String name, String numberPlate,
                double rentalPricePerDay, int availableCount, double securityDeposit) {
        super(id, name, numberPlate, rentalPricePerDay, availableCount, securityDeposit);
    }

    @Override
    public int getServiceIntervalKms() {
        return 1500; // Bike needs service every 1500 km
    }

    @Override
    public String toString() {
        return "BIKE | " + super.toString();
    }
}