package com.ai.server.agent.ai.agent.constant;

/**
 * Agent类型常量
 */
public class AgentType {

    /**
     * 数据看板提取Agent
     */
    public static final String DATA_BOARD_EXTRACT = "dataBoardExtractAgent";

    /**
     * 文档处理Agent
     */
    public static final String DOCUMENT = "documentAgent";

    /**
     * 信息提取Agent
     */
    public static final String INFO_EXTRACT = "infoExtractAgent";

    /**
     * 负荷排查Agent
     */
    public static final String LOAD_CHECK = "loadCheckAgent";

    /**
     * 指标结果Agent
     */
    public static final String METRIC_RESULT = "metricResultAgent";

    /**
     * 简单聊天客户端
     */
    public static final String SIMPLE_CHAT = "simpleChatClient";

    /**
     * 系统Agent
     */
    public static final String SYSTEM = "systemAgent";

    /**
     * 意图识别Agent
     */
    public static final String DETERMINE_INTENT = "determineIntentAgent";

    /**
     * 文档文本Agent
     */
    public static final String DOCUMENT_TEXT = "documentTextAgent";

    /**
     * 图表生成Agent
     */
    public static final String CHART_GENERATE = "chartGenerateAgent";

    /**
     * 填充审核Agent
     */
    public static final String FILL_IN_REVIEW = "fillInReviewAgent";

    /**
     * 表单铭牌识别Agent
     */
    public static final String FORM_RECOGNITION = "formRecognitionAgent";

    /**
     * 用户列表Agent
     */
    public static final String USER_LIST = "userListAgent";

    /**
     * 总结Agent
     */
    public static final String SUMMARY = "summaryAgent";

    /**
     * 表单信息Agent
     */
    public static final String FORM_INFO = "formInfoAgent";

    /**
     * 问题匹配Agent
     */
    public static final String QUESTION_MATCH = "questionMatchAgent";

    private AgentType() {
        // 私有构造方法，防止实例化
    }
}
