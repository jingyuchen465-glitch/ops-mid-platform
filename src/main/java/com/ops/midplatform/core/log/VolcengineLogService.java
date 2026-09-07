package com.ops.midplatform.core.log;

import com.volcengine.model.tls.ClientBuilder;
import com.volcengine.model.tls.ClientConfig;
import com.volcengine.model.tls.request.SearchLogsRequest;
import com.volcengine.model.tls.response.SearchLogsResponseV2;
import com.volcengine.service.tls.TLSLogClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 火山引擎日志服务查询封装。 */
@Service
public class VolcengineLogService {

    /** 单次返回日志条数上限，避免一次性向大模型返回过多日志。 */
    private static final int MAX_LIMIT = 100;

    private final VolcengineTlsProperties properties;

    public VolcengineLogService(VolcengineTlsProperties properties) {
        this.properties = properties;
    }

    public SearchLogsResponseV2 search(String query, long startTime, long endTime, int limit) throws Exception {
        validateConfig();
        SearchLogsRequest request = new SearchLogsRequest();
        request.setTopicId(properties.getTopicId());
        request.setQuery(query);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setLimit(Math.min(limit, MAX_LIMIT));
        return client().searchLogsV2(request);
    }

    private TLSLogClient client() throws Exception {
        validateConfig();
        return ClientBuilder.newClient(new ClientConfig(
                properties.getEndpoint(),
                properties.getRegion(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                ""
        ));
    }

    private void validateConfig() {
        if (!StringUtils.hasText(properties.getEndpoint())
                || !StringUtils.hasText(properties.getRegion())
                || !StringUtils.hasText(properties.getAccessKeyId())
                || !StringUtils.hasText(properties.getAccessKeySecret())
                || !StringUtils.hasText(properties.getTopicId())) {
            throw new IllegalStateException(
                    "火山日志配置不完整，请配置 BEAR_TLS_ENDPOINT/REGION/ACCESS_KEY_ID/ACCESS_KEY_SECRET/TOPIC_ID");
        }
    }
}