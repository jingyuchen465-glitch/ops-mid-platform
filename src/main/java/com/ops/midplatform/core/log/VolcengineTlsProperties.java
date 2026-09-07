package com.ops.midplatform.core.log;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 火山引擎日志服务（TLS）配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "bear.tls")
public class VolcengineTlsProperties {
    /** 日志服务访问地址。 */
    private String endpoint;
    /** 日志项目所在地域。 */
    private String region;
    /** IAM 用户 Access Key ID。 */
    private String accessKeyId;
    /** IAM 用户 Access Key Secret。 */
    private String accessKeySecret;
    /** 日志主题 ID。 */
    private String topicId;
}