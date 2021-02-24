package com.atguigu.gmall.cart.controller;

import java.math.BigDecimal;

public class TestBigDecimal {

    public static void main(String[] args) {
        // 初始化
        BigDecimal b1 = new BigDecimal(0.01f);
        BigDecimal b2 = new BigDecimal(0.01d);
        BigDecimal b3 = new BigDecimal("0.01");
        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);

        // 比较
        int i = b1.compareTo(b2);// 1 0 -1
        System.out.println(i);

        // 运算
        BigDecimal add = b1.add(b2);
        System.out.println(add);

        BigDecimal subtract = b2.subtract(b1);
        System.out.println(subtract);

        BigDecimal b4 = new BigDecimal("6");
        BigDecimal b5 = new BigDecimal("7");
        BigDecimal multiply = b4.multiply(b5);
        System.out.println(multiply);

        BigDecimal divide = b4.divide(b5,3,BigDecimal.ROUND_HALF_DOWN);
        System.out.println(divide);

        // 约数
        BigDecimal subtract1 = b2.add(b1);
        BigDecimal bigDecimal = subtract1.setScale(3, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(bigDecimal);

    }
}
