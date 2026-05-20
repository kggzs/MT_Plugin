package com.example.myplugin;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

import com.example.myplugin.util.AIHelper;
import com.example.myplugin.util.TimeFormatHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class MyPreference implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("{plugin_name}")
               .subtitle("{plugin_author}");

        builder.addText("{plugin_description}")
               .summary("{plugin_description}");

        builder.addText("{plugin_website}")
               .summary("www.kggzs.cn")
               .onClick((ui, preference) -> {
                   context.setClipboardText("www.kggzs.cn");
                   context.showToast("{copy_success_clipboard}");
               });

        builder.addText("{plugin_source}")
               .summary("https://github.com/kggzs/MT_Plugin")
               .onClick((ui, preference) -> {
                   context.setClipboardText("https://github.com/kggzs/MT_Plugin");
                   context.showToast("{copy_success_clipboard}");
               });

        builder.addText("{support_author}")
               .summary("{support_author_summary}")
               .onClick((ui, preference) -> {
                   showSupportDialog(ui, context);
               });

        builder.addHeader("{ai_config_group}");

        builder.addText("{api_config}")
               .summary("{api_config_summary}")
               .onClick((ui, preference) -> {
                   showApiConfigDialog(ui, context);
               });

        builder.addText("{ai_capability_config}")
               .summary("{ai_capability_config_summary}")
               .onClick((ui, preference) -> {
                   showAiCapabilityDialog(ui, context);
               });

        builder.addHeader("{time_config_group}");

        builder.addText("{time_format_config}")
               .summary("{time_format_config_summary}")
               .onClick((ui, preference) -> {
                   showTimeFormatConfigDialog(ui, context);
               });

        builder.addText("{reset_config}")
               .summary("{reset_config_summary}")
               .onClick((ui, preference) -> {
                   // 二次确认对话框
                   ui.buildDialog()
                       .setTitle("{confirm_reset_title}")
                       .setMessage("{confirm_reset_message}")
                       .setPositiveButton("{confirm_reset_positive}", (dialog, which) -> {
                           AIHelper.resetToDefault(context);
                           context.showToast("{config_reset_success}");
                           dialog.dismiss();
                       })
                       .setNegativeButton("{confirm_reset_negative}", null)
                       .show();
               });

        builder.addHeader("{features_group}");

        builder.addText("{features_title}")
               .summary("{features_summary}");

        builder.addText("{encode_decode_function}")
               .summary("{encode_decode_usage}");

        builder.addText("{base64_encode}")
               .summary("{base64_encode_usage}");

        builder.addText("{hex_encode}")
               .summary("{hex_encode_usage}");

        builder.addText("{unicode_encode}")
               .summary("{unicode_encode_usage}");

        builder.addText("{rot13_encode}")
               .summary("{rot13_encode_usage}");

        builder.addText("{binary_encode}")
               .summary("{binary_encode_usage}");

        builder.addText("{hash_function}")
               .summary("{hash_usage}");

        builder.addText("{timestamp_function}")
               .summary("{timestamp_usage}");

        builder.addText("{ai_code_analysis}")
               .summary("{ai_code_analysis_usage}");

        builder.addText("{ai_quick_analysis}")
               .summary("{ai_quick_analysis_usage}");
    }

    /**
     * 显示 API 统一配置对话框
     */
    private void showApiConfigDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        String currentApiKey = AIHelper.getApiKey(context);
        String maskedApiKey = maskApiKey(currentApiKey);

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{api_address}").textSize(14).marginBottom(smallMargin)
            .addEditText("api_url").hint("{api_url_hint}")
                .text(AIHelper.getApiUrl(context)).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{model_name}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditText("model_name").hint("{model_name_hint}")
                .text(AIHelper.getAiModel(context)).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{api_key}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditText("api_key").hint("{api_key_hint}")
                .text(maskedApiKey).widthMatchParent().marginBottom(smallMargin)
            .addButton("toggle_key_visibility").text("{show_api_key}").widthMatchParent().marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.PluginEditText apiUrlInput = view.requireViewById("api_url");
        bin.mt.plugin.api.ui.PluginEditText modelNameInput = view.requireViewById("model_name");
        bin.mt.plugin.api.ui.PluginEditText apiKeyInput = view.requireViewById("api_key");
        bin.mt.plugin.api.ui.PluginButton toggleBtn = view.requireViewById("toggle_key_visibility");

        apiKeyInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final boolean[] isPasswordVisible = {false};
        final String originalApiKey = currentApiKey;

        toggleBtn.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                apiKeyInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                apiKeyInput.setText(maskedApiKey);
                toggleBtn.setText("{show_api_key}");
                isPasswordVisible[0] = false;
            } else {
                apiKeyInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                apiKeyInput.setText(originalApiKey);
                toggleBtn.setText("{hide_api_key}");
                isPasswordVisible[0] = true;
            }
        });

        ui.buildDialog()
            .setTitle("{api_config}")
            .setView(view)
            .setPositiveButton("{save}", (dialog, which) -> {
                String apiUrl = apiUrlInput.getText().toString().trim();
                String modelName = modelNameInput.getText().toString().trim();
                String apiKey = apiKeyInput.getText().toString().trim();

                boolean hasValue = false;

                if (!apiUrl.isEmpty()) {
                    if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://")) {
                        context.showToast("{url_format_error}");
                        return;
                    }
                    AIHelper.setApiUrl(context, apiUrl);
                    hasValue = true;
                }

                if (!modelName.isEmpty()) {
                    AIHelper.setAiModel(context, modelName);
                    hasValue = true;
                }

                if (!apiKey.isEmpty()) {
                    if (isPasswordVisible[0]) {
                        AIHelper.setApiKey(context, apiKey);
                    } else {
                        if (!apiKey.equals(maskApiKey(originalApiKey))) {
                            AIHelper.setApiKey(context, apiKey);
                        }
                    }
                    hasValue = true;
                }

                if (hasValue) {
                    context.showToast("{saved}");
                } else {
                    context.showToast("{no_input_detected}");
                }
            })
            .setNegativeButton("{cancel}", null)
            .setNeutralButton("{reset_config}", (dialog, which) -> {
                AIHelper.setApiUrl(context, "");
                AIHelper.setAiModel(context, "");
                AIHelper.setApiKey(context, "");
                context.showToast("{reset_to_default}");
            })
            .show();
    }

    /**
     * 遮蔽 API 密钥，只显示前后各4个字符，中间用星号代替
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }
        if (apiKey.length() <= 8) {
            return apiKey;
        }
        int visibleChars = 4;
        String start = apiKey.substring(0, Math.min(visibleChars, apiKey.length()));
        String end = apiKey.substring(Math.max(apiKey.length() - visibleChars, 0));
        int maskLength = apiKey.length() - visibleChars * 2;
        if (maskLength <= 0) {
            return apiKey;
        }
        StringBuilder masked = new StringBuilder(start);
        for (int i = 0; i < maskLength; i++) {
            masked.append("*");
        }
        masked.append(end);
        return masked.toString();
    }

    /**
     * 显示 AI 能力配置对话框（提示词 + Skill）
     */
    private void showAiCapabilityDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        // 构建视图
        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{global_analysis_prompt_label}").textSize(14).marginBottom(smallMargin)
            .addEditBox("global_prompt").text(AIHelper.getPrompt(context))
                .minLines(4).maxLines(8).widthMatchParent().marginBottom(smallMargin).softWrap(bin.mt.plugin.api.ui.PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addTextView().text("{short_analysis_prompt_label}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("short_prompt").text(AIHelper.getShortPrompt(context))
                .minLines(2).maxLines(4).widthMatchParent().marginBottom(smallMargin).softWrap(bin.mt.plugin.api.ui.PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addButton("manage_quick_prompts").text("{quick_prompts_config}").widthMatchParent().marginBottom(smallMargin)
            .addButton("manage_skills").text("{manage_skills}").widthMatchParent()
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.PluginEditText globalPromptInput = view.requireViewById("global_prompt");
        bin.mt.plugin.api.ui.PluginEditText shortPromptInput = view.requireViewById("short_prompt");

        ui.buildDialog()
            .setTitle("{ai_capability_config}")
            .setView(view)
            .setPositiveButton("{save}", (dialog, which) -> {
                String globalPrompt = globalPromptInput.getText().toString();
                String shortPrompt = shortPromptInput.getText().toString();
                AIHelper.setPrompt(context, globalPrompt);
                AIHelper.setShortPrompt(context, shortPrompt);
                context.showToast("{saved}");
            })
            .setNegativeButton("{cancel}", null)
            .show();

        // 绑定 Skill 管理按钮
        bin.mt.plugin.api.ui.PluginButton manageSkillsBtn = view.requireViewById("manage_skills");
        manageSkillsBtn.setOnClickListener(v -> {
            showSkillManagementDialog(ui, context, globalPromptInput);
        });

        // 绑定快速提示词管理按钮
        bin.mt.plugin.api.ui.PluginButton manageQuickPromptsBtn = view.requireViewById("manage_quick_prompts");
        manageQuickPromptsBtn.setOnClickListener(v -> {
            showQuickPromptsManagementDialog(ui, context);
        });
    }

    /**
     * 显示 Skill 管理对话框
     */
    private void showSkillManagementDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context,
                                           bin.mt.plugin.api.ui.PluginEditText promptInput) {
        try {
            JSONArray skillsArray = new JSONArray(AIHelper.getSkills(context));
            ArrayList<String> skillNames = new ArrayList<>();
            for (int i = 0; i < skillsArray.length(); i++) {
                JSONObject skill = skillsArray.getJSONObject(i);
                skillNames.add(skill.getString("name"));
            }
            skillNames.add(context.getString("{new_skill}"));

            CharSequence[] items = skillNames.toArray(new CharSequence[0]);

            ui.buildDialog()
                .setTitle("{skill_management}")
                .setItems(items, (d, which) -> {
                    if (which == items.length - 1) {
                        showSkillEditorDialog(ui, context, null, -1, promptInput);
                    } else {
                        showSkillEditorDialog(ui, context, items[which].toString(), which, promptInput);
                    }
                })
                .setNegativeButton("{close}", null)
                .show();
        } catch (Exception e) {
            context.showToast("{load_skill_failed}: " + e.getMessage());
        }
    }

    /**
     * 显示快速提示词管理对话框
     */
    private void showQuickPromptsManagementDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context) {
        try {
            JSONArray promptsArray = new JSONArray(AIHelper.getQuickPrompts(context));
            ArrayList<String> promptNames = new ArrayList<>();
            for (int i = 0; i < promptsArray.length(); i++) {
                JSONObject prompt = promptsArray.getJSONObject(i);
                promptNames.add(prompt.getString("name"));
            }
            promptNames.add("+ " + context.getString("{add_quick_prompt}"));

            CharSequence[] items = promptNames.toArray(new CharSequence[0]);

            ui.buildDialog()
                .setTitle("{quick_prompts_config}")
                .setItems(items, (d, which) -> {
                    if (which == items.length - 1) {
                        // 添加新的快速提示词
                        if (promptsArray.length() >= 10) {
                            context.showToast("{quick_prompt_max_limit}");
                        } else {
                            showQuickPromptEditorDialog(ui, context, null, -1);
                        }
                    } else {
                        showQuickPromptEditorDialog(ui, context, items[which].toString(), which);
                    }
                })
                .setNegativeButton("{close}", null)
                .show();
        } catch (Exception e) {
            context.showToast("加载快速提示词失败: " + e.getMessage());
        }
    }

    /**
     * 显示快速提示词编辑对话框
     */
    private void showQuickPromptEditorDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context,
                                             String promptName, int promptIndex) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        String currentName = "";
        String currentPrompt = "";

        if (promptIndex >= 0) {
            try {
                JSONArray promptsArray = new JSONArray(AIHelper.getQuickPrompts(context));
                JSONObject prompt = promptsArray.getJSONObject(promptIndex);
                currentName = prompt.getString("name");
                currentPrompt = prompt.getString("prompt");
            } catch (Exception e) {
                context.showToast("加载快速提示词数据失败: " + e.getMessage());
                return;
            }
        }

        final int finalPromptIndex = promptIndex;

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{quick_prompt_name_hint}").textSize(14).marginBottom(smallMargin)
            .addEditText("prompt_name").text(currentName).hint("{quick_prompt_name_hint}").widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{quick_prompt_content_hint}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("prompt_content").text(currentPrompt).minLines(6).maxLines(10).widthMatchParent().marginBottom(smallMargin).softWrap(bin.mt.plugin.api.ui.PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addButton("delete_prompt_btn").text("{delete_quick_prompt}").widthMatchParent()
                .textColor(0xFFFF5555).marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        // 如果是新建，隐藏删除按钮
        if (promptIndex < 0) {
             view.requireViewById("delete_prompt_btn").setGone();
        }

        bin.mt.plugin.api.ui.PluginEditText nameInput = view.requireViewById("prompt_name");
        bin.mt.plugin.api.ui.PluginEditText promptEdit = view.requireViewById("prompt_content");

        // 删除按钮逻辑
        if (promptIndex >= 0) {
            view.requireViewById("delete_prompt_btn").setOnClickListener(v -> {
                ui.buildDialog()
                    .setTitle("{confirm_delete}")
                    .setMessage("{sure_to_delete_skill}: " + nameInput.getText() + "?")
                    .setPositiveButton("{delete}", (d, w) -> {
                        try {
                            JSONArray promptsArray = new JSONArray(AIHelper.getQuickPrompts(context));
                            promptsArray.remove(finalPromptIndex);
                            AIHelper.setQuickPrompts(context, promptsArray.toString());
                            context.showToast("{deleted}");
                            d.dismiss();
                            showQuickPromptsManagementDialog(ui, context);
                        } catch (Exception e) {
                            context.showToast("删除失败: " + e.getMessage());
                        }
                    })
                    .setNegativeButton("{cancel}", null)
                    .show();
            });
        }

        bin.mt.plugin.api.ui.dialog.PluginDialog dialog = ui.buildDialog()
            .setTitle(promptIndex >= 0 ? "{edit_quick_prompt}" : "{add_quick_prompt}")
            .setView(view)
            .setPositiveButton("{save}", (d, which) -> {
                String name = nameInput.getText().toString().trim();
                String prompt = promptEdit.getText().toString();
                if (name.isEmpty()) {
                    context.showToast("{name_cannot_be_empty}");
                    return;
                }
                try {
                    JSONArray promptsArray = new JSONArray(AIHelper.getQuickPrompts(context));
                    JSONObject promptObj = new JSONObject();
                    promptObj.put("name", name);
                    promptObj.put("prompt", prompt);

                    if (promptIndex >= 0) {
                        promptsArray.put(promptIndex, promptObj);
                    } else {
                        if (promptsArray.length() >= 10) {
                            context.showToast("{quick_prompt_max_limit}");
                            return;
                        }
                        promptsArray.put(promptObj);
                    }
                    AIHelper.setQuickPrompts(context, promptsArray.toString());
                    context.showToast("{skill_saved}");
                    d.dismiss();
                    showQuickPromptsManagementDialog(ui, context);
                } catch (Exception e) {
                    context.showToast("{save_failed}: " + e.getMessage());
                }
            })
            .setNegativeButton("{cancel}", null)
            .show();
    }

    /**
     * 显示 Skill 编辑/应用对话框
     */
    private void showSkillEditorDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context, 
                                       String skillName, int skillIndex, 
                                       bin.mt.plugin.api.ui.PluginEditText promptInput) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        String currentName = "";
        String currentPrompt = "";

        if (skillIndex >= 0) {
            try {
                JSONArray skillsArray = new JSONArray(AIHelper.getSkills(context));
                JSONObject skill = skillsArray.getJSONObject(skillIndex);
                currentName = skill.getString("name");
                currentPrompt = skill.getString("prompt");
            } catch (Exception e) {
                context.showToast("{load_skill_data_failed}: " + e.getMessage());
                return;
            }
        }

        final int finalSkillIndex = skillIndex;

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{skill_name}").textSize(14).marginBottom(smallMargin)
            .addEditText("skill_name").text(currentName).hint("{input_name}").widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{prompt_content}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("skill_prompt").text(currentPrompt).minLines(8).maxLines(15).widthMatchParent().marginBottom(smallMargin).softWrap(bin.mt.plugin.api.ui.PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addButton("delete_btn").text("{delete_this_skill}").widthMatchParent()
                .textColor(0xFFFF5555).marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        // 如果是新建，隐藏删除按钮（本来就不存在）
        if (skillIndex < 0) {
             view.requireViewById("delete_btn").setGone();
        }

        bin.mt.plugin.api.ui.PluginEditText nameInput = view.requireViewById("skill_name");
        bin.mt.plugin.api.ui.PluginEditText promptEdit = view.requireViewById("skill_prompt");

        // 删除按钮逻辑
        if (skillIndex >= 0) {
            view.requireViewById("delete_btn").setOnClickListener(v -> {
                ui.buildDialog()
                    .setTitle("{confirm_delete}")
                    .setMessage("{sure_to_delete_skill}: " + nameInput.getText() + "?")
                    .setPositiveButton("{delete}", (d, w) -> {
                        try {
                            JSONArray skillsArray = new JSONArray(AIHelper.getSkills(context));
                            skillsArray.remove(finalSkillIndex);
                            AIHelper.setSkills(context, skillsArray.toString());
                            context.showToast("{deleted}");
                            d.dismiss();
                            showSkillManagementDialog(ui, context, promptInput); 
                        } catch (Exception e) {
                            context.showToast("{delete_failed}: " + e.getMessage());
                        }
                    })
                    .setNegativeButton("{cancel}", null)
                    .show();
            });
        }

        bin.mt.plugin.api.ui.dialog.PluginDialog dialog = ui.buildDialog()
            .setTitle(skillIndex >= 0 ? "{edit_skill}" : "{new_skill}")
            .setView(view)
            .setPositiveButton("{save}", (d, which) -> {
                String name = nameInput.getText().toString().trim();
                String prompt = promptEdit.getText().toString();
                if (name.isEmpty()) {
                    context.showToast("{name_cannot_be_empty}");
                    return;
                }
                try {
                    JSONArray skillsArray = new JSONArray(AIHelper.getSkills(context));
                    JSONObject skill = new JSONObject();
                    skill.put("name", name);
                    skill.put("prompt", prompt);

                    if (skillIndex >= 0) {
                        skillsArray.put(skillIndex, skill);
                    } else {
                        skillsArray.put(skill);
                    }
                    AIHelper.setSkills(context, skillsArray.toString());
                    context.showToast("{skill_saved}");
                } catch (Exception e) {
                    context.showToast("{save_failed}: " + e.getMessage());
                }
            })
            .setNegativeButton("{cancel}", null)
            .show();

        if (skillIndex >= 0) {
            dialog.setNeutralButton("{apply}", (d, which) -> {
                promptInput.setText(promptEdit.getText());
                context.showToast("{skill_applied}: " + nameInput.getText());
            });
        }
    }
    
    /**
     * 显示多行输入对话框
     */
    private void showMultilineInputDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context,
                                          String title, String defaultValue,
                                          java.util.function.Consumer<String> onSave) {
        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addEditBox("input").text(defaultValue).minLines(10).maxLines(20).widthMatchParent().softWrap(bin.mt.plugin.api.ui.PluginEditText.SOFT_WRAP_KEEP_WORD)
            .paddingHorizontal(ui.dialogPaddingHorizontal())
            .paddingVertical(ui.dialogPaddingVertical())
            .build();

        bin.mt.plugin.api.ui.PluginEditText input = view.requireViewById("input");

        ui.buildDialog()
            .setTitle(title)
            .setView(view)
            .setPositiveButton("{save}", (dialog, which) -> {
                String value = input.getText().toString();
                onSave.accept(value);
                context.showToast("{saved}");
            })
            .setNegativeButton("{cancel}", null)
            .show();
    }

    /**
     * 显示时间格式配置对话框
     */
    private void showTimeFormatConfigDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context) {
        int currentFormat = TimeFormatHelper.getTimeFormatType(context);
        int currentMode = TimeFormatHelper.getTimeMode(context);

        int padding = ui.dp2px(16);
        int smallMargin = ui.dp2px(8);
        int sectionMargin = ui.dp2px(16);

        // 获取当前预览
        String currentPreview = TimeFormatHelper.getFormattedTime(context, new Date());

        // 定义格式按钮文本（不带时间）
        String[] formatTextsDateOnly = {
            "1. 标准中文格式 - 2026年5月20日",
            "2. ISO格式 - 2026-05-20",
            "3. 斜杠格式 - 2026/5/20",
            "4. 紧凑格式 - 20260520",
            "5. 带星期 - 2026年5月20日 星期三",
            "6. 传统汉字 - 丙午年四月初四",
            "7. 农历简写 - 农历四月初四",
            "8. 干支纪日 - 丙午年 癸巳月 甲午日",
            "9. 农历数字 - 农历2026年四月初四",
            "10. 公农历并列 - 2026-05-20（丙午年四月初四）"
        };

        // 定义格式按钮文本（带时间）
        String[] formatTextsWithTime = {
            "1. 标准中文格式 - 2026年5月20日 19:29:55",
            "2. ISO格式 - 2026-05-20 19:29:55",
            "3. 斜杠格式 - 2026/5/20 19:29:55",
            "4. 紧凑格式 - 20260520 19:29:55",
            "5. 带星期 - 2026年5月20日 星期三 19:29:55",
            "6. 传统汉字 - 丙午年四月初四 19:29:55",
            "7. 农历简写 - 农历四月初四 19:29:55",
            "8. 干支纪日 - 丙午年 癸巳月 甲午日 19:29:55",
            "9. 农历数字 - 农历2026年四月初四 19:29:55",
            "10. 公农历并列 - 2026-05-20（丙午年四月初四） 19:29:55"
        };

        // 根据当前模式选择显示的文本
        String[] formatTexts = (currentMode == TimeFormatHelper.MODE_DATE_ONLY) ? formatTextsDateOnly : formatTextsWithTime;
        boolean isCustom = (currentMode == TimeFormatHelper.MODE_CUSTOM);

        // 构建分类列表视图
        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView("current_preview").text("当前格式: " + currentPreview)
                .textSize(16).textColor(0xFF4CAF50).marginBottom(sectionMargin)
            .addTextView().text("【显示模式】").textSize(14).textColor(0xFF666666).marginBottom(smallMargin)
            .addButton("mode_date_only").text(currentMode == TimeFormatHelper.MODE_DATE_ONLY ? "✓ 不带时间（只显示日期）" : "不带时间（只显示日期）").widthMatchParent().marginBottom(smallMargin)
            .addButton("mode_date_time").text(currentMode == TimeFormatHelper.MODE_DATE_TIME ? "✓ 带时间（显示日期+时分秒）" : "带时间（显示日期+时分秒）").widthMatchParent().marginBottom(smallMargin)
            .addButton("mode_custom").text(currentMode == TimeFormatHelper.MODE_CUSTOM ? "✓ 自定义格式" : "自定义格式").widthMatchParent().marginBottom(sectionMargin)
            .addTextView().text("【公历格式】").textSize(14).textColor(0xFF666666).marginBottom(smallMargin)
            .addButton("format_0").text(currentFormat == 0 && !isCustom ? "✓ " + formatTexts[0] : formatTexts[0]).widthMatchParent().marginBottom(smallMargin)
            .addButton("format_1").text(currentFormat == 1 && !isCustom ? "✓ " + formatTexts[1] : formatTexts[1]).widthMatchParent().marginBottom(smallMargin)
            .addButton("format_2").text(currentFormat == 2 && !isCustom ? "✓ " + formatTexts[2] : formatTexts[2]).widthMatchParent().marginBottom(smallMargin)
            .addButton("format_3").text(currentFormat == 3 && !isCustom ? "✓ " + formatTexts[3] : formatTexts[3]).widthMatchParent().marginBottom(smallMargin)
            .addButton("format_4").text(currentFormat == 4 && !isCustom ? "✓ " + formatTexts[4] : formatTexts[4]).widthMatchParent().marginBottom(sectionMargin)
            .addTextView().text("【农历格式】").textSize(14).textColor(0xFF666666).marginBottom(smallMargin)
            .addButton("format_5").text(currentFormat == 5 && !isCustom ? "✓ " + formatTexts[5] : formatTexts[5]).widthMatchParent().marginBottom(smallMargin)
            .addButton("format_6").text(currentFormat == 6 && !isCustom ? "✓ " + formatTexts[6] : formatTexts[6]).widthMatchParent().marginBottom(smallMargin)
            .addButton("format_7").text(currentFormat == 7 && !isCustom ? "✓ " + formatTexts[7] : formatTexts[7]).widthMatchParent().marginBottom(smallMargin)
            .addButton("format_8").text(currentFormat == 8 && !isCustom ? "✓ " + formatTexts[8] : formatTexts[8]).widthMatchParent().marginBottom(smallMargin)
            .addButton("format_9").text(currentFormat == 9 && !isCustom ? "✓ " + formatTexts[9] : formatTexts[9]).widthMatchParent()
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        // 创建对话框
        bin.mt.plugin.api.ui.dialog.PluginDialog dialog = ui.buildDialog()
            .setTitle("{time_format_config}")
            .setView(view)
            .setNegativeButton("{close}", null)
            .show();

        // 绑定模式按钮事件
        bin.mt.plugin.api.ui.PluginButton modeDateOnlyBtn = view.requireViewById("mode_date_only");
        bin.mt.plugin.api.ui.PluginButton modeDateTimeBtn = view.requireViewById("mode_date_time");
        bin.mt.plugin.api.ui.PluginButton modeCustomBtn = view.requireViewById("mode_custom");

        modeDateOnlyBtn.setOnClickListener(v -> {
            TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_DATE_ONLY);
            String preview = TimeFormatHelper.getFormattedTime(context, new Date());
            context.showToast("已切换: " + preview);
            dialog.dismiss();
            showTimeFormatConfigDialog(ui, context);
        });

        modeDateTimeBtn.setOnClickListener(v -> {
            TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_DATE_TIME);
            String preview = TimeFormatHelper.getFormattedTime(context, new Date());
            context.showToast("已切换: " + preview);
            dialog.dismiss();
            showTimeFormatConfigDialog(ui, context);
        });

        modeCustomBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showCustomFormatEditorDialog(ui, context);
        });

        // 绑定预设格式按钮点击事件
        for (int i = 0; i <= 9; i++) {
            bin.mt.plugin.api.ui.PluginButton btn = view.requireViewById("format_" + i);
            final int formatIndex = i;
            final bin.mt.plugin.api.ui.dialog.PluginDialog finalDialog = dialog;
            btn.setOnClickListener(v -> {
                int mode = TimeFormatHelper.getTimeMode(context);
                if (mode == TimeFormatHelper.MODE_CUSTOM) {
                    TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_DATE_TIME);
                }
                TimeFormatHelper.setTimeFormatType(context, formatIndex);
                String preview = TimeFormatHelper.getFormattedTime(context, new Date());
                context.showToast("已切换: " + preview);
                finalDialog.dismiss();
            });
        }
    }

    /**
     * 显示自定义格式编辑对话框
     */
    private void showCustomFormatEditorDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(16);
        int smallMargin = ui.dp2px(8);

        String currentFormat = TimeFormatHelper.getCustomFormatString(context);
        String preview = TimeFormatHelper.parseCustomFormat(currentFormat, new Date());

        // 详细的格式说明
        String formatHelp = "【年份】yyyy-4位(2026) yy-2位(26)\n" +
            "【月份】MM-补0(05) M-不补0(5) N-农历(四月)\n" +
            "【日期】dd-补0(20) d-不补0(20) e-农历(初四)\n" +
            "【星期】E-周几(周三)\n" +
            "【时段】a-上午/下午 aa-精确(傍晚/凌晨)\n" +
            "【小时】HH-24时补0(19) H-24时(19)\n" +
            "       hh-12时补0(07) h-12时(7)\n" +
            "【分钟】mm-补0(08) m-不补0(8)\n" +
            "【秒数】ss-补0(55) s-不补0(55)\n" +
            "【时辰】l-地支(酉)";

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{custom_format_preview}").textSize(14).textColor(0xFF666666).marginBottom(smallMargin)
            .addTextView("format_preview").text(preview).textSize(16).textColor(0xFF4CAF50).marginBottom(smallMargin)
            .addTextView().text("{custom_format_input}").textSize(14).textColor(0xFF666666).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("format_input").text(currentFormat).minLines(2).maxLines(2).widthMatchParent().marginBottom(smallMargin)
                .softWrap(bin.mt.plugin.api.ui.PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addTextView().text("【格式说明】").textSize(13).textColor(0xFF333333).marginTop(smallMargin).marginBottom(smallMargin)
            .addTextView().text(formatHelp).textSize(12).textColor(0xFF666666)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.PluginEditText formatInput = view.requireViewById("format_input");

        ui.buildDialog()
            .setTitle("{custom_format_title}")
            .setView(view)
            .setPositiveButton("{save}", (d, which) -> {
                String format = formatInput.getText().toString().trim();
                if (format.isEmpty()) {
                    format = TimeFormatHelper.DEFAULT_CUSTOM_FORMAT;
                }
                TimeFormatHelper.setCustomFormatString(context, format);
                TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_CUSTOM);
                String finalPreview = TimeFormatHelper.parseCustomFormat(format, new Date());
                context.showToast("已保存: " + finalPreview);
            })
            .setNegativeButton("{cancel}", null)
            .setNeutralButton("{reset_default}", (d, which) -> {
                TimeFormatHelper.setCustomFormatString(context, TimeFormatHelper.DEFAULT_CUSTOM_FORMAT);
                TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_CUSTOM);
                context.showToast("已重置为默认格式");
            })
            .show();
    }

    /**
     * 显示支持作者对话框
     */
    private void showSupportDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(16);
        int smallMargin = ui.dp2px(8);

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{support_message}").textSize(14).widthMatchParent().marginBottom(smallMargin)
            .addButton("alipay_btn").text("{alipay}").widthMatchParent().marginBottom(smallMargin)
            .addButton("wechat_btn").text("{wechat_pay}").widthMatchParent()
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.dialog.PluginDialog dialog = ui.buildDialog()
            .setTitle("{support_author_title}")
            .setView(view)
            .setNegativeButton("{close}", null)
            .show();

        // 支付宝按钮点击事件
        view.requireViewById("alipay_btn").setOnClickListener(v -> {
            showPaymentImage(ui, context, "zfb.jpg", "{alipay}");
        });

        // 微信按钮点击事件
        view.requireViewById("wechat_btn").setOnClickListener(v -> {
            showPaymentImage(ui, context, "wx.jpg", "{wechat_pay}");
        });
    }

    /**
     * 显示支付二维码图片
     * 使用MT插件API的ImageView显示图片
     */
    private void showPaymentImage(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context, String imageName, String title) {
        try {
            // 从assets加载图片流
            java.io.InputStream is = context.getAssetsAsStream(imageName);
            if (is == null) {
                context.showToast("{load_image_failed}");
                return;
            }

            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
            is.close();

            if (bitmap == null) {
                context.showToast("{load_image_failed}");
                return;
            }

            // 计算图片显示尺寸（限制最大宽度）
            int maxWidth = ui.dp2px(280);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width > maxWidth) {
                float scale = (float) maxWidth / width;
                width = maxWidth;
                height = (int) (height * scale);
            }

            // 使用MT插件API创建ImageView
            int padding = ui.dp2px(16);
            int smallMargin = ui.dp2px(12);
            bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
                .addImageView("qr_image")
                    .image(bitmap)
                    .width(width)
                    .height(height)
                    .layoutGravity(android.view.Gravity.CENTER)
                    .marginBottom(smallMargin)
                .addTextView()
                    .text("{screenshot_tip}")
                    .textSize(16)
                    .textColor(0xFF666666)
                    .widthMatchParent()
                    .layoutGravity(android.view.Gravity.CENTER)
                .paddingHorizontal(padding)
                .paddingVertical(padding)
                .build();

            ui.buildDialog()
                .setTitle(title)
                .setView(view)
                .setNegativeButton("{close}", null)
                .show();

        } catch (Exception e) {
            context.showToast("{load_image_failed}: " + e.getMessage());
        }
    }
}
