package thread;

import controller.ChatController;
import javafx.fxml.FXMLLoader;
import model.entity.Command;
import model.entity.Rank;
import model.entity.User;
import model.repository.*;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PingEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import util.AppProperty;

import java.io.IOException;
import java.util.*;

public class Bot extends ListenerAdapter implements Subject{

    private Properties connect;
    private UserRepository userRepository = new UserRepositoryImpl();
    private CommandRepository commandRepository = new CommandRepositoryImpl();
    private RankRepository rankRepository = new RankRepositoryImpl();
    private Set<Observer> observers = new HashSet<>();


    public Bot() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.load(getClass().getResource("/view/chat.fxml").openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ChatController chatController = fxmlLoader.getController();
        registerObserver(chatController);
        connect = AppProperty.getProperty("connect.properties");
    }

    /**
     * PircBotx will return the exact message sent and not the raw line
     */
    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        String nick = event.getUser().getNick();
        updateUser(nick);
        String message = event.getMessage();
        notifyObservers(nick, message);
        String command = getCommandFromMessage(message);
        if (command != null) {
            runCommand(event, command);
        } else {
            String response = getResponseMessage(event, message);
            sendMessage(response);
        }
    }

    /**
     * The command will always be the first part of the message
     * We can split the string into parts by spaces to get each word
     * The first word if it starts with our command notifier "!" will get returned
     * Otherwise it will return null
     */
    private String getCommandFromMessage(String message) {
        String[] msgParts = message.split(" ");
        if (msgParts[0].startsWith("!")) {
            return msgParts[0];
        } else {
            return null;
        }
    }

    private void runCommand(GenericMessageEvent event, String command) {
        if (command.equalsIgnoreCase("!rank")) {
            runRankCommand(event);
        } else {
            runOtherCommands(command);
        }
    }

    private void runOtherCommands(String command) {
        Set<Command> commands = commandRepository.getCommands();
        for (Command com : commands) {
            if (com.getName().equalsIgnoreCase(command)) {
                sendMessage(com.getResponse());
                break;
            }
        }
    }

    private void runRankCommand(GenericMessageEvent event) {
        String nick = event.getUser().getNick();
        User userByName = userRepository.getUserByName(nick);
        Rank rank = rankRepository.getRankByExp(userByName.getExp());
        sendMessage(nick + ", твой ранк " + rank.getName());
    }

    private String getResponseMessage(GenericMessageEvent event, String message) {
        System.out.println("This is message: " + message + " from user: " + event.getUser().getNick());
        return "";
    }

    /**
     * We MUST respond to this or else we will get kicked
     */
    @Override
    public void onPing(PingEvent event) {
        BotThread.bot.sendRaw().rawLineNow(String.format("PONG %s\r\n", event.getPingValue()));
    }

    private void sendMessage(String message) {
        BotThread.bot.sendIRC().message("#" + connect.getProperty("twitch.channel"), message);
    }

    private void updateUser(String nick) {
        User userByName = userRepository.getUserByName(nick);
        if (userByName == null) {
            createNewUser(nick);
        } else {
            updateExistingUser(userByName);
        }
    }

    private void updateExistingUser(User userByName) {
        User user = new User();
        user.setName(userByName.getName());
        user.setFirstMessage(userByName.getFirstMessage());
        user.setLastMessage(new Date());
        user.setExp(userByName.getExp() + 1);
        userRepository.update(user);
    }

    private void createNewUser(String nick) {
        User user = new User();
        user.setName(nick);
        user.setFirstMessage(new Date());
        user.setLastMessage(new Date());
        user.setExp(1);
        userRepository.add(user);
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String user, String message) {
        for (Observer observer : observers) {
            observer.update(user, message);
        }
    }
}
