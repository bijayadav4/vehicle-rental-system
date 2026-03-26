package models;

public class Car extends Vehicle {
    public Car(int id, String name, String numberPlate,
               double rentalPricePerDay, int availableCount, double securityDeposit) {
        super(id, name, numberPlate, rentalPricePerDay, availableCount, securityDeposit);
    }

    @Override
    public int getServiceIntervalKms() {
        return 3000; // Car needs service every 3000 km
    }

    @Override
    public String toString() {
        return "CAR  | " + super.toString();
    }
}