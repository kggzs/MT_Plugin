package com.example.myplugin;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import com.example.myplugin.util.AIHelper;

import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.editor.BaseTextEditorFloatingMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.PluginDialog;

public class AICodeAnalysisFloatingMenu extends BaseTextEditorFloatingMenu {

    @NonNull
    @Override
    public String name() {
        return "AI快速分析";
    }

    @NonNull
    @Override
    public Drawable icon() {
        return MaterialIcons.get("psychology");
    }

    @Override
    public boolean checkVisible(@NonNull TextEditor editor) {
        return editor.hasTextSelected();
    }

    @Override
    public void onMenuClick(@NonNull PluginUI pluginUI, @NonNull TextEditor editor) {
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        String selectedText = editor.subText(start, end);

        if (selectedText.trim().isEmpty()) {
            pluginUI.showToast("请先选择要分析的代码");
            return;
        }

        PluginView thinkingView = pluginUI.buildVerticalLayout()
                .addEditBox("thinking_edit").text("正在初始化...").minLines(10).maxLines(20).textSize(12).readOnly().widthMatchParent().softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
                .paddingVertical(pluginUI.dialogPaddingVertical())
                .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                .paddingBottom(16)
                .build();

        PluginDialog dialog = pluginUI.buildDialog()
                .setView(thinkingView)
                .setCancelable(false)
                .show();

        PluginEditText thinkingEdit = thinkingView.requireViewById("thinking_edit");

        new Thread(() -> {
            try {
                // 使用简短提示词进行快速分析
                String shortPrompt = AIHelper.getShortPrompt(pluginUI.getContext());
                String[] result = AIHelper.analyzeCodeWithCustomPrompt(
                        pluginUI.getContext(),
                        selectedText,
                        shortPrompt,
                        thinkingEdit,
                        dialog
                );

                if (result != null) {
                    AIHelper.runOnMainThread(() -> {
                        dialog.dismiss();
                        showResultDialog(pluginUI, result[0]);
                    });
                }
            } catch (Exception e) {
                AIHelper.runOnMainThread(() -> {
                    dialog.dismiss();
                    pluginUI.showToast("分析失败: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showResultDialog(PluginUI pluginUI, String result) {
        pluginUI.buildDialog()
                .setTitle("分析结果")
                .setMessage(result)
                .setNegativeButton("复制", (dialog, which) -> {
                    pluginUI.getContext().setClipboardText(result);
                    pluginUI.showToast("已复制到剪贴板");
                })
                .setPositiveButton("确定", null)
                .show();
    }
}
