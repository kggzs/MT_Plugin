# MT 管理器 V3 插件开发完整指南

# MT 管理器 V3 插件开发完整文档

## 一、插件概述

### 1. 插件功能范围

MT 管理器的插件系统允许开发者扩展核心功能，主要支持三大类能力：

- **翻译引擎**：实现自定义翻译服务（含批量翻译），对接在线 API 或本地引擎，支持文本转换类工具（如大小写、繁简转换）。

- **文本编辑器扩展**：为文本编辑器添加快捷功能、浮动菜单、工具菜单，支持文本处理、加密解密、格式转换等场景。

- **设置界面**：创建插件配置界面，支持开关、输入框、单选列表等组件，实现参数配置与信息展示。

### 2. 开发基础要求

- **开发语言**：支持 Java 11+ 或 Kotlin（推荐最新稳定版）。

- **依赖特性**：支持 Java 8+ API（Stream、Optional 等）、Lambda 表达式、Try-with-resources。

- **版本要求**：

    - MT 管理器最低版本：2.19.5+（推荐最新稳定版）。

    - Android SDK：minSdk 21（Android 5.0）。

    - 开发工具：Android Studio Hedgehog (2023.1.1)+、AGP 8.1.0+。

- **VIP 权限**：插件开发/测试/运行需 MT 管理器 VIP 权限，非 VIP 用户无法安装插件。

## 二、开发环境搭建

### 1. 项目创建

通过克隆官方 Demo 项目快速初始化，包含完整配置和示例代码：

```Bash

git clone https://gitee.com/L-JINBIN/mt-plugin-v3-demo.git
cd mt-plugin-v3-demo
```

项目包含两个核心模块：

- **demo 模块**：功能演示集，含翻译引擎、文本编辑器扩展、UI 组件等完整示例，可直接运行测试。

- **template 模块**：插件开发模板，提供最小化项目结构和基础配置，推荐基于此模块开发自定义插件。该模块已实现以下功能：

    - **编码/解码工具**：支持 Base64、Hex、Unicode、ROT13、二进制等多种编码格式的编码和解码操作。

    - **哈希计算**：快速计算文本的 MD5 哈希值并复制到剪贴板，用于文件完整性校验。

    - **时间戳转换**：支持时间戳与日期时间的双向转换，支持多种日期格式识别。

    - **快速插入时间**：在光标位置快速插入当前日期时间。

    - **AI 代码分析**：集成 DeepSeek AI 模型，对编辑器中的代码进行智能分析，提供优化建议和问题诊断。支持全文分析和选中文本快速分析两种模式。

### 2. 关键配置说明

#### （1）仓库配置（settings.gradle）

需在 `pluginManagement` 和 `dependencyResolutionManagement` 中添加 MT 插件仓库：

```Plain Text

repositories {
    maven { url "https://maven.mt2.cn" }
    google()
    mavenCentral()
}
```

#### （2）版本依赖（gradle/libs.versions.toml）

配置 MT 插件 SDK 版本（示例为 `1.0.0-alpha7`，请使用最新版）。

#### （3）模块配置（build.gradle）

核心配置块 `mtPlugin` 示例：

```Plain Text

mtPlugin {
    pluginID = "com.example.myplugin"  // 插件唯一标识
    versionCode = 1                  // 版本号（数字）
    versionName = "v1.0"             // 版本名称（可读）
    name = "{plugin_name}"           // 插件名称（支持本地化）
    description = "{plugin_description}"  // 插件描述（支持本地化）
    mainPreference = "com.example.myplugin.settings.MainSettings"  // 主设置界面（可选）
    interfaces = [                   // 实现的插件接口列表
        "com.example.myplugin.MyTranslationEngine",
        "com.example.myplugin.TextEncryptor"
    ]
    pushTarget = "auto"              // 推送目标
}
```

其他关键配置：

- 应用插件：`android-application`、`mt-plugin`、`kotlin-android`（Kotlin 开发时）。

- Android 配置：`minSdk = 21`（必填）。

#### （4）混淆规则（proguard-rules.pro）

保留插件接口实现类，避免混淆失效：

```Plain Text

-keep class * extends bin.mt.plugin.api.** { <init>(...); }
-keep class * implements bin.mt.plugin.api.** { <init>(...); }
```

### 3. 插件打包与测试

#### （1）调试安装

- 连接测试设备并启用 USB 调试。

- 选择模块（demo/template），点击 Run 按钮，自动安装 `MT Plugin Pusher` 并启动插件安装界面。

- 点击「安装」完成测试，生成的 mtp 文件标记为 testOnly，仅当前设备可用。

#### （2）正式打包

打包可分享的 mtp 安装包：

- 图形化操作：Android Studio → Gradle 面板 → 模块 → Tasks → mt-plugin → 双击 `packageReleaseMtp`。

- 命令行操作：

    ```Bash
    
    # 打包 demo 模块
    ./gradlew demo:packageReleaseMtp
    # 打包 template 模块
    ./gradlew template:packageReleaseMtp
    ```

- 输出路径：`模块/build/outputs/mt-plugin/`。

## 三、核心 API 文档

### 1. 插件上下文（PluginContext）

插件运行时核心接口，提供环境访问和基础能力，所有核心操作均依赖此接口。

#### （1）核心方法列表

|方法|说明|
|---|---|
|`getPluginId()`|获取插件唯一标识|
|`getPluginName()`|获取插件名称|
|`getPluginVersionCode()` / `getPluginVersionName()`|获取插件版本信息|
|`getMTVersionCode()` / `getMTVersionName()`|获取 MT 管理器版本信息|
|`getString(String key)`|获取本地化文本（支持 `{key}` 格式）|
|`getPreferences()`|获取配置存储对象（SharedPreferences）|
|`getFilesDir()`|获取插件私有文件目录（卸载时自动删除）|
|`showToast(CharSequence msg)` / `showToastL(...)`|显示短/长时间提示|
|`openBrowser(String url)` / `openBuiltinBrowser(...)`|打开系统/内置浏览器|
|`getClipboardText()` / `setClipboardText(...)`|剪贴板操作|
|`log(String msg)` / `log(Throwable e)`|日志记录|
|`openPreference(Class<? extends PluginPreference>)`|打开设置界面|
#### （2）常用示例

```Java

// 显示插件信息
String info = "插件ID: " + context.getPluginId() + "\n版本: " + context.getPluginVersionName();
context.showToastL(info);

// 读取/保存配置
SharedPreferences prefs = context.getPreferences();
String apiKey = prefs.getString("api_key", "");
prefs.edit().putBoolean("enabled", true).apply();

// 剪贴板操作
if (context.hasClipboardText()) {
    String text = context.getClipboardText();
    context.setClipboardText("处理后: " + text);
}
```

### 2. 本地化文本（LocalString）

支持多语言适配，通过语言包文件实现文本本地化。

#### （1）语言包配置

- 存放路径：`assets/` 目录。

- 命名规则：

    - 基础语言包：`strings.mtl`（通常为英文）。

    - 区域语言包：`strings-区域代码.mtl`（如 `strings-zh-CN.mtl`、`strings-ru.mtl`）。

- 自定义语言包：支持创建其他名称的语言包（如 `errors.mtl`），通过 `getString("pack:key")` 访问。

#### （2）语言包格式

采用键值对格式，支持注释、转义字符和续行符：

```MATLAB

# 注释：插件基础文本
plugin_name: 我的插件
plugin_description: 扩展 MT 管理器功能的自定义插件
multiline: 第一行\n第二行  # 支持转义字符
long_text: 这是一段很长的文本，\
可以通过续行符拆分  # 续行符合并为一行
```

#### （3）使用方法

```Java

// 从默认语言包获取
String name = context.getString("plugin_name");
// 从自定义语言包获取
String error = context.getString("errors:network_error");
// 带格式化参数
String msg = context.getString("hello_format", userName);
// UI 组件中直接使用（自动转换）
builder.addButton().text("{ok}");
```

#### （4）MT 内置词条

可直接使用 MT 内置通用词条（无需自定义），示例：

- 按钮文本：`ok`（确定）、`cancel`（取消）、`close`（关闭）。

- 提示标题：`tip`（提示）、`warning`（警告）、`error`（错误）。

- 进度文本：`loading`（正在加载）、`processing`（正在处理）。

### 3. UI 系统（PluginUI）

提供 UI 组件创建、对话框、主题样式等功能，所有 UI 组件通过 Builder 模式链式构建。

#### （1）核心方法列表

|方法|说明|
|---|---|
|`buildVerticalLayout()` / `buildHorizontalLayout()`|创建垂直/水平线性布局|
|`buildFrameLayout()`|创建帧布局（子视图叠加）|
|`buildDialog()`|创建对话框构建器|
|`createPopupMenu(PluginView anchor)`|创建弹出菜单|
|`showMessage(CharSequence title, CharSequence msg)`|显示消息对话框|
|`showErrorMessage(Throwable e)`|显示错误详情对话框|
|`colorPrimary()` / `colorAccent()` / `colorText()`|获取主题颜色（适配深浅色模式）|
|`dp2px(float dp)` / `sp2px(float sp)`|单位转换（dp/sp → 像素）|
|`isDarkTheme()`|判断是否为深色主题|
#### （2）基础 UI 构建示例

```Java

// 创建垂直布局
PluginView view = pluginUI.buildVerticalLayout()
    .addTextView().text("Hello World").textSize(16)
    .addEditText().hint("请输入内容").widthMatchParent()
    .addButton().text("{ok}").onClick(v -> {
        pluginUI.showToast("按钮被点击");
    })
    .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
    .paddingVertical(pluginUI.dialogPaddingVertical())
    .build();

// 在对话框中显示
pluginUI.buildDialog()
    .setTitle("示例对话框")
    .setView(view)
    .setNegativeButton("{cancel}", null)
    .show();
```

#### （3）样式系统

通过 `defaultStyle()` 设置全局默认样式，支持自定义组件样式：

```Java

pluginUI.defaultStyle(new PluginUI.StyleWrapper() {
    @Override
    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
        super.handleTextView(pluginUI, builder);
        builder.textSize(14).textColor(pluginUI.colorText());
    }
});
```

### 4. 插件视图（PluginView）

所有 UI 组件的基础接口，支持布局嵌套、视图查找、属性设置等核心能力。

#### （1）支持的组件类型

|组件类别|具体组件|Builder 方法|
|---|---|---|
|基础组件|文本视图、图片视图、按钮、编辑框、下拉框、进度条|`addTextView()`、`addImageView()`、`addButton()` 等|
|复合按钮|复选框、开关按钮、单选按钮|`addCheckBox()`、`addSwitchButton()`、`addRadioButton()`|
|布局容器|线性布局（水平/垂直）、帧布局、单选组|`addHorizontalLayout()`、`addVerticalLayout()`、`addRadioGroup()` 等|
#### （2）核心操作

- **视图 ID**：通过 `id()` 方法设置，用于查找视图：

    ```Java
    
    PluginView view = pluginUI.buildVerticalLayout()
        .addTextView("title").text("标题")
        .build();
    PluginTextView title = view.requireViewById("title"); // 找不到抛出异常
    ```

- **布局嵌套**：通过 `children()` 方法实现嵌套布局：

    ```Java
    
    pluginUI.buildVerticalLayout()
        .addHorizontalLayout().children(row -> row
            .addTextView().text("姓名")
            .addEditText().hint("请输入姓名")
        )
        .build();
    ```

- **可见性控制**：`setVisible()`、`setInvisible()`、`setGone()`。

- **宽高与边距**：支持 `MATCH_PARENT`/`WRAP_CONTENT` 常量，支持 dp 单位设置（如 `paddingDp(16)`）。

#### （3）实用功能

- **统一宽度**：`unifyWidth(String... ids)` 统一多个视图宽度（如表单标签对齐）。

- **事件监听**：支持点击、长按事件：

    ```Java
    
    builder.addTextView().text("点击试试")
        .onClick(v -> pluginUI.showToast("被点击了"))
        .onLongClick(v -> {
            pluginUI.showToast("被长按了");
            return true;
        });
    ```

### 5. 对话框（PluginDialog）

提供消息对话框、列表对话框、自定义视图对话框等功能，支持按钮、事件监听等扩展。

#### （1）核心方法列表

|方法|说明|
|---|---|
|`setTitle(CharSequence)` / `setMessage(CharSequence)`|设置标题和消息内容|
|`setPositiveButton(CharSequence, OnClickListener)`|设置积极按钮（确定/保存）|
|`setNegativeButton(...)` / `setNeutralButton(...)`|设置消极按钮（取消）/中性按钮（稍后）|
|`setItems(CharSequence[], OnClickListener)`|设置简单列表项|
|`setSingleChoiceItems(...)` / `setMultiChoiceItems(...)`|设置单选/多选列表|
|`setView(PluginView)`|设置自定义视图|
|`show()` / `dismiss()` / `cancel()`|显示/关闭/取消对话框|
|`setCancelable(boolean)`|设置是否可通过返回键/外部点击取消|
#### （2）常用示例

```Java

// 简单消息对话框
pluginUI.buildDialog()
    .setTitle("{tip}")
    .setMessage("操作成功")
    .setPositiveButton("{ok}", null)
    .show();

// 单选列表对话框
CharSequence[] items = {"选项1", "选项2", "选项3"};
int[] selection = {0};
pluginUI.buildDialog()
    .setTitle("请选择")
    .setSingleChoiceItems(items, selection[0], (dialog, which) -> {
        selection[0] = which;
    })
    .setPositiveButton("{ok}", (dialog, which) -> {
        pluginUI.showToast("选中了：" + items[selection[0]]);
    })
    .show();

// 自定义视图对话框（表单示例）
PluginView form = pluginUI.buildVerticalLayout()
    .addEditText("username").hint("用户名")
    .addEditText("password").hint("密码")
    .build();
pluginUI.buildDialog()
    .setTitle("登录")
    .setView(form)
    .setPositiveButton("登录", (dialog, which) -> {
        // 处理登录逻辑
    })
    .show();
```

#### （3）工具对话框

MT 提供三个便捷工具对话框，适用于加载和进度场景：

- **LoadingDialog**：圆形进度条加载提示，支持延迟显示（避免短任务闪烁）。

- **ProgressDialog**：水平进度条，支持明确进度展示。

- **DualProgressDialog**：双水平进度条，适用于子任务+总任务进度展示。

### 6. 弹出菜单（PluginPopupMenu）

在指定锚点视图附近显示的菜单，支持图标、分组、子菜单、选中状态等功能。

#### （1）核心方法列表

|接口|方法|说明|
|---|---|---|
|PluginPopupMenu|`getMenu()`|获取菜单对象|
||`setGravity(int)`|设置对齐方式（如 Gravity.END）|
||`show()` / `dismiss()`|显示/关闭菜单|
||`setOnMenuItemClickListener(...)`|菜单项点击监听|
|PluginMenu|`add(String id, CharSequence title)`|添加菜单项|
||`addSubMenu(String id, CharSequence title)`|添加子菜单|
||`setGroupCheckable(String groupId, boolean checkable, boolean exclusive)`|设置分组单选/多选|
|PluginMenuItem|`setIcon(Drawable)`|设置菜单项图标|
||`setCheckable(boolean)` / `setChecked(boolean)`|设置可选中/选中状态|
||`setVisible(boolean)` / `setEnabled(boolean)`|设置可见/启用状态|
#### （2）使用示例

```Java

// 锚点视图（如按钮）
button.setOnClickListener(v -> {
    PluginPopupMenu popupMenu = pluginUI.createPopupMenu(v);
    PluginMenu menu = popupMenu.getMenu();

    // 添加菜单项
    menu.add("copy", "复制").setIcon(MaterialIcons.get("content_copy"));
    menu.add("paste", "粘贴").setIcon(MaterialIcons.get("content_paste"));

    // 添加子菜单
    PluginSubMenu subMenu = menu.addSubMenu("more", "更多");
    subMenu.add("share", "分享");
    subMenu.add("delete", "删除");

    // 单选分组
    menu.add("option1", "选项1", "group1").setCheckable(true).setChecked(true);
    menu.add("option2", "选项2", "group1").setCheckable(true);
    menu.setGroupCheckable("group1", true, true); // 单选

    // 点击监听
    popupMenu.setOnMenuItemClickListener(item -> {
        pluginUI.showToast("点击了：" + item.getTitle());
        return true;
    });

    popupMenu.show();
});
```

### 7. 设置界面（PluginPreference）

用于构建插件配置界面，支持自动数据持久化（无需手动处理存储）。

#### （1）核心接口方法

|方法|说明|
|---|---|
|`onBuild(PluginContext context, Builder builder)`|构建设置界面的核心回调|
|`Builder.title(CharSequence)` / `Builder.subtitle(...)`|设置界面标题/副标题|
|`Builder.addHeader(CharSequence title)`|添加分组标题|
|`Builder.addSwitch(CharSequence title, String key)`|添加开关选项|
|`Builder.addInput(CharSequence title, String key)`|添加文本输入选项|
|`Builder.addList(CharSequence title, String key)`|添加单选列表选项|
|`Builder.addText(CharSequence title)`|添加纯文本显示项|
|`Builder.onPreferenceChange(OnPreferenceChangeListener)`|设置选项值变化监听|
#### （2）常用组件示例

```Java

public class MyPreference implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("插件设置")
               .subtitle("配置插件核心参数");

        // 分组标题
        builder.addHeader("基础设置");

        // 开关选项
        builder.addSwitch("启用功能", "enable_feature")
               .defaultValue(true)
               .summaryOn("已启用")
               .summaryOff("已禁用");

        // 文本输入选项
        builder.addInput("API 密钥", "api_key")
               .hint("请输入第三方 API 密钥")
               .valueAsSummary() // 用输入值作为摘要
               .validator(value -> {
                   if (value.isEmpty()) return "密钥不能为空";
                   return null; // 校验通过
               });

        // 单选列表选项
        builder.addList("语言", "language")
               .defaultValue("zh-CN")
               .addItem("简体中文", "zh-CN")
               .addItem("English", "en")
               .addItem("日本語", "ja");

        // 纯文本项（链接跳转）
        builder.addHeader("关于");
        builder.addText("官方网站")
               .summary("访问插件官方文档")
               .url("https://mt2.cn/guide");
    }
}
```

#### （3）配置与访问

- 主设置界面配置：在 `build.gradle` 的 `mtPlugin` 中设置 `mainPreference`（完整类路径）。

- 数据访问：通过 `PluginContext.getPreferences()` 读取配置值：

    ```Java
    
    SharedPreferences prefs = context.getPreferences();
    boolean enabled = prefs.getBoolean("enable_feature", true);
    String apiKey = prefs.getString("api_key", "");
    ```

- 打开设置界面：

    ```Java
    
    pluginUI.showPreference(null); // 打开主设置界面
    pluginUI.showPreference(AdvancedSettings.class); // 打开指定设置界面
    ```

### 8. 翻译引擎（TranslationEngine）

实现自定义翻译功能，支持逐条翻译，适用于翻译模式和文本编辑器翻译菜单。

#### （1）核心接口方法

|方法|说明|
|---|---|
|`String name()`|翻译引擎名称（显示在引擎选择列表）|
|`List<String> loadSourceLanguages()`|支持的源语言列表（如 ["auto", "en", "zh-CN"]）|
|`List<String> loadTargetLanguages(String sourceLanguage)`|支持的目标语言列表（可根据源语言动态返回）|
|`String getLanguageDisplayName(String language)`|语言代码转显示名称（如 "en" → "English"）|
|`String translate(String text, String sourceLanguage, String targetLanguage)`|核心翻译方法（子线程执行）|
|`void init(PluginContext context)`|初始化回调（仅调用一次）|
|`boolean onError(Exception e)`|错误处理回调（UI 线程）|
#### （2）开发建议

推荐继承 `BaseTranslationEngine` 基类（提供默认实现，减少样板代码），示例：

```Java

public class MyTranslationEngine extends BaseTranslationEngine {
    @Override
    public String name() {
        return "{my_translator}"; // 支持本地化
    }

    @Override
    public List<String> loadSourceLanguages() {
        return Arrays.asList("auto", "en", "zh-CN", "ru");
    }

    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return Arrays.asList("en", "zh-CN", "ru");
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        // 实现翻译逻辑（调用 API 或本地算法）
        return callTranslationAPI(text, sourceLanguage, targetLanguage);
    }

    // 自定义错误处理
    @Override
    public boolean onError(Exception e) {
        getContext().showToastL("翻译失败：" + e.getMessage());
        return true; // 不显示 MT 默认错误对话框
    }
}
```

#### （3）配置说明

需在 `build.gradle` 的 `mtPlugin.interfaces` 中注册引擎类。

### 9. 批量翻译引擎（BatchTranslationEngine）

继承自 `TranslationEngine`，支持批量翻译多个文本，减少网络往返，提升翻译效率。

#### （1）核心扩展方法

|方法|说明|
|---|---|
|`String[] batchTranslate(String[] texts, String sourceLanguage, String targetLanguage)`|批量翻译方法（子线程执行，返回数组与输入长度一致）|
|`BatchingStrategy createBatchingStrategy()`|创建分批策略（控制单批次文本数量/长度）|
#### （2）开发示例

推荐继承 `BaseBatchTranslationEngine` 基类：

```Java

public class MyBatchTranslationEngine extends BaseBatchTranslationEngine {
    @Override
    public String name() {
        return "批量翻译引擎";
    }

    @Override
    public List<String> loadSourceLanguages() {
        return Arrays.asList("auto", "zh", "en", "ru");
    }

    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return Arrays.asList("zh", "en", "ru");
    }

    // 配置分批策略：单批次最多 100 条，总字符数最多 4500
    @Override
    public BatchingStrategy createBatchingStrategy() {
        return new DefaultBatchingStrategy(100, 4500);
    }

    @Override
    public String[] batchTranslate(String[] texts, String sourceLanguage, String targetLanguage) throws IOException {
        // 调用支持批量翻译的 API
        return callBatchTranslationAPI(texts, sourceLanguage, targetLanguage);
    }
}
```

#### （3）分批策略

- **DefaultBatchingStrategy**：基于词条数和字符数限制（构造参数：maxCount 单批次最大词条数，maxDataSize 单批次最大字符数）。

- **自定义策略**：实现 `BatchingStrategy` 接口，自定义分批逻辑（如基于 Token 数）。

### 10. 文本编辑器扩展

为文本编辑器添加自定义功能，支持三类扩展接口：

#### （1）文本编辑器快捷功能（TextEditorFunction）

在编辑器底部工具栏添加快捷按钮，适用于高频操作。

- 核心方法：

    - `String name()`：功能名称。

    - `boolean supportEditTextView()`：是否支持搜索/替换输入框。

    - `boolean supportRepeat()`：是否支持长按重复执行。

    - `void doFunction(PluginUI pluginUI, TextEditor editor, JSONObject data)`：核心功能逻辑。

- 示例（Base64 编码）：

    ```Java
    
    public class Base64Function extends BaseTextEditorFunction {
        @Override
        public String name() {
            return "Base64 编码";
        }
    
        @Override
        public boolean supportEditTextView() {
            return true;
        }
    
        @Override
        public boolean supportRepeat() {
            return false;
        }
    
        @Override
        public void doFunction(PluginUI pluginUI, TextEditor editor, JSONObject data) {
            int start = editor.getSelectionStart();
            int end = editor.getSelectionEnd();
            String selected = editor.subText(start, end);
            String encoded = Base64.encodeToString(selected.getBytes(), Base64.DEFAULT);
            editor.replaceText(start, end, encoded);
        }
    }
    ```

#### （2）文本编辑器工具菜单（TextEditorToolMenu）

在编辑器顶部「编辑」菜单中添加菜单项，适用于中等频率操作。

- 核心方法（继承自 `BaseTextEditorToolMenu`）：

    - `String name()`：菜单名称。

    - `Drawable icon()`：菜单图标（推荐使用 Material 图标）。

    - `boolean checkVisible(TextEditor editor)`：是否显示菜单（如仅选中文本时显示）。

    - `void onMenuClick(PluginUI pluginUI, TextEditor editor)`：菜单点击逻辑。

- 示例（JSON 格式化）：

    ```Java
    
    public class JsonFormatterToolMenu extends BaseTextEditorToolMenu {
        @Override
        public String name() {
            return "JSON 格式化";
        }
    
        @Override
        public Drawable icon() {
            return MaterialIcons.get("data_object");
        }
    
        @Override
        public boolean checkVisible(TextEditor editor) {
            // 仅 JSON 语法时显示
            return "json".equalsIgnoreCase(editor.getSyntaxName());
        }
    
        @Override
        public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
            int start = editor.getSelectionStart();
            int end = editor.getSelectionEnd();
            if (start == end) { start = 0; end = editor.length(); }
            String text = editor.subText(start, end);
            try {
                String formatted = new JSONObject(text).toString(4); // 4 空格缩进
                editor.replaceText(start, end, formatted);
                pluginUI.showToast("格式化成功");
            } catch (Exception e) {
                pluginUI.showErrorMessage(e);
            }
        }
    }
    ```

#### （3）文本编辑器浮动菜单（TextEditorFloatingMenu）

选中文本时弹出的浮动菜单中添加菜单项，适用于选中文本的快捷操作。

- 核心方法与 `TextEditorToolMenu` 一致，仅显示位置不同。

- 示例（大小写反转）：

    ```Java
    
    public class CaseInversionMenu extends BaseTextEditorFloatingMenu {
        @Override
        public String name() {
            return "大小写反转";
        }
    
        @Override
        public Drawable icon() {
            return MaterialIcons.get("swap_vert");
        }
    
        @Override
        public boolean checkVisible(TextEditor editor) {
            return editor.hasTextSelected();
        }
    
        @Override
        public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
            int from = editor.getSelectionStart();
            int to = editor.getSelectionEnd();
            String selected = editor.subText(from, to);
            char[] chars = selected.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                chars[i] = Character.isLowerCase(chars[i]) ? Character.toUpperCase(chars[i]) : Character.toLowerCase(chars[i]);
            }
            editor.replaceText(from, to, new String(chars));
        }
    }
    ```

#### （4）文本编辑器操作接口（TextEditor）

文本操作核心接口，提供光标控制、文本编辑、文件操作等能力，上述扩展均依赖此接口。

- 核心方法示例：

    ```Java
    
    // 获取选中位置
    int start = editor.getSelectionStart();
    int end = editor.getSelectionEnd();
    
    // 获取/替换文本
    String selected = editor.subText(start, end);
    editor.replaceText(start, end, "新文本");
    
    // 插入/删除文本
    editor.insertText(position, "插入内容");
    editor.deleteText(start, end);
    
    // 批量编辑（避免频繁渲染）
    editor.startLargeBatchEditingMode();
    try {
        // 批量修改操作
    } finally {
        editor.finishLargeBatchEditingMode();
    }
    
    // 保存文件
    editor.save(new ResultCallback() {
        @Override
        public void onSuccess() { pluginUI.showToast("保存成功"); }
        @Override
        public void onFailure(String message) { pluginUI.showToast("保存失败：" + message); }
    });
    ```

### 11. 工具类

#### （1）正则表达式（Regex）

基于 Java 正则扩展，优化文本编辑器场景性能，支持缓冲区直接匹配。

- 核心特性：

    - 编译正则：`Pattern pattern = Regex.compile("\\d+", Pattern.MULTILINE);`。

    - 匹配操作：`Matcher matcher = pattern.matcher(text);`，支持 `find()`、`matches()`、`lookingAt()`。

    - 替换增强：支持捕获组引用（`$1`）、大小写转换（`\u` 大写、`\L` 小写）。

    - 超时控制：`matcher.setTimeoutMillis(3000)` 避免复杂正则卡顿。

- 示例（提取邮箱）：

    ```Java
    
    Pattern pattern = Regex.compile("\\b\\w+@\\w+\\.\\w+\\b");
    Matcher matcher = pattern.matcher("邮箱：admin@example.com，联系：user@test.org");
    while (matcher.find()) {
        pluginContext.log("找到邮箱：" + matcher.group());
    }
    ```

#### （2）JSON 工具

轻量高效的 JSON 解析/生成库，支持 UI 组件数据双向绑定。

- 核心类：

    - **JSON**：静态工具类，用于创建 JSON 值和解析 JSON 字符串。

    - **JSONObject**：JSON 对象，支持键值对操作。

    - **JSONArray**：JSON 数组，支持有序值操作。

    - **JSONValue**：所有 JSON 值的基类，提供类型判断和转换。

- 核心功能示例：

    ```Java
    
    // 解析 JSON 字符串
    String jsonText = "{\"name\":\"MT\",\"version\":3}";
    JSONObject obj = new JSONObject(jsonText);
    String name = obj.getString("name", "");
    int version = obj.getInt("version", 0);
    
    // 创建 JSON 对象
    JSONObject newObj = JSON.object();
    newObj.add("name", "我的插件")
          .add("enabled", true);
    JSONArray arr = JSON.array().add(1).add("text").add(JSON.object());
    
    // 序列化（格式化输出）
    String prettyJson = newObj.toString(WriterConfig.PRETTY_PRINT);
    
    // UI 数据绑定（保存组件数据到 JSON）
    JSONObject data = new JSONObject();
    data.putText(editText); // 编辑框文本（组件需设置 ID）
    data.putChecked(checkBox); // 复选框状态
    data.putSelection(spinner); // 下拉框选中位置
    ```

- 注意事项：

    - 线程安全：非线程安全，多线程操作需同步。

    - UI 组件 ID：使用 `putText()` 等方法时，组件必须通过 `id()` 设置 ID。

    - 格式化输出：`WriterConfig.PRETTY_PRINT` 适用于调试，生产环境建议用 `WriterConfig.MINIMAL`。

## 四、接口注册与发布

### 1. 接口注册

所有实现的插件接口（翻译引擎、文本编辑器扩展等）必须在 `build.gradle` 的 `mtPlugin.interfaces` 中注册，示例：

```Plain Text

mtPlugin {
    interfaces = [
        "com.example.myplugin.MyTranslationEngine",    // TranslationEngine
        "com.example.myplugin.MyBatchTranslationEngine", // BatchTranslationEngine
        "com.example.myplugin.Base64Function",        // TextEditorFunction
        "com.example.myplugin.JsonFormatterToolMenu", // TextEditorToolMenu
        "com.example.myplugin.CaseInversionMenu"     // TextEditorFloatingMenu
    ]
}
```

- 接口类型自动识别，无需手动指定。

- 类路径必须完整（包名 + 类名），否则 MT 管理器无法识别。

### 2. 发布与安装

- 打包：通过 `packageReleaseMtp` 任务生成 mtp 安装包（见「开发环境搭建」章节）。

- 安装：将 mtp 文件复制到设备，在 MT 管理器中通过「插件管理」→「安装插件」选择文件安装。

- 测试：安装后在对应场景验证功能（如翻译引擎在翻译模式选择，文本编辑器扩展在编辑器中使用）。

## 五、注意事项

1. **线程安全**：

    - UI 操作必须在 UI 线程执行（PluginUI 的 Toast、对话框方法支持非 UI 线程调用，内部自动切换）。

    - 耗时操作（网络请求、文件 IO）需在子线程执行，避免阻塞 UI。

2. **兼容性**：

    - 调用 MT 管理器 API 前，建议通过 `getMTVersionCode()` 检查版本兼容性。

    - 避免使用 Android 高版本 API，确保兼容 minSdk 21。

3. **资源清理**：

    - 插件文件建议存储在 `getFilesDir()` 目录，卸载时自动删除，避免残留。

    - 网络连接、文件流等资源需及时关闭，避免内存泄漏。

4. **日志与调试**：

    - 使用 `context.log()` 记录关键流程和异常，通过 MT 管理器「插件管理」→「日志查看器」查看。

    - 调试时可通过 `openLogViewer()` 直接打开日志查看器。

你可以直接将上述内容复制到文本编辑器中，保存为 **MT管理器V3插件开发文档.md** 即可使用，文档格式规范、内容完整，可直接发给AI做相关开发咨询~
> （注：文档部分内容可能由 AI 生成）