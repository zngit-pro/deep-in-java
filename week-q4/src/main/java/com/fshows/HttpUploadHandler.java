/**
 * fshows.com
 * Copyright (C) 2013-2021 All Rights Reserved.
 */
package com.fshows;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author zhangn
 * @version HttUploadHandler.java, v 0.1 zhangn
 */
public class HttpUploadHandler extends SimpleChannelInboundHandler<HttpObject> {

    public HttpUploadHandler() {
        super(false);
    }

    private final HttpDataFactory factory = new DefaultHttpDataFactory(true);

    private HttpPostRequestDecoder httpDecoder;

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject) throws IOException {
        HttpRequest request;
        if (httpObject instanceof HttpRequest) {
            request = (HttpRequest) httpObject;
            if (request.uri().startsWith(HttpResponseUtil.UPLOAD_URI) && request.method().equals(HttpMethod.POST)) {
                httpDecoder = new HttpPostRequestDecoder(factory, request);
            } else {
                ctx.fireChannelRead(httpObject);
            }
        }
        if (httpObject instanceof HttpContent) {
            if (httpDecoder != null) {
                final HttpContent chunk = (HttpContent) httpObject;
                httpDecoder.offer(chunk);
                if (chunk instanceof LastHttpContent) {
                    writeChunk(ctx);
                }
            }
        }
    }

    private void writeChunk(ChannelHandlerContext ctx) throws IOException {
        while (httpDecoder.isMultipart() && httpDecoder.hasNext()) {
            InterfaceHttpData data = httpDecoder.next();
            if (data != null && InterfaceHttpData.HttpDataType.FileUpload.equals(data.getHttpDataType())) {
                final FileUpload fileUpload = (FileUpload) data;
                final File file = new File(HttpResponseUtil.FILE_PATH + "temp/" + fileUpload.getFilename());
                byte[] array = fileUpload.getByteBuf().array();
                FileUtils.writeByteArrayToFile(file, array);
                HttpResponseUtil.writeHtml("上传完毕...", ctx);
            }
        }

        httpDecoder.destroy();
        httpDecoder = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (httpDecoder != null) {
            httpDecoder.cleanFiles();
        }
    }

}