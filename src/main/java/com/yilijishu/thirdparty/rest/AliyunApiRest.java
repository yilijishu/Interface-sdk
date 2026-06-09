package com.yilijishu.thirdparty.rest;

import com.aliyun.cloudauth20190307.models.Id2MetaVerifyResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.tea.utils.StringUtils;
import com.yilijishu.thirdparty.bean.ApiRestResult;
import com.yilijishu.utils.MD5Utils;
import com.yilijishu.utils.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AliyunApiRest {

    private static AliyunApiRest rest = null;

    private static Map<String, String> map = new HashMap<>();
    static  {
        map.put("cn-beijing", "cloudauth.cn-beijing.aliyuncs.com");
        map.put("default", "cloudauth.aliyuncs.com");
    }
    private static String defaultEndpoint = "cloudauth.aliyuncs.com";
    private static com.aliyun.cloudauth20190307.Client client = null;

    private AliyunApiRest() {
    }

    /**
     *
     * @param region    传递 cn-beijing、 default
     * @param accessKeyId ak
     * @param accessKeySecret sk
     * @return 返回阿里云api请求实例
     */
    public static AliyunApiRest aliyunApiRest (String region, String accessKeyId, String accessKeySecret) throws BizException {
        if(rest == null) {
            rest = new AliyunApiRest();
            com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                    .setCredential(credential);
            config.accessKeySecret = accessKeySecret;
            config.accessKeyId = accessKeyId;
            String endpoint = map.get(region);
            if(StringUtils.isEmpty(endpoint)) {
                endpoint = defaultEndpoint;
            }
            config.endpoint = endpoint;
            try {
                client = new com.aliyun.cloudauth20190307.Client(config);
            } catch (Exception e) {
                throw new BizException("无法创建阿里云客户端实例", e);
            }
        }
        return rest;
    }

    /**
     * 校验姓名和身份证号是否一致
     * @param identidyName 姓名
     * @param identifyNum 身份证号
     * @return ApiRestResult类 Integer 1：核验一致。 2：核验不一致。3：查无记录。4: 接口查询异常
     */
    public ApiRestResult<Integer> id2MetaVerify(String identidyName, String identifyNum) {
        String firstName = identidyName.substring(0,1);
        String endName = identidyName.substring(1);
        String firstIdentifyNum = identifyNum.substring(0,6);
        String tmp = identifyNum.substring(6);
        String endIdentifyNum = tmp.substring(tmp.length()-4);
        String middleIdentifyNum = tmp.replaceAll(endIdentifyNum, "");
        com.aliyun.cloudauth20190307.models.Id2MetaVerifyRequest id2MetaVerifyRequest = new com.aliyun.cloudauth20190307.models.Id2MetaVerifyRequest()
                .setParamType("md5")
                .setIdentifyNum(firstIdentifyNum.concat(MD5Utils.encrypByMd5(middleIdentifyNum)).concat(endIdentifyNum))
                .setUserName(MD5Utils.encrypByMd5(firstName).concat(endName));
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            com.aliyun.cloudauth20190307.models.Id2MetaVerifyResponse resp = client.id2MetaVerifyWithOptions(id2MetaVerifyRequest, runtime);
            String rawData = new com.google.gson.Gson().toJson(resp);
            log.info("请求接口返回数据: {}", rawData);
            ApiRestResult<Integer> apiRestResult = new ApiRestResult<>();
            apiRestResult.setCode(200);
            apiRestResult.setRawData(rawData);
            apiRestResult.setData(4);
            if(resp.getStatusCode() == 200) {
                Id2MetaVerifyResponseBody body =  resp.getBody();
                if("200".equals(body.getCode())) {
                    Id2MetaVerifyResponseBody.Id2MetaVerifyResponseBodyResultObject result = body.getResultObject();
                    apiRestResult.setData(Integer.parseInt(result.getBizCode()));
                }
            }
            return apiRestResult;
        } catch (TeaException error) {
            log.error(error.getMessage());
            throw new BizException("Tea 错误", error);
        } catch (Exception _error) {
            log.error(_error.getMessage());
            throw new BizException("全局错误", _error);
        }
    }

}
