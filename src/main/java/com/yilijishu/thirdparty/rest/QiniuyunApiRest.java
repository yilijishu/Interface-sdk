
package com.yilijishu.thirdparty.rest;

import java.util.UUID;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import com.yilijishu.thirdparty.bean.ApiRestResult;

/**
 * 七牛云工具类
 */
public class QiniuyunApiRest {

	private static String accessKey = "PTk6ArD8U3wo03tppoB23211233311qwqQ23AEEh";
	private static String secretKey = "PTk6ArD8U3wo03tppoB23211233311qwqQ23AEEh";
	private static String bucket = "DEMO";

	private QiniuyunApiRest(String accessKey, String secretKey, String bucket) {
		QiniuyunApiRest.accessKey = accessKey;
		QiniuyunApiRest.secretKey = secretKey;
		QiniuyunApiRest.bucket = bucket;
	}

	// 上传文件
	public static ApiRestResult upload2Qiniu(String filePath, String fileName) {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		// 构造一个带指定Zone对象的配置类
		Configuration cfg = new Configuration();
		UploadManager uploadManager = new UploadManager(cfg);
		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		try {
			Response response = uploadManager.put(filePath, fileName, upToken);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
		} catch (QiniuException ex) {
			Response r = ex.response;
			try {
				System.err.println(r.bodyString());
			} catch (QiniuException ex2) {
			}
			try {
				apiRestResult.setData(r.bodyString());
			} catch (QiniuException e) {
				e.printStackTrace();
			}
			apiRestResult.setCode(500);
			return apiRestResult;
		}
		apiRestResult.setCode(200);
		apiRestResult.setData(fileName);
		return apiRestResult;
	}

	// 上传文件
	public static ApiRestResult upload2Qiniu(byte[] bytes, String fileName) {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		// 构造一个带指定Zone对象的配置类
		Configuration cfg = new Configuration();
		// ...其他参数参考类注释
		UploadManager uploadManager = new UploadManager(cfg);
		// 默认不指定key的情况下，以文件内容的hash值作为文件名
		String key = fileName;
		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		try {
			Response response = uploadManager.put(bytes, key, upToken);
			// 解析上传成功的结果
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			// System.out.println(putRet.key);
			// System.out.println(putRet.hash);
			apiRestResult.setData(putRet.key);
		} catch (QiniuException ex) {
			Response r = ex.response;
			System.err.println(r.toString());
			try {
				System.err.println(r.bodyString());
			} catch (QiniuException ex2) {
				// ignore
			}
			apiRestResult.setCode(500);
			apiRestResult.setData(r.toString());
			return apiRestResult;
		}
		apiRestResult.setCode(200);
		return apiRestResult;
	}

	// 删除文件
	public static ApiRestResult deleteFileFromQiniu(String fileName) {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		// 构造一个带指定Zone对象的配置类
		Configuration cfg = new Configuration();
		String key = fileName;
		Auth auth = Auth.create(accessKey, secretKey);
		BucketManager bucketManager = new BucketManager(auth, cfg);
		try {
			bucketManager.delete(bucket, key);
		} catch (QiniuException ex) {
			// 如果遇到异常，说明删除失败
			System.err.println(ex.code());
			System.err.println(ex.response.toString());
			apiRestResult.setCode(500);
			apiRestResult.setData(ex.response.toString());
			return apiRestResult;
		}
		apiRestResult.setCode(200);
		apiRestResult.setData(fileName);
		return apiRestResult;
	}

	public static String transcoding(String name) {

		Auth auth = Auth.create(accessKey, secretKey);

		// String key = "ce104c91-7f82-493a-9ea6-71afae7e76c44.mp4";
		// 存储空间中视频的文件名称
		String newName = UUID.randomUUID().toString();
		// String newKey = "H264_type.mp4"; //转码后，另存的文件名称
		String pipeline = "default.sys"; // 处理队列

		Configuration cfg = new Configuration();

		String saveAs = UrlSafeBase64.encodeToString(bucket + ":" + newName); // saveas接口
																				// 参数
		String fops = "avthumb/mp4/vcodec/libx264|saveas/" + saveAs; // 处理命令
																		// avthumb
																		// 和
																		// saveas
																		// 通过管道符
																		// |
																		// 进行连接
		OperationManager operationMgr = new OperationManager(auth, cfg);

		try {
			// 执行转码和另存 操作
			String persistentId = operationMgr.pfop(bucket, name, fops, new StringMap().put("persistentPipeline", pipeline));
			System.out.println(persistentId);
		} catch (QiniuException e) {
			String errorCode = String.valueOf(e.response.statusCode);
			System.out.println(errorCode);
			e.printStackTrace();
		}
		return newName;
	}

	public static void main(String[] args) {
		QiniuyunApiRest q = new QiniuyunApiRest("PTk6ArD8U3wo03tppoB23211233311qwqQ23AEEh", "PTk6ArD8U3wo03tppoB23211233311qwqQ23AEEh", "1");
		// 上传
		// q.upload2Qiniu("D:/SD/宣传视频/西沙之约.mp4", "西沙之约");

		// 删除
		// q.deleteFileFromQiniu("西沙之约");

		// 转码
		// q.transcoding("西沙之约");

		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		// String a =
		// auth.privateDownloadUrl("https://qn.shenduwenlv.com/%E8%A5%BF%E6%B2%99%E4%B9%8B%E7%BA%A6");
		// System.out.println(a);
	}
}
