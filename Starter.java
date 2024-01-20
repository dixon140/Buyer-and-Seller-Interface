import javax.swing.*;
import java.util.ArrayList;

/** A simple GUI that allows for the starting of the GUI and the creation of new Servers
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class Starter {
    public static ArrayList<Integer> ports; // all ports hosting a Server that are thus not available for a new server
    public static ArrayList<String> portNames; // the names of the servers on each port
    public static String filename; // the name of the file to be used in custom server creation
    public static ArrayList<NewUI> activeUI; // an ArrayList of all the UI objects currently running
    public static ArrayList<Server> activeServers; // an ArrayList of all the Servers currently running

    public static void main(String[] args) {
        // initialize all ArrayLists to empty ArrayLists
        ports = new ArrayList<>();
        portNames = new ArrayList<>();
        activeUI = new ArrayList<>();
        activeServers = new ArrayList<>();
        // Create the default server at 4242 and use the file users.txt
        Server mainServer = new Server(4242, "users.txt");
        // Add the server to the ArrayList
        activeServers.add(mainServer);
        // Start the server
        mainServer.start();
        // Add the used port to the list of unavailable ports
        ports.add(4242);
        // name the server "Default Server"
        portNames.add("Default Server");
        // prompt the addition of a GUI
        startNew();
    }

    public static void startNew() {
        // prompt the user if they would like to enter the marketplace
        if (showStartMenu()) {
            // if the user would like to create a new server
            if (askToCreateNew()) {
                // ask what port the server should live on and save that value
                int port = askForPort();
                if (port == 0) {
                    return;
                }
                // ask for a Server Name to be displayed
                String name = askForName();
                // add the inputted port
                ports.add(port);
                // add the inputted name
                portNames.add(name);
                // open a file explorer and ask the user to pick the .txt file of user objects to use
                filename = askForFile();
                // Create the Server and add it to the ArrayList
                activeServers.add(new Server(port, filename));
                // Start the new server
                activeServers.get(activeServers.size() - 1).start();
                // Create a new GUI in the new server
                activeUI.add(new NewUI(port));
                // Start the new GUI
                activeUI.get(activeUI.size() - 1).start();
            } else {
                // display all active servers and have the user pick one
                int port = showLivePorts();
                // create a new GUI and add it to the ArrayList
                activeUI.add(new NewUI(port));
                // start the GUI
                activeUI.get(activeUI.size() - 1).start();
            }
        }
    }

    // A menu prompting the user about whether they would like to enter the marketplace or not
    // returns True if so and False if not
    public static boolean showStartMenu() {
        int choice = JOptionPane.showConfirmDialog(null, "Would you like to enter the Marketplace Calendar?",
                "Marketplace Calendar", JOptionPane.YES_NO_OPTION);
        return choice == JOptionPane.YES_OPTION;
    }

    // A menu asking the user if they would like to start a new Server
    // returns True if so and False if not
    public static boolean askToCreateNew() {
        int choice = JOptionPane.showConfirmDialog(null, "Would you like to start a new server?",
                "Marketplace Calendar", JOptionPane.YES_NO_OPTION);
        return choice == JOptionPane.YES_OPTION;
    }

    // A menu asking the user what port a new Server should live on
    // returns the new port number that is guaranteed to be valid
    public static int askForPort() {
        int port = -1;
        boolean passed = true;
        do {
            passed = true;
            try {
                String portStr = JOptionPane.showInputDialog(null, "What port would you like to use?",
                        "Marketplace Calendar", JOptionPane.QUESTION_MESSAGE);
                if (portStr.equals("")) {
                    return 0;
                }
                port = Integer.parseInt(portStr);
                if (port <= 0) {
                    JOptionPane.showMessageDialog(null, "Please input a valid port!", "Marketplace Calendar",
                            JOptionPane.ERROR_MESSAGE);
                    passed = false;
                } else if (ports.contains(port)) {
                    JOptionPane.showMessageDialog(null, "Port Already In Use!", "Marketplace Calendar",
                            JOptionPane.ERROR_MESSAGE);
                    passed = false;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Please input a valid port!", "Marketplace Calendar",
                        JOptionPane.ERROR_MESSAGE);
                passed = false;
            }
        } while (!passed);

        return port;
    }

    // A dropdown menu displaying all active port names and prompting the user to select one
    // returns the integer value of the selected port
    public static int showLivePorts() {
        String[] options = portNames.toArray(new String[0]);
        Object selectedValue = JOptionPane.showInputDialog(null,
                "Which Server would you like to connect to?", "Marketplace Calendar",
                JOptionPane.INFORMATION_MESSAGE, null,
                options, options[0]);
        return ports.get(portNames.indexOf((String) selectedValue));
    }

    // A menu asking a user to name their newly created Server
    // returns the String value of the new name
    public static String askForName() {
        String choice = JOptionPane.showInputDialog(null, "What would you like to call your server?",
                "Marketplace Calendar", JOptionPane.QUESTION_MESSAGE);
        return choice;
    }

    // A menu with a popup that prompts a user to select a .txt file of User objects to use in the server
    // returns the path of the selected file
    public static String askForFile() {
        // create the new file chooser
        JFileChooser chooser = new JFileChooser();
        // open a message dialog to better explain to the user what type of file is necessary
        JOptionPane.showMessageDialog(null,
                "Select a .txt file of User objects to import to the Server", "Marketplace Calendar",
                JOptionPane.INFORMATION_MESSAGE);
        // name the File chooser
        chooser.setDialogTitle("Select a .txt file of User Objects");
        // wait until a file has been selected
        chooser.showOpenDialog(null);
        // return the String value of the file path
        return chooser.getSelectedFile().toString();
    }
}