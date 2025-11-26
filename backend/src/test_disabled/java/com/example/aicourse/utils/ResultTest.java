package com.example.aicourse.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 工具类测试
 * 测试统一API响应格式的各种方法
 */
class ResultTest {

    @Test
    void testOkWithoutData() {
        // 测试成功但无返回数据
        Result<String> result = Result.ok();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testOkWithData() {
        // 测试成功且有返回数据
        String testData = "test data";
        Result<String> result = Result.ok(testData);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals(testData, result.getData());
    }

    @Test
    void testOkWithNullData() {
        // 测试成功但数据为null
        Result<String> result = Result.ok((String) null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testErrorWithMessage() {
        // 测试失败：自定义消息
        String errorMessage = "操作失败";
        Result<String> result = Result.error(errorMessage);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertEquals(errorMessage, result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testErrorWithEmptyMessage() {
        // 测试失败：空消息
        Result<String> result = Result.error("");
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertEquals("", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testErrorWithNullMessage() {
        // 测试失败：null消息
        Result<String> result = Result.error(null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNull(result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testConstructorWithAllParameters() {
        // 测试全参数构造函数
        String testData = "test data";
        Result<String> result = new Result<>(200, "custom message", testData);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("custom message", result.getMsg());
        assertEquals(testData, result.getData());
    }

    @Test
    void testConstructorWithNullData() {
        // 测试构造函数：数据为null
        Result<String> result = new Result<>(500, "server error", null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("server error", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testDefaultConstructor() {
        // 测试默认构造函数
        Result<String> result = new Result<>();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNull(result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testSetAndGetMethods() {
        // 测试setter和getter方法
        Result<String> result = new Result<>();
        
        result.setCode(404);
        result.setMsg("Not Found");
        result.setData("error data");
        
        // 验证结果
        assertEquals(404, result.getCode());
        assertEquals("Not Found", result.getMsg());
        assertEquals("error data", result.getData());
    }

    @Test
    void testEqualsAndHashCode() {
        // 测试equals和hashCode方法
        Result<String> result1 = Result.ok("test");
        Result<String> result2 = Result.ok("test");
        Result<String> result3 = Result.ok("different");
        
        // 验证equals
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        
        // 验证hashCode
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }

    @Test
    void testToString() {
        // 测试toString方法
        Result<String> result = Result.ok("test data");
        String toString = result.toString();
        
        // 验证结果
        assertNotNull(toString);
        assertTrue(toString.contains("code=0"));
        assertTrue(toString.contains("msg=success"));
        assertTrue(toString.contains("data=test data"));
    }

    @Test
    void testGenericTypes() {
        // 测试不同泛型类型
        Result<String> stringResult = Result.ok("string data");
        Result<Integer> intResult = Result.ok(123);
        Result<Boolean> boolResult = Result.ok(true);
        
        // 验证结果
        assertEquals("string data", stringResult.getData());
        assertEquals(123, intResult.getData());
        assertEquals(true, boolResult.getData());
    }

    @Test
    void testComplexData() {
        // 测试复杂数据类型
        TestData testData = new TestData("test", 123);
        Result<TestData> result = Result.ok(testData);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals(testData, result.getData());
        assertEquals("test", result.getData().getName());
        assertEquals(123, result.getData().getValue());
    }

    // 内部测试类
    private static class TestData {
        private String name;
        private int value;

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestData testData = (TestData) obj;
            return value == testData.value && 
                   (name == null ? testData.name == null : name.equals(testData.name));
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + value;
            return result;
        }
    }
} 