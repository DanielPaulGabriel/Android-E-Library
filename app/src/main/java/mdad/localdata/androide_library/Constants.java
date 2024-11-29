package mdad.localdata.androide_library;

public class Constants {
    // Base URL
    public static final String BASE_URL = "http://192.168.1.245";

    // Endpoints
        // Users
        public static final String LOGIN_URL = BASE_URL + "/elibrary/users/loginUser.php";
        public static final String REGISTER_URL = BASE_URL + "/elibrary/users/registerUser.php";
        public static final String DELETE_USER_URL = BASE_URL + "/elibrary/users/deleteUser.php";
        public static final String UPDATE_USER_DETAILS_URL = BASE_URL + "/elibrary/users/updateUserDetails.php";
        public static final String GET_ALL_USERS_URL = BASE_URL + "/elibrary/users/getAllUsers.php";
        public static final String GET_USER_DETAILS_URL = BASE_URL + "/elibrary/books/getUserDetails.php";

        // Books
        public static final String CREATE_BOOK_URL = BASE_URL + "/elibrary/books/createBook.php";
        public static final String EDIT_BOOK_URL = BASE_URL + "/elibrary/books/editBook.php";
        public static final String DELETE_BOOK_URL = BASE_URL + "/elibrary/books/deleteBook.php";
        public static final String GET_ALL_BOOKS_URL = BASE_URL + "/elibrary/books/getAllBooks.php";

        // Borrowed Books
        public static final String BORROW_BOOK_URL = BASE_URL + "/elibrary/borrowed_books/borrowBook.php";
        public static final String RETURN_BOOK_URL = BASE_URL + "/elibrary/borrowed_books/returnBook.php";
        public static final String GET_USER__BORROW_HISTORY_URL = BASE_URL + "/elibrary/borrowed_books/getUserBorrowHistory.php";
        public static final String GET_OVERDUE_BOOKS_URL = BASE_URL + "/elibrary/borrowed_books/getOverdueBooks.php";

        // Reviews
        public static final String CREATE_REVIEW_URL= BASE_URL + "/elibrary/reviews/createReview.php";
        public static final String EDIT_REVIEW_URL = BASE_URL + "/elibrary/reviews/editReview.php";
        public static final String DELETE_REVIEW_URL = BASE_URL + "/elibrary/reviews/deleteReview.php";
        public static final String GET_ALL_BOOK_REVIEWS_URL = BASE_URL + "/elibrary/reviews/getBookReviews.php";
        public static final String GET_USER_REVIEWS_URL = BASE_URL + "/elibrary/reviews/getUserReviews.php";



}
