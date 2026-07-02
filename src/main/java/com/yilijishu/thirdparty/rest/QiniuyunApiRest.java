
package com.yilijishu.thirdparty.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import com.yilijishu.thirdparty.bean.ApiRestResult;

/**
 * 七牛云工具类 支持普通上传和分片上传（断点续传），解决大文件上传超时问题
 */
public class QiniuyunApiRest {

	private String accessKey = "PTk6ArD8U3wo03tppoB23211233311qwqQ23AEEh";
	private String secretKey = "PTk6ArD8U3wo03tppoB23211233311qwqQ23AEEh";
	private String bucket = "DEMO";

	/** 分片上传文件大小阈值（字节），超过此大小使用分片上传，默认 4MB */
	private static final long SLICE_UPLOAD_THRESHOLD = 4 * 1024 * 1024;

	public QiniuyunApiRest(String accessKey, String secretKey, String bucket) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.bucket = bucket;
	}

	/**
	 * 上传文件（自动选择上传方式）
	 *
	 * @param filePath
	 *            文件路径
	 * @param fileName
	 *            文件名（可包含路径）
	 * @return 上传结果
	 */
	public ApiRestResult upload2Qiniu(String filePath, String fileName) {
		File file = new File(filePath);
		if (file.exists() && file.length() > SLICE_UPLOAD_THRESHOLD) {
			return uploadLargeFile(filePath, fileName);
		}
		return uploadSmallFile(filePath, fileName);
	}

	/**
	 * 小文件上传（普通上传方式）
	 *
	 * @param filePath
	 *            文件路径
	 * @param fileName
	 *            文件名
	 * @return 上传结果
	 */
	public ApiRestResult uploadSmallFile(String filePath, String fileName) {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		Configuration cfg = new Configuration();
		UploadManager uploadManager = new UploadManager(cfg);
		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		try {
			Response response = uploadManager.put(filePath, fileName, upToken);
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			apiRestResult.setCode(200);
			apiRestResult.setData(putRet.key);
		} catch (QiniuException ex) {
			handleQiniuException(apiRestResult, ex);
		}
		return apiRestResult;
	}

	/**
	 * 大文件分片上传（断点续传），解决超时问题
	 *
	 * @param filePath
	 *            文件路径
	 * @param fileName
	 *            文件名
	 * @return 上传结果
	 */
	public ApiRestResult uploadLargeFile(String filePath, String fileName) {
		File file = new File(filePath);
		return uploadLargeFile(file, fileName);
	}

	/**
	 * 大文件分片上传（断点续传），支持 File 对象
	 *
	 * @param file
	 *            File对象
	 * @param fileName
	 *            文件名
	 * @return 上传结果
	 */
	public ApiRestResult uploadLargeFile(File file, String fileName) {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		if (file == null || !file.exists()) {
			apiRestResult.setCode(500);
			apiRestResult.setRawData("File not found");
			return apiRestResult;
		}

		String key = fileName;

		// 使用自动区域
		Configuration cfg = new Configuration(Zone.autoZone());
		Auth auth = Auth.create(accessKey, secretKey);

		try {
			// 设置断点续传记录目录
			String recorderDir = System.getProperty("java.io.tmpdir");
			FileRecorder recorder = new FileRecorder(recorderDir);

			// 创建分片上传管理器（传入 recorder 实现断点续传）
			UploadManager uploadManager = new UploadManager(cfg, recorder);

			String upToken = auth.uploadToken(bucket);

			// 使用 putFile 自动分片上传
			Response response = uploadManager.put(file, key, upToken, null, null, false);

			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			apiRestResult.setCode(200);
			apiRestResult.setData(putRet.key);
		} catch (QiniuException ex) {
			handleQiniuException(apiRestResult, ex);
		} catch (IOException e) {
			apiRestResult.setCode(500);
			apiRestResult.setRawData("IO Error: " + e.getMessage());
		}
		return apiRestResult;
	}

	/**
	 * 大文件分片上传（断点续传），支持 File 对象和额外参数
	 *
	 * @param file
	 *            File对象
	 * @param fileName
	 *            文件名
	 * @param extra
	 *            额外参数（可为null）
	 * @return 上传结果
	 */
	public ApiRestResult uploadLargeFile(File file, String fileName, Object extra) {
		// 忽略 extra 参数，直接调用核心方法
		return uploadLargeFile(file, fileName);
	}

	/**
	 * 使用字节数组上传（自动选择上传方式）
	 *
	 * @param bytes
	 *            文件字节数组
	 * @param fileName
	 *            文件名
	 * @return 上传结果
	 * @throws IOException
	 */
	public ApiRestResult upload2Qiniu(byte[] bytes, String fileName) throws IOException {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		if (bytes == null || bytes.length == 0) {
			apiRestResult.setCode(500);
			apiRestResult.setRawData("Empty file data");
			return apiRestResult;
		}

		// 根据文件大小选择上传方式
		if (bytes.length > SLICE_UPLOAD_THRESHOLD) {
			return uploadBytesSlice(bytes, fileName);
		}

		Configuration cfg = new Configuration();
		UploadManager uploadManager = new UploadManager(cfg);
		String key = fileName;
		Auth auth = Auth.create(accessKey, secretKey);
		String upToken = auth.uploadToken(bucket);
		try {
			Response response = uploadManager.put(bytes, key, upToken);
			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			apiRestResult.setCode(200);
			apiRestResult.setData(putRet.key);
		} catch (QiniuException ex) {
			handleQiniuException(apiRestResult, ex);
		}
		return apiRestResult;
	}

	/**
	 * 字节数组分片上传（用于大文件）
	 *
	 * @param bytes
	 *            文件字节数组
	 * @param fileName
	 *            文件名
	 * @return 上传结果
	 * @throws IOException
	 */
	private ApiRestResult<String> uploadBytesSlice(byte[] bytes, String fileName) throws IOException {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		String key = fileName;

		Configuration cfg = new Configuration(Zone.autoZone());
		Auth auth = Auth.create(accessKey, secretKey);

		try {
			// 设置断点续传记录目录
			String recorderDir = System.getProperty("java.io.tmpdir");
			FileRecorder recorder = new FileRecorder(recorderDir);

			// 创建分片上传管理器
			UploadManager uploadManager = new UploadManager(cfg, recorder);

			String upToken = auth.uploadToken(bucket);

			// 使用字节数组上传，put 方法支持自动分片
			Response response = uploadManager.put(bytes, key, upToken, null, null, false);

			DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
			apiRestResult.setCode(200);
			apiRestResult.setData(putRet.key);
		} catch (QiniuException ex) {
			handleQiniuException(apiRestResult, ex);
		}
		return apiRestResult;
	}

	/**
	 * 使用输入流上传
	 *
	 * @param inputStream
	 *            输入流
	 * @param fileSize
	 *            文件大小
	 * @param fileName
	 *            文件名
	 * @return 上传结果
	 * @throws IOException
	 */
	public ApiRestResult upload2Qiniu(InputStream inputStream, long fileSize, String fileName) throws IOException {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		if (inputStream == null) {
			apiRestResult.setCode(500);
			apiRestResult.setRawData("Empty input stream");
			return apiRestResult;
		}

		String key = fileName;

		// 根据文件大小选择上传方式
		if (fileSize > SLICE_UPLOAD_THRESHOLD) {
			// 大文件分片上传
			Configuration cfg = new Configuration(Zone.autoZone());
			Auth auth = Auth.create(accessKey, secretKey);

			try {
				String recorderDir = System.getProperty("java.io.tmpdir");
				FileRecorder recorder = new FileRecorder(recorderDir);
				UploadManager uploadManager = new UploadManager(cfg, recorder);

				String upToken = auth.uploadToken(bucket);

				// 将输入流写入临时文件，然后分片上传
				File tempFile = inputStreamToFile(inputStream, fileSize);
				if (tempFile != null) {
					Response response = uploadManager.put(tempFile, key, upToken, null, null, false);
					DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
					apiRestResult.setCode(200);
					apiRestResult.setData(putRet.key);
					// 删除临时文件
					tempFile.delete();
				} else {
					apiRestResult.setCode(500);
					apiRestResult.setRawData("Failed to create temp file");
				}
			} catch (QiniuException ex) {
				handleQiniuException(apiRestResult, ex);
			}
		} else {
			// 小文件直接上传
			Configuration cfg = new Configuration();
			UploadManager uploadManager = new UploadManager(cfg);
			Auth auth = Auth.create(accessKey, secretKey);
			String upToken = auth.uploadToken(bucket);
			try {
				byte[] bytes = inputStream.readAllBytes();
				Response response = uploadManager.put(bytes, key, upToken);
				DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
				apiRestResult.setCode(200);
				apiRestResult.setData(putRet.key);
			} catch (QiniuException ex) {
				handleQiniuException(apiRestResult, ex);
			} catch (IOException e) {
				apiRestResult.setCode(500);
				apiRestResult.setRawData("IO Error: " + e.getMessage());
			}
		}
		return apiRestResult;
	}

	/**
	 * 将输入流转换为临时文件
	 */
	private File inputStreamToFile(InputStream inputStream, long fileSize) {
		File tempFile = null;
		java.io.FileOutputStream fos = null;
		try {
			tempFile = File.createTempFile("qiniu_upload_", ".tmp");
			fos = new java.io.FileOutputStream(tempFile);
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
			fos.flush();
			return tempFile;
		} catch (IOException e) {
			if (tempFile != null) {
				tempFile.delete();
			}
			return null;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * 删除文件
	 *
	 * @param fileName
	 *            文件名
	 * @return 删除结果
	 */
	public ApiRestResult deleteFileFromQiniu(String fileName) {
		ApiRestResult<String> apiRestResult = new ApiRestResult<>();
		Configuration cfg = new Configuration();
		String key = fileName;
		Auth auth = Auth.create(accessKey, secretKey);
		BucketManager bucketManager = new BucketManager(auth, cfg);
		try {
			bucketManager.delete(bucket, key);
		} catch (QiniuException ex) {
			apiRestResult.setCode(500);
			apiRestResult.setRawData(ex.response.toString());
			return apiRestResult;
		}
		apiRestResult.setCode(200);
		apiRestResult.setData(fileName);
		return apiRestResult;
	}

	/**
	 * 查询文件信息
	 *
	 * @param fileName
	 *            文件名
	 * @return 文件信息
	 */
	public ApiRestResult<FileInfo> getFileInfo(String fileName) {
		ApiRestResult<FileInfo> apiRestResult = new ApiRestResult<>();
		Configuration cfg = new Configuration();
		Auth auth = Auth.create(accessKey, secretKey);
		BucketManager bucketManager = new BucketManager(auth, cfg);
		try {
			FileInfo fileInfo = bucketManager.stat(bucket, fileName);
			apiRestResult.setCode(200);
			apiRestResult.setData(fileInfo);
		} catch (QiniuException ex) {
			apiRestResult.setCode(500);
			apiRestResult.setRawData(ex.response != null ? ex.response.toString() : ex.getMessage());
		}
		return apiRestResult;
	}

	/**
	 * 视频转码
	 *
	 * @param name
	 *            源文件名
	 * @return 转码后的文件名
	 */
	public String transcoding(String name) {
		Auth auth = Auth.create(accessKey, secretKey);
		String newName = UUID.randomUUID().toString();
		String pipeline = "default.sys";
		Configuration cfg = new Configuration();
		String saveAs = UrlSafeBase64.encodeToString(bucket + ":" + newName);
		String fops = "avthumb/mp4/vcodec/libx264|saveas/" + saveAs;
		OperationManager operationMgr = new OperationManager(auth, cfg);
		try {
			String persistentId = operationMgr.pfop(bucket, name, fops, new StringMap().put("persistentPipeline", pipeline));
			System.out.println(persistentId);
		} catch (QiniuException e) {
			System.out.println(e.response.statusCode);
			e.printStackTrace();
		}
		return newName;
	}

	/**
	 * 获取上传凭证
	 *
	 * @return 上传Token
	 */
	public String getUploadToken() {
		Auth auth = Auth.create(accessKey, secretKey);
		return auth.uploadToken(bucket);
	}

	/**
	 * 获取公开空间的文件访问URL
	 *
	 * @param fileName
	 *            文件名
	 * @param domain
	 *            访问域名
	 * @return 访问URL
	 */
	public String getPublicUrl(String fileName, String domain) {
		return "https://" + domain + "/" + fileName;
	}

	/**
	 * 获取私有空间的文件访问URL（带过期时间）
	 *
	 * @param fileName
	 *            文件名
	 * @param domain
	 *            访问域名
	 * @param expireSeconds
	 *            过期秒数
	 * @return 私有访问URL
	 */
	public String getPrivateUrl(String fileName, String domain, long expireSeconds) {
		Auth auth = Auth.create(accessKey, secretKey);
		return auth.privateDownloadUrl("https://" + domain + "/" + fileName, expireSeconds);
	}

	// ==================== 私有方法 ====================

	/**
	 * 处理七牛异常
	 */
	private void handleQiniuException(ApiRestResult apiRestResult, QiniuException ex) {
		Response r = ex.response;
		if (r != null) {
			try {
				apiRestResult.setRawData(r.bodyString());
			} catch (QiniuException e) {
				apiRestResult.setRawData(r.toString());
			}
			apiRestResult.setCode(r.statusCode);
		} else {
			apiRestResult.setCode(500);
			apiRestResult.setRawData(ex.getMessage());
		}
	}

	// ==================== 内部类 ====================

	/**
	 * 上传额外参数
	 */
	public class UploadExtra {

		private String mimeType;
		private StringMap params;
		private String crc32;

		public String getMimeType() {
			return mimeType;
		}

		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		public StringMap getParams() {
			return params;
		}

		public void setParams(StringMap params) {
			this.params = params;
		}

		public String getCrc32() {
			return crc32;
		}

		public void setCrc32(String crc32) {
			this.crc32 = crc32;
		}
	}

}
