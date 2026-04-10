package com.example.myplugin;

import androidx.annotation.NonNull;

import com.example.myplugin.util.AIHelper;

import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.PluginDialog;

/**
 * AI代码分析辅助类
 * 封装ToolMenu和FloatingMenu的公共逻辑，消除重复代码
 */
public class AICodeAnalysisHelper {

    private volatile boolean isCancelled = false;

    private final String defaultUserPrompt;
    private final int minLines;
    private final int maxLines;

    /**
     * 构造函数
     *
     * @param defaultUserPrompt 默认用户提示词
     * @param minLines          输入框最小行数
     * @param maxLines          输入框最大行数
     */
    public AICodeAnalysisHelper(@NonNull String defaultUserPrompt, int minLines, int maxLines) {
        this.defaultUserPrompt = defaultUserPrompt;
        this.minLines = minLines;
        this.maxLines = maxLines;
    }

    /**
     * 显示提示词输入对话框
     *
     * @param pluginUI 插件UI上下文
     * @param code     待分析的代码
     */
    public void showPromptInputDialog(@NonNull PluginUI pluginUI, @NonNull String code) {
        PluginView inputView = pluginUI.buildVerticalLayout()
                .addTextView().text("请输入补充提示词（将作为系统提示词的前缀）：").marginBottomDp(4)
                .addEditBox("user_prompt_input").text(defaultUserPrompt).minLines(minLines).maxLines(maxLines).textSize(12).widthMatchParent()
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
     *
     * @param pluginUI   插件UI上下文
     * @param code       待分析的代码
     * @param userPrompt 用户提示词
     */
    private void startAnalysis(@NonNull PluginUI pluginUI, @NonNull String code, @NonNull String userPrompt) {
        isCancelled = false;

        PluginView contentView = pluginUI.buildVerticalLayout()
                .addTextView().text("思考过程:").textSize(14).paddingBottomDp(4)
                .addEditBox("thinking_edit").text("正在初始化...").minLines(5).maxLines(10).textSize(12).readOnly().widthMatchParent().softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
                .paddingVertical(pluginUI.dialogPaddingVertical())
                .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                .addTextView().text("分析结果:").textSize(14).paddingTopDp(8).paddingBottomDp(4)
                .addEditBox("result_edit").text("等待分析...").minLines(10).maxLines(15).textSize(12).readOnly().widthMatchParent().softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
                .paddingVertical(pluginUI.dialogPaddingVertical())
                .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                .paddingBottom(16)
                .build();

        PluginDialog dialog = pluginUI.buildDialog()
                .setTitle("AI 分析中...")
                .setView(contentView)
                .setCancelable(false)
                .setNegativeButton("取消", (d, which) -> {
                    isCancelled = true;
                    d.dismiss();
                    pluginUI.showToast("已取消分析");
                })
                .show();

        PluginEditText thinkingEdit = contentView.requireViewById("thinking_edit");
        PluginEditText resultEdit = contentView.requireViewById("result_edit");

        new Thread(() -> {
            try {
                String[] result = AIHelper.analyzeCodeWithUserPrompt(
                        pluginUI.getContext(),
                        code,
                        userPrompt,
                        thinkingEdit,
                        resultEdit,
                        dialog
                );

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

    /**
     * 显示分析结果对话框
     * 使用和思考过程一致的显示方式
     *
     * @param pluginUI 插件UI上下文
     * @param result   分析结果
     */
    private void showResultDialog(PluginUI pluginUI, String result) {
        // 过滤 Markdown 格式符号
        String cleanedResult = cleanMarkdown(result);
        
        PluginView resultView = pluginUI.buildVerticalLayout()
                .addTextView().text("分析结果").textSize(16).paddingBottomDp(8)
                .addEditBox("result_edit").text(cleanedResult).minLines(10).maxLines(20).textSize(12).readOnly().widthMatchParent().softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
                .paddingVertical(pluginUI.dialogPaddingVertical())
                .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                .paddingBottom(16)
                .build();

        pluginUI.buildDialog()
                .setView(resultView)
                .setNegativeButton("取消", null)
                .setPositiveButton("复制", (dialog, which) -> {
                    pluginUI.getContext().setClipboardText(cleanedResult);
                    pluginUI.showToast("已复制到剪贴板");
                })
                .show();
    }

    /**
     * 清理 Markdown 格式符号，提升纯文本阅读体验
     * 
     * @param markdown 包含 Markdown 格式的文本
     * @return 清理后的纯文本
     */
    private String cleanMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return markdown;
        }
        
        String result = markdown;
        
        // 移除标题符号: # ## ### #### ##### ######
        result = result.replaceAll("(?m)^#{1,6}\\s+", "");
        
        // 移除粗体和斜体符号: **text** *text* __text__ _text_
        result = result.replaceAll("\\*\\*(.+?)\\*\\*", "$1");  // **text**
        result = result.replaceAll("\\*(.+?)\\*", "$1");        // *text*
        result = result.replaceAll("__(.+?)__", "$1");          // __text__
        result = result.replaceAll("_(.+?)_", "$1");            // _text_
        
        // 移除删除线符号: ~~text~~
        result = result.replaceAll("~~(.+?)~~", "$1");
        
        // 移除代码块符号: `code` ```code```
        result = result.replaceAll("```[\\s\\S]*?```", "");    // 多行代码块
        result = result.replaceAll("`(.+?)`", "$1");            // 行内代码
        
        // 移除链接: [text](url)
        result = result.replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1");
        
        // 移除图片: ![alt](url)
        result = result.replaceAll("!\\[(.+?)\\]\\(.+?\\)", "$1");
        
        // 移除引用符号: > text
        result = result.replaceAll("(?m)^>\\s+", "");
        
        // 移除水平线: --- *** ___
        result = result.replaceAll("(?m)^[-]{3,}\\s*$", "");
        result = result.replaceAll("(?m)^\\*{3,}\\s*$", "");
        result = result.replaceAll("(?m)^_{3,}\\s*$", "");
        
        // 移除列表符号: - * + 和数字列表: 1. 2.
        result = result.replaceAll("(?m)^[\\s]*[-*+]\\s+", "");
        result = result.replaceAll("(?m)^[\\s]*\\d+\\.\\s+", "");
        
        // 移除多余的空行（保留最多一个空行）
        result = result.replaceAll("\n{3,}", "\n\n");
        
        return result.trim();
    }

    /**
     * 显示空结果提示对话框
     *
     * @param pluginUI 插件UI上下文
     * @param errorMsg 错误信息
     */
    private void showEmptyResultDialog(PluginUI pluginUI, String errorMsg) {
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
