package com.ai.server.agent.ai.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SecureUtil {

    /**
     * 根据请求头解析用户
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    public static AmiUser getUser(HttpServletRequest request) throws UnsupportedEncodingException {
        String userStr = request.getHeader("AuthorizationUser");
        if (StrUtil.isBlank(userStr)) {
            return null;
        }
        userStr = URLDecoder.decode(userStr, StandardCharsets.UTF_8.name());
        return JSONUtil.toBean(userStr, AmiUser.class);
    }

    @Data
    public static class AmiUser {
        private String operatorId;
        private String name;
        private String mgtOrgCode;
        private String mgtOrgName;
        private String distLv;
        private String userPhotoId;
        private Integer userStatus;
        private String operatorType;
        private String email;
        private String extInformation;
    }
}
