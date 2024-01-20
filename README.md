# Project-5
Adding a GUI, concurrency, and Network IO to Project 4

## Compiling and Running this project

In order to run this project first either clone or download this repository to your laptop

Make sure all of the java files are there. There should be 8 files ending in .java
* Availability.java
* Buyer.java
* Calendar.java
* EventFullException.java
* NewUI.java
* SaveUsers.java
* Seller.java
* Server.java
* Starter.java
* Store.java
* User.java

There should also be one .txt file
* users.txt

NOTE: If your IDE uses a /src file to hold source code feel free to place all the .java files in this folder but please leave users.txt in the parent folder of /src

Once all this has been confirmed open the location where the file is saved

Next, run Starter.java. All that is required to run the project is running the main method in Starter

This should start the program and a GUI should show up

Please note: When first signing in it is not necessary to start a new Server. A default Server is started with the users.txt file when running Starter's main method

For your convenience, two Users with data already loaded exist
Buyer: Username: bob Password: marls
Seller: Username: penny Password: pusher

## Who is submitting what for this project

Avery Frye - Submitted Report on Brightspace

Avery Frye - Submitted Vocareum Workspace

Avery Frye - Submitted Presentation Recording

## Class descriptions

### Availability.java
Availability is the work horse for this project. It is the item customers come in to reserve and it is what sellers post on their stores. This class holds information
such as the name of the availability as well as its date, start time, end time, maximum attendees, and how many people are currently booked. This class also stores 
what calendar it belongs to so that when someone requests an availability it can hand it up the line.

### Buyer.java
A Buyer is a User subclass that does not create or edit stores or calendars. Instead, they scroll through stores and calendars to find any Availability that they would 
like to book. A Buyer simply has a username, password, and email stored in the super class User. They also can see all of the Availability objects that they have been 
approved to attend by the Seller.

### Calendar.java
Calendar is what makes up the content of a Store. A Store contains a list of all the calendars within itself. Additionally, a Calendar is a list of Availability
objects that all go together. A Store, all of the Store's Calendars, and all of those Calendars' Availibilties belong to a single Seller. This makes up all of what a
Buyer sees when searching the store

### EventFullException.java
Simply put, an EventFullException that is thrown when the number of approved requests for an Availability attempts to exceed the maximum number of attendees as set by
the seller.

### NewUI.java
NewUI is the class that contains all of the GUIs and action handlers necessary for interacting with the server and displaying the results to a client. NewUI makes all
the requests to a Server and is the file that handles 90% of the client's interactions. Additionally, NewUI is a thread and thus multiple GUIs can be interacted with
concurrently and these threads can all have different Users signed into them.

### SaveUsers.java
SaveUsers is a thread that saves an ArrayList of User objects to a specified file. It is called after each request to a Server to ensure that, in the case of a crash,
all data is saved to be accessed later.

### Seller.java
A Seller is a User subclass that contains user information such as name, email, and password in the User class. Additionally it contains a list of Store objects that 
correspond to the various stores run by the Seller. Additionally a seller can see the Availability objects that have been requested to be booked by Buyers.

### Server.java
Server is a thread where all the sensitive User data is stored and where a client sends a request and receives their data. One Server can support multiple clients.
Additionally, the option to start a new Server with custom files is implemented allowing clients to select which Server to connect to. Lastly, Servers are almost
constantly spawning a new SaveUsers thread to save the User data in the event of a crash.

### Starter.java
Starter is the glue that makes the project run together. Starter initially spawns a Default Server for the first client to be able to connect to. Additionally, Starter
will ask each client if they would like to start their own Server or connect to a pre-existing one. It creates a new thread for each client GUI and sends them along
their way through the project.

### Store.java
A Store is a marketplace that contains Calendar objects and belongs to a Seller. It contains basic information about itself such as its name, owner, and a list of all
the Calendar objects it contains.

### User.java
User is the superclass for both Buyer and Seller that allows them to be stored and written together in files. We no longer have buyers.txt and sellers.txt we simply
have users.txt. User allows for that and simpler writing of the Buyer and Seller classes and easier file writing.
