---
title: "YanQue 表结构：student"
type: text-to-sql-schema
system: yanque
table: "student"
primaryKey: "id"
businessDomain: "学生"
resourceUri: "bear://yanque/text-to-sql/schema/student"
---

# YanQue 表结构：student

## 表说明

学生基础档案，包含学生编号、姓名、手机号、学校、专业、班级和状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 学生ID |
| `student_no` | `varchar(32)` | 学员编号 |
| `student_name` | `varchar(50)` | 学生姓名 |
| `student_phone` | `varchar(30)` | 手机号 |
| `gender` | `varchar(10)` | 性别：MALE 男，FEMALE 女 |
| `password` | `varchar(128)` | 登录密码 |
| `education` | `varchar(30)` | 学历 |
| `grade_year` | `int` | 届数 |
| `school` | `varchar(100)` | 学校 |
| `major` | `varchar(100)` | 专业 |
| `teaching_mode` | `varchar(20)` | 上课方式：ONLINE线上，OFFLINE线下 |
| `class_id` | `bigint` | 班级ID，线下班必填 |
| `student_tag` | `varchar(50)` | 学生标签 |
| `status` | `varchar(30)` | 状态：ACTIVE启用，INACTIVE停用 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_class` | `class_id` | `id` | 关联所属班级 |
| `student_product` | `id` | `student_id` | 关联已购产品 |
| `student_exam_record` | `id` | `student_id` | 关联考试记录 |
| `homework_submission` | `id` | `student_id` | 关联作业提交 |

## 字段安全

- 禁止查询字段：`password`
- 脱敏字段：
  - `student_name`：`NAME`
  - `student_phone`：`PHONE`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_student_no` | `student_no` | 是 |
| `uk_student_phone` | `student_phone` | 是 |
| `idx_status` | `status` | 否 |
| `idx_teaching_mode` | `teaching_mode` | 否 |
| `idx_class_id` | `class_id` | 否 |
| `idx_student_tag` | `student_tag` | 否 |
