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

    // 取消标志位
    private volatile boolean isCancelled = false;

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

        // 显示提示词输入对话框
        showPromptInputDialog(pluginUI, selectedText);
    }

    /**
     * 显示提示词输入对话框
     */
    private void showPromptInputDialog(@NonNull PluginUI pluginUI, @NonNull String code) {
        // 默认用户提示词
        String defaultUserPrompt = "请简要分析以下代码";

        PluginView inputView = pluginUI.buildVerticalLayout()
                .addTextView().text("请输入补充提示词（将作为系统提示词的前缀）：").marginBottomDp(4)
                .addEditBox("user_prompt_input").text(defaultUserPrompt).minLines(3).maxLines(8).textSize(12).widthMatchParent()
                .paddingVertical(pluginUI.dialogPaddingVertical())
                .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                .build();

        PluginEditText userPromptInput = inputView.requireViewById("user_prompt_input");
        userPromptInput.requestFocusAndShowIME();

        pluginUI.buildDialog()
                .setTitle("设置分析提示词")
                .setView(inputView)
                .setPositiveButton("开始分析", (dialog, which) -> {
                    String userPrompt = userPromptInput.getText().toString().trim();
                    if (userPrompt.isEmpty()) {
                        userPrompt = defaultUserPrompt;
                    }
                    dialog.dismiss();
                    startAnalysis(pluginUI, code, userPrompt);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 开始AI分析
     */
    private void startAnalysis(@NonNull PluginUI pluginUI, @NonNull String code, @NonNull String userPrompt) {
        // 重置取消标志
        isCancelled = false;

        PluginView thinkingView = pluginUI.buildVerticalLayout()
                .addEditBox("thinking_edit").text("正在初始化...").minLines(10).maxLines(20).textSize(12).readOnly().widthMatchParent().softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
                .paddingVertical(pluginUI.dialogPaddingVertical())
                .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                .paddingBottom(16)
                .build();

        PluginDialog dialog = pluginUI.buildDialog()
                .setView(thinkingView)
                .setCancelable(false)
                .setNegativeButton("取消", (d, which) -> {
                    isCancelled = true;
                    d.dismiss();
                    pluginUI.showToast("已取消分析");
                })
                .show();

        PluginEditText thinkingEdit = thinkingView.requireViewById("thinking_edit");

        new Thread(() -> {
            try {
                // 使用用户提示词，系统提示词会自动从设置中获取并组合
                String[] result = AIHelper.analyzeCodeWithUserPrompt(
                        pluginUI.getContext(),
                        code,
                        userPrompt,
                        thinkingEdit,
                        dialog
                );

                // 检查是否已取消
                if (isCancelled) {
                    return;
                }

                if (result != null) {
                    AIHelper.runOnMainThread(() -> {
                        if (!isCancelled) {
                            dialog.dismiss();
                            showResultDialog(pluginUI, result[0]);
                        }
                    });
                }
            } catch (Exception e) {
                AIHelper.runOnMainThread(() -> {
                    if (!isCancelled) {
                        dialog.dismiss();
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("返回空结果")) {
                            showEmptyResultDialog(pluginUI, errorMsg);
                        } else {
                            pluginUI.showToast("分析失败: " + errorMsg);
                        }
                    }
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

    /**
     * 显示空结果提示对话框
     */
    private void showEmptyResultDialog(PluginUI pluginUI, String errorMsg) {
        // 截断过长的错误信息，只保留关键部分
        String displayMsg = errorMsg;
        if (displayMsg.length() > 2000) {
            displayMsg = displayMsg.substring(0, 2000) + "\n\n[内容已截断...]";
        }

        pluginUI.buildDialog()
                .setTitle("分析结果为空")
                .setMessage("AI 未能返回有效的分析结果。\n\n详细错误信息:\n" + displayMsg)
                .setPositiveButton("确定", null)
                .setNegativeButton("复制错误信息", (dialog, which) -> {
                    pluginUI.getContext().setClipboardText(errorMsg);
                    pluginUI.showToast("错误信息已复制到剪贴板");
                })
                .show();
    }
}
