package models;
import java.time.LocalDate;

public class Booking {
    private static int counter = 1;

    private int bookingId;
    private Borrower borrower;
    private Vehicle vehicle;
    private LocalDate rentDate;
    private LocalDate returnDate;
    private double totalRent;
    private boolean returned;

    public Booking(Borrower borrower, Vehicle vehicle, LocalDate rentDate) {
        this.bookingId = counter++;
        this.borrower = borrower;
        this.vehicle = vehicle;
        this.rentDate = rentDate;
        this.returnDate = rentDate; // same day return by default
        this.returned = false;
    }

    // Getters
    public int getBookingId() { return bookingId; }
    public Borrower getBorrower() { return borrower; }
    public Vehicle getVehicle() { return vehicle; }
    public LocalDate getRentDate() { return rentDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getTotalRent() { return totalRent; }
    public boolean isReturned() { return returned; }

    public void setReturnDate(LocalDate date) { this.returnDate = date; }
    public void setTotalRent(double rent) { this.totalRent = rent; }
    public void markReturned() { this.returned = true; }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " | " + vehicle.getName()
             + " | From: " + rentDate + " | To: " + returnDate
             + " | Rs." + totalRent;
    }
}