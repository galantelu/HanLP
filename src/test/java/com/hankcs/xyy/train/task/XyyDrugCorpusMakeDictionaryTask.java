package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.DynamicCustomDictionary;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.xyy.train.dto.XyyDrugCorpusDictionaryRowDTO;
import com.hankcs.xyy.train.easyexcel.rows.XyyDrugCorpusDictionaryExcelRow;
import com.hankcs.xyy.train.operators.XyyDrugCorpusDictionaryExcelOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author luyong
 */
@Slf4j
public class XyyDrugCorpusMakeDictionaryTask {

    private static Segment newSegment = HanLP.newSegment("viterbi");

    private static final Pattern brandRegex = Pattern.compile("^[\\(（](.+?)[\\)）]$");

    /**
     * 过滤掉的品牌
     */
    private static final Set<String> excludeBrands = Sets.newHashSet("0", "*", "-", "1");
    /**
     * 特殊词典映射
     */
    private static final Map<String, String> specialBrandMap = Maps.newHashMap();

    /**
     * 过滤掉的展示名称
     */
    private static final Set<String> excludeShowNames = Sets.newHashSet();
    /**
     * 特殊词典映射
     */
    private static final Map<String, String> specialShowNameMap = Maps.newHashMap();

    /**
     * 过滤掉的店铺名称
     */
    private static final Set<String> excludeCorps = Sets.newHashSet();
    /**
     * 特殊词典映射
     */
    private static final Map<String, String> specialCorpMap = Maps.newHashMap();

    static {
        newSegment.enablePartOfSpeechTagging(true);
        newSegment.enableCustomDictionaryForcing(true);
        specialBrandMap.put("（汲盛堂", "汲盛堂");
        specialShowNameMap.put("", "");
        specialCorpMap.put("", "");
    }

    /**
     * 处理词典Excel
     */
    @Test
    public void dealBrandDictionaryExcel() {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的品牌_2024_04_07.xlsx";

        /* 备份 */
        XyyDrugCorpusDictionaryExcelOperator.backup(dictionaryExcelPath);

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("品牌词典Excel没有数据，终止");
            return;
        }
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            rowDTO.setRealDictionary(this.tryParseBrand(rowDTO.getDictionary()));
        }

        // 写Excel
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(rowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(dictionaryExcelPath, excelRows);
        log.debug("处理品牌词典Excel，成功。");
    }

    private String tryParseBrand(String brand) {
        brand = this.replaceNlpSpecialChar(brand);
        if (StringUtils.isEmpty(brand)) {
            return "";
        }
        if (excludeBrands.contains(brand)) {
            return "";
        }
        if (specialBrandMap.containsKey(brand)) {
            return specialBrandMap.get(brand);
        }
        Matcher matcher = brandRegex.matcher(brand);
        boolean isMatch = matcher.find();
        if (!isMatch) {
            return brand;
        } else {
            return matcher.group(1).trim();
        }
    }

    /**
     * 制作词典
     */
    @Test
    public void makeBrandDictionary() throws IOException {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的品牌_2024_04_07.xlsx";
        String dictionaryPath = "data/xyy/dictionary/brand.txt";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("品牌词典Excel没有数据，终止");
            return;
        }

        Set<String> brands = this.listBrands(rowDTOS);
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, brands);
        log.info("制作词典成功：{}", dictionaryPath);
    }

    private Set<String> listBrands(List<XyyDrugCorpusDictionaryRowDTO> rowDTOS) {
        Set<String> brands = Sets.newHashSetWithExpectedSize(rowDTOS.size());
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            if (Objects.nonNull(rowDTO) && StringUtils.isNotEmpty(rowDTO.getRealDictionary())) {
                brands.add(rowDTO.getRealDictionary());
                // 化繁为简、大写转小写，全角转半角。
                String convertDictionary = CharTable.convert(rowDTO.getRealDictionary());
                if (!Objects.equals(rowDTO.getRealDictionary(), convertDictionary)) {
                    brands.add(convertDictionary);
                }
            }
        }
        return brands;
    }

    // ====================================================================================================

    /**
     * 制作词典
     */
    @Test
    public void makeDosageDictionary() throws IOException {
        /* 可变参数 */
        String dictionaryText = "口服液,颗粒,胶囊,片,冲剂,丸,液,粉,泡腾片,口服溶液,混悬剂,滴剂,软胶囊,压片糖果,喷雾剂,乳膏,栓,喷剂,缓释片,合剂,含片,粉雾,剂,吸入剂,搽剂,霜,咀嚼片,乳,鼻气雾剂,干混悬剂,控释片,酒,贴膏,糖浆,注射剂,注射液,口服混悬剂,散,小容量注射剂,滴丸,胶,洗剂,冲洗剂,贴,曲,酊,漱液,眼膏,橡胶膏,搽剂,凝胶,气雾剂,医疗器械,生物制品,糊,滴耳液,溶液,锭剂,洗眼液,眼膏,眼用制剂,透皮贴剂,软膏,滴鼻剂,滴鼻液,露,冻干粉针剂,粉针剂,煎膏,大容量注射剂,口服乳剂,油剂,灌肠剂,含漱液,植入剂,贴,涂剂,膏,涂膜剂,浸膏,流浸膏,鼻用喷雾剂,透皮贴,鼻用滴剂,肠溶胶囊";
        String dictionaryPath = "data/xyy/dictionary/dosage.txt";

        String[] dosageArray = dictionaryText.split(",");
        Set<String> dosages = Sets.newHashSetWithExpectedSize(dosageArray.length);
        for (String dosage : dosageArray) {
            dosage = this.replaceNlpSpecialChar(dosage);
            if (StringUtils.isNotEmpty(dosage)) {
                dosages.add(dosage);
                // 化繁为简、大写转小写，全角转半角。
                String convertDictionary = CharTable.convert(dosage);
                if (!Objects.equals(dosage, convertDictionary)) {
                    dosages.add(convertDictionary);
                }
            }
        }
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, dosages);
        log.info("制作词典成功：{}", dictionaryPath);
    }

    // ====================================================================================================

    /**
     * 处理词典Excel
     */
    @Test
    public void dealShowNameDictionaryExcel() {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的展示名称和通用名称_2024_04_02.xlsx";

        /* 备份 */
        XyyDrugCorpusDictionaryExcelOperator.backup(dictionaryExcelPath);

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("商品名称词典Excel没有数据，终止");
            return;
        }
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            rowDTO.setRealDictionary(this.tryParseShowName(rowDTO.getDictionary()));
        }

        // 写Excel
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(rowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(dictionaryExcelPath, excelRows);
        log.debug("处理商品名词典Excel，成功。");
    }

    private String tryParseShowName(String showName) {
        showName = this.replaceNlpSpecialChar(showName);
        if (StringUtils.isEmpty(showName)) {
            return "";
        }
        if (excludeShowNames.contains(showName)) {
            return "";
        }
        if (specialShowNameMap.containsKey(showName)) {
            return specialShowNameMap.get(showName);
        }
        // TODO 只留下核心词，多个词之间按照空格分隔
        return newSegment.seg(showName).toString();
    }

    /**
     * 制作词典
     */
    @Test
    public void makeShowNameDictionary() throws IOException {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的展示名称和通用名称_2024_04_02.xlsx";
        String brandDictionaryPath = "data/xyy/dictionary/brand.txt";
        String coreDictionaryPath = "data/xyy/dictionary/core.txt";
        String dosageDictionaryPath = "data/xyy/dictionary/dosage.txt";
        String otherDictionaryPath = "data/xyy/dictionary/other.txt";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("商品名词典Excel没有数据，终止");
            return;
        }
        Set<String> corps = this.listCores(rowDTOS);
        File dictionaryFile = new File(coreDictionaryPath);
        FileUtils.writeLines(dictionaryFile, corps);
        log.info("制作词典成功：{}", coreDictionaryPath);
    }

    private Set<String> listCores(List<XyyDrugCorpusDictionaryRowDTO> rowDTOS) {
        Set<String> showNames = Sets.newHashSetWithExpectedSize(rowDTOS.size());
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            if (Objects.nonNull(rowDTO) && StringUtils.isNotEmpty(rowDTO.getRealDictionary())) {
                String[] words = rowDTO.getRealDictionary().split(" ");
                for (String word : words) {
                    showNames.add(word);
                    // 化繁为简、大写转小写，全角转半角。
                    String convertDictionary = CharTable.convert(word);
                    if (!Objects.equals(word, convertDictionary)) {
                        showNames.add(convertDictionary);
                    }
                }
            }
        }
        return showNames;
    }

    // ====================================================================================================

    @Test
    public void testParseCorpPattern() {
        /* 可变参数 */
        String original = "广西广恒医药有限公司";

        // 仅仅使用默认自定义词典，避免领域数据干扰行政区域数据。
        DynamicCustomDictionary dictionary = new DynamicCustomDictionary("data/dictionary/custom/CustomDictionary.txt");
        dictionary.reload();
        newSegment.enableCustomDictionary(dictionary);

        // 加载Excel
        Set<String> areas = Sets.newHashSet(this.tryParseCorpAreas(original));

        List<Pattern> parsePatterns = Lists.newArrayListWithExpectedSize(16);
        Pattern parsePattern;
        parsePattern = this.getParseCorpPattern1(areas);
        if (Objects.nonNull(parsePattern)) {
            parsePatterns.add(parsePattern);
        }
        parsePattern = this.getParseCorpPattern2(areas);
        if (Objects.nonNull(parsePattern)) {
            parsePatterns.add(parsePattern);
        }
        parsePattern = this.getParseCorpPattern3(areas);
        if (Objects.nonNull(parsePattern)) {
            parsePatterns.add(parsePattern);
        }
        parsePattern = this.getParseCorpPattern4(areas);
        if (Objects.nonNull(parsePattern)) {
            parsePatterns.add(parsePattern);
        }
        for (Pattern regex : parsePatterns) {
            Matcher matcher = regex.matcher(original);
            boolean isMatch = matcher.find();
            String real;
            if (!isMatch) {
                real = "";
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                int groupCount = matcher.groupCount();
                log.info("【{}】groupCount：【{}】", original, groupCount);
                for (int i = 1; i <= groupCount; i++) {
                    if (Objects.nonNull(matcher.group(i))) {
                        stringBuilder.append(matcher.group(i));
                    }
                }
                real = stringBuilder.toString();
            }
            log.info("【{}】解析为：【{}】", original, real);
        }

        // 还原自定义词典配置
        new DynamicCustomDictionary().reload();
    }

    private List<String> tryParseCorpAreas(String original) {
        original = this.replaceNlpSpecialChar(original);
        if (StringUtils.isEmpty(original)) {
            return null;
        }
        List<Term> terms = newSegment.seg(original);
//        log.info("分词：{}", terms.toString());
        return terms.stream().map(term -> {
            if (Objects.equals(term.nature, Nature.ns)) {
                return term.word;
            }
            return null;
        }).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    private Pattern getParseCorpPattern1(Set<String> areas) {
        StringBuilder pattern = new StringBuilder();
        pattern.append("^");
        if (CollectionUtils.isNotEmpty(areas)) {
            pattern.append("(");
            pattern.append(String.join("|", areas));
            pattern.append("){0,1}");
        }
        pattern.append("(.+?)(医疗器械|医药物流|医药工业|保健品|保健食品|健康管理|贸易|自营|集团|医药科技|生物科技|医药药材|医药|药业|科技){0,1}(?:有限公司|旗舰店|有限责任公司){0,1}$");
        log.info("解析店铺名称&厂商名称的正则表达式：{}", pattern);
        return Pattern.compile(pattern.toString());
    }

    private Pattern getParseCorpPattern2(Set<String> areas) {
        StringBuilder pattern = new StringBuilder();
        pattern.append("^");
        if (CollectionUtils.isNotEmpty(areas)) {
            pattern.append("(");
            pattern.append(String.join("|", areas));
            pattern.append("){0,1}");
        }
        pattern.append("(.+?)(?:医疗器械|医药物流|医药工业|保健品|保健食品|健康管理|贸易|自营|集团|医药科技|生物科技|医药药材|医药|药业|科技){0,1}(?:有限公司|旗舰店|有限责任公司){0,1}$");
        log.info("解析店铺名称&厂商名称的正则表达式：{}", pattern);
        return Pattern.compile(pattern.toString());
    }

    private Pattern getParseCorpPattern3(Set<String> areas) {
        StringBuilder pattern = new StringBuilder();
        pattern.append("^");
        if (CollectionUtils.isNotEmpty(areas)) {
            pattern.append("(?:");
            pattern.append(String.join("|", areas));
            pattern.append("){0,1}");
        }
        pattern.append("(.+?)(?:医疗器械|医药物流|医药工业|保健品|保健食品|健康管理|贸易|自营|集团|医药科技|生物科技|医药药材|医药|药业|科技){0,1}(?:有限公司|旗舰店|有限责任公司){0,1}$");
        log.info("解析店铺名称&厂商名称的正则表达式：{}", pattern);
        return Pattern.compile(pattern.toString());
    }

    private Pattern getParseCorpPattern4(Set<String> areas) {
        StringBuilder pattern = new StringBuilder();
        pattern.append("^");
        if (CollectionUtils.isNotEmpty(areas)) {
            pattern.append("(?:");
            pattern.append(String.join("|", areas));
            pattern.append("){0,1}");
        }
        pattern.append("(.+?)(医疗器械|医药物流|医药工业|保健品|保健食品|健康管理|贸易|自营|集团|医药科技|生物科技|医药药材|医药|药业|科技){0,1}(有限公司|旗舰店|有限责任公司){0,1}$");
        log.info("解析店铺名称&厂商名称的正则表达式：{}", pattern);
        return Pattern.compile(pattern.toString());
    }

    /**
     * 处理词典Excel
     */
    @Test
    public void dealCorpDictionaryExcel() {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的店铺展示名称&厂商_2024_04_02.xlsx";

        // 仅仅使用默认自定义词典，避免领域数据干扰行政区域数据。
        DynamicCustomDictionary dictionary = new DynamicCustomDictionary("data/dictionary/custom/CustomDictionary.txt");
        dictionary.reload();
        newSegment.enableCustomDictionary(dictionary);

        /* 备份 */
        XyyDrugCorpusDictionaryExcelOperator.backup(dictionaryExcelPath);

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("店铺名称&厂商名称词典Excel没有数据，终止");
            return;
        }

        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            rowDTO.setRealDictionary(this.tryParseCorp(rowDTO.getDictionary()));
        }

        // 写Excel
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(rowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(dictionaryExcelPath, excelRows);
        log.debug("处理店铺名称&厂商名称词典Excel，成功。");

        // 还原自定义词典配置
        new DynamicCustomDictionary().reload();
    }

    private List<Pattern> tryGetParsePatterns(String original) {
        List<Pattern> parsePatterns = Lists.newArrayListWithExpectedSize(16);
        Set<String> areas = Sets.newHashSet(this.tryParseCorpAreas(original));
        Pattern parsePattern;
        parsePattern = this.getParseCorpPattern1(areas);
        if (Objects.nonNull(parsePattern)) {
            parsePatterns.add(parsePattern);
        }
        parsePattern = this.getParseCorpPattern2(areas);
        if (Objects.nonNull(parsePattern)) {
            parsePatterns.add(parsePattern);
        }
        parsePattern = this.getParseCorpPattern3(areas);
        if (Objects.nonNull(parsePattern)) {
            parsePatterns.add(parsePattern);
        }
        parsePattern = this.getParseCorpPattern4(areas);
        if (Objects.nonNull(parsePattern)) {
            parsePatterns.add(parsePattern);
        }
        return parsePatterns;
    }

    private String tryParseCorp(String original) {
        original = this.replaceNlpSpecialChar(original);
        if (StringUtils.isEmpty(original)) {
            return "";
        }
        if (excludeCorps.contains(original)) {
            return "";
        }
        if (specialCorpMap.containsKey(original)) {
            return specialCorpMap.get(original);
        }
        List<Pattern> parsePatterns = this.tryGetParsePatterns(original);
        if (CollectionUtils.isEmpty(parsePatterns)) {
            return "";
        }
        List<String> corps = Lists.newArrayListWithExpectedSize(16);
        // 全称
        corps.add(original);
        for (Pattern parsePattern : parsePatterns) {
            Matcher matcher = parsePattern.matcher(original);
            boolean isMatch = matcher.find();
            if (isMatch) {
                int groupCount = matcher.groupCount();
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i <= groupCount; i++) {
                    String group = matcher.group(i);
                    if (Objects.nonNull(group)) {
                        stringBuilder.append(group);
                    }
                }
                corps.add(stringBuilder.toString());
            }
        }
        return String.join(" ", corps.stream().distinct().collect(Collectors.toList()));
    }

    /**
     * 制作词典
     */
    @Test
    public void makeCorpDictionary() throws IOException {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的店铺展示名称&厂商_2024_04_02.xlsx";
        String dictionaryPath = "data/xyy/dictionary/corp.txt";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("品牌词典Excel没有数据，终止");
            return;
        }

        Set<String> corps = this.listCorps(rowDTOS);
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, corps);
        log.info("制作词典成功：{}", dictionaryPath);
    }

    private Set<String> listCorps(List<XyyDrugCorpusDictionaryRowDTO> rowDTOS) {
        Set<String> corps = Sets.newHashSetWithExpectedSize(rowDTOS.size());
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            if (Objects.nonNull(rowDTO) && StringUtils.isNotEmpty(rowDTO.getRealDictionary())) {
                String[] words = rowDTO.getRealDictionary().split(" ");
                for (String word : words) {
                    corps.add(word);
                    // 化繁为简、大写转小写，全角转半角。
                    String convertDictionary = CharTable.convert(word);
                    if (!Objects.equals(word, convertDictionary)) {
                        corps.add(convertDictionary);
                    }
                }
            }
        }
        return corps;
    }

    private String replaceNlpSpecialChar(String original) {
        if (StringUtils.isEmpty(original)) {
            return "";
        }
        return original.replaceAll("/|\\s|\\[|\\]|,|#|@", "");
    }
}
