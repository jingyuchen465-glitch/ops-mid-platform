---
title: "YanQue 表结构：student_dorm_bill"
type: text-to-sql-schema
system: yanque
table: "student_dorm_bill"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/student_dorm_bill"
---

# YanQue 表结构：student_dorm_bill

## 表说明

学生宿舍缴费账单表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 宿舍缴费账单ID |
| `bill_no` | `varchar(64)` | 宿舍缴费账单号 |
| `student_id` | `bigint` | 学生ID |
| `student_name` | `varchar(50)` | 学生姓名 |
| `student_phone` | `varchar(30)` | 学生手机号 |
| `dorm_id` | `bigint` | 宿舍分配ID |
| `bill_title` | `varchar(100)` | 账单标题 |
| `fee_type` | `varchar(30)` | 费用类型：DORM_FEE住宿费，WATER_ELECTRIC水电费 |
| `bill_period` | `varchar(50)` | 账期 |
| `payable_amount` | `decimal(10,2)` | 应缴金额 |
| `paid_amount` | `decimal(10,2)` | 实缴金额 |
| `status` | `varchar(30)` | 账单状态：UNPAID待缴费，PAYING支付中，PAID已支付，CLOSED已关闭 |
| `alipay_trade_no` | `varchar(128)` | 支付宝交易号 |
| `pay_success_time` | `datetime` | 支付成功时间 |
| `due_date` | `datetime` | 缴费截止时间 |
| `remark` | `varchar(500)` | 备注 |
| `created_by` | `bigint` | 创建人用户ID |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `student_name`：`NAME`
  - `student_phone`：`PHONE`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_bill_no` | `bill_no` | 是 |
| `idx_student_id` | `student_id` | 否 |
| `idx_dorm_id` | `dorm_id` | 否 |
| `idx_status` | `status` | 否 |
| `idx_alipay_trade_no` | `alipay_trade_no` | 否 |
