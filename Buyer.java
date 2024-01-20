import java.io.Serializable;
import java.util.ArrayList;

/** A class that contains a User that can request Availabilities
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class Buyer extends User implements Serializable {
    private ArrayList<Availability> bookings; // All Availabilities this Buyer has been approved for

    // Constructor
    public Buyer(String username, String password, String email) {
        // Call the super class User and create a new User with the input values
        super(username, password, email);
        this.bookings = new ArrayList<>(); // Initialize the approved bookings to be an empty ArrayList
    }

    // void method to add an Availability to the approved booking list
    // Input: The Availability that has been approved
    public void addApprovedBooking(Availability booking) {
        bookings.add(booking); // Add the input Availability to the ArrayList
    }

    // Getter to return an ArrayList of all Availabilities this Buyer has been approved for
    public ArrayList<Availability> getBookings() {
        return bookings;
    }

    // void method to display booking to System.out
    // NOTE: To be deprecated with the new GUI
    public void viewBookings() {
        System.out.println("\nCurrently Approved Bookings: ");
        if (bookings != null) {
            for (int i = 0; i < bookings.size(); i++) {
                bookings.get(i).describe();
            }
        }
    }

    // void method to remove the approved booking at a certain index
    // Inputs: The index of the booking to be removed
    public void removeBooking(int index) {
        bookings.remove(index); // Remove the Availability in bookings at the chosen index
    }
}
