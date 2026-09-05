package com.ops.midplatform.core.groovy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks secrets observed during one script execution and removes them from outward-facing data. */
final class SensitiveValueRedactor {

    private static final String REDACTED = "***";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "appsecret", "tenantaccesstoken", "tenant_access_token", "accesstoken", "access_token"
    );

    private final ObjectMapper objectMapper;
    private final Set<String> sensitiveValues = ConcurrentHashMap.newKeySet();

    SensitiveValueRedactor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void register(String key, Object value) {
        if (isSensitiveKey(key) && value != null && !String.valueOf(value).isBlank()) {
            sensitiveValues.add(String.valueOf(value));
        }
    }

    void registerStructured(Object value) {
        registerStructured(value, null);
    }

    Object redact(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> redacted = new LinkedHashMap<>();
            map.forEach((key, item) -> {
                String name = String.valueOf(key);
                redacted.put(name, isSensitiveKey(name) ? REDACTED : redact(item));
            });
            return redacted;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::redact).toList();
        }
        if (value.getClass().isArray()) {
            List<Object> redacted = new ArrayList<>();
            for (int index = 0; index < Array.getLength(value); index++) {
                redacted.add(redact(Array.get(value, index)));
            }
            return redacted;
        }
        if (value instanceof CharSequence) {
            return redactText(String.valueOf(value));
        }
        return value;
    }

    String redactText(String value) {
        if (value == null) {
            return null;
        }
        String redacted = value;
        List<String> ordered = sensitiveValues.stream()
                .filter(item -> !item.isBlank())
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
        for (String sensitiveValue : ordered) {
            redacted = redacted.replace(sensitiveValue, REDACTED);
        }
        return redacted;
    }

    private void registerStructured(Object value, String key) {
        if (value == null) {
            return;
        }
        if (value instanceof Map<?, ?> map) {
            map.forEach((mapKey, item) -> registerStructured(item, String.valueOf(mapKey)));
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> registerStructured(item, key));
            return;
        }
        if (value instanceof CharSequence text) {
            register(key, text);
            String candidate = text.toString().trim();
            if ((candidate.startsWith("{") && candidate.endsWith("}"))
                    || (candidate.startsWith("[") && candidate.endsWith("]"))) {
                try {
                    Object parsed = objectMapper.readValue(candidate, new TypeReference<Object>() {
                    });
                    registerStructured(parsed, key);
                } catch (Exception ignored) {
                    // Ordinary text response; there is no nested JSON to inspect.
                }
            }
            return;
        }
        register(key, value);
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        return SENSITIVE_KEYS.contains(key.replace("-", "")
                .replace("_", "")
                .toLowerCase(Locale.ROOT));
    }
}
