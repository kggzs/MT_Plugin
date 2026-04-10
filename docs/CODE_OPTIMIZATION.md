# MTKang Plugin 代码优化文档

> **项目名称**: MT 管理器 V3 AI 智能编程助手插件  
> **文档版本**: v1.0  
> **生成日期**: 2026-04-10  
> **技术栈**: Java 17 | Min SDK 21 | Gradle 8.13

---

## 目录

- [一、优化优先级总览](#一优化优先级总览)
- [二、严重问题（立即处理）](#二严重问题立即处理)
- [三、中等优先级问题（短期改进）](#三中等优先级问题短期改进)
- [四、低优先级问题（长期优化）](#四低优先级问题长期优化)
- [五、架构优化建议](#五架构优化建议)
- [六、代码规范检查清单](#六代码规范检查清单)

---

## 一、优化优先级总览

| 优先级 | 问题数量 | 影响范围 | 预计工作量 |
|--------|----------|----------|------------|
| 🔴 严重 | 2 | 安全性、可维护性 | 2-3 小时 |
| 🟡 中等 | 7 | 代码质量、国际化 | 4-6 小时 |
| 🟢 低 | 5 | 代码规范、健壮性 | 2-3 小时 |

---

## 二、严重问题（立即处理）

### 2.1 API 密钥硬编码泄露

**文件**: `src/main/java/com/example/myplugin/util/AIHelper.java:29`

```java
// ❌ 当前代码 - 密钥明文暴露在源码中
private static final String DEFAULT_API_KEY = "sk-K1m4b0U2WoorIub7EhbQTIYRFpQhURRXMdIoZBywCEruujOa";
```

**风险**:
- 源码反编译后密钥直接可见
- 如果项目开源或分享，密钥将被泄露
- 恶意用户可能盗用 API 额度

**优化方案**:

#### 方案 A: 使用 `local.properties`（推荐开发环境）

```gradle
// build.gradle
def apiKey = ""
if (project.rootProject.file('local.properties').exists()) {
    Properties properties = new Properties()
    properties.load(project.rootProject.file('local.properties').newDataInputStream())
    apiKey = properties.getProperty('API_KEY', '')
}

android {
    defaultConfig {
        buildConfigField "String", "API_KEY", "\"${apiKey}\""
    }
}
```

```java
// AIHelper.java
import com.example.myplugin.BuildConfig;

private static final String DEFAULT_API_KEY = BuildConfig.API_KEY;
```

#### 方案 B: 使用用户配置（推荐生产环境）

```java
// 从 MyPreference 读取用户配置的 API Key
public static String getApiKey() {
    String userKey = MyPreference.getApiKey();
    return userKey != null && !userKey.isEmpty() ? userKey : BuildConfig.API_KEY;
}
```

**执行步骤**:
1. 在 `.gitignore` 中添加 `local.properties`
2. 选择上述方案之一实现
3. 从代码中删除硬编码的密钥
4. 更新 README 说明配置方法

---

### 2.2 重复代码 - AI 分析菜单类

**涉及文件**:
- `AICodeAnalysisToolMenu.java` (177 行)
- `AICodeAnalysisFloatingMenu.java` (184 行)

**重复内容**:
| 方法/字段 | 重复度 | 说明 |
|-----------|--------|------|
| `showPromptInputDialog()` | 95% | 仅默认提示词和行数限制不同 |
| `startAnalysis()` | 100% | 完全相同 |
| `showResultDialog()` | 100% | 完全相同 |
| `showEmptyResultDialog()` | 100% | 完全相同 |
| `volatile boolean isCancelled` | 100% | 完全相同 |

**优化方案: 抽取基类**

```java
// 新建: BaseAICodeAnalysisMenu.java
public abstract class BaseAICodeAnalysisMenu {
    
    protected volatile boolean isCancelled = false;
    
    protected abstract String getDefaultPrompt();
    protected abstract int getMinLines();
    protected abstract int getMaxLines();
    protected abstract PluginUI getPluginUI();
    
    protected void showPromptInputDialog() {
        String currentPrompt = MyPreference.getPrompt();
        PluginUI ui = getPluginUI();
        
        DialogBuilder dialog = new DialogBuilder(ui.getContext())
            .setTitle("{set_analysis_prompt}")
            .addEditText("prompt")
                .hint(getDefaultPrompt())
                .text(currentPrompt)
                .minLines(getMinLines())
                .maxLines(getMaxLines())
            .addEditText("userPrompt")
                .hint("请输入补充提示词（将作为系统提示词的前缀）：")
                .minLines(5)
                .maxLines(8);
        
        dialog.setPositiveButton("{start_analysis}", (d, which) -> {
            String prompt = dialog.getText("prompt");
            String userPrompt = dialog.getText("userPrompt");
            MyPreference.setPrompt(prompt);
            startAnalysis(prompt, userPrompt, ui);
        });
        
        dialog.setNegativeButton("{cancel}", (d, which) -> {
            isCancelled = true;
            d.dismiss();
        });
        
        dialog.show();
    }
    
    protected void startAnalysis(String prompt, String userPrompt, PluginUI ui) {
        // 完整的分析逻辑（从任一子类迁移）
    }
    
    protected void showResultDialog(String result, PluginUI ui) {
        // 结果展示逻辑
    }
    
    protected void showEmptyResultDialog(PluginUI ui) {
        // 空结果提示逻辑
    }
}
```

```java
// 重构后: AICodeAnalysisToolMenu.java
public class AICodeAnalysisToolMenu extends TextEditorToolMenu {
    
    @Override
    public void init() {
        getPluginUI().addMenuItem("{ai_code_analysis}").onClick(v -> {
            showPromptInputDialog();
        });
    }
    
    @Override
    protected String getDefaultPrompt() {
        return "你是一个代码分析助手..."; // 完整提示词
    }
    
    @Override
    protected int getMinLines() { return 10; }
    
    @Override
    protected int getMaxLines() { return 15; }
    
    @Override
    protected PluginUI getPluginUI() {
        return pluginUI;
    }
}
```

```java
// 重构后: AICodeAnalysisFloatingMenu.java
public class AICodeAnalysisFloatingMenu extends TextEditorFloatingMenu {
    
    @Override
    public void init() {
        pluginUI.addMenuItem("{ai_quick_analysis}").onClick(v -> {
            showPromptInputDialog();
        });
    }
    
    @Override
    protected String getDefaultPrompt() {
        return "分析以下代码..."; // 简短提示词
    }
    
    @Override
    protected int getMinLines() { return 3; }
    
    @Override
    protected int getMaxLines() { return 5; }
    
    @Override
    protected PluginUI getPluginUI() {
        return pluginUI;
    }
}
```

**收益**: 减少约 200 行重复代码，后续维护只需修改基类。

---

## 三、中等优先级问题（短期改进）

### 3.1 硬编码字符串未国际化

**影响文件**:
- `AICodeAnalysisToolMenu.java`
- `AICodeAnalysisFloatingMenu.java`
- `MyPreference.java`
- `EncodeDecodeMenu.java`

**未国际化的字符串清单**:

| 字符串 | 出现位置 | 建议键名 |
|--------|----------|----------|
| "AI代码分析" | AICodeAnalysisToolMenu.java | `ai_code_analysis` |
| "AI快速分析" | AICodeAnalysisFloatingMenu.java | `ai_quick_analysis` |
| "请输入补充提示词..." | 两个 AI 菜单类 | `supplementary_prompt_hint` |
| "设置分析提示词" | 两个 AI 菜单类 | `set_analysis_prompt` |
| "开始分析" | 两个 AI 菜单类 | `start_analysis` |
| "正在初始化..." | 两个 AI 菜单类 | `initializing` |
| "已取消分析" | 两个 AI 菜单类 | `analysis_cancelled` |
| "分析失败: " | 两个 AI 菜单类 | `analysis_failed` |
| "分析结果为空" | 两个 AI 菜单类 | `empty_analysis_result` |
| "已复制到剪贴板" | 两个 AI 菜单类 | `copied_to_clipboard` |
| "已重置为默认配置" | MyPreference.java | `config_reset_success` |
| "URL 必须以 http:// 或 https:// 开头" | MyPreference.java | `url_format_error` |

**优化步骤**:

1. 在 `strings.mtl` 中添加缺失的键值对:

```
# AI 分析相关
ai_code_analysis=AI代码分析
ai_quick_analysis=AI快速分析
supplementary_prompt_hint=请输入补充提示词（将作为系统提示词的前缀）：
set_analysis_prompt=设置分析提示词
start_analysis=开始分析
initializing=正在初始化...
analysis_cancelled=已取消分析
analysis_failed=分析失败: 
empty_analysis_result=分析结果为空
copied_to_clipboard=已复制到剪贴板
error_copied=错误信息已复制到剪贴板
content_truncated=[内容已截断...]
```

2. 替换代码中的硬编码字符串:

```java
// ❌ 之前
Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show();

// ✅ 之后
Toast.makeText(context, context.getString("{copied_to_clipboard}"), Toast.LENGTH_SHORT).show();
```

---

### 3.2 魔法数字替换为常量

**文件**: `AIHelper.java`, `AICodeAnalysisToolMenu.java`, `EncodeDecodeMenu.java`

```java
// 新建或在 AIHelper.java 中添加常量定义
public class AIHelper {
    
    // API 请求配置
    private static final int MAX_TOKENS = 2000;
    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int READ_TIMEOUT_MS = 60000;
    
    // 错误消息配置
    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;
    
    // 文件信息配置
    private static final int MAX_PREVIEW_FILES = 3;
    private static final int MAX_PREVIEW_LINES = 100;
    
    // ... 其他代码
    
    // 使用常量替换魔法数字
    requestBody.put("max_tokens", MAX_TOKENS);
    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    connection.setReadTimeout(READ_TIMEOUT_MS);
}
```

```java
// AICodeAnalysisToolMenu.java / AICodeAnalysisFloatingMenu.java
private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;
private static final String CONTENT_TRUNCATED_MARK = "[内容已截断...]";

// 使用
if (displayMsg.length() > MAX_ERROR_MESSAGE_LENGTH) {
    displayMsg = displayMsg.substring(0, MAX_ERROR_MESSAGE_LENGTH) + CONTENT_TRUNCATED_MARK;
}
```

```java
// EncodeDecodeMenu.java
private static final int MS_TO_S_DIVISOR = 1000;
private static final int HEX_MASK = 0xFF;

// 使用
long seconds = timestamp / MS_TO_S_DIVISOR;
long timestamp = seconds * MS_TO_S_DIVISOR;
sb.append(String.format("%02X", b & HEX_MASK));
```

---

### 3.3 Handler 重复创建优化

**文件**: `AIHelper.java:339`

```java
// ❌ 当前代码 - 每次调用创建新 Handler
public static void runOnMainThread(@NonNull Runnable action) {
    new Handler(Looper.getMainLooper()).post(action);
}
```

```java
// ✅ 优化后 - 使用静态常量缓存 Handler
public class AIHelper {
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    
    public static void runOnMainThread(@NonNull Runnable action) {
        MAIN_HANDLER.post(action);
    }
}
```

**收益**: 避免频繁创建 Handler 对象，减少 GC 压力。

---

### 3.4 线程池替代直接创建线程

**文件**: `AICodeAnalysisToolMenu.java:104`, `AICodeAnalysisFloatingMenu.java:111`

```java
// ❌ 当前代码 - 直接创建新线程
new Thread(() -> {
    // 分析逻辑
}).start();
```

```java
// ✅ 优化后 - 使用线程池
public class BaseAICodeAnalysisMenu {
    
    // 单线程池，确保同时只有一个分析任务
    private static final ExecutorService ANALYSIS_EXECUTOR = 
        Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("ai-analysis-thread");
            thread.setDaemon(true);
            return thread;
        });
    
    protected void startAnalysis(String prompt, String userPrompt, PluginUI ui) {
        isCancelled = false;
        
        ANALYSIS_EXECUTOR.submit(() -> {
            try {
                // 显示初始化提示
                AIHelper.runOnMainThread(() -> {
                    Toast.makeText(ui.getContext(), 
                        ui.getContext().getString("{initializing}"), 
                        Toast.LENGTH_SHORT).show();
                });
                
                String result = AIHelper.callOpenAI(prompt, null);
                
                if (isCancelled) {
                    AIHelper.runOnMainThread(() -> {
                        Toast.makeText(ui.getContext(), 
                            ui.getContext().getString("{analysis_cancelled}"), 
                            Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                if (result != null && !result.isEmpty()) {
                    AIHelper.runOnMainThread(() -> showResultDialog(result, ui));
                } else {
                    AIHelper.runOnMainThread(() -> showEmptyResultDialog(ui));
                }
            } catch (Exception e) {
                AIHelper.runOnMainThread(() -> showErrorDialog(e, ui));
            }
        });
    }
}
```

**收益**:
- 控制并发数量，避免资源竞争
- 减少线程创建开销
- 更好的线程生命周期管理

---

### 3.5 未使用的导入清理

**文件**: `QuickInsertFunction.java`

```java
// ❌ 删除以下未使用的导入
import bin.mt.plugin.api.editor.TextEditorFloatingMenu;
import java.util.UUID;
```

**执行**: 运行 IDE 的 "Optimize Imports" 功能，或手动删除。

---

### 3.6 EncodeDecodeMenu 过长方法拆分

**文件**: `EncodeDecodeMenu.java` (550 行)

```java
// ❌ 当前: showEncodeDecodeUI() 方法过长（约 60 行）

// ✅ 优化后: 拆分为多个方法
public class EncodeDecodeMenu {
    
    @Override
    public void init() {
        showEncodeDecodeUI();
    }
    
    private void showEncodeDecodeUI() {
        pluginUI
            .addTitle("{encode_decode_title}")
            .addDivider()
            .addEncodeSection()      // 抽取编码部分
            .addDivider()
            .addDecodeSection()      // 抽取解码部分
            .addDivider()
            .addTimestampSection()   // 抽取时间戳部分
            .show();
    }
    
    private PluginUI addEncodeSection() {
        return pluginUI
            .addTitle("{encode}")
            .addRadioButton("encodingType", "url", "URL编码")
            .addRadioButton("encodingType", "base64", "Base64编码")
            // ... 其他编码选项
            .addButton("{execute}", this::handleEncode);
    }
    
    private PluginUI addDecodeSection() {
        // 类似编码部分的结构
    }
    
    private PluginUI addTimestampSection() {
        // 时间戳转换部分
    }
}
```

---

## 四、低优先级问题（长期优化）

### 4.1 使用 try-with-resources 确保资源关闭

**文件**: `AIHelper.java`

```java
// ❌ 当前代码 - 异常时可能未关闭资源
BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
StringBuilder response = new StringBuilder();
String line;
while ((line = reader.readLine()) != null) {
    response.append(line);
}
reader.close();
connection.disconnect();
```

```java
// ✅ 优化后 - 使用 try-with-resources
try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        response.append(line);
    }
    return response.toString();
} finally {
    connection.disconnect();
}
```

---

### 4.2 方法命名规范化

**文件**: `EncodeDecodeMenu.java:227`

```java
// ❌ 当前代码 - 命名不规范且参数未使用
private String copy_success(PluginUI pluginUI, String hash) {
    return pluginUI.getContext().getString("{copy_success}");
}
```

```java
// ✅ 优化后
private String getCopySuccessMessage(PluginUI pluginUI) {
    return pluginUI.getContext().getString("{copy_success}");
}
```

**调用处同步修改**:
```java
// 之前
showToast(copy_success(pluginUI, hash));

// 之后
showToast(getCopySuccessMessage(pluginUI));
```

---

### 4.3 启用 Lint 检查

**文件**: `build.gradle`

```gradle
// ❌ 当前代码 - 完全禁用 Lint
tasks.configureEach { task ->
    if (task.name.contains("lint")) {
        task.enabled = false
    }
}
```

```gradle
// ✅ 优化后 - 仅禁用特定规则
android {
    lintOptions {
        disable 'ExpiredTargetSdkVersion'  // 仅禁用目标 SDK 版本警告
        abortOnError false                  // 错误不中断构建
        warningsAsErrors false              // 警告不视为错误
    }
}
```

---

### 4.4 输入验证增强

**文件**: `EncodeDecodeMenu.java`

```java
// ✅ 为解码方法添加输入验证
private void handleHexDecode(PluginUI pluginUI, String input) {
    if (input == null || input.isEmpty()) {
        showToast(pluginUI, "{input_empty}");
        return;
    }
    
    // 验证十六进制格式
    if (!input.matches("^[0-9a-fA-F\\s]+$")) {
        showToast(pluginUI, "{hex_format_error}");
        return;
    }
    
    // 执行解码逻辑
    try {
        // ... 解码代码
    } catch (Exception e) {
        showToast(pluginUI, "{decode_failed}: " + e.getMessage());
    }
}
```

**在 `strings.mtl` 中添加**:
```
input_empty=输入不能为空
hex_format_error=无效的十六进制格式（仅允许 0-9, a-f, A-F 和空格）
decode_failed=解码失败
```

---

### 4.5 升级 targetSdkVersion

**当前配置**: `targetSdk = 28` (Android 9.0)

**建议**: 升级到 `targetSdk = 33` 或更高

**注意事项**:
1. 需要适配 Android 10+ 的存储权限变更
2. 需要适配 Android 12+ 的通知权限
3. 可能需要更新部分 API 的调用方式
4. 充分测试所有功能

**修改位置**:
```gradle
// build.gradle
android {
    defaultConfig {
        targetSdk = 33  // 从 28 升级
    }
}
```

移除 `@SuppressWarnings("ExpiredTargetSdkVersion")` 注释。

---

## 五、架构优化建议

### 5.1 项目目录重组

```
src/main/java/com/example/myplugin/
├── ai/                          # AI 相关功能
│   ├── BaseAICodeAnalysisMenu.java    # AI 分析基类（新增）
│   ├── AICodeAnalysisToolMenu.java    # 工具菜单入口
│   ├── AICodeAnalysisFloatingMenu.java # 浮动菜单入口
│   └── AIHelper.java                  # AI 工具类（从 util 迁移）
├── codec/                       # 编解码功能
│   └── EncodeDecodeMenu.java
├── config/                      # 配置管理
│   └── MyPreference.java
├── util/                        # 通用工具
│   └── QuickInsertFunction.java
└── MyPlugin.java                # 插件主入口
```

### 5.2 设计模式应用

#### 策略模式 - 编解码器

```java
// 新建编解码器接口
public interface Codec {
    String encode(String input) throws Exception;
    String decode(String input) throws Exception;
}

// 实现具体编解码器
public class Base64Codec implements Codec {
    @Override
    public String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public String decode(String input) {
        return new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_8);
    }
}

// 使用映射表替代大量 if-else
private static final Map<String, Codec> CODEC_MAP = new HashMap<>();
static {
    CODEC_MAP.put("base64", new Base64Codec());
    CODEC_MAP.put("url", new UrlCodec());
    CODEC_MAP.put("unicode", new UnicodeCodec());
    // ... 其他编解码器
}
```

---

## 六、代码规范检查清单

在提交代码前，请检查以下事项：

### 6.1 命名规范
- [ ] 类名使用 PascalCase（如 `AICodeAnalysisToolMenu`）
- [ ] 方法名和变量名使用 camelCase（如 `startAnalysis`）
- [ ] 常量使用 UPPER_SNAKE_CASE（如 `MAX_TOKENS`）
- [ ] 不使用下划线命名（如 `copy_success` ❌）

### 6.2 代码质量
- [ ] 无硬编码的敏感信息（API 密钥、密码等）
- [ ] 无魔法数字（使用有意义的常量）
- [ ] 无重复代码（提取公共方法或基类）
- [ ] 方法长度不超过 50 行
- [ ] 类长度不超过 500 行

### 6.3 国际化
- [ ] 所有用户可见字符串使用 `{key}` 格式
- [ ] `strings.mtl` 和 `strings-en.mtl` 键值对完整
- [ ] 无硬编码的中文/英文字符串

### 6.4 资源管理
- [ ] 使用 try-with-resources 管理可关闭资源
- [ ] 无未使用的导入
- [ ] Handler 使用静态常量缓存

### 6.5 并发
- [ ] 使用线程池而非直接 `new Thread()`
- [ ] 正确处理线程中断
- [ ] 主线程操作使用 `runOnMainThread()`

### 6.6 构建配置
- [ ] Lint 检查部分启用（非完全禁用）
- [ ] targetSdkVersion 尽可能较新
- [ ] 混淆规则正确配置

---

## 附录：优化工作量估算

| 优化项 | 预计工时 | 优先级 |
|--------|----------|--------|
| API 密钥安全化 | 1 小时 | 🔴 严重 |
| 抽取 AI 分析基类 | 2 小时 | 🔴 严重 |
| 字符串国际化 | 2 小时 | 🟡 中等 |
| 魔法数字常量替换 | 1 小时 | 🟡 中等 |
| Handler 优化 | 0.5 小时 | 🟡 中等 |
| 线程池改造 | 1.5 小时 | 🟡 中等 |
| 未使用导入清理 | 0.5 小时 | 🟡 中等 |
| 方法拆分重构 | 1 小时 | 🟡 中等 |
| try-with-resources | 1 小时 | 🟢 低 |
| 命名规范化 | 0.5 小时 | 🟢 低 |
| Lint 启用 | 1 小时 | 🟢 低 |
| 输入验证增强 | 1 小时 | 🟢 低 |
| **总计** | **约 12 小时** | |

---

**文档结束**

> 💡 **建议实施顺序**: 按优先级从高到低逐步优化，每次优化后运行构建和测试确保功能正常。
