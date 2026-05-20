# JSON 库优化总结

> **优化日期**: 2026-05-20  
> **项目版本**: v2.0.2

---

## 一、当前状态

项目使用 MT 插件 SDK 提供的 JSON 库：

- `bin.mt.plugin.api.json.JSONObject`
- `bin.mt.plugin.api.json.JSONArray`
- `bin.mt.plugin.api.json.JSON`

---

## 二、JSON 库特点

| 特点 | 说明 |
|------|------|
| 轻量高效 | 专为移动端优化 |
| API 兼容 | 与 org.json 兼容 |
| 数据绑定 | 支持 UI 组件数据绑定 |
| 线程安全 | 非线程安全，需同步 |

---

## 三、使用示例

### 3.1 解析 JSON

```java
String jsonText = "{\"name\":\"MT\",\"version\":3}";
JSONObject obj = new JSONObject(jsonText);
String name = obj.getString("name", "");
int version = obj.getInt("version", 0);
```

### 3.2 创建 JSON

```java
JSONObject obj = JSON.object();
obj.add("name", "我的插件")
    .add("enabled", true);

JSONArray arr = JSON.array().add(1).add("text");
```

### 3.3 序列化

```java
String prettyJson = obj.toString(WriterConfig.PRETTY_PRINT);
String minimalJson = obj.toString(WriterConfig.MINIMAL);
```

### 3.4 UI 数据绑定

```java
JSONObject data = new JSONObject();
data.putText(editText);       // 编辑框文本
data.putChecked(checkBox);    // 复选框状态
data.putSelection(spinner);   // 下拉框选中位置
```

---

## 四、注意事项

1. **线程安全**: 多线程操作需同步
2. **组件 ID**: 使用 `putText()` 等方法时，组件必须设置 ID
3. **格式化**: 生产环境建议使用 `MINIMAL`

---

## 五、结论

项目 JSON 库使用合理，无需进一步优化。

---

<div align="center">

Made with ❤️ by 康哥

</div>
