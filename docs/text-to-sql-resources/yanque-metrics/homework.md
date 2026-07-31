---
title: "YanQue 指标口径：作业"
type: text-to-sql-metrics
system: yanque
businessDomain: "作业"
resourceUri: "bear://yanque/text-to-sql/metrics/homework"
---

# YanQue 指标口径：作业

## 业务域说明

班级作业、作业提交、逾期提交、教师批注和成绩。

## 指标口径

| 指标 | 业务含义 | 计算口径 | 主表 | 时间字段 | 可用维度 |
| --- | --- | --- | --- | --- | --- |
| 作业数 | 发布的作业数量。 | `COUNT(homework.id)` | `homework` | `homework_date` 或 `created_at` | 班级、课程内容、日期 |
| 作业提交数 | 学生提交记录数量。 | `COUNT(homework_submission.id)` | `homework_submission` | `submit_time` | 作业、班级、学生、日期 |
| 提交学生数 | 有提交记录的去重学生数。 | `COUNT(DISTINCT homework_submission.student_id)` | `homework_submission` | `submit_time` | 作业、班级、日期 |
| 作业提交率 | 提交学生数占班级学生数的比例。 | `COUNT(DISTINCT homework_submission.student_id) / COUNT(student.id)`，分母通常为同班级学生数 | `homework_submission`, `student` | `submit_time` 或 `homework.deadline` | 作业、班级、日期 |
| 逾期提交数 | 逾期提交记录数量。 | `COUNT(homework_submission.id)` where `homework_submission.late_submitted = 1` | `homework_submission` | `submit_time` | 作业、班级、学生、日期 |
| 逾期率 | 逾期提交数占提交数的比例。 | `COUNT(submission where late_submitted=1) / COUNT(submission)` | `homework_submission` | `submit_time` | 作业、班级、日期 |
| 平均成绩 | 作业提交记录的平均分。 | `AVG(homework_submission.score)` | `homework_submission` | `submit_time` | 作业、班级、学生、日期 |
| 最高成绩 | 作业提交记录最高分。 | `MAX(homework_submission.score)` | `homework_submission` | `submit_time` | 作业、班级、日期 |
| 最低成绩 | 作业提交记录最低分。 | `MIN(homework_submission.score)` | `homework_submission` | `submit_time` | 作业、班级、日期 |

## 口径说明

- 作业提交率的分母需要明确：全班学生、启用学生、或某产品学生。默认建议使用同班级学生数；如要排除停用学生，应加入 `student.status = 'ACTIVE'`。
- 成绩统计应排除 `score IS NULL` 的未批改记录。
