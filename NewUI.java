import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/** A class that displays all necessary information in a GUI and interacts with a Server
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class NewUI extends Thread implements Runnable {
    private static Scanner scan = new Scanner(System.in); // old deprecated Scanner to be removed after Seller GUI
    private int port; // the port the GUI uses
    private String username; // username of logged-in User
    private String password; // password of logged-in User
    private Socket socket = null; // Socket to be used to make request
    private ObjectInputStream ois = null; // Object Input Stream to read from Server
    private ObjectOutputStream oos = null; // Object Output Stream to write to Server
    static JComboBox<String> storeInput; // Dropdown Menu in Buyer GUI that has a selected Store
    static JComboBox<String> calendarInput; // Dropdown Menu in Buyer GUI that has a selected calendar

    private int userIndex; // The index of the currentUser in the Server. Needed to make a connection
    private User currentUser; // The User object who is currently logged in

    // Deprecated method to check if a number input was valid
    // Will be removed once Seller GUI is up
    public static int validInput(int menuLength) {
        while (true) {
            int menuOption = scan.nextInt();
            boolean optionFound = false;
            scan.nextLine();
            for (int i = 1; i <= menuLength; i++) {
                if (menuOption == i) {
                    optionFound = true;
                    break;
                }
            }
            if (optionFound) {
                return menuOption;
            } else {
                System.out.print("Invalid input, please try again: ");
            }
        }
    }

    // Constructor for newUi that sets the port to the inputted value
    public NewUI(int port) {
        this.port = port;
    }

     /** A GUI to initially be displayed and allow clients to log in
     *
     * @author CS180 Group 007
     * @version 12/12/2022
     */
    public class LoginGui extends JComponent implements Runnable {
        JFrame frame; // The frame used by the GUI
        JMenuBar menuBar; // The menuBar for the GUI that holds the add connection button
        JButton addUIButton; // The button that spawns a new Starter instance to add a new GUI concurrently
        Container content; // The content of the frame
        GridBagConstraints c; // The constraints used to position JComponents on the Screen
        JLabel welcomeText; // A label that contains a Welcome Message
        JPanel usernamePanel; // A Panel containing a textField and Label for entering a username
        JLabel usernameText; // Label containing "Username:"
        JTextField usernameInput; // The TextField containing the input username
        JPanel passwordPanel; // A Panel containing a textField and Label for entering a password
        JLabel passwordText; // A label containing "Password:"
        JPasswordField passwordInput; // The PasswordField containing the input password that is hidden on the GUI
        JPanel buttonArray; // The panel containing submit and quit buttons
        JButton submitButton; // The button to submit a username and password
        JButton quitButton; // The button to stop the login process

        // A method that runs the LoginGUI on the EventDispatchThread to prevent crashes and improve performance
        public void start() {
            SwingUtilities.invokeLater(new LoginGui());
        }

        // The actionListener that knows what to do when any button is pressed
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // if submitButton is pressed
                if (e.getSource() == submitButton) {
                    // save the username and passwords that are input
                    username = usernameInput.getText();
                    password = String.valueOf(passwordInput.getPassword());
                    // try the login and return if it is successful or not
                    boolean login = testLogin();
                    // if not a valid login
                    if (!login) {
                        // prompt to add this information as a new User
                        invalidLogin();
                        // remove this frame
                        frame.dispose();
                        // Once the new account is created move onto the LoggedIn page if the account is made
                        if (currentUser != null) {
                            loggedIn();
                        }
                    } else {
                        // if this is valid close the screen and move on
                        frame.dispose();
                        loggedIn();
                    }
                } else if (e.getSource() == quitButton) {
                    // if the quit button is hit delete the frame
                    frame.dispose();
                } else if (e.getSource().equals(addUIButton)) {
                    // if the add connection button is hit, spawn a new GUI
                    Starter.startNew();
                }
            }
        };


        // The method to set up the GUI and display it
        public void run() {
            frame = new JFrame("Marketplace Login"); // Create a new JFrame and name it "Marketplace Login"
            menuBar = new JMenuBar(); // Create A new Menu Bar
            addUIButton = new JButton("Add Connection"); // Create the Add Connection button
            addUIButton.setBackground(Color.WHITE); // Set the button Background to White
            addUIButton.addActionListener(actionListener); // Link the Add Connection Button to the ActionListener
            menuBar.add(addUIButton); // Add the add connection button to the menuBar
            frame.setJMenuBar(menuBar); // Link the MenuBar to the frame
            content = frame.getContentPane(); // Initialize content as the frame content
            content.setLayout(new GridBagLayout()); // Set the layout of content as a GridBag Layout
            c = new GridBagConstraints(); // initialize the Grid Bag Layout
            welcomeText = new JLabel("Welcome to the Marketplace Calendar"); // Create Welcome Text
            welcomeText.setFont(new Font("SansSerif", Font.BOLD, 18)); // Change welcome font
            c.fill = GridBagConstraints.CENTER; // Tell the JComponents to center themselves in the grid
            c.ipady = 20; // leave 20 pixels above and below the JComponent
            c.gridx = 1; // Put this in Grid Column 1
            c.gridy = 0; // Put this in Grid Row 0
            content.add(welcomeText, c); // Add this to the frame

            c.ipady = 5; // Reduce the vertical padding back down to 5 pixels

            // Add Username Text and Text Area
            usernamePanel = new JPanel();

            usernameText = new JLabel("Username: "); // Create username label
            usernameText.setFont(new Font("SansSerif", Font.BOLD, 12)); // Change username font
            c.fill = GridBagConstraints.BOTH; // Tell the JComponent to fill the grid Vertically and Horizontally
            c.gridx = 1; // Put this in Grid Column 1
            c.gridy = 1; // Put this in Grid Row 1
            usernamePanel.add(usernameText, c); // Add this to the frame

            usernameInput = new JTextField(); // Create a new text field
            usernameInput.setColumns(30); // Allow it to display 30 characters
            c.fill = GridBagConstraints.BOTH; // Tell the JComponent to fill the grid Vertically and Horizontally
            c.weightx = 1; // Give this a priority level of 1 in the Column (Reduces conflict with Label)
            c.gridx = 1; // Put this in Grid Column 1
            c.gridy = 1; // Put this in Grid Row 1
            usernamePanel.add(usernameInput, c); // Add this to the panel
            content.add(usernamePanel, c); // Add the panel to the frame

            // Add Password Text and Text Area
            passwordPanel = new JPanel();

            passwordText = new JLabel("Password: "); // Create password text
            passwordText.setFont(new Font("SansSerif", Font.BOLD, 12)); // Change text font
            c.fill = GridBagConstraints.BOTH; // Tell the JComponent to fill the grid Vertically and Horizontally
            c.gridx = 1; // Put this in Grid Column 1
            c.gridy = 2; // Put this in Grid Row 2
            passwordPanel.add(passwordText, c); // Add this to the panel

            passwordInput = new JPasswordField(); // Create a new password field
            passwordInput.setColumns(30); // Allow it to hold 30 characters
            c.fill = GridBagConstraints.BOTH; // Tell the JComponent to fill the grid Vertically and Horizontally
            c.weightx = 1; // Give this a priority level of 1 in the Column (Reduces conflict with Label)
            c.gridx = 1; // Put this in Grid Column 1
            c.gridy = 2; // Put this in Grid Row 2
            passwordPanel.add(passwordInput, c); // Add this to the panel
            content.add(passwordPanel, c); // Add the panel to the frame

            // Add Submit, Quit, and Create Account Buttons
            buttonArray = new JPanel();

            submitButton = new JButton("Submit"); // Create a new button with the text "Submit"
            frame.getRootPane().setDefaultButton(submitButton); // Set this as the Default Button (Enter key presses it)
            c.gridx = 1; // Put this in Grid Column 1
            c.gridy = 3; // Put this in Grid Row 3
            c.fill = GridBagConstraints.CENTER; // Tell the JComponents to center themselves in the grid
            submitButton.addActionListener(actionListener); // Link the submit button to the ActionListener
            buttonArray.add(submitButton, c); // Add the Button to the Panel

            quitButton = new JButton("Quit"); // Create a new button with the text "Quit"
            c.gridx = 1; // Put this in Grid Column 1
            c.gridy = 3; // Put this in Grid Row 3
            c.fill = GridBagConstraints.CENTER; // Tell the JComponents to center themselves in the grid
            quitButton.addActionListener(actionListener); // Link the quit button to the ActionListener
            buttonArray.add(quitButton, c); // Add the button to the panel

            content.add(buttonArray, c); // Add the panel to the frame

            frame.setSize(600, 400); // Set size of frame
            frame.setLocationRelativeTo(null); // Center frame on screen
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close display but not program on close
            frame.setVisible(true); // Display the frame
        }
    }
    
     /** A GUI that operates for a specific store opened by the Seller
     *
     * @author CS180 Group 007
     * @version 12/12/2022
     */
    public class StoreGUI extends JComponent implements Runnable {
        JFrame frame; // The frame for the GUI
        User seller;
        JMenuBar menuBar; // The MenuBar for the top of the frame
        JButton refreshButton; // The Button to hit to update the displayed content
        Container content; // The content to be displayed in the frame
        GridBagConstraints c; // The constraints for placing JComponents on screen
        ArrayList<Availability> pendingRequests; // All requests that have been approved for the Buyer
        ArrayList<Store> stores; // All stores existing in the Marketplace
        ArrayList<String> calendars; // All calendars for a specific store
        JPanel panel1; // The panel used for searching for availabilities
        JPanel panel2; // The panel used for showing approved bookings
        JPanel calendarPanel; // A panel containing the Calendar entry area. To be displayed after store selection
        JButton submitButton; // The button to submit a store and calendar to get Availabilities
        ArrayList<Availability> calendarAvails; // All Availabilities for a certain calendar
        boolean storeChosen; // A boolean that determines if a store has been chosen by the user
        Store chosenStore;
        JButton addUIButton;
        ArrayList<JButton> pendingAcceptButtons; // List of buttons for each booking that, when hit, cancel the booking
        ArrayList<JButton> pendingDeclineButtons;
        JButton addButton;
        JTextField storeField;
        JTabbedPane tabbedPane;
        public StoreGUI(Seller user) {
            try {
                this.stores = user.getStores(); // Initialize the marketplace stores as the input values


            } catch (Exception e) {
                this.stores = new ArrayList<Store>();
            }
            calendars = new ArrayList<>(); // Initialize the store calendars as an empty ArrayList
            storeChosen = false; // Initially state a store has not been chosen
            calendarAvails = new ArrayList<>(); // Initialize availabilities for a calendar to an empty ArrayList
            pendingAcceptButtons = new ArrayList<>(); // Initialize the remove booking button ArrayList as empty
            pendingDeclineButtons = new ArrayList<>();
            pendingRequests = new ArrayList<>();


        }
        public void start() {

            SwingUtilities.invokeLater(new StoreGUI((Seller) currentUser));

        }
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == submitButton) {
                    frame.dispose();
                    chosenStore = (stores.get(storeInput.getSelectedIndex()));
                    SellerGUI gui = new SellerGUI(chosenStore);
                    gui.start();
                    panel1.removeAll();


                } else if (e.getSource() == addButton) {
                    frame.dispose();
                    Seller tempUser = (Seller) currentUser;
                    tempUser.addStore(storeField.getText());
                    currentUser = tempUser;
                    chosenStore = (stores.get(stores.size() - 1));
                    try {
                        sendNewRequest("updateUser");
                        oos.writeObject(currentUser);
                        oos.flush();

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    SellerGUI gui = new SellerGUI(chosenStore);
                    gui.start();
                } else if (e.getSource() == addUIButton) {
                    Starter.startNew();
                }
            }
        };
        // Method called by start() to initialize the GUI
        public void run() {

            frame = new JFrame("Seller Menu"); // Initialize the frame with the name "Buyer Menu"
            menuBar = new JMenuBar(); // Create a new menu bar
            addUIButton = new JButton("Add Connection"); // Create the Add Connection button
            addUIButton.setBackground(Color.WHITE); // Set the button Background to White
            addUIButton.addActionListener(actionListener); // Link the Add Connection Button to the ActionListener
            menuBar.add(addUIButton); // Add the add connection button to the menuBar
            frame.setJMenuBar(menuBar); // Link the menuBar to the frame
            frame.setJMenuBar(menuBar); // Link the menuBar to the frame
            content = frame.getContentPane(); // Initialize content as the content pane of the frame
            c = new GridBagConstraints(); // Initialize the GridBagConstraints

            tabbedPane = new JTabbedPane(); // Create a pane where tabs can be placed

            panel1 = new JPanel(); // Initialize the panel for viewing Availabilities
            panel1.setLayout(new GridBagLayout()); // Set the panel layout to GridBag
            panel2 = new JPanel();
            panel2.setLayout(new GridBagLayout());



            JPanel storePanel = new JPanel(new GridBagLayout()); // Create a new panel for searching stores

            JLabel storeText = new JLabel("Store: "); // Create a new label describing an input
            storeText.setFont(new Font("SansSerif", Font.PLAIN, 12)); // Set label font
            c.anchor = GridBagConstraints.CENTER; // Anchor the label to the center of the grid square
            c.fill = GridBagConstraints.CENTER; // Have the label fill from the center out
            c.weightx = 1; // Give the label a weight of 1 compared to others (prevents overlapping)
            c.gridy = 1; // Place the label in row 1
            c.gridx = 1; // Place the label in column 1
            storePanel.add(storeText, c); // Add the label to the store panel
            JLabel currLabel;
            currLabel = new JLabel("Choose which store you would like to view the pending requests for.");
            currLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            storeInput = new JComboBox<>();
            for (int i = 0; i < stores.size(); i++) {
                storeInput.addItem(stores.get(i).getName());
            }
            storeInput.setFont(new Font("SansSerif", Font.PLAIN, 12));
            c.fill = GridBagConstraints.NONE; // Choose not to fill the grid square
            c.gridy = 1; // place the dropdown in row 1
            c.gridx = 2; // place the dropdown in column 2
            storePanel.add(storeInput, c); // Add the dropdown to the storePanel
            c.gridx = 1; // Place the panel in column 1
            panel1.add(storePanel, c);
            JPanel buttonArray = new JPanel(new GridBagLayout());
            submitButton = new JButton("Submit");
            submitButton.addActionListener(actionListener);
            c.anchor = GridBagConstraints.LINE_END; // Place the button at the end of the line
            c.insets = new Insets(0, 0, 0, 30); // Move the button 30 pixels right
            c.gridy = 1; // Place the button in row 1
            c.gridx = 1; // Place the button in column 1
            buttonArray.add(submitButton, c); // Add the button to the panel for buttons
            addButton = new JButton("Add Store");
            addButton.addActionListener(actionListener);
            c.gridy = 1;
            c.gridx = 2;
            buttonArray.add(addButton, c);
            c.gridx = 1;
            c.gridy = 3; // place the panel in row 3


            c.ipadx = 0; // remove all horizontal padding
            c.insets = new Insets(0, 0, 0, 0); // reset the insets
            storeField = new JTextField(10);
            c.anchor = GridBagConstraints.CENTER; // Return the anchor to the center



            panel1.add(storeField, c);

            c.gridy = 4;

            panel1.add(buttonArray, c); // Add the button panel to the panel for searching Availabilities













            tabbedPane.addTab("Store Menu", null, panel1,
                    "Menu for Selecting or Adding Stores");



            frame.add(tabbedPane); // Add the tabbed panels to the frame
            frame.setSize(600, 400); // Set the size of the window
            frame.setLocationRelativeTo(null); // Place the window in the center of the screen
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close the window but continue execution on close
            frame.repaint(); // repaint the screen
            frame.setVisible(true); // Display the screen

        }

    }
    
     /** A GUI for any logged in Seller
     * @author CS180 Group 007
     * @version 12/12/2022
     */
    public class SellerGUI extends JComponent implements Runnable {
        Seller tempUser = (Seller) currentUser;
        JFrame frame; // The frame for the GUI
        User seller;
        JButton addUIButton;
        JMenuBar menuBar; // The MenuBar for the top of the frame
        JButton refreshButton; // The Button to hit to update the displayed content
        Container content; // The content to be displayed in the frame
        GridBagConstraints c; // The constraints for placing JComponents on screen
        ArrayList<Availability> pendingRequests; // All requests that have been approved for the Buyer
        ArrayList<Store> stores; // All stores existing in the Marketplace
        ArrayList<Calendar> calendars; // All calendars for a specific store
        JPanel panel1; // The panel used for searching for availabilities
        JPanel panel2; // The panel used for showing approved bookings
        JPanel calendarPanel; // A panel containing the Calendar entry area. To be displayed after store selection
        JButton submitButton; // The button to submit a store and calendar to get Availabilities
        ArrayList<Availability> calendarAvails; // All Availabilities for a certain calendar
        boolean storeChosen; // A boolean that determines if a store has been chosen by the user
        Store chosenStore;
        ArrayList<JButton> pendingAcceptButtons; // List of buttons for each booking that, when hit, cancel the booking
        ArrayList<JButton> pendingDeclineButtons;
        JButton addButton;
        JTextField storeField;
        JTabbedPane tabbedPane;
        JButton declineButton;

        JButton newCalButton;
        JButton allCalButton;
        JButton newAvailButton;
        JButton displayCalButton;
        JButton acceptButton;
        JButton backButton;
        JButton newCalendar;
        JTextField field1;
        JTextField field2;

        JTextField titleField;
        JTextField dateField;
        JTextField startField;
        JTextField endField;
        JTextField maxField;
        JButton availSubmitButton;
        Calendar chosenCalendar;

        public SellerGUI(Store chosenStore) {
            this.chosenStore = chosenStore;
            calendars = new ArrayList<>(); // Initialize the store calendars as an empty ArrayList
            storeChosen = false; // Initially state a store has not been chosen
            calendarAvails = new ArrayList<>(); // Initialize availabilities for a calendar to an empty ArrayList
            pendingAcceptButtons = new ArrayList<>(); // Initialize the remove booking button ArrayList as empty
            pendingDeclineButtons = new ArrayList<>();
            pendingRequests = new ArrayList<>();


        }
        public void start() {

            SwingUtilities.invokeLater(new SellerGUI(chosenStore));

        }
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (pendingAcceptButtons.contains(e.getSource())) {

                    int chosenIndex = pendingAcceptButtons.indexOf(e.getSource());


                    sendNewRequest("approve");
                    try {
                        oos.writeObject(chosenStore);
                        oos.writeObject(chosenIndex);
                        oos.flush();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    chosenStore.approveBooking(chosenIndex);

                    try {
                        sendNewRequest("updateUser");
                        oos.writeObject(currentUser);
                        oos.flush();

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }


                    pendingAcceptButtons = new ArrayList<>();
                    panel1.removeAll();
                    frame.repaint();

                    JLabel header = new JLabel("Your pending requests"); // Create a header describing displayed content
                    header.setFont(new Font("SansSerif", Font.BOLD, 14)); // Set header font
                    c.gridy = 0; // Place header in row 0
                    c.gridx = 1; // Place header in column 1
                    c.fill = GridBagConstraints.CENTER; // Center the label in the grid square
                    c.ipady = 10; // Leave 10 pixels of vertical separation around the header
                    // add the header to the approved request panel
                    panel1.add(header, c);

                    c = new GridBagConstraints(); // reset the GridBagConstraints

                    JLabel currLabel;
                    // For all the approved requests...
                    for (int i = 0; i < pendingRequests.size(); i++) {
                        // Create a label with the title, date, and start time of the booking

                        currLabel = new JLabel(pendingRequests.get(i).getTitle() + " | Date: " +
                                Integer.toString((pendingRequests.get(i).getDate())) + " Start: " +
                                pendingRequests.get(i).getStart());

                        // Set the font of the label
                        currLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        c.ipadx = 20; // Leave 20 pixels of separation vertically
                        c.gridy = i + 1; // Place the label in row i + 1
                        c.gridx = 1; // Place the label in column 1
                        c.fill = GridBagConstraints.CENTER;
                        c.ipady = 10; // Have the label center itself in the grid

                        panel1.add(currLabel, c); // Add the label to the panel for approved requests
                        JButton tempAcceptButton = new JButton("Accept");
                        tempAcceptButton.addActionListener(actionListener);
                        pendingAcceptButtons.add(tempAcceptButton);
                        c.gridx = 2;
                        c.anchor = GridBagConstraints.LINE_END;
                        c.fill = GridBagConstraints.NONE;
                        panel1.add(acceptButton, c);
                        declineButton = new JButton("Decline");
                        declineButton.addActionListener(actionListener);
                        pendingDeclineButtons.add(declineButton);
                        c.gridx = 3;
                        panel1.add(declineButton, c);


                    }
                } else if (pendingDeclineButtons.contains(e.getSource())) {
                    int chosenIndex = pendingDeclineButtons.indexOf(e.getSource());


                    sendNewRequest("decline");
                    try {
                        oos.writeObject(chosenStore);
                        oos.writeObject(chosenIndex);
                        oos.flush();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                    chosenStore.declineBooking(chosenIndex);

                    try {
                        sendNewRequest("updateUser");
                        oos.writeObject(currentUser);
                        oos.flush();

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }


                    pendingDeclineButtons = new ArrayList<>();
                    panel1.removeAll();
                    frame.repaint();

                    JLabel header = new JLabel("Your pending requests"); // Create a header describing displayed content
                    header.setFont(new Font("SansSerif", Font.BOLD, 14)); // Set header font
                    c.gridy = 0; // Place header in row 0
                    c.gridx = 1; // Place header in column 1
                    c.fill = GridBagConstraints.CENTER; // Center the label in the grid square
                    c.ipady = 10; // Leave 10 pixels of vertical separation around the header
                    // add the header to the approved request panel
                    panel1.add(header, c);

                    c = new GridBagConstraints(); // reset the GridBagConstraints

                    JLabel currLabel;
                    // For all the approved requests...
                    for (int i = 0; i < pendingRequests.size(); i++) {
                        // Create a label with the title, date, and start time of the booking

                        currLabel = new JLabel(pendingRequests.get(i).getTitle() + " | Date: " +
                                Integer.toString((pendingRequests.get(i).getDate())) + " Start: " +
                                pendingRequests.get(i).getStart());

                        // Set the font of the label
                        currLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        c.ipadx = 20; // Leave 20 pixels of separation vertically
                        c.gridy = i + 1; // Place the label in row i + 1
                        c.gridx = 1; // Place the label in column 1
                        c.fill = GridBagConstraints.CENTER;
                        c.ipady = 10; // Have the label center itself in the grid

                        panel1.add(currLabel, c); // Add the label to the panel for approved requests
                        JButton tempAcceptButton = new JButton("Accept");
                        tempAcceptButton.addActionListener(actionListener);
                        pendingAcceptButtons.add(tempAcceptButton);
                        c.gridx = 2;
                        c.anchor = GridBagConstraints.LINE_END;
                        c.fill = GridBagConstraints.NONE;
                        panel1.add(acceptButton, c);
                        declineButton = new JButton("Decline");
                        declineButton.addActionListener(actionListener);
                        pendingDeclineButtons.add(declineButton);
                        c.gridx = 3;
                        panel1.add(declineButton, c);
                    }
                } else if (e.getSource() == allCalButton) {

                    if (chosenStore.getCalendars().size() > 0) {
                        int x = 0;
                        panel2.removeAll();
                        frame.repaint();
                        JLabel currLabel;
                        for (int i = 0; i < chosenStore.getCalendars().size(); i++) {
                            currLabel = new JLabel(chosenStore.getCalendars().get(i).getName());
                            currLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
                            c.ipadx = 20; // Leave 20 pixels of separation vertically
                            c.gridy = i + 1; // Place the label in row i + 1
                            c.gridx = 1; // Place the label in column 1
                            c.fill = GridBagConstraints.CENTER;
                            c.ipady = 10; // Have the label center  in the grid
                            panel2.add(currLabel, c); // Add the label to the panel for approved requests
                            x = i;

                        }
                        backButton = new JButton("Back");
                        backButton.addActionListener(actionListener);
                        c.gridx = 0;
                        c.gridy = x + 2;
                        panel2.add(backButton, c);
                    } else {

                        panel2.removeAll();
                        frame.repaint();
                        JLabel currLabel;
                        currLabel = new JLabel("You do not have any calendars.");
                        c.ipadx = 20; // Leave 20 pixels of separation vertically
                        c.gridy = -1; // Place the label in row i + 1
                        c.gridx = 1; // Place the label in column 1
                        c.fill = GridBagConstraints.CENTER;
                        c.ipady = 10; // Have the label center itself in the grid
                        panel2.add(currLabel, c); // Add the label to the panel for approved requests
                        backButton = new JButton("Back");
                        backButton.addActionListener(actionListener);
                        c.gridx = -5;
                        c.gridy = -5;
                        panel2.add(backButton);


                    }
                } else if (e.getSource() == backButton) {
                    panel2.removeAll();
                    frame.repaint();
                    allCalButton = new JButton("View all calendars"); // Create a header describing displayed content
                    allCalButton.addActionListener(actionListener);
                    c.gridy = 0;

                    panel2.add(allCalButton, c);
                    // Create a header describing displayed content
                    displayCalButton = new JButton("Display calendar details");
                    displayCalButton.addActionListener(actionListener);

                    c.gridy = 1;

                    panel2.add(displayCalButton, c);
                    newCalButton = new JButton("Create new calendar"); // Create a header describing displayed content
                    newCalButton.addActionListener(actionListener);

                    c.gridy = 2;

                    panel2.add(newCalButton, c);

                    newAvailButton = new JButton("Add an availability"); // Create a header describing displayed content
                    newAvailButton.addActionListener(actionListener);


                    c.gridy = 3;

                    panel2.add(newAvailButton, c);






                    tabbedPane.addTab("Bookings Menu", null, panel1, "Menu for accepting pending bookings");
                    tabbedPane.addTab("Calendar Menu", null, panel2, "Menu for managing calendars.");

                    frame.add(tabbedPane);

                } else if (e.getSource() == displayCalButton) {
                    panel2.removeAll();
                    frame.repaint();
                    c.gridx = 0;
                    c.gridy = 0;
                    c.fill = (GridBagConstraints.CENTER);
                    int x = 0;
                    if (chosenStore.getCalendars().size() > 0) {
                        String currString = (chosenStore.getCalendars().get(0).display());
                        JLabel currLabel;
                        while (currString.contains("|")) {
                            c.anchor = GridBagConstraints.CENTER;
                            currLabel = new JLabel(currString.substring(0, currString.indexOf("|")), JLabel.CENTER);
                            c.gridy = x;
                            x++;
                            currString = (currString.substring(currString.indexOf("|") + 1, currString.length()));
                            panel2.add(currLabel, c);


                        }
                    } else {
                        String currString = "No calendars to display.";
                        JLabel currLabel = new JLabel(currString, JLabel.CENTER);
                        panel2.add(currLabel, c);
                    }

                    frame.repaint();
                    backButton = new JButton("Back");
                    backButton.addActionListener(actionListener);
                    c.gridx = 0;
                    c.gridy = x + 1;
                    panel2.add(backButton, c);

                } else if (e.getSource() == newCalendar) {
                    chosenStore.addCalendar(field1.getText(), field2.getText());
                    try {
                        sendNewRequest("updateUser");
                        oos.writeObject(currentUser);
                        oos.flush();

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    panel2.removeAll();
                    frame.repaint();
                    allCalButton = new JButton("View all calendars"); // Create a header describing displayed content
                    allCalButton.addActionListener(actionListener);
                    c.gridy = 0;

                    panel2.add(allCalButton, c);
                    
                    // Create a header describing displayed content
                    displayCalButton = new JButton("Display calendar details");
                    displayCalButton.addActionListener(actionListener);

                    c.gridy = 1;

                    panel2.add(displayCalButton, c);
                    newCalButton = new JButton("Create new calendar"); // Create a header describing displayed content
                    newCalButton.addActionListener(actionListener);

                    c.gridy = 2;

                    panel2.add(newCalButton, c);

                    newAvailButton = new JButton("Add an availability"); // Create a header describing displayed content
                    newAvailButton.addActionListener(actionListener);


                    c.gridy = 3;

                    panel2.add(newAvailButton, c);






                    tabbedPane.addTab("Bookings Menu", null, panel1, "Menu for accepting pending bookings");
                    tabbedPane.addTab("Calendar Menu", null, panel2, "Menu for managing calendars.");

                    frame.add(tabbedPane);

                } else if (e.getSource() == newCalButton) {
                    panel2.removeAll();
                    panel2.repaint();
                    JLabel currLabel = new JLabel("Calendar Name");
                    field1 = new JTextField(10);
                    c.gridx = 0;
                    c.gridy = 0;
                    panel2.add(currLabel, c);
                    c.gridx = 1;
                    panel2.add(field1, c);
                    currLabel = new JLabel("Calendar Description");
                    field2 = new JTextField(10);
                    c.gridx = 0;
                    c.gridy = 1;
                    panel2.add(currLabel, c);
                    c.gridx = 1;
                    panel2.add(field2, c);
                    newCalendar = new JButton("Add Calendar");
                    c.gridy = 2;
                    c.gridx = 0;
                    panel2.add(newCalendar, c);
                    newCalendar.addActionListener(actionListener);
                    backButton = new JButton("Back");
                    backButton.addActionListener(actionListener);
                    c.gridx = -5;
                    c.gridy = -5;
                    panel2.add(backButton);


                } else if (e.getSource() == newAvailButton) {
                    panel2.removeAll();
                    frame.repaint();
                    calendars = chosenStore.getCalendars();

                    calendarInput = new JComboBox<String>();
                    for (int i = 0; i < calendars.size(); i++) {
                        calendarInput.addItem(calendars.get(i).getName());
                    }

                    JLabel currLabel;
                    currLabel = new JLabel("Choose a calendar: ");
                    c.gridx = 0;
                    c.gridy = 0;
                    panel2.add(currLabel, c);
                    c.gridx = 1;
                    panel2.add(calendarInput, c);
                    c.gridy = 1;
                    c.gridx = 0;


                    currLabel = new JLabel("Enter a title: ");
                    titleField = new JTextField(10);
                    panel2.add(currLabel, c);
                    c.gridx = 1;
                    panel2.add(titleField, c);
                    c.gridy = 2;
                    c.gridx = 0;
                    currLabel = new JLabel("Enter a day of the month: ");
                    panel2.add(currLabel, c);
                    dateField = new JTextField(10);
                    c.gridx = 1;
                    panel2.add(dateField, c);
                    c.gridx = 0;
                    c.gridy = 3;
                    currLabel = new JLabel("Enter the start time: ");
                    panel2.add(currLabel, c);
                    c.gridx = 1;
                    startField = new JTextField(10);
                    panel2.add(startField, c);
                    c.gridy = 4;
                    c.gridx = 0;
                    currLabel = new JLabel("Enter the end time: ");
                    panel2.add(currLabel, c);
                    c.gridx = 1;
                    endField = new JTextField(10);
                    panel2.add(endField, c);
                    c.gridy = 5;
                    c.gridx = 0;
                    currLabel = new JLabel("Enter the max attendees: ");
                    panel2.add(currLabel, c);
                    c.gridx = 1;
                    maxField = new JTextField(10);
                    panel2.add(maxField, c);
                    c.gridy = 6;
                    c.gridx = 0;
                    availSubmitButton = new JButton("Submit");
                    availSubmitButton.addActionListener(actionListener);
                    panel2.add(availSubmitButton, c);
                    backButton = new JButton("Back");
                    backButton.addActionListener(actionListener);
                    c.gridx = 1;
                    c.gridy = 6;
                    panel2.add(backButton);
                    frame.repaint();




                } else if (e.getSource() == availSubmitButton) {
                    chosenCalendar = calendars.get(calendarInput.getSelectedIndex());
                    try {
                        chosenCalendar.addAvailability(new Availability(titleField.getText(), 
                                                                        Integer.parseInt(dateField.getText()), 
                                                                        startField.getText(), endField.getText(), 
                                                                        Integer.parseInt(maxField.getText()), 
                                                                        chosenCalendar));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error in inputs.", "Marketplace Calendar",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    try {
                        sendNewRequest("updateUser");
                        oos.writeObject(currentUser);
                        oos.flush();

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error.", "Marketplace Calendar",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    panel2.removeAll();
                    frame.repaint();

                    allCalButton = new JButton("View all calendars"); // Create a header describing displayed content
                    allCalButton.addActionListener(actionListener);
                    c.gridy = 0;

                    panel2.add(allCalButton, c);
                    
                    // Create a header describing displayed content
                    displayCalButton = new JButton("Display calendar details");
                    displayCalButton.addActionListener(actionListener);

                    c.gridy = 1;

                    panel2.add(displayCalButton, c);
                    newCalButton = new JButton("Create new calendar"); // Create a header describing displayed content
                    newCalButton.addActionListener(actionListener);

                    c.gridy = 2;

                    panel2.add(newCalButton, c);

                    newAvailButton = new JButton("Add an availability"); // Create a header describing displayed content
                    newAvailButton.addActionListener(actionListener);


                    c.gridy = 3;

                    panel2.add(newAvailButton, c);






                    tabbedPane.addTab("Bookings Menu", null, panel1, "Menu for accepting pending bookings");
                    tabbedPane.addTab("Calendar Menu", null, panel2, "Menu for managing calendars.");

                    frame.add(tabbedPane);
                } else if (e.getSource() == addUIButton) {
                    Starter.startNew();
                }

            }
        };
        public void run() {


            frame = new JFrame("Seller Menu"); // Initialize the frame with the name "Buyer Menu"
            menuBar = new JMenuBar(); // Create a new menu bar
            addUIButton = new JButton("Add Connection"); // Create the Add Connection button
            addUIButton.setBackground(Color.WHITE); // Set the button Background to White
            addUIButton.addActionListener(actionListener); // Link the Add Connection Button to the ActionListener
            menuBar.add(addUIButton); // Add the add connection button to the menuBar
            frame.setJMenuBar(menuBar); // Link the menuBar to the frame
            frame.setJMenuBar(menuBar); // Link the menuBar to the frame
            content = frame.getContentPane(); // Initialize content as the content pane of the frame
            c = new GridBagConstraints(); // Initialize the GridBagConstraints

            tabbedPane = new JTabbedPane(); // Create a pane where tabs can be placed

            panel1 = new JPanel(); // Initialize the panel for viewing Availabilities
            panel1.setLayout(new GridBagLayout()); // Set the panel layout to GridBag
            panel2 = new JPanel();
            panel2.setLayout(new GridBagLayout());

            pendingRequests = chosenStore.getPendingBookings();
            JLabel header = new JLabel("Your pending requests"); // Create a header describing displayed content
            header.setFont(new Font("SansSerif", Font.BOLD, 14)); // Set header font
            c.gridy = 0; // Place header in row 0
            c.gridx = 1; // Place header in column 1
            c.fill = GridBagConstraints.CENTER; // Center the label in the grid square
            c.ipady = 10; // Leave 10 pixels of vertical separation around the header
            panel1.add(header, c); // add the header to the approved request panel
            JLabel currLabel;
            c = new GridBagConstraints(); // reset the GridBagConstraints
            // For all the approved requests...
            for (int i = 0; i < pendingRequests.size(); i++) {
                // Create a label with the title, date, and start time of the booking

                currLabel = new JLabel(pendingRequests.get(i).getTitle() + " | Date: " +
                        Integer.toString((pendingRequests.get(i).getDate())) + " | Start: " +
                        pendingRequests.get(i).getStart());

                // Set the font of the label
                currLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                c.ipadx = 20; // Leave 20 pixels of separation vertically
                c.gridy = i + 1; // Place the label in row i + 1
                c.gridx = 1; // Place the label in column 1
                c.fill = GridBagConstraints.CENTER;
                c.ipady = 10; // Have the label center itself in the grid

                panel1.add(currLabel, c); // Add the label to the panel for approved requests

                acceptButton = new JButton("Accept");
                acceptButton.addActionListener(actionListener);
                pendingAcceptButtons.add(acceptButton);
                c.gridx = 2;
                c.anchor = GridBagConstraints.LINE_END;
                c.fill = GridBagConstraints.NONE;
                panel1.add(acceptButton, c);
                declineButton = new JButton("Decline");
                declineButton.addActionListener(actionListener);
                pendingDeclineButtons.add(declineButton);
                c.gridx = 3;
                panel1.add(declineButton, c);
            }
            allCalButton = new JButton("View all calendars"); // Create a header describing displayed content
            allCalButton.addActionListener(actionListener);
            c.gridy = 0;

            panel2.add(allCalButton, c);
            displayCalButton = new JButton("Display calendar details"); // Create a header describing displayed content
            displayCalButton.addActionListener(actionListener);

            c.gridy = 1;

            panel2.add(displayCalButton, c);
            newCalButton = new JButton("Create new calendar"); // Create a header describing displayed content
            newCalButton.addActionListener(actionListener);

            c.gridy = 2;

            panel2.add(newCalButton, c);

            newAvailButton = new JButton("Add an availability"); // Create a header describing displayed content
            newAvailButton.addActionListener(actionListener);
            c.gridy = 3;
            panel2.add(newAvailButton, c);
            tabbedPane.addTab("Bookings Menu", null, panel1, "Menu for accepting pending bookings");
            tabbedPane.addTab("Calendar Menu", null, panel2, "Menu for managing calendars.");

            frame.add(tabbedPane);
            frame.setSize(600, 400); // Set the size of the window
            frame.setLocationRelativeTo(null); // Place the window in the center of the screen
            // Close the window but continue execution on close
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
            frame.repaint(); // repaint the screen
            frame.setVisible(true); // Display the screen
        }
    }


     /** A GUI for any logged in Buyer
     *
     * @author CS180 Group 007
     * @version 12/12/2022
     */
    public class BuyerGUI extends JComponent implements Runnable {
        JFrame frame; // The frame for the GUI
        JMenuBar menuBar; // The MenuBar for the top of the frame
        JButton refreshButton; // The Button to hit to update the displayed content
        JButton addUIButton; // The button that spawns a new Starter instance to add a new GUI concurrently
        Container content; // The content to be displayed in the frame
        GridBagConstraints c; // The constraints for placing JComponents on screen
        ArrayList<Availability> approvedRequests; // All requests that have been approved for the Buyer
        ArrayList<String> stores; // All stores existing in the Marketplace
        ArrayList<String> calendars; // All calendars for a specific store
        JPanel panel1; // The panel used for searching for availabilities
        JPanel panel2; // The panel used for showing approved bookings
        JPanel calendarPanel; // A panel containing the Calendar entry area. To be displayed after store selection
        JButton submitButton; // The button to submit a store and calendar to get Availabilities
        ArrayList<Availability> calendarAvails; // All Availabilities for a certain calendar
        boolean storeChosen; // A boolean that determines if a store has been chosen by the user
        ArrayList<JButton> approvedRemoveButtons; // List of buttons for each booking that, when hit, cancel the booking

        // ActionListener for all button presses
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If submit button pressed...
                if (e.getSource() == submitButton) {
                    // If the user has chosen a store already...
                    if (storeChosen) {
                        // Get all Availabilities for a calendar and open the AvailViewer class to display them
                        calendarAvails = getAvails((String) storeInput.getSelectedItem(),
                                (String) calendarInput.getSelectedItem());
                        AvailViewer availPopup = new AvailViewer(calendarAvails);
                        availPopup.start();
                    } else {
                        // Otherwise, display the entry box to select a calendar
                        c.gridx = 1; // Put this in column 1
                        c.gridy = 2; // Put this in row 2
                        calendars = getCalendars((String) storeInput.getSelectedItem()); // Get calendars for the store
                        calendarInput.removeAllItems(); // Clear any calendar options out
                        for (int i = 0; i < calendars.size(); i++) {
                            calendarInput.addItem(calendars.get(i)); // Add each calendar to the list of options
                        }
                        panel1.add(calendarPanel, c); // add the calendar panel to the screen
                        frame.repaint(); // repaint the screen
                        storeChosen = true; // Indicate that a store has been chosen
                    }
                } else if (approvedRemoveButtons.contains(e.getSource())) {
                    // If the selected button is one of the remove a booking buttons...
                    int chosenIndex = approvedRemoveButtons.indexOf(e.getSource()); // store the index the button is at
                    sendNewRequest("cancelRequest"); // Open request to cancel the booking
                    try {
                        // Send the server the Availability requesting to be cancelled
                        oos.writeObject(approvedRequests.get(chosenIndex));
                        oos.flush();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    // remove the request from the ArrayList
                    approvedRequests.remove(chosenIndex);
                    // reset the button ArrayList
                    approvedRemoveButtons = new ArrayList<>();
                    c = new GridBagConstraints();
                    // Clear the approved booking panel
                    panel2.removeAll();
                    // Add the label back to the panel
                    JLabel header = new JLabel("Your approved requests");
                    header.setFont(new Font("SansSerif", Font.BOLD, 18));
                    c.gridy = 0; // Put the label in row 0
                    c.gridx = 1; // Put the label in column 1
                    c.fill = GridBagConstraints.CENTER; // Have the label center itself in the grid
                    c.ipady = 10; // Leave 10 pixels of separation vertically
                    panel2.add(header, c); // Add the label to the panel
                    // For all the approved bookings...
                    for (int i = 0; i < approvedRequests.size(); i++) {
                        // Create a label with the title, date, and start time of the booking
                        JLabel currLabel = new JLabel(approvedRequests.get(i).getTitle() + " | Date: " +
                                approvedRequests.get(i).getDate() + " Start: " +
                                approvedRequests.get(i).getStart());
                        // Set the font of the label
                        currLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        c.ipadx = 20; // Leave 20 pixels of separation vertically
                        c.gridy = i + 1; // Place the label in row i + 1
                        c.gridx = 1; // Place the label in column 1
                        c.fill = GridBagConstraints.CENTER;
                        c.ipady = 10; // Have the label center itself in the grid
                        panel2.add(currLabel, c); // Add the label to the panel
                        JButton removeButton = new JButton("Cancel"); // Create a button to cancel the booking
                        removeButton.addActionListener(actionListener); // Link the button to the actionListener
                        approvedRemoveButtons.add(removeButton); // Add the button to the ArrayList
                        c.gridx = 2; // Place the button in column 2
                        c.anchor = GridBagConstraints.LINE_END; // Anchor the button to the end of the line
                        c.fill = GridBagConstraints.NONE; // Don't stretch the button to fit the grid
                        panel2.add(removeButton, c); // Add the button to the panel
                    }
                    frame.repaint(); // repaint the screen
                } else if (e.getSource().equals(refreshButton)) {
                    // If the refresh button is hit...
                    frame.dispose(); // remove the screen
                    sendNewRequest("allStores"); // Open a new request to get all the stores in the marketplace
                    String store = null;
                    try {
                        store = (String) ois.readObject(); // read the first store
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    stores = new ArrayList<>(); // reset the stores ArrayList to be empty
                    while (true) {
                        // end the loop when the store named "EOT" is passed
                        if (store.equals("EOT")) {
                            break;
                        }
                        if (store != null) {
                            stores.add(store); // Add each store to the ArrayList
                        }
                        try {
                            store = (String) ois.readObject(); // Read the next store
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    sendNewRequest("viewAll"); // Open a new request to get currently approved bookings
                    Availability availability;
                    try {
                        availability = (Availability) ois.readObject(); // Read the first availability
                    } catch (Exception v) {
                        throw new RuntimeException(v);
                    }
                    approvedRequests = new ArrayList<>(); // Reset the ArrayList to be empty
                    while (true) {
                        // end the loop when the Availability titled "EOT" is passed
                        if (availability.getTitle().equals("EOT")) {
                            break;
                        }
                        if (availability != null) {
                            approvedRequests.add(availability); // Add each Availability to the ArrayList
                        }
                        try {
                            availability = (Availability) ois.readObject(); // Read the next store
                        } catch (IOException x) {
                            throw new RuntimeException(x);
                        } catch (ClassNotFoundException x) {
                            throw new RuntimeException(x);
                        }
                    }

                    // If a store has been chosen...
                    if (storeChosen) {
                        // Send a new request to get all calendars and save them in the ArrayList
                        calendars = getCalendars((String) storeInput.getSelectedItem());
                        calendarInput.removeAllItems(); // Clear the calendar dropdown
                        for (int i = 0; i < calendars.size(); i++) {
                            calendarInput.addItem(calendars.get(i)); // Add each calendar name to the dropdown
                        }
                    }

                    frame.repaint(); // repaint the screen
                    frame.getRootPane().setDefaultButton(submitButton); // re-link the submit button to the Enter key
                    frame.setVisible(true); // Display the new screen
                } else if (e.getSource() == addUIButton) {
                    // if the add connection button is hit, spawn a new GUI
                    Starter.startNew();
                }
            }
        };

        // Constructor called by start()
        public BuyerGUI(ArrayList<Availability> approvedRequests, ArrayList<String> stores) {
            this.approvedRequests = approvedRequests; // Initialize the approved bookings as the input values
            this.stores = stores; // Initialize the marketplace stores as the input values
            calendars = new ArrayList<>(); // Initialize the store calendars as an empty ArrayList
            storeChosen = false; // Initially state a store has not been chosen
            calendarAvails = new ArrayList<>(); // Initialize availabilities for a calendar to an empty ArrayList
            approvedRemoveButtons = new ArrayList<>(); // Initialize the remove booking button ArrayList as empty
        }

        // Method to be run to start the GUI on the Event Dispatch Thread
        public void start() {
            SwingUtilities.invokeLater(new BuyerGUI(approvedRequests, stores));
        }

        // Method called by start() to initialize the GUI
        public void run() {
            frame = new JFrame("Buyer Menu"); // Initialize the frame with the name "Buyer Menu"
            menuBar = new JMenuBar(); // Create a new menu bar

            refreshButton = new JButton("Refresh"); // Create a button with the text "Refresh"
            refreshButton.setBackground(Color.WHITE); // Set the button background to white
            refreshButton.addActionListener(actionListener); // Link the button to the actionListener
            menuBar.add(refreshButton); // Add the button to the menuBar
            addUIButton = new JButton("Add Connection"); // Create the Add Connection button
            addUIButton.setBackground(Color.WHITE); // Set the button Background to White
            addUIButton.addActionListener(actionListener); // Link the Add Connection Button to the ActionListener
            menuBar.add(addUIButton); // Add the add connection button to the menuBar
            frame.setJMenuBar(menuBar); // Link the menuBar to the frame
            content = frame.getContentPane(); // Initialize content as the content pane of the frame
            c = new GridBagConstraints(); // Initialize the GridBagConstraints

            JTabbedPane tabbedPane = new JTabbedPane(); // Create a pane where tabs can be placed

            panel1 = new JPanel(); // Initialize the panel for viewing Availabilities
            panel1.setLayout(new GridBagLayout()); // Set the panel layout to GridBag

            panel2 = new JPanel(); // Initialize the panel for viewing approved bookings
            panel2.setLayout(new GridBagLayout()); // Set the panel layout to GridBag

            JLabel header = new JLabel("Your approved requests"); // Create a header describing displayed content
            header.setFont(new Font("SansSerif", Font.BOLD, 18)); // Set header font
            c.gridy = 0; // Place header in row 0
            c.gridx = 1; // Place header in column 1
            c.fill = GridBagConstraints.CENTER; // Center the label in the grid square
            c.ipady = 10; // Leave 10 pixels of vertical separation around the header
            panel2.add(header, c); // add the header to the approved request panel

            c = new GridBagConstraints(); // reset the GridBagConstraints

            // For all the approved requests...
            for (int i = 0; i < approvedRequests.size(); i++) {
                // Create a label with the title, date, and start time of the booking
                JLabel currLabel = new JLabel(approvedRequests.get(i).getTitle() + " | Date: " +
                        approvedRequests.get(i).getDate() + " Start: " +
                        approvedRequests.get(i).getStart());
                // Set the font of the label
                currLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                c.ipadx = 20; // Leave 20 pixels of separation vertically
                c.gridy = i + 1; // Place the label in row i + 1
                c.gridx = 1; // Place the label in column 1
                c.fill = GridBagConstraints.CENTER;
                c.ipady = 10; // Have the  label center itself in the grid
                panel2.add(currLabel, c); // Add the label to the panel for approved requests
                JButton removeButton = new JButton("Cancel"); // Create a button to cancel the booking
                removeButton.addActionListener(actionListener); // Link the button to the actionListener
                approvedRemoveButtons.add(removeButton); // Add the button to the ArrayList
                c.gridx = 2; // Place the button in column 2
                c.anchor = GridBagConstraints.LINE_END; // Anchor the button to the end of the line
                c.fill = GridBagConstraints.NONE; // Don't stretch the button to fit the grid
                panel2.add(removeButton, c); // Add the button to the panel for approved requests
            }

            c = new GridBagConstraints(); // reset the GridBagConstraints

            // Create new label to help users understand content
            JLabel welcomeMessage = new JLabel("Enter a Store To Search Availabilities");
            welcomeMessage.setFont(new Font("SansSerif", Font.BOLD, 18)); // Set font of the label
            c.fill = GridBagConstraints.CENTER; // Center the label in its grid square
            c.ipady = 20; // Leave 20 pixels of vertical separation for the label
            c.gridx = 1; // Place the label in column 1
            c.gridy = 0; // Place the label in row 0

            panel1.add(welcomeMessage, c); // Add the label to the panel for searching Availabilities

            JPanel storePanel = new JPanel(new GridBagLayout()); // Create a new panel for searching stores

            JLabel storeText = new JLabel("Store: "); // Create a new label describing an input
            storeText.setFont(new Font("SansSerif", Font.PLAIN, 12)); // Set label font
            c.anchor = GridBagConstraints.CENTER; // Anchor the label to the center of the grid square
            c.fill = GridBagConstraints.CENTER; // Have the label fill from the center out
            c.weightx = 1; // Give the label a weight of 1 compared to others (prevents overlapping)
            c.gridy = 1; // Place the label in row 1
            c.gridx = 1; // Place the label in column 1
            storePanel.add(storeText, c); // Add the label to the store panel

            storeInput = new JComboBox<>(); // Create a new dropdown for selecting a store

            // For each store in the ArrayList...
            for (int i = 0; i < stores.size(); i++) {
                storeInput.addItem(stores.get(i)); // Add the store to the options for the dropdown
            }
            storeInput.setFont(new Font("SansSerif", Font.PLAIN, 12)); // Set the font for the dropdown
            c.fill = GridBagConstraints.NONE; // Choose not to fill the grid square
            c.gridy = 1; // place the dropdown in row 1
            c.gridx = 2; // place the dropdown in column 2
            storePanel.add(storeInput, c); // Add the dropdown to the storePanel
            c.gridx = 1; // Place the panel in column 1
            panel1.add(storePanel, c); // Add the panel to the panel for searching Availabilities

            calendarPanel = new JPanel(new GridBagLayout()); // Initialize a new panel with a GridBagLayout
            JLabel calendarText = new JLabel("Calendar: "); // Create a new label describing an input
            calendarText.setFont(new Font("SansSerif", Font.PLAIN, 12)); // Set label font
            c.anchor = GridBagConstraints.CENTER; // Anchor the label to the center of the Grid Square
            c.fill = GridBagConstraints.CENTER; // Fill the square from the center
            c.weightx = 1; // Give the label a weight of 1 compared to others (prevents overlapping)
            c.gridy = 1; // Place the label in row 1
            c.gridx = 1; // Place the label in column 1
            calendarPanel.add(calendarText, c); // Add the label to the calendar panel

            calendarInput = new JComboBox<>(); // Create a new dropdown for selecting a calendar

            // For each calendar in the ArrayList...
            for (int i = 0; i < calendars.size(); i++) {
                calendarInput.addItem(calendars.get(i)); // Add the calendar to the dropdown
            }
            calendarInput.setFont(new Font("SansSerif", Font.PLAIN, 12)); // Set the dropdown font
            c.fill = GridBagConstraints.NONE; // Choose not to fill the Grid Square with the dropdown
            c.gridy = 1; // Place the dropdown in row 1
            c.gridx = 2; // Place the dropdown in column 2
            calendarPanel.add(calendarInput, c); // Add the dropdown to the calendar panel

            // NOTE: The calendarPanel is not displayed until a store has been chosen and the submit button is hit

            JPanel buttonArray = new JPanel(new GridBagLayout()); // Create a new panel for holding buttons
            submitButton = new JButton("Submit"); // Create a new button with the text "Submit"
            submitButton.addActionListener(actionListener); // Link the button to the actionListener
            c.anchor = GridBagConstraints.LINE_END; // Place the button at the end of the line
            c.insets = new Insets(0, 0, 0, 30); // Move the button 30 pixels right
            c.gridy = 1; // Place the button in row 1
            c.gridx = 1; // Place the button in column 1
            buttonArray.add(submitButton, c); // Add the button to the panel for buttons
            c.gridy = 3; // place the panel in row 3
            panel1.add(buttonArray, c); // Add the button panel to the panel for searching Availabilities
            c.anchor = GridBagConstraints.CENTER; // Return the anchor to the center
            c.ipadx = 0; // remove all horizontal padding
            c.insets = new Insets(0, 0, 0, 0); // reset the insets

            // Add the panel for searching availabilities as a tab titled "Calendar and Request Menu"
            tabbedPane.addTab("Calendar and Request Menu", null, panel1,
                    "Search for Available Bookings");
            // Add the panel for viewing approved bookings as a tab titled "Approved Appointments"
            tabbedPane.addTab("Approved Appointments", null, panel2,
                    "View approved booking requests");

            frame.add(tabbedPane); // Add the tabbed panels to the frame
            frame.getRootPane().setDefaultButton(submitButton); // Link the "Submit" button to the Enter key
            frame.setSize(600, 400); // Set the size of the window
            frame.setLocationRelativeTo(null); // Place the window in the center of the screen
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close the window but continue execution on close
            frame.repaint(); // repaint the screen
            frame.setVisible(true); // Display the screen
        }
    }

    /** A GUI to display all availabilities in a Store to a Buyer
     *
     * @author CS180 Group 007
     * @version 12/12/2022
     */
    public class AvailViewer extends JComponent implements Runnable {
        JFrame frame; // The frame to be displayed
        Container content; // The content pane of the frame
        GridBagConstraints c; // The GridBagConstraints for placing JComponents
        ArrayList<Availability> avails; // The ArrayList of Availabilities to be displayed
        ArrayList<JButton> bookingButtons; // The buttons used to request a booking
        ArrayList<JPanel> tempPanels; // And ArrayList of panels for each availability
        JScrollPane availPane; // The scrolling pane all the Availabilities are placed into
        JButton refreshButton; // The button used to refresh the list TO DO: Make this work
        JPanel innerScrollPanel; // The panel within the scrollpane
        JLabel noAvails; // A label to be displayed if no Availabilities exist for a store

        // An ActionListener to handle all button presses
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If one of the request buttons is hit...
                if (bookingButtons.contains(e.getSource())) {
                    // Store the chosen index
                    int chosenIndex = bookingButtons.indexOf(e.getSource());
                    sendNewRequest("addBooking"); // Open a new request from the server
                    try {
                        // Write the name of the store this is from
                        oos.writeObject(storeInput.getSelectedItem());
                        oos.flush();
                        // Write the name of the calendar this is from
                        oos.writeObject(calendarInput.getSelectedItem());
                        oos.flush();
                        // Write the index of the Availability chosen
                        oos.writeObject(chosenIndex);
                        oos.flush();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    String result; // Increase scope of the result from the server
                    try {
                        result = (String) ois.readObject(); // read whether the request was successful or not
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }

                    // If the request was successful
                    if (result.equals("Success")) {
                        // Refresh the list of available bookings
                        updateBookings();
                        // Display confirmation of success to the user
                        JOptionPane.showMessageDialog(null,
                                "Successfully Requested a Booking", "Marketplace Calendar",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // If unsuccessful, inform the User
                        JOptionPane.showMessageDialog(null,
                                "We're Sorry, that event is full", "Marketplace Calendar",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        };

        // Constructor to be called by the start() method
        public AvailViewer(ArrayList<Availability> avails) {
            this.avails = avails; // Initialize the list of availabilities to the input value
            bookingButtons = new ArrayList<>(); // Initialize the booking buttons as an empty ArrayList
            tempPanels = new ArrayList<>(); // Initialize the booking panels as an empty ArrayList
        }

        // void method called by the action listener to refresh the list of available bookings
        public void updateBookings() {
            // Clear the scroll panel of all contents
            innerScrollPanel.removeAll();
            // If more than one availability exists...
            if (tempPanels.size() > 0) {
                // for each booking panel...
                for (int i = 0; i < tempPanels.size(); i++) {
                    c.anchor = GridBagConstraints.CENTER; // Fill the grid square starting in the center
                    c.gridy = i; // Place this in row i
                    innerScrollPanel.add(tempPanels.get(i), c); // Add the booking panel to panel inside the scrollPane
                }
                content.removeAll(); // clear the frame content
                innerScrollPanel.repaint(); // repaint the panel inside the ScrollPane
                availPane = new JScrollPane(innerScrollPanel); // reset the ScrollPane to contain the innerScrollPanel
                c.fill = GridBagConstraints.BOTH; // Fill the grid square vertically and horizontally
                c.anchor = GridBagConstraints.CENTER; // Place the pane in the center of the grid
                c.gridx = 1; // Place the pane in column 1
                c.gridy = 1; // Place the pane in row 1
                content.add(availPane, c); // Add the ScrollPane back to the screen
            } else {
                // If there are no availabilities remaining...
                c.anchor = GridBagConstraints.CENTER; // Place the label in the center of the grid
                c.gridy = 0; // Place the label in row 0
                JPanel noAvailPanel = new JPanel(new GridBagLayout()); // Create a new Panel for the label
                noAvails = new JLabel("No Availabilities for this Calendar right now. " +
                        "Please check again later"); // Create a label informing the user there are no Availabilities
                noAvails.setFont(new Font("SansSerif", Font.BOLD, 12)); // Set label font
                noAvailPanel.add(noAvails, c); // Add the label to the panel
                innerScrollPanel.add(noAvails, c); // Add the panel to the panel inside the ScrollPane
                content.removeAll(); // Clear the screen of all content
                availPane.revalidate(); // reset the scrollPane
                availPane.repaint(); // repaint the ScrollPane
                c.fill = GridBagConstraints.BOTH; // Fill the grid square both horizontally and vertically
                c.anchor = GridBagConstraints.CENTER; // Place the pane in the center of the grid square
                c.gridx = 1; // Place the pane in column 1
                c.gridy = 1; // Place the pane in row 1
                content.add(availPane, c); // add the scrollPane to the screen
            }
            c.fill = GridBagConstraints.CENTER; // Fill the grid square from the center
            c.gridy = 2; // Place the button in row 2
            content.add(refreshButton, c); // Add the refresh button to the screen
            content.repaint(); // repaint the screen
            frame.pack(); // pack the frame to fit the content size
            frame.repaint(); // repaint the frame
        }

        // method to be called to start the AvailViewer on the Event Dispatch Thread
        public void start() {
            SwingUtilities.invokeLater(new AvailViewer(avails));
        }

        // Run method called by start() to initially display the GUI
        @Override
        public void run() {
            frame = new JFrame("Availabilities List"); // Create a new frame with the name "Availabilities List"
            content = frame.getContentPane(); // Set content to the contentPane of the frame
            content.setLayout(new GridBagLayout()); // Set the content layout to GridBag
            c = new GridBagConstraints(); // Initialize the grid bag constraints

            innerScrollPanel = new JPanel(new GridBagLayout()); // Declare a new panel to go within the ScrollPane
            // For each availability...
            for (int i = 0; i < avails.size(); i++) {
                JPanel loopPanel = new JPanel(new GridBagLayout()); // Initialize a new panel with a grid bag layout
                loopPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Give the panel a black border
                Availability currentAvail = avails.get(i); // create a local variable that is the current Availability
                // Create a new String describing the Availability
                String description = String.format("%s - Date: %d - from %s to %s", currentAvail.getTitle(),
                        currentAvail.getDate(), currentAvail.getStart(), currentAvail.getEnd());
                JLabel tempLabel = new JLabel(description); // Create a new label using the String description
                tempLabel.setFont(new Font("SansSerif", Font.BOLD, 12)); // Set label font
                JButton tempButton = new JButton("Request"); // Create a new button to request this availability
                tempButton.addActionListener(actionListener); // Link the button to the actionListener
                bookingButtons.add(tempButton); // Add the button to the ArrayList for later
                loopPanel.setPreferredSize(new Dimension(525, 50)); // Set the size of the panel
                c.ipadx = 20; // Leave 20 pixels of space horizontally
                c.fill = GridBagConstraints.CENTER; // Fill the grid square from the center
                c.anchor = GridBagConstraints.LINE_START; // Place the label at the beginning of the row
                c.gridx = 1; // Place the label in column 1
                c.gridy = 1; // Place the label in row 1
                loopPanel.add(tempLabel, c); // Add the label to the panel
                c.gridx = 2; // Place the Strut in column 2
                loopPanel.add(Box.createHorizontalStrut(20), c); // Add strut (empty space of 20 px) to the panel
                c.anchor = GridBagConstraints.LINE_END; // Anchor the button to the end of the row
                c.gridx = 3; // Place the button in column 3
                loopPanel.add(tempButton, c); // Add the button to the panel
                tempPanels.add(loopPanel); // Add the panel to the ArrayList for later
                // NOTE: The panels are not displayed here but are added in the call to updateBookings()
            }

            refreshButton = new JButton("Refresh"); // create the refresh button to be used in updateBookings()

            availPane = new JScrollPane(innerScrollPanel); // Initialize ScrollPane and have it contain innerScrollPanel
            availPane.setPreferredSize(new Dimension(560, 330)); // Set the size of the ScrollPane
            updateBookings(); // Place the content on the screen
            c.fill = GridBagConstraints.BOTH; // Fill the grid square vertically and horizontally
            c.anchor = GridBagConstraints.CENTER; // Center the pane in the grid square
            c.gridx = 1; // Place the pane in column 1
            c.gridy = 1; // Place the pane in row 1
            content.add(availPane, c); // add the pane to the screen

            frame.pack(); // Scale the pane only to fit the content
            frame.setResizable(false); // Do not allow the user to resize the pane
            frame.setLocationRelativeTo(null); // Center the pane on the screen
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose of frame but continue execution on close
            frame.setVisible(true); // Display the frame
        }
    }

    // Takes a login request and verifies it
    // returns True if valid credentials and False otherwise
    public boolean testLogin() {
        // Open the server and tell it you are attempting to log-in
        sendNewRequest("login");
        try {
            // write the username then the password
            oos.writeObject(username);
            oos.flush();
            oos.writeObject(password);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String logInResponse = null;
        try {
            // read back the response from the Server
            logInResponse = (String) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // if the credentials are not valid return false
        if (logInResponse.equals("not approved")) {
            return false;
        } else {
            // if valid...
            try {
                // read the userIndex and currentUser from the server
                userIndex = (Integer) ois.readObject();
                currentUser = (User) ois.readObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        // return true because of successful login
        return true;
    }

    // void method called when a login is not valid
    public void invalidLogin() {
        try {
            // ask if the user would like to make an  account with those credentials
            int decision = JOptionPane.showConfirmDialog(null,
                    "No user found for that username and password.\n" +
                            "Would you like to create a new Account with this username and password?",
                    "Marketplace Calendar",
                    JOptionPane.YES_NO_OPTION);
            // If the user decides to add that as an account...
            if (decision == JOptionPane.YES_OPTION) {
                // Tell the server to prepare to receive a new Account
                sendNewRequest("addAccount");
                // Write the username and password of the new Account
                oos.writeObject(username);
                oos.flush();
                oos.writeObject(password);
                oos.flush();
                String[] types = {"Buyer", "Seller"};
                // Prompt the user in a dropdown to select if they would like to add a Buyer or a Seller
                int userType = JOptionPane.showOptionDialog(null,
                        "Would you like to create a buyer or seller account?",
                        "Marketplace Calendar", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, types, types[0]);
                // Report the result to the server and add the email to the account
                if (userType == 0) {
                    oos.writeObject("buyer");
                    oos.flush();
                    String email = JOptionPane.showInputDialog(null, "Enter an email for the account.",
                            "Marketplace Calendar", JOptionPane.QUESTION_MESSAGE);
                    oos.writeObject(email);
                    oos.flush();
                } else {
                    oos.writeObject("seller");
                    oos.flush();
                    String email = JOptionPane.showInputDialog(null, "Enter an email for the account.",
                            "Marketplace Calendar", JOptionPane.QUESTION_MESSAGE);
                    oos.writeObject(email);
                    oos.flush();
                }
                try {
                    // read the userIndex and currentUser from the server
                    currentUser = (User) ois.readObject();
                    userIndex = (Integer) ois.readObject();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // method to be run once valid credentials have been entered
    public void loggedIn() {
        try {
            // if the current user is a Buyer
            if (currentUser.getClass().equals(Buyer.class)) {
                // Ask the server for all the stores

                sendNewRequest("allStores");

                // read each store name back from the server and add them to an ArrayList
                String store = null;
                store = (String) ois.readObject();
                ArrayList<String> stores = new ArrayList<>();
                while (true) {
                    // break when the Server sends the store with the name "EOT"
                    if (store.equals("EOT")) {
                        break;
                    }
                    if (store != null) {
                        stores.add(store);
                    }
                    store = (String) ois.readObject();
                }

                // Ask the server to send all the approvedBookings back
                sendNewRequest("viewAll");

                // Read all the approvedBookings back and store them in an ArrayList
                Availability availability;
                ArrayList<Availability> availabilities;
                try {
                    availability = (Availability) ois.readObject();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                availabilities = new ArrayList<>();
                while (true) {
                    // Stop the loop when the Availability titled "EOT" is received
                    if (availability.getTitle().equals("EOT")) {
                        break;
                    }
                    if (availability != null) {
                        availabilities.add(availability);
                    }
                    try {
                        availability = (Availability) ois.readObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                // Drop connection to the Server
                closeConnection();
                // Create a new Buyer GUI instance using the approvedBookings and Stores we just captured
                BuyerGUI gui = new BuyerGUI(availabilities, stores);
                // Start the GUI on the Event Dispatch Thread
                gui.start();
            } else {
                // If the user is a Seller
                // TO DO: Start a Seller GUI
                // Currently this just opens the old System.out GUI
                // Note for Ethan: This is where to start the Seller GUI (See Above)
                // Ask the server for all the stores

                // read each store name back from the server and add them to an ArrayList

                // Drop connection to the Server
                closeConnection();
                StoreGUI gui = new StoreGUI((Seller) currentUser);
                gui.start();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // A method that returns an ArrayList of strings that are Calendar Names for a specific store from the Server
    // Inputs: String Store Name
    // Returns: ArrayList<String>
    public ArrayList<String> getCalendars(String store) {
        try {
            // Prompt the Server for calendars
            sendNewRequest("calendar");
            // Write the store name to the server
            oos.writeObject(store);
            oos.flush();
            String result = null;
            try {
                // read if this is a valid store from the server
                result = (String) ois.readObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // if this is a valid store...
            if (result.equals("found")) {
                // read all the Calendars from the Server
                String calendar = null;
                calendar = (String) ois.readObject();
                ArrayList<String> calendars = new ArrayList<>();
                while (true) {
                    // Stop the loop when receiving a Calendar titled "EOT"
                    if (calendar.equals("EOT")) {
                        break;
                    }
                    if (calendar != null) {
                        calendars.add(calendar);
                    }
                    calendar = (String) ois.readObject();
                }
                // Return this arrayList of Calendar Names
                return calendars;
            } else {
                // If not valid, return an empty ArrayList
                return new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // A method that returns an ArrayList of Availabilities from a Calendar
    // Inputs: Store Name
    // Returns: ArrayList<Availability>
    private ArrayList<Availability> getAvails(String storeName, String calendarName) {
        try {
            // Prompt the server to get Availabilities
            sendNewRequest("getAvails");
            // Write the name of the store these are in
            oos.writeObject(storeName);
            oos.flush();
            // Read the response from the server back. Should be true as all Calendar names match. No need to store
            ois.readObject();
            // Write the calendar name
            oos.writeObject(calendarName);
            oos.flush();
            // Read back the ArrayList<Availability> From the server and immediately return it.
            return (ArrayList<Availability>) ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // The method used to start a new interaction with a server
    // Inputs: A string that is the request a server needs
    public void sendNewRequest(String message) {
        do {
            try {
                // Start the socket back up to the specified port
                socket = new Socket("localhost", port);
                // Initialize ObjectOutputStream and flush its header
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
                // open up a new ObjectInputStream from the server
                ois = new ObjectInputStream(socket.getInputStream());
                // Write whether this GUI knows who it is yet or not
                oos.writeObject((currentUser != null));
                oos.flush();
                // If the GUI knows who it is
                if (currentUser != null) {
                    // Write the index the currentUser exists at
                    oos.writeObject(userIndex);
                    oos.flush();
                    // Write the currentUser
                    oos.writeObject(currentUser);
                    oos.flush();
                    // If the current user is a seller...
                    if (currentUser.getClass().equals(Seller.class) && 
                        !message.equals("updateUser") && 
                        !message.equals("approve") && 
                        !message.equals("decline")) {
                        // send a request to get the state of the user from the Server
                        oos.writeObject("getUser");
                        oos.flush();
                        // set the currentUser to the read back value
                        currentUser = (Seller) ois.readObject();
                    }
                }
                // Send the string request needed to know what method has been called
                oos.writeObject(message);
                oos.flush();
            } catch (IOException f) {
                throw new RuntimeException(f);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } while (!socket.isConnected()); // in case the socket is preoccupied, repeat this until a connection is made
    }

    // void method to disconnect from a server
    // NOTE: Not entirely necessary as the server will kick you out once a request is finished
    public void closeConnection() {
        try {
            oos.close();
            ois.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // The method started when a new instance is formed by Starter
    public void run() {
        // NOTE: All Login information and requests go through Server
        LoginGui loginScreen = new LoginGui();
        loginScreen.start();
    }
}
