package com.bear.mcp.single.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class CalculatorTools {

    @Tool(name = "calculate", description = "执行基础四则运算。operator 支持 add、subtract、multiply、divide。")
    public Map<String, Object> calculate(
            @ToolParam(description = "左操作数") BigDecimal left,
            @ToolParam(description = "右操作数") BigDecimal right,
            @ToolParam(description = "操作符：add、subtract、multiply、divide") String operator) {
        BigDecimal result = switch (operator) {
            case "add" -> left.add(right);
            case "subtract" -> left.subtract(right);
            case "multiply" -> left.multiply(right);
            case "divide" -> {
                if (BigDecimal.ZERO.compareTo(right) == 0) {
                    throw new IllegalArgumentException("除数不能为 0");
                }
                yield left.divide(right, 8, RoundingMode.HALF_UP);
            }
            default -> throw new IllegalArgumentException("不支持的 operator: " + operator);
        };
        return Map.of("left", left, "right", right, "operator", operator, "result", result);
    }
}
