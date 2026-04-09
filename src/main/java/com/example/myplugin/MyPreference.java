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

        // AI 配置分组
        builder.addHeader("AI 代码分析配置");

        // 统一配置入口
        builder.addText("API 配置")
               .summary("配置 API 地址、模型名称和 API 密钥")
               .onClick((ui, preference) -> {
                   showApiConfigDialog(ui, context);
               });

        // AI 能力配置入口（提示词 + Skill）
        builder.addText("AI 能力配置")
               .summary("管理全局分析提示词、简短分析提示词与自定义 Skill")
               .onClick((ui, preference) -> {
                   showAiCapabilityDialog(ui, context);
               });

        builder.addText("重置配置")
               .summary("点击重置 AI 配置为默认值")
               .onClick((ui, preference) -> {
                   AIHelper.resetToDefault(context);
                   context.showToast("已重置为默认配置");
                   // 重新打开设置界面以刷新显示
                   ui.showPreference(MyPreference.class);
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
            .addTextView().text("API 地址").textSize(14).marginBottom(smallMargin)
            .addEditText("api_url").hint("默认: https://api.kggzs.cn/v1")
                .text(AIHelper.getApiUrl(context)).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("模型名称").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditText("model_name").hint("默认: deepseek-v3.2")
                .text(AIHelper.getAiModel(context)).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("API 密钥").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditText("api_key").hint("默认: sk-xxxxxxxxxxxxxxxxxxxxxxxx")
                .text(AIHelper.getApiKey(context)).widthMatchParent().marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.PluginEditText apiUrlInput = view.requireViewById("api_url");
        bin.mt.plugin.api.ui.PluginEditText modelNameInput = view.requireViewById("model_name");
        bin.mt.plugin.api.ui.PluginEditText apiKeyInput = view.requireViewById("api_key");

        ui.buildDialog()
            .setTitle("API 配置")
            .setView(view)
            .setPositiveButton("保存", (dialog, which) -> {
                String apiUrl = apiUrlInput.getText().toString().trim();
                String modelName = modelNameInput.getText().toString().trim();
                String apiKey = apiKeyInput.getText().toString().trim();

                // 验证并保存
                boolean hasValue = false;

                if (!apiUrl.isEmpty()) {
                    if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://")) {
                        context.showToast("URL 必须以 http:// 或 https:// 开头");
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
                    context.showToast("已保存");
                    // 刷新设置界面
                    ui.showPreference(MyPreference.class);
                } else {
                    context.showToast("未检测到输入");
                }
            })
            .setNegativeButton("{cancel}", null)
            .setNeutralButton("重置", (dialog, which) -> {
                AIHelper.setApiUrl(context, "");
                AIHelper.setAiModel(context, "");
                AIHelper.setApiKey(context, "");
                context.showToast("已重置为默认值");
                ui.showPreference(MyPreference.class);
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
            .addTextView().text("全局分析提示词").textSize(14).marginBottom(smallMargin)
            .addEditBox("global_prompt").text(AIHelper.getPrompt(context))
                .minLines(4).maxLines(8).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("简短分析提示词").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("short_prompt").text(AIHelper.getShortPrompt(context))
                .minLines(2).maxLines(4).widthMatchParent().marginBottom(smallMargin)
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
                ui.showPreference(MyPreference.class);
            })
            .setNegativeButton("{cancel}", null)
            .show();

        // 绑定 Skill 管理按钮
        bin.mt.plugin.api.ui.PluginButton manageSkillsBtn = view.requireViewById("manage_skills");
        manageSkillsBtn.setOnClickListener(v -> {
            showSkillManagementDialog(ui, context, globalPromptInput);
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
            skillNames.add("+ {new_skill}");

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
                context.showToast("{load_skill_data_failed}");
                return;
            }
        }

        final int finalSkillIndex = skillIndex;

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{skill_name}").textSize(14).marginBottom(smallMargin)
            .addEditText("skill_name").text(currentName).hint("{input_name}").widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{prompt_content}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("skill_prompt").text(currentPrompt).minLines(8).maxLines(15).widthMatchParent().marginBottom(smallMargin)
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
            .addEditBox("input").text(defaultValue).minLines(10).maxLines(20).widthMatchParent()
            .paddingHorizontal(ui.dialogPaddingHorizontal())
            .paddingVertical(ui.dialogPaddingVertical())
            .build();

        bin.mt.plugin.api.ui.PluginEditText input = view.requireViewById("input");

        ui.buildDialog()
            .setTitle(title)
            .setView(view)
            .setPositiveButton("保存", (dialog, which) -> {
                String value = input.getText().toString();
                onSave.accept(value);
                context.showToast("已保存");
                // 刷新设置界面
                ui.showPreference(MyPreference.class);
            })
            .setNegativeButton("{cancel}", null)
            .show();
    }
}
