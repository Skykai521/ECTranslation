import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import org.apache.http.util.TextUtils;

/**
 * Created by sky on 16/5/17.
 */
public class ECTranslation extends AnAction {
    private long latestClickTime;

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (!isFastClick(1000)) {
            getTranslation(e);
        }
    }

    private void getTranslation(AnActionEvent event) {
        Editor mEditor =  event.getData(PlatformDataKeys.EDITOR);
        if (null == mEditor) {
            return;
        }
        SelectionModel model = mEditor.getSelectionModel();
        String queryText = model.getSelectedText();
        if (TextUtils.isEmpty(queryText)) {
            return;
        }
        new Thread(new RequestRunnable(mEditor, queryText)).start();
    }

    public boolean isFastClick(long timeMillis) {
        long time = System.currentTimeMillis();
        long timeD = time - latestClickTime;
        if (0 < timeD && timeD < timeMillis) {
            return true;
        }
        latestClickTime = time;
        return false;
    }
}
