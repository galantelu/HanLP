package com.hankcs.xyy.train.task;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dictionary.NatureDictionaryMaker;
import com.hankcs.hanlp.corpus.document.CorpusLoader;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.corpus.util.CorpusUtil;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.xyy.singletons.TokenizerSingleton;
import com.hankcs.xyy.train.dto.XyyDrugCorpusTrainRowDTO;
import com.hankcs.xyy.train.easyexcel.EasyExcelDataAnalysisEventListener;
import com.hankcs.xyy.train.easyexcel.EasyExcelDataProcessor;
import com.hankcs.xyy.train.easyexcel.EasyExcelRowDataWrapper;
import com.hankcs.xyy.train.easyexcel.rows.XyyDrugCorpusTrainExcelRow;
import com.hankcs.xyy.train.enums.XyyNatureEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class XyyDrugCorpusTrainTask {

    private static final String dataExcelPath = "data/xyy/train/xyy_drug_corpus.xlsx";

    private static final String corpusPath = "data/xyy/train/xyy_drug_corpus.txt";

    private static final String modelPath = "data/xyy/model/xyy_drug_model";

    private static final String annotationDataExcelPath = "data/xyy/train/xyy_drug_corpus_annotation.xlsx";

    private static final String annotationCorpusPath = "data/xyy/train/xyy_drug_corpus_annotation.txt";

    private static final String annotationModelPath = "data/xyy/model/xyy_drug_annotation_model";

    /**
     * 训练HMM-NGram分词模型
     */
    @Test
    public void doTrainHmmBigramModel() throws IOException {
//        /* 标注制作语料库 */
//        List<XyyDrugCorpusTrainRowDTO> xyyDrugCorpusTrainRowDTOS = loadData(dataExcelPath);
//        // TODO 10个中，8个最为训练集，1个作为验证集，1个作为测试集
//        List<String> lines = convertLines(xyyDrugCorpusTrainRowDTOS, false);
//        FileUtils.writeLines(new File(corpusPath), lines);
//        /* 训练并生成模型 */
//        final NatureDictionaryMaker dictionaryMaker = new NatureDictionaryMaker();
//        CorpusLoader.walk(corpusPath, document -> dictionaryMaker.compute(CorpusUtil.convert2CompatibleList(document.getSimpleSentenceList(true))));
//        dictionaryMaker.saveTxtTo(modelPath);

        /* 标注制作语料库 */
        List<XyyDrugCorpusTrainRowDTO> xyyDrugCorpusTrainRowDTOS = loadData(annotationDataExcelPath);
        // TODO 10个中，8个最为训练集，1个作为验证集，1个作为测试集
        List<String> lines = convertLines(xyyDrugCorpusTrainRowDTOS, true);
        FileUtils.writeLines(new File(annotationCorpusPath), lines);
        /* 训练并生成模型 */
        final NatureDictionaryMaker dictionaryMaker = new NatureDictionaryMaker();
        CorpusLoader.walk(annotationCorpusPath, document -> dictionaryMaker.compute(CorpusUtil.convert2CompatibleList(document.getSimpleSentenceList(true))));
        dictionaryMaker.saveTxtTo(annotationModelPath);
    }

    @Test
    public void testTrainHmmBigramModel() throws IOException {
        /**
         * 修改hanlp.properties：
         * CoreDictionaryPath=data/xyy/train/xyy_drug_annotation_model.txt
         * BiGramDictionaryPath=data/xyy/model/xyy_drug_annotation_model.ngram.txt
         */
//        HanLP.Config.enableDebug();

        /* 加载自定义的词性标签 */
        Arrays.stream(XyyNatureEnum.values()).forEach(xyyNatureEnum -> Nature.create(xyyNatureEnum.getNature().toString()));

        // TODO 这里应该使用 验证集 和 测试集 分别对模型进行评估

        /* HMM-Bigram分词-最短分路： */
        // 方式一：使用全局的
        Segment globalSegment = StandardTokenizer.SEGMENT;
        // 方式二：使用全局的
        StandardTokenizer globalStandardTokenizer = TokenizerSingleton.getGlobalStandardTokenizer();
        // 方式三：新建分词器
        Segment newSegment = HanLP.newSegment();
        // 方式四：新建分词器
        newSegment = HanLP.newSegment("viterbi");
        // 方式四：新建分词器
        newSegment = new ViterbiSegment();
        newSegment.enablePartOfSpeechTagging(true);
        newSegment.enableCustomDictionary(false);
//        newSegment.enableIndexMode(true);
//        newSegment.enableIndexMode(1);
        /* HMM-Bigram分词-N-最短分路 */
        // 方式一：新建分词器
        Segment newNShortSegment = new NShortSegment();
        // 方式二：新建分词器
        newNShortSegment = HanLP.newSegment("nshort");
        newNShortSegment.enableCustomDictionary(false);
//        newNShortSegment.enableIndexMode(true);
//        newNShortSegment.enableIndexMode(1);

        String text;

        text = "999感冒灵颗粒";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
//        log.info("【感知机分词】文本【{}】结果：{}", text, newPerceptronLexicalAnalyzer.analyze(text));
//        log.info("【CRF分词】文本【{}】结果：{}", text, newCRFLexicalAnalyzer.analyze(text));

        text = "汤臣倍健辅酶Q10天然维生素E软胶囊";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
//        log.info("【感知机分词】文本【{}】结果：{}", text, newPerceptronLexicalAnalyzer.analyze(text));
//        log.info("【CRF分词】文本【{}】结果：{}", text, newCRFLexicalAnalyzer.analyze(text));
    }

    private List<String> convertLines(List<XyyDrugCorpusTrainRowDTO> xyyDrugCorpusTrainRowDTOS, boolean isOnlyAnnotation) {
        if (CollectionUtils.isEmpty(xyyDrugCorpusTrainRowDTOS)) {
            return Lists.newArrayList();
        }
        return xyyDrugCorpusTrainRowDTOS.stream().map(item -> convertLine(item, isOnlyAnnotation)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String convertLine(XyyDrugCorpusTrainRowDTO xyyDrugCorpusTrainRowDTO, boolean isOnlyAnnotation) {
        if (Objects.isNull(xyyDrugCorpusTrainRowDTO) || StringUtils.isEmpty(xyyDrugCorpusTrainRowDTO.getBrand())
            || StringUtils.isEmpty(xyyDrugCorpusTrainRowDTO.getCommonName())) {
            return null;
        }
        if (isOnlyAnnotation && StringUtils.isEmpty(xyyDrugCorpusTrainRowDTO.getAnnotationCommonName())) {
            return null;
        }
        if (StringUtils.isEmpty(xyyDrugCorpusTrainRowDTO.getAnnotationCommonName())) {
            // 特殊数据：
            /**
             * 正常数据：品牌、通用名（末尾为剂型）
             * 特殊数据：
             *  品牌或通用名没有数据
             *  品牌：（品牌）或(品牌)、实际不存在的品牌（如纯数字0、1；）
             *  通用名：（品牌）通用名、通用名（品牌）、（品牌）（子品牌）通用名、（子品牌）通用名（品牌）、品牌牌通用名
             *
             */
            // 去掉空格和/
            // 品牌存在 纯数字的无意义值，如0、1、还有特殊字符
            // 通用名的开头存在非品牌的XXX牌
            //
            return new StringBuilder(xyyDrugCorpusTrainRowDTO.getBrand().replaceAll("/", "").replaceAll(" ", ""))
                .append("/")
                .append(XyyNatureEnum.brand.getNature().toString())
                .append(" ")
                .append(xyyDrugCorpusTrainRowDTO.getCommonName().replaceAll("/", "").replaceAll(" ", ""))
                .append("/")
                .append(XyyNatureEnum.drug.getNature().toString())
                .toString();
        } else {
            return new StringBuilder(xyyDrugCorpusTrainRowDTO.getBrand().replaceAll("/", "").replaceAll(" ", ""))
                .append("/")
                .append(XyyNatureEnum.brand.getNature().toString())
                .append(" ")
                .append(xyyDrugCorpusTrainRowDTO.getAnnotationCommonName())
                .toString();
        }
    }


    private List<XyyDrugCorpusTrainRowDTO> loadData(String excelPath) {
        /* 解析Excel */
        // 表头
        Map<Integer, String> headMaps = Maps.newHashMapWithExpectedSize(16);
        // 行数据
        List<EasyExcelRowDataWrapper<XyyDrugCorpusTrainExcelRow>> excelRows = Lists.newArrayList();
        // 读取Excel
        EasyExcel.read(excelPath, XyyDrugCorpusTrainExcelRow.class,
            new EasyExcelDataAnalysisEventListener<>(new EasyExcelDataProcessor<XyyDrugCorpusTrainExcelRow>() {
                @Override
                public void process(List<EasyExcelRowDataWrapper<XyyDrugCorpusTrainExcelRow>> rows, AnalysisContext context) {
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

    private static XyyDrugCorpusTrainRowDTO convert(XyyDrugCorpusTrainExcelRow rowData) {
        if (Objects.isNull(rowData)) {
            return null;
        }
        return XyyDrugCorpusTrainRowDTO.builder().brand(rowData.getBrand())
            .commonName(rowData.getCommonName()).annotationCommonName(rowData.getAnnotationCommonName()).build();
    }

}
