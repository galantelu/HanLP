package com.hankcs.xyy.test;

import com.google.common.collect.Sets;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.xyy.train.enums.XyyNatureEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Slf4j
public class XyyCustomDictionaryNerTest {

    @Test
    public void test() throws IOException {
        CoreDictionary.reload();
        CustomDictionary.reload();

//        HanLP.Config.enableDebug();

        this.loadCustomDictionary();

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


    private void loadCustomDictionary() {
        String brandDictionaryPath = "data/xyy/dictionary/brand.txt";
        String corpDictionaryPath = "data/xyy/dictionary/corp.txt";
        String specDictionaryPath = "data/xyy/dictionary/spec.txt";
        String dosageDictionaryPath = "data/xyy/dictionary/dosage.txt";
        String coreDictionaryPath = "data/xyy/dictionary/core.txt";
        String otherDictionaryPath = "data/xyy/dictionary/other.txt";

        try {
            Set<String> brands = Sets.newHashSet(FileUtils.readLines(new File(brandDictionaryPath), "UTF-8"));
            brands.remove("");

            brands.forEach(item -> CustomDictionary.insert(item, XyyNatureEnum.brand.getNature().toString() + " 1024"));
        } catch (Exception e) {
            log.error("加载词典出现异常，{}，e：", e);
        }

        try {
            Set<String> corps = Sets.newHashSet(FileUtils.readLines(new File(corpDictionaryPath), "UTF-8"));
            corps.remove("");

            corps.forEach(item -> CustomDictionary.insert(item, XyyNatureEnum.corp.getNature().toString() + " 1024"));
        } catch (Exception e) {
            log.error("加载词典出现异常，{}，e：", e);
        }


        try {
            Set<String> specs = Sets.newHashSet(FileUtils.readLines(new File(specDictionaryPath), "UTF-8"));
            specs.remove("");

            specs.forEach(item -> CustomDictionary.insert(item, XyyNatureEnum.spec.getNature().toString() + " 1024"));
        } catch (Exception e) {
            log.error("加载词典出现异常，{}，e：", e);
        }

        try {
            Set<String> dosages = Sets.newHashSet(FileUtils.readLines(new File(dosageDictionaryPath), "UTF-8"));
            dosages.remove("");

            dosages.forEach(item -> CustomDictionary.insert(item, XyyNatureEnum.dosage.getNature().toString() + " 1024"));
        } catch (Exception e) {
            log.error("加载词典出现异常，{}，e：", e);
        }

        try {
            Set<String> cores = Sets.newHashSet(FileUtils.readLines(new File(coreDictionaryPath), "UTF-8"));
            cores.remove("");

            cores.forEach(item -> CustomDictionary.insert(item, XyyNatureEnum.core.getNature().toString() + " 1024"));
        } catch (Exception e) {
            log.error("加载词典出现异常，{}，e：", e);
        }

        try {
            Set<String> others = Sets.newHashSet(FileUtils.readLines(new File(otherDictionaryPath), "UTF-8"));
            others.remove("");

            others.forEach(item -> CustomDictionary.insert(item, XyyNatureEnum.other.getNature().toString() + " 1024"));
        } catch (Exception e) {
            log.error("加载词典出现异常，{}，e：", e);
        }

    }

}
