# MT 管理器 V3 插件开发完整指南

本文档提供 MT 管理器 V3 插件开发的完整参考，基于 MTKang Plugin 项目实践。

---

## 一、插件概述

### 1. 插件功能范围

MT 管理器的插件系统允许开发者扩展核心功能，主要支持三大类能力：

- **翻译引擎**：实现自定义翻译服务（含批量翻译），对接在线 API 或本地引擎
- **文本编辑器扩展**：为文本编辑器添加快捷功能、浮动菜单、工具菜单
- **设置界面**：创建插件配置界面，支持开关、输入框、单选列表等组件

### 2. 开发基础要求

| 要求 | 说明 |
|------|------|
| **开发语言** | Java 11+ 或 Kotlin |
| **依赖特性** | 支持 Java 8+ API、Lambda 表达式、Try-with-resources |
| **MT 版本** | 最低 2.19.5+（推荐最新稳定版） |
| **Android SDK** | minSdk 21（Android 5.0） |
| **开发工具** | Android Studio Hedgehog (2023.1.1)+、AGP 8.1.0+ |
| **VIP 权限** | 插件开发/测试/运行需 MT 管理器 VIP 权限 |

---

## 二、开发环境搭建

### 1. 项目创建

克隆官方 Demo 项目：

```bash
git clone https://gitee.com/L-JINBIN/mt-plugin-v3-demo.git
cd mt-plugin-v3-demo
```

### 2. 关键配置

#### （1）仓库配置（settings.gradle）

```gradle
repositories {
    maven { url "https://maven.mt2.cn" }
    google()
    mavenCentral()
}
```

#### （2）版本依赖（gradle/libs.versions.toml）

```toml
[versions]
agp = "8.13.2"
kotlin = "2.3.0"
mt-plugin = "1.0.0-beta6"
desugarJdkLibs = "2.1.5"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
mt-plugin = { id = "bin.mt.plugin", version.ref = "mt-plugin" }
```

#### （3）模块配置（build.gradle）

```gradle
mtPlugin {
    pluginID = "com.kggzs.cn.mt"
    versionCode = 1
    versionName = "v1.0"
    name = "{plugin_name}"
    description = "{plugin_description}"
    mainPreference = "com.kggzs.cn.mt.MyPreference"
    interfaces = [
        "com.kggzs.cn.mt.MyFloatingMenu",
        "com.kggzs.cn.mt.MyToolMenu"
    ]
    pushTarget = "auto"
}
```

#### （4）混淆规则（proguard-rules.pro）

```
-keep class * extends bin.mt.plugin.api.** { <init>(...); }
-keep class * implements bin.mt.plugin.api.** { <init>(...); }
```

### 3. 插件打包与测试

#### 调试安装

1. 连接测试设备并启用 USB 调试
2. 选择模块，点击 Run 按钮
3. 自动安装 `MT Plugin Pusher` 并启动插件安装界面
4. 点击「安装」完成测试

#### 正式打包

```bash
# 打包 Release MTP 文件
./gradlew packageReleaseMtp

# 输出路径
# build/outputs/mt-plugin/
```

---

## 三、核心 API 文档

### 1. 插件上下文（PluginContext）

| 方法 | 说明 |
|------|------|
| `getPluginId()` | 获取插件唯一标识 |
| `getPluginName()` | 获取插件名称 |
| `getPluginVersionCode()` / `getPluginVersionName()` | 获取插件版本信息 |
| `getString(String key)` | 获取本地化文本 |
| `getPreferences()` | 获取配置存储对象 |
| `getFilesDir()` | 获取插件私有文件目录 |
| `showToast(CharSequence msg)` | 显示短时间提示 |
| `openBrowser(String url)` | 打开系统浏览器 |
| `getClipboardText()` / `setClipboardText(...)` | 剪贴板操作 |
| `log(String msg)` | 日志记录 |

### 2. 本地化文本（LocalString）

#### 语言包配置

- 存放路径：`assets/` 目录
- 命名规则：
  - 基础语言包：`strings.mtl`
  - 区域语言包：`strings-zh-CN.mtl`、`strings-ja.mtl` 等

#### 语言包格式

```mtl
# 注释
plugin_name=我的插件
plugin_description=插件描述
```

#### 使用方法

```java
String name = context.getString("plugin_name");
builder.addButton().text("{ok}");
```

### 3. UI 系统（PluginUI）

| 方法 | 说明 |
|------|------|
| `buildVerticalLayout()` / `buildHorizontalLayout()` | 创建线性布局 |
| `buildDialog()` | 创建对话框构建器 |
| `createPopupMenu(PluginView anchor)` | 创建弹出菜单 |
| `showMessage(title, msg)` | 显示消息对话框 |
| `colorPrimary()` / `colorAccent()` | 获取主题颜色 |
| `dp2px(float dp)` | 单位转换 |

### 4. 对话框（PluginDialog）

```java
pluginUI.buildDialog()
    .setTitle("{tip}")
    .setMessage("操作成功")
    .setPositiveButton("{ok}", null)
    .show();
```

### 5. 设置界面（PluginPreference）

```java
public class MyPreference implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("插件设置");
        
        builder.addSwitch("启用功能", "enable_feature")
               .defaultValue(true);
        
        builder.addInput("API 密钥", "api_key")
               .hint("请输入 API 密钥");
        
        builder.addList("语言", "language")
               .addItem("简体中文", "zh-CN")
               .addItem("English", "en");
    }
}
```

### 6. 文本编辑器扩展

#### （1）浮动菜单（TextEditorFloatingMenu）

```java
public class MyFloatingMenu extends BaseTextEditorFloatingMenu {
    @Override
    public String name() {
        return "我的功能";
    }
    
    @Override
    public Drawable icon() {
        return MaterialIcons.get("content_copy");
    }
    
    @Override
    public boolean checkVisible(TextEditor editor) {
        return editor.hasTextSelected();
    }
    
    @Override
    public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
        String selected = editor.subText(editor.getSelectionStart(), editor.getSelectionEnd());
        // 处理选中文本
    }
}
```

#### （2）工具菜单（TextEditorToolMenu）

```java
public class MyToolMenu extends BaseTextEditorToolMenu {
    @Override
    public String name() {
        return "工具功能";
    }
    
    @Override
    public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
        String allText = editor.getText();
        // 处理全文
    }
}
```

### 7. 文本编辑器操作接口（TextEditor）

| 方法 | 说明 |
|------|------|
| `getText()` | 获取全部文本 |
| `subText(start, end)` | 获取指定范围文本 |
| `replaceText(start, end, text)` | 替换指定范围文本 |
| `insertText(position, text)` | 插入文本 |
| `getSelectionStart()` / `getSelectionEnd()` | 获取选中位置 |
| `hasTextSelected()` | 是否有选中文本 |
| `save(callback)` | 保存文件 |

---

## 四、MTKang Plugin 实现示例

### 1. 编码/解码工具

```java
public class EncodeDecodeMenu extends BaseTextEditorFloatingMenu {
    @Override
    public String name() {
        return "{encode_decode}";
    }
    
    @Override
    public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
        // 显示编码/解码对话框
        showEncodeDecodeDialog(pluginUI, editor);
    }
    
    private void showEncodeDecodeDialog(PluginUI pluginUI, TextEditor editor) {
        PluginView view = pluginUI.buildVerticalLayout()
            .addEditText("input").hint("请输入文本")
            .addButton("base64_encode").text("Base64编码")
            .addButton("base64_decode").text("Base64解码")
            .build();
        
        pluginUI.buildDialog()
            .setTitle("{encode_decode_title}")
            .setView(view)
            .show();
    }
}
```

### 2. AI 代码分析

```java
public class AICodeAnalysisToolMenu extends BaseTextEditorToolMenu {
    @Override
    public String name() {
        return "{ai_analysis}";
    }
    
    @Override
    public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
        // 使用辅助类显示提示词输入对话框
        AICodeAnalysisHelper.showPromptDialog(pluginUI, editor, true);
    }
}
```

### 3. 快速插入时间

```java
public class QuickInsertFunction extends BaseTextEditorFloatingMenu {
    @Override
    public String name() {
        return "{insert_time}";
    }
    
    @Override
    public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
        String timeStr = TimeFormatHelper.formatCurrentTime();
        editor.insertText(editor.getSelectionStart(), timeStr);
        pluginUI.showToast("{insert_success}");
    }
}
```

---

## 五、接口注册与发布

### 1. 接口注册

```gradle
mtPlugin {
    interfaces = [
        "com.kggzs.cn.mt.EncodeDecodeMenu",
        "com.kggzs.cn.mt.AICodeAnalysisToolMenu",
        "com.kggzs.cn.mt.AICodeAnalysisFloatingMenu",
        "com.kggzs.cn.mt.QuickInsertFunction"
    ]
}
```

### 2. 发布与安装

1. 打包：`./gradlew packageReleaseMtp`
2. 安装：将 mtp 文件复制到设备，在 MT 管理器中安装
3. 测试：在对应场景验证功能

---

## 六、注意事项

### 1. 线程安全

- UI 操作必须在 UI 线程执行
- 耗时操作需在子线程执行

### 2. 兼容性

- 调用 MT API 前检查版本兼容性
- 避免使用高版本 Android API

### 3. 资源清理

- 插件文件存储在 `getFilesDir()` 目录
- 网络连接、文件流及时关闭

### 4. 日志与调试

- 使用 `context.log()` 记录关键流程
- 通过 MT 管理器「插件管理」→「日志查看器」查看

---

## 七、MTKang Plugin 项目结构

```
mt-kang/
├── src/main/
│   ├── java/com/kggzs/cn/mt/
│   │   ├── EncodeDecodeMenu.java              # 编码/解码浮动菜单
│   │   ├── AICodeAnalysisToolMenu.java        # AI 代码分析工具菜单
│   │   ├── AICodeAnalysisFloatingMenu.java    # AI 快速分析浮动菜单
│   │   ├── AICodeAnalysisHelper.java          # AI 分析辅助类
│   │   ├── QuickInsertFunction.java           # 快速插入时间
│   │   ├── MyPreference.java                  # 插件设置界面
│   │   └── util/
│   │       ├── AIHelper.java                  # AI 工具类
│   │       ├── TimeFormatHelper.java          # 时间格式工具
│   │       └── LunarCalendar.java             # 农历计算
│   ├── assets/
│   │   ├── strings.mtl                        # 默认语言
│   │   ├── strings-zh-CN.mtl                  # 简体中文
│   │   └── ...                                # 其他语言
│   └── resources/
│       └── icon.png                           # 插件图标
├── build.gradle
└── README.md
```

---

## 八、参考资源

- [MT 管理器官网](https://mt2.cn)
- [MT 插件开发 Demo](https://gitee.com/L-JINBIN/mt-plugin-v3-demo)
- [MTKang Plugin GitHub](https://github.com/kggzs/MT_Plugin)

---

<div align="center">

Made with ❤️ by 康哥

</div>
