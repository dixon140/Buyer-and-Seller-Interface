import java.awt.*;
import java.io.Serializable;
import java.time.LocalDateTime; // https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
import java.util.ArrayList;
import javax.swing.*;


/** A class to handle calendar objects within stores and to hold Availabilities
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class Calendar implements Serializable { // allows class to be written to file
    private final String name; // name of the calendar
    private final Store store; // the store of the calendar
    private String description; // description of the calendar
    private ArrayList<Availability> availabilities; // all the availabilities for the calendar
    private String lastTime; // String to hold the time of the last change to the calendar

    // constructor
    public Calendar(String name, String description, Store store) {
        // Initialize variables to input values
        this.name = name;
        this.store = store;
        this.description = description;
        this.availabilities = new ArrayList<Availability>(); // start with no availabilities
        updateLastTime(); // update the last time to right now
    }

    // add an Availability to the calendar
    public void addAvailability(Availability newAvail) {
        availabilities.add(newAvail); // appends the new Availability to the calendar ArrayList availabilities
        updateLastTime(); // update the time of the last change to now
    }

    // function to update the last change to now
    private void updateLastTime() {
        // https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
        LocalDateTime rightNow = LocalDateTime.now(); // create a new LocaDateTime with the current time
        // format last time to store the current time. Example 19:36:27 11/3/2022
        lastTime = String.format("%d:%d:%d on %d/%d %d", rightNow.getHour(), rightNow.getMinute(), rightNow.getSecond(),
                rightNow.getMonthValue(), rightNow.getDayOfMonth(), rightNow.getYear());
    }

    // sort the availabilities array by start date and time
    public void sortAvailabilities() {
        ArrayList<Availability> newArrayList = new ArrayList<>(); // create a new ArrayList to add the results to
        // create a new ArrayList for all the Availabilities already sorted
        ArrayList<Integer> usedIndeces = new ArrayList<>();
        int bestIndex = -1; // set the best index to an impossible one
        int bestDate = 1000; // set the bestDate to an impossible date
        double bestStartTime = 25.00; // set the best time to an impossible time (25.00 hours)
        for (int j = 0; j < availabilities.size(); j++) {
            for (int i = 0; i < availabilities.size(); i++) { // for each availability
                if (!(usedIndeces.contains(i))) { // if the current index has already been assessed
                    // Sets the value currentAvail to the current availability being assessed
                    Availability currentAvail = availabilities.get(i);
                    if (currentAvail.getDate() < bestDate) { // if the current available date is better than the best
                        // make this availability the new best
                        bestIndex = i;
                        bestDate = currentAvail.getDate();
                        bestStartTime = currentAvail.getStartAsDecimal();
                    } else if (currentAvail.getDate() == bestDate) { 
                        // else if current available date is equal to the best
                        if (currentAvail.getStartAsDecimal() < bestStartTime) { 
                            // if the start time is sooner than the best
                            // make this availability the new best
                            bestIndex = i;
                            bestDate = currentAvail.getDate();
                            bestStartTime = currentAvail.getStartAsDecimal();
                        }
                    }
                }
            }
            // store the winner of the sort
            usedIndeces.add(bestIndex);
            bestIndex = -1;
            bestDate = 1000;
            bestStartTime = 25.00;
        } // repeat until done sorting

        // make newArrayList sorted
        for (int i = 0; i < availabilities.size(); i++) {
            newArrayList.add(availabilities.get(usedIndeces.get(i)));
        }

        // set availabilities equal to newArrayList
        availabilities = newArrayList;
    }

    // Method called by Availability to request a Booking from the Store
    public void requestBooking(Availability avail, Buyer buyer) throws EventFullException {
        store.requestBooking(this, avail, buyer);
    }

    // Getter to return the name of the Calendar
    public String getName() {
        return name;
    }

    // Getter to return the description of the Calendar
    public String getDescription() {
        return description;
    }

    // Getter to return the Availability at an index
    public Availability getAvailability(int index) {
        return availabilities.get(index);
    }

    // Getter to return all Availabilities in this Calendar
    public ArrayList<Availability> getAvailabilities() {
        return availabilities;
    }

    // function to display the calendar as described in the handout
    public String display() {
        String iguodala;

        iguodala = (name + " - " + description + "|");

        if (availabilities != null) {
            sortAvailabilities();
            for (int i = 1; i <= availabilities.size(); i++) {
                iguodala += ("Availability #" + i + "|");
                iguodala += (availabilities.get(i - 1).describe());     
            }


        } else {

            iguodala += ("No Availabilities!|");

            iguodala += ("Last Update was: " + lastTime + "|");

        }

        return iguodala;
    }
}
