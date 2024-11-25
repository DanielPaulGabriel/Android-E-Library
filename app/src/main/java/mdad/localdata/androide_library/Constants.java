package mdad.localdata.androide_library;

public class Constants {
    // Base URL
    public static final String BASE_URL = "http://192.168.86.20";

    // Endpoints
        // Users
        public static final String LOGIN_URL = BASE_URL + "/elibrary/users/loginUser.php";
        public static final String REGISTER_URL = BASE_URL + "/elibrary/users/registerUser.php";
        public static final String DELETE_USER_URL = BASE_URL + "/elibrary/users/deleteUser.php";
        public static final String UPDATE_USER_DETAILS_URL = BASE_URL + "/elibrary/users/updateUserDetails.php";
        public static final String GET_ALL_USERS_URL = BASE_URL + "/elibrary/users/getAllUsers.php";
        public static final String GET_USER_DETAILS_URL = BASE_URL + "/elibrary/books/getUserDetails.php";
        // Books
        public static final String GET_ALL_BOOKS_URL = BASE_URL + "/elibrary/books/getAllBooks.php";
        public static final String BORROW_BOOK_URL = BASE_URL + "/elibrary/books/borrowBook.php";
        public static final String RETURN_BOOK_URL = BASE_URL + "/elibrary/books/returnBook.php";

}
