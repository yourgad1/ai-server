package com.ai.server.agent.ai.agent.constant;

/**
 * 提示词类型常量
 */
public class PromptType {

    /**
     * 数据看板系统意图提示词
     */
    public static final String DATA_BOARD_SYSTEM_INTENT = "data_board_system_intent";

    /**
     * 数据看板问题匹配提示词
     */
    public static final String DATA_BOARD_QUESTION_MATCH = "data_board_question_match";

    /**
     * 文档生成提示词
     */
    public static final String DOCUMENT_GENERATE = "document_generate";

    /**
     * 图表生成提示词
     */
    public static final String CHART_GENERATE = "chart_generate";

    /**
     * 信息提取系统意图提示词
     */
    public static final String INFO_EXTRACT_SYSTEM_INTENT = "info_extract_system_intent";

    /**
     * 负荷排查填充审核提示词
     */
    public static final String LOAD_CHECK_FILL_IN_REVIEW = "load_check_fill_in_review";

    /**
     * 负荷排查表单识别提示词
     */
    public static final String LOAD_CHECK_FORM_REC = "load_check_form_rec";

    /**
     * 负荷排查用户列表检查提示词
     */
    public static final String LOAD_CHECK_USER_LIST = "load_check_user_list";

    /**
     * 负荷排查总结提示词
     */
    public static final String LOAD_CHECK_SUMMARY = "load_check_summary";

    /**
     * 指标结果系统提示词
     */
    public static final String METRIC_RESULT_SYSTEM = "metric_result_system";

    /**
     * 系统意图识别提示词
     */
    public static final String SYSTEM_DETERMINE_INTENT = "system_determine_intent";

    private PromptType() {
        // 私有构造方法，防止实例化
    }
}
