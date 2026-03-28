package xyz.yaungyue.secondhand.controller;

import com.aliyun.sts20150401.models.AssumeRoleResponse;
import com.aliyun.sts20150401.models.AssumeRoleResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/oss")
@Tag(name = "OSS 文件上传", description = "阿里云 OSS 签名获取接口")
public class OssController {

    private static final Logger logger = LoggerFactory.getLogger(OssController.class);

    //OSS 基础信息 替换为实际的 bucket 名称、region-id、host。
    String bucket = "zijin-xianyu";
    String region = "cn-shanghai";
    String host = "http://zijin-xianyu.oss-cn-shanghai.aliyuncs.com";
    // 设置上传回调 URL（即回调服务器地址），必须为公网地址。用于处理应用服务器与 OSS 之间的通信，OSS 会在文件上传完成后，把文件上传信息通过此回调 URL 发送给应用服务器。
    String callbackUrl = "http://47.101.161.4:8080/api/oss/callback";// 设置回调请求的服务器地址，例如 http://oss-demo.aliyuncs.com:23450/callback。


    //指定过期时间，单位为秒。
    Long expire_time = 3600L;

    /**
     * 通过指定有效的时长（秒）生成过期时间。
     * @param seconds 有效时长（秒）。
     * @return ISO8601 时间字符串，如："2014-12-01T12:00:00.000Z"。
     */
    public static String generateExpiration(long seconds) {
        logger.info("[Step: GenerateExpiration] 开始生成过期时间，时长：{} 秒", seconds);
        // 获取当前时间戳（以秒为单位）
        long now = Instant.now().getEpochSecond();
        // 计算过期时间的时间戳
        long expirationTime = now + seconds;
        // 将时间戳转换为 Instant 对象，并格式化为 ISO8601 格式
        Instant instant = Instant.ofEpochSecond(expirationTime);
        // 定义时区为 UTC
        ZoneId zone = ZoneOffset.UTC;
        // 将 Instant 转换为 ZonedDateTime
        ZonedDateTime zonedDateTime = instant.atZone(zone);
        // 定义日期时间格式，例如 2023-12-03T13:00:00.000Z
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // 格式化日期时间
        String formattedDate = zonedDateTime.format(formatter);
        logger.info("[Step: GenerateExpiration] 生成的过期时间：{}", formattedDate);
        // 输出结果
        return formattedDate;
    }

    //初始化 STS Client
    public static com.aliyun.sts20150401.Client createStsClient() throws Exception {
        logger.info("[Step: CreateStsClient] 开始初始化 STS Client");
        // 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考。
        // 建议使用更安全的 STS 方式。
        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
        
        if (accessKeyId == null || accessKeySecret == null) {
            logger.error("[Step: CreateStsClient] 环境变量 OSS_ACCESS_KEY_ID 或 OSS_ACCESS_KEY_SECRET 未设置！");
            throw new RuntimeException("Missing OSS Access Key Environment Variables");
        }

        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 OSS_ACCESS_KEY_ID。
                .setAccessKeyId(accessKeyId)
                // 必填，请确保代码运行环境设置了环境变量 OSS_ACCESS_KEY_SECRET。
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/Sts
        config.endpoint = "sts.cn-shanghai.aliyuncs.com";
        logger.info("[Step: CreateStsClient] STS Client 初始化完成，Endpoint: {}", config.endpoint);
        return new com.aliyun.sts20150401.Client(config);
    }

    //获取 STS 临时凭证
    public static AssumeRoleResponseBody.AssumeRoleResponseBodyCredentials getCredential() throws Exception {
        logger.info("[Step: GetCredential] 开始获取 STS 临时凭证");
        String roleArn = System.getenv("OSS_STS_ROLE_ARN");
        logger.info("[Step: GetCredential] OSS_STS_ROLE_ARN: {}", roleArn);
        
        if (roleArn == null) {
            logger.error("[Step: GetCredential] 环境变量 OSS_STS_ROLE_ARN 未设置！");
            throw new RuntimeException("Missing OSS_STS_ROLE_ARN Environment Variable");
        }

        com.aliyun.sts20150401.Client client = OssController.createStsClient();
        com.aliyun.sts20150401.models.AssumeRoleRequest assumeRoleRequest = new com.aliyun.sts20150401.models.AssumeRoleRequest()
                // 必填，请确保代码运行环境设置了环境变量 OSS_STS_ROLE_ARN
                .setRoleArn(roleArn)
                .setRoleSessionName("secondhand-platform-session");// 自定义会话名称
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            logger.info("[Step: GetCredential] 正在调用 STS AssumeRole 接口...");
            // 复制代码运行请自行打印 API 的返回值
            AssumeRoleResponse response = client.assumeRoleWithOptions(assumeRoleRequest, runtime);
            logger.info("[Step: GetCredential] STS AssumeRole 调用成功");
            // credentials 里包含了后续要用到的 AccessKeyId、AccessKeySecret 和 SecurityToken。
            return response.body.credentials;
        } catch (TeaException error) {
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            logger.error("[Step: GetCredential] STS 调用失败 - 错误消息：{}", error.getMessage());
            // 诊断地址
            logger.error("[Step: GetCredential] STS 调用失败 - 建议：{}", error.getData() != null ? error.getData().get("Recommend") : "无建议信息");
            com.aliyun.teautil.Common.assertAsString(error.message);
        } catch (Exception e) {
            logger.error("[Step: GetCredential] 发生未知异常：{}", e.getMessage(), e);
        }
        // 返回一个默认的错误响应对象，避免返回 null
        logger.warn("[Step: GetCredential] 返回错误的默认凭证对象");
        AssumeRoleResponseBody.AssumeRoleResponseBodyCredentials defaultCredentials = new AssumeRoleResponseBody.AssumeRoleResponseBodyCredentials();
        defaultCredentials.accessKeyId = "ERROR_ACCESS_KEY_ID";
        defaultCredentials.accessKeySecret = "ERROR_ACCESS_KEY_SECRET";
        defaultCredentials.securityToken = "ERROR_SECURITY_TOKEN";
        return defaultCredentials;
    }

    /**
     * 获取 OSS 上传签名
     * 用于前端直传阿里云 OSS
     * @return 包含签名、policy、token 等信息的响应
     * @throws Exception 处理异常
     */
    @GetMapping("/get_post_signature_for_oss_upload")
    @Operation(summary = "获取 OSS 上传签名", description = "获取阿里云 OSS 直传所需的签名、policy 和临时凭证")
    public ApiResponse<Map<String, String>> getPostSignatureForOssUpload(
            @Parameter(description = "上传目录", example = "default")
            @RequestParam(value = "upload_dir", defaultValue = "default") String upload_dir) throws Exception {
        logger.info("================== [Start] 开始执行 getPostSignatureForOssUpload ==================");
        
        // 步骤 0: 获取凭证
        logger.info("[Step 0] 正在获取 STS 凭证...");
        AssumeRoleResponseBody.AssumeRoleResponseBodyCredentials sts_data = getCredential();

        String accesskeyid =  sts_data.accessKeyId;
        String accesskeysecret =  sts_data.accessKeySecret;
        String securitytoken =  sts_data.securityToken;

        if ("ERROR_ACCESS_KEY_ID".equals(accesskeyid)) {
            logger.error("[Step 0] 检测到凭证获取失败，终止后续签名计算");
            return ApiResponse.error(500, "Failed to retrieve STS credentials");
        }

        logger.info("[Step 0] STS 凭证获取成功 (AccessKeyId: {}...)", accesskeyid.substring(0, Math.min(10, accesskeyid.length())));

        //获取 x-oss-credential 里的 date，当前日期，格式为 yyyyMMdd
        logger.info("[Step 1] 正在计算时间参数 (Date & X-Oss-Date)...");
        ZonedDateTime today = ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String date = today.format(formatter);
        logger.info("[Step 1] Date (yyyyMMdd): {}", date);

        //获取 x-oss-date
        ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String x_oss_date = now.format(formatter2);
        logger.info("[Step 1] X-Oss-Date: {}", x_oss_date);

        // 步骤 2：创建 policy。
        logger.info("[Step 2] 正在构建 Policy...");
        String x_oss_credential = accesskeyid + "/" + date + "/" + region + "/oss/aliyun_v4_request";
        logger.info("[Step 2] X-Oss-Credential 前缀：{}/{}", accesskeyid, date);

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> policy = new HashMap<>();
        String expirationStr = generateExpiration(expire_time);
        policy.put("expiration", expirationStr);
        logger.info("[Step 2] Policy Expiration: {}", expirationStr);

        List<Object> conditions = new ArrayList<>();

        Map<String, String> bucketCondition = new HashMap<>();
        bucketCondition.put("bucket", bucket);
        conditions.add(bucketCondition);
        logger.info("[Step 2] Condition added: bucket={}", bucket);

        Map<String, String> securityTokenCondition = new HashMap<>();
        securityTokenCondition.put("x-oss-security-token", securitytoken);
        conditions.add(securityTokenCondition);
        logger.info("[Step 2] Condition added: x-oss-security-token");

        Map<String, String> signatureVersionCondition = new HashMap<>();
        signatureVersionCondition.put("x-oss-signature-version", "OSS4-HMAC-SHA256");
        conditions.add(signatureVersionCondition);
        logger.info("[Step 2] Condition added: x-oss-signature-version=OSS4-HMAC-SHA256");

        Map<String, String> credentialCondition = new HashMap<>();
        credentialCondition.put("x-oss-credential", x_oss_credential); // 替换为实际的 access key id
        conditions.add(credentialCondition);
        logger.info("[Step 2] Condition added: x-oss-credential");

        Map<String, String> dateCondition = new HashMap<>();
        dateCondition.put("x-oss-date", x_oss_date);
        conditions.add(dateCondition);
        logger.info("[Step 2] Condition added: x-oss-date");

        conditions.add(Arrays.asList("content-length-range", 1, 10240000));
        logger.info("[Step 2] Condition added: content-length-range (1-10MB)");
        
        conditions.add(Arrays.asList("eq", "$success_action_status", "200"));
        logger.info("[Step 2] Condition added: success_action_status=200");
        
        conditions.add(Arrays.asList("starts-with", "$key", upload_dir));
        logger.info("[Step 2] Condition added: key starts-with {}", upload_dir);

        policy.put("conditions", conditions);

        String jsonPolicy = mapper.writeValueAsString(policy);
        logger.info("[Step 2] Policy JSON 生成完成 (长度：{})", jsonPolicy.length());

        // 步骤 3：构造待签名字符串（StringToSign）。
        logger.info("[Step 3] 正在构造 StringToSign (Base64 Encode Policy)...");
        String stringToSign = new String(Base64.encodeBase64(jsonPolicy.getBytes()));
        logger.info("[Step 3] StringToSign (Base64): {}", stringToSign);

        // 步骤 4：计算 SigningKey。
        logger.info("[Step 4] 正在计算 SigningKey (HMAC-SHA256 Chain)...");
        logger.info("[Step 4] Key Part 1: aliyun_v4 + Secret");
        byte[] dateKey = hmacsha256(("aliyun_v4" + accesskeysecret).getBytes(), date);
        
        logger.info("[Step 4] Key Part 2: DateKey + Region ({})", region);
        byte[] dateRegionKey = hmacsha256(dateKey, region);
        
        logger.info("[Step 4] Key Part 3: RegionKey + Service (oss)");
        byte[] dateRegionServiceKey = hmacsha256(dateRegionKey, "oss");
        
        logger.info("[Step 4] Key Part 4: ServiceKey + Request (aliyun_v4_request)");
        byte[] signingKey = hmacsha256(dateRegionServiceKey, "aliyun_v4_request");
        logger.info("[Step 4] SigningKey 计算完成");

        // 步骤 5：计算 Signature。
        logger.info("[Step 5] 正在计算最终 Signature...");
        byte[] result = hmacsha256(signingKey, stringToSign);
        String signature = BinaryUtil.toHex(result);
        logger.info("[Step 5] 最终 Signature: {}", signature);

        // 步骤 6：设置回调。
        logger.info("[Step 6] 正在构建 Callback Body...");
        JSONObject jasonCallback = new JSONObject();
        jasonCallback.put("callbackUrl", callbackUrl);
        jasonCallback.put("callbackBody","filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
        jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
        String base64CallbackBody = BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
        logger.info("[Step 6] Callback Base64: {}", base64CallbackBody);

        Map<String, String> response = new HashMap<>();
        // 将数据添加到 map 中
        response.put("version", "OSS4-HMAC-SHA256");
        response.put("policy", stringToSign);
        response.put("x_oss_credential", x_oss_credential);
        response.put("x_oss_date", x_oss_date);
        response.put("signature", signature);
        response.put("security_token", securitytoken);
        response.put("dir", upload_dir);
        response.put("host", host);
        response.put("callback", base64CallbackBody);
        
        logger.info("================== [End] getPostSignatureForOssUpload 执行完成，返回响应 ==================");
        // 返回带有状态码 200 (OK) 的 ResponseEntity，返回给 Web 端，进行 PostObject 操作
        return ApiResponse.success(response);
    }

    /**
     * HMAC-SHA256 算法实现
     * @param key 密钥
     * @param data 待加密数据
     * @return 加密后的字节数组
     */
    public static byte[] hmacsha256(byte[] key, String data) {
        try {
            logger.debug("[HmacSHA256] 开始计算，Data 长度：{}", data.length());
            // 初始化 HMAC 密钥规格，指定算法为 HMAC-SHA256 并使用提供的密钥。
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");

            // 获取 Mac 实例，并通过 getInstance 方法指定使用 HMAC-SHA256 算法。
            Mac mac = Mac.getInstance("HmacSHA256");
            // 使用密钥初始化 Mac 对象。
            mac.init(secretKeySpec);

            // 执行 HMAC 计算，通过 doFinal 方法接收需要计算的数据并返回计算结果的数组。
            byte[] hmacBytes = mac.doFinal(data.getBytes());

            logger.debug("[HmacSHA256] 计算完成");
            return hmacBytes;
        } catch (Exception e) {
            logger.error("[HmacSHA256] 计算失败：{}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }
}