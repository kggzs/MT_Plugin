package com.example.myplugin;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.editor.BaseTextEditorToolMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginUI;

/**
 * AI代码分析工具菜单
 * 用于分析整个文件的代码
 */
public class AICodeAnalysisToolMenu extends BaseTextEditorToolMenu {

    private final AICodeAnalysisHelper helper = new AICodeAnalysisHelper("请分析以下代码", 5, 10);

    @NonNull
    @Override
    public String name() {
        return "{ai_code_analysis}";
    }

    @NonNull
    @Override
    public Drawable icon() {
        return MaterialIcons.get("psychology");
    }

    @Override
    public boolean checkVisible(@NonNull TextEditor editor) {
        return true;
    }

    @Override
    public void onMenuClick(@NonNull PluginUI pluginUI, @NonNull TextEditor editor) {
        String fullText = editor.subText(0, editor.length());
        helper.showPromptInputDialog(pluginUI, fullText);
    }
}
