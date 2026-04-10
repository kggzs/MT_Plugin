# MT 管理器插件开发 API 速查手册（AI 友好版）

> 本文档为 AI 快速理解 MT 管理器 v3 插件开发体系而设计，涵盖全部 16 个 API 页面的核心内容。
> 语言：Java / Kotlin | SDK 版本：3 | minSdk：21（Android 5.0）| MT 最低版本：2.26.3+ | 需要 VIP

---

## 一、项目结构与配置

### 1.1 快速开始

```bash
git clone https://gitee.com/L-JINBIN/mt-plugin-v3-demo.git
```

项目包含两个模块：
- **demo**：功能演示，学习参考
- **template**：最小模板，基于此开发新插件

### 1.2 build.gradle 核心配置

```groovy
mtPlugin {
    pluginID = "com.example.myplugin"       // 唯一标识
    versionCode = 1                          // 版本号
    versionName = "v1.0"                     // 版本名称
    name = "{plugin_name}"                   // 插件名（支持 {key} 本地化）
    description = "{plugin_description}"     // 描述（支持本地化）
    mainPreference = "com.example.MyPreference"  // 主设置界面（可选）
    interfaces = [                           // 所有对外接口（必须注册！）
        "com.example.GoogleTranslator",      //   TranslationEngine
        "com.example.FormatCode",            //   TextEditorFunction
        "com.example.QuickActions",          //   TextEditorFloatingMenu
    ]
    pushTarget = "auto"
}
```

**关键规则**：
- `minSdk` 必须为 21
- `interfaces` 中必须列出所有实现的接口类完整路径，接口类型自动识别
- 未注册的接口不会被 MT 识别

### 1.3 打包与安装

- **测试运行**：Android Studio Run → 安装 MT Plugin Pusher → 自动打开 MT 安装界面（testOnly，不可分享）
- **正式打包**：`./gradlew demo:packageReleaseMtp` → 产出在 `build/outputs/mt-plugin/`

---

## 二、核心接口体系总览

```
MT 插件接口体系
├── 翻译引擎
│   ├── TranslationEngine（单文本翻译）
│   └── BatchTranslationEngine（批量翻译）
├── 文本编辑器扩展
│   ├── TextEditorFunction（底部快捷功能）
│   ├── TextEditorFloatingMenu（选中文本浮动菜单）
│   └── TextEditorToolMenu（顶部工具栏菜单）
├── 设置界面
│   └── PluginPreference（配置界面）
└── 工具类
    ├── Regex（正则表达式）
    └── JSON（JSON 解析/生成）
```

---

## 三、PluginContext — 插件运行时上下文

所有插件功能的入口，提供环境、资源、存储、UI 辅助等能力。

### 3.1 插件/宿主信息

| 方法 | 返回 | 说明 |
|------|------|------|
| `getPluginId()` | String | 插件唯一标识 |
| `getPluginName()` | String | 插件名称 |
| `getPluginVersionCode()` | int | 版本代码 |
| `getPluginVersionName()` | String | 版本名称 |
| `getPluginSdkVersion()` | int | SDK 版本（当前=3） |
| `getHostPackageName()` | String | MT 包名 |
| `getHostVersionName()` | String | MT 版本名 |
| `getHostVersionCode()` | int | MT 版本号（可用于兼容性判断） |

### 3.2 本地化文本

| 方法 | 说明 |
|------|------|
| `getString(key)` | 获取本地化文本（找不到抛异常） |
| `getString(key, formatArgs...)` | 带格式化参数（%s, %d） |
| `getStringNullable(key)` | 找不到返回 null |
| `getStringList(keys...)` | 批量获取（返回 List） |
| `getStringArray(keys...)` | 批量获取（返回数组，会复用传入数组） |

key 格式：`"key"` / `"{key}"` → strings 语言包（回退到 MT 内置）；`"pack:key"` → 指定语言包（无回退）

### 3.3 语言环境

| 方法 | 返回 | 示例 |
|------|------|------|
| `getLanguage()` | String | "zh", "en" |
| `getCountry()` | String | "CN", "US" |
| `getLanguageCountry()` | String | "zh-CN", "en" |

### 3.4 配置存储

```java
SharedPreferences prefs = context.getPreferences();  // 插件专用，卸载自动删除
prefs.getString("key", "default");
prefs.edit().putString("key", "value").apply();
```

### 3.5 文件操作

| 方法 | 说明 |
|------|------|
| `getFilesDir()` | 插件私有目录（卸载自动删除） |
| `getAssetsAsStream(name)` | 读取 assets 文件（如 "data/config.txt"） |

### 3.6 Toast / 浏览器 / 剪贴板

| 方法 | 说明 |
|------|------|
| `showToast(msg)` / `showToastL(msg)` | 短/长 Toast（支持 {key}，可非 UI 线程调用） |
| `cancelToast()` | 取消 Toast |
| `openBrowser(url)` | 系统浏览器 |
| `openBuiltinBrowser(url, showTitleBar)` | MT 内置浏览器（返回键直接退出） |
| `hasClipboardText()` | 剪贴板是否有文本 |
| `getClipboardText()` | 获取剪贴板文本 |
| `setClipboardText(text)` / `setClipboardText(text, msg)` | 设置剪贴板（可自定义提示） |

### 3.7 日志 / 设置界面

| 方法 | 说明 |
|------|------|
| `log(msg)` / `log(msg, e)` / `log(e)` | 写入日志（可在 MT 插件管理中查看） |
| `openLogViewer()` | 打开日志查看器 |
| `openPreference(clazz)` | 打开设置界面（null=主设置界面） |

---

## 四、LocalString — 本地化文本系统

### 4.1 语言包文件

放在 `assets/` 目录，命名规则：
- `strings.mtl` — 默认语言包（英文）
- `strings-zh-CN.mtl` — 简体中文
- `strings-zh-TW.mtl` — 繁体中文
- `errors.mtl` / `errors-zh-CN.mtl` — 自定义语言包

### 4.2 格式

```
# 注释
key: 值
multiline: 第一行\n第二行
long_text: 续行符\  下一行继续
```

### 4.3 查找优先级

`strings-zh-CN.mtl` → `strings-zh.mtl` → `strings.mtl` → MT 内置语言包（仅 strings 包有回退）

### 4.4 MT 内置词条（可直接使用）

```
ok / cancel / close / tip / notice / information / warning / error
loading / preparing / reloading / processing / canceling
press_again_to_cancel / out_of_memory_error
```

### 4.5 全球语言名称包（lang:）

```java
context.getString("lang:auto");    // 自动检测
context.getString("lang:en");      // 英语
context.getString("lang:zh-CN");   // 简体中文
context.getString("lang:ja");      // 日语
// ... 支持 100+ 种语言
```

### 4.6 UI 中直接使用 {key}

所有接受 String/CharSequence 的 UI 方法都自动支持 `{key}` 本地化：
```java
builder.addTextView().text("{plugin_name}");
builder.addButton().text("{ok}");
builder.addEditText().hint("{input_hint}");
pluginUI.buildDialog().setTitle("{warning}").setMessage("内容").show();
context.showToast("{operation_success}");
```

---

## 五、PluginUI — UI 核心

### 5.1 创建布局

```java
// 垂直/水平/帧布局
PluginView view = pluginUI.buildVerticalLayout()    // 或 buildHorizontalLayout() / buildFrameLayout()
    .addTextView("id").text("Hello")
    .addButton("btn").text("Click").onClick(v -> { ... })
    .build();

// 嵌套布局
pluginUI.buildVerticalLayout()
    .addHorizontalLayout().children(row -> row
        .addTextView().text("标签")
        .addEditText().hint("输入")
    )
    .build();

// 在对话框中显示
pluginUI.buildDialog().setView(view).show();
```

### 5.2 支持的视图组件

| 组件 | Builder 方法 | 说明 |
|------|-------------|------|
| View | `addView()` | 普通视图（分割线等） |
| TextView | `addTextView()` | 文本 |
| ImageView | `addImageView()` | 图片 |
| Button | `addButton()` | 按钮（多种风格） |
| EditText | `addEditText()` / `addEditBox()` | 编辑框（支持语法高亮） |
| Spinner | `addSpinner()` | 下拉选择 |
| ProgressBar | `addProgressBar()` | 进度条 |
| CheckBox | `addCheckBox()` | 复选框 |
| SwitchButton | `addSwitchButton()` | 开关 |
| RadioButton | `addRadioButton()` | 单选按钮 |
| RadioGroup | `addRadioGroup(isVertical)` | 单选组 |
| LinearLayout | `addHorizontalLayout()` / `addVerticalLayout()` | 线性布局 |
| FrameLayout | `addFrameLayout()` | 帧布局 |

### 5.3 视图 ID 与查找

```java
// 设置 ID
.addTextView("myText").text("Hello")

// 查找
view.findViewById("myText");      // 找不到返回 null
view.requireViewById("myText");   // 找不到抛异常

// getRootView() 缓存查找结果，可在回调中安全使用
button.onClick(v -> {
    PluginEditText input = v.getRootView().requireViewById("input");
});

// ID 严格模式（默认开启）
pluginUI.disableStrictIdMode();  // 允许重复 ID
```

### 5.4 常用属性

```java
// 尺寸
.width(100).height(50)           // px
.widthDp(100).heightDp(50)       // dp
.widthMatchParent()              // MATCH_PARENT
.width(0).layoutWeight(1)        // 权重分配

// 内外边距
.paddingDp(16).marginTopDp(8)
.marginDp(16)

// 可见性
.gone() / .visible() / .invisible()

// 事件
.onClick(v -> { ... })
.onLongClick(v -> { return true; })

// 宽度统一（表单对齐）
view.unifyWidth("label1", "label2", "label3");
```

### 5.5 主题颜色

| 方法 | 说明 |
|------|------|
| `isDarkTheme()` | 是否深色主题 |
| `colorPrimary()` | 主色 |
| `colorAccent()` | 强调色 |
| `colorDivider()` | 分割线颜色 |
| `colorError()` / `colorWarning()` | 错误/警告色 |
| `colorText()` / `colorTextSecondary()` | 主/次文本色 |
| `colorTextStateList()` | 带状态的文本色（禁用变灰） |
| `dialogPaddingHorizontal()` / `dialogPaddingVertical()` | 对话框推荐内边距 |
| `selectableItemBackground()` | 可选择项目背景 |
| `dp2px(dp)` / `sp2px(sp)` | 单位转换 |

### 5.6 样式系统

```java
pluginUI.defaultStyle(new PluginUI.StyleWrapper() {
    @Override
    protected void handleTextView(PluginUI ui, PluginTextViewBuilder builder) {
        super.handleTextView(ui, builder);  // 先继承当前样式
        builder.textSize(14);               // 再覆盖
    }
});
```

样式在 Builder 创建后立即应用，用户后续链式调用会覆盖样式设置。

### 5.7 快捷对话框

```java
pluginUI.showMessage("{tip}", "内容");           // 消息对话框
pluginUI.showErrorMessage(exception);            // 错误对话框（自动记录日志）
pluginUI.showPreference(null);                   // 打开主设置界面
```

---

## 六、PluginDialog — 对话框

### 6.1 创建

```java
PluginDialog dialog = pluginUI.buildDialog()
    .setTitle("{tip}")
    .setMessage("内容")
    .setPositiveButton("{ok}", (d, which) -> { ... })
    .setNegativeButton("{cancel}", null)
    .setNeutralButton("稍后", null)
    .setCancelable(false)  // 不可取消
    .show();               // 创建并显示
// 或 .create() 先创建，再 dialog.show()
```

### 6.2 列表对话框

```java
// 简单列表（点击自动关闭）
.setItems(items, (d, which) -> { ... })

// 单选列表（点击不关闭）
.setSingleChoiceItems(items, checkedIndex, (d, which) -> { ... })

// 多选列表
.setMultiChoiceItems(items, checkedBooleans, (d, which, isChecked) -> { ... })
```

### 6.3 自定义视图

```java
PluginView view = pluginUI.buildVerticalLayout()
    .addEditText("input").hint("请输入")
    .build();
pluginUI.buildDialog().setTitle("标题").setView(view).show();
```

### 6.4 按钮不关闭对话框

```java
PluginDialog dialog = pluginUI.buildDialog()
    .setTitle("计数器")
    .setPositiveButton("点我", null)  // listener 传 null
    .show();
dialog.getPositiveButton().setOnClickListener(v -> {
    count++;
    if (count >= 10) dialog.dismiss();  // 手动关闭
});
```

### 6.5 事件监听

```java
.setOnShowListener(d -> { ... })         // 显示时
.setOnDismissListener(d -> { ... })      // 消失时（所有关闭方式）
.setOnCancelListener(d -> { ... })       // 取消时（返回键/点外部）
.setOnKeyListener((d, keyCode, event) -> { return true; })  // 按键
```

### 6.6 工具对话框

**LoadingDialog**：加载提示（圆形进度条 + 双行文本）
```java
LoadingDialog loading = new LoadingDialog(pluginUI);
loading.showDelay(200);  // 延迟显示，避免闪烁
loading.setMessage("正在处理...");
loading.setSecondaryMessage("已完成 50 个");
loading.setCancelable();  // 双击返回键取消
loading.dismiss();        // 线程安全
```

**ProgressDialog**：水平进度条
```java
ProgressDialog progress = new ProgressDialog(pluginUI);
progress.setTitle("导出中").setMessage("...").show();
progress.setMax(100);
progress.setProgress(50);
```

---

## 七、PluginPopupMenu — 弹出菜单

### 7.1 创建与使用

```java
PluginPopupMenu popup = pluginUI.createPopupMenu(anchorView);
PluginMenu menu = popup.getMenu();

menu.add("item1", "菜单项1");
menu.add("item2", "菜单项2").setIcon(MaterialIcons.get("search"));
menu.add("item3", "选项3", "group1");  // 带分组

// 子菜单
PluginSubMenu sub = menu.addSubMenu("sub1", "子菜单");
sub.add("sub_item1", "子项1");

popup.setOnMenuItemClickListener(item -> {
    // item.getItemId() / item.getTitle()
    return true;
});
popup.setGravity(Gravity.END);
popup.show();
```

### 7.2 菜单项配置

```java
item.setTitle("新标题");
item.setIcon(drawable);
item.setCheckable(true).setChecked(true);  // 可选中
item.setVisible(false);                     // 隐藏
item.setEnabled(false);                     // 禁用
```

### 7.3 分组管理

```java
menu.setGroupCheckable("group1", true, true);  // 单选模式
menu.setGroupVisible("group1", false);          // 隐藏分组
menu.setGroupEnabled("group1", false);          // 禁用分组
menu.setGroupDividerEnabled(true);              // 显示分组分隔线
```

---

## 八、PluginPreference — 设置界面

### 8.1 基本结构

```java
public class MyPreference implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("插件设置").subtitle("配置参数");
        builder.addHeader("基础设置");
        builder.addSwitch("启用功能", "enable_feature")
               .defaultValue(true).summaryOn("已启用").summaryOff("已禁用");
        builder.addInput("API 地址", "api_url")
               .defaultValue("https://api.example.com").valueAsSummary();
    }
}
```

### 8.2 设置项类型

| 类型 | 方法 | 存储类型 | 特有方法 |
|------|------|---------|---------|
| Text | `addText(title, key)` | 无 | `summary()`, `url()`, `onClick()` |
| Input | `addInput(title, key)` | String | `defaultValue()`, `hint()`, `valueAsSummary()`, `inputType()`, `validator()` |
| Switch | `addSwitch(title, key)` | Boolean | `defaultValue()`, `summaryOn()`, `summaryOff()` |
| List | `addList(title, key)` | String | `defaultValue()`, `addItem(value, label)` |

### 8.3 通用方法

所有类型都支持：`visible(bool)`, `enable(bool)`, `interceptClick(listener)`

### 8.4 输入校验

```java
builder.addInput("端口号", "port")
    .inputType(InputType.TYPE_CLASS_NUMBER)
    .validator(value -> {
        int port = Integer.parseInt(value);
        return (port >= 1 && port <= 65535) ? null : "端口号范围：1-65535";
    });
```

### 8.5 监听器

```java
builder.onPreferenceChange((ui, item, newValue) -> { ... });  // 值变化
builder.onCreated((ui, screen) -> { ... });                     // 界面创建完成
```

### 8.6 动态操作

```java
// 在 onCreated 回调中
screen.setTitle("动态标题");
PreferenceItem item = screen.findPreference("key");
if (item != null) item.setVisible(false);
screen.recreate();  // 重新创建界面
```

---

## 九、TranslationEngine — 翻译引擎

### 9.1 两种接口

| 接口 | 基类 | 说明 |
|------|------|------|
| `TranslationEngine` | `BaseTranslationEngine` | 单文本翻译，支持分隔线批量优化 |
| `BatchTranslationEngine` | `BaseBatchTranslationEngine` | 原生批量翻译（推荐用于支持数组的 API） |

### 9.2 TranslationEngine 生命周期

```
init(context) → beforeStart() [UI线程] → onStart() [子线程]
→ translate() [子线程, 多次] → onFinish() [子线程] → afterFinish() [UI线程]
// 异常时 → onError() [UI线程]，不再调用 onFinish/afterFinish
```

### 9.3 必须实现的方法

```java
public class MyEngine extends BaseTranslationEngine {
    @Override protected void init() { /* 初始化 */ }

    @Override protected void onBuildConfiguration(ConfigurationBuilder builder) {
        super.onBuildConfiguration(builder);  // autoRepairFormatSpecifiersError = true
        builder.setMaxTranslationTextLength(4000);
        builder.setTargetLanguageMutable(true);
    }

    @NonNull @Override public String name() { return "我的翻译"; }

    @NonNull @Override public List<String> loadSourceLanguages() {
        return List.of("auto", "en", "zh-CN", "ja");
    }

    @NonNull @Override public List<String> loadTargetLanguages(String src) {
        return List.of("en", "zh-CN", "ja");
    }

    @NonNull @Override public String translate(String text, String src, String tgt) throws IOException {
        // 实现翻译逻辑
        return result;
    }

    @Override public boolean onError(Exception e) {
        context.showToastL("错误：" + e.getMessage());
        return true;  // 已处理
    }
}
```

### 9.4 配置项

| 字段 | 默认值 | 说明 |
|------|--------|------|
| `maxTranslationTextLength` | 0 | 单次最大长度，超长自动拆分 |
| `textLengthCalculator` | null | 自定义长度计算器（默认 String.length()） |
| `acceptTranslated` | false | 允许已翻译文本作为输入 |
| `forceNotToSkipTranslated` | false | 隐藏"跳过已翻译"选项 |
| `targetLanguageMutable` | false | 源语言变化时重新加载目标语言 |
| `autoRepairFormatSpecifiersError` | false | 自动修复 %s 等占位符误译（BaseTranslationEngine 默认 true） |
| `allowBatchTranslationBySeparator` | false | 分隔线批量翻译优化 |

### 9.5 BatchTranslationEngine

```java
public class MyBatchEngine extends BaseBatchTranslationEngine {
    @NonNull @Override
    public String[] batchTranslate(String[] texts, String src, String tgt) throws IOException {
        // 返回数组长度必须 = texts.length
        return results;
    }

    @Override public BatchingStrategy createBatchingStrategy() {
        return new DefaultBatchingStrategy(100, 5000);  // 最多100条，5000字符
    }
}
```

**注意**：BatchTranslationEngine 下 `maxTranslationTextLength` 和 `allowBatchTranslationBySeparator` 不生效。

---

## 十、TextEditor — 文本编辑器操作接口

所有文本编辑器扩展功能的基础依赖。

### 10.1 光标与选中

```java
int start = editor.getSelectionStart();   // 已处理，确保 start ≤ end
int end = editor.getSelectionEnd();
int rawStart = editor.getRawSelectionStart();  // 原始值，可能 start > end
boolean hasSelection = editor.hasTextSelected();
editor.setSelection(start, end);
editor.setSelection(position);  // 设置光标
editor.showCursor();
editor.pushSelectionToUndoBuffer();  // 自定义撤销后的光标位置
```

### 10.2 位置与可见

```java
// 行列 ↔ 绝对位置（行列从 0 开始）
int pos = editor.getPositionFromLineColumn(line, column);
int[] lc = editor.getLineColumnFromPosition(pos);  // [line, column]

// 确保可见
editor.ensurePositionVisible(pos);
editor.ensurePositionVisibleInCenter(pos);
editor.ensureSelectionVisible();
editor.ensureSelectionVisibleInCenter();
```

### 10.3 文本操作

```java
BufferedText text = editor.getBufferedText();  // 缓冲文本（高性能）
int len = editor.length();
String sub = editor.subText(start, end);       // 截取
String selected = editor.getSelectedText();     // 获取选中文本
editor.replaceText(start, end, "新文本");       // 替换
editor.insertText(pos, "文本");                 // 插入
editor.deleteText(start, end);                 // 删除

// 大批量编辑
editor.startLargeBatchEditingMode();
// ... 多次编辑 ...
editor.finishLargeBatchEditingMode();
```

### 10.4 文件相关

```java
String fileName = editor.getFileName();
String filePath = editor.getFilePath();
boolean changed = editor.isChanged();
editor.save(resultCallback);  // 异步保存
```

### 10.5 其他

```java
boolean readOnly = editor.isReadOnly();
int tabSize = editor.getTabSize();
boolean indentWithTabs = editor.isIndentWithTabs();
int[] bracketPos = editor.getBracketPositions();  // 括号对位置
int syntaxColor = editor.getHighlightColorAt(pos);
String syntaxName = editor.getSyntaxName();
boolean isEditTextView = editor.isEditTextView();  // 是否输入框模式
editor.showFloatingMenu();
```

---

## 十一、TextEditorFunction — 快捷功能（底部工具栏）

### 11.1 继承基类

```java
public class MyFunction extends BaseTextEditorFunction {
    @NonNull @Override public String name() { return "功能名"; }

    @Override public boolean supportEditTextView() { return true; }   // 是否支持输入框
    @Override public boolean supportRepeat() { return false; }        // 是否支持长按重复

    @Override
    public void doFunction(@NonNull PluginUI ui, @NonNull TextEditor editor, @Nullable JSONObject data) {
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        String selected = editor.subText(start, end);
        // ... 处理逻辑 ...
        editor.replaceText(start, end, result);
    }
}
```

### 11.2 可选：配置界面

```java
@Override
public PluginView buildOptionsView(@NonNull PluginUI ui, @Nullable JSONObject data) {
    return ui.buildVerticalLayout()
        .addEditText("content").text(data).hint("输入内容")
        .addSwitchButton("uppercase").text("转大写").checked(data)
        .build();
}

@Nullable @Override
public JSONObject getOptionsData(@NonNull PluginUI ui, @NonNull PluginView view) {
    PluginEditText edit = view.requireViewById("content");
    if (edit.length() == 0) {
        edit.requestFocus();
        ui.showToast("请输入内容");
        return VALIDATION_FAILED;  // 验证失败，对话框不关闭
    }
    JSONObject data = new JSONObject();
    data.putText(edit);
    data.putChecked(view.requireViewById("uppercase"));
    return data;
}
```

### 11.3 生命周期

`init(context)` → `isEnabled()` → `name()` → `buildOptionsView()` → `getOptionsData()` → `doFunction()`

---

## 十二、TextEditorToolMenu — 工具菜单（顶部编辑菜单）

### 12.1 继承基类

```java
public class MyToolMenu extends BaseTextEditorToolMenu {
    @NonNull @Override public String name() { return "工具名"; }

    @NonNull @Override public Drawable icon() { return MaterialIcons.get("code"); }

    @Override public boolean checkVisible(@NonNull TextEditor editor) { return true; }

    @Override
    public void onMenuClick(@NonNull PluginUI ui, @NonNull TextEditor editor) {
        // 核心逻辑
    }
}
```

### 12.2 图标获取方式

```java
MaterialIcons.get("code");                                    // Material 图标（推荐）
VectorDrawableLoader.fromVectorXml(context, "icon.xml");      // Vector XML
VectorDrawableLoader.fromSvg(context, "icon.svg");            // SVG
// 浏览所有图标：https://mt2.cn/icons
```

### 12.3 与 FloatingMenu 的区别

| 特性 | ToolMenu | FloatingMenu |
|------|----------|-------------|
| 位置 | 顶部工具栏「编辑」菜单 | 选中文本时弹出 |
| 触发 | 打开菜单时调用 checkVisible | 选中文本时调用 checkVisible |
| 适用 | 编码转换、格式化等工具 | 快捷文本操作 |

---

## 十三、TextEditorFloatingMenu — 浮动菜单（选中文本弹出）

### 13.1 继承基类

```java
public class MyFloatingMenu extends BaseTextEditorFloatingMenu {
    @NonNull @Override public String name() { return "大小写反转"; }

    @NonNull @Override public Drawable icon() { return MaterialIcons.get("swap_vert"); }

    @Override public boolean checkVisible(@NonNull TextEditor editor) {
        return editor.hasTextSelected();  // 仅选中文本时显示
    }

    @Override
    public void onMenuClick(@NonNull PluginUI ui, @NonNull TextEditor editor) {
        int from = editor.getSelectionStart();
        int to = editor.getSelectionEnd();
        String selected = editor.subText(from, to);
        editor.replaceText(from, to, processText(selected));
    }
}
```

**注意**：name 应尽量简短以适应屏幕空间。

---

## 十四、Regex — 正则表达式工具

基于 MT 自研正则库（兼容 Java 标准正则），针对 BufferedText 优化。

### 14.1 核心 API

```java
import bin.mt.plugin.api.regex.Regex;
import bin.mt.plugin.api.regex.Pattern;
import bin.mt.plugin.api.regex.Matcher;

// 编译
Pattern p = Regex.compile("\\d+");
Pattern p = Regex.compile("hello", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

// 验证替换模板语法
Regex.checkReplacementTemplate(pattern, "$1-$2");

// 匹配
Matcher m = p.matcher(text);
m.matches();          // 完全匹配
m.find();             // 查找下一个
m.find(startPos);     // 从指定位置查找
m.lookingAt();        // 前缀匹配
m.lookingAt(pos);     // MT 扩展：从指定位置前缀匹配

// 获取结果
m.group();            // 整个匹配
m.group(1);           // 捕获组
m.start(); m.end();   // 位置
m.groupCount();       // 捕获组数量

// 替换
m.replaceAll("替换");
m.replaceFirst("替换");
// 支持 $n, ${n}, ${name}, \l, \u, \L, \U 大小写转换

// 高级替换
StringBuilder sb = new StringBuilder();
while (m.find()) {
    m.appendReplacement(sb, processed);
}
m.appendTail(sb);

// 超时控制（默认 2000ms）
m.setTimeoutMillis(3000);

// 区域控制
m.region(start, end);

// 快照（MT 扩展）
MatcherSnapshot snap = m.toSnapshot();
```

### 14.2 MT 扩展标志

| 标志 | 说明 |
|------|------|
| `MATCH_WHOLE_WORD` | 全词匹配 |

### 14.3 在文本编辑器中使用

```java
BufferedText text = editor.getBufferedText();
Pattern p = Regex.compile("TODO:.*");
Matcher m = text.matcher(p);  // 推荐！直接在缓冲区匹配，避免 toString()
while (m.find()) { ... }
```

**⚠️ 不要用 java.util.regex 处理 BufferedText，会触发 toString() 导致性能问题。**

---

## 十五、JSON 工具

基于 minimal-json，轻量高效，支持 UI 数据双向绑定。

### 15.1 核心类

```java
import bin.mt.plugin.api.json.JSON;
import bin.mt.plugin.api.json.JSONObject;
import bin.mt.plugin.api.json.JSONArray;
import bin.mt.plugin.api.json.JSONValue;

// 创建
JSONObject obj = JSON.object();
JSONArray arr = JSON.array();
JSON.value(42);

// 常量
JSON.NULL / JSON.TRUE / JSON.FALSE

// 解析
JSONObject obj = JSON.parse(str).asObject();
JSONObject obj = new JSONObject(str);
```

### 15.2 JSONObject

```java
// 添加（不检查重复键，性能高）
obj.add("key", "value").add("num", 42);

// 设置（替换已存在的键）
obj.put("key", "newValue");

// 获取
obj.get("key");                    // JSONValue
obj.getString("key", "default");   // 带默认值
obj.getInt("key", 0);
obj.getBoolean("key", false);
obj.getJSONObject("config");
obj.getJSONArray("items");

// 查询
obj.contains("key");
obj.isEmpty();
obj.size();
obj.names();            // List<String>
obj.remove("key");

// 序列化
obj.toString();                              // 最小化
obj.toString(WriterConfig.PRETTY_PRINT);     // 格式化
```

### 15.3 JSONArray

```java
arr.add(1).add("text").add(true);
arr.set(0, 20);          // 替换
arr.get(0);              // JSONValue
arr.getInt(0);
arr.getString(1);
arr.size();
arr.remove(0);
arr.isEmpty();
```

### 15.4 JSONValue 类型判断/转换

```java
value.isObject() / isArray() / isString() / isNumber() / isBoolean() / isNull()
value.asString() / asInt() / asLong() / asDouble() / asFloat() / asBoolean()
value.asObject() / asArray()  // 类型不匹配抛 UnsupportedOperationException
```

### 15.5 UI 数据双向绑定

**UI → JSON（保存）**：
```java
JSONObject data = new JSONObject();
data.putText(editText);              // 用组件 ID 作键名
data.putChecked(checkBox);
data.putSelection(spinner);
data.putCheckedPosition(radioGroup);
data.putCheckedId(radioGroup);
```

**JSON → UI（恢复）**：
```java
.addEditText("username").text(savedData)           // 从 JSON 读取
.addSwitchButton("remember").checked(savedData)
.addSpinner("language").selection(savedData)
.addRadioGroup(true).checkedId(savedData)
```

**⚠️ 组件必须设置 ID，否则抛异常。**

---

## 十六、开发注意事项速查

### 关键规则
1. **所有接口必须在 `build.gradle` 的 `interfaces` 中注册**，否则不生效
2. **minSdk 必须为 21**
3. **需要 MT VIP** 才能安装和运行插件
4. **语言包文件放在 `assets/`**，默认包名为 `strings`
5. **`{key}` 格式在所有 String 参数中自动本地化**
6. **Toast 方法可在非 UI 线程调用**
7. **BufferedText 必须用 MT Regex 库**，不要用 java.util.regex

### 推荐继承基类
| 接口 | 推荐基类 |
|------|---------|
| TranslationEngine | BaseTranslationEngine |
| BatchTranslationEngine | BaseBatchTranslationEngine |
| TextEditorFunction | BaseTextEditorFunction |
| TextEditorToolMenu | BaseTextEditorToolMenu |
| TextEditorFloatingMenu | BaseTextEditorFloatingMenu |

### 官方资源
- Demo 项目：https://gitee.com/L-JINBIN/mt-plugin-v3-demo
- Maven 仓库：https://maven.mt2.cn
- Material 图标浏览：https://mt2.cn/icons
- 官方论坛：https://bbs.binmt.cc
- QQ 交流群：422326626
