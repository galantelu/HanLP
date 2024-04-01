package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dictionary.NatureDictionaryMaker;
import com.hankcs.hanlp.corpus.document.CorpusLoader;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.corpus.util.CorpusUtil;
import com.hankcs.hanlp.dictionary.CoreBiGramTableDictionary;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.xyy.constants.XyyConstants;
import com.hankcs.xyy.train.dto.XyyDrugCorpusRowDTO;
import com.hankcs.xyy.train.enums.XyyNatureEnum;
import com.hankcs.xyy.train.operators.XyyDrugCorpusExcelOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class XyyDrugCorpusTrainTask {

    /**
     * 训练HMM-NGram分词模型
     */
    @Test
    public void doTrainHmmBigramModel() throws IOException {
        /* 可变参数 */
        String corpusExcelPath = XyyConstants.corpusExcelPath;
        String modelPath = XyyConstants.modelPath;

        /* 标注制作语料库 */
        List<XyyDrugCorpusRowDTO> xyyDrugCorpusRowDTOS = XyyDrugCorpusExcelOperator.readAllRows(corpusExcelPath);
        // TODO 10个中，8个最为训练集，1个作为验证集，1个作为测试集
        List<String> lines = convertLines(xyyDrugCorpusRowDTOS);
        log.info("语料集，总共{}行", lines.size());
        FileUtils.writeLines(new File(XyyConstants.corpusPath), lines);

        /* 训练并生成模型 */
        final NatureDictionaryMaker dictionaryMaker = new NatureDictionaryMaker();
        CorpusLoader.walk(XyyConstants.corpusPath, document -> {
            /*
             * 将复合词中的单词作为单词，特别注意，目前仅支持一层，如 ： [感冒/drugDisease 灵/qualifier]/coreDrug 颗粒/dosageUnit
             * 但不支持 ： [[感冒/drugDisease 灵/qualifier]/coreDrug 颗粒/dosageUnit]/drug
             */
            // 以复合词层次关系计算模型
            dictionaryMaker.compute(CorpusUtil.convert2CompatibleList(document.getSimpleSentenceList(false)));
            // 以最小层次关系计算模型
            dictionaryMaker.compute(CorpusUtil.convert2CompatibleList(document.getSimpleSentenceList(true)));
        });
        dictionaryMaker.saveTxtTo(modelPath);
    }

    @Test
    public void testTrainHmmBigramModel() throws IOException {
        /* 可变参数 */
        String modelPath = XyyConstants.modelPath;

//        HanLP.Config.enableDebug();

        /* 加载自定义的词性标签 */
        Arrays.stream(XyyNatureEnum.values()).forEach(xyyNatureEnum -> Nature.create(xyyNatureEnum.getNature().toString()));

        /* 重新生成预料模型的二进制文件并加载 */
        HanLP.Config.CoreDictionaryPath = modelPath + ".txt";
        HanLP.Config.BiGramDictionaryPath = modelPath + ".ngram.txt";
        CoreDictionary.reload();
        CoreBiGramTableDictionary.reload();

        // TODO 这里应该使用 验证集 和 测试集 分别对模型进行评估

        /* HMM-Bigram分词-最短分路-粗分词： */
        Segment newSegment = HanLP.newSegment();
        newSegment.enablePartOfSpeechTagging(true);
        newSegment.enableCustomDictionary(false);
        newSegment.enableNormalization(true);

        /* HMM-Bigram分词-最短分路-细分词： */
        Segment indexNewSegment = HanLP.newSegment();
        indexNewSegment.enablePartOfSpeechTagging(true);
        indexNewSegment.enableCustomDictionary(false);
        indexNewSegment.enableNormalization(true);
//        indexNewSegment.enableIndexMode(true);
        indexNewSegment.enableIndexMode(1);
//        indexNewSegment.enableNormalization(true);

        String text;

        text = "999 感冒灵颗粒";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "感冒灵";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10天然维生素E软胶囊";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10维生素E软胶囊";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "辅酶Q10";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "辅酶Q10维生素E";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "汤臣倍健辅酶q10天然维生素E软胶囊";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10天然维生素e软胶囊";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "汤臣倍健辅酶q10天然维生素e软胶囊";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");
    }

    private List<String> convertLines(List<XyyDrugCorpusRowDTO> xyyDrugCorpusRowDTOS) {
        if (CollectionUtils.isEmpty(xyyDrugCorpusRowDTOS)) {
            return Lists.newArrayList();
        }
        return xyyDrugCorpusRowDTOS.stream().map(item -> convertLine(item)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String convertLine(XyyDrugCorpusRowDTO xyyDrugCorpusRowDTO) {
        if (Objects.isNull(xyyDrugCorpusRowDTO) || StringUtils.isEmpty(xyyDrugCorpusRowDTO.getBrand())
            || StringUtils.isEmpty(xyyDrugCorpusRowDTO.getAnnotationCommonName())) {
            return null;
        }
        return new StringBuilder(xyyDrugCorpusRowDTO.getBrand().replaceAll("/", "").replaceAll(" ", ""))
            .append("/")
            .append(XyyNatureEnum.brand.getNature().toString())
            .append(" ")
            .append(xyyDrugCorpusRowDTO.getAnnotationCommonName())
            .toString();
    }

}
