package chat.controller;

import chat.component.CustomButton;
import chat.component.CustomListView;
import chat.component.CustomScrollPane;
import chat.component.CustomVBox;
import chat.model.entity.User;
import chat.model.repository.UserRepository;
import chat.sevice.MessageEvent;
import chat.util.StyleUtil;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Controller;

import java.util.*;

/**
 * @author Oleksandr_Diachenko
 */
@Controller
public class RandomizerController implements ApplicationListener<MessageEvent> {

    @FXML
    private CustomVBox container;
    @FXML
    private CustomScrollPane scrollPane;
    @FXML
    private Label winner;
    @FXML
    private TextField keyWord;
    @FXML
    private CustomButton play;
    @FXML
    private Label countdown;
    @FXML
    private GridPane gridPane;
    @FXML
    private CustomVBox root;
    @FXML
    private CustomListView<Integer> times;
    @FXML
    private CheckBox caseCheckbox;
    private StyleUtil styleUtil;
    private ApplicationStyle applicationStyle;
    private UserRepository userRepository;
    private Random random = new Random();
    private Set<User> users = new HashSet<>();
    private Timeline timeline;

    @Autowired
    public RandomizerController(StyleUtil styleUtil, ApplicationStyle applicationStyle, UserRepository userRepository) {
        this.styleUtil = styleUtil;
        this.applicationStyle = applicationStyle;
        this.userRepository = userRepository;
    }

    @FXML
    public void initialize() {
        setTimes();
        setStyles();
        setHeights();
    }

    private void setTimes() {
        times.setData(FXCollections.observableArrayList(1, 3, 5, 10, 15, 20, 30));
        times.setSelected(0);
    }

    private void setStyles() {
        styleUtil.setRootStyle(Collections.singletonList(root), applicationStyle.getBaseColor(),
                applicationStyle.getBackgroundColor());
        styleUtil.setLabelsStyle(gridPane, applicationStyle.getNickColor());
    }

    private void setHeights() {
        scrollPane.bindPrefHeightProperty(root.getHeightProperty());
        scrollPane.bindValueProperty(container.getHeightProperty());
    }

    public void playAction() {
        List<Node> blankNodes = getBlankNodes(keyWord, times);
        if (blankNodes.isEmpty()) {
            play.disable();
            resetContainerAndUsers();
            startTimeline();
        } else {
            blink(blankNodes);
        }
    }

    private List<Node> getBlankNodes(TextField keyWord, ListView<Integer> times) {
        List<Node> blankNodes = new LinkedList<>();
        if (keyWord.getText().isEmpty()) {
            blankNodes.add(keyWord);
        }
        if (times.getSelectionModel().isEmpty()) {
            blankNodes.add(times);
        }
        return blankNodes;
    }

    protected void blink(List<Node> nodes) {
        for (Node node : nodes) {
            blink(node);
        }
    }

    protected void startTimeline() {
        Integer selectedItem = times.getSelectionModel().getSelectedItem();
        Integer[] time = {selectedItem * 60};
        timeline = new Timeline(new KeyFrame(Duration.millis(1000), event -> setCountdownTextAndStyle(time)));
        timeline.setCycleCount(time[0]);
        timeline.setOnFinished(event -> play.enable());
        timeline.play();
    }

    private void setCountdownTextAndStyle(Integer[] times) {
        countdown.setText(String.valueOf(--times[0]));
        countdown.setStyle(styleUtil.getLabelStyle(applicationStyle.getNickColor()));
    }

    private void blink(Node node) {
        FadeTransition blink = new FadeTransition(Duration.seconds(0.5), node);
        blink.setFromValue(1.0);
        blink.setToValue(0.2);
        blink.setCycleCount(4);
        blink.setOnFinished(event -> node.setOpacity(1));
        blink.play();
    }

    private void resetContainerAndUsers() {
        container.clear();
        users = new HashSet<>();
    }

    protected Label getUserName(String customName) {
        Label userName = new Label();
        userName.setText(customName);
        userName.setStyle(styleUtil.getLabelStyle(applicationStyle.getMessageColor()));
        return userName;
    }

    private boolean isEquals(String message, String keyWord) {
        boolean equals = message.equalsIgnoreCase(keyWord);
        if (caseCheckbox.isSelected()) {
            equals = message.equals(keyWord);
        }
        return equals;
    }

    public void stopAction() {
        if (timeline != null) {
            timeline.stop();
        }
        play.disable();
    }

    public void selectAction() {
        winner.setText(getRandomUser().getCustomName());
        winner.setStyle(styleUtil.getLabelStyle(applicationStyle.getNickColor()));
    }

    private User getRandomUser() {
        if (users.isEmpty()) {
            return new User();
        }
        int randomNumber = random.nextInt(users.size());
        Iterator<User> iterator = users.iterator();
        for (int index = 0; index < randomNumber; index++) {
            iterator.next();
        }
        return iterator.next();
    }

    protected void setTimesView(CustomListView<Integer> times) {
        this.times = times;
    }

    protected void setRoot(CustomVBox root) {
        this.root = root;
    }

    protected CustomVBox getRoot() {
        return root;
    }

    protected void setContainer(CustomVBox container) {
        this.container = container;
    }

    protected void setScrollPane(CustomScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    protected void setGridPane(GridPane gridPane) {
        this.gridPane = gridPane;
    }

    protected void setPlay(CustomButton play) {
        this.play = play;
    }

    protected void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    protected void setWinner(Label winner) {
        this.winner = winner;
    }

    protected void setUsers(Set<User> users) {
        this.users = users;
    }

    protected void setKeyWord(TextField keyWord) {
        this.keyWord = keyWord;
    }

    protected void setCaseCheckbox(CheckBox caseCheckbox) {
        this.caseCheckbox = caseCheckbox;
    }

    protected void setRandom(Random random) {
        this.random = random;
    }

    protected Set<User> getUsers() {
        return users;
    }

    @Override
    public void onApplicationEvent(MessageEvent messageEvent) {
        if (play != null && play.isDisable()) {
            updateContainer(messageEvent);
        }
    }

    private void updateContainer(MessageEvent message) {
        if (isEquals(message.getMessage(), keyWord.getText())) {
            Optional<User> userByName = userRepository.getUserByName(message.getNick());
            if (userByName.isPresent()) {
                User user = userByName.get();
                if (!users.contains(user)) {
                    Label userName = getUserName(user.getCustomName());
                    container.addNode(userName);
                }
                users.add(user);
            }
        }
    }
}
