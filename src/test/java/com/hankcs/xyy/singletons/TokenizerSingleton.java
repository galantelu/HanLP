package com.hankcs.xyy.singletons;

import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.io.IOException;

public class TokenizerSingleton {

    private static final StandardTokenizer GLOBAL_STANDARD_TOKENIZER = new StandardTokenizer();

    private static final PerceptronLexicalAnalyzer GLOBAL_PERCEPTRON_LEXICAL_ANALYZER;

    private static final CRFLexicalAnalyzer GLOBAL_CRF_LEXICAL_ANALYZER;

    static {
        StandardTokenizer.SEGMENT.enablePartOfSpeechTagging(true);
        try {
            GLOBAL_PERCEPTRON_LEXICAL_ANALYZER = new PerceptronLexicalAnalyzer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            GLOBAL_CRF_LEXICAL_ANALYZER = new CRFLexicalAnalyzer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static StandardTokenizer getGlobalStandardTokenizer() {
        return GLOBAL_STANDARD_TOKENIZER;
    }

    public static PerceptronLexicalAnalyzer getGlobalPerceptronLexicalAnalyzer() {
        return GLOBAL_PERCEPTRON_LEXICAL_ANALYZER;
    }

    public static CRFLexicalAnalyzer getGlobalCRFLexicalAnalyzer() {
        return GLOBAL_CRF_LEXICAL_ANALYZER;
    }

}
