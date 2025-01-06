package mdad.localdata.androide_library;

public class User {
    private int userId;
    private String username;
    private String role;
    private String createdAt;

    public User(int userId, String username, String role, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

}
