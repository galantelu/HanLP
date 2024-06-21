package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
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
        Map<String, String> xyyNatureNameToDescMap = Arrays.stream(XyyNatureEnum.values()).collect(Collectors.toMap(item -> item.toString(), item -> item.getDesc(), (f, s) -> f));

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
//            for (String topQueryKeyword : topQueryKeywords) {
            rowDTO.setDictionary(XyyDrugCorpusUtils.replaceSpecialChar(rowDTO.getDictionary()));
//                if (rowDTO.getDictionary().contains(topQueryKeyword)) {
            List<Term> terms = newSegment.seg(rowDTO.getDictionary());
            List<String> termStrList = terms.stream().map(term -> {
                if (!xyyNatureNameToDescMap.containsKey(term.nature.toString())) {
                    return term.word + "/" + XyyNatureEnum.other.toString();
                } else {
                    return term.word + "/" + term.nature.toString();
                }
//                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary(String.join(" ", termStrList));

            List<Term> indexTerms = newIndexSegment.seg(rowDTO.getDictionary());
            List<String> indexTermStrList = indexTerms.stream().map(term -> {
                if (!xyyNatureNameToDescMap.containsKey(term.nature.toString())) {
                    return term.word + "/" + XyyNatureEnum.other.toString();
                } else {
                    return term.word + "/" + term.nature.toString();
                }
//                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
            resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
//                }
//            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/店铺展示名称&厂商_词性识别结果_" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmssS") + ".xlsx";
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
        Map<String, String> xyyNatureNameToDescMap = Arrays.stream(XyyNatureEnum.values()).collect(Collectors.toMap(item -> item.toString(), item -> item.getDesc(), (f, s) -> f));

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
//            for (String topQueryKeyword : topQueryKeywords) {
            rowDTO.setDictionary(XyyDrugCorpusUtils.replaceSpecialChar(rowDTO.getDictionary()));
//                if (rowDTO.getDictionary().contains(topQueryKeyword)) {
            List<Term> terms = newSegment.seg(rowDTO.getDictionary());
            List<String> termStrList = terms.stream().map(term -> {
                if (!xyyNatureNameToDescMap.containsKey(term.nature.toString())) {
                    return term.word + "/" + XyyNatureEnum.other.toString();
                } else {
                    return term.word + "/" + term.nature.toString();
                }
//                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary(String.join(" ", termStrList));

            List<Term> indexTerms = newIndexSegment.seg(rowDTO.getDictionary());
            List<String> indexTermStrList = indexTerms.stream().map(term -> {
                if (!xyyNatureNameToDescMap.containsKey(term.nature.toString())) {
                    return term.word + "/" + XyyNatureEnum.other.toString();
                } else {
                    return term.word + "/" + term.nature.toString();
                }
//                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
            resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
//                }
//            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/商品的规格_词性识别结果_" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmssS") + ".xlsx";
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(resultRowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(resultDictionaryExcelPath, excelRows);
        log.debug("对规格进行NER成功。");
    }

    @Test
    public void nerShowName() throws IOException {
        Set<String> topQueryKeywords = Sets.newHashSet();
//        topQueryKeywords.add("枇杷露");
//        topQueryKeywords.add("感冒灵");
//        topQueryKeywords.add("阿莫西林");
//        topQueryKeywords.add("苯磺酸氨氯地平");
//        topQueryKeywords.add("西络宁");
//        topQueryKeywords.add("金嗓子");
//        topQueryKeywords.add("唫嗓子");
//        topQueryKeywords.add("救心");
//        topQueryKeywords.add("拉唑肠溶");
//        topQueryKeywords.add("万通");
//        topQueryKeywords.add("筋骨");
//        topQueryKeywords.add("蒲地蓝消炎");
//        topQueryKeywords.add("诺氟沙星");
//        topQueryKeywords.add("达格列净");
//        topQueryKeywords.add("阿司匹林");
//        topQueryKeywords.add("连花清瘟");
//        topQueryKeywords.add("拜新同");
//        topQueryKeywords.add("龙牡壮骨");
//        topQueryKeywords.add("小柴胡");
//        topQueryKeywords.add("葡萄糖");
//        topQueryKeywords.add("维生素");
//        topQueryKeywords.add("藿香正气");
//        topQueryKeywords.add("云南白药");
//        topQueryKeywords.add("头孢");
//        topQueryKeywords.add("心宝");
//        topQueryKeywords.add("蓝芩");
//        topQueryKeywords.add("六味");
//        topQueryKeywords.add("马应龙");
//        topQueryKeywords.add("枇杷膏");
//        topQueryKeywords.add("沙库巴曲缬沙坦钠");
//        topQueryKeywords.add("健胃消食");
//        topQueryKeywords.add("护肝");
//        topQueryKeywords.add("厄贝沙坦");
//        topQueryKeywords.add("奥司他韦");
//        topQueryKeywords.add("牛黄");
//        topQueryKeywords.add("稳心");

//        topQueryKeywords.add("阿斯美");
//        topQueryKeywords.add("波立维");
//        topQueryKeywords.add("三金");
//        topQueryKeywords.add("硝苯地平");
//        topQueryKeywords.add("甲氧那明");
//        topQueryKeywords.add("立普妥");
//        topQueryKeywords.add("布洛芬");
//        topQueryKeywords.add("他达拉非");
//
//        topQueryKeywords.add("止嗽");
//        topQueryKeywords.add("阿奇霉素");
//        topQueryKeywords.add("西地那非");
//        topQueryKeywords.add("感康");
//
//        topQueryKeywords.add("替米沙坦");
//        topQueryKeywords.add("金戈");
//        topQueryKeywords.add("丹参");
//        topQueryKeywords.add("养心");
//        topQueryKeywords.add("缬沙坦");
//        topQueryKeywords.add("保心丸");
//        topQueryKeywords.add("瑞舒伐他汀");

//        topQueryKeywords.add("达格列净");
//        topQueryKeywords.add("二甲双胍");
//        topQueryKeywords.add("当归");
//        topQueryKeywords.add("安神补脑");
//        topQueryKeywords.add("白术");
//        topQueryKeywords.add("施慧达");

//        topQueryKeywords.add("美托洛尔");
//        topQueryKeywords.add("琥珀酸");
//        topQueryKeywords.add("阿胶");
//        topQueryKeywords.add("金银花");
//        topQueryKeywords.add("咳特灵");
//        topQueryKeywords.add("氨酚黄那敏");

        if (CollectionUtils.isEmpty(topQueryKeywords)) {
            log.debug("没有top搜索词。");
            return;
        }

        CustomDictionary.reload();
        CoreDictionary.reload();

        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的展示名称和通用名称_2024_04_02.xlsx";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("Excel没有数据，终止");
            return;
        }
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("没有数据，终止");
            return;
        }
        Map<String, String> xyyNatureNameToDescMap = Arrays.stream(XyyNatureEnum.values()).collect(Collectors.toMap(item -> item.toString(), item -> item.getDesc(), (f, s) -> f));

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            String dictionary = XyyDrugCorpusUtils.replaceSpecialChar(rowDTO.getDictionary());
            for (String topQueryKeyword : topQueryKeywords) {
                if (dictionary.contains(topQueryKeyword)) {
                    List<Term> terms = newSegment.seg(dictionary);
                    List<String> termStrList = terms.stream().map(term -> {
                        if (!xyyNatureNameToDescMap.containsKey(term.nature.toString())) {
                            return term.word + "/" + XyyNatureEnum.other.toString();
                        } else {
                            return term.word + "/" + term.nature.toString();
                        }
//                        return term.word + "/" + term.nature.toString();
                    }).collect(Collectors.toList());
                    rowDTO.setRealDictionary(String.join(" ", termStrList));

                    List<Term> indexTerms = newIndexSegment.seg(dictionary);
                    List<String> indexTermStrList = indexTerms.stream().map(term -> {
                        if (!xyyNatureNameToDescMap.containsKey(term.nature.toString())) {
                            return term.word + "/" + XyyNatureEnum.other.toString();
                        } else {
                            return term.word + "/" + term.nature.toString();
                        }
//                        return term.word + "/" + term.nature.toString();
                    }).collect(Collectors.toList());
                    rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
                    resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
                    continue;
                }
            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/商品名称_词性识别结果_" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmssS") + ".xlsx";
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(resultRowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(resultDictionaryExcelPath, excelRows);
        log.debug("对商品名称进行NER成功。");
    }

}
