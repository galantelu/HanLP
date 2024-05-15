package com.hankcs.xyy.test;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class XyyCustomDictionaryNerTest {

    @Test
    public void test() throws IOException {
        CoreDictionary.reload();
        CustomDictionary.reload();

        HanLP.Config.enableDebug();

        /* HMM-Bigram分词-最短分路： */
        // 方式三：新建分词器
        Segment newSegment = HanLP.newSegment("viterbi");
        newSegment.enablePartOfSpeechTagging(true);
        newSegment.enableCustomDictionaryForcing(true);
        newSegment.enableNormalization(true);

        Segment newIndexSegment = HanLP.newSegment("viterbi");
        newIndexSegment.enablePartOfSpeechTagging(true);
        newIndexSegment.enableCustomDictionaryForcing(true);
        newIndexSegment.enableIndexMode(1);
        newIndexSegment.enableNormalization(true);

//        log.info("{}", CustomDictionary.get("诺金诺金"));
//        CustomDictionary.insert("诺金诺金", "brand 999999");
//        log.info("{}", CustomDictionary.get("诺金诺金"));

        String text;

        text = "乐克菲强力枇杷露";
        log.info("文本【{}】粗结果：{}", text, newSegment.seg(text));
        log.info("文本【{}】细结果：{}", text, newIndexSegment.seg(text));

        text = "福记坊枇杷秋梨膏";
        log.info("文本【{}】粗结果：{}", text, newSegment.seg(text));
        log.info("文本【{}】细结果：{}", text, newIndexSegment.seg(text));

        text = "益克停强力枇杷露";
        log.info("文本【{}】粗结果：{}", text, newSegment.seg(text));
        log.info("文本【{}】细结果：{}", text, newIndexSegment.seg(text));

        text = "诺金诺金复方感冒灵片";
        log.info("文本【{}】粗结果：{}", text, newSegment.seg(text));
        log.info("文本【{}】细结果：{}", text, newIndexSegment.seg(text));

        text = "云南万裕";
        log.info("文本【{}】粗结果：{}", text, newSegment.seg(text));
        log.info("文本【{}】细结果：{}", text, newIndexSegment.seg(text));

        text = "感冒灵";
        log.info("文本【{}】粗结果：{}", text, newSegment.seg(text));
        log.info("文本【{}】细结果：{}", text, newIndexSegment.seg(text));
    }

}
