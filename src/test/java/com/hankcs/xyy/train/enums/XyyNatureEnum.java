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
     * 核心词
     */
    core(Nature.create("core")),
    /**
     * 品牌
     */
    brand(Nature.create("brand")),

    /**
     * 子品牌
     */
    subBrand(Nature.create("subBrand")),
    /**
     * 成分
     */
    ingredient(Nature.create("ingredient")),
    /**
     * 适应症
     */
    disease(Nature.create("disease")),
    /**
     * 修饰词
     */
    qualifier(Nature.create("qualifier")),
    /**
     * 剂型
     */
    dosage(Nature.create("dosage")),
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
     * 包装类型
     */
    packaging(Nature.create("packaging")),
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
