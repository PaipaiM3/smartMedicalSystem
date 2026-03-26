-- ============================================================
-- 智能医疗服务管理系统 - 按角色补充用户数据
-- 版本：v4
-- 说明：为 sys_role 中每个 role_code 至少补充 1 个对应用户；
--       常见角色（DOCTOR/NURSE/PATIENT/RECEPTIONIST/ADMIN）按医院人员比例额外补充。
-- 约定：密码统一为 123456 的 BCrypt 加密值（与 v3_init_rbac_tables.sql 保持一致）
-- 数据库：MySQL 8.x，字符集：utf8mb4
-- ============================================================

SET NAMES utf8mb4;
USE `medical_service`;

-- 密码：123456（BCrypt）
SET @PWD_123456 = '$2a$10$rQbeBO6qTw.ha5Dy6RgaduzMUl9k5bvln4l6mLXTLd5rIdTz27WbO';

-- ----------------------------
-- 1) 新增用户（按命名规则：username 采用英文/拼音 + 数字；name 使用中文，含三字名/复姓四字名）
-- ----------------------------
INSERT IGNORE INTO `sys_user`
(`username`, `password`, `name`, `email`, `mobile_phone`, `status`, `created_time`, `updated_time`)
VALUES
-- 超级管理员/管理员（极少数）
('superadmin02', @PWD_123456, '欧阳若兰', 'superadmin02@hospital.com', '13900000100', 1, NOW(), NOW()),
('admin_ops01',  @PWD_123456, '司马云飞', 'admin_ops01@hospital.com',  '13900000101', 1, NOW(), NOW()),
('admin_ops02',  @PWD_123456, '上官婉儿', 'admin_ops02@hospital.com',  '13900000102', 1, NOW(), NOW()),
('it_admin01',   @PWD_123456, '诸葛明轩', 'it_admin01@hospital.com',   '13900000103', 1, NOW(), NOW()),
('hr_manager01', @PWD_123456, '司徒雨薇', 'hr_manager01@hospital.com', '13900000104', 1, NOW(), NOW()),
('finance01',    @PWD_123456, '南宫景然', 'finance01@hospital.com',    '13900000105', 1, NOW(), NOW()),
('auditor01',    @PWD_123456, '慕容清扬', 'auditor01@hospital.com',    '13900000106', 1, NOW(), NOW()),

-- 挂号收费/结算/医保/预约协调（少量）
('reception01',  @PWD_123456, '王小明',   'reception01@hospital.com',  '13900000110', 1, NOW(), NOW()),
('reception02',  @PWD_123456, '李晓雪',   'reception02@hospital.com',  '13900000111', 1, NOW(), NOW()),
('billing01',    @PWD_123456, '张雨涵',   'billing01@hospital.com',    '13900000112', 1, NOW(), NOW()),
('insurance01',  @PWD_123456, '赵子轩',   'insurance01@hospital.com',  '13900000113', 1, NOW(), NOW()),
('appoint01',    @PWD_123456, '刘思远',   'appoint01@hospital.com',    '13900000114', 1, NOW(), NOW()),
('patient_svc01',@PWD_123456, '陈梓萱',   'patient_svc01@hospital.com','13900000115', 1, NOW(), NOW()),

-- 医生（适量）
('doctor01',     @PWD_123456, '周亦辰',   'doctor01@hospital.com',     '13900000200', 1, NOW(), NOW()),
('doctor02',     @PWD_123456, '吴欣怡',   'doctor02@hospital.com',     '13900000201', 1, NOW(), NOW()),
('doctor03',     @PWD_123456, '郑浩然',   'doctor03@hospital.com',     '13900000202', 1, NOW(), NOW()),
('er_doctor01',  @PWD_123456, '沈星河',   'er_doctor01@hospital.com',  '13900000203', 1, NOW(), NOW()),
('internist01',  @PWD_123456, '韩子墨',   'internist01@hospital.com',  '13900000204', 1, NOW(), NOW()),
('surgeon01',    @PWD_123456, '范思哲',   'surgeon01@hospital.com',    '13900000205', 1, NOW(), NOW()),
('pediatric01',  @PWD_123456, '邹嘉怡',   'pediatric01@hospital.com',  '13900000206', 1, NOW(), NOW()),
('gynecologist01', @PWD_123456, '顾念安', 'gynecologist01@hospital.com','13900000210', 1, NOW(), NOW()),
('orthopedist01',  @PWD_123456, '林知夏', 'orthopedist01@hospital.com', '13900000211', 1, NOW(), NOW()),
('dermatologist01',@PWD_123456, '宋清禾', 'dermatologist01@hospital.com','13900000212', 1, NOW(), NOW()),
('ophthalmologist01',@PWD_123456,'江望舒','ophthalmologist01@hospital.com','13900000213',1, NOW(), NOW()),
('ent_doctor01',   @PWD_123456, '陆星燃', 'ent_doctor01@hospital.com', '13900000214', 1, NOW(), NOW()),
('cardiologist01', @PWD_123456, '程予安', 'cardiologist01@hospital.com','13900000215', 1, NOW(), NOW()),
('neurologist01',  @PWD_123456, '夏清澜', 'neurologist01@hospital.com', '13900000216', 1, NOW(), NOW()),
('oncologist01',   @PWD_123456, '唐知远', 'oncologist01@hospital.com',  '13900000217', 1, NOW(), NOW()),
('psychiatrist01', @PWD_123456, '秦若宁', 'psychiatrist01@hospital.com','13900000218', 1, NOW(), NOW()),
('tcm_doctor01', @PWD_123456, '钱若瑜',   'tcm_doctor01@hospital.com', '13900000207', 1, NOW(), NOW()),
('rehab01',      @PWD_123456, '许清欢',   'rehab01@hospital.com',      '13900000208', 1, NOW(), NOW()),
('anesth01',     @PWD_123456, '何以安',   'anesth01@hospital.com',     '13900000209', 1, NOW(), NOW()),
('nutritionist01',@PWD_123456, '柳星辰',  'nutritionist01@hospital.com','13900000219', 1, NOW(), NOW()),

-- 护士/药师/相关医技（较多）
('nurse01',      @PWD_123456, '吕佳宁',   'nurse01@hospital.com',      '13900000300', 1, NOW(), NOW()),
('nurse02',      @PWD_123456, '施梓涵',   'nurse02@hospital.com',      '13900000301', 1, NOW(), NOW()),
('nurse_head01', @PWD_123456, '蒋晨曦',   'nurse_head01@hospital.com', '13900000302', 1, NOW(), NOW()),
('icu_nurse01',  @PWD_123456, '孔令仪',   'icu_nurse01@hospital.com',  '13900000303', 1, NOW(), NOW()),
('or_nurse01',   @PWD_123456, '曹一诺',   'or_nurse01@hospital.com',   '13900000304', 1, NOW(), NOW()),
('em_nurse01',   @PWD_123456, '严诗雅',   'em_nurse01@hospital.com',   '13900000305', 1, NOW(), NOW()),
('pharmacy01',   @PWD_123456, '华子衿',   'pharmacy01@hospital.com',   '13900000306', 1, NOW(), NOW()),
('pharmacy_tech01',@PWD_123456,'金雨泽',  'pharmacy_tech01@hospital.com','13900000307',1, NOW(), NOW()),
('dispense01',   @PWD_123456, '魏慕白',   'dispense01@hospital.com',   '13900000308', 1, NOW(), NOW()),
('lab_tech01',   @PWD_123456, '陶知行',   'lab_tech01@hospital.com',   '13900000309', 1, NOW(), NOW()),
('radiology01',  @PWD_123456, '姜晚晴',   'radiology01@hospital.com',  '13900000310', 1, NOW(), NOW()),
('pathologist01',@PWD_123456, '戚景行',   'pathologist01@hospital.com','13900000311', 1, NOW(), NOW()),
('bloodbank01',  @PWD_123456, '周怀瑾',   'bloodbank01@hospital.com',  '13900000313', 1, NOW(), NOW()),
('equip_tech01', @PWD_123456, '谢知远',   'equip_tech01@hospital.com', '13900000312', 1, NOW(), NOW()),

-- 其他员工（后勤/安保/保洁/采购/库管/病案/质控/科研/见习等）
('dept_director01', @PWD_123456, '邹慕辰', 'dept_director01@hospital.com', '13900000400', 1, NOW(), NOW()),
('pharmacy_mgr01',  @PWD_123456, '喻嘉禾', 'pharmacy_mgr01@hospital.com',  '13900000401', 1, NOW(), NOW()),
('qc_officer01',    @PWD_123456, '柏言希', 'qc_officer01@hospital.com',    '13900000402', 1, NOW(), NOW()),
('med_record01',    @PWD_123456, '水清宁', 'med_record01@hospital.com',    '13900000403', 1, NOW(), NOW()),
('security01',      @PWD_123456, '窦子昂', 'security01@hospital.com',      '13900000404', 1, NOW(), NOW()),
('cleaner01',       @PWD_123456, '章雨桐', 'cleaner01@hospital.com',       '13900000405', 1, NOW(), NOW()),
('logistics01',     @PWD_123456, '云若安', 'logistics01@hospital.com',     '13900000406', 1, NOW(), NOW()),
('procure01',       @PWD_123456, '苏慕言', 'procure01@hospital.com',       '13900000407', 1, NOW(), NOW()),
('warehouse01',     @PWD_123456, '潘子衿', 'warehouse01@hospital.com',     '13900000408', 1, NOW(), NOW()),
('volunteer01',     @PWD_123456, '葛思齐', 'volunteer01@hospital.com',     '13900000409', 1, NOW(), NOW()),
('research01',      @PWD_123456, '奚知夏', 'research01@hospital.com',      '13900000410', 1, NOW(), NOW()),
('student01',       @PWD_123456, '樊星然', 'student01@hospital.com',       '13900000411', 1, NOW(), NOW()),

-- 患者（数量相对更多）
('patient1001',  @PWD_123456, '彭子涵',   'patient1001@hospital.com',  '13900000500', 1, NOW(), NOW()),
('patient1002',  @PWD_123456, '郎清扬',   'patient1002@hospital.com',  '13900000501', 1, NOW(), NOW()),
('patient1003',  @PWD_123456, '鲁安然',   'patient1003@hospital.com',  '13900000502', 1, NOW(), NOW()),
('patient1004',  @PWD_123456, '韦子衿',   'patient1004@hospital.com',  '13900000503', 1, NOW(), NOW()),
('patient1005',  @PWD_123456, '昌若宁',   'patient1005@hospital.com',  '13900000504', 1, NOW(), NOW()),
('patient1006',  @PWD_123456, '马星宇',   'patient1006@hospital.com',  '13900000505', 1, NOW(), NOW()),
('patient1007',  @PWD_123456, '苗若溪',   'patient1007@hospital.com',  '13900000506', 1, NOW(), NOW()),
('patient1008',  @PWD_123456, '凤知意',   'patient1008@hospital.com',  '13900000507', 1, NOW(), NOW()),
('patient1009',  @PWD_123456, '花清妍',   'patient1009@hospital.com',  '13900000508', 1, NOW(), NOW()),
('patient1010',  @PWD_123456, '方思远',   'patient1010@hospital.com',  '13900000509', 1, NOW(), NOW());

-- ----------------------------
-- 2) 分配角色（按原脚本命名规则：INSERT IGNORE + SELECT user_id/role_id）
-- ----------------------------

-- SUPER_ADMIN / ADMIN / IT / HR / FINANCE / AUDITOR
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'superadmin02' AND r.role_code = 'SUPER_ADMIN';

INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'admin_ops01' AND r.role_code = 'ADMIN';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'admin_ops02' AND r.role_code = 'ADMIN';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'it_admin01' AND r.role_code = 'IT_ADMIN';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'hr_manager01' AND r.role_code = 'HR_MANAGER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'finance01' AND r.role_code = 'FINANCE_MANAGER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'auditor01' AND r.role_code = 'AUDITOR';

-- RECEPTIONIST / BILLING / INSURANCE / APPOINTMENT / PATIENT_SERVICE
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'reception01' AND r.role_code = 'RECEPTIONIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'reception02' AND r.role_code = 'RECEPTIONIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'billing01' AND r.role_code = 'BILLING_CLERK';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'insurance01' AND r.role_code = 'INSURANCE_CLERK';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'appoint01' AND r.role_code = 'APPOINTMENT_COORDINATOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient_svc01' AND r.role_code = 'PATIENT_SERVICE';

-- 医生相关（每个细分角色至少 1 名）
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'doctor01' AND r.role_code = 'DOCTOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'doctor02' AND r.role_code = 'DOCTOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'doctor03' AND r.role_code = 'DOCTOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'er_doctor01' AND r.role_code = 'ER_DOCTOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'pediatric01' AND r.role_code = 'PEDIATRICIAN';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'internist01' AND r.role_code = 'INTERNIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'surgeon01' AND r.role_code = 'SURGEON';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'gynecologist01' AND r.role_code = 'GYNECOLOGIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'orthopedist01' AND r.role_code = 'ORTHOPEDIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'dermatologist01' AND r.role_code = 'DERMATOLOGIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'ophthalmologist01' AND r.role_code = 'OPHTHALMOLOGIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'ent_doctor01' AND r.role_code = 'ENT_DOCTOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'cardiologist01' AND r.role_code = 'CARDIOLOGIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'neurologist01' AND r.role_code = 'NEUROLOGIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'oncologist01' AND r.role_code = 'ONCOLOGIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'psychiatrist01' AND r.role_code = 'PSYCHIATRIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'tcm_doctor01' AND r.role_code = 'TCM_DOCTOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'rehab01' AND r.role_code = 'REHAB_DOCTOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'anesth01' AND r.role_code = 'ANESTHESIOLOGIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'nutritionist01' AND r.role_code = 'NUTRITIONIST';

-- 护士/药师/医技（每个细分角色至少 1 名；同时给基础 NURSE 角色配多名）
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'nurse01' AND r.role_code = 'NURSE';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'nurse02' AND r.role_code = 'NURSE';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'nurse_head01' AND r.role_code = 'NURSE_HEAD';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'icu_nurse01' AND r.role_code = 'ICU_NURSE';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'or_nurse01' AND r.role_code = 'OR_NURSE';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'em_nurse01' AND r.role_code = 'EMERGENCY_NURSE';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'pharmacy01' AND r.role_code = 'PHARMACY_MANAGER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'pharmacy_tech01' AND r.role_code = 'PHARMACY_TECH';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'dispense01' AND r.role_code = 'DISPENSARY_CLERK';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'lab_tech01' AND r.role_code = 'LAB_TECH';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'radiology01' AND r.role_code = 'RADIOLOGY_TECH';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'pathologist01' AND r.role_code = 'PATHOLOGIST';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'bloodbank01' AND r.role_code = 'BLOOD_BANK_TECH';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'equip_tech01' AND r.role_code = 'MEDICAL_EQUIPMENT_TECH';

-- 其他员工（每个细分角色至少 1 名）
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'dept_director01' AND r.role_code = 'DEPT_DIRECTOR';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'pharmacy_mgr01' AND r.role_code = 'PHARMACY_MANAGER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'qc_officer01' AND r.role_code = 'QC_OFFICER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'med_record01' AND r.role_code = 'MEDICAL_RECORD_CLERK';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'security01' AND r.role_code = 'SECURITY_GUARD';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'cleaner01' AND r.role_code = 'CLEANER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'logistics01' AND r.role_code = 'LOGISTICS_STAFF';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'procure01' AND r.role_code = 'PROCUREMENT_STAFF';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'warehouse01' AND r.role_code = 'WAREHOUSE_KEEPER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'volunteer01' AND r.role_code = 'VOLUNTEER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'research01' AND r.role_code = 'CLINICAL_RESEARCHER';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'student01' AND r.role_code = 'MEDICAL_STUDENT';

-- 患者（每条对应 PATIENT）
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1001' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1002' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1003' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1004' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1005' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1006' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1007' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1008' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1009' AND r.role_code = 'PATIENT';
INSERT IGNORE INTO sys_user_role (user_id, role_id, created_time)
SELECT u.user_id, r.role_id, NOW() FROM sys_user u, sys_role r
WHERE u.username = 'patient1010' AND r.role_code = 'PATIENT';

-- 额外兜底：若将来角色表新增了 role_code，本脚本不会自动生成用户；可按同一规则继续追加。

