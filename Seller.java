import java.io.Serializable;
import java.util.ArrayList;

/** A class that contains a User that can make Stores, Calendars and Availabilities and approve booking requests
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class Seller extends User implements Serializable {
    private ArrayList<Store> stores; // An ArrayList of Stores that all belong to this Seller

    // Constructor to make a new Seller with a username, password, and email
    public Seller(String username, String password, String email) {
        // Call the super class User and send it the required fields: username, password, and email
        super(username, password, email);
        this.stores = new ArrayList<>(); // Initialize the stores ArrayList to be empty
    }

    // void method to add a new Store with a certain Name
    // Inputs: name of the store
    public void addStore(String storeName) {
        // Create new Store with the name provided and link it back to this Seller. Then add it to the stores ArrayList
        stores.add(new Store(storeName, this));
    }

    // getter that returns an ArrayList of all Stores owned by this Seller
    public ArrayList<Store> getStores() {
        return stores; // Returns the local stores variable
    }

    // getter that returns a store from a certain index in the ArrayList
    // Inputs: index to be retrieved from
    public Store getStore(int storeIndex) {
        return stores.get(storeIndex); // Returns the Store at the desired index
    }
}
