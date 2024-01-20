import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/** A thread to be run that saves all the user information to a file
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class SaveUsers extends Thread implements Runnable {
    ArrayList<User> allUsers; // The ArrayList of users needing to be saved
    String filename; // The name of the file to be saved to

    // Constructor called by the Server
    // Inputs: ArrayList of users to be saved, name of the file to be saved to
    public SaveUsers(ArrayList<User> users, String filename) {
        allUsers = users; // Initialize allUsers to the input value
        this.filename = filename; // Initialize filename to the input value
    }

    // run method to be called by SaveUser.start()
    @Override
    public void run() {
        try {
            File f = new File(filename); // Open the file at the filename

            // Create OutputStream to the file that is not in append mode
            FileOutputStream fos = new FileOutputStream(f, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos); // Create ObjectOutputStream over the FileOutputStream
            oos.writeObject(allUsers); // Write the ArrayList of users to the file
            oos.close(); // Close the ObjectOutputStream
            fos.close(); // Close the FileOutputStream
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
