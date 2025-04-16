package com.transformer.dao.enums;



import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 拦截处置类型
 */
public enum InterceptDispositionEnum {

    // 警告日志
    WARN_LOG("warn", "警告日志"),
    //报错日志
    ERROR_LOG("error", "错误日志"),
    //直接拦截
    INTERCEPT("intercept", "拦截"),
    INTERCEPT_SELECT("interceptSelect", "拦截查询"),
    INTERCEPT_UPDATE("interceptUpdate", "拦截修改"),
    INTERCEPT_DEL("interceptDel", "拦截删除"),
    ;

    /**
     * 编码
     */
    private final String code;
    /**
     *
     */
    private final String name;

    InterceptDispositionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
    public boolean match(String code) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        return this.name().equalsIgnoreCase(code);
    }

    public boolean contains(List<String> codes) {
        if (codes==null ||  codes.isEmpty()) {
            return false;
        }
        return this.name().equals(code);
    }


    public static InterceptDispositionEnum getByCode(String  code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (InterceptDispositionEnum enumItem : InterceptDispositionEnum.values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem ;
            }
        }
        return  null;
    }



}
