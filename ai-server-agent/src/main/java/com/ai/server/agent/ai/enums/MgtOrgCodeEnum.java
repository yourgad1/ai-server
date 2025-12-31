package com.ai.server.agent.ai.enums;

import com.alibaba.nacos.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


public enum MgtOrgCodeEnum {
    BEI_JING("11102", "北京"),
    TIAN_JIN("12101", "天津"),
    HE_BEI("13102", "河北"),
    JI_BEI("13103", "冀北"),
    SHAN_XI("14101", "山西"),
    MENG_DONG("15101", "蒙东"),
    LIAO_NING("21102", "辽宁"),
    JI_LIN("22101", "吉林"),
    HEI_LONG_JIANG("23101", "黑龙江"),
    SHANG_HAI("31102", "上海"),
    JIANG_SU("32101", "江苏"),
    ZHE_JIANG("33101", "浙江"),
    AN_HUI("34101", "安徽"),
    FU_JIAN("35101", "福建"),
    JIANG_XI("36101", "江西"),
    SHAN_DONG("37101", "山东"),
    HE_NAN("41101", "河南"),
    HU_BEI("42102", "湖北"),
    HU_NAN("43101", "湖南"),
    CHONG_QING("50101", "重庆"),
    SI_CHUAN("51101", "四川"),
    XI_ZANG("54101", "西藏"),
    SHAN_XI2("61102", "陕西"),
    GAN_SU("62101", "甘肃"),
    QING_HAI("63101", "青海"),
    NING_XIA("64101", "宁夏"),
    XIN_JIANG("65101", "国网新疆电力有限公司"),
    XIN_JIANG_1("65401", "国网乌鲁木齐供电公司"),
    XIN_JIANG_2("65402", "国网昌吉供电公司"),
    XIN_JIANG_3("65403", "国网吐鲁番供电公司"),
    XIN_JIAN_4("65404", "国网奎屯供电公司"),
    XIN_JIANG_5("65405", "国网克州供电公司"),
    XIN_JIANG_6("65406", "国网博尔塔拉供电公司"),
    XIN_JIANG_7("65407", "国网哈密供电公司"),
    XIN_JIANG_8("65408", "国网塔城供电公司"),
    XIN_JIANG_9("65409", "国网阿勒泰供电公司"),
    XIN_JIANG_10("65410", "国网伊犁伊河供电有限责任公司"),
    XIN_JIANG_11("65411", "国网巴州供电公司"),
    XIN_JIANG_12("65412", "国网和田供电公司"),
    XIN_JIANG_13("65413", "国网阿克苏供电公司"),
    XIN_JIANG_14("65414", "国网喀什供电公司"),
    XIN_JIANG_15("65415", "国网克拉玛依供电有限公司");


    MgtOrgCodeEnum(String value, String desc) {
        setValue(value);
        setDesc(desc);
    }

    private String value;
    private String desc;

    public String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    protected void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getDesc(String key) {
        for (MgtOrgCodeEnum mgtOrgCodeEnum : MgtOrgCodeEnum.values()) {
            String value = mgtOrgCodeEnum.getValue();
            if (value.equals(key)) {
                return mgtOrgCodeEnum.getDesc();
            }
        }
        return null;
    }

    public static String fromDesc(String desc) {
        for (MgtOrgCodeEnum mgtOrgCodeEnum : values()) {
            if (mgtOrgCodeEnum.desc.equals(desc)) {
                return mgtOrgCodeEnum.getValue();
            }
        }
        return null;
    }

    public static String getMap() {
        List<String> maps = new ArrayList<>();
        for (MgtOrgCodeEnum mgtOrgCodeEnum : MgtOrgCodeEnum.values()) {
            maps.add(String.format("%s=%s", mgtOrgCodeEnum.getDesc(), mgtOrgCodeEnum.getValue()));
        }
        return StringUtils.join(maps, ",");
    }

}
