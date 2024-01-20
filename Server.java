import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/** A server to handle requests made by users through a GUI
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class Server extends Thread implements Runnable {
    private ArrayList<User> userArray; // An ArrayList of User objects that are all the valid users for the server
    private ObjectOutputStream oos = null; // The outputStream used to send Objects to the clients
    private ObjectInputStream ois = null; // The inputStream used to receive Objects from the clients
    private Socket socket; // The socket that the Server is actively connected to
    private final ServerSocket serverSocket; // The ServerSocket the server lives on
    private final String filename; // The name of the file where all user data will be saved
    private User currentUser; // The user Object of the current client
    private int userIndex; // The index of the current client in the users ArrayList

    // Constructor called by Starter to start a new Server at a port with a certain file
    public Server(int port, String filename) {
        this.filename = filename; // Save the filename for later use
        try {
            serverSocket = new ServerSocket(port); // Start the server on the chosen port
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        userArray = new ArrayList<>(); // Initialize the users ArrayList as empty
        currentUser = null; // Set the initial user to be a null pointer
        userIndex = -1; // Set the initial userIndex to be -1 to prove no user is connected
        try {
            FileInputStream fis = new FileInputStream(this.filename); // Open an input stream to the selected file
            ObjectInputStream fileOis = new ObjectInputStream(fis); // Create an ObjectInputStream to the file
            Object currentLine = fileOis.readObject(); // Read the Object stored in the file
            userArray = (ArrayList<User>) currentLine; // Set users to be the ArrayList read back from the file
            fileOis.close(); // close the ObjectInputStream
            fis.close(); // close the FileInputStream
        } catch (EOFException e) {
            // end of file
        } catch (IOException e) {
            System.out.println("File not found.");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    // void method that runs before each request that will wait for a client to connect
    // does all the work of establishing a connection and reading the User object who is connected
    public void waitForConnection() {
        try {
            socket = serverSocket.accept(); // Wait until an attempt to connect and then store the connection as socket
            oos = new ObjectOutputStream(socket.getOutputStream()); // Create a new ObjectOutputStream to the client
            oos.flush(); // Send the header so the client can initialize a new ObjectInputStream
            ois = new ObjectInputStream(socket.getInputStream()); // Create a new ObjectInputStream from the client
            Boolean preExists = (Boolean) ois.readObject(); // Read whether this is a new user or not
            // If the user has connected to the server before...
            if (preExists) {
                userIndex = (Integer) ois.readObject(); // read the index the user is at in the ArrayList
                currentUser = (User) ois.readObject(); // read the User object that is communicating
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // void method that closes the connection to the socket
    // run after each request is handled
    public void closeConnection() {
        try {
            oos.close(); // close the ObjectOutputStream
            socket.close(); // close the Socket
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // run method required to implement Runnable and extend Thread
    // called by the Server.start() method in Starter
    public void run() {
        while (true) {
            waitForConnection(); // wait until a connection is made and read all necessary information
            String request; // allow scope of request to exceed try-catch
            try {
                request = (String) ois.readObject(); // read the request from the client
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            // NOTE: If getUser is run, another request is allowed after. This is why it is separate
            // if the client sends the request "getUser"
            if (request.equals("getUser")) {
                returnSeller(); // run the void to send their own object back

                // read the next request in the line
                try {
                    request = (String) ois.readObject(); // read the request from the client
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            if (request.equals("getUserPlease")) {
                returnSeller();
            }

            // If the request is...
            if (request.equals("login")) {
                testLogin(); // Checks to see if login criteria are valid
            } else if (request.equals("addAccount")) {
                addAccount(); // Adds a new User account
            } else if (request.equals("allStores")) {
                writeAllStores(); // Returns all Stores in the Server
            } else if (request.equals("calendar")) {
                writeAllCalendars(); // Returns all the Calendars for a Store
            } else if (request.equals("cancelRequest")) {
                cancelRequest(); // Cancels a Buyer's booking request
            } else if (request.equals("viewAll")) {
                viewAllBookings(); // Returns all Approved Bookings for a Buyer
            } else if (request.equals("getAvails")) {
                getCalendarAvails(); // Gets all Availabilities for a Calendar
            } else if (request.equals("addBooking")) {
                addBooking(); // Requests a booking from a Seller
            } else if (request.equals("updateUser")) {
                sellerUpdateUsers(); // Reads the Seller object from the client and saves it
            } else if (request.equals("approve")) {
                handleRequest(true); // Approves a pending booking request
            } else if (request.equals("decline")) {
                handleRequest(false); // Declines a pending booking request
            } else {
                // Throws error if invalid request made
                throw new RuntimeException("No Such Request Found for request: " + request);
            }

            try {
                runUserSaver(); // Saves the state of all Users to the file
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            closeConnection(); // Closes the connection to the client
        }
    }

    // Login Methods

    // Tests a client's login criteria and tells the client if the request is approved
    // If valid, sends the User object and index to the client
    private synchronized void testLogin() {
        try {
            String username = (String) ois.readObject(); // Read the username from the client
            String password = (String) ois.readObject(); // Read the password from the client

            // If we have users...
            if (userArray.size() > 0) {
                // For each user...
                for (int i = 0; i < userArray.size(); i++) {
                    // If the username and password match the current user...
                    if (userArray.get(i).getUsername().equals(username) && 
                        userArray.get(i).getPassword().equals(password)) {
                        oos.writeObject("approved"); // Tell the user their request is approved
                        oos.flush();
                        userIndex = i; // Save the user index as i
                        currentUser = userArray.get(i); // Save the currentUser as the User at i
                        oos.writeObject(userIndex); // Inform the user of their index
                        oos.flush();
                        oos.writeObject(currentUser); // Send the client their User object
                        oos.flush();
                        break;
                    } else if (i == userArray.size() - 1) {
                        // If we reach the last user and the criteria doesn't match...
                        oos.writeObject("not approved"); // Inform the user their request is not approved
                        oos.flush();
                    }
                }
            } else {
                // If we have zero users...
                oos.writeObject("not approved"); // Inform the user their request is not approved
                oos.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Add a new account with the necessary Criteria
    // Then send the client their index and User object
    private synchronized void addAccount() {
        // Extend the scope of the necessary fields past the try/catch
        String username;
        String password;
        String type;
        String email;
        try {
            username = (String) ois.readObject(); // Read the new username
            password = (String) ois.readObject(); // Read the new password
            type = (String) ois.readObject(); // Read the type if user to create
            email = (String) ois.readObject(); // Read the email associated with the account
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // If creating a Buyer...
        if (type.equals("buyer")) {
            // Create a new Buyer with the input criteria
            currentUser = new Buyer(username, password, email);
        } else {
            // Otherwise, create a Seller with the input criteria
            currentUser = new Seller(username, password, email);
        }
        userArray.add(currentUser); // Add the new User to the arrayList
        userIndex = userArray.size() - 1; // Save the userIndex as the last Index of the ArrayList
        try {
            oos.writeObject(currentUser); // Send the client their User object
            oos.flush();
            oos.writeObject(userIndex); // Send the client their index
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Buyer Methods

    // Writes to the client every Store name that exists on the Server
    private synchronized void writeAllStores() {
        try {
            // For every User in the ArrayList
            for (User user : userArray) {
                // If the User is a Seller
                if (user.getClass() == Seller.class) {
                    ArrayList<Store> userStores = ((Seller) user).getStores(); // Save all stores the seller has
                    // For each store the seller has...
                    for (Store userStore : userStores) {
                        oos.writeObject(userStore.getName()); // Write the name of the Store to the User
                        oos.flush();
                    }
                }
            }
            oos.writeObject("EOT"); // Write End of Transmission to inform the client all have sent
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Writes to the client every Calendar name that exists for a Store
    private synchronized void writeAllCalendars() {
        try {
            String storeName = (String) ois.readObject(); // Read the Store name from the client
            Store returnedStore = getStore(userArray, storeName); // Find the specific Store from the Seller
            // If the Store cannot be found
            if (returnedStore == null) {
                oos.writeObject("notFound"); // Inform the client the Store could not be found
                oos.flush();
            } else {
                // Otherwise, if a store is found...
                oos.writeObject("found"); // Inform the client the Store has been found
                oos.flush();
                // For every Calendar in the Store...
                for (int i = 0; i < returnedStore.getCalendars().size(); i++) {
                    // Write out the name of the Calendar to the client
                    oos.writeObject(returnedStore.getCalendars().get(i).getName());
                    oos.flush();
                }
                oos.writeObject("EOT"); // Inform the client that all names have been sent
                oos.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Cancels an approved booking
    private synchronized void cancelRequest() {
        Availability cancelSelection; // Extends the scope of the selected Availability
        try {
            cancelSelection = (Availability) ois.readObject(); // Read selected Availability from the client
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // If a valid Availability is received...
        if (cancelSelection != null) {
            int foundIndex = -1; // Default the index to impossible value
            // For all approved bookings the User has...
            for (int i = 0; i < ((Buyer) currentUser).getBookings().size(); i++) {
                // Store the Availability at index i as currentAvail
                Availability currentAvail = ((Buyer) currentUser).getBookings().get(i);
                // If all the criteria for the Availability matches up...
                // NOTE: Running currentAvail.equals(cancelSelection) would be easier but always returns false
                if (currentAvail.getTitle().equals(cancelSelection.getTitle()) &&
                        currentAvail.getDate() == cancelSelection.getDate() &&
                        currentAvail.getStart().equals(cancelSelection.getStart()) &&
                        currentAvail.getEnd().equals(cancelSelection.getEnd())) {
                    foundIndex = i; // The index of the Availability that matches up is i
                    break;
                }
            }
            ((Buyer) currentUser).removeBooking(foundIndex); // Remove the booking at the index found above
            userArray.set(userIndex, currentUser); // Store the update User to the ArrayList
        }
    }

    // Return all approved bookings the client has
    private synchronized void viewAllBookings() {
        // Store all currently approved bookings into an ArrayList
        ArrayList<Availability> currentBookings = ((Buyer) currentUser).getBookings();
        try {
            // For each availability that has been approved...
            for (Availability currentBooking : currentBookings) {
                oos.writeObject(currentBooking); // Write the Availability to the client
                oos.flush();
            }
            // Write a not-real Availability with the name "EOT" to tell the client all Availabilities have been written
            oos.writeObject(new Availability("EOT", 1, null, null, 1, null));
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Writes all Availabilities for a Calendar to the client
    private synchronized void getCalendarAvails() {
        try {
            String storeName = (String) ois.readObject(); // Read the Store name from the client
            Store returnedStore = getStore(userArray, storeName); // Get the Store associated with that name
            // If a Store with that name is not found
            if (returnedStore == null) {
                oos.writeObject("notFound"); // Inform the client that Store could not be found
                oos.flush();
            } else {
                // Otherwise, if a Store is found...
                oos.writeObject("found"); // Inform the user the store has been found
                oos.flush();
                Seller storeOwner = returnedStore.getOwner(); // Save the Seller object who owns the Store

                // Store the index of the Store object we found
                returnedStore = storeOwner.getStore(storeOwner.getStores().indexOf(returnedStore));
                String calendar = (String) ois.readObject(); // Read which calendar the client wants to read from
                // For each calendar in the Store...
                for (int i = 0; i < returnedStore.getCalendars().size(); i++) {
                    // If the Calendar name matches the name requested...
                    if (returnedStore.getCalendars().get(i).getName().equals(calendar)) {
                        // Write an ArrayList of Availabilities from the Calendar to the client
                        oos.writeObject(returnedStore.getCalendars().get(i).getAvailabilities());
                        oos.flush();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Request a booking from a Store and Calendar
    private synchronized void addBooking() {
        try {
            String storeName = (String) ois.readObject(); // Read the store name from the client
            Store returnedStore = getStore(userArray, storeName); // Find the Store object to match the name
            // If a store with that name is found...
            if (returnedStore != null) {
                Seller storeOwner = returnedStore.getOwner(); // Save the Seller that owns the Store
                int sellerIndex = userArray.indexOf(storeOwner); // Save the Index where the Seller is

                // Get the Store from the Seller
                returnedStore = storeOwner.getStore(storeOwner.getStores().indexOf(returnedStore));
                String calendar = (String) ois.readObject(); // Read the Calendar name from the client
                // For each Calendar in the Store...
                for (int i = 0; i < returnedStore.getCalendars().size(); i++) {
                    // If the name of the Calendar matches the request
                    if (returnedStore.getCalendars().get(i).getName().equals(calendar)) {
                        int choice = (Integer) ois.readObject(); // Read the Index the user has chosen
                        try {
                            returnedStore.getCalendars().get(i).getAvailabilities().get(choice).addBooking((
                                    (Buyer) currentUser)); // Add the Booking through the Availability
                            userArray.set(sellerIndex, storeOwner); // Save the updated Seller
                            userArray.set(userIndex, currentUser); // Save the updated Buyer
                            oos.writeObject("Success"); // Inform the client a successful request was made
                            oos.flush();
                        } catch (EventFullException e) {
                            oos.writeObject("Event Full"); // Inform the client that this event is full
                            oos.flush();
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Seller Methods

    // Updates the Seller to the one input
    private synchronized void sellerUpdateUsers() {
        try {
            currentUser = (Seller) ois.readObject(); // Read the Seller object from the client
            userArray.set(userIndex, currentUser); // Set the User at the Index to be the inputted value
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Approves a pending booking from a Buyer
    private synchronized void handleRequest(boolean approve) {
        Store chosenStore; // Extends scope of chosenStore outside try-catch
        Integer availNum; // Extends scope of availNum outside try-catch
        try {
            chosenStore = (Store) ois.readObject(); // Read the store the request is in from the client
            availNum = (Integer) ois.readObject(); // Read the index of the approved request from the client
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Buyer requester = chosenStore.getPendingBookingsBuyer().get(availNum); // Save the Buyer who made the request
        int buyerIndex = -1; // Initialize the index of the Buyer to an impossible value
        // For each user...
        for (int i = 0; i < userArray.size(); i++) {
            // If the user is a Buyer...
            if (userArray.get(i).getClass() == Buyer.class) {
                // If the username and password match the Requester...
                if (userArray.get(i).getUsername().equals(requester.getUsername())
                        && userArray.get(i).getPassword().equals(requester.getPassword())) {
                    buyerIndex = i; // Save the index of the Buyer
                    break;
                }
            }
        }
        
        // If the request was to approve the booking...
        if (approve) {
            chosenStore.approveBooking(availNum); // Approve the booking in the Store
        } else {
            // Otherwise, the request was to decline the booking
            chosenStore.declineBooking(availNum); // Decline the booking in the Store
        }
        userArray.set(buyerIndex, requester); // Update the Buyer who requested it
        userArray.set(userIndex, currentUser); // Update the Seller who handled it
    }

    // Returns the current state of the Seller
    private synchronized void returnSeller() {
        try {
            oos.writeObject(userArray.get(userIndex)); // Write the Seller object at the userIndex to the client
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // void method that starts a Thread to save the state of all users to the specified file
    private void runUserSaver() throws InterruptedException {
        // Create a new thread to save all the users to the file
        SaveUsers userSaver = new SaveUsers(userArray, filename);
        userSaver.start(); // Start the thread
        userSaver.join(); // Wait until the thread finishes to avoid race conditions
    }

    // method to find a particular store given the name of the Store
    // Takes input of ArrayList of Users and a Store name
    // Returns: Store object with the name
    private Store getStore(ArrayList<User> users, String storeName) {
        // For each user...
        for (User user : users) {
            // If the user is a Seller...
            if (user.getClass() == Seller.class) {
                // Store all the Stores the seller has to an ArrayList
                ArrayList<Store> userStores = ((Seller) user).getStores();
                // For each Store in the ArrayList
                for (Store userStore : userStores) {
                    // If the name of the Store matches the request
                    if (userStore.getName().equals(storeName)) {
                        return userStore; // Return that Store
                    }
                }
            }
        }
        // If no Store with that name is found...
        return null;
    }
}
