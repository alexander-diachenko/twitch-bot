package chat.command;

import chat.model.entity.Command;
import chat.model.repository.CommandRepository;

import java.util.Optional;

/**
 * @author Alexander Diachenko
 */
public class JSONCommand implements ICommand {

    private final CommandRepository commandRepository;
    private Command command;

    public JSONCommand(final CommandRepository commandRepository) {
        this.commandRepository = commandRepository;
    }

    @Override
    public boolean canExecute(final String command) {
        final Optional<Command> commandByName = this.commandRepository.getCommandByName(command);
        if(commandByName.isPresent()) {
            this.command = commandByName.get();
            return true;
        }
        return false;
    }

    @Override
    public String execute() {
        return this.command.getResponse();
    }
}