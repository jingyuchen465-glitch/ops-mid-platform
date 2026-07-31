---
title: "YanQue 业务术语：教学教务"
type: text-to-sql-business-terms
system: yanque
businessDomain: "教学教务"
resourceUri: "bear://yanque/text-to-sql/business/teaching"
---

# YanQue 业务术语：教学教务

## 业务域说明

课程、课程阶段、班级、校区、课表和值班安排。

## 核心业务术语

| 术语 | 含义 | 主要表字段 |
| --- | --- | --- |
| 课程 | 课程基础信息，包含课程名称、课程天数、授课方式和资料路径。 | `sys_course.id`, `sys_course.course_name`, `sys_course.course_days`, `sys_course.teaching_mode` |
| 课程阶段 / 每日课程内容 | 课程下的阶段或每日教学内容。 | `sys_course_detail.course_id`, `sys_course_detail.stage_name`, `sys_course_detail.day_number`, `sys_course_detail.class_content` |
| 班级 | 线下或线上教学组织单元，关联课程、校区和班主任。 | `sys_class.id`, `sys_class.class_period`, `sys_class.course_id`, `sys_class.campus_id`, `sys_class.head_teacher_id` |
| 班级期数 | 班级的期次标识。 | `sys_class.class_period` |
| 校区 | 教学场地或校区信息。 | `sys_campus.id`, `sys_campus.campus_location`, `sys_campus.manager_name` |
| 班主任 | 班级负责老师或管理老师。 | `sys_class.head_teacher_id`, `sys_user.id` |
| 班级课表 | 班级在具体日期的课程安排，包含老师、课程详情、课程内容和上课类型。 | `sys_class_schedule.class_id`, `sys_class_schedule.teacher_id`, `sys_class_schedule.schedule_date`, `sys_class_schedule.course_detail_id`, `sys_class_schedule.class_type` |
| 上课类型 | 课表记录中的课程类型。 | `sys_class_schedule.class_type` |
| 值班安排 | 班级或校区相关的值班记录。 | `sys_class_duty` |

## 常用状态值

| 对象 | 字段 | 值 | 含义 |
| --- | --- | --- | --- |
| 课程 | `sys_course.teaching_mode` | `ONLINE` | 线上 |
| 课程 | `sys_course.teaching_mode` | `OFFLINE` | 线下 |
| 班级课表 | `sys_class_schedule.class_type` | `CLASS` | 正课 |
| 班级课表 | `sys_class_schedule.class_type` | `SELF_STUDY` | 自习 |
| 班级课表 | `sys_class_schedule.class_type` | `HOLIDAY` | 放假 |

## 常用关联

| 关系 | 连接字段 |
| --- | --- |
| 班级关联课程 | `sys_class.course_id = sys_course.id` |
| 班级关联校区 | `sys_class.campus_id = sys_campus.id` |
| 班级关联班主任 | `sys_class.head_teacher_id = sys_user.id` |
| 课程关联课程阶段 | `sys_course.id = sys_course_detail.course_id` |
| 班级关联课表 | `sys_class.id = sys_class_schedule.class_id` |
| 课表关联授课老师 | `sys_class_schedule.teacher_id = sys_user.id` |
| 课表关联课程详情 | `sys_class_schedule.course_detail_id = sys_course_detail.id` |
| 学生关联班级 | `student.class_id = sys_class.id` |
