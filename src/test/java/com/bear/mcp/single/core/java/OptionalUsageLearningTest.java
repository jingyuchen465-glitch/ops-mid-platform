package com.bear.mcp.single.core.java;

import com.bear.mcp.single.core.auth.TokenAuthInfo;
import com.bear.mcp.single.core.dynamic.DynamicTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * Optional 的作用和基础用法演示。
 *
 * <p>Optional 最核心的作用，不是让代码看起来更高级，而是把“这个方法可能没有返回值”
 * 这件事明确写在方法返回类型里。</p>
 *
 * <p>如果一个方法返回 TokenAuthInfo，调用方容易默认它一定有值；如果返回 Optional&lt;TokenAuthInfo&gt;，
 * 调用方一眼就知道：Token 可能校验成功，也可能因为为空、过期、无效、用户被禁用而没有结果。</p>
 *
 * <p>在本项目里，Optional 主要适合下面两类场景：</p>
 *
 * <p>1. 查询可能查不到。比如 DynamicToolService#findEnabledByName，根据工具名查启用的动态工具，
 * 工具名为空、工具不存在、工具被禁用时，都可以返回 Optional.empty()。</p>
 *
 * <p>2. 校验可能失败。比如 TokenService#validate，Token 校验成功时返回 Optional.of(authInfo)，
 * 校验失败时返回 Optional.empty()，调用方再决定是继续处理，还是返回 401。</p>
 *
 * <p>Optional 能带来的好处：</p>
 *
 * <p>1. 减少随手返回 null 导致的 NullPointerException。</p>
 * <p>2. 让方法签名更清楚，调用方不用猜这个返回值会不会是 null。</p>
 * <p>3. 把“没有值时怎么办”集中写出来，比如给默认值、抛业务异常、跳过后续逻辑。</p>
 *
 * <p>但 Optional 不是什么地方都要用。一般不建议把 Optional 用在 Entity 字段、Req/Res 字段、
 * 方法参数里；这些地方用普通字段加校验规则更清楚。Optional 更适合用在 Service 层方法的返回值，
 * 表达“这次查询或校验可能没有结果”。</p>
 */
class OptionalUsageLearningTest {

    @Test
    @DisplayName("01 empty：表示没有值")
    void emptyMeansNoValue() {
        Optional<TokenAuthInfo> authInfo = Optional.empty();

        System.out.println("Optional 是否为空: " + authInfo.isEmpty());
        System.out.println("Optional 是否有值: " + authInfo.isPresent());
    }

    @Test
    @DisplayName("02 of：包装一个明确非空的值")
    void ofWrapsNonNullValue() {
        TokenAuthInfo info = new TokenAuthInfo(
                1L,
                10001L,
                "demo-admin",
                Set.of("ADMIN"),
                Set.of("calculator", "echo_dynamic"),
                Set.of(),
                Set.of()
        );

        Optional<TokenAuthInfo> optional = Optional.of(info);

        System.out.println("Optional 是否有值: " + optional.isPresent());
        System.out.println("Token 归属用户: " + optional.get().userName());
    }

    @Test
    @DisplayName("03 ofNullable：值可能为 null 时使用")
    void ofNullableAcceptsNull() {
        TokenAuthInfo nullableInfo = null;

        Optional<TokenAuthInfo> optional = Optional.ofNullable(nullableInfo);

        System.out.println("用 ofNullable 包装 null 后是否为空: " + optional.isEmpty());
    }

    @Test
    @DisplayName("04 get：只能在确认有值后使用")
    void getShouldBeUsedAfterCheckingPresent() {
        Optional<String> toolName = Optional.of("echo_dynamic");
        if (toolName.isPresent()) {
            System.out.println("确认有值后再 get: " + toolName.get());
        }

        try {
            Optional.empty().get();
        } catch (NoSuchElementException e) {
            System.out.println("直接 get 空 Optional 会报错: " + e.getClass().getSimpleName());
        }
    }

    @Test
    @DisplayName("05 orElse：没有值时给默认值")
    void orElseProvidesDefaultValue() {
        Optional<String> description = Optional.empty();

        String text = description.orElse("暂无描述");

        System.out.println("没有描述时使用默认值: " + text);
    }

    @Test
    @DisplayName("06 orElseGet：没有值时再执行默认值逻辑")
    void orElseGetRunsSupplierOnlyWhenEmpty() {
        Optional<String> exists = Optional.of("已发布动态工具");
        Optional<String> empty = Optional.empty();

        String existsText = exists.orElseGet(() -> buildDefaultDescription());
        String emptyText = empty.orElseGet(() -> buildDefaultDescription());

        System.out.println("有值时不会使用默认逻辑: " + existsText);
        System.out.println("没值时才执行默认逻辑: " + emptyText);
    }

    @Test
    @DisplayName("07 orElseThrow：没有值时抛出业务异常")
    void orElseThrowFailsFastWhenValueMissing() {
        Optional<DynamicTool> dynamicTool = Optional.empty();

        try {
            dynamicTool.orElseThrow(() -> new IllegalArgumentException("动态工具不存在或已禁用"));
        } catch (IllegalArgumentException e) {
            System.out.println("没有值时抛出业务异常: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("08 map：有值时转换，没有值时保持 empty")
    void mapTransformsValueWhenPresent() {
        DynamicTool tool = new DynamicTool(
                "echo_dynamic",
                "回显动态工具",
                "{}",
                "return params",
                List.of("demo_echo"),
                List.of(),
                List.of(),
                true
        );

        Optional<String> name = Optional.of(tool)
                .map(DynamicTool::name);

        Optional<String> missingName = Optional.<DynamicTool>empty()
                .map(DynamicTool::name);

        System.out.println("有工具时 map 出工具名: " + name.orElse("无工具名"));
        System.out.println("没有工具时 map 后仍然为空: " + missingName.isEmpty());
    }

    @Test
    @DisplayName("09 filter：有值但不满足条件时变成 empty")
    void filterKeepsValueOnlyWhenMatched() {
        DynamicTool tool = new DynamicTool(
                "echo_dynamic",
                "回显动态工具",
                "{}",
                "return params",
                List.of("demo_echo"),
                List.of(),
                List.of(),
                true
        );

        Optional<DynamicTool> enabledTool = Optional.of(tool)
                .filter(DynamicTool::enabled);

        Optional<DynamicTool> builtinTool = Optional.of(tool)
                .filter(item -> item.name().equals("calculator"));

        System.out.println("启用工具通过 filter 后是否还有值: " + enabledTool.isPresent());
        System.out.println("工具名不匹配时 filter 后是否为空: " + builtinTool.isEmpty());
    }

    @Test
    @DisplayName("10 ifPresent：有值时执行一段逻辑")
    void ifPresentRunsActionWhenValueExists() {
        Optional<String> toolName = Optional.of("echo_dynamic");
        StringBuilder builder = new StringBuilder();

        toolName.ifPresent(name -> builder.append("工具名: ").append(name));

        System.out.println("ifPresent 执行结果: " + builder);
    }

    private String buildDefaultDescription() {
        return "默认工具描述";
    }
}
