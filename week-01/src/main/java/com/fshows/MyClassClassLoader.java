package com.fshows;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class MyClassClassLoader extends ClassLoader {
    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> clazz = new MyClassClassLoader().findClass("Hello.xlass");
        System.out.println(clazz);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] bytes = IOUtils.toByteArray(this.getClass().getResourceAsStream("/" + name));
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) ((byte) 255 - bytes[i]);
            }
            return defineClass(null, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(e.getMessage());
        }


    }
}
