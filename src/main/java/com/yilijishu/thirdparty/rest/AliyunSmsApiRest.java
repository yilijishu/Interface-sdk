
package com.yilijishu.thirdparty.rest;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;

public class AliyunSmsApiRest {

	private com.aliyun.dysmsapi20170525.Client aliYunClient = null;

	private String endpoint = "dysmsapi.aliyuncs.com";
	private String accessKey = "LTAI5tA3N1odaBvP9";
	private String secretKey = "vdmaRLItsN1OYV2F9qPIIBH";

	public AliyunSmsApiRest(String accessKey, String secretKey) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	private Client getClient() throws Exception {
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
	public SendSmsResponse sendMessage(String contact, String SignName, String TemplateCode, String templateParam) throws Exception {
		if (contact == null || contact.isEmpty()) {
			return null;
		}
		Client client = getClient();

		SendSmsRequest sendSmsRequest = new SendSmsRequest().setPhoneNumbers(contact).setSignName(SignName).setTemplateCode(TemplateCode).setTemplateParam(templateParam);
		// 复制代码运行请自行打印 API 的返回值
		SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);

		return sendSmsResponse;
	}

	public static void main(String[] args) throws Exception {
		// SendSmsResponse sendMessage =
		// AliyunSmsApiRest.sendMessage("13488791523", "北京", "SMS_330",
		// "{\"code\":\"123321\"}");
	}

}