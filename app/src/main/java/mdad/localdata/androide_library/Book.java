package mdad.localdata.androide_library;

public class Book {
    private int bookId;
    private String title;
    private String author;
    private String genre;
    private String summary;
    private int quantity;
    private String contentPath; // Points to book content file location in the server
    private String coverPath; // Points to book cover image file location in the server

    // Constructor
    public Book(int bookId, String title, String author, String genre, String summary, int quantity, String contentPath, String coverPath) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.summary = summary;
        this.quantity = quantity;
        this.contentPath = contentPath;
        this.coverPath = coverPath;
    }

    // Getters
    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getSummary() {
        return summary;
    }

    public int getQuantity() {
        return quantity;
    }
    public String getContentPath() {
        return contentPath;
    }

    public String getCoverPath() {
        return coverPath;
    }
}
