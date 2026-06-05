package com.kggzs.cn.mt;

import android.text.InputType;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;

import com.kggzs.cn.mt.util.AIHelper;

/**
 * API 配置对话框
 * 管理 API 地址、模型名称、API 密钥的设置界面
 */
public class PreferenceApiDialog {

    /**
     * 显示 API 统一配置对话框
     *
     * @param ui      MT 插件 UI 接口
     * @param context MT 插件上下文
     */
    public static void showApiConfigDialog(PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(12);
        int smallMargin = ui.dp2px(8);

        String currentApiKey = AIHelper.getApiKey(context);
        String maskedApiKey = maskApiKey(currentApiKey);

        PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{api_address}").textSize(14).marginBottom(smallMargin)
            .addEditText("api_url").hint("{api_url_hint}")
                .text(AIHelper.getApiUrl(context)).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{model_name}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditText("model_name").hint("{model_name_hint}")
                .text(AIHelper.getAiModel(context)).widthMatchParent().marginBottom(smallMargin)
            .addTextView().text("{api_key}").textSize(14).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditText("api_key").hint("{api_key_hint}")
                .text(maskedApiKey).widthMatchParent().marginBottom(smallMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        PluginEditText apiUrlInput = view.requireViewById("api_url");
        PluginEditText modelNameInput = view.requireViewById("model_name");
        PluginEditText apiKeyInput = view.requireViewById("api_key");

        apiKeyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

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
                    if (!apiKey.equals(maskedApiKey)) {
                        AIHelper.setApiKey(context, apiKey);
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
     *
     * @param apiKey 原始 API 密钥
     * @return 遮蔽后的密钥字符串
     */
    private static String maskApiKey(String apiKey) {
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
}