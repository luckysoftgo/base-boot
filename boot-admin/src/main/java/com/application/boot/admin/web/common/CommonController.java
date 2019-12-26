package com.application.boot.admin.web.common;

import com.alibaba.fastjson.JSON;
import com.application.boot.common.config.Global;
import com.application.boot.common.config.ServerConfig;
import com.application.boot.common.constant.Constants;
import com.application.boot.common.core.domain.AjaxResult;
import com.application.boot.common.utils.StringUtils;
import com.application.boot.common.utils.file.FileUploadUtils;
import com.application.boot.common.utils.file.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用请求处理
 * 
 * @author 孤狼
 */
@Controller
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("common/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            if (!FileUtils.isValidFilename(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = Global.getDownloadPath() + fileName;

            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition",
                    "attachment;fileName=" + FileUtils.setFileDownloadHeader(request, realFileName));
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求
     */
    @PostMapping("/common/upload")
    @ResponseBody
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = Global.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("fileName", fileName);
            ajax.put("url", url);
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/common/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        // 本地资源路径
        String localPath = Global.getProfile();
        // 数据库资源地址
        String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
        // 下载名称
        String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition",
                "attachment;fileName=" + FileUtils.setFileDownloadHeader(request, downloadName));
        FileUtils.writeBytes(downloadPath, response.getOutputStream());
    }
	
	/**
	 * 获取天眼查的信息.
	 * @param target
	 * @param companyName
	 * @param companyId
	 * @return
	 * @throws Exception
	 */
	public static String getInfoByTianYanCha(String url,String target,String companyName, String companyId){
		System.out.println("url="+url+",target="+target+",companyName="+companyName+",companyId="+companyId);
		String result=null;
		try {
			String baseURL = "v4/open/"+target+"?id={id}&name={name}&pageNum={pageNum}";
			System.out.println("baseURL="+baseURL);
			String serverUrl = baseURL.replace("{name}", urlEncodeURL(companyName)).replace("{id}", companyId).replace("{pageNum}","1");
			url = url + serverUrl;
			System.out.println("url="+url);
			result = getInfo(url);
			return result;
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * URLEncode处理
	 */
	public static String urlEncodeURL(String url) {
		try {
			String result = URLEncoder.encode(url, "UTF-8");
			//+实际上是 空格 url encode而来
			result = result.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("\\+", "%20");
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public static final Map<String, String> errMap = new HashMap<>();
	static {
		errMap.put("300001", "请求失败");
		errMap.put("300002", "账号失效");
		errMap.put("300003", "账号过期");
		errMap.put("300004", "访问频率过快");
		errMap.put("300005", "无权限访问此api");
		errMap.put("300006", "余额不足");
		errMap.put("300007", "剩余次数不足");
		errMap.put("300008", "缺少必要参数");
		errMap.put("300009", "账号信息有误");
		errMap.put("300010", "URL不存在");
	}
	
	public static String getInfo(String url, Map<String, String> params) throws UnsupportedEncodingException {
		int index = 0;
		for (String key : params.keySet()) {
			String value = params.get(key);
			if (index == 0) {
				url += "?" + key + "=" + URLEncoder.encode(value, "UTF-8");
				index = 1;
			} else {
				url += "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
			}
		}
		
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			String tyc_token = "4c00c062-3a13-4b3e-9e35-7ddce7f207b5";
			// 设置通用的请求属性
			connection.setRequestProperty("Authorization", tyc_token);
			// 建立实际的连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally { // 使用finally块来关闭输入流
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		System.out.println("天眼查返回结果信息：" + result);
		
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> resMap = JSON.parseObject(result, Map.class);
			String error_code = String.valueOf(resMap.get("error_code"));
			if (errMap.containsKey(error_code)) {
				System.out.println("结果状态为：" + errMap.get(error_code));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String getInfo(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("Authorization", "4c00c062-3a13-4b3e-9e35-7ddce7f207b5");
			// 建立实际的连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally { // 使用finally块来关闭输入流
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> resMap = JSON.parseObject(result, Map.class);
			String error_code = String.valueOf(resMap.get("error_code"));
			if (errMap.containsKey(error_code)) {
				System.out.println("结果状态为：" + errMap.get(error_code));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
}
