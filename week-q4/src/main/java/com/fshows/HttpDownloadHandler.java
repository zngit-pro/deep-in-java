package com.fshows;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

public class HttpDownloadHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        String uri = URLDecoder.decode(request.uri(), "utf-8");

        if (!uri.startsWith(HttpResponseUtil.UPLOAD_URI)) {
            File file = new File(HttpResponseUtil.FILE_PATH + uri);

            if (!file.exists()) {
                HttpResponseUtil.writeHtml("文件不存在", ctx);
                return;
            }

            if (file.isFile()) {
                downloadFile(file, ctx);
            } else {
                showFileList(ctx, file, uri);
            }
        } else {
            ctx.fireChannelRead(request);
        }
    }

    private void downloadFile(File file, ChannelHandlerContext channelHandlerContext) throws IOException {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add("Context-type", "application/octet-stream");
        response.headers().add("Content-Length", file.length());
        Channel channel = channelHandlerContext.channel();
        byte[] bytes = FileUtils.readFileToByteArray(file);
        response.content().writeBytes(bytes);
        channel.writeAndFlush(response);
    }


    private void showFileList(ChannelHandlerContext ctx, File file, String uri) {

        StringBuilder buffer = new StringBuilder();
        String input = "<form method=\"post\" enctype=\"multipart/form-data\"  action=\"http://localhost:8888/@upload\"><input name=\"file\" type=\"file\"/><br/><button type=\"submit\" >上传</button></form><br/>";
        buffer.append(input);
        // 获取目录的子文件
        File[] files = file.listFiles();

        if (files == null) {
            HttpResponseUtil.writeHtml("文件不存在", ctx);
            return;
        }

        for (File f : files) {
            String name = f.getName();
            if ("/".equals(uri)) {
                buffer.append("<li><a href= '").append(uri).append(name).append("'>").append(name).append("</a></li>");
            } else {
                buffer.append("<li><a href= '").append(uri).append(File.separator).append(name).append("'>").append(name).append("</a></li>");
            }
        }

        HttpResponseUtil.writeHtml(buffer.toString(), ctx);
    }

}
