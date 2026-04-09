package com.example.myplugin;

import android.graphics.drawable.Drawable;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.editor.TextEditorFloatingMenu;
import bin.mt.plugin.api.editor.BaseTextEditorFloatingMenu;
import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.ui.PluginUI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class QuickInsertFunction extends BaseTextEditorFloatingMenu {
    @Override
    public String name() {
        return "{quick_insert_function}";
    }

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
        int insertPos = editor.getSelectionEnd();
        String text = getCurrentDateTime();
        editor.insertText(insertPos, text);
        pluginUI.showToast("{insert_success}");
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
