
package com.yilijishu.thirdparty.rest;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.CreateSmsTemplateRequest;
import com.aliyun.dysmsapi20170525.models.CreateSmsTemplateResponse;
import com.aliyun.dysmsapi20170525.models.DeleteSmsTemplateRequest;
import com.aliyun.dysmsapi20170525.models.DeleteSmsTemplateResponse;
import com.aliyun.dysmsapi20170525.models.GetSmsTemplateRequest;
import com.aliyun.dysmsapi20170525.models.GetSmsTemplateResponse;
import com.aliyun.dysmsapi20170525.models.QuerySmsTemplateListRequest;
import com.aliyun.dysmsapi20170525.models.QuerySmsTemplateListResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.UpdateSmsTemplateRequest;
import com.aliyun.dysmsapi20170525.models.UpdateSmsTemplateResponse;
import com.aliyun.teaopenapi.models.Config;
import com.yilijishu.thirdparty.bean.ApiRestResult;

/**
 * 阿里云短信服务工具类
 * <p>
 * 支持以下功能：
 * <ul>
 * <li>发送短信（SendSms）</li>
 * <li>申请短信模板（CreateSmsTemplate）</li>
 * <li>查询模板审核详情（GetSmsTemplate）</li>
 * <li>查询模板列表（QuerySmsTemplateList）</li>
 * <li>修改短信模板（UpdateSmsTemplate）</li>
 * <li>删除短信模板（DeleteSmsTemplate）</li>
 * </ul>
 */
public class AliyunSmsApiRest {

	private com.aliyun.dysmsapi20170525.Client aliYunClient = null;

	private String endpoint = "dysmsapi.aliyuncs.com";
	private String accessKey = "";
	private String secretKey = "";

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

	// ==================== 短信发送 ====================

	/**
	 * 发送短信
	 *
	 * @param contact
	 *            手机号码
	 * @param signName
	 *            短信签名
	 * @param templateCode
	 *            模板Code
	 * @param templateParam
	 *            模板变量参数（JSON格式）
	 * @return SendSmsResponse
	 * @throws Exception
	 */
	public SendSmsResponse sendMessage(String contact, String signName, String templateCode, String templateParam) throws Exception {
		if (contact == null || contact.isEmpty()) {
			return null;
		}
		Client client = getClient();
		SendSmsRequest sendSmsRequest = new SendSmsRequest().setPhoneNumbers(contact).setSignName(signName).setTemplateCode(templateCode).setTemplateParam(templateParam);
		return client.sendSms(sendSmsRequest);
	}

	// ==================== 短信模板管理 ====================

	/**
	 * 申请短信模板
	 * <p>
	 * 模板审核通过后才可以发送短信。一个自然日最多提交100次，建议每次间隔至少30秒。
	 *
	 * @param templateName
	 *            模板名称，长度不超过30个字符
	 * @param templateContent
	 *            模板内容，长度不超过500个字符，变量格式：${code}
	 * @param templateType
	 *            短信类型：0-验证码，1-短信通知，2-推广短信，3-国际/港澳台消息
	 * @param remark
	 *            申请说明，不超过100个字符（描述业务场景有助于审核通过）
	 * @param relatedSignName
	 *            关联签名名称（TemplateType为0/1/2时必填，关联签名需为审核通过的签名）
	 * @return ApiRestResult 包含模板Code和工单号
	 */
	public ApiRestResult<CreateSmsTemplateResponse> createSmsTemplate(String templateName, String templateContent, Integer templateType, String remark, String relatedSignName) {
		ApiRestResult<CreateSmsTemplateResponse> result = new ApiRestResult<>();
		try {
			CreateSmsTemplateRequest request = new CreateSmsTemplateRequest().setTemplateName(templateName).setTemplateContent(templateContent).setTemplateType(templateType).setRemark(remark)
					.setRelatedSignName(relatedSignName);
			CreateSmsTemplateResponse response = getClient().createSmsTemplate(request);
			if ("OK".equals(response.getBody().getCode())) {
				result.setCode(200);
				result.setData(response);
			} else {
				result.setCode(500);
			}
			result.setRawData(response.getBody().getMessage());
		} catch (Exception e) {
			result.setCode(500);
			result.setRawData("创建短信模板异常: " + e.getMessage());
		}
		return result;
	}

	/**
	 * 申请短信模板（完整参数版）
	 *
	 * @param templateName
	 *            模板名称
	 * @param templateContent
	 *            模板内容
	 * @param templateType
	 *            短信类型：0-验证码，1-通知，2-推广，3-国际/港澳台
	 * @param remark
	 *            申请说明
	 * @param relatedSignName
	 *            关联签名名称（0/1/2类型必填）
	 * @param templateRule
	 *            模板变量规则（JSON格式，如 {"code":"characterWithNumber"}）
	 * @param applySceneContent
	 *            业务场景链接
	 * @return ApiRestResult 包含模板Code和工单号
	 */
	public ApiRestResult<CreateSmsTemplateResponse> createSmsTemplate(String templateName, String templateContent, Integer templateType, String remark, String relatedSignName, String templateRule,
			String applySceneContent) {
		ApiRestResult<CreateSmsTemplateResponse> result = new ApiRestResult<>();
		try {
			CreateSmsTemplateRequest request = new CreateSmsTemplateRequest().setTemplateName(templateName).setTemplateContent(templateContent).setTemplateType(templateType).setRemark(remark)
					.setRelatedSignName(relatedSignName).setTemplateRule(templateRule).setApplySceneContent(applySceneContent);
			CreateSmsTemplateResponse response = getClient().createSmsTemplate(request);
			if ("OK".equals(response.getBody().getCode())) {
				result.setCode(200);
				result.setData(response);
			} else {
				result.setCode(500);
			}
			result.setRawData(response.getBody().getMessage());
		} catch (Exception e) {
			result.setCode(500);
			result.setRawData("创建短信模板异常: " + e.getMessage());
		}
		return result;
	}

	/**
	 * 查询模板审核详情
	 * <p>
	 * 通过模板Code查询单个模板的审核状态和详情。审核状态返回值：
	 * <ul>
	 * <li>0 - 审核中</li>
	 * <li>1 - 通过审核</li>
	 * <li>2 - 未通过审核（返回审核失败原因）</li>
	 * <li>10 - 取消审核</li>
	 * </ul>
	 *
	 * @param templateCode
	 *            短信模板Code（如 SMS_20375****）
	 * @return ApiRestResult 包含模板详细信息
	 */
	public ApiRestResult<GetSmsTemplateResponse> getSmsTemplate(String templateCode) {
		ApiRestResult<GetSmsTemplateResponse> result = new ApiRestResult<>();
		try {
			GetSmsTemplateRequest request = new GetSmsTemplateRequest().setTemplateCode(templateCode);
			GetSmsTemplateResponse response = getClient().getSmsTemplate(request);
			if ("OK".equals(response.getBody().getCode())) {
				result.setCode(200);
				result.setData(response);
			} else {
				result.setCode(500);
			}
			result.setRawData(response.getBody().getMessage());
		} catch (Exception e) {
			result.setCode(500);
			result.setRawData("查询短信模板异常: " + e.getMessage());
		}
		return result;
	}

	/**
	 * 查询模板列表
	 * <p>
	 * 查询当前账号下的所有短信模板，包括模板审核状态、模板类型、模板内容等。
	 *
	 * @param pageIndex
	 *            当前页码，默认1
	 * @param pageSize
	 *            每页显示个数，范围1~50，默认10
	 * @return ApiRestResult 包含模板列表
	 */
	public ApiRestResult<QuerySmsTemplateListResponse> querySmsTemplateList(Integer pageIndex, Integer pageSize) {
		ApiRestResult<QuerySmsTemplateListResponse> result = new ApiRestResult<>();
		try {
			QuerySmsTemplateListRequest request = new QuerySmsTemplateListRequest();
			if (pageIndex != null) {
				request.setPageIndex(pageIndex);
			}
			if (pageSize != null) {
				request.setPageSize(pageSize);
			}
			QuerySmsTemplateListResponse response = getClient().querySmsTemplateList(request);
			if ("OK".equals(response.getBody().getCode())) {
				result.setCode(200);
				result.setData(response);
			} else {
				result.setCode(500);
			}
			result.setRawData(response.getBody().getMessage());
		} catch (Exception e) {
			result.setCode(500);
			result.setRawData("查询短信模板列表异常: " + e.getMessage());
		}
		return result;
	}

	/**
	 * 查询模板列表（默认分页）
	 *
	 * @return ApiRestResult 包含模板列表（第1页，每页10条）
	 */
	public ApiRestResult<QuerySmsTemplateListResponse> querySmsTemplateList() {
		return querySmsTemplateList(1, 10);
	}

	/**
	 * 修改短信模板
	 * <p>
	 * 仅支持修改未通过审核的模板，修改后自动提交审核。
	 *
	 * @param templateCode
	 *            未通过审核的模板Code
	 * @param templateName
	 *            模板名称，长度不超过30个字符
	 * @param templateContent
	 *            模板内容，长度不超过500个字符
	 * @param templateType
	 *            短信类型：0-验证码，1-短信通知，2-推广短信，3-国际/港澳台消息
	 * @param remark
	 *            申请说明
	 * @param relatedSignName
	 *            关联签名名称
	 * @return ApiRestResult 包含修改后的模板Code和工单号
	 */
	public ApiRestResult<UpdateSmsTemplateResponse> updateSmsTemplate(String templateCode, String templateName, String templateContent, Integer templateType, String remark, String relatedSignName) {
		ApiRestResult<UpdateSmsTemplateResponse> result = new ApiRestResult<>();
		try {
			UpdateSmsTemplateRequest request = new UpdateSmsTemplateRequest().setTemplateCode(templateCode).setTemplateName(templateName).setTemplateContent(templateContent)
					.setTemplateType(templateType).setRemark(remark).setRelatedSignName(relatedSignName);
			UpdateSmsTemplateResponse response = getClient().updateSmsTemplate(request);
			if ("OK".equals(response.getBody().getCode())) {
				result.setCode(200);
				result.setData(response);
			} else {
				result.setCode(500);
			}
			result.setRawData(response.getBody().getMessage());
		} catch (Exception e) {
			result.setCode(500);
			result.setRawData("修改短信模板异常: " + e.getMessage());
		}
		return result;
	}

	/**
	 * 修改短信模板（完整参数版）
	 *
	 * @param templateCode
	 *            未通过审核的模板Code
	 * @param templateName
	 *            模板名称
	 * @param templateContent
	 *            模板内容
	 * @param templateType
	 *            短信类型：0-验证码，1-通知，2-推广，3-国际/港澳台
	 * @param remark
	 *            申请说明
	 * @param relatedSignName
	 *            关联签名名称
	 * @param templateRule
	 *            模板变量规则（JSON格式）
	 * @param applySceneContent
	 *            业务场景链接
	 * @return ApiRestResult 包含修改后的模板Code和工单号
	 */
	public ApiRestResult<UpdateSmsTemplateResponse> updateSmsTemplate(String templateCode, String templateName, String templateContent, Integer templateType, String remark, String relatedSignName,
			String templateRule, String applySceneContent) {
		ApiRestResult<UpdateSmsTemplateResponse> result = new ApiRestResult<>();
		try {
			UpdateSmsTemplateRequest request = new UpdateSmsTemplateRequest().setTemplateCode(templateCode).setTemplateName(templateName).setTemplateContent(templateContent)
					.setTemplateType(templateType).setRemark(remark).setRelatedSignName(relatedSignName).setTemplateRule(templateRule).setApplySceneContent(applySceneContent);
			UpdateSmsTemplateResponse response = getClient().updateSmsTemplate(request);
			if ("OK".equals(response.getBody().getCode())) {
				result.setCode(200);
				result.setData(response);
			} else {
				result.setCode(500);
			}
			result.setRawData(response.getBody().getMessage());
		} catch (Exception e) {
			result.setCode(500);
			result.setRawData("修改短信模板异常: " + e.getMessage());
		}
		return result;
	}

	/**
	 * 删除短信模板
	 * <p>
	 * 支持删除已撤回、审核失败或审核通过的模板，审核中的模板不支持删除。 删除后不可恢复，且不可再使用该模板发送短信。
	 *
	 * @param templateCode
	 *            短信模板Code
	 * @return ApiRestResult 包含已删除的模板Code
	 */
	public ApiRestResult<DeleteSmsTemplateResponse> deleteSmsTemplate(String templateCode) {
		ApiRestResult<DeleteSmsTemplateResponse> result = new ApiRestResult<>();
		try {
			DeleteSmsTemplateRequest request = new DeleteSmsTemplateRequest().setTemplateCode(templateCode);
			DeleteSmsTemplateResponse response = getClient().deleteSmsTemplate(request);
			if ("OK".equals(response.getBody().getCode())) {
				result.setCode(200);
				result.setData(response);
			} else {
				result.setCode(500);
			}
			result.setRawData(response.getBody().getMessage());
		} catch (Exception e) {
			result.setCode(500);
			result.setRawData("删除短信模板异常: " + e.getMessage());
		}
		return result;
	}

	public static void main(String[] args) {
		// AliyunSmsApiRest sms = new AliyunSmsApiRest();
		// ApiRestResult<QuerySmsTemplateListResponse> querySmsTemplateList =
		// sms.querySmsTemplateList(1, 11);
		// System.out.println(querySmsTemplateList);
	}

}
