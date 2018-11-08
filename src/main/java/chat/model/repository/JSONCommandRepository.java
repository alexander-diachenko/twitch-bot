package chat.model.repository;

import chat.model.entity.Command;
import chat.util.JSONParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Alexander Diachenko.
 */
@Repository
public class JSONCommandRepository implements CommandRepository {

    private final static Logger logger = LogManager.getLogger(JSONCommandRepository.class);

    private ObjectMapper mapper = new ObjectMapper();
    private Set<Command> commands;
    private String path;

    public JSONCommandRepository() {
    }

    public JSONCommandRepository(final String path) {
        this.path = path;
        this.commands = getAll();
    }

    @Override
    public Set<Command> getAll() {
        try {
            return new HashSet<>(this.mapper.readValue(JSONParser.readFile(this.path), new TypeReference<List<Command>>() {
            }));
        } catch (IOException exception) {
            logger.error(exception.getMessage(), exception);
        }
        return new HashSet<>();
    }

    @Override
    public Optional<Command> getCommandByName(final String name) {
        return this.commands
                .stream()
                .filter(command -> command.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public Command add(final Command command) {
        this.commands.add(command);
        flush();
        return command;
    }

    @Override
    public Command update(final Command command) {
        this.commands.remove(command);
        this.commands.add(command);
        flush();
        return command;
    }

    @Override
    public Command delete(final Command command) {
        this.commands.remove(command);
        flush();
        return command;
    }

    private void flush() {
        final Thread thread = new Thread(() -> {
            synchronized (this) {
                try {
                    this.mapper.writeValue(new FileOutputStream(this.path), this.commands);
                } catch (IOException exception) {
                    logger.error(exception.getMessage(), exception);
                    throw new RuntimeException("Commands failed to save. Put commands.json to data/");
                }
            }
        });
        thread.start();
    }
}
