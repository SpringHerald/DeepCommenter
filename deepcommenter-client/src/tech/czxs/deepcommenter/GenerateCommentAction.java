package tech.czxs.deepcommenter;

import com.intellij.codeInsight.hint.HintManager;
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

import java.util.Properties;

public class GenerateCommentAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        Project project = e.getProject();

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null) return;
        int lineBreakCount = 0;
        for (int i = 0; i < selectedText.length(); i++) {
            if (selectedText.charAt(i) == '\n') lineBreakCount++;
        }
        if (lineBreakCount > 50) {
            HintManager.getInstance().showErrorHint(editor, "The method content is too long.");
            return;
        }

        Document document = editor.getDocument();

        int selectionStart = selectionModel.getSelectionStart();
        int selectionEnd = selectionModel.getSelectionEnd();
        String docString = document.getText();
        String s = docString.substring(selectionStart, selectionEnd);
        int start = getKeywordIndex(s);
        if (start == -1) {
            selectionStart = getNearestKeywordIndex(docString, selectionStart);
        } else {
            selectionStart += start;
        }
        selectionEnd = getMethodEndIndex(docString.substring(selectionStart));
        selectionEnd += selectionStart;
        selectedText = docString.substring(selectionStart, selectionEnd + 1);

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

        String result;
        try {
            Properties props = new Properties();
            props.load(this.getClass().getResourceAsStream("/server.properties"));
            String serverAddr = props.getProperty("server-address");

            result = HttpClientPool.getHttpClient().post("http://" + serverAddr + ":5000/s", selectedText);
        } catch (Exception ex) {
            ex.printStackTrace();
            Messages.showMessageDialog("Failed to connect to server.", "Information", Messages.getInformationIcon());
            return;
        }

        int line = 0;
        int lineOffset = 0;
        for (int i = 0; i < document.getLineCount(); i++) {
            int tmpOffset = document.getLineStartOffset(i);
            if (tmpOffset <= selectionStart) {
                line = i;
                lineOffset = tmpOffset;
            } else break;
        }
        int lineEndOffset = document.getLineEndOffset(line);

        s = document.getText(new TextRange(lineOffset, lineEndOffset));

        int spaceNum = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') spaceNum++;
            else break;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaceNum; i++) sb.append(" ");

        final int insertOffset = lineOffset;
        final String result1 = sb + "/**\n" + sb + " * " + result + sb + " */\n";
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.insertString(insertOffset, result1));

        selectionStart += result1.length();
        selectionEnd += result1.length();
        selectionModel.setSelection(selectionStart, selectionEnd + 1);

    }

    public static int getNearestKeywordIndex(String s, int idx) {
        for (int i = idx; i >= 0; i--) {
            if(s.charAt(i) == 'p') {
                if(s.substring(i).startsWith("public")) return i;
                if(s.substring(i).startsWith("private")) return i;
                if(s.substring(i).startsWith("protected")) return i;
            }
        }
        return 0;
    }

    public static int getKeywordIndex(String s) {
        boolean stringBlind = false;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '"') {
                if(i == 0 || s.charAt(i-1) != '\\')
                    stringBlind = !stringBlind;
            }
            if(stringBlind) continue;
            if(s.charAt(i) == 'p') {
                if(s.substring(i).startsWith("public")) return i;
                if(s.substring(i).startsWith("private")) return i;
                if(s.substring(i).startsWith("protected")) return i;
            }
        }
        return -1;
    }

    public static int getMethodEndIndex(String s) {
        int endIdx = s.length() - 1;
        int bracketCount = 0;
        boolean flag = false;
        boolean stringBlind = false;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '"') {
                if(i == 0 || s.charAt(i-1) != '\\')
                    stringBlind = !stringBlind;
            }
            if(stringBlind) continue;
            char c = s.charAt(i);
            if(c == '{') {
                bracketCount++;
                flag = true;
            } else if(c == '}' && bracketCount > 0) {
                bracketCount--;
            }
            if(flag && bracketCount == 0) {
                endIdx = i;
                break;
            }
        }
        return endIdx;
    }

}
