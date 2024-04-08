package com.hankcs.xyy.train.operators;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hankcs.xyy.constants.XyyConstants;
import com.hankcs.xyy.train.dto.XyyDrugCorpusDictionaryRowDTO;
import com.hankcs.xyy.train.easyexcel.EasyExcelDataAnalysisEventListener;
import com.hankcs.xyy.train.easyexcel.EasyExcelDataProcessor;
import com.hankcs.xyy.train.easyexcel.EasyExcelRowDataWrapper;
import com.hankcs.xyy.train.easyexcel.rows.XyyDrugCorpusDictionaryExcelRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class XyyDrugCorpusDictionaryExcelOperator {

    public static void backup(String corpusExcelPath) {
        File file = new File(corpusExcelPath);
        String fileName = file.getName();
        String backupFilename = fileName + "." + DateFormatUtils.format(new Date(), "YYYYMMddHHmmssS");
        if (log.isDebugEnabled()) {
            log.debug("备份词典Excel，备份文件名：{}", backupFilename);
        }
        String backupFilePath = XyyConstants.backupDirPath + "/" + backupFilename;
        try {
            FileUtils.copyFile(file, new File(backupFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("备份词典Excel成功：{}", backupFilePath);
    }

    public static void coverWrite(String corpusExcelPath, List<XyyDrugCorpusDictionaryExcelRow> xyyDrugCorpusExcelRows) {
        EasyExcel.write(corpusExcelPath, XyyDrugCorpusDictionaryExcelRow.class).sheet().doWrite(xyyDrugCorpusExcelRows);
    }

    private static List<XyyDrugCorpusDictionaryExcelRow> readAllExcelRows(String corpusExcelPath) {
        /* 解析Excel */
        // 表头
        Map<Integer, String> headMaps = Maps.newHashMapWithExpectedSize(16);
        // 行数据
        List<EasyExcelRowDataWrapper<XyyDrugCorpusDictionaryExcelRow>> excelRows = Lists.newArrayList();
        // 读取Excel
        EasyExcel.read(corpusExcelPath, XyyDrugCorpusDictionaryExcelRow.class,
            new EasyExcelDataAnalysisEventListener<>(new EasyExcelDataProcessor<XyyDrugCorpusDictionaryExcelRow>() {
                @Override
                public void process(List<EasyExcelRowDataWrapper<XyyDrugCorpusDictionaryExcelRow>> rows, AnalysisContext context) {
                    if (CollectionUtils.isNotEmpty(rows)) {
                        excelRows.addAll(rows);
                    }
                }

                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    Integer rowIndex = context.readRowHolder().getRowIndex();
                    if (Objects.equals(rowIndex, 0) && MapUtils.isNotEmpty(headMap)) {
                        headMaps.putAll(headMap);
                    }
                }
            })).sheet().headRowNumber(1).doRead();
        if (CollectionUtils.isEmpty(excelRows)) {
            return Lists.newArrayList();
        }
        return excelRows.stream().map(EasyExcelRowDataWrapper::getRowData).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<XyyDrugCorpusDictionaryRowDTO> readAllRows(String corpusExcelPath) {
        return createRowDTOS(readAllExcelRows(corpusExcelPath));
    }

    private static List<XyyDrugCorpusDictionaryRowDTO> createRowDTOS(List<XyyDrugCorpusDictionaryExcelRow> excelRows) {
        if (CollectionUtils.isEmpty(excelRows)) {
            return Lists.newArrayList();
        }
        return excelRows.stream().map(item -> createRowDTO(item)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static XyyDrugCorpusDictionaryRowDTO createRowDTO(XyyDrugCorpusDictionaryExcelRow rowData) {
        if (Objects.isNull(rowData)) {
            return null;
        }
        return XyyDrugCorpusDictionaryRowDTO.builder().dictionary(rowData.getDictionary())
            .realDictionary(rowData.getRealDictionary())
            .build();
    }

    public static List<XyyDrugCorpusDictionaryExcelRow> createExcelRows(List<XyyDrugCorpusDictionaryRowDTO> xyyDrugCorpusRowDTOS) {
        if (CollectionUtils.isEmpty(xyyDrugCorpusRowDTOS)) {
            return Lists.newArrayList();
        }
        return xyyDrugCorpusRowDTOS.stream().map(item -> createExcelRow(item)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static XyyDrugCorpusDictionaryExcelRow createExcelRow(XyyDrugCorpusDictionaryRowDTO xyyDrugCorpusRowDTO) {
        if (Objects.isNull(xyyDrugCorpusRowDTO)) {
            return null;
        }
        return XyyDrugCorpusDictionaryExcelRow.builder().dictionary(xyyDrugCorpusRowDTO.getDictionary())
                .realDictionary(xyyDrugCorpusRowDTO.getRealDictionary())
            .build();
    }
}
