## Test 1: User log in

Steps:
1. User launches application
2. User confirms they want to enter
3. User declines to start new server
4. User selects to enter the default server
5. User types a valid username into the username field
6. User types a valid password into the password field
7. User selects submit

Expected result: The user is approved to login and is directed to the GUI that corresponds with their type

Test status: Passed

## Test 2: User invalid login

Steps:
1. User launches application
2. User confirms they want to enter
3. User declines to start new server
4. User selects to enter the default server
5. User types an invalid username into the username field
6. User types an invalid password into the password field
7. User selects submit

Expected result: The user is asked if they would like to create a new account with those credentials

Test status: Passed

## Test 3: Store creation

Steps:
1. Seller signs in to their account
2. The seller types in a store name
3. The seller hits the submit button

Expected result: The seller enters in to manage the store and can see the store exists and all buyers can see the store

Test result: Passed

## Test 4: Calendar creation

Steps:
1. Seller signs in to their account
2. The seller selects a store
3. The seller hits the submit button
4. The seller hits the calendar tab
5. The seller hits add calendar
6. The seller types in all necessary information for the calendar
7. The seller hits the add calendar button

Expected result: The seller can see the calendar in their calendar list and can see the description in the description menu and it can be seen by buyers

Test result: Passed

## Test 5: Availability creation

Steps:
1. Seller signs in to their account
2. The seller selects a store
3. The seller hits the submit button
4. The seller hits the calendar tab
5. The seller hits add availability
6. The seller types in all necessary information for the availability
7. The seller hits the submit button

Expected result: The seller can see the availability under the calendar description and it can be seen by buyers

Test result: Passed

## Test 6: Availability request

Steps:
1. Buyer signs in to their account
2. The buyer selects a store to search and hits submit
3. The buyer selects a calendar to search and hits submit
4. The buyer chooses an availability from the list and requests it

Expected result: The user is shown confirmation and the seller who owns the store can go in and see the request

Test status: Passed

## Test 7: Availability approval

Steps:
1. Seller signs in to their account
2. The seller selects the store where the request hits submit
3. The seller selects one of the pending requests to approve

Expected result: The request goes away for the user and the Buyer who made the request can go in and see they have been approved

Test result: Passed
