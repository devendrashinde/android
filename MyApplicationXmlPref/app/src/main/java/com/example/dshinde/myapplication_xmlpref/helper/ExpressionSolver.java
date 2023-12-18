package com.example.dshinde.myapplication_xmlpref.helper;

import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;

import java.util.Map;

public class ExpressionSolver {
    public static String evaluateExpression(String[] expression, Map<String, String> data) {
        String lastValue = null;
        boolean numericExp = expression.length > 1;
        for (int index = 0; index < expression.length; index++) {
            if(numericExp) {
                if (fieldIsOperator(expression[index])) {
                    if (lastValue == null) {
                        lastValue = data.get(expression[index - 1]);
                    }
                    String operator = expression[index];
                    String value2 = data.get(expression[index + 1]);
                    index++;
                    lastValue = solveExpression(lastValue, value2, operator);
                }
            } else {
                if(expression[index].trim().length() > 0) {
                    String value = expression[index];
                    if(data.containsKey(expression[index])) {
                        value = data.get(expression[index]);
                    }
                    if (lastValue == null) {
                        lastValue = value;
                    } else {
                        lastValue = solveExpression(lastValue, value);
                    }
                }
            }
        }

        return lastValue;
    }

    private static String solveExpression(String value1, String value2) {
        return value1 + " " + value2;
    }

    private static String solveExpression(String value1, String value2, String operator) {
        if(!Utils.isNumeric(value1) || !Utils.isNumeric(value2)) {
            return solveExpression(value1, value2);
        }
        float v1 = ((value1 != null) && !value1.isEmpty()) ? Float.parseFloat(value1) : 0;
        float v2 = ((value2 != null) && !value2.isEmpty()) ? Float.parseFloat(value2) : 0;
        float result = 0;
        switch (operator){
            case "+":
                result = v1 + v2;
                break;
            case "-":
                result = v1 - v2;
                break;
            case "/":
                if(v2 > 0) {
                    result = v1 / v2;
                }
                break;
            case "*":
                result = v1 * v2;
                break;
            default:
                result = 0;
        }
        return String.valueOf(result);
    }

    private static boolean fieldIsOperator(String field) {
        switch (field){
            case "+":
            case "-":
            case "/":
            case "*":
                return true;
        }
        return false;
    }

}
