package com.ai.server.agent.ai.interceptor;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class UserContextHolder {
    private ThreadLocal<Map<String, String>> threadLocal;
    private ThreadLocal<String> userContextStrTreadLocal;

    private UserContextHolder() {
        this.threadLocal = new ThreadLocal();
        this.userContextStrTreadLocal = new ThreadLocal<>();
    }

    public static UserContextHolder getInstance() {
        return SingletonHolder.sInstance;
    }

    public void setContext(Map<String, String> map) {
        this.threadLocal.set(map);
    }

    public void setContextStr(String contextStr) {
        this.userContextStrTreadLocal.set(contextStr);
    }

    public String getContextStr() {
        return Optional.ofNullable(userContextStrTreadLocal.get()).orElse("");
    }

    public Map<String, String> getContext() {
        return (Map) this.threadLocal.get();
    }

    public String getRoleId() {
        return (String) ((Map) Optional.ofNullable(this.threadLocal.get()).orElse(Maps.newHashMap())).get("roleId");
    }

    public String getName() {
        return (String) ((Map) Optional.ofNullable(this.threadLocal.get()).orElse(Maps.newHashMap())).get("name");
    }

    public String getUsername() {
        return (String) ((Map) Optional.ofNullable(this.threadLocal.get()).orElse(Maps.newHashMap())).get("username");
    }

    public String getUserId() {
        Map<String, String> context = Optional.ofNullable(this.threadLocal.get()).orElse(Maps.newHashMap());
        return context.get("id");
    }

    public String getOrgId() {
        return (String) ((Map) Optional.ofNullable(this.threadLocal.get()).orElse(Maps.newHashMap())).get("orgId");
    }

    public String getOrgTitle() {
        return (String) ((Map) Optional.ofNullable(this.threadLocal.get()).orElse(Maps.newHashMap())).get("orgTitle");
    }

    public void clear() {
        this.threadLocal.remove();
        this.userContextStrTreadLocal.remove();
    }

    private static class SingletonHolder {
        private static final UserContextHolder sInstance = new UserContextHolder();
    }
}