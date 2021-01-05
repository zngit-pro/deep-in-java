/**
 * fshows.com
 * Copyright (C) 2013-2021 All Rights Reserved.
 */
package com.fshows;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhangn
 * @version Reactor.java, v 0.1 2021-01-05 zhangn
 */
public class Reactor implements Runnable {

    private ServerSocketChannel serverSocketChannel;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    private Selector selector;

    public Reactor(Integer port) throws IOException {

        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.bind(new InetSocketAddress("localhost", port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {

        while (true) {

            try {
                int select = selector.select();
                if (select <= 0) {
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {

                SelectionKey selectionKey = iterator.next();

                dispatch(selectionKey);

                iterator.remove();
            }
        }

    }

    private void dispatch(SelectionKey selectionKey) {

        if (selectionKey.isAcceptable()) {
            try {
                SocketChannel accept = this.serverSocketChannel.accept();
                accept.configureBlocking(false);
                accept.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                System.out.println("客户端连接:" + accept.getRemoteAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //读写事件
            new Handler(selectionKey, executorService).run();
        }

    }

}