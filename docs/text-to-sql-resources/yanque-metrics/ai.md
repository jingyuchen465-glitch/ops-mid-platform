---
title: "YanQue 指标口径：AI能力"
type: text-to-sql-metrics
system: yanque
businessDomain: "AI能力"
resourceUri: "bear://yanque/text-to-sql/metrics/ai"
---

# YanQue 指标口径：AI能力

## 业务域说明

AI 问答会话、知识库、知识文档、提示词模板和版本。

## 指标口径

| 指标 | 业务含义 | 计算口径 | 主表 | 时间字段 | 可用维度 |
| --- | --- | --- | --- | --- | --- |
| AI 会话数 | AI 问答会话数量。 | `COUNT(ai_chat_session.id)` | `ai_chat_session` | `created_at` | 学生、状态、日期 |
| 活跃会话数 | 状态为正常的 AI 会话数量。 | `COUNT(ai_chat_session.id)` where `ai_chat_session.status = 'ACTIVE'` | `ai_chat_session` | `created_at` | 学生、日期 |
| AI 消息数 | AI 问答消息数量。 | `COUNT(ai_chat_message.id)` | `ai_chat_message` | `created_at` | 会话、角色、模型、日期 |
| 用户消息数 | 角色为 user 的消息数量。 | `COUNT(ai_chat_message.id)` where `ai_chat_message.role = 'user'` | `ai_chat_message` | `created_at` | 会话、模型、日期 |
| 助手消息数 | 角色为 assistant 的消息数量。 | `COUNT(ai_chat_message.id)` where `ai_chat_message.role = 'assistant'` | `ai_chat_message` | `created_at` | 会话、模型、日期 |
| Token 消耗量 | AI 消息记录的 Token 消耗合计。 | `SUM(ai_chat_message.tokens)` | `ai_chat_message` | `created_at` | 会话、角色、模型、日期 |
| 单会话平均消息数 | 每个会话平均消息数量。 | `COUNT(ai_chat_message.id) / COUNT(DISTINCT ai_chat_message.session_id)` | `ai_chat_message` | `created_at` | 模型、日期 |
| 知识库数 | 知识库数量。 | `COUNT(knowledge_base.id)` | `knowledge_base` | `created_at` | 状态、日期 |
| 活跃知识库数 | 状态为启用的知识库数量。 | `COUNT(knowledge_base.id)` where `knowledge_base.status = 'ACTIVE'` | `knowledge_base` | `created_at` | 日期 |
| 知识文档数 | 知识文档数量。 | `COUNT(knowledge_document.id)` | `knowledge_document` | `created_at` | 知识库、入库状态、日期 |
| 已入库文档数 | 入库状态为 indexed 的文档数量。 | `COUNT(knowledge_document.id)` where `knowledge_document.index_status = 'INDEXED'` | `knowledge_document` | `indexed_at` | 知识库、日期 |
| 文档切片数 | 知识文档切片数量合计。 | `SUM(knowledge_document.chunk_count)` | `knowledge_document` | `indexed_at` 或 `created_at` | 知识库、入库状态、日期 |
| 提示词模板数 | Prompt 模板数量。 | `COUNT(prompt_template.id)` | `prompt_template` | `created_at` | 状态、日期 |
| 提示词版本数 | Prompt 模板版本数量。 | `COUNT(prompt_template_version.id)` | `prompt_template_version` | `created_at` | 模板、状态、日期 |

## 口径说明

- Token 消耗以消息表 `ai_chat_message.tokens` 为准；如部分历史消息为空，聚合会自然忽略或按 SQL 实现处理为空值。
- 知识文档“入库成功”使用 `knowledge_document.index_status = 'INDEXED'`，失败原因在 `error_message`。
