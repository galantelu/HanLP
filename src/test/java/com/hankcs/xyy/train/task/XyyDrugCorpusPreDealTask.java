package com.hankcs.xyy.train.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.DynamicCustomDictionary;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.xyy.constants.XyyConstants;
import com.hankcs.xyy.train.cache.XyyDrugCorpusTrainPreAnnotationReferenceCache;
import com.hankcs.xyy.train.dto.XyyDrugCorpusRowDTO;
import com.hankcs.xyy.train.easyexcel.rows.XyyDrugCorpusExcelRow;
import com.hankcs.xyy.train.operators.XyyDrugCorpusExcelOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author luyong
 */
@Slf4j
public class XyyDrugCorpusPreDealTask {

    /**
     * PS：运行之前请对可变参数中涉及的文件进行备份，以免发生文件数据丢失。
     *
     * @throws IOException
     */
    @Test
    public void doPreDeal() {
        /* 可变参数 */
        String corpusExcelPath = XyyConstants.corpusExcelPath;

        /* 备份 */
        XyyDrugCorpusExcelOperator.backup(corpusExcelPath);

        // 加载Excel
        List<XyyDrugCorpusRowDTO> xyyDrugCorpusRowDTOS = XyyDrugCorpusExcelOperator.readAllRows(corpusExcelPath);
        if (CollectionUtils.isEmpty(xyyDrugCorpusRowDTOS)) {
            log.info("物料Excel没有数据，终止");
            return;
        }
        /* 特殊字符处理 */
        xyyDrugCorpusRowDTOS.stream().forEach(item -> {
            /*
             * 去掉特殊字符：
             * /
             * 空格
             * [
             * ]
             * ,
             * #
             * @
             */
            if (StringUtils.isNotEmpty(item.getBrand())) {
                item.setBrand(item.getBrand().replaceAll("/|\\s|\\[|\\]|,|#|@", ""));
            }
            if (StringUtils.isNotEmpty(item.getCommonName())) {
                item.setCommonName(item.getCommonName().replaceAll("/|\\s|\\[|\\]|,|#|@", ""));
            }
            /* 字符正规化。*/
            if (StringUtils.isNotEmpty(item.getBrand())) {
                CharTable.normalization(item.getBrand().toCharArray());
            }
            if (StringUtils.isNotEmpty(item.getCommonName())) {
                CharTable.normalization(item.getCommonName().toCharArray());
            }

        });
        // 写Excel
        List<XyyDrugCorpusExcelRow> excelRows = XyyDrugCorpusExcelOperator.createExcelRows(xyyDrugCorpusRowDTOS);
        XyyDrugCorpusExcelOperator.coverWrite(corpusExcelPath, excelRows);
        log.debug("预标记处理。成功。");
    }

    /**
     * PS：运行之前请对可变参数中涉及的文件进行备份，以免发生文件数据丢失。
     */
    @Test
    public void doPreAnnotation() {
        /* 可变参数 */
        String corpusExcelPath = XyyConstants.corpusExcelPath;

        /* 预标记：是根据预标记参考进行标记，请提前根据现有数据，维护好参考数据。参考数据见：data/xyy/train/xyy_drug_corpus_pre_annotation_reference.xlsx */
        // top 1 核心词
        List<String> coreTerms = Lists.newArrayList();
//        coreTerms.add("枇杷");
//        coreTerms.add("感冒灵");
        if (CollectionUtils.isEmpty(coreTerms)) {
            log.info("没有需要预处理的关键字，终止");
            return;
        }

        /* 备份 */
        XyyDrugCorpusExcelOperator.backup(corpusExcelPath);

        // 加载Excel
        List<XyyDrugCorpusRowDTO> xyyDrugCorpusRowDTOS = XyyDrugCorpusExcelOperator.readAllRows(corpusExcelPath);
        if (CollectionUtils.isEmpty(xyyDrugCorpusRowDTOS)) {
            log.info("物料Excel没有数据，终止");
            return;
        }
        coreTerms = coreTerms.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(coreTerms)) {
            log.info("没有需要预处理的关键字，终止");
            return;
        }

        // 预标记
        for (String coreTerm : coreTerms) {
            List<XyyDrugCorpusRowDTO> matchCoreTermCorpusRowDTOS = xyyDrugCorpusRowDTOS.stream().filter(item -> Objects.nonNull(item) && StringUtils.isNotEmpty(item.getCommonName())
                && item.getCommonName().contains(coreTerm)).collect(Collectors.toList());
            this.doPreAnnotation(coreTerm, matchCoreTermCorpusRowDTOS);
        }
        // 写Excel
        List<XyyDrugCorpusExcelRow> excelRows = XyyDrugCorpusExcelOperator.createExcelRows(xyyDrugCorpusRowDTOS);
        XyyDrugCorpusExcelOperator.coverWrite(corpusExcelPath, excelRows);
        log.debug("预标记处理。成功。");
    }

    private Segment coverLoadCustomDictionary(String coreTerm) {
        // 强制高优使用自己的词典
        Segment newSegment = HanLP.newSegment("viterbi");
        DynamicCustomDictionary myDictionary = new DynamicCustomDictionary("data/xyy/train/XyyBlackCustomDictionary.txt");
        myDictionary.reload();
        newSegment.enableCustomDictionary(myDictionary);
        newSegment.enablePartOfSpeechTagging(false);
        newSegment.enableCustomDictionary(true);
        newSegment.enableCustomDictionaryForcing(true);
        List<String> segments = XyyDrugCorpusTrainPreAnnotationReferenceCache.listSegments(coreTerm);
        if (log.isDebugEnabled()) {
            log.debug("预标记处理，coreTerm：{}, 预处理的参考分词列表：{}", coreTerm, JSONArray.toJSON(segments));
        }
        if (CollectionUtils.isEmpty(segments)) {
            log.debug("预标记处理，coreTerm：{}, 没有预处理的参考分词列表。", coreTerm);
            return newSegment;
        }
        for (String segment : segments) {
            newSegment.customDictionary.insert(segment);
        }
        return newSegment;
    }

    private void doPreAnnotation(String coreTerm, List<XyyDrugCorpusRowDTO> matchCoreTermCorpusRowDTOS) {
        if (StringUtils.isEmpty(coreTerm) || CollectionUtils.isEmpty(matchCoreTermCorpusRowDTOS)) {
            return;
        }
        Map<String, String> segmentToAnnotationMap = XyyDrugCorpusTrainPreAnnotationReferenceCache.getSegmentAnnotationMap(coreTerm);
        if (log.isDebugEnabled()) {
            log.debug("预标记处理，coreTerm：{}，预处理的参考分词与标注的映射关系：{}", coreTerm, JSONObject.toJSON(segmentToAnnotationMap));
        }
        if (MapUtils.isEmpty(segmentToAnnotationMap)) {
            log.info("预标记处理，coreTerm：{}，预处理的参考分词与标注的映射关系，跳过当前关键词。", coreTerm);
            return;
        }
        Segment segment = this.coverLoadCustomDictionary(coreTerm);
        for (XyyDrugCorpusRowDTO xyyDrugCorpusRowDTO : matchCoreTermCorpusRowDTOS) {
            if (!xyyDrugCorpusRowDTO.getCommonName().contains(coreTerm)) {
                continue;
            }
            // 分词
            List<Term> terms = segment.seg(xyyDrugCorpusRowDTO.getCommonName());
            if (log.isDebugEnabled()) {
                log.debug("预标记处理，coreTerm：{}, commonName：{}，分词：{}", coreTerm, xyyDrugCorpusRowDTO.getCommonName(), terms);
            }
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
        // 还原
        new DynamicCustomDictionary().reload();
    }

}
