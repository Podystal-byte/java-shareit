package ru.practicum.shareit.user.repo;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserRepostory implements UserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User create(User user) {
        if (user.getId() == null || user.getId() == 0) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User updateUser) {
        users.put(updateUser.getId(), updateUser);
        return updateUser;
    }

    @Override
    public void delete(User user) {
        users.remove(user.getId());
    }

    @Override
    public User findById(int id) {
        return users.get(id);
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}