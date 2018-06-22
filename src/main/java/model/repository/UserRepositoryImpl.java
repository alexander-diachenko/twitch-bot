package model.repository;

import model.entity.User;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import util.JSONParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander Diachenko.
 */
public class UserRepositoryImpl implements UserRepository {

    private final static Logger logger = Logger.getLogger(UserRepositoryImpl.class);
    private ObjectMapper mapper = new ObjectMapper();
    private Set<User> users;

    public UserRepositoryImpl() {
        users = getUsers();
    }

    @Override
    public Set<User> getUsers() {
        try {
            return new HashSet<>(mapper.readValue(JSONParser.readFile("./settings/users.json"), new TypeReference<List<User>>() {
            }));
        } catch (IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
        return new HashSet<>();
    }

    @Override
    public User getUserByName(String name) {
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void add(User user) {
        users.add(user);
        write();
    }

    @Override
    public void update(User user) {
        users.remove(user);
        users.add(user);
        write();
    }

    private void write() {
        Thread thread = new Thread(() -> {
            synchronized (this) {
                try {
                    mapper.writeValue(new FileOutputStream("./settings/users.json"), users);
                } catch (IOException exception) {
                    logger.error(exception.getMessage(), exception);
                }
            }
        });
        thread.start();
    }
}
