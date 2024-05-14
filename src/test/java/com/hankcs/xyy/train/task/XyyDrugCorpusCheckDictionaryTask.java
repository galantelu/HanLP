package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.DynamicCustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.xyy.train.dto.XyyDrugCorpusDictionaryRowDTO;
import com.hankcs.xyy.train.easyexcel.rows.XyyDrugCorpusDictionaryExcelRow;
import com.hankcs.xyy.train.enums.XyyNatureEnum;
import com.hankcs.xyy.train.operators.XyyDrugCorpusDictionaryExcelOperator;
import com.hankcs.xyy.utils.XyyDrugCorpusUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class XyyDrugCorpusCheckDictionaryTask {

    private static Segment newSegment = HanLP.newSegment("viterbi");

    private static Segment newIndexSegment = HanLP.newSegment("viterbi");

    static {
        newSegment.enablePartOfSpeechTagging(true);
        newSegment.enableCustomDictionaryForcing(true);

        newIndexSegment.enablePartOfSpeechTagging(true);
        newIndexSegment.enableCustomDictionaryForcing(true);
        newIndexSegment.enableIndexMode(1);

    }

    @Test
    public void nerCorp() throws IOException {
        new DynamicCustomDictionary().reload();

        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的店铺展示名称&厂商_2024_04_02.xlsx";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("Excel没有数据，终止");
            return;
        }
//        Set<String> topQueryKeywords = Sets.newHashSet("");
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("没有数据，终止");
            return;
        }
        Set<String> natureSet = Arrays.stream(XyyNatureEnum.values()).map(XyyNatureEnum::toString).collect(Collectors.toSet());

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
//            for (String topQueryKeyword : topQueryKeywords) {
            rowDTO.setDictionary(XyyDrugCorpusUtils.replaceSpecialChar(rowDTO.getDictionary()));
//                if (rowDTO.getDictionary().contains(topQueryKeyword)) {
            List<Term> terms = newSegment.seg(rowDTO.getDictionary());
            List<String> termStrList = terms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary(String.join(" ", termStrList));

            List<Term> indexTerms = newIndexSegment.seg(rowDTO.getDictionary());
            List<String> indexTermStrList = indexTerms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
            resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
//                }
//            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的店铺展示名称&厂商_2024_04_02_result.xlsx";
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(resultRowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(resultDictionaryExcelPath, excelRows);
        log.debug("对企业名称进行NER成功。");
    }

    @Test
    public void nerSpec() throws IOException {
        new DynamicCustomDictionary().reload();

        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的规格_2024_04_02.xlsx";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("Excel没有数据，终止");
            return;
        }
//        Set<String> topQueryKeywords = Sets.newHashSet("");
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("没有数据，终止");
            return;
        }
        Set<String> natureSet = Arrays.stream(XyyNatureEnum.values()).map(XyyNatureEnum::toString).collect(Collectors.toSet());

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
//            for (String topQueryKeyword : topQueryKeywords) {
            rowDTO.setDictionary(XyyDrugCorpusUtils.replaceSpecialChar(rowDTO.getDictionary()));
//                if (rowDTO.getDictionary().contains(topQueryKeyword)) {
            List<Term> terms = newSegment.seg(rowDTO.getDictionary());
            List<String> termStrList = terms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary(String.join(" ", termStrList));

            List<Term> indexTerms = newIndexSegment.seg(rowDTO.getDictionary());
            List<String> indexTermStrList = indexTerms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
            resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
//                }
//            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的规格_2024_04_02_result.xlsx";
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(resultRowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(resultDictionaryExcelPath, excelRows);
        log.debug("对规格进行NER成功。");
    }

    @Test
    public void nerShowName() throws IOException {
        new DynamicCustomDictionary().reload();

        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的展示名称和通用名称_2024_04_02.xlsx";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("Excel没有数据，终止");
            return;
        }
//        Set<String> topQueryKeywords = Sets.newHashSet("感冒灵");
        Set<String> topQueryKeywords = Sets.newHashSet("枇杷");
//        Set<String> topQueryKeywords = Sets.newHashSet("感冒灵", "枇杷", "阿莫西林");
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("没有数据，终止");
            return;
        }
        Set<String> natureSet = Arrays.stream(XyyNatureEnum.values()).map(XyyNatureEnum::toString).collect(Collectors.toSet());

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            for (String topQueryKeyword : topQueryKeywords) {
                rowDTO.setDictionary(XyyDrugCorpusUtils.replaceSpecialChar(rowDTO.getDictionary()));
                if (rowDTO.getDictionary().contains(topQueryKeyword)) {
                    List<Term> terms = newSegment.seg(rowDTO.getDictionary());
                    List<String> termStrList = terms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                        return term.word + "/" + term.nature.toString();
                    }).collect(Collectors.toList());
                    rowDTO.setRealDictionary(String.join(" ", termStrList));

                    List<Term> indexTerms = newIndexSegment.seg(rowDTO.getDictionary());
                    List<String> indexTermStrList = indexTerms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                        return term.word + "/" + term.nature.toString();
                    }).collect(Collectors.toList());
                    rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
                    resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
                }
            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的展示名称和通用名称_2024_04_02_result.xlsx";
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(resultRowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(resultDictionaryExcelPath, excelRows);
        log.debug("对商品名称进行NER成功。");
    }

}
