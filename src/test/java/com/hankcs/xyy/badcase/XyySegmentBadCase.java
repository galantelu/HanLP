package com.hankcs.xyy.badcase;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.xyy.singletons.TokenizerSingleton;
import com.hankcs.xyy.train.enums.XyyNatureEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class XyySegmentBadCase {

    @Test
    public void testStandardSegment() {

        /* 加入自定义词典 */
        // 覆盖式插入
        // 品牌
        CustomDictionary.insert("芯尔泰", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("李夫人", XyyNatureEnum.brand.getNature().toString() + " 1024");
        CustomDictionary.insert("朗迪", XyyNatureEnum.brand.getNature().toString() + " 1024");
        /* 药品 or 成分 */
        CustomDictionary.insert("磷酸氢钙", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("维D2", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("维生素D", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("碳酸钙", XyyNatureEnum.drug.getNature().toString() + " 1024");
        CustomDictionary.insert("D3", XyyNatureEnum.drug.getNature().toString() + " 1024");
        /* 修饰词 */
        CustomDictionary.insert("采森牌", XyyNatureEnum.qualifier.getNature().toString() + " 1024");
        /* 剂型 */
        CustomDictionary.insert("钙片", XyyNatureEnum.dosageUnit.getNature().toString() + " 1024");
        CustomDictionary.insert("片", XyyNatureEnum.dosageUnit.getNature().toString() + " 1024");

        String text;

        StandardTokenizer globalStandardTokenizer = TokenizerSingleton.getGlobalStandardTokenizer();
        Segment newNShortSegment = new NShortSegment();
        PerceptronLexicalAnalyzer globalPerceptronLexicalAnalyzer = TokenizerSingleton.getGlobalPerceptronLexicalAnalyzer();
        CRFLexicalAnalyzer globalCRFLexicalAnalyzer = TokenizerSingleton.getGlobalCRFLexicalAnalyzer();

        text = "芯尔泰 维D2磷酸氢钙片";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));

        text = "李夫人 采森牌维生素D钙片";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));

        text = "朗迪 碳酸钙D3片(Ⅱ)";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));

        log.info("===================开启索引分词===================");
        StandardTokenizer.SEGMENT.enableIndexMode(true);
        newNShortSegment.enableIndexMode(true);
        globalPerceptronLexicalAnalyzer.enableIndexMode(true);
        globalCRFLexicalAnalyzer.enableIndexMode(true);

        text = "芯尔泰 维D2磷酸氢钙片";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));

        text = "李夫人 采森牌维生素D钙片";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));

        text = "朗迪 碳酸钙D3片(Ⅱ)";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));

        log.info("===================开启强制用户自定义词典优先===================");

        StandardTokenizer.SEGMENT.enableCustomDictionaryForcing(true);
        newNShortSegment.enableCustomDictionaryForcing(true);
        globalPerceptronLexicalAnalyzer.enableCustomDictionaryForcing(true);
        globalCRFLexicalAnalyzer.enableCustomDictionaryForcing(true);

        text = "芯尔泰 维D2磷酸氢钙片";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));

        text = "李夫人 采森牌维生素D钙片";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));

        text = "朗迪 碳酸钙D3片(Ⅱ)";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, globalStandardTokenizer.segment(text));
        log.info("【HMM-Bigram分词-N-最短分路】文本【{}】结果：{}", text, newNShortSegment.seg(text));
        log.info("【感知机分词】文本【{}】结果：{}", text, globalPerceptronLexicalAnalyzer.analyze(text));
        log.info("【CRF分词】文本【{}】结果：{}", text, globalCRFLexicalAnalyzer.analyze(text));
    }

}
