package dao;

import model.User;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final List<User> users = new ArrayList<>();
    private static int nextId = 2;

    static {
        users.add(new User(1, "admin", "admin123", "admin"));
    }

    public User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equals(username)
                    && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public List<User> getAll() { return users; }

    public void add(String username, String password, String role) {
        users.add(new User(nextId++, username, password, role));
    }

    public void delete(int id) {
        users.removeIf(u -> u.getId() == id);
    }
}