package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.dictionary.NatureDictionaryMaker;
import com.hankcs.hanlp.corpus.document.CorpusLoader;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.corpus.util.CorpusUtil;
import com.hankcs.hanlp.dictionary.CoreBiGramTableDictionary;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.model.crf.CRFNERecognizer;
import com.hankcs.hanlp.model.crf.CRFPOSTagger;
import com.hankcs.hanlp.model.crf.CRFSegmenter;
import com.hankcs.hanlp.model.perceptron.*;
import com.hankcs.hanlp.model.perceptron.tagset.NERTagSet;
import com.hankcs.hanlp.model.perceptron.tagset.TagSet;
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

    @Test
    public void trainPkuAndXyyDrugModels() throws IOException {
        HanLP.Config.enableDebug();
        this.doTrainHmmBigramModel("data/xyy/train/199801_xyy_drug.txt", "data/xyy/model/199801_xyy_drug_model");
        this.doTrainPerceptronModel("data/xyy/train/199801_xyy_drug.txt",
                "data/xyy/model/perceptron/199801_xyy_drug_cws.bin",
                "data/xyy/model/perceptron/199801_xyy_drug_pos.bin",
                "data/xyy/model/perceptron/199801_xyy_drug_ner.bin");
        this.doTrainCrfModel("data/xyy/train/199801_xyy_drug.txt",
                "data/xyy/model/crf/199801_xyy_drug_cws.bin",
                "data/xyy/model/crf/199801_xyy_drug_pos.bin",
                "data/xyy/model/crf/199801_xyy_drug_ner.bin");
    }

    @Test
    public void comparePkuAndXyyDrugModels() throws IOException {
        this.testTrainHmmBigramModel("data/xyy/model/199801_xyy_drug_model");
        this.testTrainPerceptronModel("data/xyy/model/perceptron/199801_xyy_drug_cws.bin",
                "data/xyy/model/perceptron/199801_xyy_drug_pos.bin",
                "data/xyy/model/perceptron/199801_xyy_drug_ner.bin");
        this.testTrainCrfModel("data/xyy/model/crf/199801_xyy_drug_cws.bin",
                "data/xyy/model/crf/199801_xyy_drug_pos.bin",
                "data/xyy/model/crf/199801_xyy_drug_ner.bin");
    }

    @Test
    public void trainModels() throws IOException {
        this.generateXyyDrugCwsText();
        this.doTrainHmmBigramModel(XyyConstants.corpusPath, XyyConstants.modelPath);
        this.doTrainPerceptronModel(XyyConstants.corpusPath, XyyConstants.perceptronCwsModelPath, XyyConstants.perceptronPosModelPath, XyyConstants.perceptronNerModelPath);
        this.doTrainCrfModel(XyyConstants.corpusPath, XyyConstants.crfCwsModelPath, XyyConstants.crfPosModelPath, XyyConstants.crfNerModelPath);
    }

    @Test
    public void compareModels() throws IOException {
        this.testTrainHmmBigramModel(XyyConstants.modelPath);
        this.testTrainPerceptronModel(XyyConstants.perceptronCwsModelPath, XyyConstants.perceptronPosModelPath, XyyConstants.perceptronNerModelPath);
        this.testTrainCrfModel(XyyConstants.crfCwsModelPath, XyyConstants.crfPosModelPath, XyyConstants.crfNerModelPath);
    }

    /**
     * 训练CRF模型
     *
     * @param corpusPath
     * @param cwsModelPath
     * @param posModelPath
     * @param nerModelPath
     * @throws IOException
     */
    public void doTrainCrfModel(String corpusPath, String cwsModelPath, String posModelPath, String nerModelPath) throws IOException {
        /* 可变参数 */
        if (StringUtils.isEmpty(corpusPath)) {
            corpusPath = XyyConstants.corpusPath;
        }
        if (StringUtils.isEmpty(cwsModelPath)) {
            cwsModelPath = XyyConstants.crfCwsModelPath;
        }
        if (StringUtils.isEmpty(posModelPath)) {
            posModelPath = XyyConstants.crfPosModelPath;
        }
        if (StringUtils.isEmpty(nerModelPath)) {
            nerModelPath = XyyConstants.crfNerModelPath;
        }
//        String corpusPath = XyyConstants.corpusPath;
//        String cwsModelPath = XyyConstants.crfCwsModelPath;
//        String posModelPath = XyyConstants.crfPosModelPath;
//        String nerModelPath = XyyConstants.crfNerModelPath;

        /* 训练并生成模型 */
        // 感知机中文分词
        CRFSegmenter cwsTrainer = new CRFSegmenter(null);
        cwsTrainer.train(corpusPath, cwsModelPath);
        log.info("CRF中文分词，训练模型完成。");
        // 感知机词性标注
        CRFPOSTagger posTrainer = new CRFPOSTagger(null);
        posTrainer.train(corpusPath, posModelPath);
        log.info("CRF词性标注，训练模型完成。");
        // 感知机NER
        String[] customNERTags = Arrays.stream(XyyNatureEnum.values()).map(xyyNatureEnum -> xyyNatureEnum.getNature().toString())
                .collect(Collectors.toList()).toArray(new String[]{});
        CRFNERecognizer nerTrainer = new CRFNERecognizer(null, customNERTags);
        nerTrainer.train(corpusPath, nerModelPath);
        log.info("CRF NER，训练模型完成。");
    }

    /**
     * 测试CRF模型
     *
     * @param cwsModelPath
     * @param posModelPath
     * @param nerModelPath
     * @throws IOException
     */
    public void testTrainCrfModel(String cwsModelPath, String posModelPath, String nerModelPath) throws IOException {
        /* 可变参数 */
        if (StringUtils.isEmpty(cwsModelPath)) {
            cwsModelPath = XyyConstants.crfCwsModelPath;
        }
        if (StringUtils.isEmpty(posModelPath)) {
            posModelPath = XyyConstants.crfPosModelPath;
        }
        if (StringUtils.isEmpty(nerModelPath)) {
            nerModelPath = XyyConstants.crfNerModelPath;
        }
//        String cwsModelPath = XyyConstants.crfCwsModelPath;
//        String posModelPath = XyyConstants.crfPosModelPath;
//        String nerModelPath = XyyConstants.crfNerModelPath;

//        HanLP.Config.enableDebug();

        /* CRF-粗分词： */
        CRFLexicalAnalyzer analyzer = new CRFLexicalAnalyzer(cwsModelPath, posModelPath, nerModelPath);
        analyzer.enablePartOfSpeechTagging(true);
        analyzer.enableCustomDictionary(false);
        analyzer.enableNormalization(true);
        /* CRF-粗分词： */
        CRFLexicalAnalyzer indexAnalyzer = new CRFLexicalAnalyzer(cwsModelPath, posModelPath, nerModelPath);
        indexAnalyzer.enablePartOfSpeechTagging(true);
        indexAnalyzer.enableCustomDictionary(false);
        indexAnalyzer.enableNormalization(true);
        indexAnalyzer.enableIndexMode(1);

        // TODO 这里应该使用 验证集 和 测试集 分别对模型进行评估

        String text;

        text = "999 感冒灵颗粒";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "感冒灵";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "强力枇杷露";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "蜜炼雪梨枇杷";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "蜜炼枇杷糖（方形铁盒）";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10天然维生素E软胶囊";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10维生素E软胶囊";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "辅酶Q10";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "辅酶Q10维生素E";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶q10天然维生素E软胶囊";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10天然维生素e软胶囊";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶q10天然维生素e软胶囊";
        log.info("【CRF-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【CRF-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");
    }

    /**
     * 训练感知机模型
     *
     * @param corpusPath
     * @param cwsModelPath
     * @param posModelPath
     * @param nerModelPath
     * @throws IOException
     */
    public void doTrainPerceptronModel(String corpusPath, String cwsModelPath, String posModelPath, String nerModelPath) throws IOException {
        /* 可变参数 */
        if (StringUtils.isEmpty(corpusPath)) {
            corpusPath = XyyConstants.corpusPath;
        }
        if (StringUtils.isEmpty(cwsModelPath)) {
            cwsModelPath = XyyConstants.perceptronCwsModelPath;
        }
        if (StringUtils.isEmpty(posModelPath)) {
            posModelPath = XyyConstants.perceptronPosModelPath;
        }
        if (StringUtils.isEmpty(nerModelPath)) {
            nerModelPath = XyyConstants.perceptronNerModelPath;
        }
//        String corpusPath = XyyConstants.corpusPath;
//        String cwsModelPath = XyyConstants.perceptronCwsModelPath;
//        String posModelPath = XyyConstants.perceptronPosModelPath;
//        String nerModelPath = XyyConstants.perceptronNerModelPath;

        /* 训练并生成模型 */
        // 感知机中文分词
        PerceptronTrainer cwsTrainer = new CWSTrainer();
        PerceptronTrainer.Result perceptronTrainerResult = cwsTrainer.train(corpusPath, cwsModelPath);
        log.info("感知机中文分词，训练模型准确率:{}", perceptronTrainerResult.getAccuracy());
        // 感知机词性标注
        PerceptronTrainer posTrainer = new POSTrainer();
        PerceptronTrainer.Result posTrainerResult = posTrainer.train(corpusPath, posModelPath);
        log.info("感知机词性标注，训练模型准确率:{}", posTrainerResult.getAccuracy());
        // 感知机NER
        PerceptronTrainer nerTrainer = new NERTrainer() {
            @Override
            protected TagSet createTagSet() {
                NERTagSet tagSet = new NERTagSet();
                tagSet.nerLabels.add("nr");
                tagSet.nerLabels.add("ns");
                tagSet.nerLabels.add("nt");
                Arrays.stream(XyyNatureEnum.values()).forEach(xyyNatureEnum -> tagSet.nerLabels.add(xyyNatureEnum.getNature().toString()));
                return tagSet;
            }
        };
        PerceptronTrainer.Result nerTrainerResult = nerTrainer.train(corpusPath, nerModelPath);
        log.info("感知机NER，训练模型准确率:{}", nerTrainerResult.getAccuracy());
    }

    /**
     * 测试感知机模型
     *
     * @param cwsModelPath
     * @param posModelPath
     * @param nerModelPath
     * @throws IOException
     */
    public void testTrainPerceptronModel(String cwsModelPath, String posModelPath, String nerModelPath) throws IOException {
        /* 可变参数 */
        if (StringUtils.isEmpty(cwsModelPath)) {
            cwsModelPath = XyyConstants.perceptronCwsModelPath;
        }
        if (StringUtils.isEmpty(posModelPath)) {
            posModelPath = XyyConstants.perceptronPosModelPath;
        }
        if (StringUtils.isEmpty(nerModelPath)) {
            nerModelPath = XyyConstants.perceptronNerModelPath;
        }
//        String cwsModelPath = XyyConstants.perceptronCwsModelPath;
//        String posModelPath = XyyConstants.perceptronPosModelPath;
//        String nerModelPath = XyyConstants.perceptronNerModelPath;

//        HanLP.Config.enableDebug();

        /* 感知机-粗分词： */
        PerceptronLexicalAnalyzer analyzer = new PerceptronLexicalAnalyzer(cwsModelPath, posModelPath, nerModelPath);
        analyzer.enablePartOfSpeechTagging(true);
        analyzer.enableCustomDictionary(false);
        analyzer.enableNormalization(true);
        /* 感知机-粗分词： */
        PerceptronLexicalAnalyzer indexAnalyzer = new PerceptronLexicalAnalyzer(cwsModelPath, posModelPath, nerModelPath);
        indexAnalyzer.enablePartOfSpeechTagging(true);
        indexAnalyzer.enableCustomDictionary(false);
        indexAnalyzer.enableNormalization(true);
        indexAnalyzer.enableIndexMode(1);

        // TODO 这里应该使用 验证集 和 测试集 分别对模型进行评估

        String text;

        text = "999 感冒灵颗粒";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "感冒灵";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "强力枇杷露";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "蜜炼雪梨枇杷";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "蜜炼枇杷糖（方形铁盒）";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10天然维生素E软胶囊";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10维生素E软胶囊";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "辅酶Q10";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "辅酶Q10维生素E";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶q10天然维生素E软胶囊";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶Q10天然维生素e软胶囊";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");

        text = "汤臣倍健辅酶q10天然维生素e软胶囊";
        log.info("【感知机-粗分词】文本【{}】结果：{}", text, analyzer.analyze(text));
        log.info("【感知机-细分词】文本【{}】结果：{}", text, indexAnalyzer.analyze(text));
        log.info("         ");
    }

    /**
     * 训练HMM-NGram分词模型
     *
     * @param corpusPath
     * @param modelPath
     */
    public void doTrainHmmBigramModel(String corpusPath, String modelPath) {
        /* 可变参数 */
        if (StringUtils.isEmpty(corpusPath)) {
            corpusPath = XyyConstants.corpusPath;
        }
        if (StringUtils.isEmpty(modelPath)) {
            modelPath = XyyConstants.modelPath;
        }
//        String corpusPath = XyyConstants.corpusPath;
//        String modelPath = XyyConstants.modelPath;

        /* 训练并生成模型 */
        final NatureDictionaryMaker dictionaryMaker = new NatureDictionaryMaker();
        CorpusLoader.walk(corpusPath, document -> {
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

    /**
     * 测试HMM-NGram分词模型
     *
     * @param modelPath
     */
    public void testTrainHmmBigramModel(String modelPath) {
        /* 可变参数 */
        if (StringUtils.isEmpty(modelPath)) {
            modelPath = XyyConstants.modelPath;
        }
//        String modelPath = XyyConstants.modelPath;

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

        text = "强力枇杷露";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "蜜炼雪梨枇杷";
        log.info("【HMM-Bigram分词-最短分路-粗分词】文本【{}】结果：{}", text, newSegment.seg(text));
        log.info("【HMM-Bigram分词-最短分路-细分词】文本【{}】结果：{}", text, indexNewSegment.seg(text));
        log.info("         ");

        text = "蜜炼枇杷糖（方形铁盒）";
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

    @Test
    public void generateXyyDrugCwsText() throws IOException {
        /* 可变参数 */
        String corpusExcelPath = XyyConstants.corpusExcelPath;
        String corpusPath = XyyConstants.corpusPath;

        /* 标注制作语料库 */
        List<XyyDrugCorpusRowDTO> xyyDrugCorpusRowDTOS = XyyDrugCorpusExcelOperator.readAllRows(corpusExcelPath);
        // TODO 10个中，8个最为训练集，1个作为验证集，1个作为测试集
        List<String> lines = convertLines(xyyDrugCorpusRowDTOS);
        log.info("语料集，总共{}行", lines.size());
        FileUtils.writeLines(new File(corpusPath), lines);
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
                .append("  /w ")
                .append(xyyDrugCorpusRowDTO.getAnnotationCommonName())
                .toString();
    }

}
