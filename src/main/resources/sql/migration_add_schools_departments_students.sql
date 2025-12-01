-- ====================================
-- UniSport 学校学院学生表结构补充
-- 用于支持用户注册时的学号验证
-- ====================================

USE unisport;

-- ====================================
-- 学校表
-- ====================================
DROP TABLE IF EXISTS `schools`;
CREATE TABLE `schools` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学校ID',
    `name` VARCHAR(100) NOT NULL COMMENT '学校名称',
    `code` VARCHAR(20) NOT NULL COMMENT '学校代码',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用, 0-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_province_city` (`province`, `city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校表';

-- ====================================
-- 学院表
-- ====================================
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学院ID',
    `school_id` BIGINT NOT NULL COMMENT '学校ID',
    `name` VARCHAR(100) NOT NULL COMMENT '学院名称',
    `code` VARCHAR(20) NOT NULL COMMENT '学院代码',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用, 0-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_school_code` (`school_id`, `code`),
    KEY `idx_school` (`school_id`),
    FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

-- ====================================
-- 学生表
-- 用于验证学生身份（软绑定）
-- ====================================
DROP TABLE IF EXISTS `students`;
CREATE TABLE `students` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `student_id` VARCHAR(30) NOT NULL COMMENT '学号',
    `name` VARCHAR(50) NOT NULL COMMENT '学生姓名',
    `school_id` BIGINT NOT NULL COMMENT '学校ID',
    `department_id` BIGINT NOT NULL COMMENT '学院ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-在校, 0-已毕业/离校',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    KEY `idx_school_dept` (`school_id`, `department_id`),
    KEY `idx_status` (`status`),
    FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`department_id`) REFERENCES `departments`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- ====================================
-- 初始化基础数据
-- ====================================

-- 插入学校数据
INSERT INTO `schools` (`name`, `code`, `province`, `city`, `sort_order`, `status`) VALUES
('清华大学', 'THU', '北京', '北京市', 1, 1),
('北京大学', 'PKU', '北京', '北京市', 2, 1),
('复旦大学', 'FDU', '上海', '上海市', 3, 1),
('上海交通大学', 'SJTU', '上海', '上海市', 4, 1),
('浙江大学', 'ZJU', '浙江', '杭州市', 5, 1);

-- 插入学院数据（清华大学）
INSERT INTO `departments` (`school_id`, `name`, `code`, `sort_order`, `status`) VALUES
(1, '计算机系', 'CS', 1, 1),
(1, '经管学院', 'SEM', 2, 1),
(1, '建筑学院', 'ARCH', 3, 1),
(1, '电子工程系', 'EE', 4, 1),
(1, '自动化系', 'AUTO', 5, 1);

-- 插入学院数据（北京大学）
INSERT INTO `departments` (`school_id`, `name`, `code`, `sort_order`, `status`) VALUES
(2, '信息科学技术学院', 'EECS', 1, 1),
(2, '光华管理学院', 'GSM', 2, 1),
(2, '元培学院', 'YP', 3, 1),
(2, '数学科学学院', 'SMS', 4, 1);

-- 插入学院数据（复旦大学）
INSERT INTO `departments` (`school_id`, `name`, `code`, `sort_order`, `status`) VALUES
(3, '计算机科学技术学院', 'CS', 1, 1),
(3, '管理学院', 'FDSM', 2, 1),
(3, '经济学院', 'SOE', 3, 1);

-- 插入学院数据（上海交通大学）
INSERT INTO `departments` (`school_id`, `name`, `code`, `sort_order`, `status`) VALUES
(4, '电子信息与电气工程学院', 'SEIEE', 1, 1),
(4, '安泰经济与管理学院', 'ACEM', 2, 1),
(4, '机械与动力工程学院', 'SME', 3, 1);

-- 插入学院数据（浙江大学）
INSERT INTO `departments` (`school_id`, `name`, `code`, `sort_order`, `status`) VALUES
(5, '计算机科学与技术学院', 'CS', 1, 1),
(5, '管理学院', 'SOM', 2, 1),
(5, '竺可桢学院', 'CKC', 3, 1);

-- ====================================
-- 插入测试学生数据
-- 用于测试注册功能
-- ====================================

-- 清华大学学生
INSERT INTO `students` (`student_id`, `name`, `school_id`, `department_id`, `status`) VALUES
('2024001001', '张三', 1, 1, 1),  -- 清华计算机系
('2024001002', '李四', 1, 1, 1),  -- 清华计算机系
('2024002001', '王五', 1, 2, 1),  -- 清华经管学院
('2024002002', '赵六', 1, 2, 1),  -- 清华经管学院
('2024003001', '孙七', 1, 3, 1);  -- 清华建筑学院

-- 北京大学学生
INSERT INTO `students` (`student_id`, `name`, `school_id`, `department_id`, `status`) VALUES
('2024101001', '周八', 2, 6, 1),  -- 北大信科
('2024102001', '吴九', 2, 7, 1),  -- 北大光华
('2024103001', '郑十', 2, 8, 1);  -- 北大元培

-- 复旦大学学生
INSERT INTO `students` (`student_id`, `name`, `school_id`, `department_id`, `status`) VALUES
('2024201001', '钱一', 3, 10, 1), -- 复旦计算机
('2024202001', '孙二', 3, 11, 1); -- 复旦管理

-- 上海交通大学学生
INSERT INTO `students` (`student_id`, `name`, `school_id`, `department_id`, `status`) VALUES
('2024301001', '陈三', 4, 13, 1), -- 交大电院
('2024302001', '林四', 4, 14, 1); -- 交大安泰

-- 浙江大学学生
INSERT INTO `students` (`student_id`, `name`, `school_id`, `department_id`, `status`) VALUES
('2024401001', '黄五', 5, 16, 1), -- 浙大计算机
('2024402001', '刘六', 5, 17, 1); -- 浙大管理

-- ====================================
-- 说明
-- ====================================
-- 1. schools表：存储学校基础信息
-- 2. departments表：存储学院信息，关联学校
-- 3. students表：存储学生信息，用于注册时验证学号
-- 4. 注册流程：前端选择学校->加载学院列表->输入学号->后端验证学号是否存在且匹配
-- 5. 软绑定：users表的student_id字段与students表的student_id字段软关联，非外键约束
