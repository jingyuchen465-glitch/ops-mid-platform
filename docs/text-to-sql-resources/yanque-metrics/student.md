---
title: "YanQue 指标口径：学生管理"
type: text-to-sql-metrics
system: yanque
businessDomain: "学生管理"
resourceUri: "bear://yanque/text-to-sql/metrics/student"
---

# YanQue 指标口径：学生管理

## 业务域说明

学生档案、学生状态、班级归属、学生产品、学生服务和回访。

## 指标口径

| 指标 | 业务含义 | 计算口径 | 主表 | 时间字段 | 可用维度 |
| --- | --- | --- | --- | --- | --- |
| 学生总数 | 学生档案总数量。 | `COUNT(student.id)` | `student` | `created_at` | 状态、授课方式、班级、学校、专业、届数 |
| 启用学生数 | 当前状态为启用的学生数量。 | `COUNT(student.id)` where `student.status = 'ACTIVE'` | `student` | `created_at` | 授课方式、班级、学校、专业、届数 |
| 停用学生数 | 当前状态为停用的学生数量。 | `COUNT(student.id)` where `student.status = 'INACTIVE'` | `student` | `created_at` | 授课方式、班级、学校、专业、届数 |
| 线上学生数 | 授课方式为线上的学生数量。 | `COUNT(student.id)` where `student.teaching_mode = 'ONLINE'` | `student` | `created_at` | 状态、学校、专业、届数 |
| 线下学生数 | 授课方式为线下的学生数量。 | `COUNT(student.id)` where `student.teaching_mode = 'OFFLINE'` | `student` | `created_at` | 状态、班级、校区、学校、专业、届数 |
| 班级学生数 | 每个班级下的学生数量。 | `COUNT(student.id)` group by `student.class_id` | `student` | `created_at` | 班级、课程、校区、班主任 |
| 已购产品学生数 | 有学生产品关系的去重学生数量。 | `COUNT(DISTINCT student_product.student_id)` | `student_product` | `created_at` | 产品、产品状态、日期 |
| 启用产品学生数 | 已开通启用产品的去重学生数量。 | `COUNT(DISTINCT student_product.student_id)` where `student_product.status = 'ACTIVE'` | `student_product` | `created_at` | 产品、日期 |
| SOP 分配数 | 已分配 SOP 的记录数量。 | `COUNT(student_sop.id)` where `student_sop.status = 'ASSIGNED'` | `student_sop` | `sop_time` 或 `created_at` | 导师、学生、日期 |

## 口径说明

- “在读”“有效”“活跃”不是单一字段，未明确时不要等同于 `student.status = 'ACTIVE'`；可按业务确认后扩展为正式指标。
- 涉及手机号、密码等敏感字段时，指标统计一般不需要查询明细值。
