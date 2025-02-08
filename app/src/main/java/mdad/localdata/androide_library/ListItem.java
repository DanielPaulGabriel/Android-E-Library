package mdad.localdata.androide_library;

public interface ListItem { // Interface for book genre header in borrowed book tab
    int TYPE_HEADER = 0;
    int TYPE_BOOK = 1;

    int getType();
}
