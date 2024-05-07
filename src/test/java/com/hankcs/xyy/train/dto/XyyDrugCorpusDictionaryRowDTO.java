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
public class XyyDrugCorpusDictionaryRowDTO implements Serializable {

    /**
     * 词典
     */
    private String dictionary;

    /**
     * 词典
     */
    private String realDictionary;

    /**
     * 词典2
     */
    private String realDictionary2;
}
