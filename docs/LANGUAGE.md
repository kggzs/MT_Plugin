# 多语言支持说明

MTKang Plugin 支持 10 种语言界面，语言会根据设备系统语言自动切换。

## 支持的语言

| 语言 | 语言代码 | 文件名 | 说明 |
|------|----------|--------|------|
| 中文（默认） | - | `strings.mtl` | 默认语言，当系统语言不匹配时使用 |
| 简体中文 | zh-CN | `strings-zh-CN.mtl` | 简体中文 |
| 繁体中文 | zh-TW | `strings-zh-TW.mtl` | 繁体中文（台湾） |
| 日语 | ja | `strings-ja.mtl` | 日语 |
| 韩语 | ko | `strings-ko.mtl` | 韩语 |
| 阿拉伯语 | ar | `strings-ar.mtl` | 阿拉伯语（RTL） |
| 德语 | de | `strings-de.mtl` | 德语 |
| 西班牙语 | es | `strings-es.mtl` | 西班牙语 |
| 法语 | fr | `strings-fr.mtl` | 法语 |
| 俄语 | ru | `strings-ru.mtl` | 俄语 |

## 语言文件格式

语言文件使用 MT 语言文件格式（`.mtl`），格式为键值对：

```mtl
# 插件信息
plugin_name=MTKang Plugin
plugin_description=AI 智能编程助手

# 菜单文本
encode_decode=编码/解码
ai_analysis=AI 代码分析
ai_quick_analysis=AI 快速分析
insert_time=插入时间

# 对话框标题
encode_decode_title=编码/解码工具
ai_analysis_title=AI 代码分析
hash_calculate=哈希计算
timestamp_convert=时间戳转换

# 按钮文本
encode=编码
decode=解码
calculate=计算
convert=转换
replace=替换
cancel=取消
copy=复制
reset=重置
save=保存

# 设置界面
settings=设置
api_config=API 配置
ai_capability_config=AI 能力配置
time_format_config=时间格式设置
reset_config=重置配置

# 提示信息
processing=处理中...
success=成功
error=错误
no_selection=请先选中要分析的代码
analysis_complete=分析完成
```

## 语言切换机制

插件使用 MT 管理器的语言切换机制：

1. 获取设备系统语言设置
2. 查找匹配的语言文件（如 `strings-zh-CN.mtl`）
3. 如果找不到匹配的语言文件，使用默认语言文件（`strings.mtl`）

## 字符串键列表

以下是所有可用的字符串键：

### 插件信息

| 键名 | 说明 | 默认值 |
|------|------|--------|
| `plugin_name` | 插件名称 | MTKang Plugin |
| `plugin_description` | 插件描述 | AI 智能编程助手 |
| `plugin_author` | 作者 | 康哥 |
| `plugin_website` | 网站 | www.kggzs.cn |
| `plugin_github` | GitHub | github.com/kggzs/MT_Plugin |

### 菜单文本

| 键名 | 说明 | 默认值 |
|------|------|--------|
| `encode_decode` | 编码/解码菜单 | 编码/解码 |
| `ai_analysis` | AI 分析菜单 | AI 代码分析 |
| `ai_quick_analysis` | AI 快速分析菜单 | AI 快速分析 |
| `insert_time` | 插入时间菜单 | 插入时间 |

### 编码/解码

| 键名 | 说明 | 默认值 |
|------|------|--------|
| `base64_encode` | Base64 编码 | Base64编码 |
| `base64_decode` | Base64 解码 | Base64解码 |
| `hex_encode` | Hex 编码 | Hex编码 |
| `hex_decode` | Hex 解码 | Hex解码 |
| `unicode_encode` | Unicode 编码 | Unicode编码 |
| `unicode_decode` | Unicode 解码 | Unicode解码 |
| `url_encode` | URL 编码 | URL编码 |
| `url_decode` | URL 解码 | URL解码 |
| `rot13_encode` | ROT13 编码 | ROT13编码 |
| `rot13_decode` | ROT13 解码 | ROT13解码 |
| `binary_encode` | 二进制编码 | 二进制编码 |
| `binary_decode` | 二进制解码 | 二进制解码 |

### 哈希计算

| 键名 | 说明 | 默认值 |
|------|------|--------|
| `hash_calculate` | 哈希计算 | 哈希计算 |
| `hash_md5` | MD5 | MD5 |
| `hash_sha256` | SHA-256 | SHA-256 |
| `hash_sha512` | SHA-512 | SHA-512 |

### 时间戳转换

| 键名 | 说明 | 默认值 |
|------|------|--------|
| `timestamp_convert` | 时间戳转换 | 时间戳转换 |
| `timestamp_to_date` | 时间戳转日期 | 时间戳转日期 |
| `date_to_timestamp` | 日期转时间戳 | 日期转时间戳 |

### AI 分析

| 键名 | 说明 | 默认值 |
|------|------|--------|
| `ai_analysis_title` | AI 分析标题 | AI 代码分析 |
| `ai_prompt_hint` | 提示词提示 | 请输入分析要求... |
| `ai_thinking` | 思考中 | 思考中... |
| `ai_analyzing` | 分析中 | 分析中... |
| `ai_result` | 分析结果 | 分析结果 |
| `ai_background` | 后台运行 | 后台运行 |
| `quick_prompt` | 快速提示词 | 快速提示词 |
| `skill_select` | Skill 选择 | Skill 选择 |

### 设置界面

| 键名 | 说明 | 默认值 |
|------|------|--------|
| `settings` | 设置 | 设置 |
| `api_config` | API 配置 | API 配置 |
| `api_url` | API 地址 | API 地址 |
| `api_model` | 模型名称 | 模型名称 |
| `api_key` | API 密钥 | API 密钥 |
| `ai_capability_config` | AI 能力配置 | AI 能力配置 |
| `global_prompt` | 全局提示词 | 全局分析提示词 |
| `short_prompt` | 简短提示词 | 简短分析提示词 |
| `time_format_config` | 时间格式设置 | 时间格式设置 |
| `reset_config` | 重置配置 | 重置配置 |
| `support_author` | 支持作者 | 支持作者 |

### 通用

| 键名 | 说明 | 默认值 |
|------|------|--------|
| `ok` | 确定 | 确定 |
| `cancel` | 取消 | 取消 |
| `save` | 保存 | 保存 |
| `reset` | 重置 | 重置 |
| `copy` | 复制 | 复制 |
| `replace` | 替换 | 替换 |
| `undo` | 撤销 | 撤销 |
| `success` | 成功 | 成功 |
| `error` | 错误 | 错误 |
| `processing` | 处理中 | 处理中... |
| `no_selection` | 无选中 | 请先选中文本 |

## 添加新语言

要添加新的语言支持，请按以下步骤操作：

### 1. 确定语言代码

查找语言的标准代码，如：
- 英语: `en`
- 葡萄牙语: `pt`
- 意大利语: `it`

### 2. 创建语言文件

在 `src/main/assets/` 目录下创建新文件：

```
strings-{语言代码}.mtl
```

例如：`strings-pt.mtl`（葡萄牙语）

### 3. 翻译字符串

复制 `strings.mtl` 的内容到新文件，然后翻译所有值：

```mtl
# 葡萄牙语示例
plugin_name=MTKang Plugin
plugin_description=Assistente de Programação AI

encode_decode=Codificar/Decodificar
ai_analysis=Análise de Código AI
insert_time=Inserir Hora
```

### 4. 测试

构建插件并测试新语言是否正确显示。

## RTL 语言支持

对于从右到左（RTL）的语言，如阿拉伯语：

1. 文本方向会自动根据系统设置调整
2. 界面布局会自动镜像
3. 确保翻译文本正确

## 注意事项

1. **编码**: 所有语言文件必须使用 UTF-8 编码
2. **格式**: 使用 `key=value` 格式，每行一个键值对
3. **注释**: 以 `#` 开头的行会被忽略
4. **空格**: 键和值中的空格会被保留
5. **特殊字符**: 如需在值中使用 `=`，请转义为 `\=`

---

<div align="center">

Made with ❤️ by 康哥

</div>
