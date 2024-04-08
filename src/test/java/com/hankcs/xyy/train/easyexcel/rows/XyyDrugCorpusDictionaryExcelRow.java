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
public class XyyDrugCorpusDictionaryExcelRow implements Serializable {

    /**
     * 词典
     */
    @ColumnWidth(value = 100)
    @ExcelProperty(index = 0)
    private String dictionary;

    /**
     * 词典
     */
    @ColumnWidth(value = 100)
    @ExcelProperty(index = 1)
    private String realDictionary;
}
