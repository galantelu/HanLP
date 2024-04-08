package com.hankcs.xyy.test;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class XyyCustomDictionaryNerTest {

    @Test
    public void test() throws IOException {
//        HanLP.Config.enableDebug();

        /* HMM-Bigram分词-最短分路： */
        // 方式三：新建分词器
        Segment newSegment = HanLP.newSegment("viterbi");
        newSegment.enablePartOfSpeechTagging(true);
        newSegment.enableCustomDictionaryForcing(true);

        String text;

        text = "999感冒灵颗粒";
        log.info("【HMM-Bigram分词-最短分路】文本【{}】结果：{}", text, newSegment.seg(text));
    }

}
