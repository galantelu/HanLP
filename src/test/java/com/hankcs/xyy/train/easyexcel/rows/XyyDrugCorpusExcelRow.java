package com.hankcs.xyy.train.easyexcel.rows;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.io.Serializable;

/**
 *
 */
@HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND)
// 头字体设置成20
@HeadFontStyle(fontHeightInPoints = 16)
// 内容字体设置成16
@ContentFontStyle(fontHeightInPoints = 16)
@HeadRowHeight(value = 30)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class XyyDrugCorpusExcelRow implements Serializable {
    /**
     * 品牌
     */
    @ColumnWidth(value = 30)
    @ExcelProperty(index = 0)
    private String brand;

    /**
     * 通用名
     */
    @ColumnWidth(value = 100)
    @ExcelProperty(index = 1)
    private String commonName;

    /**
     * 标记的通用名
     */
    @ColumnWidth(value = 300)
    @ExcelProperty(index = 2)
    private String annotationCommonName;

    /**
     * 是否已预标记
     * 1：是
     */
    @ColumnWidth(value = 20)
    @ExcelProperty(index = 3)
    private String isPreAnnotation;

    /**
     * 预标记的通用名
     */
    @ColumnWidth(value = 300)
    @ExcelProperty(index = 4)
    private String preAnnotationCommonName;
}
