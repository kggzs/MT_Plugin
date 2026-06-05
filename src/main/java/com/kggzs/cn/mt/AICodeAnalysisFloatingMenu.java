package com.kggzs.cn.mt;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.editor.BaseTextEditorFloatingMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginUI;

/**
 * AI代码分析浮动菜单
 * 用于分析选中的代码片段
 */
public class AICodeAnalysisFloatingMenu extends BaseTextEditorFloatingMenu {

    private final AICodeAnalysisHelper helper = new AICodeAnalysisHelper("{ai_quick_analysis_prompt}", 3, 8);

    @NonNull
    @Override
    public String name() {
        return "{ai_quick_analysis}";
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
            pluginUI.showToast("{select_code_hint}");
            return;
        }

        helper.showPromptInputDialog(pluginUI, selectedText);
    }
}
