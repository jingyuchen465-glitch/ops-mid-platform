---
title: "YanQue Text-to-SQL 表结构资源目录"
type: text-to-sql-schema-index
system: yanque
resourceUri: "bear://yanque/text-to-sql/schema"
---

# YanQue Text-to-SQL 表结构资源目录

本目录由 YanQue 现有 Text-to-SQL 元数据生成，用于审核后上传到 Bear MCP Resource。

## 上传建议

- 总目录 Resource URI：`bear://yanque/text-to-sql/schema`
- 单表 Resource URI：`bear://yanque/text-to-sql/schema/{tableName}`
- Resource 内容类型：Markdown
- 使用场景：Agent 生成 SQL 前读取表结构、字段含义、关联关系和字段安全标记。

## 表清单

### AI配置

- [`prompt_template`](./prompt_template.md)：AI 提示词模板，维护提示词编码、名称、当前生效版本和状态。
- [`prompt_template_version`](./prompt_template_version.md)：提示词模板历史版本，记录版本号、内容、备注和状态。

### AI问答

- [`ai_chat_message`](./ai_chat_message.md)：AI 问答消息，记录用户或助手角色、消息内容、模型和 Token 消耗。
- [`ai_chat_session`](./ai_chat_session.md)：学生 AI 问答会话，记录会话标题、状态和压缩后的历史摘要。

### 产品订单

- [`order_product`](./order_product.md)：可售课程产品及价格信息。

### 作业

- [`course_homework_template`](./course_homework_template.md)：课程作业标准或训练集，按课程、授课方式、阶段或天数定义。
- [`homework`](./homework.md)：班级作业，记录作业日期、内容文件、答案文件、开始和截止时间。
- [`homework_submission`](./homework_submission.md)：学生作业提交记录，包含提交时间、是否逾期、教师批注和成绩。

### 学习计划

- [`student_learning_calendar`](./student_learning_calendar.md)：线上学生每日学习日历，记录学习日期、阶段、计划天数和完成状态。
- [`student_learning_plan`](./student_learning_plan.md)：线上学生学习计划，关联学生、课程、入学 SOP 和计划状态。

### 学生

- [`student`](./student.md)：学生基础档案，包含学生编号、姓名、手机号、学校、专业、班级和状态。

### 学生产品

- [`student_product`](./student_product.md)：学生已购产品关系，记录来源支付订单和产品开通状态。

### 学生回访

- [`student_followup_record`](./student_followup_record.md)：学生回访记录，包含回访标签、应回访日期、回访内容、回访人和状态。
- [`student_followup_tag`](./student_followup_tag.md)：学生回访标签配置，定义各标签对应的回访间隔和启用状态。

### 学生服务

- [`student_sop`](./student_sop.md)：学生入学 SOP 记录，包含导师、SOP 时间、视频和完成状态。

### 未标注

- [`course_video`](./course_video.md)：课程视频表
- [`dorm_assignment`](./dorm_assignment.md)：宿舍入住记录表
- [`dorm_bed`](./dorm_bed.md)：宿舍床位表
- [`dorm_building`](./dorm_building.md)：宿舍楼栋表
- [`dorm_room`](./dorm_room.md)：宿舍房间表
- [`student_dorm`](./student_dorm.md)：学生宿舍分配表
- [`student_dorm_bill`](./student_dorm_bill.md)：学生宿舍缴费账单表
- [`student_video_progress`](./student_video_progress.md)：学生视频观看进度表
- [`sys_config`](./sys_config.md)：系统配置表
- [`sys_permission`](./sys_permission.md)：权限表
- [`sys_role`](./sys_role.md)：角色表
- [`sys_role_permission`](./sys_role_permission.md)：角色权限关联表
- [`sys_user`](./sys_user.md)：用户表
- [`sys_user_role`](./sys_user_role.md)：用户角色关联表

### 校区

- [`sys_campus`](./sys_campus.md)：校区基础信息，包含校区地点和负责人。

### 班级

- [`sys_class`](./sys_class.md)：班级信息，包含班级期数、班主任、校区和课程。

### 班级教学

- [`sys_class_duty`](./sys_class_duty.md)：班级或校区值班安排，记录老师、日期、时间段和值班类型。
- [`sys_class_schedule`](./sys_class_schedule.md)：班级每日课表，记录上课日期、老师、课程内容和课程阶段。

### 知识库

- [`knowledge_base`](./knowledge_base.md)：知识库基础信息，包含名称、描述和启用状态。
- [`knowledge_document`](./knowledge_document.md)：知识库文档，记录对象存储文件、版本、向量入库状态和切片数量。

### 考试

- [`exam`](./exam.md)：考试安排，关联试卷和班级，记录考试时间、时长、监考人和结果可见状态。
- [`exam_paper`](./exam_paper.md)：试卷信息，包含试卷名称、所属课程、阶段和总分。
- [`exam_paper_question`](./exam_paper_question.md)：试卷与题目的关联关系，记录题目在试卷中的分值。
- [`student_exam_answer`](./student_exam_answer.md)：学生单题答题记录，包含题目、答案内容、对错和得分。
- [`student_exam_record`](./student_exam_record.md)：学生参加考试的记录，包含开始、截止、提交时间、状态、评分状态和总分。

### 考试题库

- [`exam_question`](./exam_question.md)：题库题目，记录题型、题干、答案、难度和启用状态。
- [`exam_question_course`](./exam_question_course.md)：题目与课程阶段的关联关系。
- [`exam_question_option`](./exam_question_option.md)：选择题选项，记录选项标识和选项内容。

### 订单支付

- [`order_payment`](./order_payment.md)：支付订单流水，记录支付金额、支付状态、支付成功时间和退款申请金额。
- [`prepay_order`](./prepay_order.md)：预支付订单，记录学生购买产品、原价、优惠和待支付或已支付状态。

### 订单退款

- [`order_refund`](./order_refund.md)：退款订单流水，记录退款金额、退款状态、退款原因和退款成功时间。

### 课程

- [`sys_course`](./sys_course.md)：课程基础信息，包含课程名称、天数和授课方式。
- [`sys_course_detail`](./sys_course_detail.md)：课程阶段或每日课程内容，记录阶段名称、天数和上课内容。
