package com.hankcs.xyy.train.cache;

import com.hankcs.xyy.train.enums.EcNlpNatureEnum;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"word"})
public class EcNlpCustomDictionaryCachePO implements Serializable {

    /**
     * 词性名称，brand：品牌，core：核心词，...，详见 EcNlpNatureEnum 。
     *
     * @see EcNlpNatureEnum
     */
    private String natureName;

    /**
     * 标准词
     */
    private String word;

    /**
     * 频次
     */
    private Long frequency;

}