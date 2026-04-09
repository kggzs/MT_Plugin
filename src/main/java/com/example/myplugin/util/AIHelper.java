package com.example.myplugin.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.dialog.PluginDialog;

/**
 * AI 工具类，封装 AI API 调用的公共逻辑
 * 支持通过设置自定义配置 API 地址、模型、密钥和提示词
 */
public class AIHelper {
    // 默认配置
    private static final String DEFAULT_API_URL = "https://api.kggzs.cn/v1";
    private static final String DEFAULT_AI_MODEL = "deepseek-v3.2";
    private static final String DEFAULT_API_KEY = "sk-K1m4b0U2WoorIub7EhbQTIYRFpQhURRXMdIoZBywCEruujOa";
    private static final String DEFAULT_PROMPT = "你是一位资深的代码安全分析专家，精通基于 MT 管理器的安卓软件逆向分析，具备丰富的安卓代码审计与逆向工程实战经验。请针对提供的安卓软件代码（含 smali/Java 代码），结合 MT 管理器的操作特性，围绕 \"绕过权限验证\" 和 \"删除广告弹窗\" 两大核心目标，从以下维度进行专业、深度且精炼的分析，并输出分析结果：1. 与权限验证 / 广告弹窗相关的潜在安全逻辑点（含逻辑漏洞类型、触发条件、影响范围，明确在 MT 管理器中可验证该逻辑点的操作方式）2. 基于 MT 管理器的逆向工程检测方案（仅围绕 MT 管理器展开，聚焦权限验证 / 广告弹窗相关逻辑，含具体操作步骤、关键检测位置、需重点查看的代码特征 / 数值）3. 可落地的绕过权限验证、删除广告弹窗的具体操作方法（明确操作目标为 \"绕过指定权限验证\"/\"删除指定广告弹窗\"，并逐一列出在 MT 管理器中可直接执行的具体操作步骤，如文件定位、smali 代码修改、配置文件调整、搜索替换等）要求：所有输出内容均为纯文本格式，严禁使用任何 markdown 格式符号（包括但不限于 #、*、`、-、> 等），仅保留文字、数字、中文标点符号；分析结果需逻辑清晰、内容精炼，无冗余表述，不使用表情符号；所有操作方法需完全贴合 MT 管理器的操作逻辑，具备极强的实操性，避免提及其他无关工具。";
    private static final String DEFAULT_SHORT_PROMPT = "请简要分析以下代码，指出主要问题和改进建议：";

    // SharedPreferences 键名
    private static final String PREF_API_URL = "ai_api_url";
    private static final String PREF_AI_MODEL = "ai_model";
    private static final String PREF_API_KEY = "ai_api_key";
    private static final String PREF_CUSTOM_PROMPT = "ai_custom_prompt";
    private static final String PREF_SHORT_PROMPT = "ai_short_prompt";
    private static final String PREF_SKILLS = "ai_skills";

    /**
     * 获取 API 地址
     */
    @NonNull
    public static String getApiUrl(@NonNull PluginContext context) {
        String url = context.getPreferences().getString(PREF_API_URL, "");
        return url.isEmpty() ? DEFAULT_API_URL : url;
    }

    /**
     * 获取 AI 模型名称
     */
    @NonNull
    public static String getAiModel(@NonNull PluginContext context) {
        String model = context.getPreferences().getString(PREF_AI_MODEL, "");
        return model.isEmpty() ? DEFAULT_AI_MODEL : model;
    }

    /**
     * 获取 API 密钥
     */
    @NonNull
    public static String getApiKey(@NonNull PluginContext context) {
        String key = context.getPreferences().getString(PREF_API_KEY, "");
        return key.isEmpty() ? DEFAULT_API_KEY : key;
    }

    /**
     * 获取提示词
     */
    @NonNull
    public static String getPrompt(@NonNull PluginContext context) {
        String prompt = context.getPreferences().getString(PREF_CUSTOM_PROMPT, "");
        return prompt.isEmpty() ? DEFAULT_PROMPT : prompt;
    }

    /**
     * 获取简短分析提示词
     */
    @NonNull
    public static String getShortPrompt(@NonNull PluginContext context) {
        String prompt = context.getPreferences().getString(PREF_SHORT_PROMPT, "");
        return prompt.isEmpty() ? DEFAULT_SHORT_PROMPT : prompt;
    }

    /**
     * 保存 API 地址
     */
    public static void setApiUrl(@NonNull PluginContext context, @NonNull String url) {
        context.getPreferences().edit().putString(PREF_API_URL, url).apply();
    }

    /**
     * 保存 AI 模型名称
     */
    public static void setAiModel(@NonNull PluginContext context, @NonNull String model) {
        context.getPreferences().edit().putString(PREF_AI_MODEL, model).apply();
    }

    /**
     * 保存 API 密钥
     */
    public static void setApiKey(@NonNull PluginContext context, @NonNull String key) {
        context.getPreferences().edit().putString(PREF_API_KEY, key).apply();
    }

    /**
     * 保存自定义提示词
     */
    public static void setPrompt(@NonNull PluginContext context, @NonNull String prompt) {
        context.getPreferences().edit().putString(PREF_CUSTOM_PROMPT, prompt).apply();
    }

    /**
     * 保存简短分析提示词
     */
    public static void setShortPrompt(@NonNull PluginContext context, @NonNull String prompt) {
        context.getPreferences().edit().putString(PREF_SHORT_PROMPT, prompt).apply();
    }

    /**
     * AI 分析代码（使用自定义提示词）
     * @param context 插件上下文
     * @param code 要分析的代码
     * @param customPrompt 自定义提示词
     * @param thinkingEdit 思考过程显示的编辑框
     * @param dialog 显示对话框
     * @return 分析结果数组，第一个元素是分析结果
     */
    @Nullable
    public static String[] analyzeCodeWithCustomPrompt(
            @NonNull PluginContext context,
            @NonNull String code,
            @NonNull String customPrompt,
            @NonNull PluginEditText thinkingEdit,
            @NonNull PluginDialog dialog) throws Exception {
        return analyzeCodeWithAI(context, code, thinkingEdit, dialog, true, customPrompt);
    }

    /**
     * 获取自定义 Skill 列表 (JSON 格式)
     */
    @NonNull
    public static String getSkills(@NonNull PluginContext context) {
        return context.getPreferences().getString(PREF_SKILLS, "[]");
    }

    /**
     * 保存自定义 Skill 列表
     */
    public static void setSkills(@NonNull PluginContext context, @NonNull String skillsJson) {
        context.getPreferences().edit().putString(PREF_SKILLS, skillsJson).apply();
    }

    /**
     * 重置为默认配置
     */
    public static void resetToDefault(@NonNull PluginContext context) {
        context.getPreferences().edit()
                .remove(PREF_API_URL)
                .remove(PREF_AI_MODEL)
                .remove(PREF_API_KEY)
                .remove(PREF_CUSTOM_PROMPT)
                .remove(PREF_SHORT_PROMPT)
                .remove(PREF_SKILLS)
                .apply();
    }

    /**
     * AI 分析代码（显示思考过程）
     */
    @Nullable
    public static String[] analyzeCodeWithThinking(
            @NonNull PluginContext context,
            @NonNull String code,
            @NonNull PluginEditText thinkingEdit,
            @NonNull PluginDialog dialog) throws Exception {
        return analyzeCodeWithAI(context, code, thinkingEdit, dialog, true, null);
    }

    /**
     * AI 分析代码
     * @param context 插件上下文
     * @param code 要分析的代码
     * @param thinkingEdit 思考过程显示的编辑框（可为 null）
     * @param dialog 显示对话框（可为 null）
     * @param showThinking 是否显示思考过程
     * @param customPrompt 自定义提示词（如果为 null 则使用默认）
     * @return 分析结果数组，第一个元素是分析结果
     */
    @Nullable
    public static String[] analyzeCodeWithAI(
            @NonNull PluginContext context,
            @NonNull String code,
            @Nullable PluginEditText thinkingEdit,
            @Nullable PluginDialog dialog,
            boolean showThinking,
            @Nullable String customPrompt) throws Exception {

        String apiUrl = getApiUrl(context);
        String aiModel = getAiModel(context);
        String apiKey = getApiKey(context);
        String prompt = (customPrompt != null && !customPrompt.isEmpty()) ? customPrompt : getPrompt(context);

        // 构建 API URL（确保以 /chat/completions 结尾）
        String completionsUrl = apiUrl.endsWith("/chat/completions") ? apiUrl : 
                               (apiUrl.endsWith("/") ? apiUrl + "chat/completions" : apiUrl + "/chat/completions");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", aiModel);
        requestBody.put("enable_thinking", showThinking);
        requestBody.put("stream", true);

        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", prompt);
        messages.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "请分析以下代码：\n\n" + code);
        messages.put(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        URL url = new URL(completionsUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);

        connection.getOutputStream().write(requestBody.toString().getBytes(StandardCharsets.UTF_8));

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new Exception("AI API错误: " + responseCode + " - " + errorResponse.toString());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder fullReasoning = new StringBuilder();
        StringBuilder fullContent = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("data: ")) {
                String data = line.substring(6);
                if (data.equals("[DONE]")) {
                    break;
                }

                try {
                    JSONObject chunk = new JSONObject(data);
                    JSONArray choices = chunk.optJSONArray("choices");
                    if (choices != null && choices.length() > 0) {
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject delta = firstChoice.optJSONObject("delta");
                        if (delta != null) {
                            String reasoningContent = delta.optString("reasoning_content", "");
                            String content = delta.optString("content", "");

                            if (showThinking && !reasoningContent.isEmpty() && !"null".equals(reasoningContent)) {
                                fullReasoning.append(reasoningContent);
                                if (thinkingEdit != null) {
                                    final String currentReasoning = fullReasoning.toString();
                                    runOnMainThread(() -> {
                                        thinkingEdit.setText(currentReasoning);
                                        thinkingEdit.selectEnd();
                                    });
                                }
                            }

                            if (!content.isEmpty() && !"null".equals(content)) {
                                fullContent.append(content);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略 JSON 解析错误
                }
            }
        }
        reader.close();
        connection.disconnect();

        if (fullContent.length() == 0) {
            throw new Exception("AI API返回空结果");
        }

        return new String[]{fullContent.toString()};
    }

    /**
     * 在主线程执行任务
     */
    public static void runOnMainThread(@NonNull Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }
}
