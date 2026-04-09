# MT 管理器 V3 插件 Demo 功能演示集

> 本文档详细说明了 demo 模块中所有示例代码的功能和实现方式，帮助开发者快速理解插件 API 的各种用法。

## 目录

- [一、插件上下文（PluginContext）](#一插件上下文plugincontext)
- [二、UI 组件（PluginUI）](#二ui-组件pluginui)
- [三、对话框（PluginDialog）](#三对话框plugindialog)
- [四、弹出菜单（PluginPopupMenu）](#四弹出菜单pluginpopupmenu)
- [五、设置界面（PluginPreference）](#五设置界面pluginpreference)
- [六、翻译引擎（TranslationEngine）](#六翻译引擎translationengine)
- [七、批量翻译引擎（BatchTranslationEngine）](#七批量翻译引擎batchtranslationengine)
- [八、文本编辑器快捷功能（TextEditorFunction）](#八文本编辑器快捷功能texteditorfunction)
- [九、文本编辑器浮动菜单（TextEditorFloatingMenu）](#九文本编辑器浮动菜单texteditorfloatingmenu)
- [十、文本编辑器工具菜单（TextEditorToolMenu）](#十文本编辑器工具菜单texteditortoolmenu)

---

## 一、插件上下文（PluginContext）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/examples/ExampleContext.java`

### 1.1 基本信息

展示如何获取插件基本信息：

```java
// 获取 SDK 版本、插件 ID、版本号等
PluginContext.SDK_VERSION
context.getPluginId()
context.getPluginVersionCode()
context.getPluginVersionName()
context.getLanguageCountry()
```

### 1.2 获取本地化文本

演示多种获取本地化文本的方式：

```java
// 从默认语言包获取
context.getString("{key}")

// 直接通过 key 获取（无需花括号）
context.getString("key")

// 从自定义语言包获取
context.getString("{example:key}")
```

### 1.3 读取 assets 内文件

演示如何读取插件 assets 目录中的文件：

```java
try (InputStream is = context.getAssetsAsStream("strings.mtl")) {
    // 读取文件内容
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int len;
    while ((len = is.read(buf)) != -1) {
        baos.write(buf, 0, len);
    }
    String text = baos.toString("UTF-8");
}
```

### 1.4 打开浏览器

```java
// 打开系统浏览器
context.openBrowser("https://mt2.cn");

// 打开 MT 内置浏览器
context.openBuiltinBrowser("https://mt2.cn", false);
```

### 1.5 剪贴板操作

```java
// 检查是否有剪贴板内容
context.hasClipboardText()

// 获取剪贴板文本
context.getClipboardText()

// 设置剪贴板文本
context.setClipboardText("abc")
```

### 1.6 Toast 消息

演示三种 Toast 显示方式：

```java
// 短时间 Toast
context.showToast("{key}")

// 长时间 Toast（支持格式化参数）
context.showToastL("{key}=%s\ntime=%d", "{key}", System.currentTimeMillis())

// 取消当前 Toast
context.cancelToast()
```

### 1.7 日志操作

```java
// 写出普通日志
context.log("这是一条日志")

// 写出错误日志
context.log("这是一条错误日志", new Exception())

// 打开日志查看器
context.openLogViewer()
```

---

## 二、UI 组件（PluginUI）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/examples/ExampleUI.java`

### 2.1 布局系统

#### 垂直布局（VerticalLayout）

从上往下布局，适合表单等场景：

```java
pluginUI.buildVerticalLayout()
    .addTextView().text("第一行")
    .addTextView().text("第二行")
    .addTextView().text("第三行")
    .build()
```

#### 水平布局（HorizontalLayout）

从左往右布局：

```java
pluginUI.buildHorizontalLayout()
    .addTextView().text("第一列")
    .addTextView().text("第二列").textColor(Color.RED)
    .addTextView().text("第三列").textColor(Color.GREEN)
    .build()
```

#### 对齐方式

设置线性布局对齐方式和子视图单独对齐：

```java
pluginUI.buildVerticalLayout()
    .gravity(Gravity.CENTER_HORIZONTAL)  // 整体居中
    .addTextView().text("第一行")
    .addTextView().text("第二行").layoutGravity(Gravity.START)  // 单独左对齐
    .addTextView().text("第三行")
    .addTextView().text("第四行").layoutGravity(Gravity.END)  // 单独右对齐
    .build()
```

#### 组合布局

垂直布局嵌套水平布局，实现复杂布局：

```java
pluginUI.buildVerticalLayout()
    .addHorizontalLayout().children(subBuilder -> subBuilder
        .addTextView().text("1-1").backgroundColor(0xFFFF5555)
        .addTextView().text("1-2").backgroundColor(0xFF55FF55)
        .addTextView().text("1-3").backgroundColor(0xFF5555FF)
    )
    .addHorizontalLayout().children(subBuilder -> subBuilder
        .addTextView().text("2-1")
        // ...
    )
    .build()
```

#### 统一宽度

让多个 View 保持相同宽度（以最宽者为准）：

```java
pluginUI.buildVerticalLayout()
    .addHorizontalLayout().children(subBuilder -> subBuilder
        .addTextView("text1").text("用户名")
        .addEditText()
    )
    .addHorizontalLayout().children(subBuilder -> subBuilder
        .addTextView("text2").text("密码")
        .addEditText()
    )
    .unifyWidth("text1", "text2")  // 统一宽度
    .build()
```

#### 帧布局（FrameLayout）

子视图叠加显示，支持对齐方式：

```java
pluginUI.buildFrameLayout()
    .addTextView().text("默认")
    .addTextView().text("局中").layoutGravity(Gravity.CENTER)
    .addTextView().text("靠右").layoutGravity(Gravity.END)
    .build()
```

### 2.2 基本组件

#### 文本视图（PluginTextView）

支持丰富的文本属性设置：

```java
pluginUI.buildVerticalLayout()
    // 基础文本
    .addTextView().text("① 这是一个文本")
    
    // 字体颜色和背景色
    .addTextView().text("② 设置了字体颜色和背景色")
        .textColor(Color.WHITE).backgroundColor(Color.BLACK)
    
    // 文本对齐方式
    .addTextView().text("③ 文本右对齐")
        .textGravity(Gravity.END).widthMatchParent()
    
    // 内边距和外边距
    .addTextView().text("④ 设置了内边距").paddingDp(24)
    .addTextView().text("⑤ 设置了外边距").marginDp(24)
    
    // 字号设置
    .addTextView().text("⑥ 字号48").textSize(48)
    .addTextView().text("⑦ 字号12").textSize(12)
    
    // 字体样式（粗体、斜体）
    .addTextView().text("⑧ 粗斜体").textStyle(true, true)
    
    // 等宽字体
    .addTextView("mono").text("⑨ 等宽体 |W|I|")
        .typeface(Typeface.MONOSPACE)
    
    // 富文本（SpannableString）
    .addTextView().text(new SpannableString("⑩ 富文本") {{
        setSpan(new RelativeSizeSpan(2), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setSpan(new ForegroundColorSpan(pluginUI.colorError()), 3, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setSpan(new StyleSpan(Typeface.BOLD), 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }})
    
    // 可点击文本
    .addTextView().text("⑪ 点我试试")
        .paddingVerticalDp(8)
        .background(pluginUI.selectableItemBackground())
        .onClick(view -> {
            pluginUI.getContext().setClipboardText(((PluginTextView) view).getText());
        })
    .build()
```

#### 多行文本

```java
pluginUI.buildVerticalLayout()
    // 多行文本
    .addTextView().text("这是一个多行文本\n这是一个多行文本")
    
    // 限制最多行数
    .addTextView().text("限制最多2行\n限制最多2行").maxLines(2)
    
    // 超出显示省略号
    .addTextView().text("限制最多2行\n超出部分显示省略号")
        .maxLines(2).ellipsize(TextUtils.TruncateAt.END)
    
    // 固定显示行数
    .addTextView().text("设置显示高度为3行").lines(3)
    
    // 调整行间距
    .addTextView().text("行间距大一点\n行间距大一点")
        .lineSpacing(0, 1.5f)
    .build()
```

**lines(1) 与 singleLine() 的区别**：
- `lines(1)`: 只显示一行，但文本仍是多行模式
- `singleLine()`: 真正的单行模式，文本不会换行

#### 图片视图（PluginImageView）

支持 Material 图标和颜色过滤：

```java
pluginUI.defaultStyle(new PluginUI.StyleWrapper() {
    @Override
    protected void handleImageView(PluginUI pluginUI, PluginImageViewBuilder builder) {
        builder.background(pluginUI.selectableItemBackgroundBorderless())
            .paddingDp(6)
            .colorFilter(pluginUI.colorText())
            .onClick(view -> pluginUI.showToast(view.getId()));
    }
})
.buildVerticalLayout()
.addHorizontalLayout().children(subBuilder -> subBuilder
    .addImageView("search_icon").image(MaterialIcons.get("search"))
    .addView().layoutWeight(1)  // 占位
    .addImageView("code_icon").image(MaterialIcons.get("code"))
    .addView().layoutWeight(1)
    .addImageView("copy_icon").image(MaterialIcons.get("content_copy"))
)
.build()
```

#### 普通按钮（PluginButton）

支持多种样式和点击/长按事件：

```java
pluginUI.defaultStyle(new PluginUI.StyleWrapper() {
    @Override
    protected void handleButton(PluginUI pluginUI, PluginButtonBuilder builder) {
        super.handleButton(pluginUI, builder);
        builder.widthMatchParent();
        builder.onClick(view -> context.showToast("点击了 " + ((PluginButton) view).getText()));
        builder.onLongClick(view -> {
            context.showToast("长按了 " + ((PluginButton) view).getText());
            return true;
        });
    }
})
.buildVerticalLayout()
.addButton().text("default").style(PluginButton.Style.DEFAULT)
.addButton().text("filled").style(PluginButton.Style.FILLED)
.addButton().text("outlined").style(PluginButton.Style.OUTLINED)
// 关闭按钮强制大写
.addButton().text("default").allCaps(false)
.build()
```

#### 单选按钮（PluginRadioButton 与 PluginRadioGroup）

两种定位方式：position 和 id：

```java
pluginUI.buildVerticalLayout()
    // 方式1：使用 position 定位
    .addRadioGroup("groupPos", true).children(subBuilder -> subBuilder
        .addRadioButton().text("选项0")
        .addRadioButton().text("选项1")
        .addRadioButton().text("选项2")
    ).check(1)  // 按位置选中
    .postOnCheckedChanged((group, checkedButton, checkedPosition) -> 
        context.showToast("选中了position " + checkedPosition))
    
    // 方式2：使用 id 定位
    .addRadioGroup("groupId", false).children(subBuilder -> subBuilder
        .addRadioButton().id("radio0").text("选项0")
        .addRadioButton().id("radio1").text("选项1")
        .addRadioButton().id("radio2").text("选项2")
    ).check("radio1")  // 按id选中
    
    // 方式3：手动实现单选逻辑
    .addHorizontalLayout().children(subBuilder -> subBuilder
        .addRadioButton().id("radio-m0").text("选项0")
        .addRadioButton().id("radio-m1").text("选项1").check()
    )
    .build()
```

**手动单选逻辑**：

```java
PluginCompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
    if (!isChecked) return;
    for (PluginRadioButton radioButton : radioButtons) {
        if (radioButton != buttonView && radioButton.isChecked()) {
            radioButton.setChecked(false);
        }
    }
};
```

#### 开关按钮（PluginSwitchButton）

```java
pluginUI.buildVerticalLayout()
    .addSwitchButton("switch1").text("① 开关1").widthMatchParent()
    .addSwitchButton("switch2").text("② 开关2").check()
    .addSwitchButton("switch3").text("③ 事件监听")
        .onCheckedChange((buttonView, isChecked) -> 
            context.showToast("isChecked=" + isChecked))
    .addButton("button").text("获取状态").onClick(view -> {
        PluginViewGroup rootView = view.getRootView();
        PluginSwitchButton switch1 = rootView.requireViewById("switch1");
        // 获取状态...
    })
    .build()
```

#### 多选框（PluginCheckBox）

```java
pluginUI.buildVerticalLayout()
    .addCheckBox("check1").text("① 多选框1").check()
    .addCheckBox("check2").text("② 多选框2")
    .addCheckBox("check3").text("③ 事件监听")
        .onCheckedChange((buttonView, isChecked) -> 
            context.showToast("isChecked=" + isChecked))
    .build()
```

#### 进度条（PluginProgressBar）

```java
pluginUI.buildVerticalLayout()
    // 确定进度的水平进度条
    .addTextView().text("确定进度的水平进度条")
    .addProgressBar().progress(75)  // 默认最大100
    .addProgressBar().progress(66).secondaryProgress(132).maxProgress(200)
    
    // 不确定进度的水平进度条
    .addTextView().text("不确定进度的水平进度条")
    .addProgressBar().indeterminate(true)
    
    // 圆形进度条
    .addTextView().text("不确定进度的圆形进度条")
    .addHorizontalLayout().gravity(Gravity.CENTER_VERTICAL).children(subBuilder -> subBuilder
        .addProgressBar().style(PluginProgressBar.Style.CIRCULAR_SMALL).width(0).layoutWeight(1)
        .addProgressBar().style(PluginProgressBar.Style.CIRCULAR).width(0).layoutWeight(1)
        .addProgressBar().style(PluginProgressBar.Style.CIRCULAR_LARGE).width(0).layoutWeight(1)
    )
    .build()
```

#### 下拉选择框（PluginSpinner）

```java
pluginUI.buildVerticalLayout()
    .addSpinner("spinner").widthMatchParent()
        .items(Arrays.asList("选项1", "选项2", "选项3"))
        .selection(1)  // 默认选中第2项
        .onItemSelected((spinner, position) -> 
            context.showToast("选中了 " + spinner.getItem(position)))
    .build()
```

### 2.3 输入框（PluginEditText）

#### 两种风格

```java
pluginUI.buildVerticalLayout()
    // 普通风格（默认单行）
    .addEditText().text("普通风格输入框 - 默认单行模式")
    
    // Box 风格（默认多行）
    .addEditBox().text("Box风格输入框 - 默认多行模式").lines(5)
    .build()
```

#### 提示内容（Hint）

```java
pluginUI.buildVerticalLayout()
    .addTextView().text("设置 hint 在编辑框内容为空时指导用户输入")
    .addEditText().hint("请输入内容")
    .addEditBox().hint("请输入多行内容\n支持换行")
    .build()
```

#### 输入类型（InputType）

```java
pluginUI.buildVerticalLayout()
    // 纯文本（默认）
    .addEditText().text("纯文本").inputTypeText()
    
    // 密码
    .addEditText().text("123456").inputTypePassword()
    
    // 邮箱
    .addEditText().hint("邮箱地址").inputTypeEmail()
    
    // 数字
    .addEditText().hint("整数").inputTypeNumber()
    
    // 电话号码
    .addEditText().hint("电话号码").inputTypePhone()
    
    // 小数
    .addEditText().hint("小数").inputTypeNumberDecimal()
    .build()
```

#### 单行与多行

```java
pluginUI.buildVerticalLayout()
    // 单行模式
    .addTextView().text("单行模式")
    .addEditText().text("单行文本").singleLine(true)
    .addEditText().text("单行文本").maxLines(1)
    
    // 多行模式
    .addTextView().text("多行模式")
    .addEditBox().text("多行文本\n多行文本").singleLine(false)
    .addEditBox().text("固定5行").lines(5)
    .addEditBox().text("最少3行，最多10行").minLines(3).maxLines(10)
    .build()
```

#### 自动换行

```java
pluginUI.buildVerticalLayout()
    // 按词换行（英文单词不会断开）
    .addEditBox().text("Keep word together").softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
    
    // 按字符换行（默认）
    .addEditBox().text("按字符换行").softWrap(PluginEditText.SOFT_WRAP_ANY_CHAR)
    
    // 不换行
    .addEditBox().text("不换行").softWrap(PluginEditText.NO_SOFT_WRAP)
    .build()
```

#### 只读模式

```java
pluginUI.buildVerticalLayout()
    .addEditBox().text("这是一个只读的编辑框").readOnly(true)
    .build()
```

#### 语法高亮

```java
pluginUI.buildVerticalLayout()
    .addEditBox().text("public class Hello {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World\");\n    }\n}")
        .syntaxHighlight("Java")
    .addEditBox().text("# Python 代码\ndef hello():\n    print('Hello World')")
        .syntaxHighlight("Python")
    .build()
```

#### 文本变化监听

```java
editText.addTextChangedListener(new PluginEditTextWatcher.Simple() {
    @Override
    public void afterTextChanged(PluginEditText editText, Editable s) {
        // 文本变化后执行
    }
    
    @Override
    public void beforeTextChanged(PluginEditText editText, CharSequence s, int start, int count, int after) {
        // 文本变化前执行
    }
});
```

#### 获取焦点并弹出输入法

```java
editText.requestFocusAndShowIME();
```

### 2.4 样式系统

通过 `defaultStyle()` 设置全局默认样式：

```java
pluginUI.defaultStyle(new PluginUI.StyleWrapper() {
    @Override
    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
        super.handleTextView(pluginUI, builder);  // 先应用当前样式
        builder.textSize(14).textColor(pluginUI.colorText());  // 再自定义
    }
    
    @Override
    protected void handleButton(PluginUI pluginUI, PluginButtonBuilder builder) {
        super.handleButton(pluginUI, builder);
        builder.widthMatchParent();
    }
});
```

**样式复用**：

```java
// 定义基础样式
PluginUI.Style style = new PluginUI.StyleWrapper() {
    @Override
    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
        builder.textColor(0xFF000000).backgroundColor(0xFFAAAAAA);
    }
};

// 继承并修改
PluginUI.Style extendedStyle = new PluginUI.StyleWrapper(style) {
    @Override
    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
        super.handleTextView(pluginUI, builder);
        builder.textSize(14);
    }
};
```

### 2.5 视图查找

通过 ID 查找视图：

```java
PluginView view = pluginUI.buildVerticalLayout()
    .addTextView("title").text("标题")
    .addEditText("input").hint("输入")
    .build();

// 查找视图（找不到返回 null）
PluginTextView title = view.findViewById("title");

// 查找视图（找不到抛出异常）
PluginTextView title2 = view.requireViewById("title");

// 获取根视图
PluginViewGroup rootView = anyView.getRootView();
```

---

## 三、对话框（PluginDialog）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/examples/ExampleDialog.java`

### 3.1 基本对话框

#### 文字消息

```java
pluginUI.buildDialog()
    .setTitle("文字消息")
    .setMessage("消息内容")
    .setPositiveButton("{close}", null)
    .show()
```

#### 列表项目

```java
CharSequence[] items = {"项目0", "项目1", "项目2", "项目3"};

pluginUI.buildDialog()
    .setTitle("列表项目")
    .setItems(items, (dialog, which) -> {
        context.showToast("点击了" + items[which]);
    })
    .show()
```

#### 单选列表

```java
int[] selection = {1};  // 记录选中位置

pluginUI.buildDialog()
    .setTitle("单选列表")
    .setSingleChoiceItems(items, selection[0], (dialog, which) -> {
        selection[0] = which;  // 更新选中位置
    })
    .setPositiveButton("{ok}", (dialog, which) -> {
        context.showToast("选中了" + items[selection[0]]);
    })
    .show()
```

#### 多选列表

```java
boolean[] checked = {false, true, false, true};  // 记录选中状态

pluginUI.buildDialog()
    .setTitle("多选列表")
    .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> {
        checked[which] = isChecked;  // 更新选中状态
    })
    .setPositiveButton("{ok}", (dialog, which) -> {
        StringBuilder sb = new StringBuilder("选中了:");
        for (int i = 0; i < checked.length; i++) {
            if (checked[i]) {
                sb.append(' ').append(items[i]);
            }
        }
        context.showToast(sb);
    })
    .show()
```

#### 自定义视图

```java
PluginView pluginView = pluginUI.buildVerticalLayout()
    .addEditText("input").build();

PluginEditText input = pluginView.requireViewById("input");
input.requestFocusAndShowIME();  // 弹出输入法

pluginUI.buildDialog()
    .setTitle("自定义View")
    .setView(pluginView)
    .setPositiveButton("{ok}", (dialog, which) -> {
        String text = input.getText().toString();
        context.showToast("输入了: " + text);
    })
    .show()
```

### 3.2 对话框按钮

对话框最多支持 3 个按钮：

```java
pluginUI.buildDialog()
    .setTitle("对话框按钮")
    .setMessage("对话框一共可以设置3个按钮")
    .setPositiveButton("按钮1", (dialog, which) -> {
        // which == PluginDialog.BUTTON_POSITIVE
        context.showToast("点击了按钮1");
    })
    .setNegativeButton("按钮2", (dialog, which) -> {
        // which == PluginDialog.BUTTON_NEGATIVE
        context.showToast("点击了按钮2");
    })
    .setNeutralButton("按钮3", (dialog, which) -> {
        // which == PluginDialog.BUTTON_NEUTRAL
        context.showToast("点击了按钮3");
    })
    .show()
```

**点击按钮后对话框不消失**：

```java
PluginDialog dialog = pluginUI.buildDialog()
    .setTitle("对话框按钮")
    .setPositiveButton("点我", null)
    .show();

int[] count = {0};
dialog.getPositiveButton().setOnClickListener(view -> {
    context.showToast("点击10次后对话框消失 [" + (++count[0]) + "]");
    if (count[0] == 10) {
        dialog.dismiss();
    }
});
```

### 3.3 对话框行为控制

#### 不可取消

```java
pluginUI.buildDialog()
    .setTitle("对话框不可取消")
    .setMessage("点击对话框外部或按下返回键，对话框不会消失")
    .setCancelable(false)
    .setPositiveButton("{close}", null)
    .show()
```

### 3.4 事件监听

#### 显示事件

```java
pluginUI.buildDialog()
    .setTitle("显示事件监听器")
    .setOnShowListener(dialog -> context.showToast("对话框显示"))
    .show()
```

#### 消失事件

```java
pluginUI.buildDialog()
    .setTitle("消失事件监听器")
    .setOnDismissListener(dialog -> context.showToast("对话框消失"))
    .show()
```

#### 取消事件

```java
pluginUI.buildDialog()
    .setTitle("取消事件监听器")
    .setOnCancelListener(dialog -> context.showToast("对话框取消"))
    .show()
```

#### 按键事件

```java
pluginUI.buildDialog()
    .setTitle("按键事件监听器")
    .setCancelable(false)  // 先设置不可取消
    .setOnKeyListener((dialog, keyCode, event) -> {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            context.showToast("按下了返回键");
            dialog.cancel();
            return true;  // 事件已处理
        }
        return false;
    })
    .show()
```

### 3.5 通用封装对话框

#### LoadingDialog（加载对话框）

```java
LoadingDialog loadingDialog = new LoadingDialog(pluginUI)
    .setMessage("{processing}")
    .setSecondaryMessage("10 秒后消失，或者按返回键取消")
    .setCancelable()
    .setOnCancelListener(dialog -> {
        pluginUI.showToast("已取消");
        dialog.dismiss();
    })
    .show();

new Thread(() -> {
    for (int i = 9; i >= 0; i--) {
        SystemClock.sleep(1000);
        loadingDialog.setSecondaryMessage(i + " 秒后消失");
        if (loadingDialog.isCanceled()) {
            return;
        }
    }
    pluginUI.showToast("处理完成");
    loadingDialog.dismiss();
}).start();
```

#### ProgressDialog（进度对话框）

```java
ProgressDialog progressDialog = new ProgressDialog(pluginUI)
    .setTitle("{processing}")
    .setMessage("10 秒后消失")
    .setCancelable()
    .show();

new Thread(() -> {
    for (int i = 1; i <= 80; i++) {
        progressDialog.setProgress(i * 100 / 80);
        SystemClock.sleep(100);
        if (progressDialog.isCanceled()) {
            return;
        }
    }
    // 最后改为不确定进度模式
    progressDialog.setIndeterminate();
    SystemClock.sleep(2000);
    progressDialog.dismiss();
}).start();
```

#### DualProgressDialog（双进度对话框）

适用于子任务+总任务的场景：

```java
DualProgressDialog progressDialog = new DualProgressDialog(pluginUI)
    .setTitle("{processing}")
    .setMessage("10 秒后消失")
    .setCancelable()
    .show();

new Thread(() -> {
    for (int i = 0; i < 4; i++) {
        for (int j = 1; j <= 100; j++) {
            progressDialog.setSubProgress(j);  // 子任务进度
            progressDialog.setTotalProgress(i * 25 + j / 4);  // 总进度
            SystemClock.sleep(25);
            if (progressDialog.isCanceled()) {
                return;
            }
        }
    }
    pluginUI.showToast("处理完成");
    progressDialog.dismiss();
}).start();
```

---

## 四、弹出菜单（PluginPopupMenu）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/examples/ExamplePopupMenu.java`

### 4.1 基本用法

```java
PluginView.OnClickListener listener = button -> {
    // 创建弹出菜单
    PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
    PluginMenu menu = popupMenu.getMenu();
    
    // 添加菜单项
    menu.add("{menu1}", "菜单1");  // 指定 id 和标题
    menu.add("{menu2}");  // id 和标题相同（来自语言包）
    menu.add("菜单3");  // 只有标题
    
    popupMenu.setOnMenuItemClickListener(commonListener);
    popupMenu.show();
};
```

### 4.2 菜单图标

使用 Material 图标：

```java
dialog.getNeutralButton().setOnClickListener(view -> {
    PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
    PluginMenu menu = popupMenu.getMenu();
    
    menu.add("{menu1}").setIcon(MaterialIcons.get("search"));
    menu.add("{menu2}").setIcon(MaterialIcons.get("content_copy"));
    menu.add("{menu3}").setIcon(MaterialIcons.get("content_cut"));
    menu.add("{menu4}").setIcon(MaterialIcons.get("delete"));
    
    popupMenu.show();
});
```

### 4.3 多选菜单

```java
Set<String> checkedItems = new HashSet<>();

dialog.getNeutralButton().setOnClickListener(view -> {
    PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
    PluginMenu menu = popupMenu.getMenu();
    
    menu.add("{menu1}").setCheckable(true).setChecked(checkedItems.contains("{menu1}"));
    menu.add("{menu2}").setCheckable(true).setChecked(checkedItems.contains("{menu2}"));
    menu.add("{menu3}").setCheckable(true).setChecked(checkedItems.contains("{menu3}"));
    
    popupMenu.setOnMenuItemClickListener(menuItem -> {
        if (menuItem.isChecked()) {
            checkedItems.remove(menuItem.getItemId());
            menuItem.setChecked(false);
        } else {
            checkedItems.add(menuItem.getItemId());
            menuItem.setChecked(true);
        }
        return true;
    });
    
    popupMenu.show();
});
```

### 4.4 单选菜单

使用分组实现单选：

```java
String[] checkId = new String[]{"{menu1}"};

dialog.getNeutralButton().setOnClickListener(view -> {
    PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
    PluginMenu menu = popupMenu.getMenu();
    
    // 添加到同一分组
    menu.add("{menu1}", "{menu1}", "group0").setChecked(checkId[0].equals("{menu1}"));
    menu.add("{menu2}", "{menu2}", "group0").setChecked(checkId[0].equals("{menu2}"));
    menu.add("{menu3}", "{menu3}", "group0").setChecked(checkId[0].equals("{menu3}"));
    
    // 设置分组为单选
    menu.setGroupCheckable("group0", true, true);
    
    popupMenu.setOnMenuItemClickListener(menuItem -> {
        menuItem.setChecked(true);
        checkId[0] = menuItem.getItemId();
        return true;
    });
    
    popupMenu.show();
});
```

### 4.5 多级菜单（子菜单）

```java
dialog.getNeutralButton().setOnClickListener(view -> {
    PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
    PluginMenu menu = popupMenu.getMenu();
    
    // 添加子菜单
    PluginSubMenu subMenu1 = menu.addSubMenu("{menu1}");
    subMenu1.add("sub1_0", "{item0}");
    subMenu1.add("sub1_1", "{item1}");
    subMenu1.add("sub1_2", "{item2}");
    
    PluginSubMenu subMenu2 = menu.addSubMenu("{menu2}");
    subMenu2.add("sub2_0", "{item0}");
    subMenu2.add("sub2_1", "{item1}");
    
    // 添加普通菜单项
    menu.add("{menu3}");
    menu.add("{menu4}");
    
    popupMenu.show();
});
```

### 4.6 分割线

不同 group 之间会自动显示分割线：

```java
dialog.getNeutralButton().setOnClickListener(view -> {
    PluginPopupMenu popupMenu = pluginUI.createPopupMenu(view);
    PluginMenu menu = popupMenu.getMenu();
    
    menu.add("{menu1}");
    menu.add("{menu2}");
    menu.add("{menu3}", "{menu3}", "group1");  // 新分组
    menu.add("{menu4}", "{menu4}", "group1");
    
    menu.setGroupDividerEnabled(true);  // 启用分组分割线
    
    popupMenu.show();
});
```

---

## 五、设置界面（PluginPreference）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/examples/ExamplePreference.java`

### 5.1 基本设置项

#### 纯文本

```java
builder.addText("纯文本")
    .summary("单纯用来显示文字");
```

#### 链接文本

```java
builder.addText("链接文本")
    .summary("除了显示文字，点击还能打开网址")
    .url("https://bbs.binmt.cc");
```

#### 输入框

```java
builder.addInput("选项-输入内容", "key_input")
    .summary("请输入内容")
    .hint("提示内容")
    .valueAsSummary()  // 使用输入值作为摘要
    .defaultValue("默认值");
```

#### 单选列表

```java
builder.addList("选项-单选列表", "key_list")
    .summary("未选中任何项目")
    .addItem("项目1", "1").summary("选中了选项1")
    .addItem("项目2", "2").summary("选中了选项2");
```

#### 开关

```java
builder.addSwitch("选项-开关", "key_switch")
    .defaultValue(true)
    .summaryOn("开")
    .summaryOff("关");
```

### 5.2 自定义点击事件

#### 自定义点击

```java
AtomicInteger count = new AtomicInteger();
builder.addText("自定义点击", "custom_click")
    .summary("点击了 0 次")
    .onClick((pluginUI, item) -> 
        item.setSummary("点击了 " + count.incrementAndGet() + " 次"));
```

#### 自定义对话框

```java
SharedPreferences preferences = context.getPreferences();

builder.addText("自定义对话框", "custom_input")
    .summary("点击试试")
    .onClick((pluginUI, item) -> {
        // 创建输入框
        PluginView view = pluginUI.buildVerticalLayout()
            .addEditText("input").text(preferences.getString(item.getKey(), null))
                .selectAll()
            .build();
        PluginEditText input = view.requireViewById("input");
        
        // 获取焦点并弹出输入法
        input.requestFocusAndShowIME();
        
        // 创建对话框
        pluginUI.buildDialog()
            .setTitle("自定义对话框")
            .setView(view)
            .setPositiveButton("确定", (dialog, which) -> {
                String text = input.getText().toString();
                preferences.edit().putString(item.getKey(), text).apply();
                item.setSummary("您输入了：" + text);
            })
            .setNegativeButton("取消", null)
            .show();
    });
```

### 5.3 监听选项变化

```java
// 监听用户改变选项事件
builder.onPreferenceChange((pluginUI, preferenceItem, newValue) -> {
    PreferenceScreen preferenceScreen = preferenceItem.getPreferenceScreen();
    switch (preferenceItem.getKey()) {
        case "enable_custom" -> {
            boolean enable = (boolean) newValue;
            preferenceScreen.requireHeader("custom_header").setEnabled(enable);
            preferenceScreen.requirePreference("custom_click").setEnabled(enable);
        }
        case "listen_input" -> {
            pluginUI.showToast("输入内容：" + newValue);
        }
    }
});
```

### 5.4 初始化回调

```java
builder.onCreated((pluginUI, preferenceScreen) -> {
    boolean enable = preferences.getBoolean("custom_header", true);
    preferenceScreen.requireHeader("custom_header").setEnabled(enable);
    preferenceScreen.requirePreference("custom_click").setEnabled(enable);
});
```

### 5.5 拦截点击事件

#### 简单拦截

```java
builder.addSwitch("开启拦截", "enable_intercept")
    .summary("开启后点击下面的开关试试")
    .defaultValue(true);

builder.addSwitch("我被拦截了吗", "be_intercepted")
    .summary("T T")
    .interceptClick((pluginUI, item) -> {
        if (preferences.getBoolean("enable_intercept", true)) {
            pluginUI.showToast("我被拦截了");
            return true;  // 拦截默认行为
        } else {
            pluginUI.showToast("我没有被拦截");
            return false;  // 不拦截
        }
    });
```

#### 确认对话框

```java
builder.addSwitch("危险选项", "dangerous_option")
    .summary("更改此选项前需要手动确认")
    .interceptClick((pluginUI, item) -> {
        pluginUI.buildDialog()
            .setTitle("警告")
            .setMessage("确定要修改此选项吗？")
            .setPositiveButton("{ok}", (dialog, which) -> {
                // 用户确认后，手动切换开关状态
                SharedPreferences prefs = context.getPreferences();
                boolean current = prefs.getBoolean("dangerous_option", false);
                prefs.edit().putBoolean("dangerous_option", !current).apply();
                // 刷新界面
                item.getPreferenceScreen().recreate();
            })
            .setNegativeButton("{cancel}", null)
            .show();
        return true;  // 拦截默认行为
    });
```

---

## 六、翻译引擎（TranslationEngine）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/TranslationEngineDemo.java`

### 6.1 基本实现

继承 `BaseTranslationEngine` 实现简单翻译引擎：

```java
public class TranslationEngineDemo extends BaseTranslationEngine {
    
    /**
     * 翻译引擎名称
     */
    @NonNull
    @Override
    public String name() {
        return "{case_conversion}";  // 支持本地化文本
    }
    
    /**
     * 源语言代码列表
     */
    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        return List.of("src");
    }
    
    /**
     * 目标语言代码列表
     */
    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return List.of("upper", "lower");
    }
    
    /**
     * 将语言代码转为可视化名称
     */
    @NonNull
    @Override
    public String getLanguageDisplayName(String language) {
        return switch (language) {
            case "src" -> "原文";
            case "upper" -> "大写";
            case "lower" -> "小写";
            default -> "???";
        };
    }
    
    /**
     * 翻译方法（子线程执行）
     */
    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (targetLanguage.equals("upper"))
            return text.toUpperCase();  // 转为大写
        else
            return text.toLowerCase();  // 转为小写
    }
}
```

### 6.2 配置选项

通过构造函数配置翻译引擎行为：

```java
public TranslationEngineDemo() {
    super(new ConfigurationBuilder()
        // 关闭「跳过已翻译词条」
        .setForceNotToSkipTranslated(true)
        .build());
}
```

---

## 七、批量翻译引擎（BatchTranslationEngine）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/GoogleTranslationEngine.java`

### 7.1 基本实现

继承 `BaseBatchTranslationEngine` 实现批量翻译：

```java
public class GoogleTranslationEngine extends BaseBatchTranslationEngine {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
        .callTimeout(8, TimeUnit.SECONDS)
        .build();
    
    @NonNull
    @Override
    public String name() {
        return "{google_translator}";
    }
    
    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        return List.of("auto", "zh", "en", "ru");
    }
    
    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return List.of("zh", "en", "ru");
    }
    
    /**
     * 配置分批策略
     */
    @Override
    public BatchingStrategy createBatchingStrategy() {
        // 单批次最多 100 条，总字符数最多 4500
        return new DefaultBatchingStrategy(100, 4500) {
            @Override
            protected int getTextDataSize(String text) {
                return text.length() + 10;  // 预留分割线大小
            }
        };
    }
    
    /**
     * 批量翻译方法（子线程执行）
     */
    @NonNull
    @Override
    public String[] batchTranslate(String[] texts, String sourceLanguage, String targetLanguage) 
            throws IOException {
        // 实现批量翻译逻辑
        // ...
    }
}
```

### 7.2 谷歌翻译实现

使用分割线合并文本，翻译后再拆分：

```java
@NonNull
@Override
public String[] batchTranslate(String[] texts, String sourceLanguage, String targetLanguage) 
        throws IOException {
    // 生成一个分割线，确保原文里面没有
    String divider = "--------";
    while (containsDivider(texts, divider)) {
        divider += "--";
    }
    
    while (true) {
        // 拼接所有文本
        String mergedText = Arrays.stream(texts)
            .collect(Collectors.joining("\n" + divider + "\n"));
        
        // 调用单文本翻译接口
        String translatedText = translate(mergedText, sourceLanguage, targetLanguage);
        
        // 分割翻译结果
        String[] array = translatedText.split("\n" + divider + "\n");
        
        // 确保前后数量一致
        if (array.length == texts.length) {
            return array;
        }
        
        // 如果不一致，增加分割线长度再试一次
        divider += "--";
    }
}
```

### 7.3 单文本翻译

```java
@NonNull
@Override
public String translate(String text, String sourceLanguage, String targetLanguage) 
        throws IOException {
    String url = "http://142.250.0.160/translate_a/single";
    FormBody formBody = new FormBody.Builder()
        .add("client", "gtx")
        .add("dt", "t")
        .add("sl", sourceLanguage)
        .add("tl", targetLanguage)
        .add("q", text)
        .build();
    
    Request request = new Request.Builder()
        .post(formBody)
        .url(url)
        .header("Host", "translate.googleapis.com")
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
        .build();
    
    try (Response response = HTTP_CLIENT.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            throw new IOException("HTTP response code: " + response.code());
        }
        ResponseBody body = response.body();
        if (body == null) {
            throw new IOException("Body is null");
        }
        return getResult(body.string());
    }
}
```

---

## 八、文本编辑器快捷功能（TextEditorFunction）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/TextEditorFunctionDemo.java`

### 8.1 基本实现

继承 `BaseTextEditorFunction` 实现快捷功能：

```java
public class TextEditorFunctionDemo extends BaseTextEditorFunction {
    
    @NonNull
    @Override
    public String name() {
        return "{editor:find_and_replace}";
    }
    
    @Override
    public boolean supportEditTextView() {
        return false;  // 是否支持搜索/替换输入框
    }
    
    @Override
    public boolean supportRepeat() {
        return false;  // 是否支持长按重复执行
    }
}
```

### 8.2 构建选项视图

提供用户配置界面：

```java
@Nullable
@Override
public PluginView buildOptionsView(@NonNull PluginUI pluginUI, @Nullable JSONObject data) {
    // 提前获取文本范围列表
    List<String> itemList = pluginUI.getContext().getStringList(
        "{editor:selected_text}",
        "{editor:current_line_text}",
        "{editor:text_before_cursor}",
        "{editor:text_after_cursor}",
        "{editor:full_text}"
    );
    
    // 构建选项View
    return pluginUI.buildVerticalLayout()
        // 查找内容
        .addTextView().text("{editor:find_content}")
        .addEditText("find").text(data).singleLine(true).requestFocus()
        
        // 替换内容
        .addTextView().text("{editor:replace_content}").marginTopDp(10)
        .addEditText("replace").text(data).singleLine(true)
        
        // 区分大小写
        .addSwitchButton("matchCase").text("{editor:match_case}")
            .checked(data).widthMatchParent().marginTopDp(8)
        
        // 正则表达式
        .addSwitchButton("regex").text("{editor:regex}")
            .checked(data).widthMatchParent().marginTopDp(8)
            .onCheckedChange((buttonView, isChecked) -> {
                PluginViewGroup rootView = buttonView.getRootView();
                PluginEditText find = rootView.requireViewById("find");
                PluginEditText replace = rootView.requireViewById("replace");
                // 设置正则语法高亮
                find.setSyntaxHighlight(isChecked ? PluginEditText.SYNTAX_REGEX : null);
                replace.setSyntaxHighlight(isChecked ? PluginEditText.SYNTAX_REGEX_REPLACEMENT : null);
            })
        
        // 文本范围组
        .addHorizontalLayout().children(builder -> builder
            .addTextView("label1").text("{editor:text_range}")
            .addSpinner("textRange").items(itemList).selection(data)
                .widthMatchParent().marginLeftDp(4)
        )
        
        // 替换次数组
        .addHorizontalLayout().gravity(Gravity.CENTER).children(builder -> builder
            .addTextView("label2").text("{editor:replace_count}")
            .addEditText("replaceCount").text(data).textSize(16)
                .hint("{editor:replace_count_hint}").inputTypeNumber()
                .marginLeftDp(4)
        )
        
        // 让「文本范围」和「替换次数」保持相同宽度
        .unifyWidth("label1", "label2")
        .build();
}
```

### 8.3 获取选项数据

验证并保存用户输入：

```java
@Nullable
@Override
public JSONObject getOptionsData(@NonNull PluginUI pluginUI, @NonNull PluginView pluginView) {
    PluginEditText findEditText = pluginView.requireViewById("find");
    PluginEditText replaceEditText = pluginView.requireViewById("replace");
    PluginSwitchButton matchCaseSwitch = pluginView.requireViewById("matchCase");
    PluginSwitchButton regexSwitch = pluginView.requireViewById("regex");
    
    // 检查输入内容
    if (findEditText.length() == 0) {
        findEditText.requestFocus();
        getContext().showToast("{editor:enter_content}");
        return VALIDATION_FAILED;
    }
    
    if (regexSwitch.isChecked()) {
        // 检查正则表达式
        Pattern pattern;
        try {
            pattern = Regex.compile(findEditText.getText().toString());
        } catch (Exception ex) {
            pluginUI.showErrorMessage(ex);
            findEditText.selectAll();
            findEditText.requestFocus();
            return VALIDATION_FAILED;
        }
    }
    
    // 保存数据到JSON
    JSONObject data = new JSONObject();
    data.putText(findEditText);
    data.putText(replaceEditText);
    data.putChecked(matchCaseSwitch);
    data.putChecked(regexSwitch);
    data.putSelection(pluginView.requireViewById("textRange"));
    return data;
}
```

### 8.4 执行功能逻辑

异步执行查找替换操作：

```java
private boolean doingFunction;  // 防止同时执行多个任务

@Override
public void doFunction(PluginUI pluginUI, TextEditor editor, @Nullable JSONObject data) {
    if (doingFunction) {
        return;
    }
    
    Objects.requireNonNull(data);
    String find = data.getString("find");
    String replace = data.getString("replace");
    boolean matchCase = data.getBoolean("matchCase");
    boolean regex = data.getBoolean("regex");
    int textRange = data.getInt("textRange");
    
    // 编译正则表达式
    Pattern pattern;
    try {
        pattern = Regex.compile(find, regex ? Pattern.MULTILINE : Pattern.LITERAL);
    } catch (Exception e) {
        pluginUI.showToast(e.toString());
        return;
    }
    
    // 获取文本和选中范围
    BufferedText text = editor.getBufferedText();
    int[] selection = {editor.getSelectionStart(), editor.getSelectionEnd()};
    
    // 根据文本范围调整 selection
    if (textRange == 0) {  // 选中的文本
        if (selection[0] == selection[1]) {
            pluginUI.showToast("{editor:no_text_selected}");
            return;
        }
    } else if (textRange == 1) {  // 当前行
        selection[0] = TextUtils.lastIndexOf(text, '\n', selection[0] - 1) + 1;
        selection[1] = TextUtils.indexOf(text, '\n', selection[1]);
        if (selection[1] == -1) {
            selection[1] = text.length();
        }
    }
    // ... 其他范围处理
    
    // 创建匹配器
    Matcher matcher = text.matcher(pattern);
    matcher.region(selection[0], selection[1]);
    ArrayList<MatcherSnapshot> snapshots = new ArrayList<>();
    
    // 异步执行
    new AsyncTask(getContext()) {
        LoadingDialog loadingDialog;
        
        @Override
        protected void beforeThread() throws Exception {
            doingFunction = true;
            loadingDialog = new LoadingDialog(pluginUI)
                .setMessage("{processing}")
                .showDelay(200);  // 延迟200ms显示，避免闪烁
        }
        
        @Override
        protected void onThread() throws Exception {
            // 查找所有匹配项
            while (matcher.find()) {
                snapshots.add(matcher.toSnapshot());
            }
            
            // 准备正则替换
            if (regex && !snapshots.isEmpty()) {
                for (MatcherSnapshot snapshot : snapshots) {
                    snapshot.prepareReplacement(replace);
                }
            }
        }
        
        @Override
        protected void afterThread() throws Exception {
            if (!snapshots.isEmpty()) {
                // 批量替换
                editor.startLargeBatchEditingMode();
                try {
                    for (int i = snapshots.size() - 1; i >= 0; i--) {
                        MatcherSnapshot snapshot = snapshots.get(i);
                        String replacement = regex ? snapshot.getComputedReplacement() : replace;
                        editor.replaceText(snapshot.start(), snapshot.end(), replacement);
                    }
                } finally {
                    editor.finishLargeBatchEditingMode();
                }
                
                pluginUI.showToast("{editor:replace_result}", snapshots.size());
            } else {
                pluginUI.showToast("{editor:text_not_found}");
            }
        }
        
        @Override
        protected void onException(Exception e) {
            pluginUI.showErrorMessage(e);
        }
        
        @Override
        protected void onFinally() {
            doingFunction = false;
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
        }
    }.start();
}
```

---

## 九、文本编辑器浮动菜单（TextEditorFloatingMenu）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/TextEditorFloatingMenuDemo.java`

### 9.1 基本实现

继承 `BaseTextEditorFloatingMenu` 实现浮动菜单：

```java
public class TextEditorFloatingMenuDemo extends BaseTextEditorFloatingMenu {
    
    @NonNull
    @Override
    public String name() {
        return "{case_inversion}";
    }
    
    @NonNull
    @Override
    public Drawable icon() {
        // 使用 Material 图标
        return MaterialIcons.get("swap_vert");
        
        // 也可以加载外部矢量图
        // return VectorDrawableLoader.fromVectorXml(getContext(), "case.xml");
        // return VectorDrawableLoader.fromSvg(getContext(), "case.svg");
    }
    
    @Override
    public boolean checkVisible(@NonNull TextEditor editor) {
        // 仅在选中文本时显示
        return editor.hasTextSelected();
    }
    
    @Override
    public void onMenuClick(@NonNull PluginUI pluginUI, @NonNull TextEditor editor) {
        // 获取选中文本
        int from = editor.getSelectionStart();
        int to = editor.getSelectionEnd();
        char[] charArray = editor.subText(from, to).toCharArray();
        
        // 大小写反转
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (Character.isLowerCase(c)) {
                charArray[i] = Character.toUpperCase(c);
            } else {
                charArray[i] = Character.toLowerCase(c);
            }
        }
        
        // 替换原文
        editor.replaceText(from, to, new String(charArray));
    }
}
```

---

## 十、文本编辑器工具菜单（TextEditorToolMenu）

**文件位置**: `demo/src/main/java/bin/mt/plugin/demo/TextEditorToolMenuDemo.java`

### 10.1 基本实现

继承 `BaseTextEditorToolMenu` 实现工具菜单：

```java
public class TextEditorToolMenuDemo extends BaseTextEditorToolMenu {
    public static final String KEY_BASE64_FLAGS = "base64Flags";
    public static final String KEY_CHARSET = "charset";
    
    @NonNull
    @Override
    public String name() {
        return "Base64";
    }
    
    @NonNull
    @Override
    public Drawable icon() {
        return MaterialIcons.get("code");
    }
    
    @Override
    public boolean checkVisible(@NonNull TextEditor editor) {
        return true;  // 始终显示
    }
}
```

### 10.2 菜单点击事件

创建对话框实现 Base64 编码/解码：

```java
@Override
public void onMenuClick(@NonNull PluginUI pluginUI, @NonNull TextEditor editor) {
    int selStart = editor.getSelectionStart();
    int selEnd = editor.getSelectionEnd();
    String selectedText = editor.subText(selStart, selEnd);
    
    // 构建对话框视图
    PluginView view = pluginUI
        .defaultStyle(new PluginUI.StyleWrapper() {
            @Override
            protected void handleEditText(PluginUI pluginUI, PluginEditTextBuilder builder) {
                super.handleEditText(pluginUI, builder);
                builder.minLines(5).maxLines(10).textSize(12)
                    .softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD);
            }
            
            @Override
            protected void handleButton(PluginUI pluginUI, PluginButtonBuilder builder) {
                super.handleButton(pluginUI, builder);
                builder.style(PluginButton.Style.FILLED);
                builder.text("{base64:" + builder.getId() + "}");
            }
        })
        .buildVerticalLayout()
        .paddingTop(pluginUI.dialogPaddingVertical() / 2)
        .addEditBox("input1").text(selectedText)
        .addHorizontalLayout().gravity(Gravity.CENTER_VERTICAL).children(layout -> layout
            .addButton("encode").width(0).layoutWeight(1)
            .addButton("decode").width(0).layoutWeight(1)
        )
        .addEditBox("input2")
        .build();
    
    // 如果有选中文本，添加替换原文按钮
    if (!selectedText.isEmpty()) {
        // 添加替换原文按钮（默认禁用）
        // ...
    }
    
    // 显示对话框
    PluginDialog dialog = pluginUI.buildDialog()
        .setTitle(name())
        .setView(view)
        .setPositiveButton("{close}", null)
        .setNegativeButton("{base64:exchange}", null)
        .setNeutralButton("{base64:options}", null)
        .show();
    
    // 获取视图
    PluginEditText input1 = view.requireViewById("input1");
    PluginEditText input2 = view.requireViewById("input2");
    SharedPreferences preferences = pluginUI.getContext().getPreferences();
    
    // 编码按钮
    view.requireViewById("encode").setOnClickListener(button -> {
        String text = input1.getText().toString();
        int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);
        Charset charset = Charset.forName(preferences.getString(KEY_CHARSET, "UTF-8"));
        input2.setText(Base64.encodeToString(text.getBytes(charset), flags));
    });
    
    // 解码按钮
    view.requireViewById("decode").setOnClickListener(button -> {
        String text = input1.getText().toString();
        int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);
        Charset charset = Charset.forName(preferences.getString(KEY_CHARSET, "UTF-8"));
        try {
            input2.setText(new String(Base64.decode(text.getBytes(charset), flags), charset));
        } catch (Exception e) {
            pluginUI.showToast(e.toString());
        }
    });
    
    // 交换按钮
    dialog.getNegativeButton().setOnClickListener(button -> {
        String text = input1.getText().toString();
        input1.setText(input2.getText());
        input2.setText(text);
    });
    
    // 选项菜单
    dialog.getNeutralButton().setOnClickListener(button -> {
        PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
        PluginMenu menu = popupMenu.getMenu();
        
        int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);
        String[] charsets = new String[]{"UTF-8", "UTF-16", "GBK", "Big5"};
        String currentCharset = preferences.getString(KEY_CHARSET, "UTF-8");
        
        // 添加 Base64 Flags 选项组
        menu.add("0", "{base64:flag_no_padding}").setCheckable(true)
            .setChecked((flags & Base64.NO_PADDING) != 0);
        menu.add("1", "{base64:flag_no_wrap}").setCheckable(true)
            .setChecked((flags & Base64.NO_WRAP) != 0);
        menu.add("2", "{base64:flag_url_safe}").setCheckable(true)
            .setChecked((flags & Base64.URL_SAFE) != 0);
        
        // 添加文本编码选项组
        PluginSubMenu charsetGroup = menu.addSubMenu("charsets", "{base64:charset}");
        for (String charset : charsets) {
            charsetGroup.add(charset, charset, "group")
                .setChecked(currentCharset.equals(charset));
        }
        charsetGroup.setGroupCheckable("group", true, true);
        
        // 设置菜单点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            // 处理选项点击
            return true;
        });
        
        popupMenu.show();
    });
}
```

---

## 附录：接口注册

所有实现的插件接口必须在 `build.gradle` 的 `mtPlugin.interfaces` 中注册：

```groovy
mtPlugin {
    interfaces = [
        "bin.mt.plugin.demo.TranslationEngineDemo",
        "bin.mt.plugin.demo.GoogleTranslationEngine",
        "bin.mt.plugin.demo.TextEditorFunctionDemo",
        "bin.mt.plugin.demo.TextEditorFloatingMenuDemo",
        "bin.mt.plugin.demo.TextEditorToolMenuDemo",
    ]
}
```

接口类型会自动识别，无需手动指定。

---

## 总结

本文档详细展示了 MT 管理器 V3 插件的各项功能示例：

1. **PluginContext**: 插件上下文，提供环境访问和基础能力
2. **PluginUI**: UI 系统，支持布局、组件、样式等
3. **PluginDialog**: 对话框系统，支持消息、列表、自定义视图等
4. **PluginPopupMenu**: 弹出菜单，支持图标、分组、子菜单等
5. **PluginPreference**: 设置界面，支持自动数据持久化
6. **TranslationEngine**: 翻译引擎，支持逐条翻译
7. **BatchTranslationEngine**: 批量翻译引擎，支持批量翻译
8. **TextEditorFunction**: 文本编辑器快捷功能
9. **TextEditorFloatingMenu**: 文本编辑器浮动菜单
10. **TextEditorToolMenu**: 文本编辑器工具菜单

开发者可根据实际需求，参考这些示例代码快速开发自己的插件。
