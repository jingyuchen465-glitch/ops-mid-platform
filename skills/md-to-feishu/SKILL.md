---
name: md-to-feishu
description: 将本地 Markdown 批量转换为飞书云文档(docx)，写入标题/正文/列表/代码/引用等结构化内容，并回读校验。适用于讲稿、面试指南、简历模板、逐字稿、教程等长篇内容全量写入。
---

# Bear Markdown → 飞书文档（md-to-feishu）

把本地 Markdown 转成飞书新版云文档（docx），供讲稿、面试指南、简历模板、逐字稿、技术教程等长篇内容全量写入。

## 流程

1. **创建文档**：`feishu_create_document`（参数 `title`）→ 拿到 `document_id`，URL 为 `https://feishu.cn/docx/{document_id}`。
2. **Markdown → 块**：逐行解析成飞书块结构。
3. **写入**：用「创建嵌套块」`POST /docx/v1/documents/{doc}/blocks/{doc}/descendant` 分批追加（`index=-1`）。
4. **校验**：`feishu_read_document`（参数名 `documentId`）读回，确认开头标题与结尾完整。

## 为什么用 /descendant 而非 /children

- `/blocks/{doc}/children`（创建块）**只支持纯文本块**，标题/列表/代码等富文本会报 `1770001 invalid param`。
- `/blocks/{doc}/descendant`（创建嵌套块）**支持全部块类型**，长文必须用它。

## 请求与块结构

```json
{
  "index": -1,
  "children_id": ["b1", "b2"],
  "descendants": [
    { "block_id": "b1", "block_type": 2, "text": {"elements": [{"text_run": {"content": "正文"}}]}, "children": [] }
  ]
}
```

- 每个块必须带**全局唯一 `block_id`**，并被 `children_id` 引用。
- `index=-1` 追加到末尾；多次调用依次追加，保持文档顺序。

### 块类型枚举

| Markdown | block_type | 内容字段 |
|---|---|---|
| 正文段落 | 2 | text |
| `#` / `##` / `###` / `####` | 3 / 4 / 5 / 6 | heading1 / heading2 / heading3 / heading4 |
| 无序列表 `-` | 12 | bullet |
| 有序列表 `1.` | 13 | ordered |
| 代码块 ` ```` ``` `` ` | 14 | code |
| 引用 `>` | 15 | quote |
| 分割线 `---` | 22 | divider |

### 文本元素与代码块

文本元素：`{"text_run": {"content": "..."}}`；加粗/链接用 `text_element_style` 的 `bold` / `link`。
代码块：`{"elements": [{"text_run": {"content": "code"}}], "style": {"language": 1, "wrap": true}}`（`language=1` 为纯文本）。

## 表格处理（可靠方案）

飞书原生 Table 块校验严格、易报错（TableCell 结构繁琐）。**不要构造原生表格块**。把表格扁平化为若干文本块：

- 首行表头加粗：`**局限  |  现象  |  工程解法**`
- 数据行：`幻觉  |  编造  |  RAG`
- 分隔行（全为 `-`/`:` 的行）直接跳过。

## 分批写入与频率限制

- 每批建议 ≤ 220 个块（或 JSON ≤ 600KB）。
- 单文档并发编辑限 3 次/秒：两次请求之间 `sleep ≥ 0.4s`；遇 400/429 用指数退避（`0.8 * 2^attempt`）重试。
- 用「时间戳 + 自增」生成全局唯一 `block_id`。

## 前置条件

- 飞书应用需开通 `docx:document`（文档读写）权限。
- token：用应用凭证换取 `tenant_access_token`，或由绑定的动态 Groovy 工具自动获取（请求头 `Authorization: Bearer <token>`）。

## 完成标准

- `feishu_read_document` 回读开头（标题）与结尾完整。
- 成功写入块数 = 总块数（fail=0）。
- 向用户输出文档链接 `https://feishu.cn/docx/{document_id}`。