---
title: "YanQue 表结构：knowledge_document"
type: text-to-sql-schema
system: yanque
table: "knowledge_document"
primaryKey: "id"
businessDomain: "知识库"
resourceUri: "bear://yanque/text-to-sql/schema/knowledge_document"
---

# YanQue 表结构：knowledge_document

## 表说明

知识库文档，记录对象存储文件、版本、向量入库状态和切片数量。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 文档ID |
| `knowledge_base_id` | `bigint` | 知识库ID |
| `document_name` | `varchar(200)` | 文档名称 |
| `object_key` | `varchar(500)` | TOS对象Key |
| `file_size` | `bigint` | 文件大小 |
| `version` | `varchar(50)` | 版本号 |
| `index_status` | `varchar(20)` | 入库状态：PENDING/INDEXING/INDEXED/FAILED |
| `chunk_count` | `int` | 切片数量 |
| `vector_dim` | `int` | 向量维度 |
| `error_message` | `varchar(1000)` | 失败原因 |
| `indexed_at` | `datetime` | 入库完成时间 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `knowledge_base` | `knowledge_base_id` | `id` | 关联知识库 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `document_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_knowledge_document_base` | `knowledge_base_id` | 否 |
| `idx_knowledge_document_status` | `index_status` | 否 |
