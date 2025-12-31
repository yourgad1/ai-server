package com.ai.server.agent.ai.constant;

/**
 * 智能体类型
 */
public class AgentTypeConstant {
    //意图判断
    public static final String SYS_DETERMIN_INTENT ="determineIntent";
    //yml文件生成
    public static final String SYS_FILE ="FILE";




    /**
     * 负荷排查智能体
     */
    // 工单填充审核智能体
    public static final String LOAD_CHECK_CLIENT_FILL_IN = "fillInReview";
    //总结结果意见智能体
    public static final String SUMMARY_RESULT = "summaryResult";
    // 语音信息提取
    public static final String FORM_INFO = "formInfo";
    // 表单铭牌识别
    public static final String FORM_RECOGNITION = "formRecognition";
           //识别模式-基础表单信息
           public static final String BASE_INFO = "baseFormInfo";
           //识别模式-用户回路及可调节负荷资源信息
           public static final String USER_LOOP_INFO = "userLoopInfo";
           //识别模式-电采暖负荷普查专项调研
           public static final String HEATING_LOAD_INFO = "heatingLoadInfo";
    // 用户推荐
    public static final String USER_RECOMMEND = "userRecommend";




    /**
     * word模板填充
     */
    public static final String WORD_DOCUMENT = "wordDocument";
    //图表生成
    public static final String CHART_GENERATION = "chartGeneration";
}
