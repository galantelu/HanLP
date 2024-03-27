package com.hankcs.xyy.train.easyexcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadSheetHolder;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class EasyExcelDataAnalysisEventListener<T> extends AnalysisEventListener<T> {

    private List<EasyExcelRowDataWrapper<T>> rowDataWrappers = Lists.newArrayListWithExpectedSize(16);

    private EasyExcelDataProcessor<T> easyExcelDataProcessor;

    public EasyExcelDataAnalysisEventListener(EasyExcelDataProcessor<T> easyExcelDataProcessor) {
        this.easyExcelDataProcessor = easyExcelDataProcessor;
    }

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        ReadSheetHolder readSheetHolder = context.readSheetHolder();
        Integer sheetNo = readSheetHolder.getSheetNo();
        String sheetName = readSheetHolder.getSheetName();
        Integer rowIndex = readSheetHolder.getRowIndex();
        EasyExcelRowDataWrapper<T> easyExcelRowDataWrapper = new EasyExcelRowDataWrapper();
        easyExcelRowDataWrapper.setRowIndex(rowIndex);
        easyExcelRowDataWrapper.setRowData(data);
        rowDataWrappers.add(easyExcelRowDataWrapper);
    }

    /**
     * 解析完成后调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            easyExcelDataProcessor.process(rowDataWrappers, context);
            log.info("【Excel数据解析监听器】数据解析完成。总共{}行", rowDataWrappers.size());
        } catch (Exception e) {
            log.error("【Excel数据解析监听器】数据解析异常", e);
        }
    }

    /**
     * 表头数据，这里会一行行的返回头。
     *
     * @param headMap
     * @param context
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        try {
            easyExcelDataProcessor.invokeHeadMap(headMap, context);
        } catch (Exception e) {
            log.error("【Excel数据解析监听器】数据解析表头异常", e);
        }
    }
}
