---
name: yanque-text-to-sql
description: Generate safe YanQue read-only SQL from natural language by using Bear MCP Resources for schema, business terms, and metric definitions.
---

# YanQue Text-to-SQL Skill

## Purpose

Use this skill when the user asks questions about YanQue business data and expects SQL, query results, or a metric explanation. The skill converts natural language into safe read-only SQL by combining YanQue schema Resources, business term Resources, metric Resources, and Bear MCP SQL execution tools.

## Capability Boundary

This skill can:

- Identify the relevant YanQue business domain from the user question.
- Read YanQue table schema Resources under `bear://yanque/text-to-sql/schema`.
- Read YanQue business term Resources under `bear://yanque/text-to-sql/business`.
- Read YanQue metric definition Resources under `bear://yanque/text-to-sql/metrics`.
- Generate a single read-only `SELECT` SQL statement.
- Execute SQL through Bear MCP `query_data_source` only when the user or prompt explicitly asks to execute and a valid `datasource_id` is available.
- Explain which business terms, metric definitions, tables, fields, joins, and time fields were used.

This skill must not:

- Generate `INSERT`, `UPDATE`, `DELETE`, `MERGE`, `DROP`, `ALTER`, `TRUNCATE`, `CREATE`, `REPLACE`, permission changes, stored procedures, or multi-statement SQL.
- Use tables or fields that are not declared in the YanQue schema Resources.
- Invent metric formulas when a named metric is missing or ambiguous.
- Default to sensitive detail fields such as phone numbers or passwords for ordinary analytical answers.
- Guess money units, pass thresholds, active-user definitions, or vague time fields when Resources do not define them.

## Resource Map

| Knowledge Type | URI Pattern | Usage |
| --- | --- | --- |
| Schema index | `bear://yanque/text-to-sql/schema` | Discover available table schema Resources. |
| Table schema | `bear://yanque/text-to-sql/schema/{tableName}` | Verify fields, primary keys, indexes, and joins before writing SQL. |
| Business terms index | `bear://yanque/text-to-sql/business` | Discover domain term Resources. |
| Business terms | `bear://yanque/text-to-sql/business/{domain}` | Resolve business objects, statuses, enum values, and common joins. |
| Metrics index | `bear://yanque/text-to-sql/metrics` | Discover domain metric Resources. |
| Metrics | `bear://yanque/text-to-sql/metrics/{domain}` | Resolve metric formulas, main tables, time fields, and dimensions. |

## Business Domain Routing

| User Intent Keywords | Domain | Business Resource | Metric Resource |
| --- | --- | --- | --- |
| 订单、支付、退款、销售额、产品、购买、客单价 | `order` | `bear://yanque/text-to-sql/business/order` | `bear://yanque/text-to-sql/metrics/order` |
| 学生、学员、档案、状态、班级归属、标签、SOP、回访、宿舍 | `student` | `bear://yanque/text-to-sql/business/student` | `bear://yanque/text-to-sql/metrics/student` |
| 课程、班级、校区、课表、上课、老师、值班、阶段 | `teaching` | `bear://yanque/text-to-sql/business/teaching` | `bear://yanque/text-to-sql/metrics/teaching` |
| 学习计划、学习日历、学习进度、视频进度、完成率 | `learning` | `bear://yanque/text-to-sql/business/learning` | `bear://yanque/text-to-sql/metrics/learning` |
| 作业、提交、逾期、批注、成绩、平均分 | `homework` | `bear://yanque/text-to-sql/business/homework` | `bear://yanque/text-to-sql/metrics/homework` |
| 考试、试卷、题目、题库、答题、分数、成绩、正确率 | `exam` | `bear://yanque/text-to-sql/business/exam` | `bear://yanque/text-to-sql/metrics/exam` |
| AI、问答、会话、消息、Token、知识库、文档、提示词 | `ai` | `bear://yanque/text-to-sql/business/ai` | `bear://yanque/text-to-sql/metrics/ai` |

If one user question spans multiple domains, read all relevant business and metric Resources. Use schema Resources for every table referenced by the final SQL.

## Workflow

1. Classify the request as one of:
   - `detail_query`: user asks for rows, lists, records, or detailed attributes.
   - `metric_query`: user asks for counts, sums, averages, rates, rankings, trends, distributions, or KPI values.
   - `definition_query`: user asks what a term or metric means.
   - `ambiguous_query`: key domain, metric, time field, or filter is unclear.

2. Select Resources:
   - Always use the relevant business term Resource for the selected domain.
   - For `metric_query`, always use the relevant metric Resource.
   - Read schema Resources for every table that may appear in the SQL.
   - Use joins only when fields are declared by the schema or business Resources.

3. Resolve business meaning:
   - Map business words to concrete tables, fields, statuses, and enum values.
   - For named metrics, use the metric Resource formula.
   - For vague expressions such as “有效学生”, “活跃学生”, “及格率”, “最近”, or “上个月”, confirm the needed definition when the Resource does not define it.

4. Build SQL:
   - Generate one SQL statement only.
   - Use explicit column names instead of `SELECT *`.
   - Use table aliases only when they improve readability.
   - Add `WHERE` filters for status, time range, domain conditions, and user-specified filters.
   - Add `GROUP BY`, `ORDER BY`, and aggregate functions only when required by the user question.
   - Add `LIMIT` to detail queries. Default to `LIMIT 100` unless the prompt provides another `max_rows`.
   - For grouped aggregate queries, add `LIMIT` when the result can have many groups.

5. Validate before final answer:
   - The SQL is read-only and single-statement.
   - Every table and field appears in a schema Resource.
   - Every join uses declared relationship fields or obvious foreign key fields confirmed by schema.
   - Every metric formula follows the metric Resource.
   - Every time filter uses an appropriate time field from the metric or business Resource.
   - Detail output avoids sensitive fields unless explicitly requested and allowed.

6. Execute only when requested:
   - Use Bear MCP `query_data_source` with `datasource_id` and `sql`.
   - Do not execute if `datasource_id` is missing.
   - Do not execute if SQL validation fails.
   - Report execution errors separately from SQL generation errors.

## Clarification Rules

Return a clarification instead of SQL when:

- The user asks for a named metric not present in `bear://yanque/text-to-sql/metrics/{domain}`.
- A metric has multiple valid formulas and the user did not specify one.
- A required time range is relative but the current date or target date is unavailable.
- The requested business concept has no defined table or field.
- The user requests write operations or database changes.
- The user asks for sensitive detail fields without a clear analytical need.

Use short clarification questions, for example:

- “你这里的活跃学生是指 `student.status = 'ACTIVE'`，还是按登录/学习行为定义？”
- “及格率需要及格线。你希望按 60 分，还是按试卷总分的 60%？”
- “销售额按支付成功时间 `pay_success_time` 统计，还是按订单创建时间 `created_at`？”

## Output Contract

For SQL generation, answer in this structure:

````markdown
## 口径

- 业务域：
- 使用指标：
- 时间字段：
- 过滤条件：

## SQL

```sql
SELECT ...
```

## 使用的 Resource

- `bear://yanque/text-to-sql/business/...`
- `bear://yanque/text-to-sql/metrics/...`
- `bear://yanque/text-to-sql/schema/...`

## 注意

- ...
````

For executed queries, append:

```markdown
## 查询结果

...
```

For clarification, answer in this structure:

```markdown
需要确认一个口径：

...
```

## Safety Notes

- `query_data_source` already rejects non-SELECT and semicolon SQL; still validate before calling it.
- Dynamic tools that wrap `runSql` must keep their linked data source whitelist narrow.
- Resource knowledge has priority over old code or memory.
- If Resource content conflicts, prefer the more specific Resource in this order: table schema, metric definition, business terms, index Resource.
