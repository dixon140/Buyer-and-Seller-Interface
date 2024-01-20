import java.io.Serializable;
import java.util.ArrayList;

/** A class that contains calendars and is created by a Seller
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class Store implements Serializable {
    private String name; // name of the store
    private ArrayList<Calendar> calendars; // All calendars in the store
    private ArrayList<Availability> pendingBookings; // Availabilities requested by buyers but not approved
    private ArrayList<Calendar> pendingBookingsCalendar; // The Calendar for each pendingBooking
    private ArrayList<Buyer> pendingBookingsBuyer; // The Buyer for each pendingBooking
    private Seller owner; // The Seller who owns the store

    public Store(String name, Seller owner) {
        // set name to input value and owner to store creator
        this.name = name;
        this.owner = owner;
        // default to no calendars and no pending bookings
        this.calendars = new ArrayList<>();
        this.pendingBookings = new ArrayList<>();
        this.pendingBookingsBuyer = new ArrayList<>();
        this.pendingBookingsCalendar = new ArrayList<>();
    }

    // create getters for all class variables
    public String getName() {
        return name;
    }

    public ArrayList<Buyer> getPendingBookingsBuyer() {
        return pendingBookingsBuyer;
    }

    public ArrayList<Availability> getPendingBookings() {
        return pendingBookings;
    }

    public Availability getPendingBookingsAvail(int index) {
        return pendingBookings.get(index);
    }

    public String getPendingBuyer(int index) {
        return pendingBookingsBuyer.get(index).getUsername();
    }
    public ArrayList<Calendar> getCalendars() {
        return calendars;
    }
    public Seller getOwner() {
        return owner;
    }

    // allow for the creation of new Calendar objects
    public void addCalendar(String called, String description) {
        calendars.add(new Calendar(called, description, this));
    }
    
    public void addCalendar(Calendar c) {
	    calendars.add(c);
    }

    // replace a currently held Calendar with a new one
    public void setCalendar(int index, Calendar replacement) {
        calendars.set(index, replacement);
    }

    // handle incoming booking requests by adding the necessary information to the ArrayLists
    public void requestBooking(Calendar cal, Availability avail, Buyer buyer) throws EventFullException {
        if (avail.getNumBookings() == avail.getMaxAttendees()) {
            throw new EventFullException("This event is fully booked! Sorry");
        } else {
            pendingBookings.add(avail);
            pendingBookingsCalendar.add(cal);
            pendingBookingsBuyer.add(buyer);
        }
    }

    // handle the situation where a Seller accepts a booking by contacting the Buyer and removing it from pending
    public void approveBooking(int i) {
        pendingBookings.get(i).bookingApproved(pendingBookingsBuyer.get(i));
        pendingBookings.remove(i);
        pendingBookingsCalendar.remove(i);
        pendingBookingsBuyer.remove(i);
    }

    // handle the situation where a Seller declines a booking by removing it from pending
    public void declineBooking(int i) {
        pendingBookings.remove(i);
        pendingBookingsCalendar.remove(i);
        pendingBookingsBuyer.remove(i);
    }

    // deprecated method used to display info for all pending bookings to System.out
    // TO DO: Remove after GUI is done
    public void showPendingBookings() {
        System.out.printf("Pending Bookings:\n");
        if (pendingBookings.size() != 0) {
            for (int i = 0; i < pendingBookings.size(); i++) {
                System.out.printf("Booking [%d]\n", i);
                System.out.printf("From Buyer: %s\n", pendingBookingsBuyer.get(i).getUsername());
                System.out.printf("From Calendar: %s\n", pendingBookingsCalendar.get(i).getName());
                System.out.printf("For the event on Day %d\n", pendingBookings.get(i).getDate());
                System.out.printf(" - Title: %s\n", pendingBookings.get(i).getTitle());
            }
        } else {
            System.out.printf("None!\n");
        }
    }
}
