---
title: "YanQue 指标口径：学习计划"
type: text-to-sql-metrics
system: yanque
businessDomain: "学习计划"
resourceUri: "bear://yanque/text-to-sql/metrics/learning"
---

# YanQue 指标口径：学习计划

## 业务域说明

线上学习计划、学习日历、每日学习进度和视频进度。

## 指标口径

| 指标 | 业务含义 | 计算口径 | 主表 | 时间字段 | 可用维度 |
| --- | --- | --- | --- | --- | --- |
| 学习计划数 | 学习计划记录数量。 | `COUNT(student_learning_plan.id)` | `student_learning_plan` | `created_at` 或 `start_date` | 状态、课程、学生、日期 |
| 生效学习计划数 | 状态为生效的学习计划数量。 | `COUNT(student_learning_plan.id)` where `student_learning_plan.status = 'ACTIVE'` | `student_learning_plan` | `start_date` | 课程、学生、日期 |
| 取消学习计划数 | 状态为取消的学习计划数量。 | `COUNT(student_learning_plan.id)` where `student_learning_plan.status = 'CANCELED'` | `student_learning_plan` | `created_at` 或 `start_date` | 课程、学生、日期 |
| 计划学习天数 | 学习日历记录数量。 | `COUNT(student_learning_calendar.id)` | `student_learning_calendar` | `study_date` | 计划、学生、课程、阶段、日期 |
| 已完成学习天数 | 状态为已完成的学习日历数量。 | `COUNT(student_learning_calendar.id)` where `student_learning_calendar.status = 'DONE'` | `student_learning_calendar` | `study_date` | 计划、学生、课程、阶段、日期 |
| 学习完成率 | 已完成学习天数占计划学习天数的比例。 | `COUNT(calendar where status='DONE') / COUNT(calendar)` | `student_learning_calendar` | `study_date` | 计划、学生、课程、阶段、日期 |
| 视频观看人数 | 有视频观看进度记录的去重学生数。 | `COUNT(DISTINCT student_video_progress.student_id)` | `student_video_progress` | `last_watch_time` | 视频、学生、日期 |
| 视频完成人数 | 视频完成标记为 Y 的去重学生数。 | `COUNT(DISTINCT student_video_progress.student_id)` where `student_video_progress.completed = 'Y'` | `student_video_progress` | `last_watch_time` | 视频、学生、日期 |
| 视频平均进度 | 视频观看进度百分比平均值。 | `AVG(student_video_progress.progress_percent)` | `student_video_progress` | `last_watch_time` | 视频、学生、日期 |
| SOP 分配数 | 已分配 SOP 记录数量。 | `COUNT(student_sop.id)` where `student_sop.status = 'ASSIGNED'` | `student_sop` | `sop_time` | 导师、学生、日期 |

## 口径说明

- 学习计划状态只表示计划是否生效，不代表计划完成；完成情况应使用 `student_learning_calendar.status`。
- 视频完成率可按人数、记录数或视频数统计；本资源默认按去重学生数。
