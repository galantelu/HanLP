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
     * 药名
     */
    drug(Nature.create("drug")),
    /**
     * 核心药名
     */
    coreDrug(Nature.create("coreDrug")),
    /**
     * 品牌
     */
    brand(Nature.create("brand")),
    /**
     * 子品牌
     */
    subBrand(Nature.create("subBrand")),
    /**
     * 药品成分
     */
    drugIngredient(Nature.create("drugIngredient")),
    /**
     * 药品病症
     */
    drugDisease(Nature.create("drugDisease")),
    /**
     * 修饰词
     */
    qualifier(Nature.create("qualifier")),
    /**
     * 适应人群
     */
    customer(Nature.create("customer")),
    /**
     * 规格
     */
    spec(Nature.create("spec")),
    /**
     * 规格：单位
     */
    specUint(Nature.create("specUint")),
    /**
     * 规格：总量
     */
    specTotal(Nature.create("specTotal")),
    /**
     * 剂型
     */
    dosageUnit(Nature.create("dosageUnit")),
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
