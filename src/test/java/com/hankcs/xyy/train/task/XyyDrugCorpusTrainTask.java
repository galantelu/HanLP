package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dictionary.NatureDictionaryMaker;
import com.hankcs.hanlp.corpus.document.CorpusLoader;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.corpus.util.CorpusUtil;
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
        /* 标注制作语料库 */
        List<XyyDrugCorpusRowDTO> xyyDrugCorpusRowDTOS = XyyDrugCorpusExcelOperator.readAllRows(XyyConstants.annotationDataExcelPath);
        // TODO 10个中，8个最为训练集，1个作为验证集，1个作为测试集
        List<String> lines = convertLines(xyyDrugCorpusRowDTOS, true);
        FileUtils.writeLines(new File(XyyConstants.annotationCorpusPath), lines);
        /* 训练并生成模型 */
        final NatureDictionaryMaker dictionaryMaker = new NatureDictionaryMaker();
        CorpusLoader.walk(XyyConstants.annotationCorpusPath, document -> dictionaryMaker.compute(CorpusUtil.convert2CompatibleList(document.getSimpleSentenceList(true))));
        dictionaryMaker.saveTxtTo(XyyConstants.annotationModelPath);
    }

    @Test
    public void testTrainHmmBigramModel() throws IOException {
        /**
         * 修改hanlp.properties：
         * CoreDictionaryPath=data/xyy/model/xyy_drug_annotation_model.txt
         * BiGramDictionaryPath=data/xyy/model/xyy_drug_annotation_model.ngram.txt
         */
//        HanLP.Config.enableDebug();

        /* 加载自定义的词性标签 */
        Arrays.stream(XyyNatureEnum.values()).forEach(xyyNatureEnum -> Nature.create(xyyNatureEnum.getNature().toString()));

        // TODO 这里应该使用 验证集 和 测试集 分别对模型进行评估

        /* HMM-Bigram分词-最短分路： */
        Segment newSegment = HanLP.newSegment();
        newSegment.enablePartOfSpeechTagging(true);
        newSegment.enableCustomDictionary(false);
//        newSegment.enableIndexMode(true);
//        newSegment.enableIndexMode(1);
        /* HMM-Bigram分词-N-最短分路 */
        // 方式一：新建分词器
        Segment newNShortSegment = HanLP.newSegment("nshort");
        newNShortSegment.enablePartOfSpeechTagging(true);
        newNShortSegment.enableCustomDictionary(false);
//        newNShortSegment.enableIndexMode(true);
//        newNShortSegment.enableIndexMode(1);

        String text;

        text = "999感冒灵颗粒";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));

        text = "汤臣倍健辅酶Q10天然维生素E软胶囊";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
    }

    private List<String> convertLines(List<XyyDrugCorpusRowDTO> xyyDrugCorpusRowDTOS, boolean isOnlyAnnotation) {
        if (CollectionUtils.isEmpty(xyyDrugCorpusRowDTOS)) {
            return Lists.newArrayList();
        }
        return xyyDrugCorpusRowDTOS.stream().map(item -> convertLine(item, isOnlyAnnotation)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String convertLine(XyyDrugCorpusRowDTO xyyDrugCorpusRowDTO, boolean isOnlyAnnotation) {
        if (Objects.isNull(xyyDrugCorpusRowDTO) || StringUtils.isEmpty(xyyDrugCorpusRowDTO.getBrand())
            || StringUtils.isEmpty(xyyDrugCorpusRowDTO.getCommonName())) {
            return null;
        }
        if (isOnlyAnnotation && StringUtils.isEmpty(xyyDrugCorpusRowDTO.getAnnotationCommonName())) {
            return null;
        }
        if (StringUtils.isEmpty(xyyDrugCorpusRowDTO.getAnnotationCommonName())) {
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
            return new StringBuilder(xyyDrugCorpusRowDTO.getBrand().replaceAll("/", "").replaceAll(" ", ""))
                .append("/")
                .append(XyyNatureEnum.brand.getNature().toString())
                .append(" ")
                .append(xyyDrugCorpusRowDTO.getCommonName().replaceAll("/", "").replaceAll(" ", ""))
                .append("/")
                .append(XyyNatureEnum.drug.getNature().toString())
                .toString();
        } else {
            return new StringBuilder(xyyDrugCorpusRowDTO.getBrand().replaceAll("/", "").replaceAll(" ", ""))
                .append("/")
                .append(XyyNatureEnum.brand.getNature().toString())
                .append(" ")
                .append(xyyDrugCorpusRowDTO.getAnnotationCommonName())
                .toString();
        }
    }

}
