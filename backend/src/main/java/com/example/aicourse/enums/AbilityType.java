package com.example.aicourse.enums;

/**
 * 能力维度枚举
 * 用于学生能力模型雷达图分析
 */
public enum AbilityType {
    /**
     * 理论能力 - 理论知识的理解和掌握
     */
    THEORY("理论", "Theory"),

    /**
     * 实践能力 - 动手实践和应用能力
     */
    PRACTICE("实践", "Practice"),

    /**
     * 逻辑能力 - 逻辑思维和分析能力
     */
    LOGIC("逻辑", "Logic"),

    /**
     * 创新能力 - 创新思维和问题解决能力
     */
    INNOVATION("创新", "Innovation");

    private final String chineseName;
    private final String englishName;

    AbilityType(String chineseName, String englishName) {
        this.chineseName = chineseName;
        this.englishName = englishName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    /**
     * 根据字符串获取对应的能力类型
     * @param value 能力类型字符串
     * @return AbilityType，如果不匹配则返回 THEORY 作为默认值
     */
    public static AbilityType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return THEORY; // 默认值
        }

        try {
            return AbilityType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 如果无法匹配，返回默认值
            return THEORY;
        }
    }

    /**
     * 检查字符串是否为有效的能力类型
     * @param value 能力类型字符串
     * @return true 如果有效，否则返回 false
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            AbilityType.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
