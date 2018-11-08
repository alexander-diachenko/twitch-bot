package chat;

import chat.component.ChatDialog;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Alexander Diachenko
 */
@Component
public class PlusChatFX extends Application {

    private static ClassPathXmlApplicationContext context;
    public static Stage stage;
    private static ChatDialog chatDialog;

    public PlusChatFX() {
        //do nothing
    }

    @Autowired
    public PlusChatFX(final ChatDialog chatDialog) {
        PlusChatFX.chatDialog = chatDialog;
    }

    @Override
    public void init() {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        PlusChatFX.chatDialog.openDialog();
    }

    @Override
    public void stop() {
        context.close();
    }
}
