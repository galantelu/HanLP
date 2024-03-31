package com.hankcs.xyy.train.cache;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hankcs.xyy.train.dto.XyyDrugCorpusPreAnnotationReferenceDTO;
import com.hankcs.xyy.train.easyexcel.EasyExcelDataAnalysisEventListener;
import com.hankcs.xyy.train.easyexcel.EasyExcelDataProcessor;
import com.hankcs.xyy.train.easyexcel.EasyExcelRowDataWrapper;
import com.hankcs.xyy.train.easyexcel.rows.XyyDrugCorpusPreAnnotationReferenceExcelRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author luyong
 */
@Slf4j
public class XyyDrugCorpusTrainPreAnnotationReferenceCache {

    private static final String dataExcelPath = "data/xyy/train/xyy_drug_corpus_pre_annotation_reference.xlsx";

    private static final Map<String, List<String>> coreTermToSegmentsMap;

    private static final Map<String, Map<String, String>> coreTermToSegmentAnnotationMap;

    static {
        coreTermToSegmentsMap = Maps.newHashMapWithExpectedSize(16);
        coreTermToSegmentAnnotationMap = Maps.newHashMapWithExpectedSize(16);
        List<XyyDrugCorpusPreAnnotationReferenceDTO> referenceDTOS = loadData(dataExcelPath);
        for (XyyDrugCorpusPreAnnotationReferenceDTO referenceDTO : referenceDTOS) {
            String coreTerm = referenceDTO.getCoreTerm();
            String segment = referenceDTO.getSegment();
            String annotationSegment = referenceDTO.getAnnotationSegment();
            if (StringUtils.isEmpty(coreTerm) || StringUtils.isEmpty(segment) || StringUtils.isEmpty(annotationSegment)) {
                continue;
            }
            List<String> segments = coreTermToSegmentsMap.get(coreTerm);
            if (Objects.isNull(segments)) {
                segments = Lists.newArrayListWithExpectedSize(16);
                coreTermToSegmentsMap.put(coreTerm, segments);
            }
            segments.add(segment);
            Map<String, String> segmentAnnotationMap = coreTermToSegmentAnnotationMap.get(coreTerm);
            if (Objects.isNull(segmentAnnotationMap)) {
                segmentAnnotationMap = Maps.newHashMapWithExpectedSize(16);
                coreTermToSegmentAnnotationMap.put(coreTerm, segmentAnnotationMap);
            }
            segmentAnnotationMap.put(segment, annotationSegment);
        }
    }

    public static List<String> listSegments(String coreTerm) {
        return coreTermToSegmentsMap.get(coreTerm);
    }

    public static Map<String, String> getSegmentAnnotationMap(String coreTerm) {
        return coreTermToSegmentAnnotationMap.get(coreTerm);
    }

    private static List<XyyDrugCorpusPreAnnotationReferenceDTO> loadData(String excelPath) {
        /* 解析Excel */
        // 表头
        Map<Integer, String> headMaps = Maps.newHashMapWithExpectedSize(16);
        // 行数据
        List<EasyExcelRowDataWrapper<XyyDrugCorpusPreAnnotationReferenceExcelRow>> excelRows = Lists.newArrayList();
        // 读取Excel
        EasyExcel.read(excelPath, XyyDrugCorpusPreAnnotationReferenceExcelRow.class,
            new EasyExcelDataAnalysisEventListener<>(new EasyExcelDataProcessor<XyyDrugCorpusPreAnnotationReferenceExcelRow>() {
                @Override
                public void process(List<EasyExcelRowDataWrapper<XyyDrugCorpusPreAnnotationReferenceExcelRow>> rows, AnalysisContext context) {
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
        return excelRows.stream().map(item -> convert(item.getRowData())).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static XyyDrugCorpusPreAnnotationReferenceDTO convert(XyyDrugCorpusPreAnnotationReferenceExcelRow rowData) {
        if (Objects.isNull(rowData)) {
            return null;
        }
        return XyyDrugCorpusPreAnnotationReferenceDTO.builder().coreTerm(rowData.getCoreTerm())
            .segment(rowData.getSegment()).annotationSegment(rowData.getAnnotationSegment()).build();
    }

}
