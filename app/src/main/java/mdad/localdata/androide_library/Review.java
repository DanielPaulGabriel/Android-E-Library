package mdad.localdata.androide_library;

public class Review {
    private int reviewId;
    private String username;
    private int rating;
    private String reviewText;
    private String createdAt;

    // Constructor
    public Review(int reviewId, String username, int rating, String reviewText, String createdAt) {
        this.reviewId = reviewId;
        this.username = username;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
    }

    // Getters
    public int getReviewId() {
        return reviewId;
    }

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
}
