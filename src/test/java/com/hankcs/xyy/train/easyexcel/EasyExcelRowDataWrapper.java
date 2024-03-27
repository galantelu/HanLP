package com.hankcs.xyy.train.easyexcel;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class EasyExcelRowDataWrapper<T> implements Serializable {

    /**
     * sheet 索引
     */
    private Integer sheetNo;

    /**
     * sheet 名称
     */
    private String sheetName;

    /**
     * 行索引
     */
    private Integer rowIndex;

    /**
     * 行数据
     */
    private T rowData;

}
