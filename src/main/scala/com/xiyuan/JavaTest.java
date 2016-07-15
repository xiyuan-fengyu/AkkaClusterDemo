package com.xiyuan;

import java.lang.reflect.Field;

/**
 * Created by xiyuan_fengyu on 2016/7/14.
 */
public class JavaTest {

    public static void main(String[] args) {
        A a = new A();
        for(Field f: A.class.getDeclaredFields()) {
            f.setAccessible(true);
            System.out.println(f.isAccessible());
            try {
                f.set(a, 1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        System.out.println(a.getPrivatePro());
    }

}

class A {
    private int privatePro = 1;
    public int publicPro = 1;

    public int getPrivatePro() {
        return privatePro;
    }
}