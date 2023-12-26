package com.company.businessdomain.common.domain;

/**
 * id
 *
 * @author only
 * @since 2020-05-22
 */
public abstract class Id implements ValueObject<Long> {
    /** id */
    private Long id;

    public Id(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("id must gt 0");
        }
        this.id = id;
    }

    public Long value() {
        return id;
    }
}
