package com.nutcracker.server;

import java.math.BigDecimal;

/**
 * @author
 * @date 2022-05-24 16:05
 */
public class Test {
    public static void main(String[] args) {
        BigDecimal bg = new BigDecimal(1.1199).setScale(4, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal num1 = (bg.subtract(new BigDecimal(1).setScale(4, BigDecimal.ROUND_HALF_DOWN)));
        BigDecimal num2 = num1.divide(new BigDecimal(140),4).setScale(4, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(num2);
    }
}
