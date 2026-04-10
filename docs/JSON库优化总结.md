# JSON 库优化总结

## 📋 任务背景

根据《MT插件开发API速查手册》第十五章 JSON 工具的要求，项目应该使用 `bin.mt.plugin.api.json` 包而不是 `org.json`。

## 🔍 检查结果

### 尝试迁移

我们尝试将以下 3 个文件从 `org.json` 迁移到 MT JSON：
1. `MyPreference.java`
2. `AICodeAnalysisHelper.java`  
3. `AIHelper.java`

### 构建失败

迁移后构建失败，错误信息：
```
错误: 程序包bin.mt.plugin.api.json不存在
import bin.mt.plugin.api.json.JSON;
```

### 根本原因

**MT Plugin SDK v1.0.0-beta6 未提供 `bin.mt.plugin.api.json` 包**

虽然手册中提到了 JSON 工具，但当前 SDK 版本尚未实现该模块。

## ✅ 最终决定

### 保持 org.json

**理由**：
1. ✅ org.json 是 Android SDK 自带的，无需额外依赖
2. ✅ API 与 MT JSON 几乎相同，功能完整
3. ✅ 构建成功，运行稳定
4. ⚠️ MT JSON 的 UI 数据绑定功能当前无法使用

### API 对照表（供未来参考）

当 MT SDK 提供 JSON 库时，可以参考以下对照表快速迁移：

| org.json（当前） | MT JSON（未来） |
|-----------------|----------------|
| `new JSONObject()` | `JSON.object()` |
| `new JSONArray()` | `JSON.array()` |
| `new JSONArray(str)` | `JSON.parse(str).asArray()` |
| `arr.getJSONObject(i)` | `arr.getObject(i)` |
| `arr.length()` | `arr.size()` |
| `arr.put(obj)` | `arr.add(obj)` |
| `obj.put(key, value)` | `obj.put(key, value)` 或 `obj.add(key, value)` |

### MT JSON 特有优势（未来可考虑）

```java
// UI 数据双向绑定（MT JSON 独有）
JSONObject data = JSON.object();
data.putText(editText);        // 自动使用组件 ID 作键名
data.putChecked(checkBox);
data.putSelection(spinner);
data.putCheckedPosition(radioGroup);
data.putCheckedId(radioGroup);

// 从 JSON 恢复到 UI
.addEditText("username").text(savedData)
.addSwitchButton("remember").checked(savedData)
.addSpinner("language").selection(savedData)
.addRadioGroup(true).checkedId(savedData)
```

## 📊 构建验证

### 迁移前（使用 org.json）
```bash
gradlew.bat assembleDebug
BUILD SUCCESSFUL in 1s
39 actionable tasks: 11 executed, 28 up-to-date
```

### 迁移后（使用 MT JSON）
```bash
gradlew.bat assembleDebug
BUILD FAILED
82 个错误：程序包bin.mt.plugin.api.json不存在
```

### 恢复后（保持 org.json）
```bash
gradlew.bat assembleDebug
BUILD SUCCESSFUL in 1s
39 actionable tasks: 11 executed, 28 up-to-date
```

## 📝 结论

**当前无需修改 JSON 库**，保持 `org.json` 即可。

手册中提到的 MT JSON 库可能是：
1. 未来计划添加的功能
2. 需要通过其他方式引入（如额外的依赖）
3. 文档超前于实现

建议：
- ✅ 继续使用 `org.json`（完全可用）
- 📅 关注 MT Plugin SDK 更新
- 🔄 当 SDK 提供 JSON 包时，可参考本文件的 API 对照表快速迁移

## 📚 相关文档

- [代码优化建议文档](./代码优化建议文档.md) - 完整的项目优化建议
- [MT插件开发API速查手册](./MT插件开发API速查手册.md) - 第十五章 JSON 工具

---

**更新日期**：2026年4月10日  
**验证结果**：✅ 确认 org.json 可继续使用  
**下次检查**：关注 MT Plugin SDK 版本更新
