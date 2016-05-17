import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by sky on 16/5/17.
 */
public class ECTranslation extends AnAction {
    private static final String HOST = "fanyi.youdao.com";
    private static final String PATH = "/openapi.do";
    private static final String PARAM_KEY_FROM = "keyfrom";
    private static final String PARAM_KEY = "key";
    private static final String PARAM_TYPE = "type";
    private static final String TYPE = "data";
    private static final String PARAM_DOC_TYPE = "doctype";
    private static final String DOC_TYPE = "json";
    private static final String PARAM_CALL_BACK = "callback";
    private static final String CALL_BACK = "show";
    private static final String PARAM_VERSION = "version";
    private static final String VERSION = "1.1";
    private static final String PARAM_QUERY = "q";
    //replace your own key, see http://fanyi.youdao.com/openapi?path=data-mode
    private static final String KEY_FROM = "Skykai521";
    private static final String KEY = "977124034";

    @Override
    public void actionPerformed(AnActionEvent e) {
        getTranslation(e);
    }

    private void getTranslation(AnActionEvent event) {
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (null == editor) return;
        SelectionModel model = editor.getSelectionModel();
        String queryText = model.getSelectedText();
        if (TextUtils.isEmpty(queryText)) return;

        try {
            //获取URI
            URI uri = createTranslationURI(queryText);
            //System.out.println(uri.toString());
            //配置GET请求
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000).build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            HttpClient client = HttpClients.createDefault();

            //请求网络
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                //获取响应数据
                HttpEntity resEntity = response.getEntity();
                String json = EntityUtils.toString(resEntity, "UTF-8");

                //转化为Translation对象
                Gson gson = new Gson();
                Translation translation = gson.fromJson(json, Translation.class);

                //显示结果
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(translation.toString(), MessageType.INFO, null)
                        .setFadeoutTime(3000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor) , Balloon.Position.below);

            } else {
                //显示错误代码和错误信息
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(response.getStatusLine().getReasonPhrase(), MessageType.ERROR, null)
                        .setFadeoutTime(3000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor) , Balloon.Position.below);
            }
        } catch (IOException e) {
            //显示异常信息
            JBPopupFactory factory = JBPopupFactory.getInstance();
            factory.createHtmlTextBalloonBuilder(e.getMessage(), MessageType.ERROR, null)
                    .setFadeoutTime(3000)
                    .createBalloon()
                    .show(factory.guessBestPopupLocation(editor) , Balloon.Position.below);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成URI
     *
     * @param query 查询内容
     * @return URI
     * @throws URISyntaxException
     */
    private URI createTranslationURI(String query) throws URISyntaxException {

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http")
                .setHost(HOST)
                .setPath(PATH)
                .addParameter(PARAM_KEY_FROM, KEY_FROM)
                .addParameter(PARAM_KEY, KEY)
                .addParameter(PARAM_TYPE, TYPE)
                .addParameter(PARAM_VERSION, VERSION)
                .addParameter(PARAM_DOC_TYPE, DOC_TYPE)
                .addParameter(PARAM_CALL_BACK, CALL_BACK)
                .addParameter(PARAM_QUERY, query)
        ;
        return builder.build();
    }
}
