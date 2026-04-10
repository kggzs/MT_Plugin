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
    private static final String DEFAULT_AI_MODEL = "MT-v1";
    private static final String DEFAULT_API_KEY = "sk-K1m4b0U2WoorIub7EhbQTIYRFpQhURRXMdIoZBywCEruujOa";
    private static final String DEFAULT_PROMPT = "你是资深代码安全分析专家，精通MT管理器安卓逆向分析，擅长smali/Java代码审计。请严格按照用户后续指定的分析方向，结合MT管理器操作特性，对提供的安卓软件代码开展精准分析。输出要求：仅围绕用户指定目标，提供MT管理器可直接执行的检测方案、安全逻辑点分析、实操修改方法；内容精炼、逻辑清晰、无冗余、无表情符号，全程不使用MT管理器以外的任何工具。";
    private static final String DEFAULT_SHORT_PROMPT = "请简要分析以下代码，指出主要问题和改进建议：";

    // SharedPreferences 键名
    private static final String PREF_API_URL = "ai_api_url";
    private static final String PREF_AI_MODEL = "ai_model";
    private static final String PREF_API_KEY = "ai_api_key";
    private static final String PREF_CUSTOM_PROMPT = "ai_custom_prompt";
    private static final String PREF_SHORT_PROMPT = "ai_short_prompt";
    private static final String PREF_SKILLS = "ai_skills";
    private static final String PREF_QUICK_PROMPTS = "ai_quick_prompts";

    // 默认快速提示词
    private static final String DEFAULT_QUICK_PROMPT_1 = "分析此代码是否存在混淆或解密情况，指出混淆技术和解密方法";

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
     * 获取快速提示词列表（JSON数组格式）
     */
    @NonNull
    public static String getQuickPrompts(@NonNull PluginContext context) {
        String prompts = context.getPreferences().getString(PREF_QUICK_PROMPTS, "");
        if (prompts.isEmpty()) {
            // 返回默认的快速提示词
            JSONArray defaultPrompts = new JSONArray();
            try {
                JSONObject prompt1 = new JSONObject();
                prompt1.put("name", "分析代码混淆");
                prompt1.put("prompt", DEFAULT_QUICK_PROMPT_1);
                defaultPrompts.put(prompt1);
            } catch (Exception e) {
                android.util.Log.e("AIHelper", "创建默认快速提示词失败", e);
            }
            return defaultPrompts.toString();
        }
        return prompts;
    }

    /**
     * 保存快速提示词列表
     */
    public static void setQuickPrompts(@NonNull PluginContext context, @NonNull String promptsJson) {
        context.getPreferences().edit().putString(PREF_QUICK_PROMPTS, promptsJson).apply();
    }

    /**
     * AI 分析代码（使用自定义提示词）
     * @param context 插件上下文
     * @param code 要分析的代码
     * @param customPrompt 自定义提示词
     * @param thinkingEdit 思考过程显示的编辑框
     * @param resultEdit 结果展示的编辑框
     * @param dialog 显示对话框
     * @return 分析结果数组，第一个元素是分析结果
     */
    @Nullable
    public static String[] analyzeCodeWithCustomPrompt(
            @NonNull PluginContext context,
            @NonNull String code,
            @NonNull String customPrompt,
            @NonNull PluginEditText thinkingEdit,
            @NonNull PluginEditText resultEdit,
            @NonNull PluginDialog dialog) throws Exception {
        return analyzeCodeWithAI(context, code, thinkingEdit, resultEdit, dialog, true, customPrompt);
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
                .remove(PREF_QUICK_PROMPTS)
                .apply();
    }

    /**
     * AI 分析代码（显示思考过程）
     * @param context 插件上下文
     * @param code 要分析的代码
     * @param thinkingEdit 思考过程显示的编辑框
     * @param resultEdit 结果展示的编辑框
     * @param dialog 显示对话框
     * @return 分析结果数组，第一个元素是分析结果
     */
    @Nullable
    public static String[] analyzeCodeWithThinking(
            @NonNull PluginContext context,
            @NonNull String code,
            @NonNull PluginEditText thinkingEdit,
            @NonNull PluginEditText resultEdit,
            @NonNull PluginDialog dialog) throws Exception {
        return analyzeCodeWithAI(context, code, thinkingEdit, resultEdit, dialog, true, null);
    }

    /**
     * AI 分析代码（用户提示词插入到系统提示词中）
     * @param context 插件上下文
     * @param code 要分析的代码
     * @param userPrompt 用户提示词（将插入到系统提示词中）
     * @param thinkingEdit 思考过程显示的编辑框
     * @param resultEdit 结果展示的编辑框
     * @param dialog 显示对话框
     * @return 分析结果数组，第一个元素是分析结果
     */
    @Nullable
    public static String[] analyzeCodeWithUserPrompt(
            @NonNull PluginContext context,
            @NonNull String code,
            @NonNull String userPrompt,
            @NonNull PluginEditText thinkingEdit,
            @NonNull PluginEditText resultEdit,
            @NonNull PluginDialog dialog) throws Exception {
        String systemPrompt = getPrompt(context);
        String combinedSystemPrompt = userPrompt + "\n\n" + systemPrompt;
        return analyzeCodeWithAI(context, code, thinkingEdit, resultEdit, dialog, true, combinedSystemPrompt);
    }

    /**
     * AI 分析代码（无UI版本，用于后台分析）
     * @param context 插件上下文
     * @param code 要分析的代码
     * @param userPrompt 用户提示词（将插入到系统提示词中）
     * @return 分析结果数组，第一个元素是分析结果
     */
    @Nullable
    public static String[] analyzeCodeWithUserPromptNoUI(
            @NonNull PluginContext context,
            @NonNull String code,
            @NonNull String userPrompt) throws Exception {
        String systemPrompt = getPrompt(context);
        String combinedSystemPrompt = userPrompt + "\n\n" + systemPrompt;
        return analyzeCodeWithAINoUI(context, code, true, combinedSystemPrompt);
    }

    /**
     * AI 分析代码
     * @param context 插件上下文
     * @param code 要分析的代码
     * @param thinkingEdit 思考过程显示的编辑框（可为 null）
     * @param resultEdit 结果展示的编辑框（可为 null，如果提供则流式显示结果）
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
            @Nullable PluginEditText resultEdit,
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
        // 移除max_tokens限制，让AI根据内容长度自由回答

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
        StringBuilder rawResponse = new StringBuilder();
        StringBuilder contentBuffer = new StringBuilder();
        boolean hasDetectedThinkingTag = false;
        boolean hasFoundEndTag = false;
        String line;

        while ((line = reader.readLine()) != null) {
            rawResponse.append(line).append("\n");
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
                        if (firstChoice != null) {
                            String reasoningContent = null;
                            String content = null;

                            // ========== 格式A: 独立字段型 ==========
                            
                            // A1: DeepSeek/通义千问格式 - delta.reasoning_content
                            JSONObject delta = firstChoice.optJSONObject("delta");
                            if (delta != null) {
                                content = delta.optString("content", "");
                                reasoningContent = delta.optString("reasoning_content", "");
                                if (reasoningContent == null || reasoningContent.isEmpty()) {
                                    reasoningContent = delta.optString("thinking", "");
                                }
                            }

                            // A2: 百度文心一言格式 - choices[0].thinking + choices[0].result
                            if ((content == null || content.isEmpty()) && (reasoningContent == null || reasoningContent.isEmpty())) {
                                reasoningContent = firstChoice.optString("thinking", "");
                                content = firstChoice.optString("result", "");
                            }

                            // A3: 讯飞星火格式 - choices[0].text[0].thought + choices[0].text[0].content
                            if ((content == null || content.isEmpty()) && (reasoningContent == null || reasoningContent.isEmpty())) {
                                JSONArray textArray = firstChoice.optJSONArray("text");
                                if (textArray != null && textArray.length() > 0) {
                                    JSONObject textObj = textArray.getJSONObject(0);
                                    content = textObj.optString("content", "");
                                    reasoningContent = textObj.optString("thought", "");
                                }
                            }

                            // A4: message.content 变体（某些模型）
                            if ((content == null || content.isEmpty()) && (reasoningContent == null || reasoningContent.isEmpty())) {
                                JSONObject message = firstChoice.optJSONObject("message");
                                if (message != null) {
                                    content = message.optString("content", "");
                                    reasoningContent = message.optString("reasoning_content", "");
                                    if (reasoningContent == null || reasoningContent.isEmpty()) {
                                        reasoningContent = message.optString("thinking", "");
                                    }
                                }
                            }

                            // ========== 格式B: 结构化块型 ==========
                            
                            // B1: Claude格式 - content[] 数组，type:thinking + type:text
                            if ((content == null || content.isEmpty()) && (reasoningContent == null || reasoningContent.isEmpty())) {
                                JSONArray contentArray = firstChoice.optJSONArray("content");
                                if (contentArray != null && contentArray.length() > 0) {
                                    StringBuilder contentBuilder = new StringBuilder();
                                    StringBuilder reasoningBuilder = new StringBuilder();
                                    for (int i = 0; i < contentArray.length(); i++) {
                                        JSONObject block = contentArray.optJSONObject(i);
                                        if (block != null) {
                                            String type = block.optString("type", "");
                                            if ("thinking".equals(type)) {
                                                reasoningBuilder.append(block.optString("thinking", ""));
                                            } else if ("text".equals(type)) {
                                                contentBuilder.append(block.optString("text", ""));
                                            }
                                        }
                                    }
                                    if (reasoningBuilder.length() > 0) {
                                        reasoningContent = reasoningBuilder.toString();
                                    }
                                    if (contentBuilder.length() > 0) {
                                        content = contentBuilder.toString();
                                    }
                                }
                            }

                            // B2: Gemini格式 - parts[] 数组，thought + text
                            if ((content == null || content.isEmpty()) && (reasoningContent == null || reasoningContent.isEmpty())) {
                                JSONArray partsArray = firstChoice.optJSONArray("parts");
                                if (partsArray != null && partsArray.length() > 0) {
                                    StringBuilder contentBuilder = new StringBuilder();
                                    StringBuilder reasoningBuilder = new StringBuilder();
                                    for (int i = 0; i < partsArray.length(); i++) {
                                        JSONObject part = partsArray.optJSONObject(i);
                                        if (part != null) {
                                            if (part.has("thought")) {
                                                reasoningBuilder.append(part.optString("thought", ""));
                                            }
                                            if (part.has("text")) {
                                                contentBuilder.append(part.optString("text", ""));
                                            }
                                        }
                                    }
                                    if (reasoningBuilder.length() > 0) {
                                        reasoningContent = reasoningBuilder.toString();
                                    }
                                    if (contentBuilder.length() > 0) {
                                        content = contentBuilder.toString();
                                    }
                                }
                            }

                            // ========== 格式C: 简单字段型 ==========
                            
                            // C1: text字段（某些简单接口）
                            if ((content == null || content.isEmpty()) && (reasoningContent == null || reasoningContent.isEmpty())) {
                                content = firstChoice.optString("text", "");
                            }

                            // C2: content直接字段
                            if ((content == null || content.isEmpty()) && (reasoningContent == null || reasoningContent.isEmpty())) {
                                content = firstChoice.optString("content", "");
                            }

                            // ========== 处理思考过程和正式内容 ==========
                            
                            // 处理标准 reasoning_content/thinking/thought 字段
                            if (showThinking && reasoningContent != null && !reasoningContent.isEmpty() && !"null".equals(reasoningContent)) {
                                fullReasoning.append(reasoningContent);
                                if (thinkingEdit != null) {
                                    final String currentReasoning = fullReasoning.toString();
                                    runOnMainThread(() -> {
                                        thinkingEdit.setText(currentReasoning);
                                        thinkingEdit.selectEnd();
                                    });
                                }
                            }

                            // 处理主内容 - 需要检测并分离内嵌标签型思考内容
                            if (content != null && !content.isEmpty() && !"null".equals(content)) {
                                if (showThinking) {
                                    // 如果已经确定没有思考标签，直接追加
                                    if (!hasDetectedThinkingTag && contentBuffer.length() == 0 && fullContent.length() > 0) {
                                        fullContent.append(content);
                                        if (resultEdit != null) {
                                            final String currentContent = fullContent.toString();
                                            runOnMainThread(() -> {
                                                resultEdit.setText(currentContent);
                                                resultEdit.selectEnd();
                                            });
                                        }
                                    } else {
                                        // 累积内容到缓冲区
                                        contentBuffer.append(content);
                                        String allContent = contentBuffer.toString();

                                        // 检测是否包含思考标签
                                        if (!hasDetectedThinkingTag) {
                                            if (containsThinkingTag(allContent)) {
                                                hasDetectedThinkingTag = true;
                                            } else if (allContent.length() > 100) {
                                                // 超过100字符无标签，认为无思考过程
                                                fullContent.append(allContent);
                                                contentBuffer.setLength(0); // 清空buffer，后续内容直接追加到fullContent
                                                if (resultEdit != null) {
                                                    final String currentContent = fullContent.toString();
                                                    runOnMainThread(() -> {
                                                        resultEdit.setText(currentContent);
                                                        resultEdit.selectEnd();
                                                    });
                                                }
                                            }
                                        }

                                        // 如果检测到思考标签，分离并显示
                                        if (hasDetectedThinkingTag) {
                                            String[] result = separateThinkingFromContent(allContent);
                                            String thinking = result[0];
                                            String finalContent = result[1];
                                            hasFoundEndTag = result[2].equals("true");

                                            // 更新思考过程显示
                                            if (!thinking.isEmpty()) {
                                                fullReasoning.setLength(0);
                                                fullReasoning.append(thinking);
                                                if (thinkingEdit != null) {
                                                    final String currentReasoning = fullReasoning.toString();
                                                    runOnMainThread(() -> {
                                                        thinkingEdit.setText(currentReasoning);
                                                        thinkingEdit.selectEnd();
                                                    });
                                                }
                                            }

                                            // 更新正式内容显示
                                            if (!finalContent.isEmpty()) {
                                                fullContent.setLength(0);
                                                fullContent.append(finalContent);
                                                if (resultEdit != null) {
                                                    final String currentContent = fullContent.toString();
                                                    runOnMainThread(() -> {
                                                        resultEdit.setText(currentContent);
                                                        resultEdit.selectEnd();
                                                    });
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // 不显示思考过程，直接追加
                                    fullContent.append(content);
                                    if (resultEdit != null) {
                                        final String currentContent = fullContent.toString();
                                        runOnMainThread(() -> {
                                            resultEdit.setText(currentContent);
                                            resultEdit.selectEnd();
                                        });
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.w("AIHelper", "JSON解析错误: " + e.getMessage() + ", data: " + data);
                }
            }
        }
        reader.close();
        connection.disconnect();

        // 处理缓冲区中剩余的内容
        if (contentBuffer.length() > 0) {
            String remainingContent = contentBuffer.toString();
            if (hasDetectedThinkingTag) {
                // 如果检测到思考标签，需要分离
                String[] result = separateThinkingFromContent(remainingContent);
                String thinking = result[0];
                String finalContent = result[1];

                if (!thinking.isEmpty()) {
                    fullReasoning.append(thinking);
                }
                if (!finalContent.isEmpty()) {
                    fullContent.append(finalContent);
                }
            } else {
                // 没有思考标签，直接追加
                fullContent.append(remainingContent);
            }
        }

        // 只使用 content 作为最终结果，不使用 reasoning_content
        // reasoning_content 是思考过程，不应该作为最终结果
        if (fullContent.length() == 0) {
            String errorDetail = "AI API返回空结果（未返回正式回答）\n\n思考过程:\n" + fullReasoning.toString() + "\n\n原始响应:\n" + rawResponse.toString();
            android.util.Log.e("AIHelper", errorDetail);
            throw new Exception(errorDetail);
        }

        // 清理 content 中可能混入的思考过程标记
        String finalContent = fullContent.toString();
        String cleanedContent = cleanThinkingTags(finalContent);

        // 返回结果：result[0]=正式内容（content），result[1]=思考过程（reasoning_content）
        return new String[]{cleanedContent, fullReasoning.toString()};
    }

    /**
     * 检测内容是否包含思考标签
     */
    private static boolean containsThinkingTag(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return content.contains("<think>") ||
               content.contains("<thinking>") ||
               content.contains("<reasoning>") ||
               content.contains("[思考]") ||
               content.contains("[Thinking]") ||
               content.contains("**思考过程**");
    }

    /**
     * 从内容中分离思考过程和正式内容
     * 支持多种思考标签格式：<think>, <thinking>, <reasoning>, [思考], **思考过程** 等
     *
     * @param content 完整内容
     * @return 数组：[0]=思考过程, [1]=正式内容, [2]=是否找到结束标签("true"/"false")
     */
    private static String[] separateThinkingFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return new String[]{"", "", "false"};
        }

        String thinking = "";
        String finalContent = "";
        boolean foundEndTag = false;

        // 定义思考标签的正则模式
        String[][] thinkingPatterns = {
            {"<think>", "</think>"},
            {"<thinking>", "</thinking>"},
            {"<reasoning>", "</reasoning>"},
            {"\\[思考\\]", "\\[/思考\\]"},
            {"\\[Thinking\\]", "\\[/Thinking\\]"},
            {"\\*\\*思考过程\\*\\*", "\\*\\*分析结果\\*\\*"}
        };

        String remainingContent = content;

        for (String[] pattern : thinkingPatterns) {
            String startPattern = pattern[0];
            String endPattern = pattern[1];

            java.util.regex.Matcher startMatcher = java.util.regex.Pattern.compile(startPattern).matcher(remainingContent);
            if (startMatcher.find()) {
                int startIndex = startMatcher.start();
                int startEnd = startMatcher.end();

                // 检查是否有结束标签
                java.util.regex.Matcher endMatcher = java.util.regex.Pattern.compile(endPattern).matcher(remainingContent);
                if (endMatcher.find(startIndex)) {
                    int endIndex = endMatcher.start();
                    int endEnd = endMatcher.end();

                    // 提取思考内容（不含标签）
                    thinking = remainingContent.substring(startEnd, endIndex).trim();

                    // 提取结束标签后的正式内容
                    finalContent = remainingContent.substring(endEnd).trim();
                    foundEndTag = true;
                } else {
                    // 结束标签还未出现，所有内容都是思考过程
                    thinking = remainingContent;
                    finalContent = "";
                    foundEndTag = false;
                }
                break;
            }
        }

        // 如果没有找到任何思考标签，所有内容都是正式内容
        if (thinking.isEmpty() && finalContent.isEmpty()) {
            finalContent = content;
        }

        return new String[]{thinking, finalContent, foundEndTag ? "true" : "false"};
    }

    /**
     * 清理内容中的思考过程标记
     * 某些模型会将思考过程混入 content 中，需要用此方法清理
     *
     * @param content AI 返回的内容
     * @return 清理后的内容
     */
    private static String cleanThinkingTags(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String result = content;

        // 移除 <think>...</think> 标签及其内容
        result = result.replaceAll("(?s)<think>.*?</think>", "");

        // 移除 <think>...</think> 标签及其内容
        result = result.replaceAll("(?s)<think>.*?</think>", "");

        // 移除 <thinking>...</thinking> 标签及其内容
        result = result.replaceAll("(?s)<thinking>.*?</thinking>", "");

        // 移除 [思考]...[/思考] 标签及其内容
        result = result.replaceAll("(?s)\\[思考\\].*?\\[/思考\\]", "");

        // 移除 [Thinking]...[/Thinking] 标签及其内容
        result = result.replaceAll("(?s)\\[Thinking\\].*?\\[/Thinking\\]", "");

        // 移除 **思考过程** 开头到 **分析结果** 结尾之间的内容
        result = result.replaceAll("(?s)\\*\\*思考过程\\*\\*.*?\\*\\*分析结果\\*\\*", "**分析结果**");

        // 移除 "思考过程：" 开头到 "分析结果：" 之间的内容
        result = result.replaceAll("(?s)思考过程[：:].*?分析结果[：:]", "分析结果：");

        // 清理多余的空行
        result = result.replaceAll("\n{3,}", "\n\n");

        // 去除首尾空白
        result = result.trim();

        return result;
    }

    /**
     * 在主线程执行任务
     */
    public static void runOnMainThread(@NonNull Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }

    /**
     * AI 分析代码（无UI版本，用于后台分析）
     * @param context 插件上下文
     * @param code 要分析的代码
     * @param showThinking 是否显示思考过程
     * @param customPrompt 自定义提示词（如果为 null 则使用默认）
     * @return 分析结果数组，第一个元素是分析结果
     */
    @Nullable
    public static String[] analyzeCodeWithAINoUI(
            @NonNull PluginContext context,
            @NonNull String code,
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

        URL url = new URL(completionsUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000); // 后台分析给更多时间
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
        StringBuilder fullContent = new StringBuilder();
        StringBuilder contentBuffer = new StringBuilder();
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
                        if (firstChoice != null) {
                            String content = null;

                            // 尝试多种格式获取内容
                            JSONObject delta = firstChoice.optJSONObject("delta");
                            if (delta != null) {
                                content = delta.optString("content", "");
                            }

                            if (content == null || content.isEmpty()) {
                                content = firstChoice.optString("text", "");
                            }

                            if (content == null || content.isEmpty()) {
                                content = firstChoice.optString("content", "");
                            }

                            if (content != null && !content.isEmpty() && !"null".equals(content)) {
                                contentBuffer.append(content);
                            }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.w("AIHelper", "JSON解析错误: " + e.getMessage());
                }
            }
        }
        reader.close();
        connection.disconnect();

        // 处理缓冲区剩余内容
        if (contentBuffer.length() > 0) {
            fullContent.append(contentBuffer.toString());
        }

        if (fullContent.length() == 0) {
            throw new Exception("AI API返回空结果（未返回正式回答）");
        }

        String result = fullContent.toString();
        return new String[]{result};
    }
}
