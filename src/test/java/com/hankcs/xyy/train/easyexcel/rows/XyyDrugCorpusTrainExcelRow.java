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
public class XyyDrugCorpusTrainExcelRow implements Serializable {
    /**
     * 品牌
     */
    @ExcelProperty(index = 0)
    private String brand;

    /**
     * 通用名
     */
    @ExcelProperty(index = 1)
    private String commonName;

    /**
     * 标记的通用名
     */
    @ExcelProperty(index = 2)
    private String annotationCommonName;
}
