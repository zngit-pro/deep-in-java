package com.fshows;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyFileServer {
    public static void main(String[] args) throws Exception {

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        ChannelFuture channelFuture = serverBootstrap
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {

                        ChannelPipeline pipeline = channel.pipeline();

                        pipeline.addLast(new HttpServerCodec());

                        pipeline.addLast(new HttpObjectAggregator(1000 * 1024));

                        pipeline.addLast(new HttpDownloadHandler());

                        pipeline.addLast(new ChunkedWriteHandler());

                        pipeline.addLast(new HttpUploadHandler());

                    }
                }).bind(8888);

        // 阻塞
        channelFuture.sync();
        System.out.println("服务启动成功");
    }

}
