package tech.czxs.deepcommenter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

public class GenerateCommentAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        Project project = e.getProject();

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null) return;

        ProgressManager.getInstance().run(
                new Task.Modal(project, "Generating Comment", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {

                        indicator.setFraction(0.1);
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            ex.printStackTrace();
//        }
        String result;
        try {
            String s = selectedText;
            result = HttpClientPool.getHttpClient().post("http://47.98.158.183:5000/s", s);
//            result = HttpClientPool.getHttpClient().post("http://127.0.0.1:5000/s", s);

        } catch (Exception ex) {
            ex.printStackTrace();
            Messages.showMessageDialog("Failed to connect to server.", "Information", Messages.getInformationIcon());
            return;
        }

        Document document = editor.getDocument();


        int selectionStart = selectionModel.getSelectionStart();
        int line = 0;
        int lineOffset = 0;
        for (int i = 0; i < document.getLineCount(); i++) {
            int tmpOffset = document.getLineStartOffset(i);
            if(tmpOffset <= selectionStart) {
                line = i;
                lineOffset = tmpOffset;
            } else break;
        }
        int lineEndOffset = document.getLineEndOffset(line);

        String s = document.getText(new TextRange(lineOffset, lineEndOffset));

        int spaceNum = 0;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == ' ') spaceNum++;
            else break;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaceNum; i++) sb.append(" ");

        final int insertOffset = lineOffset;
        final String result1 = result;
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.insertString(insertOffset, sb + "/**\n" + sb + " * " + result1 + sb + " */\n"));

        selectionModel.removeSelection();

    }

}
