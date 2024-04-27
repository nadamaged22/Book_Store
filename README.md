# Online Bookstore Application
- This is a client-server application developed in Java with MongoDB as the database management system. It allows users to browse, search, borrow, and lend books.
# Features
- Server-Client Communication: The application uses Java SE sockets to handle user requests and responses.
- Book Inventory Management: The server maintains a MongoDB database of available books, including details such as title, author, genre, price, quantity, and the list of clients who have the book.
- User Authentication: Existing users can log in.
   - New users need to register first.
  - To login, the user needs to send their username and password.
   - To register, the user needs to send their name, username, and password.
  
- Invalid login/registration scenarios are handled as follows:
  - If the password is wrong, a 401 error appears (unauthorized).
  - If the username is not found, a 404 error appears (not found).
  - If a username cannot be used to register a new user because it is already reserved, a custom error appears.
    
- Browse and Search Books:
   - Users can browse through the bookstore's catalog, search for specific books by title, author, genre, and view detailed information about each book.
     
- Add and Remove Books:
    - Users can add books they wish to lend and specify the details of these books. Also, they can remove books they don’t want to lend anymore.
      
- Submit a Request:
    - A user (as a borrower) can submit a borrowing request to another user (as a lender)
      
- Accept / Reject a Request:
    - Users can check the incoming borrowing requests from other users and can accept or reject any of them. Note: A user can be both a borrower and a lender at the same time.
      
- Request History:
   - Users have access to their requests’ history, allowing them to view and track their status (accepted, rejected, or pending).
     
- Library Overall Statistics:
    - The admin can view the overall status of the library, including current borrowed books, available books, and accepted/rejected/pending requests.
      
- Error Handling:
   - The application implements error handling mechanisms to deal with various scenarios, such as invalid user input.

# Technologies Used
- Java SE for server-client communication and application logic.
- MongoDB for book inventory management and user authentication.
