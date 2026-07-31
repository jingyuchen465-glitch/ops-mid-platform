---
title: "YanQue 业务术语：学习计划"
type: text-to-sql-business-terms
system: yanque
businessDomain: "学习计划"
resourceUri: "bear://yanque/text-to-sql/business/learning"
---

# YanQue 业务术语：学习计划

## 业务域说明

线上学习计划、学习日历、每日学习进度和视频进度。

## 核心业务术语

| 术语 | 含义 | 主要表字段 |
| --- | --- | --- |
| 学习计划 | 线上学员的课程学习计划，关联学生、课程和 SOP。 | `student_learning_plan.id`, `student_learning_plan.student_id`, `student_learning_plan.course_id`, `student_learning_plan.sop_id`, `student_learning_plan.start_date`, `student_learning_plan.status` |
| 生效学习计划 | 状态为生效的学习计划。 | `student_learning_plan.status = 'ACTIVE'` |
| 取消学习计划 | 状态为取消的学习计划。 | `student_learning_plan.status = 'CANCELED'` |
| 学习日历 | 学习计划拆解到每天的学习安排，记录学习日期、阶段名称、第几天和完成状态。 | `student_learning_calendar.plan_id`, `student_learning_calendar.study_date`, `student_learning_calendar.stage_name`, `student_learning_calendar.day_index`, `student_learning_calendar.status` |
| 待学习日 | 学习日历中状态为待学习的日期。 | `student_learning_calendar.status = 'TODO'` |
| 已完成学习日 | 学习日历中状态为已完成的日期。 | `student_learning_calendar.status = 'DONE'` |
| 阶段名称 | 学习日历中的课程阶段名称。 | `student_learning_calendar.stage_name` |
| 视频观看进度 | 学生观看课程视频的进度记录，包含已观看秒数、视频总时长、进度百分比和完成标记。 | `student_video_progress.student_id`, `student_video_progress.video_id`, `student_video_progress.watched_seconds`, `student_video_progress.duration_seconds`, `student_video_progress.progress_percent`, `student_video_progress.completed` |
| 视频完成 | 视频进度记录中完成标记为 Y。 | `student_video_progress.completed = 'Y'` |
| 学生 SOP | 学生入学 SOP 或导师服务记录。 | `student_sop.student_id`, `student_sop.mentor_id`, `student_sop.sop_time`, `student_sop.status` |

## 常用状态值

| 对象 | 字段 | 值 | 含义 |
| --- | --- | --- | --- |
| 学习计划 | `student_learning_plan.status` | `ACTIVE` | 生效 |
| 学习计划 | `student_learning_plan.status` | `CANCELED` | 取消 |
| 学习日历 | `student_learning_calendar.status` | `TODO` | 待学习 |
| 学习日历 | `student_learning_calendar.status` | `DONE` | 已完成 |
| 视频进度 | `student_video_progress.completed` | `Y` | 已完成 |
| 视频进度 | `student_video_progress.completed` | `N` | 未完成 |
| 学生 SOP | `student_sop.status` | `ASSIGNED` | 已分配 |
| 学生 SOP | `student_sop.status` | `CANCELED` | 已取消 |

## 常用关联

| 关系 | 连接字段 |
| --- | --- |
| 学习计划关联学生 | `student_learning_plan.student_id = student.id` |
| 学习计划关联课程 | `student_learning_plan.course_id = sys_course.id` |
| 学习计划关联 SOP | `student_learning_plan.sop_id = student_sop.id` |
| 学习日历关联学习计划 | `student_learning_calendar.plan_id = student_learning_plan.id` |
| 学习日历关联学生 | `student_learning_calendar.student_id = student.id` |
| 视频进度关联学生 | `student_video_progress.student_id = student.id` |
| 视频进度关联课程视频 | `student_video_progress.video_id = course_video.id` |
