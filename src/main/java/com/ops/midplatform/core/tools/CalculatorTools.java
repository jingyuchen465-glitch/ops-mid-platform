package com.ops.midplatform.core.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class CalculatorTools {

    /**
     * 内置计算器工具。
     *
     * <p>被 @Tool 标记后，Spring AI MCP Server 会把它注册成 MCP Tool。
     * 只要 tools/list 过滤后允许返回 calculate，AI Client 就能看到这个工具描述和参数。</p>
     */
    @Tool(name = "calculate", description = "执行基础四则运算。operator 支持 add、subtract、multiply、divide。")
    public Map<String, Object> calculate(
            @ToolParam(description = "左操作数") BigDecimal left,
            @ToolParam(description = "右操作数") BigDecimal right,
            @ToolParam(description = "操作符：add、subtract、multiply、divide") String operator) {
        /*
         * BigDecimal 用于避免 double 浮点精度问题。
         */
        BigDecimal result = switch (operator) {
            case "add" -> left.add(right);
            case "subtract" -> left.subtract(right);
            case "multiply" -> left.multiply(right);
            case "divide" -> {
                if (BigDecimal.ZERO.compareTo(right) == 0) {
                    throw new IllegalArgumentException("除数不能为 0");
                }

                /*
                 * 除法可能出现无限小数，必须指定精度和舍入方式。
                 */
                yield left.divide(right, 8, RoundingMode.HALF_UP);
            }
            default -> throw new IllegalArgumentException("不支持的 operator: " + operator);
        };
        return Map.of("left", left, "right", right, "operator", operator, "result", result);
    }
}
