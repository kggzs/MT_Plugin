package com.kggzs.cn.mt;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import com.kggzs.cn.mt.util.AIChatHelper;
import com.kggzs.cn.mt.util.AIHelper;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.editor.BaseTextEditorToolMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginCheckBox;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.PluginDialog;

/**
 * AI 对话功能
 * 提供与 AI 直接聊天的界面，支持多轮对话和 MCP 集成
 */
public class AIChatMenu extends BaseTextEditorToolMenu {

    /**
     * 从设置页面打开 AI 对话弹窗
     */
    public static void showDialog(@NonNull PluginUI ui, @NonNull PluginContext context) {
        showChatDialog(ui, context);
    }

    /**
     * 构建并显示聊天对话框
     */
    private static void showChatDialog(@NonNull PluginUI ui, @NonNull PluginContext context) {
        String labelYou = context.getString("{ai_chat_you}");
        String labelAi = context.getString("{ai_chat_assistant}");
        String labelError = context.getString("{ai_chat_error}");
        boolean mcpEnabled = AIHelper.isMcpEnabled(context);
        String mcpSuffix = mcpEnabled ? "\n\n[MCP " + context.getString("{ai_mcp_active}") + "]" : "";
        String welcomeText = context.getString("{ai_chat_welcome}") + mcpSuffix;

        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        AIChatHelper chatHelper = new AIChatHelper(context);
        chatHelper.rebuildSystemMessage();

        PluginView view = ui.buildVerticalLayout()
            .addEditBox("chat_display")
                .text(welcomeText)
                .minLines(18).maxLines(28).textSize(13)
                .readOnly().widthMatchParent()
                .softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
                .marginBottom(smallMargin)
            .addEditBox("chat_input")
                .hint("{ai_chat_input_hint}")
                .minLines(2).maxLines(4).textSize(13)
                .widthMatchParent().marginBottom(smallMargin)
                .softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addCheckBox("chk_system_prompt").text("{ai_chat_use_system_prompt}")
                .checked(true).widthMatchParent()
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        PluginEditText chatDisplay = view.requireViewById("chat_display");
        PluginEditText chatInput = view.requireViewById("chat_input");
        PluginCheckBox chkSystemPrompt = view.requireViewById("chk_system_prompt");

        final boolean[] isSending = {false};

        PluginDialog dialog = ui.buildDialog()
            .setTitle("{ai_chat_title}")
            .setView(view)
            .setNegativeButton("{close}", (d, which) -> d.dismiss())
            .setNeutralButton("{ai_chat_clear}", (d, which) -> {
                chatHelper.clearHistory();
                chatDisplay.setText(welcomeText);
                ui.showToast("{ai_chat_cleared}");
            })
            .setPositiveButton("{ai_chat_send}", (d, which) -> {
                if (isSending[0]) return;
                String userMessage = chatInput.getText().toString().trim();
                if (userMessage.isEmpty()) {
                    ui.showToast("{ai_chat_empty_input}");
                    return;
                }

                isSending[0] = true;
                chatInput.setText("");
                String currentText = chatDisplay.getText().toString();
                chatDisplay.setText(currentText + "\n\n" + labelYou + ": " + userMessage);
                chatDisplay.selectEnd();

                new Thread(() -> {
                    chatHelper.sendMessage(userMessage,
                        accumulated -> AIHelper.runOnMainThread(() -> {
                            String text = chatDisplay.getText().toString();
                            String prefix = "\n" + labelAi + ": ";
                            int lastAi = text.lastIndexOf(prefix);
                            if (lastAi >= 0) {
                                text = text.substring(0, lastAi);
                            } else {
                                String youPrefix = "\n\n" + labelYou + ": ";
                                int youIdx = text.lastIndexOf(youPrefix);
                                if (youIdx >= 0) {
                                    text = text.substring(0, youIdx);
                                }
                            }
                            chatDisplay.setText(text + prefix + accumulated);
                            chatDisplay.selectEnd();
                        }),
                        fullContent -> AIHelper.runOnMainThread(() -> {
                            isSending[0] = false;
                            chatDisplay.selectEnd();
                        }),
                        error -> AIHelper.runOnMainThread(() -> {
                            isSending[0] = false;
                            String text = chatDisplay.getText().toString();
                            chatDisplay.setText(text + "\n\n" + labelError + ": " + error);
                            chatDisplay.selectEnd();
                        })
                    );
                }).start();
            })
            .show();

        chkSystemPrompt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chatHelper.setUseSystemPrompt(isChecked);
            chatHelper.rebuildSystemMessage();
        });

        chatInput.requestFocusAndShowIME();
    }

    // ========== ToolMenu 实现 ==========

    @NonNull
    @Override
    public String name() {
        return "{ai_chat_title}";
    }

    @NonNull
    @Override
    public Drawable icon() {
        return MaterialIcons.get("chat");
    }

    @Override
    public boolean checkVisible(@NonNull TextEditor editor) {
        return true;
    }

    @Override
    public void onMenuClick(@NonNull PluginUI pluginUI, @NonNull TextEditor editor) {
        showChatDialog(pluginUI, pluginUI.getContext());
    }
}