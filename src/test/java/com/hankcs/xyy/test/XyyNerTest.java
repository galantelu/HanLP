package com.hankcs.xyy.test;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.xyy.singletons.TokenizerSingleton;
import com.hankcs.xyy.train.enums.XyyNatureEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class XyyNerTest {

    @Test
    public void test() throws IOException {
//        HanLP.Config.enableDebug();
        /* 加入自定义词典 */
        // 覆盖式插入
        // 品牌
        CustomDictionary.insert("万邦医药", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("仲景", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("欧贝", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("哈药六", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("亚邦药业", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("999", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("妇炎洁", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("汤臣倍健", XyyNatureEnum.brand.getNature().toString() + " 1024");
        /* 药品 */
        CustomDictionary.insert("阿司匹林肠溶", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("复方黄连素", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("双氯芬酸钠缓释", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("醋酸泼尼松", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("辅酶Q10维生素E", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("辅酶Q10", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("维生素E", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("维生素E", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("感冒灵", XyyNatureEnum.drug.getNature().toString() + " 1024");
        /* 修饰词 */
        CustomDictionary.insert("天然", XyyNatureEnum.qualifier.getNature().toString() + " 1024");
        /* 剂型 */
        CustomDictionary.insert("口服液", XyyNatureEnum.dosageUnit.getNature().toString() + " 1024");
        CustomDictionary.insert("颗粒", XyyNatureEnum.dosageUnit.getNature().toString() + " 1024");
        CustomDictionary.insert("胶囊", XyyNatureEnum.dosageUnit.getNature().toString() + " 1024");
        CustomDictionary.insert("软胶囊", XyyNatureEnum.dosageUnit.getNature().toString() + " 1024");

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

        /* HMM-Bigram分词-N-最短分路 */
        // 方式一：新建分词器
        Segment newNShortSegment = new NShortSegment();
        // 方式二：新建分词器
        newNShortSegment = HanLP.newSegment("nshort");

        /* 感知机分词 */
        // 方式一：使用全局的
        PerceptronLexicalAnalyzer globalPerceptronLexicalAnalyzer = TokenizerSingleton.getGlobalPerceptronLexicalAnalyzer();
        // 方式二：新建分词器
        PerceptronLexicalAnalyzer newPerceptronLexicalAnalyzer = new PerceptronLexicalAnalyzer();
        newPerceptronLexicalAnalyzer.enablePartOfSpeechTagging(true);
        /* CRF分词 */
        // 方式一：使用全局的
        CRFLexicalAnalyzer globalCRFLexicalAnalyzer = TokenizerSingleton.getGlobalCRFLexicalAnalyzer();
        // 方式二：新建分词器
        CRFLexicalAnalyzer newCRFLexicalAnalyzer = new CRFLexicalAnalyzer();
        newCRFLexicalAnalyzer.enablePartOfSpeechTagging(true);

        String text;

        text = "999感冒灵颗粒";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, newPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, newCRFLexicalAnalyzer.analyze(text));

        text = "汤臣倍健辅酶Q10天然维生素E软胶囊";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, newPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, newCRFLexicalAnalyzer.analyze(text));
    }

}
