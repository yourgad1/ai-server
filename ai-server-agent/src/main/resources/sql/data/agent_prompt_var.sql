-- 3.1 DataBoardExtractAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'api_list',
    '{}',
    'dynamic',
    'business',
    'API列表，业务侧传入'
FROM `agent_config` WHERE `agent_name` = 'dataBoardExtractAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'tem',
    '{"packList": [], "expectDate": ""}',
    'static',
    'database',
    '模板结构'
FROM `agent_config` WHERE `agent_name` = 'dataBoardExtractAgent';



-- 添加DataBoardExtractAgent的结果模板静态参数
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'RESULT_INFO_JSON',
    '{\n  "expectDate": "期望日期（格式参照 2015-01-01）",\n  "packList": [\n    {"id": 170, "reportName": "出力对比"},\n    {"id": 180, "reportName": "设备负荷"},\n    ...\n  ]\n}',
    'static',
    'database',
    '结果信息JSON模板'
FROM `agent_config` WHERE `agent_name` = 'dataBoardExtractAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'QUESTION_RESULT_INFO_JSON',
    '{\n  "expectDate": "期望日期(格式参照2025-11-01)",\n  "items": [1,2,3],\n  "questionId": "问题记录qId字段"\n}',
    'static',
    'database',
    '问题结果信息JSON模板'
FROM `agent_config` WHERE `agent_name` = 'dataBoardExtractAgent';

-- 3.2 LoadCheckAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'form_data',
    '{}',
    'dynamic',
    'business',
    '表单数据，业务侧传入'
FROM `agent_config` WHERE `agent_name` = 'loadCheckAgent';

-- 添加LoadCheckAgent的user_condition和user_example静态参数
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'user_condition',
    '- name: CONTRACT_CAP\n   display: 合同容量\n   type: string\t\t\n - name: RUN_CAP\n   display: 运行容量\n   type: string\t\t\n - name: LINE_NAME\n   display: 所属线路名称\n   type: string\n - name: TRANS_NAME\n   display: 所属变电站\n   type: string\n - name: AREA_NAME\n   display: 所属台区\n   type: string\n - name: ORG_ID\n   display: 供电单位标识\n   type: string\n - name: COUNTY_CODE\n   display: 区县级供电单位标识\n   type: string  \n - name: CITY_CODE\n   display: 地市级供电单位标识\n   type: string  \n - name: PROVINCE_CODE\n   display: 省级供电单位标识\n   type: string  \n - name: IS_DOUBLE_HIGH\n   display: 是否双高用户(1是 0否)\n   type: int\n - name: IS_ORDER\n   display: 是否有序用电用户(1是 0否)\n   type: int\n - name: IS_FOCUS_INDUSTRY\n   display: 是否重点行业用户(1是 0否)\n   type: int\n - name: IS_ENERGY_EFFICIENCY\n   display: 是否能效客户(1是 0否)\n   type: int\n - name: USER_TYPE\n   display: 专变/专线用户(0专变/1专线)\n   type: int       \n - name: VOLTAGE_LEVEL\n   display: 电压等级\n   type: string\n - name: INDUSTRY_CODE\n   display: 行业分类代码\n   type: string\n - name: IS_ORDINARY\n   display: 是否普通用户(1是 0否)\n   type: int\n - name: IS_AIR_CONDITIONING\n   display: 是否空调用户(1是 0否)\n   type: int\n - name: IS_ENERGY\n   display: 是否能源用户(1是 0否)\n   type: int\n - name: DISTRIBUTED_CONS_FLAG\n   display: 是否低压分布式光伏客户(1是 0否)\n   type: int\n - name: IS_DCN\n   display: 是否电采暖用户(1是 0否)\n   type: int\n - name: IS_LIGHT\n   display: 是否照明用户(1是 0否)\n   type: int\n - name: IS_CHG\n   display: 是否充电桩用户(1是 0否)\n   type: int\n - name: LAST_PC_TIME\n   display: 上次排查时间(yyyy-mm-dd,不为空)\n   type: date\n - name: IS_XQXY\n   display: 是否需求响应用户(1是 0否)\n   type: int',
    'static',
    'database',
    '用户条件列表'
FROM `agent_config` WHERE `agent_name` = 'loadCheckAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'user_example',
    '[\n  {\n    "name": "RUN_CAP",\n    "display": "运行容量",\n    "operator": "=",\n    "value": "200"\n  },\n  {\n    "name": "IS_XQXY",\n    "display": "是否需求响应用户",\n    "operator": "=",\n    "value": "1"\n  }\n]',
    'static',
    'database',
    '用户示例'
FROM `agent_config` WHERE `agent_name` = 'loadCheckAgent';

-- 3.3 SystemAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'user_query',
    '',
    'dynamic',
    'business',
    '用户查询，业务侧传入'
FROM `agent_config` WHERE `agent_name` = 'systemAgent';

-- 3.4 InfoExtractAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'tem1',
    '{"metricName":"重要用户数","dataDate":[{current_data}],"mgtOrgCode":[34101]}',
    'static',
    'database',
    '完整输出模板'
FROM `agent_config` WHERE `agent_name` = 'infoExtractAgent';

-- 添加current_date变量，用于动态生成当前日期
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'current_date',
    '',
    'dynamic',
    'system',
    '当前日期，系统自动生成'
FROM `agent_config` WHERE `agent_name` = 'infoExtractAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'tem2',
    '{"metricName":"重要用户数","dataDate":[],"mgtOrgCode":[]}',
    'static',
    'database',
    '部分输出模板'
FROM `agent_config` WHERE `agent_name` = 'infoExtractAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'tem3',
    '{"metricName":"未找到相关指标"}',
    'static',
    'database',
    '未找到指标模板'
FROM `agent_config` WHERE `agent_name` = 'infoExtractAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'metric',
    '',
    'dynamic',
    'business',
    '系统指标列表'
FROM `agent_config` WHERE `agent_name` = 'infoExtractAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'orgCode',
    '',
    'dynamic',
    'business',
    '组织机构编码'
FROM `agent_config` WHERE `agent_name` = 'infoExtractAgent';

-- 3.5 documentTextAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'name',
    '',
    'dynamic',
    'business',
    '文档名称'
FROM `agent_config` WHERE `agent_name` = 'documentTextAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'document',
    '',
    'dynamic',
    'business',
    '文档内容'
FROM `agent_config` WHERE `agent_name` = 'documentTextAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'tem',
    '{"fileName": "文档名称", "content": [{"order":"占位符次序","change":"替换占位符的文字","isChange":"true/false 是否有变更内容"}], "text": "占位符替换后的文档内容"}',
    'static',
    'database',
    '输出模板'
FROM `agent_config` WHERE `agent_name` = 'documentTextAgent';

-- 3.6 chartGenerateAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'document',
    '',
    'dynamic',
    'business',
    '文档内容'
FROM `agent_config` WHERE `agent_name` = 'chartGenerateAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'tem',
    '{"chartName": "图表名称", "chartType": "图表类型（bar/pie/line 三选一）", "chartData": "图表数据"}',
    'static',
    'database',
    '输出模板'
FROM `agent_config` WHERE `agent_name` = 'chartGenerateAgent';

-- 3.7 formRecognitionAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'formInfo',
    '',
    'dynamic',
    'business',
    '表单信息JSON格式'
FROM `agent_config` WHERE `agent_name` = 'formRecognitionAgent';

-- 3.8 userListAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'user_condition',
    '- name: CONTRACT_CAP\n   display: 合同容量\n   type: string\t\t\n - name: RUN_CAP\n   display: 运行容量\n   type: string\t\t\n - name: LINE_NAME\n   display: 所属线路名称\n   type: string\n - name: TRANS_NAME\n   display: 所属变电站\n   type: string\n - name: AREA_NAME\n   display: 所属台区\n   type: string\n - name: ORG_ID\n   display: 供电单位标识\n   type: string\n - name: COUNTY_CODE\n   display: 区县级供电单位标识\n   type: string  \n - name: CITY_CODE\n   display: 地市级供电单位标识\n   type: string  \n - name: PROVINCE_CODE\n   display: 省级供电单位标识\n   type: string  \n - name: IS_DOUBLE_HIGH\n   display: 是否双高用户(1是 0否)\n   type: int\n - name: IS_ORDER\n   display: 是否有序用电用户(1是 0否)\n   type: int\n - name: IS_FOCUS_INDUSTRY\n   display: 是否重点行业用户(1是 0否)\n   type: int\n - name: IS_ENERGY_EFFICIENCY\n   display: 是否能效客户(1是 0否)\n   type: int\n - name: USER_TYPE\n   display: 专变/专线用户(0专变/1专线)\n   type: int       \n - name: VOLTAGE_LEVEL\n   display: 电压等级\n   type: string\n - name: INDUSTRY_CODE\n   display: 行业分类代码\n   type: string\n - name: IS_ORDINARY\n   display: 是否普通用户(1是 0否)\n   type: int\n - name: IS_AIR_CONDITIONING\n   display: 是否空调用户(1是 0否)\n   type: int\n - name: IS_ENERGY\n   display: 是否能源用户(1是 0否)\n   type: int\n - name: DISTRIBUTED_CONS_FLAG\n   display: 是否低压分布式光伏客户(1是 0否)\n   type: int\n - name: IS_DCN\n   display: 是否电采暖用户(1是 0否)\n   type: int\n - name: IS_LIGHT\n   display: 是否照明用户(1是 0否)\n   type: int\n - name: IS_CHG\n   display: 是否充电桩用户(1是 0否)\n   type: int\n - name: LAST_PC_TIME\n   display: 上次排查时间(yyyy-mm-dd,不为空)\n   type: date\n - name: IS_XQXY\n   display: 是否需求响应用户(1是 0否)\n   type: int',
    'static',
    'database',
    '用户条件列表'
FROM `agent_config` WHERE `agent_name` = 'userListAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'user_example',
    '[\n  {\n    "name": "RUN_CAP",\n    "display": "运行容量",\n    "operator": "=",\n    "value": "200"\n  },\n  {\n    "name": "IS_XQXY",\n    "display": "是否需求响应用户",\n    "operator": "=",\n    "value": "1"\n  }\n]',
    'static',
    'database',
    '用户示例'
FROM `agent_config` WHERE `agent_name` = 'userListAgent';

-- 3.9 questionMatchAgent - 提示词变量
INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'today',
    '',
    'dynamic',
    'system',
    '当前日期，系统自动生成'
FROM `agent_config` WHERE `agent_name` = 'questionMatchAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'question_list',
    '',
    'dynamic',
    'business',
    '已有问题列表'
FROM `agent_config` WHERE `agent_name` = 'questionMatchAgent';

INSERT INTO `agent_prompt_var` (`agent_id`, `var_key`, `var_value`, `var_type`, `var_source`, `description`)
SELECT
    agent_id,
    'tem',
    '{"expectDate": "期望日期(格式参照2025-11-01)", "items": [1,2,3], "questionId": "问题记录qId字段"}',
    'static',
    'database',
    '输出模板'
FROM `agent_config` WHERE `agent_name` = 'questionMatchAgent';
