package com.hankcs.xyy.train.easyexcel;

import com.alibaba.excel.context.AnalysisContext;

import java.util.List;
import java.util.Map;

public interface EasyExcelDataProcessor<T> {

    void process(List<EasyExcelRowDataWrapper<T>> rows, AnalysisContext context);

    void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context);

}
