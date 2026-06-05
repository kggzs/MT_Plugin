package com.kggzs.cn.mt.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bin.mt.plugin.api.PluginContext;

import com.kggzs.cn.mt.AIChatMenu;
import com.kggzs.cn.mt.util.MCPClient;

/**
 * AI 对话辅助类
 * 管理与 AI 的对话历史，支持多轮对话和 MCP 工具真实调用
 */
public class AIChatHelper {

    private final JSONArray messages;
    private final PluginContext context;

    private static final String SYSTEM_PROMPT = "你是一个有用的AI助手，请根据用户的问题提供详细、准确的回答。";
    private static final int MAX_TOOL_ROUNDS = 10;

    public AIChatHelper(@NonNull PluginContext context) {
        this.context = context;
        this.messages = new JSONArray();
    }

    /**
     * 初始化系统消息（始终使用系统提示词）
     */
    public void rebuildSystemMessage() {
        // 先移除旧的系统消息（如果有）
        if (messages.length() > 0) {
            try {
                JSONObject firstMsg = messages.getJSONObject(0);
                if ("system".equals(firstMsg.optString("role", ""))) {
                    messages.remove(0);
                }
            } catch (Exception e) {
                android.util.Log.e("AIChatHelper", "检查系统消息失败", e);
            }
        }
        // 添加系统消息到开头
        try {
            String systemPrompt = AIHelper.getPrompt(context);
            if (systemPrompt == null || systemPrompt.isEmpty()) {
                systemPrompt = SYSTEM_PROMPT;
            }

            if (AIHelper.isMcpEnabled(context)) {
                systemPrompt += "\n\n你可以通过MCP (Model Context Protocol) 服务反复调用外部工具来辅助回答。当需要调用工具时，请在回答中输出以下格式的JSON（不要用代码块包裹）:\n{\"tool\": \"工具名\", \"arguments\": { \"参数名\": \"参数值\" }}\n\n系统会自动执行工具并将结果返回给你。你可以根据结果决定是继续调用其他工具还是给出最终回答。如果需要多次调用工具，只需在每次回答时输出新的工具调用JSON即可。";

                String skillsJson = AIHelper.getSkills(context);
                try {
                    JSONArray skills = new JSONArray(skillsJson);
                    if (skills.length() > 0) {
                        systemPrompt += "\n\n已配置的自定义技能:";
                        for (int i = 0; i < skills.length(); i++) {
                            JSONObject skill = skills.getJSONObject(i);
                            String name = skill.optString("name", "");
                            String prompt = skill.optString("prompt", "");
                            systemPrompt += "\n- " + name;
                            if (!prompt.isEmpty()) {
                                String shortPrompt = prompt.length() > 80 ? prompt.substring(0, 80) + "..." : prompt;
                                systemPrompt += ": " + shortPrompt;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }

                String mcpSkillsJson = AIHelper.getMcpSkills(context);
                try {
                    JSONArray mcpSkills = new JSONArray(mcpSkillsJson);
                    if (mcpSkills.length() > 0) {
                        systemPrompt += "\n\n可用的MCP技能:";
                        for (int i = 0; i < mcpSkills.length(); i++) {
                            JSONObject skill = mcpSkills.getJSONObject(i);
                            String name = skill.optString("name", "");
                            String prompt = skill.optString("prompt", "");
                            systemPrompt += "\n- " + name;
                            if (!prompt.isEmpty()) {
                                systemPrompt += ": " + prompt;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            // 将系统消息插入到开头
            JSONArray newMessages = new JSONArray();
            newMessages.put(systemMessage);
            for (int i = 0; i < messages.length(); i++) {
                newMessages.put(messages.getJSONObject(i));
            }
            // 清空原数组并复制新数组
            while (messages.length() > 0) {
                messages.remove(0);
            }
            for (int i = 0; i < newMessages.length(); i++) {
                messages.put(newMessages.getJSONObject(i));
            }
        } catch (Exception e) {
            android.util.Log.e("AIChatHelper", "初始化系统消息失败", e);
        }
    }

    /**
     * 发送消息并自动处理 MCP 工具调用（Agent 模式）
     * 支持多轮工具调用：AI → 检测工具 → 执行 → 反馈 → AI → 检测工具 → ... → 最终回答
     */
    public void sendMessage(@NonNull String userMessage,
                            @Nullable OnStreamCallback onStream,
                            @Nullable OnCompleteCallback onComplete,
                            @Nullable OnErrorCallback onError) {
        try {
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);

            String firstResponse = callAiAndStream(onStream, onError);
            if (firstResponse == null) {
                if (onError != null) onError.onError("AI 返回空响应");
                return;
            }

            if (!AIHelper.isMcpEnabled(context)) {
                JSONObject assistantMsgFinal = new JSONObject();
                assistantMsgFinal.put("role", "assistant");
                assistantMsgFinal.put("content", firstResponse);
                messages.put(assistantMsgFinal);
                if (onComplete != null) onComplete.onComplete(firstResponse);
                return;
            }

            String lastContent = firstResponse;
            int toolRound = 0;

            while (toolRound < MAX_TOOL_ROUNDS) {
                String toolCallArgs = detectToolCall(lastContent);
                if (toolCallArgs == null) break;

                JSONObject assistantMsg = new JSONObject();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", lastContent);
                messages.put(assistantMsg);

                String toolResult = executeToolCall(toolCallArgs);
                toolRound++;

                String statusMsg = "\n\n[Agent 第" + toolRound + "轮] MCP工具已调用，获取结果中...";
                if (onStream != null) {
                    onStream.onStream(lastContent + statusMsg);
                }

                String feedbackContent = "以下是第" + toolRound + "轮工具调用返回结果:\n" + (toolResult != null ? toolResult : "工具调用无返回结果") + "\n\n请根据以上结果继续回答。如果需要再次调用其他工具，可以继续输出工具调用JSON。如果任务已完成，请给出最终回答。";
                JSONObject toolMsg = new JSONObject();
                toolMsg.put("role", "user");
                toolMsg.put("content", feedbackContent);
                messages.put(toolMsg);

                lastContent = callAiAndStream(onStream, onError);
                if (lastContent == null) return;
            }

            if (toolRound >= MAX_TOOL_ROUNDS) {
                lastContent += "\n\n[Agent] 已达到最大工具调用次数(" + MAX_TOOL_ROUNDS + "轮)，已停止继续调用。";
                if (onStream != null) {
                    onStream.onStream(lastContent);
                }
            }

            JSONObject finalAssistantMsg = new JSONObject();
            finalAssistantMsg.put("role", "assistant");
            finalAssistantMsg.put("content", lastContent);
            messages.put(finalAssistantMsg);

            if (onComplete != null) onComplete.onComplete(lastContent);

        } catch (Exception e) {
            android.util.Log.e("AIChatHelper", "对话失败", e);
            if (onError != null) {
                onError.onError(e.getMessage() != null ? e.getMessage() : "未知错误");
            }
        }
    }

    /**
     * 调用 AI API 并流式返回结果
     */
    @Nullable
    private String callAiAndStream(@Nullable OnStreamCallback onStream,
                                   @Nullable OnErrorCallback onError) throws Exception {
        String apiUrl = AIHelper.getApiUrl(context);
        String aiModel = AIHelper.getAiModel(context);
        String apiKey = AIHelper.getApiKey(context);

        String completionsUrl = apiUrl.endsWith("/chat/completions") ? apiUrl :
                               (apiUrl.endsWith("/") ? apiUrl + "chat/completions" : apiUrl + "/chat/completions");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", aiModel);
        requestBody.put("stream", true);
        requestBody.put("messages", messages);

        URL url = new URL(completionsUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000);
        connection.setDoOutput(true);

        // 注册活跃连接，支持中断
        AIChatMenu.setActiveConnection(connection);

        connection.getOutputStream().write(requestBody.toString().getBytes(StandardCharsets.UTF_8));

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            AIChatMenu.setActiveConnection(null);
            throw new Exception("AI API错误: " + responseCode + " - " + errorResponse);
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        // 分别累积 reasoning_content（思考过程）和 content（正式内容）
        StringBuilder reasoningBuffer = new StringBuilder();
        StringBuilder contentBuffer = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("data: ")) {
                String data = line.substring(6);
                if ("[DONE]".equals(data)) {
                    break;
                }
                try {
                    JSONObject chunk = new JSONObject(data);
                    JSONArray choices = chunk.optJSONArray("choices");
                    if (choices != null && choices.length() > 0) {
                        JSONObject firstChoice = choices.getJSONObject(0);
                        AIHelper.StreamChunkResult parsed = AIHelper.StreamChunkParser.parse(firstChoice);

                        boolean hasContent = parsed.hasContent();
                        boolean hasReasoning = parsed.hasReasoning();

                        if (hasReasoning) {
                            reasoningBuffer.append(parsed.reasoning);
                        }
                        if (hasContent) {
                            contentBuffer.append(parsed.content);
                        }

                        if (onStream != null && hasContent) {
                            // 已有正式内容时，只显示清理后的正式内容
                            // 思考过程（reasoning）不显示在对话界面，以免干扰用户阅读
                            String displayContent = contentBuffer.toString();
                            String[] separated = StreamParser.separateThinkingFromContent(displayContent);
                            String cleanContent = "true".equals(separated[2]) ? separated[1] : displayContent;
                            onStream.onStream(cleanContent);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.w("AIChatHelper", "解析chunk失败: " + e.getMessage());
                }
            }
        }
        reader.close();
        connection.disconnect();
        AIChatMenu.setActiveConnection(null);

        // 最终以正式内容为准，兜底清理内嵌思考标签
        String finalContent = contentBuffer.length() > 0
                ? contentBuffer.toString() : reasoningBuffer.toString();
        String result = StreamParser.cleanThinkingTags(finalContent);
        if (result.isEmpty()) {
            throw new Exception("AI返回空结果");
        }
        return result;
    }

    /**
     * 检测 AI 响应中是否包含 MCP 工具调用
     * 支持格式: {"tool": "工具名", "arguments": {...}} 或 {"name": "工具名", "arguments": {...}}
     * 支持在 ```json ... ``` 代码块内或直接输出
     *
     * @return 匹配到的完整 JSON 字符串，无工具调用时返回 null
     */
    @Nullable
    private String detectToolCall(@NonNull String response) {
        if (response == null || response.isEmpty()) return null;

        String text = response;

        int jsonBlockStart = text.indexOf("```json");
        if (jsonBlockStart >= 0) {
            int jsonBlockEnd = text.indexOf("```", jsonBlockStart + 7);
            if (jsonBlockEnd >= 0) {
                text = text.substring(jsonBlockStart + 7, jsonBlockEnd).trim();
            }
        }

        Pattern pattern = Pattern.compile("\\{\\s*\"(tool|name)\"\\s*:\\s*\"([^\"]+)\"\\s*[,}]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int braceStart = text.indexOf("{", matcher.start());
            if (braceStart >= 0) {
                int depth = 0;
                int braceEnd = -1;
                for (int i = braceStart; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (c == '{') depth++;
                    else if (c == '}') {
                        depth--;
                        if (depth == 0) {
                            braceEnd = i + 1;
                            break;
                        }
                    }
                }
                if (braceEnd > 0) {
                    return text.substring(braceStart, braceEnd);
                }
            }
        }
        return null;
    }

    /**
     * 执行 MCP 工具调用
     *
     * @param toolCallJson 工具调用 JSON 字符串，如 {"tool": "xxx", "arguments": {...}}
     * @return 工具调用结果文本，失败时返回 null
     */
    @Nullable
    private String executeToolCall(@NonNull String toolCallJson) {
        try {
            String mcpUrl = AIHelper.getMcpServerUrl(context);
            JSONObject callObj = new JSONObject(toolCallJson);

            String toolName = callObj.optString("tool", "");
            if (toolName.isEmpty()) {
                toolName = callObj.optString("name", "");
            }
            if (toolName.isEmpty()) {
                android.util.Log.w("AIChatHelper", "MCP tool name is empty, skipping tool call");
                return null;
            }

            JSONObject arguments = callObj.optJSONObject("arguments");
            if (arguments == null) {
                arguments = new JSONObject();
            }

            MCPClient client = new MCPClient(mcpUrl);
            JSONObject result = client.callTool(toolName, arguments);

            if (result.has("result")) {
                JSONObject resultObj = result.getJSONObject("result");
                JSONArray content = resultObj.optJSONArray("content");
                if (content != null && content.length() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < content.length(); i++) {
                        JSONObject item = content.getJSONObject(i);
                        String type = item.optString("type", "text");
                        if ("text".equals(type)) {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append(item.optString("text", ""));
                        } else {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append("[类型: ").append(type).append("] ");
                            sb.append(item.optString("text", item.toString()));
                        }
                    }
                    if (sb.length() > 0) return sb.toString();
                }
                boolean isError = resultObj.optBoolean("isError", false);
                if (isError) {
                    return "工具调用出错";
                }
            }

            if (result.has("error")) {
                JSONObject error = result.getJSONObject("error");
                return "工具调用失败: " + error.optString("message", "未知错误");
            }

            return result.toString();
        } catch (Exception e) {
            android.util.Log.e("AIChatHelper", "执行MCP工具失败", e);
            return "工具调用异常: " + e.getMessage();
        }
    }

    /**
     * 获取对话历史
     */
    @NonNull
    public ArrayList<ChatMessage> getChatHistory() {
        ArrayList<ChatMessage> history = new ArrayList<>();
        try {
            for (int i = 1; i < messages.length(); i++) {
                JSONObject msg = messages.getJSONObject(i);
                String role = msg.optString("role", "");
                String content = msg.optString("content", "");
                if (!role.isEmpty() && !content.isEmpty()) {
                    history.add(new ChatMessage(role, content));
                }
            }
        } catch (Exception e) {
            android.util.Log.e("AIChatHelper", "获取对话历史失败", e);
        }
        return history;
    }

    /**
     * 清空对话历史（保留系统消息）
     */
    public void clearHistory() {
        while (messages.length() > 1) {
            messages.remove(messages.length() - 1);
        }
    }

    public static class ChatMessage {
        public final String role;
        public final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public interface OnStreamCallback {
        void onStream(String accumulatedContent);
    }

    public interface OnCompleteCallback {
        void onComplete(String fullContent);
    }

    public interface OnErrorCallback {
        void onError(String errorMessage);
    }
}