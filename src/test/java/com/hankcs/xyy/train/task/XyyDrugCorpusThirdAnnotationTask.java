package com.hankcs.xyy.train.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hankcs.xyy.train.dto.XyyDrugCorpusThirdAnnotationRowDTO;
import com.hankcs.xyy.train.operators.XyyDrugCorpusThirdAnnotationExcelOperator;
import com.hankcs.xyy.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.springframework.http.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Slf4j
public class XyyDrugCorpusThirdAnnotationTask {

    private static final String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";

    private static final String appKey = "b69kx1BjwU0s5yBqmxfpYaqG";

    private static final String appSecret = "Q4iaVnbUw7tNOUZPH3nCedqqOOBw2cwq";

    private static final String chatUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro";

    @Test
    public void doAnnotationByBaiduErnie4() {
        /* 可变参数 */
        String corpusExcelPath = "data/xyy/train/xyy_drug_show_name_cws.xlsx";
        List<XyyDrugCorpusThirdAnnotationRowDTO> rowDTOS = XyyDrugCorpusThirdAnnotationExcelOperator.readAllRows(corpusExcelPath);
        if (CollectionUtils.isEmpty(rowDTOS)) {
            log.info("没有物料数据，终止。");
            return;
        }
        String accessToken = this.getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            log.error("没有获取到Token，终止。");
            return;
        }
        int i = 0;
        for (XyyDrugCorpusThirdAnnotationRowDTO rowDTO : rowDTOS) {
            if (StringUtils.isNotEmpty(rowDTO.getThirdAnnotationResult())) {
                continue;
            }
            String showName = rowDTO.getShowName();
            String result;
            try {
                result = chantWithBaiduErnie4(accessToken, showName);
            } catch (Exception e) {
                if (Objects.equals(e.getMessage(), "111")) {
                    log.warn("【百度Ernie4】access token过期，即将重新获取令牌后重新请求，showName：{}", showName);
                    accessToken = this.getAccessToken();
                    result = chantWithBaiduErnie4(accessToken, showName);
                } else {
                    result = null;
                    log.error("【百度Ernie4】chat对话失败失败（出现异常），showName：{}，异常信息：", showName, e);
                }
            }
            rowDTO.setThirdAnnotationResult(result);
            i++;
            if (i % 20 == 0) {
                // 备份
                XyyDrugCorpusThirdAnnotationExcelOperator.backup(corpusExcelPath);
                log.info("【百度Ernie4】备份源文件成功");
                // 写
                XyyDrugCorpusThirdAnnotationExcelOperator.coverWrite(corpusExcelPath, XyyDrugCorpusThirdAnnotationExcelOperator.createExcelRows(rowDTOS));
                log.info("【百度Ernie4】阶段性保存源文件成功");
            }
        }
    }

    private String getAccessToken() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<String> responseEntity = HttpClientUtils.restTemplate.postForEntity(tokenUrl + "?grant_type=client_credentials&client_id=" + appKey + "&client_secret=" + appSecret, request, String.class);
        stopWatch.stop();
        HttpStatus httpStatus = responseEntity.getStatusCode();
        String result = responseEntity.getBody();
        if (log.isDebugEnabled()) {
            log.debug("【百度Ernie4】获取Token，耗时：{}，响应数据：{}", stopWatch, result);
        }
        if (!httpStatus.is2xxSuccessful()) {
            log.error("【百度Ernie4】获取Token失败（非200状态码），耗时：{}，响应数据：{}", stopWatch, result);
            return null;
        }
        if (StringUtils.isEmpty(result)) {
            log.error("【百度Ernie4】获取Token失败（没有响应数据），耗时：{}，响应数据：{}", stopWatch, result);
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        Integer errcode = jsonObject.getInteger("error_code");
        String errmsg = jsonObject.getString("error_msg");
        if (Objects.nonNull(errcode)) {
            if (Objects.equals(errcode, 111)) {
                throw new RuntimeException("111");
            }
            log.error("【百度Ernie4】获取Token失败（响应结果失败），耗时：{}，响应数据：{}", stopWatch, result);
            return null;
        }
        String accessToken = jsonObject.getString("access_token");
        if (StringUtils.isNotEmpty(accessToken)) {
            return accessToken;
        }
        log.info("【百度Ernie4】获取Token失败（没有结果），响应数据：{}", result);
        return null;
    }

    private String chantWithBaiduErnie4(String accessToken, String showName) {
        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(showName)) {
            return null;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String requestParamJson = getBaiduErnie4ChatRequestParamJson(showName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestParamJson, headers);
        ResponseEntity<String> responseEntity = HttpClientUtils.restTemplate.postForEntity(chatUrl + "?access_token=" + accessToken, request, String.class);
        stopWatch.stop();
        HttpStatus httpStatus = responseEntity.getStatusCode();
        String result = responseEntity.getBody();
        if (log.isDebugEnabled()) {
            log.debug("【百度Ernie4】chat对话，耗时：{}，showName：{}，参数：{}，响应数据：{}", stopWatch, showName, requestParamJson, result);
        }
        if (!httpStatus.is2xxSuccessful()) {
            log.error("【百度Ernie4】chat对话失败（非200状态码），耗时：{}，showName：{}，参数：{}，响应数据：{}", stopWatch, showName, requestParamJson, result);
            return null;
        }
        if (StringUtils.isEmpty(result)) {
            log.error("【百度Ernie4】chat对话失败（没有响应数据），耗时：{}，showName：{}，参数：{}，响应数据：{}", stopWatch, showName, requestParamJson, result);
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        Integer errcode = jsonObject.getInteger("error_code");
        String errmsg = jsonObject.getString("error_msg");
        if (Objects.nonNull(errcode)) {
            if (Objects.equals(errcode, 111)) {
                throw new RuntimeException("111");
            }
            log.error("【百度Ernie4】chat对话失败（响应结果失败），耗时：{}，showName：{}，参数：{}，响应数据：{}", stopWatch, showName, requestParamJson, result);
            return null;
        }
        String id = jsonObject.getString("id");
        String resultStr = jsonObject.getString("result");
        if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(resultStr)) {
            return resultStr;
        }
        log.info("【百度Ernie4】chat对话失败（没有结果），showName：{}，参数：{}，响应数据：{}", showName, requestParamJson, result);
        return null;
    }

    private String getBaiduErnie4ChatRequestParamJson(String showName) {
        JSONObject requestParamJSONObject = new JSONObject();
        JSONObject messageJSONObject = new JSONObject();
        messageJSONObject.put("role", "user");
        String content = MessageFormat.format("你是一个自然语言分析器，我需要应用于医药领域，请帮我进行ner分词，并且将结果用” “分割开，例如，“布洛芬胶囊”，返回结果为：布洛芬/成分词 胶囊/剂型词，请将“{0}”进行分词并返回，返回结果只要分词和词性，别的文字无需返回", showName);
        messageJSONObject.put("content", content);
        JSONArray messagesJSONArray = new JSONArray();
        messagesJSONArray.add(messageJSONObject);
        requestParamJSONObject.put("messages", messagesJSONArray);
        return requestParamJSONObject.toJSONString();
    }

}
