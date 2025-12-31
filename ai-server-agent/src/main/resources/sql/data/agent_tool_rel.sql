
-- 4.1 LoadCheckAgent - 工具关联
-- 与老代码一致，添加loadCheckTools和dateTool
INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`)
SELECT
    agent_id,
    'loadCheckTools',
    1
FROM `agent_config` WHERE `agent_name` = 'loadCheckAgent';

INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`)
SELECT
    agent_id,
    'dateTool',
    2
FROM `agent_config` WHERE `agent_name` = 'loadCheckAgent';

-- 4.2 DocumentAgent - 工具关联
-- 与老代码一致，添加documentTools和chartDataTools
INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`)
SELECT
    agent_id,
    'documentTools',
    1
FROM `agent_config` WHERE `agent_name` = 'documentAgent';

INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`)
SELECT
    agent_id,
    'chartDataTools',
    2
FROM `agent_config` WHERE `agent_name` = 'documentAgent';

-- 4.3 SimpleChatClient - 工具关联
-- 与老代码一致，添加dateTool
INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`)
SELECT
    agent_id,
    'dateTool',
    1
FROM `agent_config` WHERE `agent_name` = 'simpleChatClient';

-- 4.4 SystemAgent - 工具关联
INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`)
SELECT
    agent_id,
    'dateTool',
    1
FROM `agent_config` WHERE `agent_name` = 'systemAgent';

INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`)
SELECT
    agent_id,
    'dateTool',
    1
FROM `agent_config` WHERE `agent_name` = 'InfoExtractAgent';

INSERT INTO `agent_tool_rel` (`agent_id`, `tool_name`, `tool_order`)
SELECT
    agent_id,
    'dateTool',
    1
FROM `agent_config` WHERE `agent_name` = 'metricResultAgent';