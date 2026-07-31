---
name: yanque-text-to-sql
description: Use this skill when the user asks YanQue business data questions that require SQL generation, metric calculation, result querying, or business term explanation.
---

# YanQue Text-to-SQL

## Trigger

Use this skill when the user asks about YanQue business data, including:

- orders, payments, refunds, products, sales amount, average order value
- students, classes, campuses, student products, SOP, follow-up records
- courses, schedules, teachers, duties
- learning plans, learning calendars, video progress
- homework, submissions, late submissions, scores
- exams, papers, questions, answers, grades
- AI chat sessions, messages, token usage, knowledge bases, prompt templates

Do not use this skill for general SQL questions unrelated to YanQue.

## Required Runtime Prompt

For YanQue Text-to-SQL tasks, use Bear MCP Prompt:

```text
yanque_text_to_sql
```

The Prompt supplies runtime parameters and output rules:

- `question`
- `datasource_id`
- `max_rows`
- `execute_sql`

Use this Skill for the method. Use the Prompt for the current task parameters and final response format.

If the MCP client does not automatically support `prompts/get` as an agent-callable action, call the Bear MCP Tool:

```text
render_prompt
```

with:

```json
{
  "prompt_name": "yanque_text_to_sql",
  "arguments_json": "{\"question\":\"<user question>\",\"datasource_id\":\"<datasource id>\",\"max_rows\":\"100\",\"execute_sql\":\"false\"}"
}
```

Then follow the returned `rendered_prompt`.

## Procedure

1. Classify the user question into one or more YanQue domains:
   - `order`
   - `student`
   - `teaching`
   - `learning`
   - `homework`
   - `exam`
   - `ai`

2. Read the matching business Resource:
   - `bear://yanque/text-to-sql/business/{domain}`

3. If the question asks for a metric, ranking, trend, count, sum, average, rate, distribution, or KPI, read the matching metric Resource:
   - `bear://yanque/text-to-sql/metrics/{domain}`

4. Before using any table in SQL, read its schema Resource:
   - `bear://yanque/text-to-sql/schema/{tableName}`

5. Generate SQL only after the Resources confirm:
   - the table exists
   - every selected field exists
   - join fields exist
   - status or enum values are known
   - metric formula is known
   - time field is known or clarified

6. If the task should execute SQL and the Prompt has `execute_sql=true`, call `query_data_source` with the Prompt-provided `datasource_id`.

7. If a required definition is missing or ambiguous, ask a clarification question instead of generating SQL.

## Domain Routing

| Domain | Use When User Mentions |
| --- | --- |
| `order` | 订单、支付、退款、销售额、产品、购买、客单价 |
| `student` | 学生、学员、档案、班级归属、标签、SOP、回访、宿舍 |
| `teaching` | 课程、班级、校区、课表、上课、老师、值班、阶段 |
| `learning` | 学习计划、学习日历、学习进度、视频进度、完成率 |
| `homework` | 作业、提交、逾期、批注、成绩、训练集 |
| `exam` | 考试、试卷、题目、题库、答题、分数、成绩、正确率 |
| `ai` | AI、问答、会话、消息、Token、知识库、文档、提示词 |

If multiple domains are involved, read Resources for all involved domains.

## Task Types

Classify the request before generating SQL:

| Type | Meaning | Required Resources |
| --- | --- | --- |
| `detail_query` | user asks for rows, lists, records, details | business + schema |
| `metric_query` | user asks for counts, sums, averages, rates, rankings, trends | business + metrics + schema |
| `definition_query` | user asks what a term or metric means | business or metrics |
| `clarification_needed` | key term, metric, time field, or filter is unclear | ask user |

## Clarify Instead Of Guessing

Ask a clarification question when:

- the metric definition is missing
- the time field is unclear
- the pass threshold is missing
- active/effective/enrolled student definition is unclear
- a metric has multiple possible formulas
- the user asks for sensitive details without a clear need
- the requested table or field cannot be found in Resources

Examples:

```text
需要确认：这里的活跃学生是指 student.status = 'ACTIVE'，
还是按登录、学习、作业或考试行为定义？
```

```text
需要确认：及格率按 60 分计算，还是按试卷总分的 60% 计算？
```

## SQL Safety

Only generate one read-only `SELECT` statement.

Never generate:

- `INSERT`
- `UPDATE`
- `DELETE`
- `MERGE`
- `DROP`
- `ALTER`
- `TRUNCATE`
- `CREATE`
- multi-statement SQL
- SQL with semicolons

Do not use `SELECT *`.

Do not use tables or fields that are not declared in YanQue schema Resources.

Do not default to sensitive detail fields such as phone numbers or passwords.

Do not invent money unit conversions.

## Recommended Tools

| Tool | Use |
| --- | --- |
| `render_prompt` | render `yanque_text_to_sql` when the client does not automatically invoke MCP Prompt |
| `query_data_source` | execute one read-only SQL statement |
| `list_data_sources` | inspect available published data sources |
| `get_skill` | fetch this Skill by Skill ID when installation is needed |

## Minimal Working Pattern

For a normal metric query:

1. Identify domain.
2. Read business Resource.
3. Read metric Resource.
4. Read table schema Resources.
5. Generate SQL.
6. Execute only if the Prompt says `execute_sql=true`.
7. Return metric basis, SQL, used Resources, and result if executed.
