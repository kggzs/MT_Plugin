# 多语言配置说明

## 语言文件结构

项目支持两种语言：

| 语言 | 文件名 | 说明 |
|------|--------|------|
| **中文（默认）** | `strings.mtl` | 默认语言，中国大陆用户 |
| **英文** | `strings-en.mtl` | 英文语言包，国际用户 |

## 语言切换

MT 管理器会根据系统语言自动切换：

- **中文系统** → 自动使用 `strings.mtl`（中文）
- **英文系统** → 自动使用 `strings-en.mtl`（英文）
- **其他语言** → 使用默认语言（中文）

## 语言文件位置

```
src/main/assets/
├── strings.mtl          # 中文（默认语言）
├── strings-en.mtl       # 英文
└── 提示词.txt           # AI 提示词（中文）
```

## 命名规则

- `strings.mtl` - 基础语言包（默认语言）
- `strings-区域代码.mtl` - 区域语言包
  - 例如：`strings-en.mtl`（英文）
  - 例如：`strings-ja.mtl`（日文）
  - 例如：`strings-ko.mtl`（韩文）

## 添加新语言

如需添加新语言，按以下步骤操作：

1. 在 `assets` 目录创建新文件，如 `strings-ja.mtl`
2. 复制 `strings.mtl` 的内容
3. 将所有中文翻译为目标语言
4. 保持键名（冒号前的部分）不变

### 示例：添加日文支持

创建 `strings-ja.mtl`：
```
plugin_name: 開発者ツールプラグイン
plugin_description: 強力な開発者ツールプラグイン
plugin_author: カン・ガー
...（其他翻译）
```

## 使用方式

在代码中使用本地化字符串：

```java
// 方式1：使用花括号（推荐用于 UI 构建）
builder.addButton().text("{close}");

// 方式2：直接通过 context 获取
context.getString("plugin_name");

// 方式3：带格式化参数
String msg = context.getString("hello_format", userName);
```

## 当前翻译状态

### 中文 (strings.mtl)
- ✅ 完整翻译
- ✅ 作为默认语言

### 英文 (strings-en.mtl)
- ✅ 完整翻译
- ✅ 所有功能项已翻译

## 更新语言文件

当添加新功能时，需要同步更新所有语言文件：

1. 在 `strings.mtl`（中文）中添加新键值对
2. 在 `strings-en.mtl`（英文）中添加对应翻译
3. 确保所有语言文件的键名一致

## 注意事项

1. **键名格式**：只能使用字母、数字、下划线和点
2. **换行符**：使用 `\n` 表示换行
3. **续行符**：使用 `\` 在行末可续行
4. **注释**：使用 `#` 开头的行为注释
5. **特殊字符**：需要使用转义字符

## 测试多语言

### 测试中文
1. 将设备语言设置为中文（简体）
2. 安装并打开插件
3. 验证所有文本显示为中文

### 测试英文
1. 将设备语言设置为英文
2. 安装并打开插件
3. 验证所有文本显示为英文

## 语言文件示例

### 基本格式
```
# 这是注释
key: value
another_key: 这是值
multiline: 第一行\n第二行
long_text: 这是一段很长的文本，\
 可以通过续行符拆分
```

### 中文示例 (strings.mtl)
```
plugin_name: 开发者工具插件
encode_decode_function: 编码/解码
close: 关闭
```

### 英文示例 (strings-en.mtl)
```
plugin_name: Developer Tools Plugin
encode_decode_function: Encode/Decode
close: Close
```

## 维护建议

1. 保持所有语言文件同步
2. 添加新功能时先更新中文，再翻译英文
3. 定期检查翻译准确性
4. 欢迎社区贡献更多语言翻译
