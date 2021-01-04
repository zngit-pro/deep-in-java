package com.fshows;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HttpServletRequest extends SimpleChannelInboundHandler<FullHttpRequest> {
    final String GET_REQUEST = "GET";

    // 文件夹路劲自己设一个就行  我这里乱写了一个我maven仓库地址
    final String FILE_PATH = "D:" + File.separator;

    final String CONTEXT_TYPE = "Context-type";

    final String HTML_TYPE = "text/html;charset=UTF-8";

    final String CONTENT_LENGTH = "Content-Length";

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {

        // 获取用户的uri
        String uri = request.uri();
        String name = request.method().name();

        // 只支持GEt请求
        if (!GET_REQUEST.equals(name)) {
            // 响应浏览器
            responseError("只支持GET请求", channelHandlerContext);
            return;
        }

        File file = new File(FILE_PATH + URLDecoder.decode(uri, "UTF-8"));

        if (!file.exists()) {
            responseError("文件不存在。。。", channelHandlerContext);
            return;
        }

        if (file.isFile()) {
            // 如果是一个文件，就执行下载
            responseFileCopy(file, channelHandlerContext);
        } else {
            // 如果是一个目录就显示子目录
            responseDir(channelHandlerContext, file, uri);
        }

    }

    /**
     * 文件下载
     */
    private void responseFileCopy(File file, ChannelHandlerContext channelHandlerContext) {

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(CONTEXT_TYPE, "application/octet-stream");
        response.headers().add(CONTENT_LENGTH, file.length());

        Channel channel = channelHandlerContext.channel();

        FileInputStream ips = null;
        try {
            ips = new FileInputStream(file);
            byte[] by = new byte[1024];
            int read;
            while ((read = ips.read(by, 0, by.length)) != -1) {
                response.content().writeBytes(by, 0, read);
            }
            channel.writeAndFlush(response);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ips != null) {
                try {
                    ips.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 响应目录信息
     */
    private void responseDir(ChannelHandlerContext channelHandlerContext, File file, String uri) {

        StringBuilder buffer = new StringBuilder();
        // 获取目录的子文件
        File[] files = file.listFiles();

        for (File file1 : Objects.requireNonNull(files)) {
            if ("/".equals(uri)) {
                buffer.append("<li><a href= '").append(uri).append(file1.getName()).append("'>").append(file1.getName()).append("</a></li>");
            } else {
                buffer.append("<li><a href= '").append(uri).append(File.separator).append(file1.getName()).append("'>").append(file1.getName()).append("</a></li>");
            }
        }
        responseClient(buffer.toString(), channelHandlerContext);
    }

    /**
     * 响应客户端
     */
    public void responseClient(String text, ChannelHandlerContext channelHandlerContext) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        // 设置响应头
        response.headers().add(CONTEXT_TYPE, HTML_TYPE);

        String msg = "<html> \t<meta charset=\"UTF-8\" />" + text + "</html>";

        response.content().writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        channelHandlerContext.channel().writeAndFlush(response);
    }

    /**
     * 响应错误信息
     */
    public void responseError(String text, ChannelHandlerContext channelHandlerContext) {
        String msg = "<h1>" + text + "</h1>";
        responseClient(msg, channelHandlerContext);
    }
}
