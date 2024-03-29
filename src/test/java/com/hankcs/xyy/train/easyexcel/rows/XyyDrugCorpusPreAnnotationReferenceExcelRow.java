package com.hankcs.xyy.train.easyexcel.rows;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class XyyDrugCorpusPreAnnotationReferenceExcelRow implements Serializable {
    /**
     * 关键词
     */
    @ExcelProperty(index = 0)
    private String keyword;

    /**
     * 原词
     */
    @ExcelProperty(index = 1)
    private String segment;

    /**
     * 标记词
     */
    @ExcelProperty(index = 2)
    private String annotationSegment;
}
