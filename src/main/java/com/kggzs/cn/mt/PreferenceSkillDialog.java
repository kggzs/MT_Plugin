package com.kggzs.cn.mt;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.ui.PluginButton;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.PluginDialog;

import com.kggzs.cn.mt.util.AIHelper;
import com.kggzs.cn.mt.util.SkillManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * AI 能力配置对话框
 * 管理全局提示词、简短提示词、Skill 和 QuickPrompt 的编辑/管理界面
 */
public class PreferenceSkillDialog {

    /**
     * 显示 AI 能力配置对话框（提示词 + Skill + QuickPrompt）
     *
     * @param ui      MT 插件 UI 接口
     * @param context MT 插件上下文
     */
    public static void showAiCapabilityDialog(PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{global_analysis_prompt_label}").textSize(14).marginBottom(smallMargin)
            .addEditBox("global_prompt").text(AIHelper.getPrompt(context))
                .minLines(4).maxLines(8).widthMatchParent().marginBottom(smallMargin).softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addTextView().text("{short_analysis_prompt_label}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("short_prompt").text(AIHelper.getShortPrompt(context))
                .minLines(2).maxLines(4).widthMatchParent().marginBottom(smallMargin).softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addButton("manage_quick_prompts").text("{quick_prompts_config}").widthMatchParent().marginBottom(smallMargin)
            .addButton("manage_skills").text("{manage_skills}").widthMatchParent()
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        PluginEditText globalPromptInput = view.requireViewById("global_prompt");
        PluginEditText shortPromptInput = view.requireViewById("short_prompt");

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
        PluginButton manageSkillsBtn = view.requireViewById("manage_skills");
        manageSkillsBtn.setOnClickListener(v -> {
            showSkillManagementDialog(ui, context, globalPromptInput);
        });

        // 绑定快速提示词管理按钮
        PluginButton manageQuickPromptsBtn = view.requireViewById("manage_quick_prompts");
        manageQuickPromptsBtn.setOnClickListener(v -> {
            showQuickPromptsManagementDialog(ui, context);
        });
    }

    /**
     * 显示 Skill 管理对话框
     */
    private static void showSkillManagementDialog(PluginUI ui, PluginContext context,
                                                  PluginEditText promptInput) {
        try {
            JSONArray skillsArray = new JSONArray(AIHelper.getSkills(context));
            ArrayList<String> skillNames = SkillManager.extractNames(skillsArray);
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
    private static void showQuickPromptsManagementDialog(PluginUI ui, PluginContext context) {
        try {
            JSONArray promptsArray = new JSONArray(AIHelper.getQuickPrompts(context));
            ArrayList<String> promptNames = SkillManager.extractNames(promptsArray);
            promptNames.add("+ " + context.getString("{add_quick_prompt}"));

            CharSequence[] items = promptNames.toArray(new CharSequence[0]);

            ui.buildDialog()
                .setTitle("{quick_prompts_config}")
                .setItems(items, (d, which) -> {
                    if (which == items.length - 1) {
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
            context.showToast("{load_quick_prompts_failed}: " + e.getMessage());
        }
    }

    /**
     * 显示快速提示词编辑对话框
     */
    private static void showQuickPromptEditorDialog(PluginUI ui, PluginContext context,
                                                    String promptName, int promptIndex) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        String currentName = "";
        String currentPrompt = "";

        if (promptIndex >= 0) {
            try {
                JSONArray promptsArray = new JSONArray(AIHelper.getQuickPrompts(context));
                currentName = SkillManager.getSkillName(promptsArray, promptIndex);
                currentPrompt = SkillManager.getSkillPrompt(promptsArray, promptIndex);
            } catch (Exception e) {
                context.showToast("{load_quick_prompts_failed}: " + e.getMessage());
                return;
            }
        }

        final int finalPromptIndex = promptIndex;

        PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{quick_prompt_name_hint}").textSize(14).marginBottom(smallMargin)
            .addEditText("prompt_name").text(currentName).hint("{quick_prompt_name_hint}").widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{quick_prompt_content_hint}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("prompt_content").text(currentPrompt).minLines(6).maxLines(10).widthMatchParent().marginBottom(smallMargin).softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addButton("delete_prompt_btn").text("{delete_quick_prompt}").widthMatchParent()
                .textColor(0xFFFF5555).marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        if (promptIndex < 0) {
            view.requireViewById("delete_prompt_btn").setGone();
        }

        PluginEditText nameInput = view.requireViewById("prompt_name");
        PluginEditText promptEdit = view.requireViewById("prompt_content");

        if (promptIndex >= 0) {
            view.requireViewById("delete_prompt_btn").setOnClickListener(v -> {
                ui.buildDialog()
                    .setTitle("{confirm_delete}")
                    .setMessage("{sure_to_delete_skill}: " + nameInput.getText() + "?")
                    .setPositiveButton("{delete}", (d, w) -> {
                        try {
                            JSONArray promptsArray = new JSONArray(AIHelper.getQuickPrompts(context));
                            SkillManager.deleteSkill(promptsArray, finalPromptIndex);
                            AIHelper.setQuickPrompts(context, promptsArray.toString());
                            context.showToast("{deleted}");
                            d.dismiss();
                            showQuickPromptsManagementDialog(ui, context);
                        } catch (Exception e) {
                            context.showToast("{delete_failed}: " + e.getMessage());
                        }
                    })
                    .setNegativeButton("{cancel}", null)
                    .show();
            });
        }

        ui.buildDialog()
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
                    if (promptIndex < 0 && promptsArray.length() >= 10) {
                        context.showToast("{quick_prompt_max_limit}");
                        return;
                    }
                    SkillManager.saveSkill(promptsArray, name, prompt, promptIndex);
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
    private static void showSkillEditorDialog(PluginUI ui, PluginContext context,
                                              String skillName, int skillIndex,
                                              PluginEditText promptInput) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        String currentName = "";
        String currentPrompt = "";

        if (skillIndex >= 0) {
            try {
                JSONArray skillsArray = new JSONArray(AIHelper.getSkills(context));
                currentName = SkillManager.getSkillName(skillsArray, skillIndex);
                currentPrompt = SkillManager.getSkillPrompt(skillsArray, skillIndex);
            } catch (Exception e) {
                context.showToast("{load_skill_data_failed}: " + e.getMessage());
                return;
            }
        }

        final int finalSkillIndex = skillIndex;

        PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{skill_name}").textSize(14).marginBottom(smallMargin)
            .addEditText("skill_name").text(currentName).hint("{input_name}").widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{prompt_content}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("skill_prompt").text(currentPrompt).minLines(8).maxLines(15).widthMatchParent().marginBottom(smallMargin).softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addButton("delete_btn").text("{delete_this_skill}").widthMatchParent()
                .textColor(0xFFFF5555).marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        if (skillIndex < 0) {
            view.requireViewById("delete_btn").setGone();
        }

        PluginEditText nameInput = view.requireViewById("skill_name");
        PluginEditText promptEdit = view.requireViewById("skill_prompt");

        if (skillIndex >= 0) {
            view.requireViewById("delete_btn").setOnClickListener(v -> {
                ui.buildDialog()
                    .setTitle("{confirm_delete}")
                    .setMessage("{sure_to_delete_skill}: " + nameInput.getText() + "?")
                    .setPositiveButton("{delete}", (d, w) -> {
                        try {
                            JSONArray skillsArray = new JSONArray(AIHelper.getSkills(context));
                            SkillManager.deleteSkill(skillsArray, finalSkillIndex);
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

        PluginDialog dialog = ui.buildDialog()
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
                    SkillManager.saveSkill(skillsArray, name, prompt, skillIndex);
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
}