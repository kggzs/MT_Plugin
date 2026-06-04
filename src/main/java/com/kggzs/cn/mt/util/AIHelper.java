package com.kggzs.cn.mt.util;

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
    private static final String DEFAULT_AI_MODEL = "MT-v2";
    private static final String DEFAULT_API_KEY = "sk-MT-kggzs-API-key-1724464998";
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

    // MCP 配置键名
    private static final String PREF_MCP_ENABLED = "mcp_enabled";
    private static final String PREF_MCP_SERVER_URL = "mcp_server_url";
    private static final String PREF_MCP_SKILLS = "mcp_skills";

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

    // ============================================================
    // MCP 配置方法
    // ============================================================

    /**
     * 获取 MCP 服务是否启用
     */
    public static boolean isMcpEnabled(@NonNull PluginContext context) {
        return context.getPreferences().getBoolean(PREF_MCP_ENABLED, false);
    }

    /**
     * 设置 MCP 服务是否启用
     */
    public static void setMcpEnabled(@NonNull PluginContext context, boolean enabled) {
        context.getPreferences().edit().putBoolean(PREF_MCP_ENABLED, enabled).apply();
    }

    /**
     * 获取 MCP 服务器地址
     */
    @NonNull
    public static String getMcpServerUrl(@NonNull PluginContext context) {
        String url = context.getPreferences().getString(PREF_MCP_SERVER_URL, "");
        return url.isEmpty() ? "http://127.0.0.1:8787/mcp" : url;
    }

    /**
     * 设置 MCP 服务器地址
     */
    public static void setMcpServerUrl(@NonNull PluginContext context, @NonNull String url) {
        context.getPreferences().edit().putString(PREF_MCP_SERVER_URL, url).apply();
    }

    /**
     * 获取 MCP Skill 列表（JSON数组格式）
     * 每个元素：{"name": "Skill名称", "prompt": "提示词内容"}
     * 当未配置时，自动初始化默认 MCP 技能
     */
    @NonNull
    public static String getMcpSkills(@NonNull PluginContext context) {
        String skills = context.getPreferences().getString(PREF_MCP_SKILLS, "");
        if (!skills.isEmpty() && !"[]".equals(skills)) {
            return skills;
        }
        // 首次使用或列表为空时，初始化默认 MCP 技能
        String defaultSkill = createDefaultMcpSkill();
        if (!"[]".equals(defaultSkill)) {
            setMcpSkills(context, defaultSkill);
        }
        return defaultSkill;
    }

    /**
     * 创建默认 MCP 技能（mt-mcp-apk-analyzer）
     */
    @NonNull
    private static String createDefaultMcpSkill() {
        try {
            JSONArray skills = new JSONArray();
            JSONObject skill = new JSONObject();
            skill.put("name", "mt-mcp-apk-analyzer");
            skill.put("prompt", """
---
name: "mt-mcp-apk-analyzer"
description: "MT管理器MCP服务专用APK分析技能。基于MT管理器的mt_mcp服务对APK文件进行深度分析（支付功能、敏感字符串、类结构、资源等），生成结构化分析报告。当用户需要分析APK时触发此技能。"
---

# MT管理器MCP APK分析技能

MT管理器MCP服务专用APK分析技能，使用 mt_mcp 服务对 APK 文件进行深度分析并生成结构化报告。

## 核心原则

**本技能不预设固定分析内容，而是根据用户的具体分析需求动态执行分析。**

用户可以指定：
- APK 文件（支持模糊名称匹配）
- 分析关键词（单个或多个）
- 搜索范围（dex_string、dex_class、dex_method、smali、axml、resource_table_* 等）
- 分析深度（简单搜索、详细分析、完整报告）
- 特定包名或类名过滤

### 分析决策框架

```
用户意图
├─ "这个APP做了什么"        → 架构概览路径（T1）
├─ "找到XXX功能在哪"        → 功能定位路径（T2）
├─ "有没有安全隐患"          → 安全审计路径（T4）
├─ "网络请求都去了哪"        → 网络追踪路径（T3）
├─ "这个方法谁调用的"        → 调用链追踪路径（T5）
├─ "能不能改成XXX"          → 先定位 → 再出修改方案
└─ 模糊意图                 → 先读 Manifest 建立全局认知，再追问细化
```

**关键判断：** 用户说"分析"时往往隐含了"然后修改"的意图。分析过程中应主动记录可修改点，为后续解决方案建议积累素材。

---

# 第一部分：快速入门

## 1.1 核心流程

> **必须先调用 `open()`** → 获取 `workspaceId` → 后续所有调用都需要此 ID

| 工具 | 用途 | 关键参数 |
|------|------|----------|
| `mt_apk_open` | 打开 APK，获取 workspaceId | path |
| `mt_apk_list` | 浏览类/文件/资源 | view, prefix / className |
| `mt_apk_search` | 搜索关键词/代码/字符串 | query, scopes, queryType |
| `mt_apk_read` | 读取 Smali/XML/资源详情 | locator |
| `mt_apk_continue` | 翻页 | nextCursor |

## 1.2 搜索范围选择器

| 查找目标 | Scope | 示例 query |
|----------|-------|------------|
| 按名称查类 | `dex_class` | `"VipStatus"` |
| 按名称查方法 | `dex_method` | `"isVip"` |
| 按名称查字段 | `dex_field` | `"vipStatus"` |
| 硬编码文本/URL/密钥 | `dex_string` | `"https://api."` |
| Smali 指令模式 | `smali` | `"sget-object.*VipStatus"` (regex) |
| XML 清单/布局文本 | `axml` | `"android.permission"` |
| 按名称查资源 | `resource_table_name` | `"app_name"` |
| 按值查资源 | `resource_table_value` | `"error message"` |
| 按路径查文件 | `zip_entries` | `"lib/armeabi"` |
| 未知/广泛探索 | `["dex_string","smali"]` | `"keyword"` |

## 1.3 常见陷阱

**#1 scopes 格式：**

```
OK:   scopes="dex_string"                    (单 scope 字符串)
OK:   scopes=["dex_string","smali"]          (多 scope 数组)
BAD:  scopes="dex_string,smali"              (逗号字符串 → 错误)
BAD:  scopes='["dex_string"]'                (引号包裹 JSON → 错误)
```

**#2 Dalvik 类名格式：**

```
Java:   com.example.MyClass         →  Dalvik: Lcom/example/MyClass;
Java:   com.example.MyClass$Inner   →  Dalvik: Lcom/example/MyClass$Inner;
规则:   L 前缀 + / 替换 . + ; 后缀
```

**#3 list 与 read 的 kind 映射：**

```
list: view="dex_class_outline"  →  read: kind="dex_class"     (不是 dex_class_outline!)
list: dex_classes 条目 locator  →  read: kind="dex_class"
outline methods[].locator       →  read: kind="dex_method"
outline fields[].locator        →  read: kind="dex_field"
```

**#4 搜索结果"看起来没有"但实际有：** dex_string 搜不到时，换用 `smali` scope 搜索 `const-string` 指令，或用 `axml` scope 搜索 Manifest 中的 meta-data。

**#5 混淆类方法太多找不到目标：** 先看 `methods[].interestingStrings`，找到包含目标字符串的方法，再定向读取。

**#6 分页游标过期：** 两次 continue 间隔太久游标可能失效，重新执行原始查询即可。

**#7 read 静默截断：** 返回内容末尾不是完整的 `.method` / `.end method` 结构时，说明被截断，需增大 `maxChars` 或用 `startLine` 分段读取。

---

# 第二部分：工具规范

## 2.1 mt_apk_open

打开 APK 工作区。**必须第一个调用。** 返回 `workspaceId`，后续所有工具都需要此 ID。

**参数：**

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| path | 是 | - | 相对 APK 路径或 `"mt://current-apk"` |
| reuseWorkspaceByPath | 否 | true | 对相同 APK 复用已有工作区 |
| workspaceId | 否 | - | 通过 ID 重新打开指定工作区 |

路径规则：仅支持相对路径，不支持绝对路径、`\\` 反斜杠、`.`/`..` 路径段。

**返回字段：**

| 字段 | 说明 |
|------|------|
| `workspaceId` | **保存此值** — 后续所有调用必需 |
| `compact.manifest.package` | 应用包名 |
| `compact.manifest.versionName` | 版本字符串 |
| `compact.manifest.versionCode` | 版本号 |
| `compact.count.dexClasses` | 总类数（帮助决定搜索策略） |
| `compact.count.zipEntries` | 总文件数 |
| `compact.count.resourceTableEntries` | 总资源数 |
| `compact.capability.hasDex` | 是否有 DEX 数据 |
| `compact.capability.hasResources` | 是否有资源表 |
| `compact.capability.hasAssets` | 是否有 assets |

## 2.2 mt_apk_list

按视图类型浏览 APK 结构。返回数据数组 + 分页信息。

**参数：**

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| workspaceId | 是 | - | 来自 open 结果 |
| view | 是 | - | 见下方视图表 |
| prefix | 否 | - | 按前缀过滤（格式因视图而异） |
| className | 否 | - | **仅 view=dex_class_outline**，必须是 Dalvik 描述符 |
| limit | 否 | 200 | 页大小，最大 1000 |

**视图类型：**

| view | 用途 | 关键参数 | 返回 |
|------|------|----------|------|
| `zip_entries` | 列出 APK 中的文件 | prefix="res/layout/" | zip_entry 定位器 |
| `dex_classes` | 列出 DEX 类 | prefix="com/example" | dex_class 定位器 + fieldCount / methodCount / classSmaliStartLine |
| `dex_class_outline` | 类字段 + 方法 | className="Lcom/ex/Cls;" (Dalvik!) | header + fields[] + methods[] 含定位器 |
| `resource_table_entries` | 资源表条目 | prefix="string/" 或 "0x7f01" | resource_table_entry 定位器 |

**outline 返回字段（最常用视图）：**

| 字段 | 说明 |
|------|------|
| `header.className` | 完整 Dalvik 类名 |
| `header.super` | 父类（用于继承分析） |
| `header.implements` | 实现的接口（数组） |
| `header.source` | 源文件名（.kt = Kotlin, .java = Java） |
| `header.access` | public / private / abstract 等 |
| `fields[].name` / `fields[].sig` / `fields[].access` | 字段名 / 签名 / 修饰符 |
| `fields[].locator` | 传给 read → kind="dex_field" |
| `methods[].name` / `methods[].sig` / `methods[].access` | 方法名 / 签名 / 修饰符 |
| `methods[].interestingStrings` | 此方法使用的字符串常量 |
| `methods[].interestingInvokes` | 此方法调用的其他方法 |
| `methods[].locator` | 传给 read → kind="dex_method" |
| `classSmaliStartLine` | 可用作 read startLine 进行定向读取 |

## 2.3 mt_apk_read

通过 list 或 search 获取的 locator 读取详细内容。

**参数：**

| 参数 | 默认值 | 最大值 | 适用范围 |
|------|--------|--------|----------|
| workspaceId | - | - | 所有 |
| locator | - | - | 所有（来自 list/search 结果） |
| startLine | 1 | - | dex_class, zip_entry, axml |
| startColumn | 1 | - | dex_class, zip_entry, axml |
| limit | 500 | 2000 | 文本定位器（hex 格式时不可用） |
| maxChars | 49152 | 131072 | 文本定位器（resource_table_entry 时不可用） |
| includeLineNumbers | false | - | 文本定位器 |
| format | "text" | - | 仅 zip_entry："text" 或 "hex" |
| maxBytes | 256 | 4096 | 仅 hex 格式 |
| hexOffset | 0 | - | 仅 hex 格式 |
| perValueTextMaxChars | 4096 | 131072 (limit=1时) | 仅 resource_table_entry |
| valueOffset | 0 | - | 仅 resource_table_entry |

**Locator 种类：**

| kind | 必填字段 | 输出 | 示例 |
|------|----------|------|------|
| `dex_class` | className | 完整 Smali 源码 | `{"className":"Lcom/ex/Cls;","kind":"dex_class"}` |
| `dex_method` | className + methodSig | 方法 Smali | `{"className":"Lcom/ex/Cls;","methodSig":"onCreate()V","kind":"dex_method"}` |
| `dex_field` | className + fieldSig | 字段定义 | `{"className":"Lcom/ex/Cls;","fieldSig":"name:Ljava/lang/String;","kind":"dex_field"}` |
| `zip_entry` | path | 原始文件内容 | `{"path":"assets/config.json","kind":"zip_entry"}` |
| `axml` | path | 解码后的 Android XML | `{"path":"AndroidManifest.xml","kind":"axml"}` |
| `resource_table_entry` | resourceTableId | 资源值 | `{"resourceTableId":"0x7f040001","kind":"resource_table_entry"}` |

**读取大型类的策略：**

| 策略 | 适用场景 | 示例 |
|------|----------|------|
| 行范围 | 知道大概行号 | `read(locator, startLine=100, limit=50)` |
| 最大字符 | 需要完整类但默认太小 | `read(locator, maxChars=131072)` |
| 先 outline | 只需特定方法 | `list(outline) → 找到方法 → read(locator, startLine=methodLine, limit=30)` |

## 2.4 mt_apk_search

最强大的工具。搜索所有 APK 内容。

**必填参数：**

| 参数 | 说明 |
|------|------|
| workspaceId | 来自 open 结果 |
| query | 搜索关键词或正则表达式 |
| queryType | `"literal"` (文本匹配) 或 `"regex"` (正则表达式) |
| caseSensitive | 布尔值 — 是否区分大小写 |
| scopes | 搜索范围 — 见下方 scope 表 |

**可选参数：**

| 参数 | 默认值 | 最大值 | 说明 |
|------|--------|--------|------|
| limit | 50 | 200 | 每页最大命中数 |
| matchMode | "contains" | - | "contains" (子串) 或 "exact" (全匹配)。**scopes 包含 smali 时 exact 无效** |
| includeSnippet | true | - | 返回预览片段 |
| includeMatchOffsets | false | - | 返回 matchStart, matchEnd, matchedText |
| snippetMaxChars | 240 | 1000 | 每条命中的最大片段长度 |
| classPrefix | - | - | 按类所有者前缀过滤（Java、斜杠或 L 前缀格式） |
| zipEntryPrefix | - | - | 按 ZIP 条目路径前缀过滤（区分大小写） |
| resourceTableTypes | - | - | 按资源类型过滤（如 "string", "layout"） |
| dexStringGranularity | "class" | - | 仅 dex_string："class" 返回匹配类，"member" 返回匹配字段/方法 |

**所有 Scope：**

| Scope | 搜索内容 | 返回 locator kind | 最佳用途 |
|-------|----------|-------------------|----------|
| `zip_entries` | ZIP 文件路径 | zip_entry | 按路径查找文件 |
| `axml` | 解码后的 XML 文本 | axml | 搜索清单/布局 |
| `resource_table_id` | 资源 ID (0x...) | resource_table_entry | 按 ID 查找资源 |
| `resource_table_name` | 资源名称 | resource_table_entry | 按名称查找资源 |
| `resource_table_value` | 资源值 | resource_table_entry | 查找字符串资源 |
| `dex_class` | 类名/描述符 | dex_class | 查找类定义 |
| `dex_field` | 字段名（不含签名） | dex_field | 查找成员变量 |
| `dex_method` | 方法名（不含签名） | dex_method | 查找函数定义 |
| `dex_string` | 字符串字面量（不含类/方法/字段名） | dex_class 或 dex_field/dex_method | 硬编码文本、URL、密钥 |
| `smali` | 完整 Smali 反汇编文本 | dex_class | 指令模式、字段访问、调用 |

**搜索返回：** 每条命中包含 `{locator, matchKind, snippet, matchedText, matchTarget, matchStart, matchEnd}`。搜索结果中的 `locator` 可直接传给 `mt_apk_read`。

## 2.5 mt_apk_continue

继续 list、read 或 search 的分页结果。

| 参数 | 必填 | 说明 |
|------|------|------|
| workspaceId | 是 | |
| nextCursor | 是 | 来自 `pagination.nextCursor` |
| limit | 否 | 覆盖页大小（先检查 `pagination.limitMax`） |

当任何调用返回 `pagination.hasMore: true` 时使用。**完全照搬**返回的 `workspaceId` 和 `nextCursor`，不要解析、构造、拼接或修改游标。

## 2.6 通用返回结构

```json
{
  "ok": true/false,
  "data": { ... },
  "error": { "code": "...", "message": "...", "severity": "...", "recoverable": true/false },
  "nextActions": [ { "tool": "mt_apk_continue", "purpose": "continue", "arguments": { ... } } ]
}
```

**分页元数据：**

| 字段 | 说明 |
|------|------|
| `pagination.hasMore` | 是否有更多页 |
| `pagination.nextCursor` | 不透明游标，用于继续翻页 |
| `pagination.returnedCount` | 当前页返回数量 |
| `pagination.limitMax` | 继续翻页的最大 limit |
| `pagination.totalAvailableCount` | 已知的总数 |

始终检查 `nextActions` 数组中的继续/重试操作。

## 2.7 错误速查

| 错误 | 原因 | 修复 |
|------|------|------|
| Invalid scope | scopes 格式错误 | 单个：`scopes="x"` / 多个：`scopes=["x","y"]` / 绝不要逗号字符串或引号包裹 JSON |
| Class not found | className 非 Dalvik 格式 | 使用 `Lcom/example/Cls;` 而非 `com.example.Cls` |
| Unsupported locator | read 中 kind 错误 | 使用 `dex_class` 而非 `dex_class_outline`；确保所有必填字段存在 |
| Missing query | 使用 classPrefix 时无 query | `query` 始终必填 |
| Data truncated | 输出过大 | 增加 maxChars（最大 131072）/ 使用 startLine+limit / 使用 continue |
| Path rejected | 路径格式错误 | 仅相对路径，正斜杠，无 `.`/`..` |
| exact + smali 冲突 | matchMode=exact 且 scopes 含 smali | scopes 包含 "smali" 时使用 matchMode="contains"（默认值） |

错误码：4401 = 参数校验错误，400 = 业务逻辑错误。检查 `recoverable` 和 `retrySameArguments` 标志，部分错误包含 `argument`、`badValue`、`allowedValues`、`example` 等辅助字段。

---

# 第三部分：APK 匹配与场景

## 3.1 APK 智能匹配

**支持模糊名称匹配，无需精确输入完整文件名。**

**匹配优先级：** 精确匹配 > 包名匹配 > 应用名匹配 > 模糊匹配（子串）

| 用户输入 | 匹配方式 | 说明 |
|---------|---------|------|
| `mt://current-apk` | 当前APK | 直接使用当前打开的 APK |
| `微信` | 模糊匹配 | 匹配包含"微信"的 APK 文件名或包名 |
| `com.tencent.mm` | 包名匹配 | 匹配包名包含该字符串的 APK |
| `a.apk` | 精确匹配 | 直接使用指定文件 |
| `kwyy` | 模糊匹配 | 匹配文件名或包名包含该字符串的 APK |

**常见应用名称映射：**

| 用户可能输入 | 可能匹配的 APK/包名 |
|-------------|-------------------|
| 微信 | wechat, com.tencent.mm |
| 支付宝 | alipay, com.eg.android.AlipayGphone |
| 淘宝 | taobao, com.taobao.taobao |
| 抖音 | douyin, com.ss.android.ugc.aweme |
| 快手 | kuaishou, com.smile.gifmaker |
| QQ | qq, com.tencent.mobileqq |
| 百度 | baidu, com.baidu.searchbox |
| 京东 | jd, com.jingdong.app.mall |

**智能选择流程：**

1. 调用 `mt_apk_open` 尝试打开
2. 如果返回 `CURRENT_APK_NOT_AVAILABLE`，从 `availableApkFiles` 列表中模糊匹配用户输入
3. 匹配到多个 APK 时，列出结果让用户选择
4. 打开匹配的 APK

## 3.2 分析场景速查

**功能分析：**

| 场景 | 推荐关键词 | 推荐范围 |
|------|-----------|---------|
| 支付功能 | payment, pay, purchase, order, checkout | dex_string, dex_class |
| 会员/VIP | vip, premium, member, subscribe, subscription | dex_string, dex_class |
| 登录认证 | login, auth, token, session, credential | dex_string, smali |
| 社交分享 | share, social, wechat, weibo, facebook | dex_string |
| 推送通知 | push, notification, fcm, jpush, xiaomi | dex_string |
| 广告SDK | ad, ads, banner, interstitial, reward | dex_string, dex_class |
| 地图定位 | map, location, gps, baidu, amap | dex_string |
| 网络请求 | http, request, api, retrofit, okhttp | dex_string, smali |
| 数据存储 | database, sqlite, room, sharedpreferences | dex_string, dex_class |
| 文件操作 | file, download, upload, storage | dex_string, smali |

**安全分析：**

| 场景 | 推荐关键词 | 推荐范围 |
|------|-----------|---------|
| 敏感字符串 | password, secret, key, token, api_key | dex_string |
| 加密相关 | encrypt, decrypt, cipher, aes, rsa, md5 | dex_string, smali |
| 硬编码数据 | http://, https://, api., .com, .net | dex_string |
| 权限分析 | permission, uses-permission | axml |
| WebView | webview, javascript, loadurl | dex_string, smali |
| 组件暴露 | exported=true, intent-filter | axml |

**结构分析：**

| 场景 | 分析方法 | 说明 |
|------|---------|------|
| 入口分析 | 分析 AndroidManifest | Activity、Service、Receiver |
| 类继承 | dex_class_outline | 查看父类和接口 |
| 方法调用 | smali 搜索 | 查找 invoke 指令 |
| 资源引用 | resource_table_* | 查找资源使用 |
| 第三方库 | dex_classes + prefix | 分析第三方包结构 |

## 3.3 搜索模式速查

| 目标 | scopes | queryType | query |
|------|--------|-----------|-------|
| API URL | `dex_string` | regex | `https?://[a-zA-Z0-9._/-]+` |
| 点击事件处理 | `dex_method` | literal | `onClick` |
| 枚举类 | `dex_class` | regex | `.*Status$|.*Type$|.*Mode$` |
| 静态字段读取 | `smali` | regex | `sget-object.*->FIELD:Lcom/target/.*;` |
| 实例字段读取 | `smali` | regex | `iget-object.*->FIELD:Lcom/target/.*;` |
| 字段写入操作 | `smali` | regex | `sput-object|iput-object.*->FIELD:` |
| 接口方法调用 | `smali` | regex | `invoke-interface.*->methodName` |
| 虚方法调用 | `smali` | regex | `invoke-virtual.*->methodName` |
| 静态方法调用 | `smali` | regex | `invoke-static.*->ClassName;->methodName` |
| 构造函数调用 | `smali` | regex | `invoke-direct.*-><init>` |
| 空值检查分支 | `smali` | regex | `if-eqz.*:cond_|if-nez.*:cond_` |
| 字符串常量使用 | `smali` | regex | `const-string.*"target text"` |
| 字符串资源 | `resource_table_value` | literal | `error_message` + resourceTableTypes="string" |
| 字符串使用详情 | `dex_string` | literal | `api_key` + dexStringGranularity="member" |
| 包范围搜索 | 任意 | 任意 | 任意 + classPrefix="com/example/pkg" |

> **注意：** `query` 始终必填，即使使用了 `classPrefix`。

## 3.4 进阶搜索策略

单次搜索只能定位到"某个类包含这个字符串"，真正有价值的分析需要多次搜索串联。

**锚点扩散法** — 从已知字符串出发，逐步扩大范围：

```
1. search(scopes="dex_string", query="已知字符串")  → 找到锚点类
2. list(view="dex_class_outline", className=<锚点类>)  → 了解结构
3. search(scopes="smali", query="锚点类Dalvik名", queryType="regex")  → 扩散到引用者
4. 对扩散结果重复步骤 2-3
```

**字段追踪法** — 理解状态变量的完整生命周期：

```
1. search(scopes="dex_field", query="isVip")  → 找到所有同名字段
2. search(scopes="smali", query="iget.*isVip|sget.*isVip")  → 读取点
3. search(scopes="smali", query="iput.*isVip|sput.*isVip")  → 写入点
4. 读取写入点所在方法，理解赋值逻辑
```

**接口实现追踪法** — 找到接口的所有实现类：

```
1. search(scopes="dex_class", query="IPaymentCallback")  → 找到接口
2. list(view="dex_class_outline", className=<接口类>)  → 确认是接口
3. search(scopes="smali", query="invoke-interface.*IPaymentCallback", queryType="regex")  → 找调用点
```

**资源反向定位法** — 从 UI 资源反推代码逻辑：

```
1. search(scopes="resource_table_name", query="btn_vip_purchase")  → 找到资源 ID
2. search(scopes="smali", query="0x7f......")  → 用资源 ID 找引用类
3. list(view="dex_class_outline", className=<引用类>)  → 找事件处理方法
```

---

# 第四部分：工作流模板

### T1: APK 架构概览

```
1. open(path="mt://current-apk")
   → workspaceId + manifest 信息（包名、版本、类数量）

2. read(locator={"path":"AndroidManifest.xml","kind":"axml"})
   → activities, services, receivers, permissions, application 类

3. list(view="dex_class_outline", className=<AppClass>)
   → 初始化流程、注入的服务

4. search(query="http|https|wss", scopes="dex_string", limit=20)
   → API 域名、服务器地址

5. search(query="Retrofit|OkHttp|Glide|Room|WorkManager", scopes="dex_string", limit=10)
   → 使用的第三方框架

6. list(view="dex_classes", prefix=<mainPkg>, limit=1)
   → 主包中的类数量（检查 totalAvailableCount）
```

### T2: 定位特定功能/字段

```
1. search(query="keyword", scopes=["dex_string","smali"], limit=20)
   → 找到所有引用关键词的类

2. list(view="dex_class_outline", className=<hitClass>)
   → 理解类的角色、字段、方法

3. read(locator=hit.locator, maxChars=50000)
   → 读取完整 Smali 实现

4. search(query="fieldName", scopes="dex_field", classPrefix="relevant/pkg")
   → 在包范围内查找同名字段

5. search(query="sget.*fieldName|iget.*fieldName", scopes="smali", queryType="regex")
   → 追踪所有访问此字段的代码位置
```

### T3: 网络配置分析

```
1. search(query="DomainConstants|BaseUrl|ApiHost|ServerConfig", scopes="dex_class")
   → 找到网络配置类

2. read(locator={"className":<configClass>,"kind":"dex_class"})
   → 提取域名定义、环境配置

3. search(query="https?://[a-zA-Z0-9._-]+", scopes="dex_string", queryType="regex", limit=30)
   → 提取应用中所有 URL

4. search(query="network_security_config", scopes="zip_entries")
   → 找到网络安全配置文件

5. read(locator={"path":"res/xml/network_security_config.xml","kind":"axml"})
   → 读取安全策略（明文传输、证书固定）
```

### T4: 安全分析

```
1. read(locator={"path":"AndroidManifest.xml","kind":"axml"})
   → 所有声明的权限、保护级别

2. search(query="CAMERA|RECORD_AUDIO|READ_PHONE|ACCESS_FINE_LOCATION", scopes="axml")
   → 敏感权限使用

3. search(query="encrypt|decrypt|cipher|AES|RSA|ssl|tls", scopes="dex_string")
   → 加密操作

4. search(query="KeyStore|KeyGenerator|Cipher|Signature", scopes="dex_class")
   → 安全相关类

5. search(query="WebView|loadUrl|javascript", scopes="dex_string")
   → WebView 使用（潜在 XSS 向量）
```

### T5: 追踪方法调用链

```
1. list(view="dex_class_outline", className=<targetClass>)
   → 找到感兴趣的方法签名

2. search(query="methodName", scopes="smali", limit=30)
   → 找到所有调用此方法的 Smali 位置

3. read(locator=caller.locator, startLine=matchStart, limit=30)
   → 读取调用周围的上下文

4. search(query="invoke.*targetClass;->methodName", scopes="smali", queryType="regex")
   → 追踪特定的 invoke 模式
```

---

# 第五部分：高级技巧

## 5.1 混淆与反混淆

**ProGuard / R8 混淆特征：**

- 短类名：`a`, `b`, `c`, `aa`, `ab`（1-2 字符，常在默认包中）
- 短方法名：混淆类中的 `a()`, `b()`, `c()`
- 短字段名：`a`, `b`, `c` 配合泛型类型
- 无源文件信息：outline header 中 `source="Unknown"` 或缺失

**反混淆策略：**

| 策略 | 方法 | 工具调用 |
|------|------|----------|
| 字符串常量 | 混淆不会重命名字符串 | `search(scopes="dex_string", query="已知文本")` |
| 类层次 | 检查 super / implements 中的已知类型 | `list(view="dex_class_outline")` → 检查 header |
| 框架 API | 按 Android API 调用搜索 | `search(scopes="smali", query="Landroid/widget/TextView;->setText")` |
| 入口点 | 从 manifest 组件追踪 | `read(kind="axml")` → 找到 Activities → 追踪 |
| 资源引用 | 按 R.type.name 引用搜索 | `search(scopes="smali", query="R.string.xxx")` |

多 DEX 透明处理 — 所有搜索自动覆盖所有 DEX 文件，无需特殊操作。

## 5.2 架构模式识别

理解 APP 的架构模式能大幅缩小搜索范围——知道代码在哪一层，就不用大海捞针。

**架构特征速判：**

| 架构 | 包名特征 | 关键类后缀 | 业务逻辑所在 |
|------|---------|-----------|------------|
| MVVM | `.ui.`, `.viewmodel.`, `.repository.` | ViewModel, Repository | ViewModel 中 |
| MVP | `.presenter.`, `.view.`, `.contract.` | Presenter, Contract | Presenter 中 |
| MVC | `.controller.`, `.model.` | Controller, Model | Activity/Controller 中 |
| Clean | `.domain.`, `.data.`, `.presentation.` | UseCase, Repository (接口) | UseCase / Interactor 中 |
| MVI | `.intent.`, `.state.`, `.reducer.` | Reducer, State, Intent | Reducer 中 |

**实操建议：** 先用 `dex_classes` 的 prefix 浏览包结构，观察目录命名规律，判断架构模式后再定向搜索。比如看到 `ui/main/viewmodel/` 目录，就知道是 MVVM，VIP 逻辑大概率在 `MainViewModel` 里。

## 5.3 防御机制识别

很多 APP 内置了防篡改检测，分析时需要先识别这些机制，才能在报告中给出可行的修改方案。

**常见防御类型及识别：**

| 防御类型 | 特征关键词 | 识别方法 |
|----------|-----------|----------|
| 签名校验 | Signature, PackageInfo, signatures | 搜索 `getPackageInfo` + `GET_SIGNATURES` 组合 |
| Root 检测 | su, Superuser, Magisk | 搜索 `"/su"` `"Superuser"` `"isRooted"` |
| 模拟器检测 | emulator, goldfish, genymotion, nox | 搜索 `"goldfish"` `"nox"` `"isEmulator"` |
| 调试检测 | isDebuggerConnected, TracerPid | 搜索 `isDebuggerConnected` `TracerPid` |
| 完整性校验 | CRC, MD5, SHA, digest, checksum | 搜索 `CRC32` `MessageDigest` |
| Hook 框架检测 | Xposed, Frida, substrate, riru | 搜索 `"Xposed"` `"frida-server"` |
| 多重校验 | Timer + 校验，后台 Service 反复验证 | 搜索 `Timer` `AlarmManager` 配合校验关键词 |

**绕过思路：**

| 防御类型 | 绕过思路 | Smali 修改要点 |
|----------|---------|---------------|
| 签名校验 | 让校验方法始终返回 true | `const/4 v0, 0x1` + `return v0` |
| Root 检测 | 让检测方法返回 false | `const/4 v0, 0x0` + `return v0` |
| 模拟器检测 | 同 Root 检测 | 返回 false |
| 调试检测 | 移除检测调用或让其返回 false | nop 掉或改返回值 |
| 完整性校验 | 让校验方法返回 true 或跳过 | 修改条件跳转 |
| Hook 检测 | 让检测返回 false | 同 Root 检测 |

**注意：** 某些 APP 会把多种防御机制分散在不同类中，甚至用延时检测、多线程检测来增加绕过难度。先用 `dex_string` 全面搜索所有防御关键词，绘制出完整的防御体系图，再逐一处理。

## 5.4 大型 APK 增量分析

当 APK 包含上万类时，全量搜索既慢又容易淹没在第三方库代码中。

**三层过滤模型：**

```
第一层：包名隔离
  list(view="dex_classes", prefix="com/target/app", limit=1)
  → 确认主包类数量，后续搜索优先用 classPrefix 限定在主包

第二层：功能域定位
  search(scopes="dex_string", query="关键词", classPrefix="com/target/app")
  → 在主包范围内搜索，排除第三方库干扰

第三层：精准读取
  对命中类 outline → 定位目标方法 → 定向 read
  → 只读需要的行，避免读取整个大类
```

**第三方库快速排除：** 大型 APP 中 60-80% 的类来自第三方库，搜索时用 `classPrefix` 限定在应用主包可自动跳过。如果不确定主包名，先读 Manifest 获取 `package` 属性。

| 常见库包名前缀 | 所属框架 |
|---------------|---------|
| `com/google/` | Google 服务、Gson、Guava |
| `com/squareup/` | OkHttp、Retrofit、Picasso |
| `io/reactivex/` | RxJava |
| `kotlin/`, `kotlinx/` | Kotlin 标准库、协程 |
| `androidx/`, `com/android/` | AndroidX、Support 库 |
| `okhttp3/`, `retrofit2/` | OkHttp3、Retrofit2 |
| `com/bumptech/` | Glide |
| `org/greenrobot/` | EventBus、GreenDAO |

## 5.5 性能优化

**分析优先级（最快获得结果的路径）：**

| 优先级 | 操作 | 原因 |
|--------|------|------|
| 1st | 读取 AndroidManifest.xml | 零搜索成本，揭示整个应用结构 |
| 2nd | 搜索 dex_string 关键词 | 定位功能代码最快的方式 |
| 3rd | 列出命中类的 outline | 在读代码前理解结构 |
| 4th | 读取特定方法 | 仅在确定目标后 |
| 5th | 用 smali regex 追踪 | 最耗时 — 仅在需要时使用 |

**性能规则：**

| 规则 | 慢方式 | 快方式 |
|------|--------|--------|
| 使用前缀过滤 | 列出全部 → 本地过滤 | `list(prefix="com/target/pkg")` |
| 使用特定 scope | 搜索所有 scope | 只需字符串时用 `scopes="dex_string"` |
| 设置合理 limit | 总是用最大 limit | 20-50 浏览 / 100-200 分析 / 500-1000 导出 |
| 批量并行调用 | 顺序搜索 | 同一消息中发起独立搜索 |
| 缓存 workspaceId | 每次重新打开 APK | 打开一次，复用 workspaceId |
| 先 outline 再 read | 读取完整类（数千行） | Outline → 定位目标 → 读取特定方法 |
| 使用 classSmaliStartLine | 猜测行号 | `outline.methods[].locator` → 定向行范围读取 |

---

# 第六部分：输出规范

## 6.1 报告模板

报告文件命名：`{apkName}_{分析类型}报告.md`

```markdown
# {APK名称} - {分析类型}分析报告

## 基本信息

| 项目 | 值 |
|------|------|
| APK名称 | {apkFileName} |
| 包名 | {packageName} |
| 版本 | {versionName} ({versionCode}) |
| 最低SDK | {minSdk} |
| 目标SDK | {targetSdk} |
| 文件大小 | {文件大小} |

## 分析配置

| 配置项 | 值 |
|--------|-----|
| 分析类型 | {分析类型} |
| 搜索关键词 | {关键词列表} |
| 搜索范围 | {scopes列表} |
| 分析时间 | {时间戳} |

## 分析结果

### 1. {结果分类1}

{详细内容，包含代码片段、定位器信息等}

### 2. {结果分类2}

{详细内容}

...

## 详细代码

{可选：关键代码片段}

## 总结

{分析总结和建议}

---

*报告生成时间: {时间}*
*分析工具: mt_mcp APK Analyzer*
```

## 6.2 报告撰写心法

一份好的分析报告不只是罗列搜索结果，而是让读者看完就知道"该改哪里、怎么改"。

**报告质量分级：**

| 等级 | 特征 | 读者体验 |
|------|------|---------|
| 初级 | 粘贴搜索结果原文 | "看到了，但不知道怎么办" |
| 中级 | 按功能模块分类整理，标注关键类/方法 | "知道在哪，但不确定怎么改" |
| 高级 | 每个发现都附带定位器、修改方案、验证方法 | "直接可以动手改" |

**每个分析发现的必备要素：**

1. **定位信息** — 类名（Dalvik 格式）、方法签名、关键字符串
2. **代码上下文** — 关键 Smali 代码片段，包含条件判断和跳转逻辑
3. **影响分析** — 这个发现意味着什么，修改后会有什么效果
4. **修改方案** — 具体改哪一行、改成什么，给出修改前后的 Smali 对比
5. **风险评估** — 修改是否可能影响其他功能、是否需要同时处理防御机制

**代码片段呈现规范：** 用 `← [修改点]` 标注需要修改的行：

```
.method public isVip()Z
    ...
    if-eqz v0, :cond_0        ← [修改点] 改为 if-nez 跳过 VIP 检查
    ...
    return v0
.end method
```

## 6.3 解决方案建议

**重要原则：分析报告必须包含解决方案建议部分，指导用户如何处理分析结果。**

**工具选择原则：**

| 场景 | 推荐工具 | 说明 |
|------|---------|------|
| 常规修改 | MT管理器 | 首选，手机端直接操作 |
| 字符串修改 | MT管理器 | 字符串常量替换 |
| 跳过逻辑 | MT管理器 | 方法修改、条件跳转 |
| 资源修改 | MT管理器 | XML、图片等资源 |
| 复杂重构 | 电脑端工具 | 需要重新编译的情况 |
| 大规模修改 | 电脑端工具 | 批量处理、自动化 |

**MT管理器操作指南：**

| 操作 | MT管理器方法 |
|------|-------------|
| 打开APK | 长按APK → 查看详情 → 打开方式 → APK查看 |
| 查看DEX | APK查看 → classes.dex → Dex编辑器++ |
| 搜索字符串 | Dex编辑器++ → 搜索 → 字符串搜索 |
| 搜索类名 | Dex编辑器++ → 搜索 → 类名搜索 |
| 搜索方法 | Dex编辑器++ → 搜索 → 方法搜索 |

**常见修改模式：**

| 需求 | Smali修改方法 |
|------|--------------|
| 跳过VIP验证 | if-eq → if-ne 或 return-void |
| 解锁功能 | const/4 v0, 0x0 → const/4 v0, 0x1 |
| 移除广告 | return-void 提前返回 |
| 绕过登录 | 修改登录状态检查 |
| 禁用更新 | 修改版本比较逻辑 |

**字符串修改：** 打开 APK → Dex编辑器++ → 字符串搜索 → 转到定义 → 长按编辑 → 保存并退出 → 自动签名

**方法修改：** 打开 APK → Dex编辑器++ → 搜索方法 → 查看 Smali → 修改条件跳转（if-eq→if-ne 或 goto）→ 保存并退出 → 自动签名

**XML修改：** 打开 APK → 查看详情 → 找到目标 XML → 长按编辑 → 修改 → 保存 → 自动重新打包签名

**资源修改：** 打开 APK → 查看详情 → res/ 目录 → 替换/编辑资源 → 返回 → 自动重新打包签名

**电脑端工具推荐：**

| 工具 | 用途 | 说明 |
|------|------|------|
| jadx | 反编译查看 | 查看Java源码，理解逻辑 |
| apktool | 完整反编译 | 反编译Smali和资源 |
| Android Studio | 调试分析 | 动态调试、内存分析 |
| Frida | 动态Hook | 运行时修改、绕过检测 |
| Xposed | 框架Hook | 系统级Hook |
| np管理器 | 类似MT | 另一个手机端选择 |

电脑端工作流：jadx 理解逻辑 → apktool 反编译 → 修改 Smali/资源 → apktool 重打包 → 签名 → 安装测试

**解决方案模板：**

```markdown
## 解决方案建议

### 推荐工具

本分析结果推荐使用 **MT管理器** 进行修改。

### 修改步骤

1. **定位目标**
   - 类名: `com.example.xxx`
   - 方法名: `methodName`
   - 关键字符串: `xxx`

2. **MT管理器操作**
   - 打开APK → Dex编辑器++
   - 搜索 [类名/方法名/字符串]
   - 进入目标位置

3. **具体修改**
   - 找到指令: `if-eq v0, v1, :cond_x`
   - 修改为: `if-ne v0, v1, :cond_x`
   - 保存并退出

4. **验证**
   - 安装修改后的APK
   - 测试功能是否正常

### 注意事项

- 修改后需要重新签名
- 部分应用有签名校验，需要额外处理
- 建议备份原APK

### 备选方案

如果MT管理器无法处理，推荐使用:
- jadx + apktool（电脑端）
- Frida（动态Hook）
```

**特殊情况处理：**

| 情况 | MT管理器方案 | 电脑端方案 |
|------|-------------|-----------|
| 签名校验 | 搜索签名验证代码并移除 | Lucky Patcher / 核心破解 |
| 加壳应用 | 无法处理 | 脱壳工具（BlackDex等） |
| 混淆代码 | 需要分析映射关系 | jadx + proguard mapping |
| Native库 | 无法直接修改 | IDA Pro / Ghidra |
| 资源加密 | 需要解密脚本 | Python脚本处理 |

---

# 附录

## A. Dalvik 描述符参考

**类型映射：**

| Java | Dalvik | | Java | Dalvik |
|------|--------|-|------|--------|
| void | V | | boolean | Z |
| int | I | | long | J |
| float | F | | double | D |
| byte | B | | char | C |
| short | S | | Object | Ljava/lang/Object; |
| String | Ljava/lang/String; | | int[] | [I |
| String[] | [Ljava/lang/String; | | MyClass | Lcom/example/MyClass; |
| MyClass[] | [Lcom/example/MyClass; | | List<T> | Ljava/util/List; (擦除) |

**方法签名：** `methodName(ParamTypes)ReturnType`

```
void onCreate(Bundle)           →  onCreate(Landroid/os/Bundle;)V
boolean isVip()                 →  isVip()Z
String getName(int)             →  getName(I)Ljava/lang/String;
void set(int, Object)           →  set(ILjava/lang/Object;)V
int[] getIds()                  →  getIds()[I
```

**字段签名：** `fieldName:Type`

```
VipStatus Activated             →  Activated:Lcom/example/VipStatus;
int count                       →  count:I
boolean isVip                   →  isVip:Z
String name                     →  name:Ljava/lang/String;
```

## B. Smali 速查

**字段访问：**

```
sget-object v0, LCls;->FIELD:LType;      # 读取静态字段
sput-object v0, LCls;->FIELD:LType;      # 写入静态字段
iget-object v0, v1, LCls;->FIELD:LType;  # 读取实例字段 (v1=对象引用)
iput-object v0, v1, LCls;->FIELD:LType;  # 写入实例字段 (v1=对象引用)

原始类型变体: sget/sput/iget/iput (int), -wide (long/double), -boolean
```

**方法调用：**

```
invoke-virtual   {v0,v1}, LCls;->method(I)V       # 实例方法
invoke-static    {v0},    LCls;->staticMethod(I)I  # 静态方法
invoke-interface {v0},    LIfc;->method()V          # 接口方法
invoke-direct    {v0,v1}, LCls;-><init>(I)V         # 构造函数 / private
invoke-super     {v0,v1}, LCls;->method(I)V         # 父类调用

调用后: move-result v0 (原始类型) / move-result-object v0 (对象类型)
```

**控制流与常量：**

```
if-eqz v0, :label           # if v0 == null → 跳转到 label
if-nez v0, :label           # if v0 != null → 跳转到 label
instance-of v0, v1, LType;  # v0 = (v1 instanceof Type)
check-cast v0, LType;       # 将 v0 转型为 Type
return v0                   # 返回 int/boolean 等
return-object v0            # 返回对象引用
return-void                 # 返回 void
const/4 v0, 0x1             # 小整数常量 (0-7)
const/16 v0, 0x100          # 中等整数常量
const-string v0, "text"     # 字符串常量
aget-object v0, v1, v2      # v0 = v1[v2] (数组读取)
aput-object v0, v1, v2      # v1[v2] = v0 (数组写入)
```

## C. 注意事项

1. **APK 选择**: 支持模糊名称匹配，无需精确输入完整文件名
2. **多匹配处理**: 如果匹配到多个 APK，列出结果让用户选择
3. **大型 APK**: 使用分页处理（limit + nextCursor）
4. **性能优化**: 使用 classPrefix、zipEntryPrefix 缩小范围
5. **敏感信息**: 在报告中适当脱敏
6. **资源释放**: 分析完成后可关闭工作区
7. **报告保存**: 保存在当前工作目录
8. **MCP 服务器地址**: 以实际连接成功的 MCP 配置为准，`http://127.0.0.1:8787/mcp` 仅为示例
""");
            skills.put(skill);
            return skills.toString();
        } catch (Exception e) {
            android.util.Log.e("AIHelper", "创建默认MCP技能失败", e);
            return "[]";
        }
    }

    /**
     * 保存 MCP Skill 列表
     */
    public static void setMcpSkills(@NonNull PluginContext context, @NonNull String skillsJson) {
        context.getPreferences().edit().putString(PREF_MCP_SKILLS, skillsJson).apply();
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
                .remove(PREF_MCP_ENABLED)
                .remove(PREF_MCP_SERVER_URL)
                .remove(PREF_MCP_SKILLS)
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
        // 不设 temperature 等非必需参数，以确保最大兼容性

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
    public static String cleanThinkingTags(String content) {
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

    // ============================================================
    // 流式响应解析器 - 统一多模型兼容层
    // ============================================================

    /**
     * 流式响应解析结果
     */
    public static class StreamChunkResult {
        /** 正式回答内容 */
        @NonNull
        public final String content;
        /** 思考过程内容 */
        @NonNull
        public final String reasoning;

        public StreamChunkResult(@Nullable String content, @Nullable String reasoning) {
            this.content = content != null ? content : "";
            this.reasoning = reasoning != null ? reasoning : "";
        }

        public boolean hasContent() {
            return !content.isEmpty();
        }

        public boolean hasReasoning() {
            return !reasoning.isEmpty();
        }
    }

    /**
     * 流式响应解析器
     * 统一处理所有主流 AI 模型的 SSE chunk 格式
     */
    public static class StreamChunkParser {

        /**
         * 从 SSE chunk 的 choices[0] 中提取纯文本内容
         * 按优先级尝试所有主流模型格式，返回第一个非空内容
         *
         * @param firstChoice SSE chunk 中的 choices[0] JSONObject
         * @return 提取的文本内容，无内容时返回空字符串
         */
        @NonNull
        public static String extractContent(@NonNull JSONObject firstChoice) {
            String content = null;

            // 1. OpenAI 兼容格式：delta.content
            JSONObject delta = firstChoice.optJSONObject("delta");
            if (delta != null) {
                content = delta.optString("content", "");
                if (content != null && !content.isEmpty() && !"null".equals(content)) {
                    return content;
                }
            }

            // 2. 百度文心一言格式：result
            content = firstChoice.optString("result", "");
            if (!content.isEmpty() && !"null".equals(content)) {
                return content;
            }

            // 3. 讯飞星火格式：text[0].content
            JSONArray textArray = firstChoice.optJSONArray("text");
            if (textArray != null && textArray.length() > 0) {
                JSONObject textObj = textArray.optJSONObject(0);
                if (textObj != null) {
                    content = textObj.optString("content", "");
                    if (!content.isEmpty() && !"null".equals(content)) {
                        return content;
                    }
                }
            }

            // 4. Claude 格式：content[] 数组（type:text）
            JSONArray contentArray = firstChoice.optJSONArray("content");
            if (contentArray != null && contentArray.length() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < contentArray.length(); i++) {
                    JSONObject block = contentArray.optJSONObject(i);
                    if (block != null && "text".equals(block.optString("type", ""))) {
                        sb.append(block.optString("text", ""));
                    }
                }
                if (sb.length() > 0) {
                    return sb.toString();
                }
            }

            // 5. Gemini 格式：parts[] 数组（text）
            JSONArray partsArray = firstChoice.optJSONArray("parts");
            if (partsArray != null && partsArray.length() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < partsArray.length(); i++) {
                    JSONObject part = partsArray.optJSONObject(i);
                    if (part != null && part.has("text")) {
                        sb.append(part.optString("text", ""));
                    }
                }
                if (sb.length() > 0) {
                    return sb.toString();
                }
            }

            // 6. message.content 变体
            JSONObject message = firstChoice.optJSONObject("message");
            if (message != null) {
                content = message.optString("content", "");
                if (!content.isEmpty() && !"null".equals(content)) {
                    return content;
                }
            }

            // 7. 简单 text 字段
            content = firstChoice.optString("text", "");
            if (!content.isEmpty() && !"null".equals(content)) {
                return content;
            }

            // 8. 直接 content 字段
            content = firstChoice.optString("content", "");
            if (!content.isEmpty() && !"null".equals(content)) {
                return content;
            }

            return "";
        }

        /**
         * 从 SSE chunk 的 choices[0] 中提取完整解析结果（含思考过程）
         *
         * @param firstChoice SSE chunk 中的 choices[0] JSONObject
         * @return StreamChunkResult，包含 content 和 reasoning
         */
        @NonNull
        public static StreamChunkResult parse(@NonNull JSONObject firstChoice) {
            String content = "";
            String reasoning = "";

            // 解析思考过程
            JSONObject delta = firstChoice.optJSONObject("delta");
            if (delta != null) {
                reasoning = delta.optString("reasoning_content", "");
                if (reasoning.isEmpty() || "null".equals(reasoning)) {
                    reasoning = delta.optString("thinking", "");
                }
            }

            if (reasoning.isEmpty() || "null".equals(reasoning)) {
                reasoning = firstChoice.optString("thinking", "");
            }

            // 解析正式内容
            content = extractContent(firstChoice);

            return new StreamChunkResult(content, reasoning);
        }
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
        // 不设 temperature 等非必需参数，以确保最大兼容性

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

