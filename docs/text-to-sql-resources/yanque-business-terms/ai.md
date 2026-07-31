---
title: "YanQue 业务术语：AI能力"
type: text-to-sql-business-terms
system: yanque
businessDomain: "AI能力"
resourceUri: "bear://yanque/text-to-sql/business/ai"
---

# YanQue 业务术语：AI能力

## 业务域说明

AI 问答会话、知识库、知识文档、提示词模板和版本。

## 核心业务术语

| 术语 | 含义 | 主要表字段 |
| --- | --- | --- |
| AI 会话 | 学生与 AI 问答的一次会话，包含学生、标题、状态和压缩摘要。 | `ai_chat_session.id`, `ai_chat_session.student_id`, `ai_chat_session.title`, `ai_chat_session.status`, `ai_chat_session.summary` |
| 活跃会话 | 状态为正常的 AI 会话。 | `ai_chat_session.status = 'ACTIVE'` |
| 删除会话 | 状态为删除的 AI 会话。 | `ai_chat_session.status = 'DELETED'` |
| AI 消息 | 会话中的单条消息，包含角色、内容、模型、Token 消耗和压缩状态。 | `ai_chat_message.session_id`, `ai_chat_message.role`, `ai_chat_message.content`, `ai_chat_message.model`, `ai_chat_message.tokens`, `ai_chat_message.compressed` |
| 用户消息 | 角色为 user 的消息。 | `ai_chat_message.role = 'user'` |
| 助手消息 | 角色为 assistant 的消息。 | `ai_chat_message.role = 'assistant'` |
| 消息 Token | 单条 AI 消息记录的 Token 消耗数量。 | `ai_chat_message.tokens` |
| 知识库 | 用于 AI 检索问答的知识集合。 | `knowledge_base.id`, `knowledge_base.knowledge_base_name`, `knowledge_base.status` |
| 知识文档 | 知识库下的文档，包含对象存储 Key、文件大小、入库状态、切片数量和向量维度。 | `knowledge_document.knowledge_base_id`, `knowledge_document.document_name`, `knowledge_document.index_status`, `knowledge_document.chunk_count`, `knowledge_document.vector_dim` |
| 已入库文档 | 入库状态为 indexed 的知识文档。 | `knowledge_document.index_status = 'INDEXED'` |
| 提示词模板 | 可复用的 Prompt 模板定义。 | `prompt_template.code`, `prompt_template.name`, `prompt_template.active_version_id`, `prompt_template.status` |
| 当前生效提示词版本 | 模板当前启用版本对应的版本记录。 | `prompt_template.active_version_id = prompt_template_version.id` |

## 常用状态值

| 对象 | 字段 | 值 | 含义 |
| --- | --- | --- | --- |
| AI 会话 | `ai_chat_session.status` | `ACTIVE` | 正常 |
| AI 会话 | `ai_chat_session.status` | `DELETED` | 删除 |
| AI 消息 | `ai_chat_message.role` | `user` | 用户 |
| AI 消息 | `ai_chat_message.role` | `assistant` | AI 助手 |
| AI 消息 | `ai_chat_message.compressed` | `1` | 已压缩进会话摘要 |
| AI 消息 | `ai_chat_message.compressed` | `0` | 未压缩 |
| 知识库 | `knowledge_base.status` | `ACTIVE` | 启用 |
| 知识库 | `knowledge_base.status` | `INACTIVE` | 停用 |
| 知识文档 | `knowledge_document.index_status` | `PENDING` | 待入库 |
| 知识文档 | `knowledge_document.index_status` | `INDEXING` | 入库中 |
| 知识文档 | `knowledge_document.index_status` | `INDEXED` | 入库成功 |
| 知识文档 | `knowledge_document.index_status` | `FAILED` | 入库失败 |
| 提示词模板 | `prompt_template.status` | `ACTIVE` | 启用 |
| 提示词模板 | `prompt_template.status` | `INACTIVE` | 停用 |
| 提示词版本 | `prompt_template_version.status` | `ACTIVE` | 启用 |
| 提示词版本 | `prompt_template_version.status` | `INACTIVE` | 停用 |

## 常用关联

| 关系 | 连接字段 |
| --- | --- |
| AI 会话关联学生 | `ai_chat_session.student_id = student.id` |
| AI 消息关联会话 | `ai_chat_message.session_id = ai_chat_session.id` |
| 知识文档关联知识库 | `knowledge_document.knowledge_base_id = knowledge_base.id` |
| 提示词模板关联当前版本 | `prompt_template.active_version_id = prompt_template_version.id` |
| 提示词版本关联模板 | `prompt_template_version.template_id = prompt_template.id` |
