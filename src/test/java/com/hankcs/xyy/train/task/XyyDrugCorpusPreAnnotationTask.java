package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.DynamicCustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.xyy.constants.XyyConstants;
import com.hankcs.xyy.train.cache.XyyDrugCorpusTrainPreAnnotationReferenceCache;
import com.hankcs.xyy.train.dto.XyyDrugCorpusRowDTO;
import com.hankcs.xyy.train.easyexcel.rows.XyyDrugCorpusExcelRow;
import com.hankcs.xyy.train.operators.XyyDrugCorpusExcelOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class XyyDrugCorpusPreAnnotationTask {

    @Test
    public void doPreAnnotation() throws IOException {
        /* 当前任务的入参 */
        List<String> keywords = Lists.newArrayList("感冒灵");
        if (CollectionUtils.isEmpty(keywords)) {
            log.info("没有需要预处理的关键字，终止");
            return;
        }
        // 加载Excel
        List<XyyDrugCorpusRowDTO> xyyDrugCorpusRowDTOS = XyyDrugCorpusExcelOperator.readAllRows(XyyConstants.annotationDataExcelPath);
        if (CollectionUtils.isEmpty(xyyDrugCorpusRowDTOS)) {
            log.info("物料Excel没有数据，终止");
            return;
        }
        keywords = keywords.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(keywords)) {
            log.info("没有需要预处理的关键字，终止");
            return;
        }
        for (String keyword : keywords) {
            List<XyyDrugCorpusRowDTO> matchKeywordCorpusRowDTOS = xyyDrugCorpusRowDTOS.stream().filter(item -> Objects.nonNull(item) && StringUtils.isNotEmpty(item.getCommonName())
                && item.getCommonName().contains(keyword)).collect(Collectors.toList());
            this.doPreAnnotation(keyword, matchKeywordCorpusRowDTOS);
        }
        // 写Excel
        List<XyyDrugCorpusExcelRow> excelRows = XyyDrugCorpusExcelOperator.createExcelRows(xyyDrugCorpusRowDTOS);
        XyyDrugCorpusExcelOperator.coverWrite(XyyConstants.annotationDataExcelPath, excelRows);
        log.debug("预标记处理。成功。");
    }

    private static final Segment newSegment = HanLP.newSegment("viterbi");

    private void coverLoadCustomDictionary(String keyword) {
        // 强制高优使用自己的词典
        DynamicCustomDictionary myDictionary = new DynamicCustomDictionary();
        newSegment.enableCustomDictionary(myDictionary);
        newSegment.enablePartOfSpeechTagging(false);
        newSegment.enableCustomDictionary(true);
        newSegment.enableCustomDictionaryForcing(true);
        List<String> segments = XyyDrugCorpusTrainPreAnnotationReferenceCache.listSegments(keyword);
        if (CollectionUtils.isEmpty(segments)) {
            return;
        }
        for (String segment : segments) {
            CustomDictionary.insert(segment);
        }
    }

    private void doPreAnnotation(String keyword, List<XyyDrugCorpusRowDTO> matchKeywordCorpusRowDTOS) {
        if (StringUtils.isEmpty(keyword) || CollectionUtils.isEmpty(matchKeywordCorpusRowDTOS)) {
            return;
        }
        this.coverLoadCustomDictionary(keyword);
        for (XyyDrugCorpusRowDTO xyyDrugCorpusRowDTO : matchKeywordCorpusRowDTOS) {
            if (!xyyDrugCorpusRowDTO.getCommonName().contains(keyword)) {
                continue;
            }
            // 分词
            List<Term> terms = newSegment.seg(xyyDrugCorpusRowDTO.getCommonName().trim());
            if (log.isDebugEnabled()) {
                log.debug("预标记处理，keyword：{}, commonName：{}，分词：{}", keyword, xyyDrugCorpusRowDTO.getCommonName(), terms);
            }
            Map<String, String> segmentToAnnotationMap = XyyDrugCorpusTrainPreAnnotationReferenceCache.getSegmentAnnotationMap(keyword);
            StringBuilder stringBuilder = new StringBuilder();
            for (Term term : terms) {
                if (segmentToAnnotationMap.containsKey(term.word)) {
                    stringBuilder.append(segmentToAnnotationMap.get(term.word));
                } else {
                    stringBuilder.append(term.word);
                }
                stringBuilder.append(" ");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            xyyDrugCorpusRowDTO.setIsPreAnnotation("1");
            xyyDrugCorpusRowDTO.setPreAnnotationCommonName(stringBuilder.toString());
        }
    }

}
