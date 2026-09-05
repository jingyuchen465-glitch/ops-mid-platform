package com.ops.midplatform.core.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 火山 TOS 配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "bear.storage.tos")
public class TosStorageProperties {
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private Long uploadExpireSeconds = 600L;
}
