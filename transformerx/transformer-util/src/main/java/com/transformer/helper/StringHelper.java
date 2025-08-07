/**
 * taobao.com Inc. Copyright (c) 2009-2012 All Rights Reserved.
 */
package com.transformer.helper;

import com.google.common.collect.Maps;
import com.transformer.consts.StringConst;
import com.transformer.exception.helper.ExceptionHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * 基本的运算
 * <ul>
 * <li>字节数组转十六进制字符</li>
 * <li>十六进制字符转字节数组</li>
 * <li>字符串编码URLEncoder.encode封装</li>
 * <li>字符串解码URLDecoder.decode封装</li>
 * </ul>
 *
 * @author only
 */
public final class StringHelper {
    /** byte字节最大值 */
    private static final int BYTE_MAX = 0xFF;

    private StringHelper(){}

    /**
     * 字节数组转换成十六进制字符串
     *
     * @param b 字节数组
     * @return 十六进制字符串
     */
    public static String byte2hex(byte[] b) {
        StringBuilder builder = new StringBuilder();

        for (byte element : b) {
            String hex = Integer.toHexString(element & BYTE_MAX);

            if (hex.length() == 1) {
                builder.append("0");
            }
            builder.append(hex);
        }
        return builder.toString().toUpperCase();
    }

    /**
     * 十六进制字符串转换成字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hex2byte(String hex) {
        if ((hex.length() % 2) != 0) {
            return new byte[0];
        }

        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];

        int j = 0;
        int l = hex.length();
        for (int i = 0; i < l; i += 2) {
            String swap = StringConst.EMPTY + arr[i] + arr[i + 1];
            int byteInt = Integer.parseInt(swap, 16) & BYTE_MAX;

            b[j++] = (byte) byteInt;
        }

        return b;
    }

    /**
     * 以系统默认编码对参数进行解码
     *
     * @param s 需要解码的字符串
     * @return 解码后的字符串
     */
    public static String decode(String s) {
        return decode(s, "UTF-8");
    }

    /**
     * 以系统默认编码对参数进行编码
     *
     * @param s 需要编码的字符串
     * @return 编码后的字符串
     */
    public static String encode(String s) {
        return encode(s, "UTF-8");
    }

    /**
     * 以charset编码对参数str进行解码
     *
     * @param s       需要解码的字符串
     * @param charset 字符串编码
     * @return 解码后的字符串
     */
    public static String decode(String s, String charset) {
        try {
            return URLDecoder.decode(s, charset);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionHelper.createNestedException(e);
        }
    }

    /**
     * 以charset编码对参数str进行编码
     *
     * @param s       需要编码的字符串
     * @param charset 字符串编码
     * @return 编码后的字符串
     */
    public static String encode(String s, String charset) {
        try {
            return URLEncoder.encode(s, charset);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionHelper.createNestedException(e);
        }
    }

    /**
     * 将字符串str转换为charset编码格式的byte数组
     *
     * @param str     字符串
     * @param charset 编码格式
     * @return 字符数组
     */
    public static byte[] str2byte(String str, String charset) {
        byte[] bytes = null;
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            bytes = new byte[0];
        } else {
            // 取编码格式为charset的base64编码字符串
            if (org.apache.commons.lang3.StringUtils.isNotBlank(charset)) {
                try {
                    bytes = str.getBytes(charset);
                } catch (UnsupportedEncodingException e) {
                    throw ExceptionHelper.createNestedException(e);
                }
            }
            // 如果bytes为空，初始化
            if (bytes == null) {
                bytes = str.getBytes();
            }
        }
        return bytes;
    }

    /**
     * 将字符串str转换为charset编码格式的byte数组
     *
     * @param bytes   字符数组
     * @param charset 编码格式
     * @return 字符串
     */
    public static String byte2str(byte[] bytes, String charset) {
        String str = StringConst.EMPTY;
        if (bytes != null) {
            // 取编码格式为charset的base64编码字符串
            if (org.apache.commons.lang3.StringUtils.isNotBlank(charset)) {
                try {
                    str = new String(bytes, charset);
                } catch (UnsupportedEncodingException e) {
                    throw ExceptionHelper.createNestedException(e);
                }
            }
            // 如果str为空，初始化
            if (str.equals(StringConst.EMPTY)) {
                str = new String(bytes);
            }
        }
        return str;
    }

    /**
     * 将字符串按分隔符和连接符转换为数据映射Map
     *
     * @param str       字符串
     * @param separate  分隔符
     * @param connector 连接符
     * @return 映射Map
     */
    public static Map<String, String> str2map(String str, String separate, String connector) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(str)) {
            String[] kvArray = org.apache.commons.lang3.StringUtils.split(str, separate);

            if (kvArray == null) {
                return Collections.emptyMap();
            }

            Map<String, String> map = Maps.newHashMap();
            for (String each : kvArray) {
                String[] kv = org.apache.commons.lang3.StringUtils.split(each, connector, 2);
                if (kv == null || kv.length < 2) {
                    continue;
                }
                map.put(kv[0], kv[1]);
            }
            return map;
        }
        return Collections.emptyMap();
    }

    /**
     * 将数据映射Map转换为字符串
     *
     * @param dataMap   数据映射
     * @param separate  分隔符
     * @param connector 连接符
     * @return 字符串
     */
    public static String map2Str(Map<String, Object> dataMap, String separate, String connector) {
        StringBuilder sb = new StringBuilder();
        if (dataMap != null) {
            Set<Entry<String, Object>> entrySet = dataMap.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null) {
                    sb.append(entry.getKey());
                    if (connector != null) {
                        sb.append(connector);
                    }
                    sb.append(entry.getValue());
                    if (separate != null) {
                        sb.append(separate);
                    }
                }
            }
            if (!dataMap.isEmpty() && separate != null) {
                sb.setLength(sb.length() - separate.length());
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串转换为数据映射Map
     *
     * @param dataMap   数据映射
     * @param separate  分隔符
     * @param connector 连接符
     * @return 字符串
     */
    public static String treeMap2Str(Map<String, String> dataMap, String separate, String connector) {
        if (dataMap == null || dataMap.isEmpty()) {
            return StringConst.EMPTY;
        }
        return map2Str(new TreeMap<>(dataMap), separate, connector);
    }

    /**
     * 将数据映射Map转换为字符串
     *
     * @param dataMap 数据映射Map
     * @return 字符串
     */
    public static String treeMap2Str(Map<String, String> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return StringConst.EMPTY;
        }
        return map2Str(new TreeMap<>(dataMap), StringConst.EMPTY, StringConst.EMPTY);
    }

    /**
     * 将数据映射Map转换为字符串
     *
     * @param dataMap 数据映射Map
     * @return 字符串
     */
    public static String map2Str(Map<String, String> dataMap) {
        return map2Str(new HashMap<>(dataMap), StringConst.EMPTY, StringConst.EMPTY);
    }
}
