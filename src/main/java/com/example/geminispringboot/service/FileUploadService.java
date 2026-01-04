package com.example.geminispringboot.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileUploadService {


    private static File convertToTempFile(MultipartFile multipartFile) throws IOException {
        // 创建临时文件，使用原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        String safeFilename = originalFilename != null ?
                originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "upload_file";

        File tempFile = File.createTempFile(
                "upload_" + System.currentTimeMillis() + "_",
                "_" + safeFilename
        );

        // 将MultipartFile内容写入临时文件
        multipartFile.transferTo(tempFile);

        return tempFile;
    }

    /**
     * 文件上传接口
     * 
     * @param uploadUrl 上传地址
     * @param enterpriseCode 企业编码
     * @param signature 签名
     * @param timestamp 时间戳
     * @return 包含响应码和消息的Map对象
     */
    public  Map<String, Object> uploadFile(String uploadUrl, MultipartFile file,
                                                 String enterpriseCode, 
                                                 String signature, 
                                                 String timestamp) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 验证必填参数
        if (uploadUrl == null || uploadUrl.isEmpty()) {
            result.put("code", 400);
            result.put("message", "上传地址不能为空");
            return result;
        }
        

        
        // 验证文件是否存在
        File uploadFile = null;
        try {
            uploadFile = convertToTempFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uploadUrl);
        
        try {
            // 配置请求超时时间
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(60 * 1000)        // 连接超时：60秒
                    .setSocketTimeout(300 * 1000)        // 读取超时：300秒
                    .setConnectionRequestTimeout(60 * 1000) // 请求队列超时：60秒
                    .build();
            httpPost.setConfig(requestConfig);
            
            // 构建Multipart表单上传体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(StandardCharsets.UTF_8)
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            
            // 添加文件
            builder.addPart("fileData", new FileBody(uploadFile, ContentType.DEFAULT_BINARY, uploadFile.getName()));
            
            // 添加其他参数（如果提供了值）
            if (enterpriseCode != null && !enterpriseCode.isEmpty()) {
                builder.addTextBody("enterpriseCode", enterpriseCode);
            }
            
            if (signature != null && !signature.isEmpty()) {
                builder.addTextBody("signature", signature);
            }
            
            if (timestamp != null && !timestamp.isEmpty()) {
                builder.addTextBody("timestamp", timestamp);
            }
            
            // 封装请求体并执行请求
            org.apache.http.HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            
            // 获取响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            
            // 处理响应结果
            if (statusCode == 200) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                result.put("code", 200);
                result.put("message", "上传成功");
                result.put("data", responseBody);
            } else {
                String errorResponse = "";
                try {
                    errorResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    errorResponse = "无法读取错误响应体";
                }
                result.put("code", statusCode);
                result.put("message", "上传失败，服务器返回状态码: " + statusCode);
                result.put("errorDetail", errorResponse);
            }
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "上传异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                // 关闭异常不返回给调用方，仅打印日志
                e.printStackTrace();
            }
        }
        
        return result;
    }


}