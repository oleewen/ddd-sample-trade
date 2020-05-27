package com.company.department.business.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额
 * <p>
 * 1. 提供单位元、分的构造
 * 2. 提供折扣、增加、减免操作
 * </p>
 * <p>
 * 此对象只读、线程安全
 *
 * @author only
 * @date 2018-01-31
 */
public final class MonetaryAmount {
    /** 1元=100分 */
    private static final BigDecimal MULTIPLE = BigDecimal.valueOf(100);
    /** 金额小数点精度 */
    private static final int SCALE = 2;
    /** 金额 */
    private BigDecimal monetaryAmount;
    /** 小数点后精度 */
    private int scale;

    /**
     * 按金额和小数点后精度构造金额
     *
     * @param monetaryAmount 金额
     * @param scale          小数点后精度
     */
    private MonetaryAmount(BigDecimal monetaryAmount, int scale) {
        this.monetaryAmount = monetaryAmount;
        this.scale = scale;
    }

    /**
     * 用金额数值（单位：元）初始化金额实例，默认精确到小数点后两位
     *
     * @param amount 金额，单位：元
     * @return 金额实例
     */
    public static MonetaryAmount create(double amount) {
        return create(amount, SCALE);
    }

    /**
     * 用金额数值（单位：分）初始化金额实例，默认精确到小数点后两位
     *
     * @param cent 金额，单位：分
     * @return 金额实例
     */
    public static MonetaryAmount create(long cent) {
        return create(cent, SCALE);
    }

    /**
     * 用金额数值（单位：元）、小数点后精度初始化金额实例
     *
     * @param amount 金额，单位：元
     * @param scale  小数点后精度
     * @return 金额实例
     */
    public static MonetaryAmount create(double amount, int scale) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount is illegal");
        }
        if (scale < 0) {
            throw new IllegalArgumentException("scale is illegal");
        }

        BigDecimal monetaryAmount = BigDecimal.valueOf(amount);

        return new MonetaryAmount(monetaryAmount, scale);
    }

    /**
     * 用金额数值（单位：分）、小数点后精度初始化金额实例
     *
     * @param cent  金额，单位：分
     * @param scale 小数点后精度
     * @return 金额实例
     */
    public static MonetaryAmount create(long cent, int scale) {
        if (cent < 0) {
            throw new IllegalArgumentException("cent is illegal");
        }
        if (scale < 0) {
            throw new IllegalArgumentException("scale is illegal");
        }

        BigDecimal decimal = BigDecimal.valueOf(cent);
        /** 分转元 */
        BigDecimal monetaryAmount = decimal.divide(MULTIPLE);

        return new MonetaryAmount(monetaryAmount, scale);
    }

    /**
     * 取金额数值（默认四舍五入）
     *
     * @return 金额，小数点后2位
     */
    public double getAmount() {
        return getAmount(RoundingMode.HALF_UP);
    }

    /**
     * 取金额数值，指定小数点精度后数值的舍入规则
     *
     * @param roundingMode 小数点精度后数值的舍入规则
     * @return 金额数值，单位：元
     */
    public double getAmount(RoundingMode roundingMode) {
        return monetaryAmount.setScale(getScale(), roundingMode).doubleValue();
    }

    /**
     * 取金额数值，单位：分
     *
     * @return 金额数值，单位：分
     */
    public long getCent() {
        BigDecimal cent = monetaryAmount.multiply(MULTIPLE);

        return cent.longValue();
    }

    /**
     * 金额打折
     *
     * @param discount 折扣，e.g. 八八折 0.88
     * @return 折后金额实例
     */
    public MonetaryAmount discount(double discount) {
        if (discount < 0) {
            throw new IllegalArgumentException("discount is illegal");
        }

        BigDecimal monetaryAmount = this.monetaryAmount.multiply(BigDecimal.valueOf(discount));

        return create(monetaryAmount.doubleValue(), getScale());
    }

    /**
     * 金额打折
     * <p>
     * e.g.
     * 88折 discount(88, 100), discount(8800, 10000)
     * 88.88折 discount(8888, 10000)
     * </p>
     *
     * @param discount  折扣数值
     * @param precision 折扣精度
     * @return 折后金额实例
     */
    public MonetaryAmount discount(int discount, int precision) {
        if (discount < 0) {
            throw new IllegalArgumentException("discount is illegal");
        }
        if (precision <= 0) {
            throw new IllegalArgumentException("precision is illegal");
        }

        BigDecimal monetaryAmount = this.monetaryAmount.multiply(BigDecimal.valueOf(discount)).divide(BigDecimal.valueOf(precision));

        return create(monetaryAmount.doubleValue(), getScale());
    }

    /**
     * 金额增加amount（单位：元）数值
     *
     * @param amount 金额减数，单位：元
     * @return 减去amount后的金额实例
     */
    public MonetaryAmount add(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount is illegal");
        }

        BigDecimal result = monetaryAmount.add(BigDecimal.valueOf(amount));

        return create(result.doubleValue(), getScale());
    }

    /**
     * 金额增加cent（单位：分）数值
     *
     * @param cent 金额减数，单位：分
     * @return 减去amount后的金额实例
     */
    public MonetaryAmount add(long cent) {
        if (cent < 0) {
            throw new IllegalArgumentException("cent is illegal");
        }

        long result = getCent() + cent;

        return create(result, getScale());
    }

    /**
     * 金额减去amount（单位：元）数值
     *
     * @param amount 金额减数，单位：元
     * @return 减去amount后的金额实例
     */
    public MonetaryAmount subtract(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount is illegal");
        }

        BigDecimal result = monetaryAmount.subtract(BigDecimal.valueOf(amount));

        double value = result.doubleValue();
        if (value < 0) {
            throw new IllegalArgumentException("balance is not enough");
        }

        return create(value, getScale());
    }

    /**
     * 金额减去cent（单位：分）数值
     *
     * @param cent 金额减数，单位：分
     * @return 减去amount后的金额实例
     */
    public MonetaryAmount subtract(long cent) {
        if (cent < 0) {
            throw new IllegalArgumentException("cent is illegal");
        }

        long result = getCent() - cent;
        if (result < 0) {
            throw new IllegalArgumentException("balance is not enough");
        }

        return create(result, getScale());
    }

    public int getScale() {
        return scale;
    }

    /**
     * 格式化金额
     *
     * @return 金额格式，e.g. ￥88.88
     */
    public String format() {
        return "￥" + getAmount();
    }

    /**
     * 字符串格式化
     *
     * @return 金额格式，e.g. 88.88元
     */
    @Override
    public String toString() {
        return getAmount() + "元";
    }
}
