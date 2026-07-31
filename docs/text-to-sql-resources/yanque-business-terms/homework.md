---
title: "YanQue 业务术语：作业"
type: text-to-sql-business-terms
system: yanque
businessDomain: "作业"
resourceUri: "bear://yanque/text-to-sql/business/homework"
---

# YanQue 业务术语：作业

## 业务域说明

班级作业、作业提交、逾期提交、教师批注和成绩。

## 核心业务术语

| 术语 | 含义 | 主要表字段 |
| --- | --- | --- |
| 作业 | 班级发布的作业任务，包含标题、内容文件、答案文件、作业日期、开始时间、截止时间和备注。 | `homework.id`, `homework.title`, `homework.class_id`, `homework.homework_date`, `homework.start_time`, `homework.deadline` |
| 作业内容 | 作业题目或内容文件，存储为对象存储 Key 和文件名。 | `homework.content_object_key`, `homework.content_file_name` |
| 作业答案 | 作业答案文件及学生可见状态。 | `homework.answer_object_key`, `homework.answer_file_name`, `homework.answer_student_visible` |
| 作业提交 | 学生对作业的提交记录，包含提交文件、提交时间、逾期标记、老师批注和分数。 | `homework_submission.homework_id`, `homework_submission.student_id`, `homework_submission.submit_time`, `homework_submission.late_submitted`, `homework_submission.score` |
| 已提交作业 | 存在提交记录的学生作业。 | `homework_submission.id` |
| 逾期提交 | 提交记录中逾期标记为真。 | `homework_submission.late_submitted = 1` |
| 作业成绩 | 老师批改后的分数。 | `homework_submission.score` |
| 老师批注 | 老师对学生提交内容的批注。 | `homework_submission.teacher_remark` |

## 常用状态值

| 对象 | 字段 | 值 | 含义 |
| --- | --- | --- | --- |
| 作业答案 | `homework.answer_student_visible` | `1` | 学生可见 |
| 作业答案 | `homework.answer_student_visible` | `0` | 学生不可见 |
| 作业提交 | `homework_submission.late_submitted` | `1` | 逾期提交 |
| 作业提交 | `homework_submission.late_submitted` | `0` | 非逾期提交 |

## 常用关联

| 关系 | 连接字段 |
| --- | --- |
| 作业关联提交记录 | `homework.id = homework_submission.homework_id` |
| 作业关联班级 | `homework.class_id = sys_class.id` |
| 提交记录关联学生 | `homework_submission.student_id = student.id` |
| 提交记录关联班级 | `homework_submission.class_id = sys_class.id` |
