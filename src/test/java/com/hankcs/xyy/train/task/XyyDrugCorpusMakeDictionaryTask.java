package com.hankcs.xyy.train.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.xyy.utils.XyyDrugCorpusUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * xyy物料：制作词典
 *
 * @author luyong
 */
public class XyyDrugCorpusMakeDictionaryTask {

    @Test
    public void checkDictionaryDuplicate() throws IOException {
        /* 可变参数 */
        String brandDictionaryPath = "data/xyy/dictionary/brand.original.txt";
        String corpDictionaryPath = "data/xyy/dictionary/corp.original.txt";
        String specDictionaryPath = "data/xyy/dictionary/spec.original.txt";
        String dosageDictionaryPath = "data/xyy/dictionary/dosage.original.txt";
        String coreDictionaryPath = "data/xyy/dictionary/core.original.txt";
        String otherDictionaryPath = "data/xyy/dictionary/other.original.txt";

        Set<String> brands = Sets.newHashSet(FileUtils.readLines(new File(brandDictionaryPath), "UTF-8"));
        brands.remove("");

        Set<String> corps = Sets.newHashSet(FileUtils.readLines(new File(corpDictionaryPath), "UTF-8"));
        corps.remove("");

        Set<String> specs = Sets.newHashSet(FileUtils.readLines(new File(specDictionaryPath), "UTF-8"));
        specs.remove("");

        Set<String> dosages = Sets.newHashSet(FileUtils.readLines(new File(dosageDictionaryPath), "UTF-8"));
        dosages.remove("");

        Set<String> cores = Sets.newHashSet(FileUtils.readLines(new File(coreDictionaryPath), "UTF-8"));
        cores.remove("");

        Set<String> others = Sets.newHashSet(FileUtils.readLines(new File(otherDictionaryPath), "UTF-8"));
        others.remove("");

        // 品牌
        System.out.println("=======>品牌<========");
        Sets.intersection(brands, corps).stream().forEach(System.out::println);
        Sets.intersection(brands, specs).stream().forEach(System.out::println);
        Sets.intersection(brands, dosages).stream().forEach(System.out::println);
        Sets.intersection(brands, cores).stream().forEach(System.out::println);
        Sets.intersection(brands, others).stream().forEach(System.out::println);

        // 企业
        System.out.println("=======>企业<========");
        Sets.intersection(corps, brands).stream().forEach(System.out::println);
        Sets.intersection(corps, specs).stream().forEach(System.out::println);
        Sets.intersection(corps, dosages).stream().forEach(System.out::println);
        Sets.intersection(corps, cores).stream().forEach(System.out::println);
        Sets.intersection(corps, others).stream().forEach(System.out::println);

        // 规格
        System.out.println("=======>规格<========");
        Sets.intersection(specs, brands).stream().forEach(System.out::println);
        Sets.intersection(specs, corps).stream().forEach(System.out::println);
        Sets.intersection(specs, dosages).stream().forEach(System.out::println);
        Sets.intersection(specs, cores).stream().forEach(System.out::println);
        Sets.intersection(specs, others).stream().forEach(System.out::println);

        // 核心
        System.out.println("=======>核心<========");
        Sets.intersection(cores, brands).stream().forEach(System.out::println);
        Sets.intersection(cores, corps).stream().forEach(System.out::println);
        Sets.intersection(cores, dosages).stream().forEach(System.out::println);
        Sets.intersection(cores, specs).stream().forEach(System.out::println);
        Sets.intersection(cores, others).stream().forEach(System.out::println);

    }

//    @Test
    public void dealDictionaryDuplicate() throws IOException {
        /* 可变参数 */
        String brandDictionaryPath = "data/xyy/dictionary/brand.original.txt";
        String corpDictionaryPath = "data/xyy/dictionary/corp.original.txt";
        String specDictionaryPath = "data/xyy/dictionary/spec.original.txt";
        String dosageDictionaryPath = "data/xyy/dictionary/dosage.original.txt";

        {
            // 删除不是品牌的词
            List<String> originalBrands = FileUtils.readLines(new File(brandDictionaryPath), "UTF-8");
            Set<String> notBrandSet = Sets.newHashSet("中西制药","安琪胶业","福广制药","莱美药业","海德药业","费森尤斯卡比","成都倍特","瑞阳制药","力生制药","华晟制药","山东鲁抗","佳泰制药","川眉药业","海鲸药业","崇光药业","金华隆制药","中辰药业","逸云医药","松龄堂中药","苗德堂药业","云南养尊堂","国润制药","武汉华珍","吉凯药业","诺华制药","华奥药业","医创药业","德成制药","古蔺宏安","药友制药","恒生制药","鲁药制药","天成药业","天龙药业","江苏鹏鹞","国源国药","桂林红会","鸿博药业","济邦药业","人福药业","金丝利药业","上海新亚","和黄药业","青峰药业","同和春药业","普元药业","永基药业","先声","山西华康","中诺药业","颐和药业","亚宝药业","京新药业","珠海同源","斯威药业","龟鹿药业","天方","泰升药业","江西草珊瑚","东格尔","欧意药业","金柯制药","万汉制药","开封制药","洛阳紫光","益欣药业","邦克实业","澳迩药业","华辰制药","美优制药","俊宏药业","金芝堂药业","仁和堂","灵源药业","福建古田","白鹿制药","融昱药业","维威制药","粤东药业","东乐制药","济生制药","涛生制药","良方制药","南国药业","维吾尔药业","正方制药","华仁药业","合瑞制药","金汇药业","天衡制药","全康药业","众腾药业","汉唐制药","京丰制药","千方中药","悦兴药业","福建广生堂","上海青平","韩美药品","天瑞","雪樱花制药","宝太生物科技","天生","施美药业","贵州苗彝","乐声药业","良福制药","百盛药业","山海中药","建昌帮","纽斯葆广赛","仁心药业","河北国金","保定迈卓","全宇制药","北京益民","草珊瑚药业","助邦科技","江波制药","和明制药","白云山汤阴东泰","卫华药业","科伦制药","迪龙制药","手心制药","百科亨迪","雪山七草","中兴药业","保金药业","三真药业","三九药业","鸿烁制药","山西同达","维康药业","武汉五景","明华制药","三诚实业","凯程医药","罗浮山国药","澳洲雀巢Nestle","时珍制药","长江药业","品先实业","通园制药","菲德力制药","万辉制药","欧化","北京京丰","华泰药业","爱民药业","郑州凯利","康维德","荷普药业","诺安药业","广康药业","吉林福辉","中法制药","康缘药业","惠州市中药厂","红岩药业","双鹭药业","百正药业","胜合制药","山东益康","云南七丹","万方制药","双吉制药","弘腾药业","普德药业","赛灵药业","福建同安堂","库尔科技","诺捷制药","华润赛科","和田维吾尔","豫章药业","青平药业","南阳艾美","晨光药业","抚顺青松","天润药业","广州康和药业","益康药业","高邈药业","知原药业","云南向辉","仁康药业","依科制药","太宝制药","金鸿药业","黄海制药","禾泰药业","天津同仁堂","江苏飞马","柏林药业","可为实业","南京海辰","石家庄四药","同达药业","德国拜耳","全泰药业","天德制药","心意药业","盛林药业","药王集团","万森制药","浙江鼎泰","安徽城市","河北东风","明珠药业","健民药业","丹东医创","润德药业","红桃开","唐山集川","道君药业","万泽药业","大亚制药","蓝天药业","祥芝药业","雪山七草医药","一方制药","金水宝制药","重庆天致","上海松华","扬州制药","成都锦华","金龙药业","千海兴龙旗舰店","九阳药业","浙江医药","利君制药","三晋药业","太康海恩","贝参药业","安徽仁和","华仁","汉方药业","康华卫材","中药制药","绿叶制药","九郡药业","康乐药业","津升制药","吉贝尔药业","上海朝晖","浙江新光","田田药业","特瑞药业","药牛中药","长泰药业","国正药业","民济药业","金峰制药","祥禾卫材","尔康制药","弘森药业","瑞龙制药","金诃藏药","九势制药","湖南洞庭","白云山盈康药业","海鹤药业","天津宏仁堂","丹东药业","中大药业","康嘉药业","广东南国","万通药业","美欣制药","国光药业","众妙药业","湘雅制药","鲁抗医药","阳光药业","永丰药业","永宁药业","长坤科技","爱心药业","北方药业","华南药业","世彪药业","湖南科伦制药","南京同仁堂生物科技有限公司","中华药业","宇妥藏药","通化久铭","旭阳药业","淘儿宁药械","名客刷业","正大天晴药业","恒诚制药","环球","华康卫材","迪康药业","江西中正","振兴制药","拜耳","川奇药业","贵州威利德","抚松制药","百善药业","幸福医药","汇中制药","集川药业","贵州宏宇","四药制药","滕王阁药业","森科药业","广西泰嵘","沈阳红药集团","天目药业","固康药业","永康药业","维和药业","现代制药","中新药业","田美药业","大连汉方","双鹤药业","特研药业","华威药业","旭峰药业","庚贤堂制药","厚捷制药","康和药业","良辉药业","盛安堂药业","红林制药","宛东药业","宏大药业","云南云河","百利药业","科鹏电子","众生药业","大冢制药","新华制药","通化振霖","遂成药业","华北制药","中蒙制药","海天制药","华润双鹤","东阳光药业","恒康药业","太洋药业","益翔药业","齐云卫材","中药四厂","大连盛泓","扬州中宝","红瑞制药","燕兴药业","海斯制药","希力药业","武汉健民","红蝶新材料","普爱药业","千海兴龙","御嘉药业","力康药业","美图制药","逢春制药","东方丝路医纺","皓博药业","山东胜利","康迪药械","保利制药","普康药业","德致制药","蒙药","润都制药","明德药业","通化百信","海纳制药","通化斯威","同方药业","河北维格拉","天方药业","汉盛药业","福康药业","上海凯宝","一格制药","齐都药业","怀仁制药","奥生科技","华联","青襄药业","健峰药业","安阳九州","和硕药业","东陵药业","华润赛科药业","锦瑞制药","云南圣科","石家庄康力","西安北方","成都迪康","西南药业","岷海制药","正大青春宝","中南制药","九连山药业","承开中药","广信药业","天致药业","盛翔制药","天瑞药业","通化白山","杭州民生","海恩药业","新亚","苏州弘森","海洲药业","浙江泰利森","新华达制药","云鹏医药","金恒制药","源瑞制药","天津金耀","纽兰药业","海沣药业","信谊药厂","海悦药业","多多药业","仁和制药","三九全康药业","隆信制药","石药集团","中宝药业","北大医药","英科医疗","春柏药业","迪耳药业","联谊药业","一力制药");
            notBrandSet.addAll(Sets.newHashSet("大洋制药","医大药业","植物药业","天生药业","鲁银药业","九典制药","华侨药业","辽宁天龙","沈阳红药","东风药业","立健药业","承德天原","中孚药业","嘉应制药","中族中药","大别山药业","精华制药","贵阳新天","南京海鲸","昊森药业","永安制药","汇元药业","长生药业","南京老山","九方制药","亿友药业","倍特药业","百澳药业","湖北虎泉","宝芝林药业","正恒药业","碑林药业","云南裕丰","康福药业","在田药业","紫金药业","新生制药","永宁制药","华信制药","金宝药业","卓宇制药","四川启隆","兰药药业","力强药业","白医制药","绿丹药业","紫光制药","海王药业","正达药业","仁民药业","古田药业","同药集团","康芝药业","万东药业","广东一力","李众胜堂","健之源","新辉药业","本草制药","一品制药","宝龙","华润三九","广承药业","万高药业","北京福元","隆泰制药","祯杨家","东新药业","琨腾药业","云鹏制药","宝珠制药","松辽制药","万岁药业","北陆药业","万禾制药","常州康普","瑞华制药","丰原药业","万杰制药","瑶铭药业","三药制药","仙琚制药","兰太药业","红星药业","华夏药材","万德制药","云南良方","路坦制药","锦华药业","通化利民","云南植物","百正","滇中药业","实正药业","东阿阿华医疗","弘景药业","江西汪氏","心康制药","英太制药","亚太药业","天之海药业","五盛药业集团","环球药业","福辉药业","荷花池药业","赤峰万泽","创美实业","君山制药","四川科伦","南昌卫材","金石制药","比智高","金耀药业","林宝药业","大恩药业","神龙药业","包头中药","天山药业","青云山药业","石家庄北方","中杰药业","山西太行","广康","美罗药业","万润药业","管城制药","国草药业","诺成药业","康奇","成都亨达","万康","中科利君","蒙欣药业","第一制药","金页制药","天力药业","弘升药业","江西制药","银谷制药","邵阳神农","百神药业","亿帆药业","西峰制药","天植中药","金发科技","华中药业","太行药业","深大药业","普阳药业","鑫瑞药业","林恒制药","罗邦药业","三普","山西天致","遂成","亳州花玉颜","汉晨药业","可济药业","泰复制药","赛诺制药","兰茂药业","五景药业","红石药业","江西民济","中药六厂","百康药业","众志药业","昊骏药业","民康制药","天生制药","康普药业","乐普","天宝药业","贵州飞云岭","天原药业","华源制药","邯郸制药","浙江都邦","常乐制药","祥瑞药业","东方药业","恒生药业","威门药业","江苏长江","胜光药业","孚众药业","振东制药","莉君药业","东宝药业","九州药业","正大制药","山西太原","新亚药业","润华药业","泓圃药业","汾河制药","昊邦制药","广誉远国药","浙江惠迪森","辉成药业","科顿制药","正通药业","白云山制药","都邦药业","中盛药业","白山药业","扬子江药业","宜昌人福","飞龙药业","奥林特药业","国宏药业","天台山制药","光正制药","赛卓药业","归正药业","众康","紫竹药业","迈迪制药","宛西制药","天然药业","新功药业","海山药业","奇力制药","东海药业","以德制药","恒新药业","悦康药业","蜀中药业","振兴中药","朗宏实业","江西钟山","华西制药","颐生药业","云南国鹤","虎泉药业","吉林真元","德成","华药药业","金马药业","上锦制药","智慧脸","国药集团天目湖","万玮制药","仙河药业","大安制药","安徽宏业"));
            originalBrands = originalBrands.stream().filter(item -> {
                if (StringUtils.isNotEmpty(item) && notBrandSet.contains(item)) {
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
            File dictionaryFile = new File(brandDictionaryPath);
            FileUtils.writeLines(dictionaryFile, originalBrands);
        }

    }

    @Test
    public void makeDictionary() throws IOException {
        makeBrandDictionary();
        makeDosageDictionary();
        makeCorpDictionary();
        makeSpecDictionary();
        makeCoreDictionary();
        makeOtherDictionary();
    }

    @Test
    public void makeBrandDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/brand.original.txt";
        String dictionaryPath = "data/xyy/dictionary/brand.txt";

        // 加载brand词典
        List<String> originalBrands = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultBrands = Lists.newArrayListWithExpectedSize(originalBrands.size());
        for (String originalBrand : originalBrands) {
            originalBrand = XyyDrugCorpusUtils.replaceSpecialChar(originalBrand);
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
    public void makeDosageDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/dosage.original.txt";
        String dictionaryPath = "data/xyy/dictionary/dosage.txt";

        // 加载词典
        List<String> originalDosages = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultDosages = Lists.newArrayListWithExpectedSize(originalDosages.size());
        for (String originalDosage : originalDosages) {
            originalDosage = XyyDrugCorpusUtils.replaceSpecialChar(originalDosage);
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

    @Test
    public void makeCorpDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/corp.original.txt";
        String dictionaryPath = "data/xyy/dictionary/corp.txt";

        // 加载词典
        List<String> originalCorps = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultCorps = Lists.newArrayListWithExpectedSize(originalCorps.size());
        for (String originalCorp : originalCorps) {
            originalCorp = XyyDrugCorpusUtils.replaceSpecialChar(originalCorp);
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
    public void makeSpecDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/spec.original.txt";
        String dictionaryPath = "data/xyy/dictionary/spec.txt";

        // 加载词典
        List<String> originalSpecs = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultSpecs = Lists.newArrayListWithExpectedSize(originalSpecs.size());
        for (String originalSpec : originalSpecs) {
            originalSpec = XyyDrugCorpusUtils.replaceSpecialChar(originalSpec);
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
    public void makeCoreDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/core.original.txt";
        String dictionaryPath = "data/xyy/dictionary/core.txt";

        // 加载词典
        List<String> originalCores = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultCores = Lists.newArrayListWithExpectedSize(originalCores.size());
        for (String originalCore : originalCores) {
            originalCore = XyyDrugCorpusUtils.replaceSpecialChar(originalCore);
            if (StringUtils.isNotEmpty(originalCore)) {
                resultCores.add(originalCore);
                // 化繁为简、大写转小写，全角转半角。
                String convertDictionary = CharTable.convert(originalCore);
                if (!Objects.equals(originalCore, convertDictionary)) {
                    resultCores.add(convertDictionary);
                }
            }
        }
        // 排序
        resultCores = resultCores.stream()
//                .sorted(String::compareTo)
                .distinct().collect(Collectors.toList());
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, resultCores);
    }

    @Test
    public void makeOtherDictionary() throws IOException {
        /* 可变参数 */
        String originalDictionaryPath = "data/xyy/dictionary/other.original.txt";
        String dictionaryPath = "data/xyy/dictionary/other.txt";

        // 加载词典
        List<String> originalOthers = FileUtils.readLines(new File(originalDictionaryPath), "UTF-8");

        List<String> resultOthers = Lists.newArrayListWithExpectedSize(originalOthers.size());
        for (String originalOther : originalOthers) {
            originalOther = XyyDrugCorpusUtils.replaceSpecialChar(originalOther);
            if (StringUtils.isNotEmpty(originalOther)) {
                resultOthers.add(originalOther);
                // 化繁为简、大写转小写，全角转半角。
                String convertDictionary = CharTable.convert(originalOther);
                if (!Objects.equals(originalOther, convertDictionary)) {
                    resultOthers.add(convertDictionary);
                }
            }
        }
        // 排序
        resultOthers = resultOthers.stream()
//                .sorted(String::compareTo)
                .distinct().collect(Collectors.toList());
        File dictionaryFile = new File(dictionaryPath);
        FileUtils.writeLines(dictionaryFile, resultOthers);
    }
}
