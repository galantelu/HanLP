package com.hankcs.xyy.train.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 枚举
 *
 * @author luyong
 */
@Getter
public enum XyyDictWordTypeEnum {

    /**
     * 品牌
     */
    BRAND(1, "品牌"),

    /**
     * 剂型
     */
    DOSAGE_UNIT(2, "剂型"),

    ;

    private Integer type;
    private String name;

    XyyDictWordTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 自定义 valueOf()方法
     *
     * @param type
     * @return
     */
    public static XyyDictWordTypeEnum valueOfCustom(Integer type) {
        if (Objects.isNull(type)) {
            return null;
        }
        for (XyyDictWordTypeEnum anEnum : values()) {
            if (Objects.equals(anEnum.getType(), type)) {
                return anEnum;
            }
        }
        return null;
    }

}
