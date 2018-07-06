package chat.controller;

import chat.Main;
import chat.component.ConfirmDialog;
import chat.component.StyleUtil;
import chat.util.ColorUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import chat.util.AppProperty;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Alexander Diachenko.
 */
public class SettingController {

    @FXML
    private ColorPicker baseColorPicker;
    @FXML
    private Node settingsRoot;
    @FXML
    private Label transparencyValue;
    @FXML
    private Label fontSize;
    @FXML
    private ChoiceBox<String> languageChoiceBox;
    @FXML
    private ChoiceBox<String> themeChoiceBox;
    @FXML
    private Slider fontSizeSlider;
    @FXML
    private Slider transparencySlider;
    @FXML
    private ColorPicker backgroundColorPicker;
    @FXML
    private ColorPicker nickColorPicker;
    @FXML
    private ColorPicker separatorColorPicker;
    @FXML
    private ColorPicker messageColorPicker;
    private Properties settings;
    private Map<String, String> languages;
    private Node chatRoot;

    public void initialize() {
        this.settings = AppProperty.getProperty("./settings/settings.properties");
        this.chatRoot = getChatRoot();
        initLanguage();
        initTheme();
        initFontSizeSlider();
        initTransparencySlider();
        initBaseColorPicker();
        initBackGroundColorPicker();
        initNickColorPicker();
        initSeparatorColorPicker();
        initMessageColorPicker();
    }

    private void initBaseColorPicker() {
        this.baseColorPicker.setValue(Color.valueOf(this.settings.getProperty("root.base.color")));
        this.baseColorPicker.valueProperty().addListener((ov, old_val, new_val) -> StyleUtil.setRootStyle(this.chatRoot, this.settingsRoot, ColorUtil.getHexColor(new_val), ColorUtil.getHexColor(this.backgroundColorPicker.getValue())));
    }

    private void initLanguage() {
        this.languages = new HashMap<>();
        this.languages.put("en", "English");
        this.languages.put("ru", "Russian");
        this.languages.put("ua", "Ukrainian");
        this.languageChoiceBox.setItems(FXCollections.observableArrayList(this.languages.values()));
        this.languageChoiceBox.setValue(this.languages.get(this.settings.getProperty("root.language")));
    }

    private void initTheme() {
        try {
            File[] themes = new File(getClass().getResource("/theme").toURI()).listFiles();
            List<String> list = new ArrayList<>();
            for (File theme : themes) {
                list.add(theme.getName());
            }
            this.themeChoiceBox.setItems(FXCollections.observableArrayList(list));
            this.themeChoiceBox.setValue(this.settings.getProperty("root.theme"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void initFontSizeSlider() {
        String fontSizeValue = this.settings.getProperty("font.size");
        this.fontSize.setText(fontSizeValue);
        this.fontSizeSlider.setValue(Double.parseDouble(fontSizeValue));
        this.fontSizeSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            this.fontSize.setText(String.valueOf(Math.round(new_val.doubleValue())));
            StyleUtil.setLabelStyle(this.chatRoot, this.settingsRoot, String.valueOf(new_val), ColorUtil.getHexColor(this.nickColorPicker.getValue()), ColorUtil.getHexColor(this.separatorColorPicker.getValue()), ColorUtil.getHexColor(this.messageColorPicker.getValue()));
        });
    }

    private void initTransparencySlider() {
        String backgroundTransparencyValue = this.settings.getProperty("background.transparency");
        this.transparencyValue.setText(backgroundTransparencyValue);
        this.transparencySlider.setValue(Long.valueOf(backgroundTransparencyValue) * 100);
        this.transparencySlider.valueProperty().addListener((ov, old_val, new_val) -> {
            String format = String.format("%.2f", new_val.doubleValue() / 100);
            this.transparencyValue.setText(format);
        });
    }

    private void initBackGroundColorPicker() {
        this.backgroundColorPicker.setValue(Color.valueOf(this.settings.getProperty("root.background.color")));
        this.backgroundColorPicker.valueProperty().addListener((ov, old_val, new_val) -> StyleUtil.setRootStyle(this.chatRoot, this.settingsRoot, ColorUtil.getHexColor(this.baseColorPicker.getValue()), ColorUtil.getHexColor(new_val)));
    }

    private void initNickColorPicker() {
        this.nickColorPicker.setValue(Color.valueOf(this.settings.getProperty("nick.font.color")));
        this.nickColorPicker.valueProperty().addListener((ov, old_val, new_val) -> {
            StyleUtil.setLabelStyle(this.chatRoot, this.settingsRoot, this.fontSize.getText(), ColorUtil.getHexColor(new_val), ColorUtil.getHexColor(this.separatorColorPicker.getValue()), ColorUtil.getHexColor(this.messageColorPicker.getValue()));
        });
    }

    private void initSeparatorColorPicker() {
        this.separatorColorPicker.setValue(Color.valueOf(this.settings.getProperty("separator.font.color")));
        this.separatorColorPicker.valueProperty().addListener((ov, old_val, new_val) -> {
            StyleUtil.setLabelStyle(this.chatRoot, this.settingsRoot, this.fontSize.getText(), ColorUtil.getHexColor(this.nickColorPicker.getValue()), ColorUtil.getHexColor(new_val), ColorUtil.getHexColor(this.messageColorPicker.getValue()));
        });
    }

    private void initMessageColorPicker() {
        this.messageColorPicker.setValue(Color.valueOf(this.settings.getProperty("message.font.color")));
        this.messageColorPicker.valueProperty().addListener((ov, old_val, new_val) -> StyleUtil.setLabelStyle(this.chatRoot, this.settingsRoot, this.fontSize.getText(), ColorUtil.getHexColor(this.nickColorPicker.getValue()), ColorUtil.getHexColor(this.separatorColorPicker.getValue()), ColorUtil.getHexColor(new_val)));
    }

    public void confirmAction() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.openDialog(getStage(), this.settings, this.nickColorPicker.getValue());
        Stage stage = confirmDialog.getStage();
        stage.setOnCloseRequest(event -> {
            if (confirmDialog.isConfirmed()) {
                this.settings.setProperty("background.transparency", this.transparencyValue.getText());
                this.settings.setProperty("font.size", this.fontSize.getText());
                this.settings.setProperty("root.language", getLanguage(this.languageChoiceBox.getValue()));
                this.settings.setProperty("root.theme", this.themeChoiceBox.getValue());

                this.settings.setProperty("root.base.color", ColorUtil.getHexColor(this.baseColorPicker.getValue()));
                this.settings.setProperty("root.background.color", ColorUtil.getHexColor(this.backgroundColorPicker.getValue()));
                this.settings.setProperty("nick.font.color", ColorUtil.getHexColor(this.nickColorPicker.getValue()));
                this.settings.setProperty("separator.font.color", ColorUtil.getHexColor(this.separatorColorPicker.getValue()));
                this.settings.setProperty("message.font.color", ColorUtil.getHexColor(this.messageColorPicker.getValue()));
                AppProperty.setProperties(this.settings);
            }
        });
    }

    public void cancelAction() {
        StyleUtil.reverseStyle(this.settings, chatRoot, settingsRoot);
        getStage().close();
    }

    private String getLanguage(String value) {
        for (String key : this.languages.keySet()) {
            if (this.languages.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }

    private Stage getStage() {
        return (Stage) settingsRoot.getScene().getWindow();
    }

    private Node getChatRoot() {
        Stage owner = Main.stage;
        return owner.getScene().lookup("#root");
    }

    public Node getSettingsRoot() {
        return settingsRoot;
    }
}
