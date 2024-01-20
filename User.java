import java.io.Serializable;

/** A super class that contains the username, password, and email of all users in the marketplace
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class User implements Serializable {
    // set variables to hold information necessary for login and account creation
    String username;
    String password;
    String email;

    public User(String username, String password, String email) {
        // set class variables to the values inputted.
        this.username = username;
        this.password = password;
        this.email = email;
    }
    // create getters for all values held by the class User
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
