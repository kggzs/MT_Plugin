package com.example.myplugin;

import androidx.annotation.NonNull;

import com.example.myplugin.util.AIHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import bin.mt.plugin.api.ui.PluginButton;
import bin.mt.plugin.api.ui.PluginCheckBox;
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
        // 获取快速提示词列表
        JSONArray quickPrompts;
        int quickPromptCount;
        try {
            quickPrompts = new JSONArray(AIHelper.getQuickPrompts(pluginUI.getContext()));
            quickPromptCount = Math.min(quickPrompts.length(), 8);
        } catch (Exception e) {
            android.util.Log.w("AICodeAnalysisHelper", "加载快速提示词失败: " + e.getMessage());
            quickPrompts = new JSONArray();
            quickPromptCount = 0;
        }
        final JSONArray finalQuickPrompts = quickPrompts;
        final int finalQuickPromptCount = quickPromptCount;

        // 获取 Skill 列表
        JSONArray skills;
        int skillCount;
        try {
            skills = new JSONArray(AIHelper.getSkills(pluginUI.getContext()));
            skillCount = skills.length();
        } catch (Exception e) {
            android.util.Log.w("AICodeAnalysisHelper", "加载 Skill 失败: " + e.getMessage());
            skills = new JSONArray();
            skillCount = 0;
        }
        final JSONArray finalSkills = skills;
        final int finalSkillCount = skillCount;

        // 判断是否有按钮需要显示
        boolean hasButtons = finalQuickPromptCount > 0 || finalSkillCount > 0;

        // 构建视图
        PluginView inputView;
        if (hasButtons) {
            // 构建包含按钮分组的视图
            var viewBuilder = pluginUI.buildVerticalLayout()
                .addTextView().text("请输入提示词").marginBottomDp(4)
                .addEditBox("user_prompt_input").text(defaultUserPrompt).minLines(minLines).maxLines(maxLines).textSize(12).widthMatchParent().marginBottomDp(8);

            // 添加快速提示词分组
            if (finalQuickPromptCount > 0) {
                viewBuilder.addTextView().text("快速提示词").textSize(14).marginBottomDp(4);
                for (int i = 0; i < finalQuickPromptCount; i++) {
                    try {
                        JSONObject prompt = finalQuickPrompts.getJSONObject(i);
                        String name = prompt.getString("name");
                        String buttonName = name.length() > 8 ? name.substring(0, 8) : name;
                        String buttonId = "quick_prompt_btn_" + i;
                        viewBuilder.addButton(buttonId).text(buttonName).widthMatchParent().marginBottomDp(4);
                    } catch (Exception e) {
                        android.util.Log.w("AICodeAnalysisHelper", "构建快速提示词按钮失败: " + e.getMessage());
                    }
                }
            }

            // 添加 Skill 多选框
            if (finalSkillCount > 0) {
                // 如果有快速提示词，添加分隔
                if (finalQuickPromptCount > 0) {
                    viewBuilder.addTextView().text("").marginBottomDp(4);
                }
                viewBuilder.addCheckBox("skill_selector").text("选择自定义 Skill（点击选择）").widthMatchParent();
            }

            inputView = viewBuilder
                .paddingVertical(pluginUI.dialogPaddingVertical())
                .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                .build();
        } else {
            // 没有按钮，使用简单布局
            inputView = pluginUI.buildVerticalLayout()
                .addTextView().text("请输入提示词").marginBottomDp(4)
                .addEditBox("user_prompt_input").text(defaultUserPrompt).minLines(minLines).maxLines(maxLines).textSize(12).widthMatchParent()
                .paddingVertical(pluginUI.dialogPaddingVertical())
                .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                .build();
        }

        PluginEditText userPromptInput = inputView.requireViewById("user_prompt_input");
        userPromptInput.requestFocusAndShowIME();

        // 存储用户选中的 Skill 索引
        final ArrayList<Integer> selectedSkillIndexes = new ArrayList<>();

        // 绑定快速提示词按钮事件 - 追加到现有内容
        if (finalQuickPromptCount > 0 && finalQuickPrompts.length() > 0) {
            for (int i = 0; i < finalQuickPromptCount; i++) {
                try {
                    JSONObject prompt = finalQuickPrompts.getJSONObject(i);
                    String promptContent = prompt.getString("prompt");
                    String buttonId = "quick_prompt_btn_" + i;

                    PluginButton button = inputView.requireViewById(buttonId);
                    final String finalPromptContent = promptContent;
                    button.setOnClickListener(v -> {
                        // 追加到现有内容
                        String currentText = userPromptInput.getText().toString();
                        if (!currentText.isEmpty() && !currentText.endsWith("\n")) {
                            currentText += "\n";
                        }
                        userPromptInput.setText(currentText + finalPromptContent);
                        pluginUI.showToast("已添加快速提示词");
                    });
                } catch (Exception e) {
                    android.util.Log.w("AICodeAnalysisHelper", "绑定快速提示词按钮失败: " + e.getMessage());
                }
            }
        }

        // 绑定 Skill 多选框事件
        if (finalSkillCount > 0 && finalSkills.length() > 0) {
            final PluginCheckBox skillCheckBox = inputView.requireViewById("skill_selector");
            
            // 构建 Skill 名称数组
            CharSequence[] skillNames = new CharSequence[finalSkillCount];
            try {
                for (int i = 0; i < finalSkillCount; i++) {
                    JSONObject skill = finalSkills.getJSONObject(i);
                    skillNames[i] = skill.getString("name");
                }
            } catch (Exception e) {
                android.util.Log.w("AICodeAnalysisHelper", "构建 Skill 名称失败: " + e.getMessage());
            }

            // 点击复选框时弹出多选列表
            skillCheckBox.setOnClickListener(v -> {
                boolean[] checkedItems = new boolean[finalSkillCount];
                for (int i = 0; i < selectedSkillIndexes.size(); i++) {
                    checkedItems[selectedSkillIndexes.get(i)] = true;
                }

                pluginUI.buildDialog()
                    .setTitle("选择自定义 Skill")
                    .setMultiChoiceItems(skillNames, checkedItems, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            if (!selectedSkillIndexes.contains(which)) {
                                selectedSkillIndexes.add(which);
                            }
                        } else {
                            selectedSkillIndexes.remove(Integer.valueOf(which));
                        }
                    })
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 更新复选框显示文本
                        if (selectedSkillIndexes.isEmpty()) {
                            skillCheckBox.setText("选择自定义 Skill（点击选择）");
                        } else {
                            StringBuilder sb = new StringBuilder("已选择 Skill：");
                            for (int i = 0; i < selectedSkillIndexes.size(); i++) {
                                if (i > 0) sb.append("、");
                                try {
                                    sb.append(skillNames[selectedSkillIndexes.get(i)]);
                                } catch (Exception e) {
                                    sb.append("未知");
                                }
                            }
                            skillCheckBox.setText(sb.toString());
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            });
        }

        // 构建最终的用户提示词（包含选中的 Skill 内容）
        pluginUI.buildDialog()
                .setTitle("设置分析提示词")
                .setView(inputView)
                .setPositiveButton("开始分析", (dialog, which) -> {
                    String userPrompt = userPromptInput.getText().toString().trim();
                    
                    // 如果有选中的 Skill，追加到提示词后面
                    if (!selectedSkillIndexes.isEmpty() && finalSkills.length() > 0) {
                        StringBuilder promptBuilder = new StringBuilder(userPrompt);
                        for (Integer index : selectedSkillIndexes) {
                            try {
                                JSONObject skill = finalSkills.getJSONObject(index);
                                String skillPrompt = skill.getString("prompt");
                                if (!skillPrompt.isEmpty()) {
                                    if (promptBuilder.length() > 0) {
                                        promptBuilder.append("\n\n");
                                    }
                                    promptBuilder.append(skillPrompt);
                                }
                            } catch (Exception e) {
                                android.util.Log.w("AICodeAnalysisHelper", "获取 Skill 内容失败: " + e.getMessage());
                            }
                        }
                        userPrompt = promptBuilder.toString();
                    }
                    
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
                    // 二次确认对话框
                    pluginUI.buildDialog()
                        .setTitle("确认取消")
                        .setMessage("确定要取消当前分析吗？")
                        .setPositiveButton("确定取消", (dialog2, which2) -> {
                            isCancelled = true;
                            d.dismiss();
                            dialog2.dismiss();
                            pluginUI.showToast("已取消分析");
                        })
                        .setNegativeButton("继续分析", null)
                        .show();
                })
                .setNeutralButton("后台运行", (d, which) -> {
                    d.dismiss();
                    pluginUI.showToast("已在后台运行分析，完成后将弹出结果");
                    PluginEditText thinkingEdit2 = contentView.requireViewById("thinking_edit");
                    PluginEditText resultEdit2 = contentView.requireViewById("result_edit");
                    startBackgroundAnalysis(pluginUI, code, userPrompt, thinkingEdit2, resultEdit2);
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
     * 后台AI分析（不阻塞当前界面）
     *
     * @param pluginUI     插件UI上下文
     * @param code         待分析的代码
     * @param userPrompt   用户提示词
     * @param thinkingEdit 思考过程编辑框（用于传递引用，实际不使用）
     * @param resultEdit   结果编辑框（用于传递引用，实际不使用）
     */
    private void startBackgroundAnalysis(@NonNull PluginUI pluginUI, @NonNull String code, 
                                         @NonNull String userPrompt, 
                                         @NonNull PluginEditText thinkingEdit,
                                         @NonNull PluginEditText resultEdit) {
        new Thread(() -> {
            try {
                // 在后台线程中执行分析，不传入UI组件
                String[] result = AIHelper.analyzeCodeWithUserPromptNoUI(
                        pluginUI.getContext(),
                        code,
                        userPrompt
                );

                if (isCancelled) {
                    return;
                }

                if (result != null) {
                    AIHelper.runOnMainThread(() -> {
                        if (!isCancelled) {
                            // 分析完成，弹出结果对话框
                            showResultDialog(pluginUI, result[0]);
                        }
                    });
                }
            } catch (Exception e) {
                AIHelper.runOnMainThread(() -> {
                    if (!isCancelled) {
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("返回空结果")) {
                            showEmptyResultDialog(pluginUI, errorMsg);
                        } else {
                            pluginUI.showToast("后台分析失败: " + errorMsg);
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
