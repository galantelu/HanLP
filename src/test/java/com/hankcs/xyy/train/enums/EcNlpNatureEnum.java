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
public enum EcNlpNatureEnum {
    /**
     * 品牌
     */
    brand(Nature.create("brand")),
    /**
     * 核心词
     */
    core(Nature.create("core")),
    /**
     * 剂型
     */
    dosage(Nature.create("dosage")),
    /**
     * 企业名称
     */
    corp(Nature.create("corp")),
    /**
     * 规格
     */
    spec(Nature.create("spec")),
    /**
     * other
     */
    other(Nature.create("other")),

    ;
    private Nature nature;

    EcNlpNatureEnum(Nature nature) {
        this.nature = nature;
    }

    /**
     * 自定义 valueOf()方法
     *
     * @param name
     * @return
     */
    public static EcNlpNatureEnum valueOfCustom(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (EcNlpNatureEnum anEnum : values()) {
            if (Objects.equals(anEnum.getNature().toString(), name)) {
                return anEnum;
            }
        }
        return null;
    }

}
