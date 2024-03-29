package com.hankcs.xyy.train.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class XyyDrugCorpusRowDTO implements Serializable {

    /**
     * 品牌
     */
    private String brand;

    /**
     * 通用名
     */
    private String commonName;

    /**
     * 标记的通用名
     */
    private String annotationCommonName;

    /**
     * 是否已预标记
     * 1：是
     */
    private String isPreAnnotation;

    /**
     * 预标记的通用名
     */
    private String preAnnotationCommonName;
}
