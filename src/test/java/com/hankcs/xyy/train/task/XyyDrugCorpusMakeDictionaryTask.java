package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.DynamicCustomDictionary;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.xyy.train.dto.XyyDrugCorpusDictionaryRowDTO;
import com.hankcs.xyy.train.easyexcel.rows.XyyDrugCorpusDictionaryExcelRow;
import com.hankcs.xyy.train.enums.XyyNatureEnum;
import com.hankcs.xyy.train.operators.XyyDrugCorpusDictionaryExcelOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author luyong
 */
@Slf4j
public class XyyDrugCorpusMakeDictionaryTask {

    private static Segment newSegment = HanLP.newSegment("viterbi");

    private static Segment newIndexSegment = HanLP.newSegment("viterbi");

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

    private static final Set<String> removeCorpPrefixSet = Sets.newHashSet();

    private static final Pattern parseSpecialCorpYuanPattern = Pattern.compile("^(.*?)原(.*)$");

    private static final Pattern parseSpecialCorpWeiTuoPattern = Pattern.compile("^(.*?)委托(.*)$");

    static {
        newSegment.enablePartOfSpeechTagging(true);
        newSegment.enableCustomDictionaryForcing(true);

        newIndexSegment.enablePartOfSpeechTagging(true);
        newIndexSegment.enableCustomDictionaryForcing(true);
        newIndexSegment.enableIndexMode(1);

        specialBrandMap.put("（汲盛堂", "汲盛堂");
        specialShowNameMap.put("", "");
        specialCorpMap.put("", "");
        removeCorpPrefixSet.add("4月3日1700截单");
        removeCorpPrefixSet.add("4月3日16时截单4月7日发货");
        removeCorpPrefixSet.add("4月3日1500截单");
        removeCorpPrefixSet.add("4月3日1730截单");
        removeCorpPrefixSet.add("4月3日1600截单");
        removeCorpPrefixSet.add("4月3日16时截单6日7时发货");
        removeCorpPrefixSet.add("4月2日2000截单");
        removeCorpPrefixSet.add("4月3日1600截单");
        removeCorpPrefixSet.add("清明不打烊");

    }

    @Test
    public void checkDictionaryDifference() throws IOException {
        /* 可变参数 */
        String brandDictionaryPath = "data/xyy/dictionary/brand.original.txt";
        String corpDictionaryPath = "data/xyy/dictionary/corp.original.txt";
        String specDictionaryPath = "data/xyy/dictionary/spec.original.txt";

        String dosageDictionaryPath = "data/xyy/dictionary/dosage.original.txt";

        Set<String> brands = Sets.newHashSet(FileUtils.readLines(new File(brandDictionaryPath), "UTF-8"));
        brands.remove("");

        Set<String> corps = Sets.newHashSet(FileUtils.readLines(new File(corpDictionaryPath), "UTF-8"));
        corps.remove("");

        Set<String> specs = Sets.newHashSet(FileUtils.readLines(new File(specDictionaryPath), "UTF-8"));
        specs.remove("");

        Set<String> dosages = Sets.newHashSet(FileUtils.readLines(new File(dosageDictionaryPath), "UTF-8"));
        specs.remove("");

        //
//        Sets.intersection(brands, corps).stream().forEach(System.out::println);
//        Sets.intersection(brands, specs).stream().forEach(System.out::println);
//        Sets.intersection(brands, dosages).stream().forEach(System.out::println);
//
//
        Sets.intersection(corps, brands).stream().forEach(System.out::println);
        Sets.intersection(corps, specs).stream().forEach(System.out::println);
        Sets.intersection(corps, dosages).stream().forEach(System.out::println);


//        Sets.intersection(specs, brands).stream().forEach(System.out::println);
//        Sets.intersection(specs, corps).stream().forEach(System.out::println);
//        Sets.intersection(specs, dosages).stream().forEach(System.out::println);

    }

    // =============================================================================================================

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
        brand = this.replaceSpecialChar(brand);
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
        String dictionaryPath = "data/xyy/dictionary/brand.original.txt";

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
            }
        }
        return brands;
    }

    @Test
    public void dealBrandDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/brand.original.txt";
        String dictionaryPath = "data/xyy/dictionary/brand.txt";

        // 加载brand词典
        List<String> originalBrands = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultBrands = Lists.newArrayListWithExpectedSize(originalBrands.size());
        for (String originalBrand : originalBrands) {
            originalBrand = replaceSpecialChar(originalBrand);
            if (StringUtils.isNotEmpty(originalBrand)) {
                resultBrands.add(originalBrand);
                // 化繁为简、大写转小写，全角转半角。
                String convertDictionary = CharTable.convert(originalBrand);
                if (!Objects.equals(originalBrand, convertDictionary)) {
                    resultBrands.add(convertDictionary);
                }
            }
        }
        // 排序
        resultBrands = resultBrands.stream()
//                .sorted(String::compareTo)
                .distinct().collect(Collectors.toList());
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, resultBrands);
    }

    @Test
    public void checkBrandDictionary() throws IOException {
        new DynamicCustomDictionary().reload();

        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的展示名称和通用名称_2024_04_02.xlsx";

        /* 可变参数 */
        String brandDictionaryPath = "data/xyy/dictionary/brand.original.txt";
        String corpDictionaryPath = "data/xyy/dictionary/corp.original.txt";

        Set<String> corps = Sets.newHashSet(FileUtils.readLines(new File(corpDictionaryPath), "UTF-8"));
        // 加载brand词典
        List<String> brands = FileUtils.readLines(new File(brandDictionaryPath), "UTF-8");

        Set<String> brandSet = brands.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        Set<String> corpSet = corps.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toSet());

        Set<String> duplicateWords = Sets.newHashSet();
        for (String brand : brands) {
            if (StringUtils.isNotEmpty(brand) && corps.contains(brand)) {
                duplicateWords.add(brand);
            }
        }

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("词典Excel没有数据，终止");
            return;
        }
        Set<String> topQueryKeywords = Sets.newHashSet("感冒灵");
//        Set<String> topQueryKeywords = Sets.newHashSet("感冒灵", "枇杷", "阿莫西林");
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("词典没有数据，终止");
            return;
        }
        Set<String> natureSet = Arrays.stream(XyyNatureEnum.values()).map(XyyNatureEnum::toString).collect(Collectors.toSet());

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            for (String topQueryKeyword : topQueryKeywords) {
                rowDTO.setDictionary(replaceSpecialChar(rowDTO.getDictionary()));
                if (rowDTO.getDictionary().contains(topQueryKeyword)) {
                    List<Term> terms = newSegment.seg(rowDTO.getDictionary());
                    List<String> termStrList = terms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                        return term.word + "/" + term.nature.toString();
                    }).collect(Collectors.toList());
                    rowDTO.setRealDictionary(String.join(" ", termStrList));

                    List<Term> indexTerms = newIndexSegment.seg(rowDTO.getDictionary());
                    List<String> indexTermStrList = indexTerms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                        return term.word + "/" + term.nature.toString();
                    }).collect(Collectors.toList());
                    rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
                    resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
                }
            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的展示名称和通用名称_2024_04_02_result.xlsx";
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(resultRowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(resultDictionaryExcelPath, excelRows);
        log.debug("处理词典Excel，成功。");
    }
    // ====================================================================================================

    /**
     * 制作词典
     */
    @Test
    public void makeDosageDictionary() throws IOException {
        /* 可变参数 */
        String dictionaryText = "片剂,普通片,片,分散片,咀嚼片,肠溶片,缓释片,控释片,口腔崩解片,胶囊,硬胶囊,软胶囊,肠溶胶囊,肠溶软胶囊,缓释胶囊,控释胶囊,颗粒,缓释颗粒,控释颗粒,混悬液,干混悬剂,口服溶液剂,口服溶液,合剂,口服液,糖浆剂,散剂,粉剂,滴丸剂,滴丸,丸剂,丸,酊剂,煎膏剂,煎膏,膏滋,膏滋,酒剂,注射液,注射用无菌粉末,冻干粉针剂,软膏剂,软膏,乳膏剂,乳膏,凝胶剂,凝胶,外用溶液剂,外用溶液,胶浆剂,胶浆,贴膏剂,贴膏,橡胶膏剂,橡胶膏,膏药,酊剂,洗剂,涂剂,散剂,冻干粉,气雾剂,雾化溶液剂,雾化溶液,吸入溶液剂,吸入溶液,吸入粉雾剂,喷雾剂,鼻喷雾剂,灌肠剂,滴眼剂,眼膏剂,滴剂,滴鼻剂,滴耳剂,栓剂,阴道片,阴道泡腾片,阴道软胶囊";
        String dictionaryPath = "data/xyy/dictionary/dosage.original.txt";

        String[] dosageArray = dictionaryText.split(",");
        Set<String> dosages = Sets.newHashSetWithExpectedSize(dosageArray.length);
        for (String dosage : dosageArray) {
            dosage = this.replaceSpecialChar(dosage);
            if (StringUtils.isNotEmpty(dosage)) {
                dosages.add(dosage);
            }
        }
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, dosages);
        log.info("制作词典成功：{}", dictionaryPath);
    }

    @Test
    public void dealDosageDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/dosage.original.txt";
        String dictionaryPath = "data/xyy/dictionary/dosage.txt";

        // 加载词典
        List<String> originalDosages = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultDosages = Lists.newArrayListWithExpectedSize(originalDosages.size());
        for (String originalDosage : originalDosages) {
            originalDosage = replaceSpecialChar(originalDosage);
            if (StringUtils.isNotEmpty(originalDosage)) {
                resultDosages.add(originalDosage);
                // 化繁为简、大写转小写，全角转半角。
                String convertDictionary = CharTable.convert(originalDosage);
                if (!Objects.equals(originalDosage, convertDictionary)) {
                    resultDosages.add(convertDictionary);
                }
            }
        }
        // 排序
        resultDosages = resultDosages.stream()
//                .sorted(String::compareTo)
                .distinct().collect(Collectors.toList());
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, resultDosages);
    }
// ====================================================================================================
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

    private String tryParseCorp(String original) {
        original = this.replaceSpecialChar(original);
        if (StringUtils.isEmpty(original)) {
            return "";
        }
        if (excludeCorps.contains(original)) {
            return "";
        }
        if (specialCorpMap.containsKey(original)) {
            return specialCorpMap.get(original);
        }
        for (String replaceCorpPrefix : removeCorpPrefixSet) {
            if (original.startsWith(replaceCorpPrefix)) {
                original = original.replace(replaceCorpPrefix, "");
            }
        }
        // TODO 处理末尾带有.. 或 .

        if (StringUtils.isEmpty(original)) {
            return "";
        }
        // 判断是否是 原
        List<String> corps = Lists.newArrayListWithExpectedSize(16);
        Pattern parsePattern = Pattern.compile("^(.*?)有限公司原公司名(.*)$");
        Matcher matcher = parsePattern.matcher(original);
        boolean isMatch = matcher.find();
        if (isMatch) {
            // 全称
            corps.add(original);
            // 第一个
            tryParseSimpleCorp(matcher.group(1)+"有限公司").forEach(item -> corps.add(item));
            // 第二个
            tryParseSimpleCorp(matcher.group(2)).forEach(item -> corps.add(item));
        } else {
            parsePattern = Pattern.compile("^(.*?)有限公司原名(.*)$");
            matcher = parsePattern.matcher(original);
            isMatch = matcher.find();
            if (isMatch) {
                // 全称
                corps.add(original);
                // 第一个
                tryParseSimpleCorp(matcher.group(1)+"有限公司").forEach(item -> corps.add(item));
                // 第二个
                tryParseSimpleCorp(matcher.group(2)).forEach(item -> corps.add(item));
            } else {
                parsePattern = Pattern.compile("^(.*?)有限公司原(.*)$");
                matcher = parsePattern.matcher(original);
                isMatch = matcher.find();
                if (isMatch) {
                    // 全称
                    corps.add(original);
                    // 第一个
                    tryParseSimpleCorp(matcher.group(1)+"有限公司").forEach(item -> corps.add(item));
                    // 第二个
                    tryParseSimpleCorp(matcher.group(2)).forEach(item -> corps.add(item));
                } else {
                    parsePattern = Pattern.compile("^(.*?)有限责任公司原公司名(.*)$");
                    matcher = parsePattern.matcher(original);
                    isMatch = matcher.find();
                    if (isMatch) {
                        // 全称
                        corps.add(original);
                        // 第一个
                        tryParseSimpleCorp(matcher.group(1)+"有限责任公司").forEach(item -> corps.add(item));
                        // 第二个
                        tryParseSimpleCorp(matcher.group(2)).forEach(item -> corps.add(item));
                    } else {
                        parsePattern = Pattern.compile("^(.*?)有限责任公司原名(.*)$");
                        matcher = parsePattern.matcher(original);
                        isMatch = matcher.find();
                        if (isMatch) {
                            // 全称
                            corps.add(original);
                            // 第一个
                            tryParseSimpleCorp(matcher.group(1)+"有限责任公司").forEach(item -> corps.add(item));
                            // 第二个
                            tryParseSimpleCorp(matcher.group(2)).forEach(item -> corps.add(item));
                        } else {
                            parsePattern = Pattern.compile("^(.*?)有限责任公司原(.*)$");
                            matcher = parsePattern.matcher(original);
                            isMatch = matcher.find();
                            if (isMatch) {
                                // 全称
                                corps.add(original);
                                // 第一个
                                tryParseSimpleCorp(matcher.group(1)+"有限责任公司").forEach(item -> corps.add(item));
                                // 第二个
                                tryParseSimpleCorp(matcher.group(2)).forEach(item -> corps.add(item));
                            } else {
                                // 委托
                                parsePattern = Pattern.compile("^(.*?)有限公司委托(.*)$");
                                matcher = parsePattern.matcher(original);
                                isMatch = matcher.find();
                                if (isMatch) {
                                    // 全称
                                    corps.add(original);
                                    // 第一个
                                    tryParseSimpleCorp(matcher.group(1)+"有限公司").forEach(item -> corps.add(item));
                                    // 第二个
                                    tryParseSimpleCorp(matcher.group(2)).forEach(item -> corps.add(item));
                                } else {
                                    // 委托
                                    parsePattern = Pattern.compile("^(.*?)有限责任公司委托(.*)$");
                                    matcher = parsePattern.matcher(original);
                                    isMatch = matcher.find();
                                    if (isMatch) {
                                        // 全称
                                        corps.add(original);
                                        // 第一个
                                        tryParseSimpleCorp(matcher.group(1)+"有限责任公司").forEach(item -> corps.add(item));
                                        // 第二个
                                        tryParseSimpleCorp(matcher.group(2)).forEach(item -> corps.add(item));
                                    } else {
                                        tryParseSimpleCorp(original).forEach(item -> corps.add(item));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return String.join(" ", corps.stream().distinct().collect(Collectors.toList()));
    }

    private List<String> tryParseSimpleCorp(String original) {
        if (StringUtils.isEmpty(original)) {
            return Lists.newArrayList();
        }
        List<Pattern> parsePatterns = this.tryGetParsePatterns(original);
        if (CollectionUtils.isEmpty(parsePatterns)) {
            return Lists.newArrayList();
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
        return corps;
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

    private List<String> tryParseCorpAreas(String original) {
        original = this.replaceSpecialChar(original);
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
        pattern.append("(.+?)(医疗器械|医药物流|医药工业|保健品|保健食品|健康管理|贸易|自营|集团|医药科技|生物科技|医药药材|医药|药业|科技){0,1}(?:有限公司|旗舰店|有限责任公司|股份有限公司){0,1}$");
//        log.info("解析店铺名称&厂商名称的正则表达式：{}", pattern);
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
        pattern.append("(.+?)(?:医疗器械|医药物流|医药工业|保健品|保健食品|健康管理|贸易|自营|集团|医药科技|生物科技|医药药材|医药|药业|科技){0,1}(?:有限公司|旗舰店|有限责任公司|股份有限公司){0,1}$");
//        log.info("解析店铺名称&厂商名称的正则表达式：{}", pattern);
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
        pattern.append("(.+?)(医疗器械|医药物流|医药工业|保健品|保健食品|健康管理|贸易|自营|集团|医药科技|生物科技|医药药材|医药|药业|科技){0,1}(?:有限公司|旗舰店|有限责任公司|股份有限公司){0,1}$");
//        log.info("解析店铺名称&厂商名称的正则表达式：{}", pattern);
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
        pattern.append("(.+?)(医疗器械|医药物流|医药工业|保健品|保健食品|健康管理|贸易|自营|集团|医药科技|生物科技|医药药材|医药|药业|科技){0,1}(有限公司|旗舰店|有限责任公司|股份有限公司){0,1}$");
//        log.info("解析店铺名称&厂商名称的正则表达式：{}", pattern);
        return Pattern.compile(pattern.toString());
    }

    /**
     * 制作词典
     */
    @Test
    public void makeCorpDictionary() throws IOException {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的店铺展示名称&厂商_2024_04_02.xlsx";
        String dictionaryPath = "data/xyy/dictionary/corp.original.txt";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("品牌词典Excel没有数据，终止");
            return;
        }

        List<String> corps = this.listCorps(rowDTOS);

        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, corps);
        log.info("制作词典成功：{}", dictionaryPath);
    }

    private List<String> listCorps(List<XyyDrugCorpusDictionaryRowDTO> rowDTOS) {
        List<String> corps = Lists.newArrayListWithExpectedSize(rowDTOS.size());
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            if (Objects.nonNull(rowDTO) && StringUtils.isNotEmpty(rowDTO.getRealDictionary())) {
                String[] words = rowDTO.getRealDictionary().split(" ");
                for (String word : words) {
                    corps.add(word);
                }
                corps.add("\n");
            }
        }
        return corps;
    }

    @Test
    public void testParseSpecialCorp() {
        /* 可变参数 */
        List<String> originalCorps = Lists.newArrayList("上海名流卫生用品股份有限公司原上海名邦橡胶制品有限公司","上海名流卫生用品股份有限公司原上海名邦橡胶制品有限公司","","","","","","稳健平安医疗科技湖南有限公司原湖南平安医械科技有限公司","稳健平安医疗科技湖南有限公司原湖南平安医械科技有限公司","","","四川科瑞德制药股份有限公司原四川科瑞德制药有限公司","四川科瑞德制药股份有限公司原四川科瑞德制药有限公司","","","","","","","","","","辽宁海一制药有限公司原辽宁亿邦制药有限公司","辽宁海一制药有限公司原辽宁亿邦制药有限公司","","","","石药控股集团河北唐威药业有限公司原石药集团河北唐威药业有限公司","石药控股集团河北唐威药业有限公司原石药集团河北唐威药业有限公司","","","","","河北唐威药业有限公司原石药控股集团河北唐威药业有限公司","河北唐威药业有限公司原石药控股集团河北唐威药业有限公司","","","","","","浙江国光生物制药股份有限公司原浙江国光生物制药有限公司","浙江国光生物制药股份有限公司原浙江国光生物制药有限公司","","","","","","四川峨嵋山药业有限公司原四川峨嵋山药业股份有限公司","四川峨嵋山药业有限公司原四川峨嵋山药业股份有限公司","","","","","","武汉璟泓科技股份有限公司原武汉璟泓万方堂医药科技股份有限公司","武汉璟泓科技股份有限公司原武汉璟泓万方堂医药科技股份有限公司","","","","","回春堂药业股份有限公司原湖南省回春堂药业有限公司","回春堂药业股份有限公司原湖南省回春堂药业有限公司","","","","","","欧化药业香港有限公司原欧化药业有限公司","欧化药业香港有限公司原欧化药业有限公司","","","","","扬子江药业集团江苏龙凤堂中药有限公司原扬子江药业集团有限公司","扬子江药业集团江苏龙凤堂中药有限公司原扬子江药业集团有限公司","","","","","","广东葛仙堂健康股份有限公司原博罗罗浮山双梅爽保健食品有限公司","广东葛仙堂健康股份有限公司原博罗罗浮山双梅爽保健食品有限公司","","","","","","江西瑞博保健食品有限公司原江西瑞博食品有限公司","江西瑞博保健食品有限公司原江西瑞博食品有限公司","","","","","","衡水恒康医疗器材有限责任公司原冀州市恒康医疗器材有限责任公司","衡水恒康医疗器材有限责任公司原冀州市恒康医疗器材有限责任公司","","","","","","","武汉时珍要方医药科技有限公司原武汉时珍要方卫生品有限公司","武汉时珍要方医药科技有限公司原武汉时珍要方卫生品有限公司","","","","","四川宝鉴堂药业有限公司原四川升和药业股份有限公司","四川宝鉴堂药业有限公司原四川升和药业股份有限公司","","","","安徽三九全康药业有限公司原太和县三九全康生物科技有限公司","安徽三九全康药业有限公司原太和县三九全康生物科技有限公司","","","","","江西沃华济顺医药有限公司原南昌济顺制药有限公司","江西沃华济顺医药有限公司原南昌济顺制药有限公司","","","","江西苗仁堂生物科技有限公司原永丰苗仁堂生物科技有限公司","江西苗仁堂生物科技有限公司原永丰苗仁堂生物科技有限公司","","","","广东国源国药制药有限公司原深圳国源国药有限公司","广东国源国药制药有限公司原深圳国源国药有限公司","","","","江西齐仁堂中药饮片有限公司原江西樟树葛玄药饮片有限公司","江西齐仁堂中药饮片有限公司原江西樟树葛玄药饮片有限公司","","","","","西班牙SiegfriedBarberaS.L.原西班牙NovartisFarmaceuticaS.A.","西班牙siegfriedbarberas.l.原西班牙novartisfarmaceuticas.a.","","","","","","榆林利君制药有限公司原陕西德福康制药有限公司","榆林利君制药有限公司原陕西德福康制药有限公司","","","山东卓健医疗科技股份有限公司原山东卓健医疗科技有限公司","山东卓健医疗科技股份有限公司原山东卓健医疗科技有限公司","","","上海迪赛诺医药集团股份有限公司原上海迪赛诺生物医药有限公司","上海迪赛诺医药集团股份有限公司原上海迪赛诺生物医药有限公司","","","上海自然堂集团有限公司原伽蓝集团股份有限公司","上海自然堂集团有限公司原伽蓝集团股份有限公司","","","","","天士力医药集团股份有限公司原天士力制药集团股份有限公司","天士力医药集团股份有限公司原天士力制药集团股份有限公司","","","","洛阳安普生物科技股份有限公司原洛阳市安普生物科技有限公司","洛阳安普生物科技股份有限公司原洛阳市安普生物科技有限公司","","","","津药和平天津制药有限公司原天津金耀药业有限公司","津药和平天津制药有限公司原天津金耀药业有限公司","","","","安徽盛健生物科技有限公司原厂家亳州市盛健生物科技有限公司","安徽盛健生物科技有限公司原厂家亳州市盛健生物科技有限公司","","","广州花城药业有限公司原广州市花城制药厂","广州花城药业有限公司原广州市花城制药厂","","","仁和堂药业有限公司原名山东仁和堂药业有限公司","仁和堂药业有限公司原名山东仁和堂药业有限公司","","","石家庄北方药业集团有限公司原石家庄北方药业有限公司","石家庄北方药业集团有限公司原石家庄北方药业有限公司","","","","兆科药业广州有限公司原兆科药业合肥有限公司","兆科药业广州有限公司原兆科药业合肥有限公司","","","","山西国泰中药股份有限公司原山西国泰中药饮片有限公司","山西国泰中药股份有限公司原山西国泰中药饮片有限公司","","","黑龙江诺捷制药有限责任公司原哈药集团三精制药诺捷有限责任公司","黑龙江诺捷制药有限责任公司原哈药集团三精制药诺捷有限责任公司","","","天士力医药集团股份有限公司原天士力制药集团股份有限公司","天士力医药集团股份有限公司原天士力制药集团股份有限公司","","","","","深圳大佛药业股份有限公司原深圳大佛药业有限公司","深圳大佛药业股份有限公司原深圳大佛药业有限公司","","","","广西壮族自治区花红药业集团股份公司原广西壮族自治区花红药业股份有限公司","广西壮族自治区花红药业集团股份公司原广西壮族自治区花红药业股份有限公司","","","","齐鲁安替制药有限公司原齐鲁制药有限公司","齐鲁安替制药有限公司原齐鲁制药有限公司","","","四川省通园制药集团有限公司原四川省通园制药有限公司","四川省通园制药集团有限公司原四川省通园制药有限公司","","","北京长城制药有限公司原北京长城制药厂","北京长城制药有限公司原北京长城制药厂","","","一力制药罗定有限公司原广东一力罗定制药有限公司","一力制药罗定有限公司原广东一力罗定制药有限公司","","","仁和堂药业有限公司原山东仁和堂药业有限公司","仁和堂药业有限公司原山东仁和堂药业有限公司","","","黑龙江比福金北药制药有限公司原伊春金北药制药有限公司","黑龙江比福金北药制药有限公司原伊春金北药制药有限公司","","","广州白云山医药集团股份有限公司白云山制药总厂原名广州白云山制药股份有限公司广州白云山制药总厂","广州白云山医药集团股份有限公司白云山制药总厂原名广州白云山制药股份有限公司广州白云山制药总厂","","","","沈阳清宫药业集团有限公司原沈阳康达制药集团有限公司","沈阳清宫药业集团有限公司原沈阳康达制药集团有限公司","","","石家庄北方药业集团有限公司原石家庄北方药业有限公司","石家庄北方药业集团有限公司原石家庄北方药业有限公司","","","","仙乐健康科技股份有限公司原广东仙乐制药有限公司","仙乐健康科技股份有限公司原广东仙乐制药有限公司","","","广西厚德药业有限公司原广西厚德大健康产业股份有限公司","广西厚德药业有限公司原广西厚德大健康产业股份有限公司","","","","葵花药业集团襄阳隆中有限公司原湖北襄阳隆中药业集团有限公司","葵花药业集团襄阳隆中有限公司原湖北襄阳隆中药业集团有限公司","","","","兰州和盛堂制药股份有限公司原兰州和盛堂制药有限公司","兰州和盛堂制药股份有限公司原兰州和盛堂制药有限公司","","","成都华神科技集团股份有限公司制药厂原成都泰合健康科技集团股份有限公司华神制药厂","成都华神科技集团股份有限公司制药厂原成都泰合健康科技集团股份有限公司华神制药厂","","","江西南昌桑海制药有限责任公司原江西南昌桑海制药厂","江西南昌桑海制药有限责任公司原江西南昌桑海制药厂","","","","广西吉民堂药业有限公司原广西金诺制药有限公司","广西吉民堂药业有限公司原广西金诺制药有限公司","","","","镇平时通实业有限公司原镇平时通药业有限公司","镇平时通实业有限公司原镇平时通药业有限公司","","","","江西南昌济生制药有限责任公司原江西南昌桑海制药有限责任公司","江西南昌济生制药有限责任公司原江西南昌桑海制药有限责任公司","","","健适宝上海实业有限公司原耀信电子科技上海有限公司","健适宝上海实业有限公司原耀信电子科技上海有限公司","","","","山东罗欣药业集团股份有限公司原山东罗欣药业股份有限公司","山东罗欣药业集团股份有限公司原山东罗欣药业股份有限公司","","","广西迪泰制药股份有限公司原广西迪泰制药有限公司","广西迪泰制药股份有限公司原广西迪泰制药有限公司","","","哈尔滨北星药业有限公司原哈尔滨三联药业股份有限公司","哈尔滨北星药业有限公司原哈尔滨三联药业股份有限公司","","","安徽方达药械股份有限公司原安徽方达药械有限公司","安徽方达药械股份有限公司原安徽方达药械有限公司","","","","上海上药新亚药业有限公司原上海新亚药业有限公司","上海上药新亚药业有限公司原上海新亚药业有限公司","","","","吉林隆泰制药有限责任公司原吉林隆泰制药股份有限公司","吉林隆泰制药有限责任公司原吉林隆泰制药股份有限公司","","","红云制药梁河有限公司原云南梁河民族制药有限公司","红云制药梁河有限公司原云南梁河民族制药有限公司","","","天津金耀集团河北永光制药有限公司原永光制药有限公司","天津金耀集团河北永光制药有限公司原永光制药有限公司","","","成都倍特得诺药业有限公司原四川宝鉴堂药业有限公司","成都倍特得诺药业有限公司原四川宝鉴堂药业有限公司","","","哈尔滨敷尔佳科技股份有限公司原哈尔滨北星药业有限公司","哈尔滨敷尔佳科技股份有限公司原哈尔滨北星药业有限公司","","","","哈尔滨瀚钧现代制药有限公司原哈尔滨瀚钧药业有限公司","哈尔滨瀚钧现代制药有限公司原哈尔滨瀚钧药业有限公司","","","","天津信谊津津药业有限公司原天津市津津药业有限公司","天津信谊津津药业有限公司原天津市津津药业有限公司","","","","","鼎复康药业股份有限公司原河南鼎复康药业股份有限公司","鼎复康药业股份有限公司原河南鼎复康药业股份有限公司","","","吉林福康药业股份有限公司原海南天煌制药有限公司","吉林福康药业股份有限公司原海南天煌制药有限公司","","","江西马应龙美康药业有限公司原江西禾氏美康药业有限公司","天地恒一制药股份有限公司原湖南天地恒一制药有限公司","","","","","","","","","","","","","","","","","","");
        originalCorps = originalCorps.stream().filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());

        for (String originalCorp : originalCorps) {
            System.out.println(originalCorp);
            Pattern parsePattern = Pattern.compile("^(.*?)有限公司原(.*)$");
            Matcher matcher = parsePattern.matcher(originalCorp);
            boolean isMatch = matcher.find();
            if (isMatch) {
                System.out.println(matcher.group(1));
                Arrays.stream(tryParseCorp(matcher.group(1)+"有限公司").split(" ")).forEach(item -> {
                    System.out.println(item);
                });
                System.out.println();
                System.out.println(matcher.group(2));
                Arrays.stream(tryParseCorp(matcher.group(2)).split(" ")).forEach(item -> {
                    System.out.println(item);
                });

                System.out.println();
                System.out.println();
                System.out.println();
            }
        }

    }

    @Test
    public void testParseSpecialCorp2() {
        /* 可变参数 */
        List<String> originalCorps = Lists.newArrayList("江西今典生物科技有限公司委托江西云恩健康产业有限公司","芜湖市诺康生物科技有限公司委托浙江柏客健实业有限公司","广州市康采医疗用品有限公司委托郑州航空港区康悦生物技术有限公司","上卫中亚卫生材料江苏有限公司委托浙江汇康医药用品有限公司","山东健康药业有限公司委托山东博山制药有限公司","稳健医疗黄冈有限公司委托稳健医疗天门有限公司","脱普日用化学品中国有限公司委托景辰无锡塑业有限公司","珠海康奇有限公司委托无锡健特药业有限公司","","甘肃奇正藏药有限公司委托方西藏奇正藏药股份有限公司","东阿阿胶股份有限公司委托燕之初健康美厦门食品有限公司","南京白敬宇制药有限责任公司委托方西安杨森制药有限公司","上海上药信谊药厂有限公司委托上海新亚药业闵行有限公司","成都亿帆达生物科技有限公司委托广东源健食品有限公司","可孚医疗科技股份有限公司委托湖南可孚医疗设备有限公司","天津玉匾国健医药科技有限公司委托广东长兴生物科技股份有限公司","北京欣乐佳国际健康科技有限公司委托云南白药集团丽江药业有限公司","广州汇纳生物科技有限公司委托开平市美康泉生物科技有限公司","广州奈梵斯健康产品有限公司委托汤臣倍健股份有限公司","广州卯金氏医药科技有限公司委托湖南公信堂药业有限公司","北京纳吉兴保健食品有限公司委托安徽全康药业有限公司","振德医疗用品股份有限公司委托许昌振德医用敷料有限公司","济南胜胜药业有限公司委托山东登胜药业有限公司","稳健医疗用品股份有限公司委托山东东华医疗科技有限公司","重庆科瑞东和制药有限责任公司委托方重庆科瑞制药集团有限公司","养生堂药业有限公司委托杭州养生堂保健品有限公司","上海复沃实业有限公司委托广州清碧化妆品有限公司","海南正康药业有限公司委托广东长兴生物科技股份有限公司","青岛健康家生物科技有限公司委托杭州麦金励生物科技有限公司","浙江上药九旭药业有限公司委托格乐瑞无锡营养科技有限公司","深圳市海王健康科技发展有限公司委托杭州海王生物工程有限公司","广西邦琪药业集团有限公司委托广西百琪药业有限公司","湖北马应龙护理品有限公司委托德阳市美妆庭纸业有限公司","广州王老吉大健康产业有限公司委托贵州省潮映大健康饮料有限公司","华润三九医药股份有限公司委托惠州市九惠制药股份有限公司","成都亿帆达生物科技有限公司委托广东多合生物科技有限公司","广州市佰健生物工程有限公司委托汤臣倍健股份有限公司","成都康弘制药有限公司委托四川济生堂药业有限公司","广州白云山奇星药业有限公司委托广州白云山中一药业有限公司","丁家宜苏州工业园区化妆品贸易有限公司委托士齐生物研发中心苏州工业园内有限公司","蓝洋药业辽宁集团有限公司委托铁岭市蓝洋医疗器械有限公司","西南药业股份有限公司委托太极集团四川太极制药有限公司","上海艾申特生物科技有限公司委托纽斯葆广赛广东生物科技股份有限公司","迪庆香格里拉雪域生物有限公司委托成都市益康药业有限公司","云南白药集团股份有限公司委托振德医疗用品股份有限公司","惠州市鑫福来实业发展有限公司委托湖北康恩萃药业有限公司","扬子江药业集团北京海燕药业有限公司委托扬子江药业集团江苏紫龙药业有限公司","紫光格林泰乐生物技术济南有限公司委托河北一然生物科技股份有限公司","广东星昊药业有限公司委托北京星昊医药股份有限公司","振德医疗用品股份有限公司委托安徽美迪斯医疗用品有限公司","以岭健康科技有限公司委托温州云上生物科技有限公司","云南通用善美制药有限责任公司委托云南云河药业股份有限公司","云南白药集团股份有限公司委托江苏南方卫材医药股份有限公司","江苏蒲地蓝日化有限公司委托苏州克劳丽化妆品有限公司","健民药业集团股份有限公司委托健民集团叶开泰国药随州有限公司","福建片仔癀化妆品有限公司委托福建省梦娇兰日用化学品有限公司","岐伯医药吉林有限公司委托广东德洲医疗器械有限公司","江西金川宁生物科技有限公司委托企业广州正龙生物科技有限公司","海南万民康肽生物科技有限公司委托江西云恩健康产业集团有限公司","华润三九枣庄药业有限公司委托方华润三九医药股份有限公司","山东新华制药股份有限公司委托方拜耳医药保健有限公司","湖南嘉晗医疗器械有限公司委托湖南银华棠医药科技有限公司","江西济仁药业有限公司委托南昌市草珊瑚生物技术有限公司","江苏南方卫材医药股份有限公司委托方桂林天和药业伊维有限公司","潮州市潮安区优崔莱食品厂委托方成都亿帆达生物科技有限公司","贵州宏宇药业有限公司委托赛维泰广州健康药业有限公司","广州白云山陈李济药厂有限公司委托惠州市乐口佳食品有限公司","广州白云山星群药业股份有限公司委托汕头市利是堂保健食品厂","江西药都樟树制药有限公司委托江西药都仁和制药有限公司","深圳市海王健康科技发展有限公司委托吉林海王健康生物科技有限公司","云南米芽科技有限公司委托丘北愚公农业发展有限责任公司","广州市惠优喜生物科技有限公司委托纽斯葆广赛广东生物科技股份有限公司","储康保健科技南京有限公司委托江苏滋补堂药业有限公司","江苏亚邦爱普森药业有限公司委托江苏亚邦强生药业有限公司","北京华素制药股份有限公司委托广州市花都区晶神保健品厂","江苏澳新生物工程有限公司浙江澳兴生物科技有限公司委托浙江柏客健实业有限公司","");
        originalCorps = originalCorps.stream().filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());

        for (String originalCorp : originalCorps) {
            System.out.println(originalCorp);
            Pattern parsePattern = Pattern.compile("^(.*?)委托(.*)$");
            Matcher matcher = parsePattern.matcher(originalCorp);
            boolean isMatch = matcher.find();
            if (isMatch) {
                System.out.println(matcher.group(1));
                Arrays.stream(tryParseCorp(matcher.group(1)).split(" ")).forEach(item -> {
                    System.out.println(item);
                });
                System.out.println();
                System.out.println(matcher.group(2));
                Arrays.stream(tryParseCorp(matcher.group(2)).split(" ")).forEach(item -> {
                    System.out.println(item);
                });

                System.out.println();
                System.out.println();
                System.out.println();
            }
        }
    }

    @Test
    public void dealCorpDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/corp.original.txt";
        String dictionaryPath = "data/xyy/dictionary/corp.txt";

        // 加载词典
        List<String> originalCorps = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultCorps = Lists.newArrayListWithExpectedSize(originalCorps.size());
        for (String originalCorp : originalCorps) {
            originalCorp = replaceSpecialChar(originalCorp);
            if (StringUtils.isNotEmpty(originalCorp)) {
                resultCorps.add(originalCorp);
                // 化繁为简、大写转小写，全角转半角。
                String convertDictionary = CharTable.convert(originalCorp);
                if (!Objects.equals(originalCorp, convertDictionary)) {
                    resultCorps.add(convertDictionary);
                }
            }
        }
        // 排序
        resultCorps = resultCorps.stream()
//                .sorted(String::compareTo)
                .distinct().collect(Collectors.toList());
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, resultCorps);
    }

    @Test
    public void checkCorpDictionary() throws IOException {
        new DynamicCustomDictionary().reload();

        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的店铺展示名称&厂商_2024_04_02.xlsx";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("词典Excel没有数据，终止");
            return;
        }
//        Set<String> topQueryKeywords = Sets.newHashSet("");
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("词典没有数据，终止");
            return;
        }
        Set<String> natureSet = Arrays.stream(XyyNatureEnum.values()).map(XyyNatureEnum::toString).collect(Collectors.toSet());

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
//            for (String topQueryKeyword : topQueryKeywords) {
                rowDTO.setDictionary(replaceSpecialChar(rowDTO.getDictionary()));
//                if (rowDTO.getDictionary().contains(topQueryKeyword)) {
                    List<Term> terms = newSegment.seg(rowDTO.getDictionary());
                    List<String> termStrList = terms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                        return term.word + "/" + term.nature.toString();
                    }).collect(Collectors.toList());
                    rowDTO.setRealDictionary(String.join(" ", termStrList));

                    List<Term> indexTerms = newIndexSegment.seg(rowDTO.getDictionary());
                    List<String> indexTermStrList = indexTerms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                        return term.word + "/" + term.nature.toString();
                    }).collect(Collectors.toList());
                    rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
                    resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
//                }
//            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/查询EC上线中的店铺展示名称&厂商_2024_04_02_result.xlsx";
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(resultRowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(resultDictionaryExcelPath, excelRows);
        log.debug("处理词典Excel，成功。");
    }
    // ====================================================================================================
    /**
     * 处理词典Excel
     */
    @Test
    public void dealSpecDictionaryExcel() {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的规格_2024_04_02.xlsx";

        // 仅仅使用默认自定义词典，避免领域数据干扰行政区域数据。
        DynamicCustomDictionary dictionary = new DynamicCustomDictionary("data/dictionary/custom/CustomDictionary.txt");
        dictionary.reload();
        newSegment.enableCustomDictionary(dictionary);

        /* 备份 */
        XyyDrugCorpusDictionaryExcelOperator.backup(dictionaryExcelPath);

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("规格词典Excel没有数据，终止");
            return;
        }

        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            rowDTO.setRealDictionary(this.tryParseSpec(rowDTO.getDictionary()));
        }

        // 写Excel
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(rowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(dictionaryExcelPath, excelRows);
        log.debug("处理规格词典Excel，成功。");

        // 还原自定义词典配置
        new DynamicCustomDictionary().reload();
    }

    private String tryParseSpec(String original) {
        original = this.replaceSpecialChar(original);
        if (StringUtils.isEmpty(original) || original.length() <= 2) {
            return "";
        }
        return original;
    }

    /**
     * 制作词典
     */
    @Test
    public void makeSpecDictionary() throws IOException {
        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的规格_2024_04_02.xlsx";
        String dictionaryPath = "data/xyy/dictionary/spec.original.txt";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("规格词典Excel没有数据，终止");
            return;
        }

        List<String> corps = this.listSpecs(rowDTOS);

        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, corps);
        log.info("规格词典成功：{}", dictionaryPath);
    }

    private List<String> listSpecs(List<XyyDrugCorpusDictionaryRowDTO> rowDTOS) {
        List<String> specs = Lists.newArrayListWithExpectedSize(rowDTOS.size());
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
            if (Objects.nonNull(rowDTO) && StringUtils.isNotEmpty(rowDTO.getRealDictionary())) {
                specs.add(rowDTO.getRealDictionary());
            }
        }
        return specs;
    }

    @Test
    public void dealSpecDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/spec.original.txt";
        String dictionaryPath = "data/xyy/dictionary/spec.txt";

        // 加载词典
        List<String> originalSpecs = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultSpecs = Lists.newArrayListWithExpectedSize(originalSpecs.size());
        for (String originalSpec : originalSpecs) {
            originalSpec = replaceSpecialChar(originalSpec);
            if (StringUtils.isNotEmpty(originalSpec)) {
                resultSpecs.add(originalSpec);
                // 化繁为简、大写转小写，全角转半角。
                String convertDictionary = CharTable.convert(originalSpec);
                if (!Objects.equals(originalSpec, convertDictionary)) {
                    resultSpecs.add(convertDictionary);
                }
            }
        }
        // 排序
        resultSpecs = resultSpecs.stream()
//                .sorted(String::compareTo)
                .distinct().collect(Collectors.toList());
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, resultSpecs);
    }

    @Test
    public void checkSpecDictionary() throws IOException {
        new DynamicCustomDictionary().reload();

        /* 可变参数 */
        String dictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的规格_2024_04_02.xlsx";

        // 加载Excel
        List<XyyDrugCorpusDictionaryRowDTO> rowDTOS = XyyDrugCorpusDictionaryExcelOperator.readAllRows(dictionaryExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("词典Excel没有数据，终止");
            return;
        }
//        Set<String> topQueryKeywords = Sets.newHashSet("");
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("词典没有数据，终止");
            return;
        }
        Set<String> natureSet = Arrays.stream(XyyNatureEnum.values()).map(XyyNatureEnum::toString).collect(Collectors.toSet());

        List<XyyDrugCorpusDictionaryRowDTO> resultRowDTOS = Lists.newArrayListWithExpectedSize(16);
        for (XyyDrugCorpusDictionaryRowDTO rowDTO : rowDTOS) {
//            for (String topQueryKeyword : topQueryKeywords) {
            rowDTO.setDictionary(replaceSpecialChar(rowDTO.getDictionary()));
//                if (rowDTO.getDictionary().contains(topQueryKeyword)) {
            List<Term> terms = newSegment.seg(rowDTO.getDictionary());
            List<String> termStrList = terms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary(String.join(" ", termStrList));

            List<Term> indexTerms = newIndexSegment.seg(rowDTO.getDictionary());
            List<String> indexTermStrList = indexTerms.stream().map(term -> {
//                        if (!natureSet.contains(term.nature.toString())) {
//                            return term.word + "/" + XyyNatureEnum.other;
//                        }
                return term.word + "/" + term.nature.toString();
            }).collect(Collectors.toList());
            rowDTO.setRealDictionary2(String.join(" ", indexTermStrList));
            resultRowDTOS.add(rowDTO);
//                    log.info("关键词【{}】，【{}】分词：{}", topQueryKeyword, rowDTO.getDictionary(), newSegment.seg(rowDTO.getDictionary()).toString());
//                }
//            }
        }
        // 写Excel
        String resultDictionaryExcelPath = "data/xyy/dictionary/查询EC在售商品的规格_2024_04_02_result.xlsx";
        List<XyyDrugCorpusDictionaryExcelRow> excelRows = XyyDrugCorpusDictionaryExcelOperator.createExcelRows(resultRowDTOS);
        XyyDrugCorpusDictionaryExcelOperator.coverWrite(resultDictionaryExcelPath, excelRows);
        log.debug("处理词典Excel，成功。");
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
        showName = this.replaceSpecialChar(showName);
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


    private String replaceSpecialChar(String original) {
        if (StringUtils.isEmpty(original)) {
            return "";
        }
        return original.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9.*.]+", "");
    }

}
