# MT 插件开发 API 速查手册

本文档提供 MT 管理器 V3 插件开发的 API 快速参考。

## 插件接口

MT 插件框架提供以下核心接口：

### FloatingMenu - 浮动菜单接口

在文本编辑器浮动菜单中添加自定义选项。

```java
public interface FloatingMenu {
    /**
     * 获取菜单项文本
     * @return 菜单项显示文本
     */
    String getText();
    
    /**
     * 获取菜单项图标
     * @return 图标 Drawable，返回 null 则不显示图标
     */
    Drawable getIcon();
    
    /**
     * 菜单项点击回调
     * @param view 点击的 View
     */
    void onClick(View view);
}
```

**使用示例**:

```java
public class MyFloatingMenu implements FloatingMenu {
    private final Editor editor;
    
    public MyFloatingMenu(Editor editor) {
        this.editor = editor;
    }
    
    @Override
    public String getText() {
        return "我的功能";
    }
    
    @Override
    public Drawable getIcon() {
        return null; // 或返回自定义图标
    }
    
    @Override
    public void onClick(View view) {
        // 处理点击事件
        String selectedText = editor.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            // 处理选中文本
        }
    }
}
```

---

### ToolMenu - 工具菜单接口

在文本编辑器工具栏中添加自定义选项。

```java
public interface ToolMenu {
    /**
     * 获取菜单项文本
     * @return 菜单项显示文本
     */
    String getText();
    
    /**
     * 获取菜单项图标
     * @return 图标 Drawable，返回 null 则不显示图标
     */
    Drawable getIcon();
    
    /**
     * 菜单项点击回调
     * @param view 点击的 View
     */
    void onClick(View view);
}
```

**使用示例**:

```java
public class MyToolMenu implements ToolMenu {
    private final Editor editor;
    
    public MyToolMenu(Editor editor) {
        this.editor = editor;
    }
    
    @Override
    public String getText() {
        return "工具功能";
    }
    
    @Override
    public Drawable getIcon() {
        return null;
    }
    
    @Override
    public void onClick(View view) {
        // 处理点击事件
        String allText = editor.getText();
        // 处理全文
    }
}
```

---

### Preference - 设置界面接口

提供插件设置界面。

```java
public interface Preference {
    /**
     * 创建设置界面
     * @param context Android Context
     * @return 设置界面的根 View
     */
    View onCreate(Context context);
}
```

**使用示例**:

```java
public class MyPreference implements Preference {
    @Override
    public View onCreate(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        // 添加标题
        TextView title = new TextView(context);
        title.setText("插件设置");
        title.setTextSize(18);
        layout.addView(title);
        
        // 添加设置项
        // ...
        
        return layout;
    }
}
```

---

## Editor 对象

Editor 对象提供文本编辑器的操作接口。

### 常用方法

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getText()` | `String` | 获取编辑器全部文本 |
| `getSelectedText()` | `String` | 获取选中的文本 |
| `getSelectionStart()` | `int` | 获取选中起始位置 |
| `getSelectionEnd()` | `int` | 获取选中结束位置 |
| `setText(String text)` | `void` | 设置编辑器文本 |
| `insert(String text)` | `void` | 在光标位置插入文本 |
| `replaceSelection(String text)` | `void` | 替换选中的文本 |
| `getLineCount()` | `int` | 获取行数 |
| `getLine(int line)` | `String` | 获取指定行的文本 |
| `getFilePath()` | `String` | 获取当前文件路径 |

---

## Context 对象

使用 Android Context 进行 UI 操作。

### 常用操作

```java
// 显示 Toast
Toast.makeText(context, "消息", Toast.LENGTH_SHORT).show();

// 显示对话框
new AlertDialog.Builder(context)
    .setTitle("标题")
    .setMessage("消息")
    .setPositiveButton("确定", (dialog, which) -> {
        // 确定按钮点击
    })
    .setNegativeButton("取消", null)
    .show();

// 获取 SharedPreferences
SharedPreferences prefs = context.getSharedPreferences("my_plugin", Context.MODE_PRIVATE);

// 获取资源
Resources res = context.getResources();
```

---

## 插件配置

### build.gradle 配置

```gradle
mtPlugin {
    // 推送目标：auto（自动检测）、mt（MT管理器）、np（NP管理器）
    pushTarget = "auto"
    
    // 插件 ID（唯一标识）
    pluginID = "com.kggzs.cn.mt"
    
    // 版本号（整数，用于版本比较）
    versionCode = 1
    
    // 版本名称（显示给用户）
    versionName = "v1.0.0"
    
    // 插件名称（支持本地化，使用 {key} 引用语言包）
    name = "{plugin_name}"
    
    // 插件描述（支持本地化）
    description = "{plugin_description}"
    
    // 设置界面类（实现 Preference 接口）
    mainPreference = "com.kggzs.cn.mt.MyPreference"
    
    // 功能接口列表
    interfaces = [
        "com.kggzs.cn.mt.MyFloatingMenu",
        "com.kggzs.cn.mt.MyToolMenu"
    ]
}
```

---

## 语言包

### 语言文件格式

语言文件使用 `.mtl` 格式，位于 `src/main/assets/` 目录：

```
strings.mtl        # 默认语言
strings-zh-CN.mtl  # 简体中文
strings-zh-TW.mtl  # 繁体中文
strings-ja.mtl     # 日语
...
```

### 语言文件内容

```mtl
# 注释以 # 开头
plugin_name=我的插件
plugin_description=插件描述
menu_text=菜单文本
```

### 使用语言字符串

在配置中使用 `{key}` 引用语言包：

```gradle
name = "{plugin_name}"
description = "{plugin_description}"
```

---

## 异步处理

插件中的耗时操作应在后台线程执行：

```java
// 使用 Thread
new Thread(() -> {
    // 后台操作
    String result = doHeavyWork();
    
    // 切换到主线程更新 UI
    new Handler(Looper.getMainLooper()).post(() -> {
        updateUI(result);
    });
}).start();
```

---

## 网络请求

使用 HttpURLConnection 进行网络请求：

```java
public String httpRequest(String url, String method, String body) throws Exception {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod(method);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);
    
    // 写入请求体
    try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes("UTF-8"));
    }
    
    // 读取响应
    int responseCode = conn.getResponseCode();
    if (responseCode == 200) {
        try (InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    throw new Exception("HTTP Error: " + responseCode);
}
```

---

## 数据存储

### SharedPreferences

```java
// 保存数据
SharedPreferences prefs = context.getSharedPreferences("my_plugin", Context.MODE_PRIVATE);
prefs.edit()
    .putString("key", "value")
    .putInt("number", 123)
    .putBoolean("flag", true)
    .apply();

// 读取数据
String value = prefs.getString("key", "default");
int number = prefs.getInt("number", 0);
boolean flag = prefs.getBoolean("flag", false);
```

---

## UI 组件

### 创建对话框

```java
public void showCustomDialog(Context context) {
    // 创建自定义布局
    LinearLayout layout = new LinearLayout(context);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(48, 32, 48, 32);
    
    // 添加输入框
    EditText input = new EditText(context);
    input.setHint("请输入内容");
    layout.addView(input);
    
    // 显示对话框
    new AlertDialog.Builder(context)
        .setTitle("自定义对话框")
        .setView(layout)
        .setPositiveButton("确定", (dialog, which) -> {
            String text = input.getText().toString();
            // 处理输入
        })
        .setNegativeButton("取消", null)
        .show();
}
```

### 创建按钮

```java
Button button = new Button(context);
button.setText("点击我");
button.setOnClickListener(v -> {
    // 处理点击
});
```

### 创建列表

```java
String[] items = {"选项1", "选项2", "选项3"};
new AlertDialog.Builder(context)
    .setTitle("选择选项")
    .setItems(items, (dialog, which) -> {
        String selected = items[which];
        // 处理选择
    })
    .show();
```

---

## 常用工具类

### 编码/解码

```java
// Base64
String encoded = Base64.encodeToString(text.getBytes(), Base64.DEFAULT);
String decoded = new String(Base64.decode(encoded, Base64.DEFAULT));

// Hex
String hex = bytesToHex(bytes);
byte[] bytes = hexToBytes(hex);

// URL
String encoded = URLEncoder.encode(text, "UTF-8");
String decoded = URLDecoder.decode(text, "UTF-8");
```

### 哈希计算

```java
// MD5
MessageDigest md = MessageDigest.getInstance("MD5");
byte[] digest = md.digest(text.getBytes("UTF-8"));
String hash = bytesToHex(digest);

// SHA-256
MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
byte[] digest = sha256.digest(text.getBytes("UTF-8"));
```

---

## 注意事项

1. **线程安全**: UI 操作必须在主线程执行
2. **资源释放**: 使用 try-with-resources 确保资源释放
3. **异常处理**: 捕获并处理所有可能的异常
4. **权限**: 在 AndroidManifest.xml 中声明必要权限
5. **兼容性**: 注意 Android 版本兼容性

---

## 示例项目结构

```
my-plugin/
├── src/main/
│   ├── java/com/kggzs/cn/mt/
│   │   ├── MyFloatingMenu.java    # 浮动菜单实现
│   │   ├── MyToolMenu.java        # 工具菜单实现
│   │   └── MyPreference.java      # 设置界面实现
│   ├── assets/
│   │   ├── strings.mtl            # 默认语言
│   │   └── strings-zh-CN.mtl      # 简体中文
│   ├── resources/
│   │   └── icon.png               # 插件图标
│   └── AndroidManifest.xml
├── build.gradle
└── README.md
```

---

<div align="center">

Made with ❤️ by 康哥

</div>
