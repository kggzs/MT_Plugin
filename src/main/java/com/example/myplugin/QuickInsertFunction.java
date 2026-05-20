package com.example.myplugin;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.editor.BaseTextEditorFloatingMenu;
import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.PluginContext;

import com.example.myplugin.util.TimeFormatHelper;

import java.util.Date;

/**
 * 快速插入时间功能
 * 支持多种时间格式，可在插件设置中调整
 */
public class QuickInsertFunction extends BaseTextEditorFloatingMenu {

    @NonNull
    @Override
    public String name() {
        return "{quick_insert_function}";
    }

    @NonNull
    @Override
    public Drawable icon() {
        return MaterialIcons.get("add_circle");
    }

    @Override
    public boolean checkVisible(TextEditor editor) {
        return true;
    }

    @Override
    public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
        PluginContext context = pluginUI.getContext();
        String formattedTime = TimeFormatHelper.getFormattedTime(context, new Date());

        int insertPos = editor.getSelectionEnd();
        editor.insertText(insertPos, formattedTime);
        pluginUI.showToast("{insert_success}");
    }
}
