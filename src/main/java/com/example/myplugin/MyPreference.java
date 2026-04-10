package com.example.myplugin;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

import com.example.myplugin.util.AIHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyPreference implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("{plugin_name}")
               .subtitle("{plugin_author}");

        builder.addText("{plugin_description}")
               .summary("{plugin_description}");

        builder.addText("{plugin_website}")
               .summary("www.kggzs.cn");

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

        builder.addText("{reset_config}")
               .summary("{reset_config_summary}")
               .onClick((ui, preference) -> {
                   // 二次确认对话框
                   ui.buildDialog()
                       .setTitle("确认重置")
                       .setMessage("确定要重置所有配置为默认值吗？\n此操作不可撤销。")
                       .setPositiveButton("确定重置", (dialog, which) -> {
                           AIHelper.resetToDefault(context);
                           context.showToast("{config_reset_success}");
                           dialog.dismiss();
                       })
                       .setNegativeButton("取消", null)
                       .show();
               });

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

        builder.addText("{quick_insert_function}")
               .summary("{quick_insert_usage}");

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

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{api_address}").textSize(14).marginBottom(smallMargin)
            .addEditText("api_url").hint("{api_url_hint}")
                .text(AIHelper.getApiUrl(context)).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{model_name}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditText("model_name").hint("{model_name_hint}")
                .text(AIHelper.getAiModel(context)).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{api_key}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditText("api_key").hint("{api_key_hint}")
                .text(AIHelper.getApiKey(context)).widthMatchParent().marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.PluginEditText apiUrlInput = view.requireViewById("api_url");
        bin.mt.plugin.api.ui.PluginEditText modelNameInput = view.requireViewById("model_name");
        bin.mt.plugin.api.ui.PluginEditText apiKeyInput = view.requireViewById("api_key");

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
                    AIHelper.setApiKey(context, apiKey);
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
}
