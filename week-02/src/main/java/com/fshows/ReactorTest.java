/**
 * fshows.com
 * Copyright (C) 2013-2021 All Rights Reserved.
 */
package com.fshows;

import java.io.IOException;

/**
 * @author zhangn
 * @version ReactorTest.java, v 0.1 2021-01-05 zhangn
 */
public class ReactorTest {

    public static void main(String[] args) throws IOException {
        new Thread(new Reactor(8899)).start();
    }

}