package com.kggzs.cn.mt;

import androidx.annotation.NonNull;

import com.kggzs.cn.mt.util.AIHelper;
import com.kggzs.cn.mt.util.MCPClient;
import com.kggzs.cn.mt.util.SkillManager;
import com.kggzs.cn.mt.util.ThreadPoolManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.ui.PluginUI;

/**
 * MCP 服务配置辅助类
 * 提供 MCP 服务器地址配置、连接测试、工具查看和独立 Skill 管理
 * 通过对话框方式集成到 MyPreference 设置页面中
 */
public class MCPServiceMenu {

    /**
     * 显示 MCP 服务主菜单对话框
     */
    public static void showMainDialog(@NonNull PluginUI ui, @NonNull PluginContext context) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        boolean isEnabled = AIHelper.isMcpEnabled(context);

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{mcp_service_desc}").textSize(14)
                .textColor(0xFF666666).marginBottom(smallMargin).widthMatchParent()
            .addButton("btn_toggle").text(isEnabled ? "{mcp_status_enabled}" : "{mcp_status_disabled}")
                .widthMatchParent().marginBottom(smallMargin)
            .addButton("btn_server_config").text("{mcp_server_config}")
                .widthMatchParent().marginBottom(smallMargin)
            .addButton("btn_test_connection").text("{mcp_test_connection}")
                .widthMatchParent().marginBottom(smallMargin)
            .addButton("btn_view_tools").text("{mcp_tools_view}")
                .widthMatchParent().marginBottom(smallMargin)
            .addButton("btn_skills").text("{mcp_skill_config}")
                .widthMatchParent().marginBottom(smallMargin)
            .addButton("btn_reset").text("{reset_config}")
                .widthMatchParent().textColor(0xFFFF5555).marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.dialog.PluginDialog dialog = ui.buildDialog()
            .setTitle("{mcp_service_config}")
            .setView(view)
            .setNegativeButton("{close}", null)
            .show();

        view.requireViewById("btn_toggle").setOnClickListener(v -> {
            dialog.dismiss();
            showEnableToggleDialog(ui, context);
        });

        view.requireViewById("btn_server_config").setOnClickListener(v -> {
            dialog.dismiss();
            showServerConfigDialog(ui, context);
        });

        view.requireViewById("btn_test_connection").setOnClickListener(v -> {
            dialog.dismiss();
            testConnection(ui, context);
        });

        view.requireViewById("btn_view_tools").setOnClickListener(v -> {
            dialog.dismiss();
            showMcpToolsDialog(ui, context);
        });

        view.requireViewById("btn_skills").setOnClickListener(v -> {
            dialog.dismiss();
            showMcpSkillManagementDialog(ui, context);
        });

        view.requireViewById("btn_reset").setOnClickListener(v -> {
            dialog.dismiss();
            ui.buildDialog()
                .setTitle("{confirm_reset_title}")
                .setMessage("{mcp_reset_confirm_message}")
                .setPositiveButton("{confirm_reset_positive}", (d, which) -> {
                    AIHelper.setMcpServerUrl(context, "");
                    AIHelper.setMcpEnabled(context, false);
                    AIHelper.setMcpSkills(context, "[]");
                    context.showToast("{config_reset_success}");
                })
                .setNegativeButton("{confirm_reset_negative}", null)
                .show();
        });
    }

    /**
     * 启用/禁用切换对话框
     */
    private static void showEnableToggleDialog(PluginUI ui, PluginContext context) {
        boolean isEnabled = AIHelper.isMcpEnabled(context);

        ui.buildDialog()
            .setTitle("{mcp_enable_title}")
            .setMessage(isEnabled ? "{mcp_disable_confirm}" : "{mcp_enable_confirm}")
            .setPositiveButton("{confirm_selection}", (dialog, which) -> {
                AIHelper.setMcpEnabled(context, !isEnabled);
                context.showToast(isEnabled ? "{mcp_disabled}" : "{mcp_enabled}");
            })
            .setNegativeButton("{cancel}", null)
            .show();
    }

    /**
     * MCP 服务器配置对话框
     */
    private static void showServerConfigDialog(PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        String currentUrl = AIHelper.getMcpServerUrl(context);

        bin.mt.plugin.api.ui.PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{mcp_server_url}").textSize(14).marginBottom(smallMargin)
            .addEditText("mcp_url").hint("{mcp_server_url_hint}")
                .text(currentUrl).widthMatchParent()
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.PluginEditText urlInput = view.requireViewById("mcp_url");

        ui.buildDialog()
            .setTitle("{mcp_server_config}")
            .setView(view)
            .setPositiveButton("{save}", (dialog, which) -> {
                String url = urlInput.getText().toString().trim();
                if (url.isEmpty()) {
                    context.showToast("{mcp_url_empty_error}");
                    return;
                }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    context.showToast("{url_format_error}");
                    return;
                }
                AIHelper.setMcpServerUrl(context, url);
                context.showToast("{saved}");
            })
            .setNegativeButton("{cancel}", null)
            .show();
    }

    /**
     * 测试 MCP 连接
     */
    private static void testConnection(PluginUI ui, PluginContext context) {
        String serverUrl = AIHelper.getMcpServerUrl(context);
        context.showToast("{mcp_connecting}");

        ThreadPoolManager.execute(() -> {
            MCPClient client = new MCPClient(serverUrl);
            String result = client.testConnection();

            AIHelper.runOnMainThread(() -> {
                ui.buildDialog()
                    .setTitle("{mcp_test_connection}")
                    .setMessage(result)
                    .setPositiveButton("{confirm}", null)
                    .setNegativeButton("{copy}", (dialog, which) -> {
                        context.setClipboardText(result);
                        context.showToast("{copy_success_clipboard}");
                    })
                    .show();
            });
        });
    }

    /**
     * 列出 MCP 工具
     */
    private static void showMcpToolsDialog(PluginUI ui, PluginContext context) {
        String serverUrl = AIHelper.getMcpServerUrl(context);
        context.showToast("{mcp_loading_tools}");

        ThreadPoolManager.execute(() -> {
            try {
                MCPClient client = new MCPClient(serverUrl);
                JSONArray tools = client.listTools();

                StringBuilder sb = new StringBuilder();
                if (tools != null && tools.length() > 0) {
                    for (int i = 0; i < tools.length(); i++) {
                        JSONObject tool = tools.getJSONObject(i);
                        String name = tool.optString("name", "{unknown}");
                        String desc = tool.optString("description", "");
                        sb.append("• ").append(name);
                        if (!desc.isEmpty()) {
                            sb.append(" - ").append(desc);
                        }
                        sb.append("\n");

                        JSONObject inputSchema = tool.optJSONObject("inputSchema");
                        if (inputSchema != null) {
                            JSONObject properties = inputSchema.optJSONObject("properties");
                            if (properties != null) {
                                JSONArray propNames = properties.names();
                                if (propNames != null && propNames.length() > 0) {
                                    sb.append("  参数: ");
                                    for (int j = 0; j < propNames.length(); j++) {
                                        if (j > 0) sb.append(", ");
                                        sb.append(propNames.getString(j));
                                    }
                                    sb.append("\n");
                                }
                            }
                        }
                        sb.append("\n");
                    }
                } else {
                    sb.append("{mcp_no_tools_found}");
                }

                String finalResult = sb.toString();
                AIHelper.runOnMainThread(() -> {
                    ui.buildDialog()
                        .setTitle("{mcp_tools_view}")
                        .setMessage(finalResult)
                        .setPositiveButton("{confirm}", null)
                        .setNegativeButton("{copy}", (dialog, which) -> {
                            context.setClipboardText(finalResult);
                            context.showToast("{copy_success_clipboard}");
                        })
                        .show();
                });
            } catch (Exception e) {
                AIHelper.runOnMainThread(() -> {
                    context.showToast("{mcp_load_tools_failed}: " + e.getMessage());
                });
            }
        });
    }

    /**
     * MCP Skill 管理对话框
     */
    private static void showMcpSkillManagementDialog(PluginUI ui, PluginContext context) {
        try {
            JSONArray skillsArray = new JSONArray(AIHelper.getMcpSkills(context));
            ArrayList<String> skillNames = SkillManager.extractNames(skillsArray);
            skillNames.add(context.getString("{mcp_new_skill}"));

            CharSequence[] items = skillNames.toArray(new CharSequence[0]);

            ui.buildDialog()
                .setTitle("{mcp_skill_config}")
                .setItems(items, (d, which) -> {
                    if (which == items.length - 1) {
                        showMcpSkillEditorDialog(ui, context, null, -1);
                    } else {
                        showMcpSkillEditorDialog(ui, context, items[which].toString(), which);
                    }
                })
                .setNegativeButton("{close}", null)
                .show();
        } catch (Exception e) {
            context.showToast("{load_skill_failed}: " + e.getMessage());
        }
    }

    /**
     * MCP Skill 编辑对话框
     */
    private static void showMcpSkillEditorDialog(PluginUI ui, PluginContext context,
                                                  String skillName, int skillIndex) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        String currentName = "";
        String currentPrompt = "";

        if (skillIndex >= 0) {
            try {
                JSONArray skillsArray = new JSONArray(AIHelper.getMcpSkills(context));
                currentName = SkillManager.getSkillName(skillsArray, skillIndex);
                currentPrompt = SkillManager.getSkillPrompt(skillsArray, skillIndex);
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
            .addEditBox("skill_prompt").text(currentPrompt).minLines(6).maxLines(10).widthMatchParent().marginBottom(smallMargin).softWrap(bin.mt.plugin.api.ui.PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addButton("delete_btn").text("{delete_this_skill}").widthMatchParent()
                .textColor(0xFFFF5555).marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        if (skillIndex < 0) {
            view.requireViewById("delete_btn").setGone();
        }

        bin.mt.plugin.api.ui.PluginEditText nameInput = view.requireViewById("skill_name");
        bin.mt.plugin.api.ui.PluginEditText promptEdit = view.requireViewById("skill_prompt");

        // 先构建编辑器对话框，确保 editorDialog 变量已声明
        bin.mt.plugin.api.ui.dialog.PluginDialog editorDialog = ui.buildDialog()
            .setTitle(skillIndex >= 0 ? "{edit_skill}" : "{mcp_new_skill}")
            .setView(view)
            .setPositiveButton("{save}", (d, which) -> {
                String name = nameInput.getText().toString().trim();
                String prompt = promptEdit.getText().toString();
                if (name.isEmpty()) {
                    context.showToast("{name_cannot_be_empty}");
                    return;
                }
                try {
                    JSONArray skillsArray = new JSONArray(AIHelper.getMcpSkills(context));
                    SkillManager.saveSkill(skillsArray, name, prompt, skillIndex);
                    AIHelper.setMcpSkills(context, skillsArray.toString());
                    context.showToast("{skill_saved}");
                } catch (Exception e) {
                    context.showToast("{save_failed}: " + e.getMessage());
                }
            })
            .setNegativeButton("{cancel}", null)
            .show();

        // 编辑模式下显示删除按钮
        if (skillIndex >= 0) {
            view.requireViewById("delete_btn").setOnClickListener(v -> {
                ui.buildDialog()
                    .setTitle("{confirm_delete}")
                    .setMessage(context.getString("{sure_to_delete_skill}") + " " + nameInput.getText() + "?")
                    .setPositiveButton("{delete}", (d, w) -> {
                        try {
                            JSONArray skillsArray = new JSONArray(AIHelper.getMcpSkills(context));
                            SkillManager.deleteSkill(skillsArray, finalSkillIndex);
                            AIHelper.setMcpSkills(context, skillsArray.toString());
                            context.showToast("{deleted}");
                            d.dismiss();
                            editorDialog.dismiss();
                            showMcpSkillManagementDialog(ui, context);
                        } catch (Exception e) {
                            context.showToast("{delete_failed}: " + e.getMessage());
                        }
                    })
                    .setNegativeButton("{cancel}", null)
                    .show();
            });
        }
    }
}