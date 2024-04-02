package com.hankcs.xyy.train.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class XyyDrugCorpusThirdAnnotationRowDTO implements Serializable {
    /**
     * 展示名称
     */
    private String showName;

    /**
     * 第三方标记结果
     */
    private String thirdAnnotationResult;

}
