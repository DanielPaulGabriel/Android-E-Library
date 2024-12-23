package mdad.localdata.androide_library;
public class StaffAccount {
    private final int id;
    private final String username;
    private final String role;

    public StaffAccount(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
