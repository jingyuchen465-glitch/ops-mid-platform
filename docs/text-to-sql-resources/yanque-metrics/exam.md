---
title: "YanQue 指标口径：考试"
type: text-to-sql-metrics
system: yanque
businessDomain: "考试"
resourceUri: "bear://yanque/text-to-sql/metrics/exam"
---

# YanQue 指标口径：考试

## 业务域说明

题库、试卷、考试安排、考试记录、答题明细和成绩。

## 指标口径

| 指标 | 业务含义 | 计算口径 | 主表 | 时间字段 | 可用维度 |
| --- | --- | --- | --- | --- | --- |
| 考试场次数 | 发布的考试安排数量。 | `COUNT(exam.id)` | `exam` | `start_time` | 班级、试卷、监考老师、日期 |
| 参考人数 | 有考试记录的去重学生数。 | `COUNT(DISTINCT student_exam_record.student_id)` | `student_exam_record` | `start_time` 或 `submit_time` | 考试、班级、学生、日期 |
| 已提交人数 | 状态为已提交的去重学生数。 | `COUNT(DISTINCT student_exam_record.student_id)` where `student_exam_record.status = 'SUBMITTED'` | `student_exam_record` | `submit_time` | 考试、班级、日期 |
| 超时人数 | 状态为超时的去重学生数。 | `COUNT(DISTINCT student_exam_record.student_id)` where `student_exam_record.status = 'TIMEOUT'` | `student_exam_record` | `updated_at` 或 `deadline_time` | 考试、班级、日期 |
| 批改完成人数 | 批改状态为完成的去重学生数。 | `COUNT(DISTINCT student_exam_record.student_id)` where `student_exam_record.grading_status = 'COMPLETED'` | `student_exam_record` | `updated_at` | 考试、班级、日期 |
| 平均分 | 学生考试记录得分平均值。 | `AVG(student_exam_record.score)` where `student_exam_record.grading_status = 'COMPLETED'` | `student_exam_record` | `submit_time` 或 `updated_at` | 考试、班级、学生、日期 |
| 最高分 | 学生考试记录最高得分。 | `MAX(student_exam_record.score)` where `student_exam_record.grading_status = 'COMPLETED'` | `student_exam_record` | `submit_time` 或 `updated_at` | 考试、班级、日期 |
| 最低分 | 学生考试记录最低得分。 | `MIN(student_exam_record.score)` where `student_exam_record.grading_status = 'COMPLETED'` | `student_exam_record` | `submit_time` 或 `updated_at` | 考试、班级、日期 |
| 客观题正确率 | 客观题答题明细中正确数量占有判定结果题目数量的比例。 | `SUM(CASE WHEN correct=1 THEN 1 ELSE 0 END) / COUNT(correct)` where `correct IS NOT NULL` | `student_exam_answer` | `created_at` | 考试、试卷、题目、题型、班级、学生 |
| 题目平均得分率 | 题目得分占题目分值的平均比例。 | `AVG(student_exam_answer.score / student_exam_answer.question_score)` where `question_score > 0` | `student_exam_answer` | `created_at` | 考试、试卷、题目、题型、学生 |

## 口径说明

- “参考人数”默认指产生考试记录的人数，不等于班级应考人数。
- “及格率”需要及格分数线或百分比阈值，当前元数据没有统一阈值；需要补充业务口径后计算。
- 主观题或编程题可能没有 `correct` 判定，正确率应使用 `correct IS NOT NULL` 的记录。
