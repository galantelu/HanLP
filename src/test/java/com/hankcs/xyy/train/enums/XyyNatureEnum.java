package com.hankcs.xyy.train.enums;

import com.hankcs.hanlp.corpus.tag.Nature;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 枚举
 *
 * @author luyong
 */
@Getter
public enum XyyNatureEnum {

    /**
     * 品牌
     */
    brand(Nature.create("brand")),
    /**
     * 剂型
     */
    dosageUnit(Nature.create("dosageUnit")),
    /**
     * 药名
     */
    drug(Nature.create("drug")),
    /**
     * 核心药名
     */
    coreDrug(Nature.create("coreDrug")),
    /**
     * 修饰词
     */
    qualifier(Nature.create("qualifier")),
    ;
    private Nature nature;

    XyyNatureEnum(Nature nature) {
        this.nature = nature;
    }

    /**
     * 自定义 valueOf()方法
     *
     * @param name
     * @return
     */
    public static XyyNatureEnum valueOfCustom(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (XyyNatureEnum anEnum : values()) {
            if (Objects.equals(anEnum.getNature().toString(), name)) {
                return anEnum;
            }
        }
        return null;
    }

}
