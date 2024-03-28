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
public class XyyDrugCorpusTrainRowDTO implements Serializable {

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
}
