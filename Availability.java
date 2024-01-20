import javax.swing.*;
import java.io.Serializable;
import java.io.*;

/** A class to handle availabilities created by sellers
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class Availability implements Serializable {
    private int date; // The date this Availability occurs on
    private String start; // The Start time for this Availability
    private String end; // The End time for this Availability
    private final String title; // The name for this Availability
    private int maxAttendees; // The maximum number of Buyers who can attend
    private int bookings; // The total number of Buyers who have been approved to come
    private final Calendar calendar; // The Calendar this Availability belongs to

    // Constructor that takes all necessary information for a new Availability
    public Availability(String title, int date, String start, String end, int maxAttendees, Calendar calendar) {
        // Initialize all class variables to input values
        this.date = date;
        this.start = start;
        this.end = end;
        this.title = title;
        this.maxAttendees = maxAttendees;
        this.bookings = 0; // Default: nobody is approved yet
        this.calendar = calendar;
    }

    // void method to add a new Booking to this Availability
    public void addBooking(Buyer buyer) throws EventFullException {
        // If the event is full...
        if (bookings == maxAttendees) {
            // Throw a new EventFullException
            throw new EventFullException("This event is fully booked! Sorry");
        } else {
            // If space is still left...
            calendar.requestBooking(this, buyer); // Hand this request up to the Calendar
        }
    }

    // Getter to get the number of people already approved to come to this Availability
    public int getNumBookings() {
        return bookings;
    }

    // Void method to approve a booking
    // Inputs: The buyer that has been approved
    public void bookingApproved(Buyer buyer) {
        buyer.addApprovedBooking(this); // Add this Availability to the list of approved bookings for the Buyer
        bookings++; // Increment the total number of bookings for this Availability
    }

    // Getter to get the Name of this Availability
    public String getTitle() {
        return title;
    }

    // Getter to get the Date this Availability occurs on
    public int getDate() {
        return date;
    }

    // Getter to get the Start time of this Availability
    public String getStart() {
        return start;
    }

    // Getter to get the maximum number of people allowed to attend this Availability
    public int getMaxAttendees() {
        return maxAttendees;
    }

    // Getter to get the End time of this Availability
    public String getEnd() {
        return end;
    }

    // Setter to set the Date of this Availability. Used for rescheduling
    public void setDate(int date) {
        this.date = date;
    }

    // Setter to set the Start time of this Availability. Used for rescheduling
    public void setStart(String start) {
        this.start = start;
    }

    // Setter to set the End time of this Availability. Used for rescheduling
    public void setEnd(String end) {
        this.end = end;
    }

    // method to return the double value of the start time out of 24. Used for sorting
    public double getStartAsDecimal() {
        double startTime = 0; // Initialize value to 0
        int colonIndex = start.indexOf(":"); // Get wher the colon is in the Start Time
        startTime += Integer.parseInt(start.substring(0, colonIndex)); // Get the hour this occurs at
        startTime += Integer.parseInt(start.substring(colonIndex + 1)) / 60.0; // Add the minutes to the hours
        return startTime; // Return the double value of the Start time for this event
    }

    public String describe() {
        String ending = "th";
        if (date / 10 != 1) {
            switch (date % 10) {
                case 1:
                    ending = "st";
                    break;
                case 2:
                    ending = "nd";
                    break;
                case 3:
                    ending = "rd";
                    break;
                default:
                    ending = "th";
            }
        }
        String iguodala = ("Availability on the " + date + ending + "| - Title: " +
                title + "| - Maximum Attendees: " + maxAttendees + "| - Current number of Approved Bookings: " +
                bookings + "| - Start: " + start + " End: " + end + "|");

        return iguodala;
    }

    // Setter to set the Maximum number of people allowed to attend this Availability
    // Inputs: Integer value of the maximum number of people now allowed to attend
    public void setMaxAttendees(int amount) {
        maxAttendees = amount;
    }
}
