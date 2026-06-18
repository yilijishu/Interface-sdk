
package com.yilijishu.thirdparty.rest;

import static com.aliyun.teautil.Common.toJSONString;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;

public class AliyunSmsApiRest {

	private static com.aliyun.dysmsapi20170525.Client aliYunClient = null;

	private static String endpoint = "dysmsapi.aliyuncs.com";
	private static String accessKey = "PTk6ArD8U3wo03tppoB23211233311qwqQ23AEEh";
	private static String secretKey = "PTk6ArD8U3wo03tppoB23211233311qwqQ23AEEh";

	private AliyunSmsApiRest(String accessKey, String secretKey) {
		AliyunSmsApiRest.accessKey = accessKey;
		AliyunSmsApiRest.secretKey = secretKey;
	}

	private static Client getClient() throws Exception {
		if (aliYunClient == null) {
			Config config = new Config().setAccessKeyId(accessKey).setAccessKeySecret(secretKey);
			config.endpoint = endpoint;
			aliYunClient = new com.aliyun.dysmsapi20170525.Client(config);
		}
		return aliYunClient;
	}

	/**
	 * 发送短信
	 */
	public static SendSmsResponse sendMessage(String contact, String SignName, String TemplateCode, String templateParam) throws Exception {
		if (contact == null || contact.isEmpty()) {
			return null;
		}
		Client client = AliyunSmsApiRest.getClient();

		SendSmsRequest sendSmsRequest = new SendSmsRequest().setPhoneNumbers(contact).setSignName(SignName).setTemplateCode(TemplateCode).setTemplateParam(templateParam);
		// 复制代码运行请自行打印 API 的返回值
		SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);

		return sendSmsResponse;
	}

	public static void main(String[] args) throws Exception {
		SendSmsResponse sendMessage = AliyunSmsApiRest.sendMessage("13488888888", "阿里云", "SMS_15305****", "{\"name\":\"张三\",\"number\":\"1390000****\"}");
		System.out.println(toJSONString(sendMessage));
	}

}