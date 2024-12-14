package mdad.localdata.androide_library;

public class Review {
    private int reviewId;
    private String username;
    private int bookId;
    private String title;
    private String author;
    private String coverPath;
    private String summary;
    private int rating;
    private String reviewText;
    private String createdAt;

    // Constructor
    public Review( int reviewId, String username, int rating, String reviewText, String createdAt) {
        this.reviewId = reviewId;
        this.username = username;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
    }
    public Review(int reviewId, int bookId, int rating, String reviewText, String title, String author, String coverPath, String summary, String createdAt) {
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.title = title;
        this.author = author;
        this.coverPath = coverPath;
        this.summary = summary;
        this.createdAt = createdAt;
    }

    // Getters

    public String getUsername() {
        return username;
    }

    public int getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public int getReviewId() {
        return reviewId;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public String getSummary() {
        return this.summary;
    }
}
