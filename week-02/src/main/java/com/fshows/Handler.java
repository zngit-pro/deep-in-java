/**
 * fshows.com
 * Copyright (C) 2013-2021 All Rights Reserved.
 */
package com.fshows;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;

/**
 * @author zhangn
 * @version Handler.java, v 0.1 2021-01-05 zhangn
 */
public class Handler implements Runnable {

    ExecutorService executorService;
    private SelectionKey selectionKey;

    public Handler(SelectionKey selectionKey, ExecutorService executorService) {

        this.executorService = executorService;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {

        try {
            if (selectionKey.isReadable()) {
                executorService.submit(this::read);
            } else {
                write();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void read() {
        System.out.println("read");
    }


    private void write() {
        selectionKey.cancel();
    }
}