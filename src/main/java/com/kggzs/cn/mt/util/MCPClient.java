package com.kggzs.cn.mt.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * MCP (Model Context Protocol) 客户端工具类
 * 基于 JSON-RPC 2.0 协议，通过 HTTP POST 与 MCP 服务器通信
 * 支持 tools/list、tools/call、resources/list、resources/read 等标准方法
 */
public class MCPClient {

    private final String serverUrl;
    private String clientId;
    private String protocolVersion;
    private String serverName;
    private String serverVersion;

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;

    /**
     * 构造函数
     *
     * @param serverUrl MCP 服务器地址
     */
    public MCPClient(@NonNull String serverUrl) {
        this.serverUrl = serverUrl;
        this.clientId = "mt-kang-plugin-" + UUID.randomUUID().toString().substring(0, 8);
        this.protocolVersion = "2025-03-26";
        this.serverName = "";
        this.serverVersion = "";
    }

    /**
     * 生成 JSON-RPC 2.0 请求
     */
    private JSONObject createRequest(@NonNull String method, @Nullable JSONObject params) {
        try {
            JSONObject request = new JSONObject();
            request.put("jsonrpc", "2.0");
            request.put("id", UUID.randomUUID().toString().substring(0, 8));
            request.put("method", method);
            if (params != null) {
                request.put("params", params);
            }
            return request;
        } catch (Exception e) {
            throw new RuntimeException("创建JSON-RPC请求失败", e);
        }
    }

    /**
     * 发送 JSON-RPC 请求并获取响应
     */
    @NonNull
    private JSONObject sendRequest(@NonNull JSONObject request) throws Exception {
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setDoOutput(true);

        String requestBody = request.toString();
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            String errorBody = "";
            try {
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    sb.append(line);
                }
                errorReader.close();
                errorBody = sb.toString();
            } catch (Exception ignored) {
            }
            throw new Exception("MCP服务器返回错误: " + responseCode + " - " + errorBody);
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        JSONObject jsonResponse = new JSONObject(response.toString());

        if (jsonResponse.has("error")) {
            JSONObject error = jsonResponse.getJSONObject("error");
            String errorMsg = error.optString("message", "未知错误");
            int errorCode = error.optInt("code", 0);
            throw new Exception("MCP方法调用失败 [" + errorCode + "]: " + errorMsg);
        }

        return jsonResponse;
    }

    /**
     * 初始化 MCP 连接
     * 发送 initialize 请求并获取服务器信息
     *
     * @return 服务器信息 JSON
     */
    @NonNull
    public JSONObject initialize() throws Exception {
        JSONObject params = new JSONObject();
        params.put("protocolVersion", protocolVersion);

        JSONObject clientInfo = new JSONObject();
        clientInfo.put("name", "mt-kang-plugin");
        clientInfo.put("version", "2.0.0");
        params.put("clientInfo", clientInfo);

        JSONObject capabilities = new JSONObject();
        capabilities.put("tools", new JSONObject());
        capabilities.put("resources", new JSONObject());
        params.put("capabilities", capabilities);

        JSONObject request = createRequest("initialize", params);
        JSONObject response = sendRequest(request);

        if (response.has("result")) {
            JSONObject result = response.getJSONObject("result");
            serverName = result.optJSONObject("serverInfo") != null
                ? result.getJSONObject("serverInfo").optString("name", "") : "";
            serverVersion = result.optJSONObject("serverInfo") != null
                ? result.getJSONObject("serverInfo").optString("version", "") : "";
            protocolVersion = result.optString("protocolVersion", protocolVersion);
        }

        return response;
    }

    /**
     * 获取服务器名称
     */
    @NonNull
    public String getServerName() {
        return serverName.isEmpty() ? "MCP Server" : serverName;
    }

    /**
     * 获取服务器版本
     */
    @NonNull
    public String getServerVersion() {
        return serverVersion.isEmpty() ? "Unknown" : serverVersion;
    }

    /**
     * 列出 MCP 服务器上可用的工具
     *
     * @return 工具列表 JSONArray
     */
    @NonNull
    public JSONArray listTools() throws Exception {
        JSONObject request = createRequest("tools/list", null);
        JSONObject response = sendRequest(request);

        if (response.has("result")) {
            JSONObject result = response.getJSONObject("result");
            return result.optJSONArray("tools");
        }
        return new JSONArray();
    }

    /**
     * 调用 MCP 服务器上的工具
     *
     * @param toolName  工具名称
     * @param arguments 工具参数（可为 null）
     * @return 工具调用结果
     */
    @NonNull
    public JSONObject callTool(@NonNull String toolName, @Nullable JSONObject arguments) throws Exception {
        JSONObject params = new JSONObject();
        params.put("name", toolName);
        if (arguments != null) {
            params.put("arguments", arguments);
        }

        JSONObject request = createRequest("tools/call", params);
        JSONObject response = sendRequest(request);
        return response;
    }

    /**
     * 列出 MCP 服务器上的资源
     *
     * @return 资源列表 JSONArray
     */
    @NonNull
    public JSONArray listResources() throws Exception {
        JSONObject request = createRequest("resources/list", null);
        JSONObject response = sendRequest(request);

        if (response.has("result")) {
            return response.getJSONObject("result").optJSONArray("resources");
        }
        return new JSONArray();
    }

    /**
     * 读取 MCP 服务器上的资源
     *
     * @param resourceUri 资源 URI
     * @return 资源内容
     */
    @NonNull
    public String readResource(@NonNull String resourceUri) throws Exception {
        JSONObject params = new JSONObject();
        params.put("uri", resourceUri);

        JSONObject request = createRequest("resources/read", params);
        JSONObject response = sendRequest(request);

        if (response.has("result")) {
            JSONObject result = response.getJSONObject("result");
            JSONArray contents = result.optJSONArray("contents");
            if (contents != null && contents.length() > 0) {
                return contents.getJSONObject(0).optString("text", "");
            }
        }
        return "";
    }

    /**
     * 测试与 MCP 服务器的连接
     *
     * @return 测试结果信息
     */
    @NonNull
    public String testConnection() {
        try {
            JSONObject initResult = initialize();

            String serverName = getServerName();
            String serverVer = getServerVersion();

            JSONArray tools = listTools();
            int toolCount = tools != null ? tools.length() : 0;

            return "连接成功！\n"
                + "服务器: " + serverName + " v" + serverVer + "\n"
                + "协议版本: " + protocolVersion + "\n"
                + "可用工具数: " + toolCount;
        } catch (Exception e) {
            return "连接失败: " + e.getMessage();
        }
    }
}
