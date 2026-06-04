package com.kggzs.cn.mt;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kggzs.cn.mt.util.AIChatHelper;
import com.kggzs.cn.mt.util.AIHelper;

import java.net.HttpURLConnection;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.editor.BaseTextEditorToolMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginButton;
import bin.mt.plugin.api.ui.PluginCheckBox;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.PluginDialog;

/**
 * AI 对话功能
 * 提供与 AI 直接聊天的界面，支持多轮对话、流式响应和多模型兼容
 */
public class AIChatMenu extends BaseTextEditorToolMenu {

    /** 当前活跃的 HTTP 连接，用于中断请求 */
    @Nullable
    private static volatile HttpURLConnection sActiveConnection;

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

        // 构建 UI 布局
        PluginView view = ui.buildVerticalLayout()
            .addEditBox("chat_display")
                .text(welcomeText)
                .minLines(18).maxLines(28).textSize(13)
                .readOnly().widthMatchParent()
                .softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
                .marginBottom(smallMargin)
            .addEditBox("chat_input")
                .hint("{ai_chat_input_hint}")
                .minLines(1).maxLines(3).textSize(13)
                .softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
                .marginBottom(smallMargin)
            .addHorizontalLayout("button_row").children(row -> row
                .addButton("btn_clear").text("{ai_chat_clear}")
                .addButton("btn_send").text("{ai_chat_send}")
                    .marginLeft(smallMargin)
            ).widthMatchParent().marginBottom(smallMargin)
            .addCheckBox("chk_system_prompt").text("{ai_chat_use_system_prompt}")
                .checked(chatHelper.isUseSystemPrompt()).widthMatchParent()
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        PluginEditText chatDisplay = view.requireViewById("chat_display");
        PluginEditText chatInput = view.requireViewById("chat_input");
        PluginButton btnClear = view.requireViewById("btn_clear");
        PluginButton btnSend = view.requireViewById("btn_send");
        PluginCheckBox chkSystemPrompt = view.requireViewById("chk_system_prompt");

        final boolean[] isSending = {false};

        // 创建对话框（只有关闭按钮在底部按钮区）
        PluginDialog dialog = ui.buildDialog()
            .setTitle("{ai_chat_title}")
            .setView(view)
            .setNegativeButton("{close}", (d, which) -> {
                stopActiveRequest();
                d.dismiss();
            })
            .show();

        // ---- 发送/停止按钮 ----
        btnSend.setOnClickListener(v -> {
            if (isSending[0]) {
                // 正在发送中，点击为停止
                stopActiveRequest();
                isSending[0] = false;
                btnSend.setText("{ai_chat_send}");
                chatInput.setEnabled(true);
                return;
            }

            String userMessage = chatInput.getText().toString().trim();
            if (userMessage.isEmpty()) {
                ui.showToast("{ai_chat_empty_input}");
                return;
            }

            isSending[0] = true;
            btnSend.setText("{ai_chat_stop}");
            chatInput.setEnabled(false);
            chatInput.setText("");

            // 追加用户消息到显示区
            String currentText = chatDisplay.getText().toString();
            String newText = currentText + "\n\n" + labelYou + ": " + userMessage;
            chatDisplay.setText(newText);
            chatDisplay.selectEnd();

            // 在后台线程发送消息
            new Thread(() -> {
                final StringBuilder accumulatedAiResponse = new StringBuilder();
                chatHelper.sendMessage(userMessage,
                    // onStream - 流式接收 AI 回复
                    accumulated -> AIHelper.runOnMainThread(() -> {
                        accumulatedAiResponse.setLength(0);
                        accumulatedAiResponse.append(accumulated);
                        String displayText = newText + "\n" + labelAi + ": " + accumulated;
                        chatDisplay.setText(displayText);
                        chatDisplay.selectEnd();
                    }),
                    // onComplete - AI 回复完成
                    fullContent -> AIHelper.runOnMainThread(() -> {
                        isSending[0] = false;
                        btnSend.setText("{ai_chat_send}");
                        chatInput.setEnabled(true);
                        chatInput.requestFocusAndShowIME();
                    }),
                    // onError - 出错
                    error -> AIHelper.runOnMainThread(() -> {
                        isSending[0] = false;
                        btnSend.setText("{ai_chat_send}");
                        chatInput.setEnabled(true);
                        String errorText = chatDisplay.getText().toString()
                                + "\n\n" + labelError + ": " + error;
                        chatDisplay.setText(errorText);
                        chatDisplay.selectEnd();
                    })
                );
            }).start();
        });

        // ---- 清空按钮 ----
        btnClear.setOnClickListener(v -> {
            if (isSending[0]) {
                stopActiveRequest();
                isSending[0] = false;
                btnSend.setText("{ai_chat_send}");
                chatInput.setEnabled(true);
            }
            chatHelper.clearHistory();
            chatHelper.rebuildSystemMessage();
            chatDisplay.setText(welcomeText);
            chatInput.setText("");
            ui.showToast("{ai_chat_cleared}");
        });

        // ---- 系统提示词开关 ----
        chkSystemPrompt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chatHelper.setUseSystemPrompt(isChecked);
            chatHelper.rebuildSystemMessage();
        });

        chatInput.requestFocusAndShowIME();
    }

    /**
     * 中断当前活跃的 AI 请求
     */
    private static void stopActiveRequest() {
        HttpURLConnection conn = sActiveConnection;
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ignored) {
            }
            sActiveConnection = null;
        }
    }

    /**
     * 设置活跃连接（供 AIChatHelper 调用以支持中断）
     */
    public static void setActiveConnection(@Nullable HttpURLConnection connection) {
        sActiveConnection = connection;
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