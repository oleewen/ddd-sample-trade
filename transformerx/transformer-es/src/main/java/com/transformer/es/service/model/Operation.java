package com.transformer.es.service.model;

/**
 * 描述：多字段连接
 * 作者：董兵
 * 时间：2021/8/20 16:05
 */
public enum Operation {
    NOT,
    OR,
    AND,
    EXIST,// 不为空
    NOT_EXIST, //空数据
}
