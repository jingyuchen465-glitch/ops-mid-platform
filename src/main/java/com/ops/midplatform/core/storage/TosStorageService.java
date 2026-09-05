package com.ops.midplatform.core.storage;

import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.comm.HttpMethod;
import com.volcengine.tos.model.object.GetObjectV2Input;
import com.volcengine.tos.model.object.GetObjectV2Output;
import com.volcengine.tos.model.object.PreSignedURLInput;
import com.volcengine.tos.model.object.PreSignedURLOutput;
import com.volcengine.tos.model.object.PutObjectInput;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/** 课堂版 Resource 使用的火山 TOS 服务。 */
@Service
public class TosStorageService {
    private final TosStorageProperties properties;

    public TosStorageService(TosStorageProperties properties) {
        this.properties = properties;
    }

    public PresignResult presignPut(String objectKey) {
        validateConfig();
        PreSignedURLInput input = PreSignedURLInput.builder()
                .bucket(properties.getBucket())
                .key(normalizeObjectKey(objectKey))
                .httpMethod(HttpMethod.PUT)
                .expires(properties.getUploadExpireSeconds())
                .build();
        PreSignedURLOutput output = client().preSignedURL(input);
        return new PresignResult(output.getSignedUrl(), objectKey, properties.getUploadExpireSeconds());
    }

    public PresignResult presignGet(String objectKey) {
        validateConfig();
        PreSignedURLInput input = PreSignedURLInput.builder()
                .bucket(properties.getBucket())
                .key(normalizeObjectKey(objectKey))
                .httpMethod(HttpMethod.GET)
                .expires(properties.getUploadExpireSeconds())
                .build();
        PreSignedURLOutput output = client().preSignedURL(input);
        return new PresignResult(output.getSignedUrl(), objectKey, properties.getUploadExpireSeconds());
    }

    public String downloadString(String objectKey) {
        validateConfig();
        GetObjectV2Input input = new GetObjectV2Input()
                .setBucket(properties.getBucket())
                .setKey(normalizeObjectKey(objectKey));
        try (GetObjectV2Output output = client().getObject(input)) {
            if (output.getContent() == null) {
                return "";
            }
            return new String(output.getContent().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("TOS 读取 Resource 失败: " + e.getMessage(), e);
        }
    }

    public void uploadString(String objectKey, String content) {
        validateConfig();
        byte[] bytes = (content != null ? content : "").getBytes(StandardCharsets.UTF_8);
        PutObjectInput input = new PutObjectInput()
                .setBucket(properties.getBucket())
                .setKey(normalizeObjectKey(objectKey))
                .setContent(new ByteArrayInputStream(bytes))
                .setContentLength(bytes.length);
        client().putObject(input);
    }

    public String downloadStringQuietly(String objectKey) {
        try {
            return downloadString(objectKey);
        } catch (Exception e) {
            return null;
        }
    }

    private TOSV2 client() {
        return new TOSV2ClientBuilder().build(
                properties.getRegion(),
                stripScheme(properties.getEndpoint()),
                properties.getAccessKey(),
                properties.getSecretKey()
        );
    }

    private void validateConfig() {
        if (!StringUtils.hasText(properties.getEndpoint())
                || !StringUtils.hasText(properties.getRegion())
                || !StringUtils.hasText(properties.getBucket())
                || !StringUtils.hasText(properties.getAccessKey())
                || !StringUtils.hasText(properties.getSecretKey())) {
            throw new IllegalStateException("TOS 配置不完整，请配置 BEAR_TOS_ENDPOINT/REGION/BUCKET/ACCESS_KEY/SECRET_KEY");
        }
    }

    private String normalizeObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("objectKey 不能为空");
        }
        String key = objectKey.trim().replace('\\', '/');
        if (key.startsWith("/") || key.contains("..")) {
            throw new IllegalArgumentException("objectKey 不合法");
        }
        return key;
    }

    private String stripScheme(String endpoint) {
        String value = endpoint.trim();
        if (value.startsWith("https://")) {
            return value.substring(8);
        }
        if (value.startsWith("http://")) {
            return value.substring(7);
        }
        return value;
    }

    public record PresignResult(String uploadUrl, String objectKey, Long expires) {
    }
}
