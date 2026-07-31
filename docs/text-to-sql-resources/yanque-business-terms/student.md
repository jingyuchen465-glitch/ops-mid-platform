---
title: "YanQue 业务术语：学生管理"
type: text-to-sql-business-terms
system: yanque
businessDomain: "学生管理"
resourceUri: "bear://yanque/text-to-sql/business/student"
---

# YanQue 业务术语：学生管理

## 业务域说明

学生档案、学生状态、班级归属、学生产品、学生服务和回访。

## 核心业务术语

| 术语 | 含义 | 主要表字段 |
| --- | --- | --- |
| 学生 / 学员 | 学生基础档案对象，包含学员编号、姓名、学校、专业、授课方式、班级和状态。 | `student.id`, `student.student_no`, `student.student_name`, `student.school`, `student.major`, `student.teaching_mode`, `student.class_id`, `student.status` |
| 启用学生 | 状态为启用的学生。 | `student.status = 'ACTIVE'` |
| 停用学生 | 状态为停用的学生。 | `student.status = 'INACTIVE'` |
| 线上学生 | 授课方式为线上的学生。 | `student.teaching_mode = 'ONLINE'` |
| 线下学生 | 授课方式为线下的学生。 | `student.teaching_mode = 'OFFLINE'` |
| 班级归属 | 学生所在班级。线下班级通常通过学生表的班级字段关联。 | `student.class_id`, `sys_class.id` |
| 学生标签 | 学生档案上的业务标签。 | `student.student_tag` |
| 学生已购产品 | 学生购买或开通的课程产品关系。 | `student_product.student_id`, `student_product.product_id`, `student_product.status` |
| 学生服务 SOP | 学生入学或服务流程记录，包含导师、SOP 时间和状态。 | `student_sop.student_id`, `student_sop.mentor_id`, `student_sop.sop_time`, `student_sop.status` |
| 回访标签 | 学生回访分类配置。 | `student_followup_tag.id`, `student_followup_tag.student_tag` |
| 回访记录 | 对学生执行的回访或跟进记录。 | `student_followup_record.student_id`, `student_followup_record.followup_tag_id` |
| 宿舍分配 | 学生住宿、楼栋、房间、床位等分配关系。 | `student_dorm`, `dorm_assignment`, `dorm_building`, `dorm_room`, `dorm_bed` |

## 常用状态值

| 对象 | 字段 | 值 | 含义 |
| --- | --- | --- | --- |
| 学生 | `student.status` | `ACTIVE` | 启用 |
| 学生 | `student.status` | `INACTIVE` | 停用 |
| 学生 | `student.teaching_mode` | `ONLINE` | 线上 |
| 学生 | `student.teaching_mode` | `OFFLINE` | 线下 |
| 学生产品 | `student_product.status` | `ACTIVE` | 启用 |
| 学生产品 | `student_product.status` | `INACTIVE` | 停用 |
| 学生 SOP | `student_sop.status` | `ASSIGNED` | 已分配 |
| 学生 SOP | `student_sop.status` | `CANCELED` | 已取消 |

## 常用关联

| 关系 | 连接字段 |
| --- | --- |
| 学生关联班级 | `student.class_id = sys_class.id` |
| 学生关联已购产品 | `student.id = student_product.student_id` |
| 学生已购产品关联产品 | `student_product.product_id = order_product.id` |
| 学生已购产品关联来源支付订单 | `student_product.source_order_no = order_payment.order_no` |
| 学生关联考试记录 | `student.id = student_exam_record.student_id` |
| 学生关联作业提交 | `student.id = homework_submission.student_id` |
| 学生关联学习计划 | `student.id = student_learning_plan.student_id` |
| 学生关联学习日历 | `student.id = student_learning_calendar.student_id` |
| 学生关联视频进度 | `student.id = student_video_progress.student_id` |
| 学生关联 SOP | `student.id = student_sop.student_id` |
