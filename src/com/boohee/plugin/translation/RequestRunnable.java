package com.boohee.plugin.translation;

import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by sky on 16/5/18.
 */
public class RequestRunnable implements Runnable {
    private static final String HOST = "openapi.youdao.com";
    private static final String PATH = "/api";
    private static final String PARAM_QUERY = "q";
    private static final String PARAM_KEY_FROM = "from";
    private static final String PARAM_KEY_TO = "to";
    private static final String PARAM_KEY_APP_KEY = "appKey";
    private static final String PARAM_KEY_SALT = "salt";
    private static final String PARAM_KEY_SIGN = "sign";

    private Editor mEditor;
    private String mQuery;

    public RequestRunnable(Editor editor, String query) {
        this.mEditor = editor;
        this.mQuery = query;
    }

    public void run() {
        try {
            URI uri = createTranslationURI(mQuery);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000).build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            HttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity resEntity = response.getEntity();
                String json = EntityUtils.toString(resEntity, "UTF-8");
                Gson gson = new Gson();
                Translation translation = gson.fromJson(json, Translation.class);
                //show result
                showPopupBalloon(translation.toString());
                Logger.info(translation.toString());
            } else {
                showPopupBalloon(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            showPopupBalloon(e.getMessage());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void showPopupBalloon(final String result) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                        .setFadeoutTime(5000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);
            }
        });
    }


    private URI createTranslationURI(String query) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();

        String salt = String.valueOf(System.currentTimeMillis());

        builder.setScheme("http")
                .setHost(HOST)
                .setPath(PATH)
                .addParameter(PARAM_KEY_FROM, "en")
                .addParameter(PARAM_KEY_TO, "zh_CHS")
                .addParameter(PARAM_QUERY, query)
                .addParameter(PARAM_KEY_APP_KEY, Configuration.getAppId())
                .addParameter(PARAM_KEY_SALT, salt)
                .addParameter(PARAM_KEY_SIGN, generateSign(query, salt));
        return builder.build();
    }

    private String generateSign(String q, String salt) {
        String src = Configuration.getAppId() + q + salt + Configuration.getAppKey();
        return md5(src);
    }

    private String md5(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        byte[] btInput = string.getBytes();
        try {
            /** 获得MD5摘要算法的 MessageDigest 对象 */
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            /** 使用指定的字节更新摘要 */
            mdInst.update(btInput);
            /** 获得密文 */
            byte[] md = mdInst.digest();
            /** 把密文转换成十六进制的字符串形式 */
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
