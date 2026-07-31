---
title: "YanQue 业务术语：考试"
type: text-to-sql-business-terms
system: yanque
businessDomain: "考试"
resourceUri: "bear://yanque/text-to-sql/business/exam"
---

# YanQue 业务术语：考试

## 业务域说明

题库、试卷、考试安排、考试记录、答题明细和成绩。

## 核心业务术语

| 术语 | 含义 | 主要表字段 |
| --- | --- | --- |
| 题库题目 | 题库中的独立题目，包含题型、题干、正确答案、解析、难度和状态。 | `exam_question.id`, `exam_question.question_type`, `exam_question.question_content`, `exam_question.answer_content`, `exam_question.difficulty`, `exam_question.status` |
| 题目选项 | 客观题的选项信息。 | `exam_question_option.question_id` |
| 题目课程关系 | 题目与课程的归属关系。 | `exam_question_course.question_id`, `exam_question_course.course_id` |
| 试卷 | 一套考试用卷。 | `exam_paper.id`, `exam_paper.paper_name` |
| 试卷题目 | 试卷与题目的关联及题目在试卷中的分值。 | `exam_paper_question.paper_id`, `exam_paper_question.question_id`, `exam_paper_question.question_score` |
| 考试安排 | 面向班级发布的考试，包含试卷、班级、开始/截止时间、考试时长和监考老师。 | `exam.id`, `exam.paper_id`, `exam.class_id`, `exam.start_time`, `exam.end_time`, `exam.duration_minutes`, `exam.invigilator_user_id` |
| 学生考试记录 | 学生参加某场考试的记录，包含实际开始时间、提交时间、考试状态、批改状态和得分。 | `student_exam_record.exam_id`, `student_exam_record.student_id`, `student_exam_record.status`, `student_exam_record.grading_status`, `student_exam_record.score` |
| 答题明细 | 学生在考试中每道题的作答记录，包含题型、题目分值、答案内容、是否正确和本题得分。 | `student_exam_answer.record_id`, `student_exam_answer.question_id`, `student_exam_answer.correct`, `student_exam_answer.score` |
| 已提交考试 | 学生考试记录状态为已提交。 | `student_exam_record.status = 'SUBMITTED'` |
| 考试超时 | 学生考试记录状态为超时。 | `student_exam_record.status = 'TIMEOUT'` |
| 批改完成 | 学生考试记录批改状态为完成。 | `student_exam_record.grading_status = 'COMPLETED'` |

## 常用状态值

| 对象 | 字段 | 值 | 含义 |
| --- | --- | --- | --- |
| 学生考试记录 | `student_exam_record.status` | `IN_PROGRESS` | 答题中 |
| 学生考试记录 | `student_exam_record.status` | `SUBMITTED` | 已提交 |
| 学生考试记录 | `student_exam_record.status` | `TIMEOUT` | 超时 |
| 学生考试记录 | `student_exam_record.grading_status` | `PENDING` | 待批改 |
| 学生考试记录 | `student_exam_record.grading_status` | `GRADING` | 批改中 |
| 学生考试记录 | `student_exam_record.grading_status` | `COMPLETED` | 批改完成 |
| 答题明细 | `student_exam_answer.correct` | `1` | 正确 |
| 答题明细 | `student_exam_answer.correct` | `0` | 错误 |

## 常用关联

| 关系 | 连接字段 |
| --- | --- |
| 考试关联试卷 | `exam.paper_id = exam_paper.id` |
| 考试关联班级 | `exam.class_id = sys_class.id` |
| 考试记录关联考试 | `student_exam_record.exam_id = exam.id` |
| 考试记录关联学生 | `student_exam_record.student_id = student.id` |
| 答题明细关联考试记录 | `student_exam_answer.record_id = student_exam_record.id` |
| 答题明细关联考试 | `student_exam_answer.exam_id = exam.id` |
| 答题明细关联试卷 | `student_exam_answer.paper_id = exam_paper.id` |
| 答题明细关联题目 | `student_exam_answer.question_id = exam_question.id` |
| 试卷关联题目 | `exam_paper_question.paper_id = exam_paper.id` and `exam_paper_question.question_id = exam_question.id` |
