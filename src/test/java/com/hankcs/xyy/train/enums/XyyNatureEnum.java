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
    brand(Nature.create("brand"), "品牌词"),
    /**
     * 核心词
     */
    core(Nature.create("core"), "核心词"),
    /**
     * 剂型
     */
    dosage(Nature.create("dosage"), "剂型词"),
    /**
     * 企业名称
     */
    corp(Nature.create("corp"), "企业词"),
    /**
     * 规格
     */
    spec(Nature.create("spec"), "规格词"),
    /**
     * other
     */
    other(Nature.create("other"), "其他词"),

    /* ==========以下为废弃=========== */
    /**
     * 子品牌
     */
    subBrand(Nature.create("subBrand"), "子品牌"),
    /**
     * 成分
     */
    ingredient(Nature.create("ingredient"), "成分"),
    /**
     * 适应症
     */
    disease(Nature.create("disease"), "适应症"),
    /**
     * 修饰词
     */
    qualifier(Nature.create("qualifier"), "修饰词"),
    /**
     * 规格：单位
     */
    specUint(Nature.create("specUint"), "规格：单位"),
    /**
     * 规格：总量
     */
    specTotal(Nature.create("specTotal"), "规格：总量"),
    /**
     * 包装类型
     */
    packaging(Nature.create("packaging"), "包装类型"),

    ;
    private Nature nature;

    private String desc;

    XyyNatureEnum(Nature nature, String desc) {
        this.nature = nature;
        this.desc = desc;
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
