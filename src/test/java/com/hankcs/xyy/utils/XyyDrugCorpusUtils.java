package com.hankcs.xyy.utils;

import org.apache.commons.lang3.StringUtils;

public class XyyDrugCorpusUtils {

    public static String replaceSpecialChar(String original) {
        if (StringUtils.isEmpty(original)) {
            return "";
        }
        return original.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9.*.]+", "");
    }

}
