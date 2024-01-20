/** An exception to be thrown when a Buyer requests an Availability that is full
 *
 * @author CS180 Group 007
 * @version 12/12/2022
 */

public class EventFullException extends Exception {
    // Constructor called when throwing a new EventFullException
    // Input: message to be included when thrown
    public EventFullException(String message) {
        super(message); // Create an Exception with the input message
    }
}
