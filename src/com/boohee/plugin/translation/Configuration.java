package com.boohee.plugin.translation;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by moxun on 2017/6/14.
 */
public class Configuration implements Configurable{
    private JTextField appId;
    private JTextField appKey;
    private JPanel root;

    private static final String KEY_APP_ID = "YOUDAO_APP_ID";
    private static final String KEY_APP_KEY = "YOUDAO_APP_KEY";

    @Nls
    public String getDisplayName() {
        return "ECTranslation";
    }

    @Nullable
    public String getHelpTopic() {
        return "";
    }

    @Nullable
    public JComponent createComponent() {
        appId.setText(PropertiesComponent.getInstance().getValue(KEY_APP_ID));
        appKey.setText(PropertiesComponent.getInstance().getValue(KEY_APP_KEY));
        return root;
    }

    public boolean isModified() {
        return true;
    }

    public void apply() throws ConfigurationException {
        PropertiesComponent.getInstance().setValue(KEY_APP_ID, appId.getText());
        PropertiesComponent.getInstance().setValue(KEY_APP_KEY, appKey.getText());
    }

    public void reset() {

    }

    public void disposeUIResources() {

    }

    public static String getAppId() {
        return PropertiesComponent.getInstance().getValue(KEY_APP_ID, "");
    }

    public static String getAppKey() {
        return PropertiesComponent.getInstance().getValue(KEY_APP_KEY, "");
    }
}
