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
import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
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

    private static final String modelPath = "data/xyy/train/xyy_drug_model";

    /**
     * 训练HMM-NGram分词模型
     */
    @Test
    public void doTrainHmmBigramModel() throws IOException {
        /* 标注制作语料库 */
        List<XyyDrugCorpusTrainRowDTO> xyyDrugCorpusTrainRowDTOS = loadData();
        // TODO 10个中，8个最为训练集，1个作为验证集，1个作为测试集
        List<String> lines = convertLines(xyyDrugCorpusTrainRowDTOS);
        FileUtils.writeLines(new File(corpusPath), lines);

        /* 训练并生成模型 */
        final NatureDictionaryMaker dictionaryMaker = new NatureDictionaryMaker();
        CorpusLoader.walk(corpusPath, document -> dictionaryMaker.compute(CorpusUtil.convert2CompatibleList(document.getSimpleSentenceList(true))));
        dictionaryMaker.saveTxtTo(modelPath);
    }

    @Test
    public void testTrainHmmBigramModel() throws IOException {

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

//        /* HMM-Bigram分词-N-最短分路 */
//        // 方式一：新建分词器
//        Segment newNShortSegment = new NShortSegment();
//        // 方式二：新建分词器
//        newNShortSegment = HanLP.newSegment("nshort");
//
//        /* 感知机分词 */
//        // 方式一：使用全局的
//        PerceptronLexicalAnalyzer globalPerceptronLexicalAnalyzer = TokenizerSingleton.getGlobalPerceptronLexicalAnalyzer();
//        // 方式二：新建分词器
//        PerceptronLexicalAnalyzer newPerceptronLexicalAnalyzer = new PerceptronLexicalAnalyzer();
//        newPerceptronLexicalAnalyzer.enablePartOfSpeechTagging(true);
//        /* CRF分词 */
//        // 方式一：使用全局的
//        CRFLexicalAnalyzer globalCRFLexicalAnalyzer = TokenizerSingleton.getGlobalCRFLexicalAnalyzer();
//        // 方式二：新建分词器
//        CRFLexicalAnalyzer newCRFLexicalAnalyzer = new CRFLexicalAnalyzer();
//        newCRFLexicalAnalyzer.enablePartOfSpeechTagging(true);

        String text;

        text = "999感冒灵颗粒";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
//        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
//        log.info("【感知机分词】文本【{}】结果：{}", text, newPerceptronLexicalAnalyzer.analyze(text));
//        log.info("【CRF分词】文本【{}】结果：{}", text, newCRFLexicalAnalyzer.analyze(text));

        text = "汤臣倍健辅酶Q10天然维生素E软胶囊";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
//        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
//        log.info("【感知机分词】文本【{}】结果：{}", text, newPerceptronLexicalAnalyzer.analyze(text));
//        log.info("【CRF分词】文本【{}】结果：{}", text, newCRFLexicalAnalyzer.analyze(text));

    }

    private List<String> convertLines(List<XyyDrugCorpusTrainRowDTO> xyyDrugCorpusTrainRowDTOS) {
        if (CollectionUtils.isEmpty(xyyDrugCorpusTrainRowDTOS)) {
            return Lists.newArrayList();
        }
        return xyyDrugCorpusTrainRowDTOS.stream().map(item -> convertLine(item)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String convertLine(XyyDrugCorpusTrainRowDTO xyyDrugCorpusTrainRowDTO) {
        if (Objects.isNull(xyyDrugCorpusTrainRowDTO) || StringUtils.isEmpty(xyyDrugCorpusTrainRowDTO.getBrand())
            || StringUtils.isEmpty(xyyDrugCorpusTrainRowDTO.getCommonName())) {
            return null;
        }
        // 简单点
        // 去掉空格和/
        return new StringBuilder(xyyDrugCorpusTrainRowDTO.getBrand().replaceAll("/", "").replaceAll(" ", ""))
            .append("/")
            .append(XyyNatureEnum.brand.getNature().toString())
            .append(" ")
            .append(xyyDrugCorpusTrainRowDTO.getCommonName().replaceAll("/", "").replaceAll(" ", ""))
            .append("/")
            .append(XyyNatureEnum.drug.getNature().toString())
            .toString();
    }


    private List<XyyDrugCorpusTrainRowDTO> loadData() {
        /* 解析Excel */
        // 表头
        Map<Integer, String> headMaps = Maps.newHashMapWithExpectedSize(16);
        // 行数据
        List<EasyExcelRowDataWrapper<XyyDrugCorpusTrainExcelRow>> excelRows = Lists.newArrayList();
        // 读取Excel
        EasyExcel.read(dataExcelPath, XyyDrugCorpusTrainExcelRow.class,
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
            .commonName(rowData.getCommonName()).build();
    }

}
