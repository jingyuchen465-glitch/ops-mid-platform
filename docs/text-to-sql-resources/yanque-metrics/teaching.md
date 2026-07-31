---
title: "YanQue 指标口径：教学教务"
type: text-to-sql-metrics
system: yanque
businessDomain: "教学教务"
resourceUri: "bear://yanque/text-to-sql/metrics/teaching"
---

# YanQue 指标口径：教学教务

## 业务域说明

课程、课程阶段、班级、校区、课表和值班安排。

## 指标口径

| 指标 | 业务含义 | 计算口径 | 主表 | 时间字段 | 可用维度 |
| --- | --- | --- | --- | --- | --- |
| 课程数 | 课程基础信息数量。 | `COUNT(sys_course.id)` | `sys_course` | `created_at` | 授课方式、课程名称 |
| 班级数 | 班级数量。 | `COUNT(sys_class.id)` | `sys_class` | `created_at` | 课程、校区、班主任、期数 |
| 校区班级数 | 每个校区下的班级数量。 | `COUNT(sys_class.id)` group by `sys_class.campus_id` | `sys_class` | `created_at` | 校区、课程 |
| 课程阶段数 | 课程详情或阶段数量。 | `COUNT(sys_course_detail.id)` | `sys_course_detail` | `created_at` | 课程、阶段 |
| 排课次数 | 班级课表记录数量。 | `COUNT(sys_class_schedule.id)` | `sys_class_schedule` | `schedule_date` | 班级、课程、老师、上课类型、日期 |
| 正课次数 | 上课类型为正课的课表数量。 | `COUNT(sys_class_schedule.id)` where `sys_class_schedule.class_type = 'CLASS'` | `sys_class_schedule` | `schedule_date` | 班级、课程、老师、日期 |
| 自习次数 | 上课类型为自习的课表数量。 | `COUNT(sys_class_schedule.id)` where `sys_class_schedule.class_type = 'SELF_STUDY'` | `sys_class_schedule` | `schedule_date` | 班级、课程、老师、日期 |
| 放假次数 | 上课类型为放假的课表数量。 | `COUNT(sys_class_schedule.id)` where `sys_class_schedule.class_type = 'HOLIDAY'` | `sys_class_schedule` | `schedule_date` | 班级、课程、日期 |
| 老师排课次数 | 每位老师的课表记录数量。 | `COUNT(sys_class_schedule.id)` group by `sys_class_schedule.teacher_id` | `sys_class_schedule` | `schedule_date` | 老师、班级、课程、上课类型 |

## 口径说明

- 班级创建时间、课表上课日期和值班日期是不同时间口径；查询教学发生量优先使用 `sys_class_schedule.schedule_date`。
- “课时”如果需要按时长计算，当前字段未提供明确课时长度，应补充业务口径后再计算。
