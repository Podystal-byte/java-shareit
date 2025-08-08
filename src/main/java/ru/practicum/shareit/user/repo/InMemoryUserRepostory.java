package ru.practicum.shareit.user.repo;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserRepostory implements UserRepository {
    Map<Integer, User> users;
    private int nextid = 1;

    public InMemoryUserRepostory() {
        users = new HashMap<>();
    }

    @Override
    public User create(User user) {
        if (user.getId() == 0) {
            user.setId(nextid++);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User updateUser) {
        return users.put(updateUser.getId(), updateUser);
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
