package mdad.localdata.androide_library;

// GenreHeader class for borrowed books tab
public class GenreHeader implements ListItem {
    private String genre;

    public GenreHeader(String genre) {
        this.genre = genre;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}
