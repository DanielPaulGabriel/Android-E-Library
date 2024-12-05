package mdad.localdata.androide_library;

public class UserBook implements ListItem{
    private int bookId;
    private String title;
    private String author;
    private String genre;
    private String summary;
    private String coverPath;
    private String borrow_date;
    private String due_date;
    private String return_date;


    // Constructor
    public UserBook(int bookId, String title, String author, String genre, String summary, String coverPath, String borrow_date, String due_date, String return_date) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.summary = summary;
        this.coverPath = coverPath;
        this.borrow_date = borrow_date;
        this.due_date = due_date;
        this.return_date = return_date;
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

    public String getCoverPath() {
        return coverPath;
    }

    public String getBorrow_date() {
        return borrow_date;
    }

    public String getDue_date() {
        return due_date;
    }

    public String getReturn_date() {
        return return_date;
    }

    @Override
    public int getType() {
        return TYPE_BOOK;
    }
}

