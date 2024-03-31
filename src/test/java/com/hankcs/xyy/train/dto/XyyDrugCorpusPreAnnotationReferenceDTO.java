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
public class XyyDrugCorpusPreAnnotationReferenceDTO implements Serializable {
    /**
     * 核心词
     */
    private String coreTerm;

    /**
     * 原词
     */
    private String segment;

    /**
     * 标记词
     */
    private String annotationSegment;
}
