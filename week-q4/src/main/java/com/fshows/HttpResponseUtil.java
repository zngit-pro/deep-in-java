/**
 * fshows.com
 * Copyright (C) 2013-2021 All Rights Reserved.
 */
package com.fshows;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;

/**
 * @author zhangn
 * @version HttpResponseUtil.java, v 0.1 2021-01-04 17:33 zhangn
 */
public class HttpResponseUtil {

    public static final String UPLOAD_URI = "/@upload";

    public final static String FILE_PATH = "/Users/zhangn/Downloads/";

    private static final String CONTEXT_TYPE = "Context-type";

    private static final String HTML_TYPE = "text/html;charset=UTF-8";


    public static void writeHtml(String text, ChannelHandlerContext channelHandlerContext) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(CONTEXT_TYPE, HTML_TYPE);

        String msg = "<html><meta charset=\"UTF-8\" />" + text + "</html>";

        response.content().writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        channelHandlerContext.channel().writeAndFlush(response);
    }

}