# AI 配置自定义功能更新说明

## v2.2 - 多语言支持优化

### 更新概述

优化多语言支持，将中文设置为默认语言，新增英文语言包。

### 主要变更

#### 1. 语言文件重组

**变更内容**:
- ✅ 将 `strings.mtl` 改为中文（默认语言）
- ✅ 新增 `strings-en.mtl` 英文语言包
- ❌ 删除 `strings-zh-CN.mtl`（不再需要）

**语言文件结构**:

| 语言 | 文件名 | 说明 |
|------|--------|------|
| **中文（默认）** | `strings.mtl` | 默认语言，中国大陆用户 |
| **英文** | `strings-en.mtl` | 英文语言包，国际用户 |

#### 2. 语言自动切换

MT 管理器会根据系统语言自动切换：

- **中文系统** → 自动使用 `strings.mtl`（中文）
- **英文系统** → 自动使用 `strings-en.mtl`（英文）
- **其他语言** → 使用默认语言（中文）

#### 3. 新增文档

**新建文件**: `LANGUAGE.md`
- 多语言配置详细说明
- 如何添加新语言
- 语言文件示例
- 测试多语言的步骤

### 技术实现

#### 语言文件命名规则

```
strings.mtl          # 基础语言包（默认语言）
strings-区域代码.mtl # 区域语言包
```

例如：
- `strings-en.mtl` - 英文
- `strings-ja.mtl` - 日文
- `strings-ko.mtl` - 韩文

#### 使用方式

在代码中使用本地化字符串：

```java
// 方式1：使用花括号（推荐用于 UI 构建）
builder.addButton().text("{close}");

// 方式2：直接通过 context 获取
context.getString("plugin_name");

// 方式3：带格式化参数
String msg = context.getString("hello_format", userName);
```

### 文件变更清单

#### 修改的文件

1. `src/main/assets/strings.mtl`
   - 从英文改为中文（默认语言）

2. `README.md`
   - 更新功能特性，添加多语言支持说明
   - 更新项目结构
   - 更新技术栈说明

#### 新增的文件

1. `src/main/assets/strings-en.mtl`
   - 完整的英文翻译

2. `LANGUAGE.md`
   - 多语言配置说明文档

#### 删除的文件

1. `src/main/assets/strings-zh-CN.mtl`
   - 不再需要，中文已移至 strings.mtl

### 测试建议

#### 测试中文

1. 将设备语言设置为中文（简体）
2. 安装并打开插件
3. 验证所有文本显示为中文

#### 测试英文

1. 将设备语言设置为英文
2. 安装并打开插件
3. 验证所有文本显示为英文

### 添加新语言

如需添加新语言，按以下步骤操作：

1. 在 `assets` 目录创建新文件，如 `strings-ja.mtl`
2. 复制 `strings.mtl` 的内容
3. 将所有中文翻译为目标语言
4. 保持键名（冒号前的部分）不变

### 更新日期

2026年4月9日

---

## v2.1 - AI 配置自定义

本次更新将 AI API 配置从云端获取改为本地可自定义配置，用户可以在设置界面自由修改 API 地址、模型名称、API 密钥和提示词。

## 主要变更

### 1. 移除云端密钥获取逻辑

**修改文件**: `AIHelper.java`

**变更内容**:
- ❌ 删除 RSA 加密密钥管理相关代码
- ❌ 删除云端密钥获取 API 调用
- ❌ 删除云端提示词获取 API 调用
- ✅ 改为使用本地默认配置
- ✅ 支持通过 SharedPreferences 保存和读取自定义配置

**新的默认配置**:
```java
默认 API 地址: https://api.kggzs.cn/v1
默认模型名称: deepseek-v3.2
默认 API 密钥: sk-K1m4b0U2WoorIub7EhbQTIYRFpQhURRXMdIoZBywCEruujOa
默认提示词: 从 assets/提示词.txt 读取
```

### 2. 新增配置管理功能

**AIHelper.java 新增方法**:

| 方法 | 功能 |
|------|------|
| `getApiUrl(context)` | 获取 API 地址 |
| `getAiModel(context)` | 获取 AI 模型名称 |
| `getApiKey(context)` | 获取 API 密钥 |
| `getPrompt(context)` | 获取提示词 |
| `setApiUrl(context, url)` | 保存 API 地址 |
| `setAiModel(context, model)` | 保存 AI 模型名称 |
| `setApiKey(context, key)` | 保存 API 密钥 |
| `setPrompt(context, prompt)` | 保存自定义提示词 |
| `resetToDefault(context)` | 重置为默认配置 |

### 3. 设置界面增强

**修改文件**: `MyPreference.java`

**新增功能**:
- ✅ 新增「AI 代码分析配置」分组
- ✅ 点击配置项弹出对话框进行编辑
- ✅ 单行输入框（API 地址、模型名称、API 密钥）
- ✅ 多行输入框（自定义提示词）
- ✅ 重置配置按钮

**设置项说明**:

| 配置项 | 输入类型 | 说明 |
|--------|----------|------|
| API 地址 | 单行文本 | AI API 的基础 URL 地址 |
| 模型名称 | 单行文本 | 使用的 AI 模型名称 |
| API 密钥 | 单行文本 | AI API 的访问密钥 |
| 自定义提示词 | 多行文本 | 自定义 AI 分析提示词 |
| 重置配置 | 按钮 | 恢复所有设置为默认值 |

### 4. AI 分析菜单更新

**修改文件**: 
- `AICodeAnalysisToolMenu.java`
- `AICodeAnalysisFloatingMenu.java`

**变更内容**:
- ❌ 删除硬编码的提示词 URL
- ✅ 改为从 `AIHelper` 获取配置
- ✅ 调用 `analyzeCodeWithThinking(context, code, ...)` 使用新配置

### 5. 新增默认提示词文件

**新建文件**: `assets/提示词.txt`

**内容**: 专业的代码分析提示词，包括：
- 代码功能概述
- 代码质量评估
- 潜在问题识别
- 优化建议
- 代码规范检查

## 技术实现

### 配置存储

使用 Android SharedPreferences 存储用户配置：

```java
// 配置键名
private static final String PREF_API_URL = "ai_api_url";
private static final String PREF_AI_MODEL = "ai_model";
private static final String PREF_API_KEY = "ai_api_key";
private static final String PREF_CUSTOM_PROMPT = "ai_custom_prompt";
```

### URL 自动补全

AIHelper 会自动补全 API URL：

```java
// 如果用户输入 https://api.example.com
// 会自动转换为 https://api.example.com/chat/completions
String completionsUrl = apiUrl.endsWith("/chat/completions") ? apiUrl : 
                       (apiUrl.endsWith("/") ? apiUrl + "chat/completions" : apiUrl + "/chat/completions");
```

## 用户使用指南

### 首次使用

1. 安装插件后，打开 MT 管理器
2. 进入插件管理，找到 MTKang Plugin
3. 点击进入设置界面
4. 在「AI 代码分析配置」分组中查看默认配置

### 修改配置

1. 点击要修改的配置项（如「API 地址」）
2. 在弹出的对话框中输入新值
3. 点击「保存」按钮
4. 配置立即生效

### 重置配置

1. 点击「重置配置」按钮
2. 所有 AI 配置将恢复为默认值
3. 设置界面自动刷新显示

## 兼容性说明

### 向后兼容

- ✅ 保留原有所有功能
- ✅ 编码/解码工具不受影响
- ✅ 快速插入时间功能正常
- ✅ AI 分析功能使用新配置方式

### 数据迁移

- 无需手动迁移数据
- 首次使用时自动使用默认配置
- 用户修改后自动保存

## 文件变更清单

### 修改的文件

1. `src/main/java/com/example/myplugin/util/AIHelper.java`
   - 重写配置管理逻辑
   - 删除云端密钥相关代码
   - 新增本地配置保存/读取方法

2. `src/main/java/com/example/myplugin/MyPreference.java`
   - 新增 AI 配置分组
   - 新增配置编辑对话框
   - 新增重置配置功能

3. `src/main/java/com/example/myplugin/AICodeAnalysisToolMenu.java`
   - 删除硬编码的提示词 URL
   - 改用 AIHelper 的配置获取方法

4. `src/main/java/com/example/myplugin/AICodeAnalysisFloatingMenu.java`
   - 删除硬编码的提示词 URL
   - 改用 AIHelper 的配置获取方法

### 新增的文件

1. `src/main/assets/提示词.txt`
   - 默认 AI 分析提示词

## 测试建议

### 功能测试

1. **默认配置测试**
   - 安装插件后检查默认配置是否正确
   - 使用默认配置进行 AI 分析测试

2. **自定义配置测试**
   - 修改 API 地址并保存
   - 修改模型名称并保存
   - 修改 API 密钥并保存
   - 修改提示词并保存
   - 使用自定义配置进行 AI 分析测试

3. **重置配置测试**
   - 点击重置配置按钮
   - 验证所有配置是否恢复为默认值

4. **边界测试**
   - 输入空值测试
   - 输入非法 URL 测试
   - 输入特殊字符测试

### 兼容性测试

- 在不同 Android 版本上测试
- 在不同分辨率设备上测试
- 在深色/浅色主题下测试

## 已知问题

暂无

## 后续优化建议

1. **配置验证**
   - 添加 API 连接测试功能
   - 验证 URL 格式
   - 验证 API 密钥有效性

2. **多配置方案**
   - 支持保存多套配置
   - 快速切换不同 API 服务

3. **导入导出**
   - 支持配置导出为文件
   - 支持从文件导入配置

4. **提示词模板**
   - 提供多种预设提示词模板
   - 用户可选择不同分析风格

## 更新日期

2026年4月9日
