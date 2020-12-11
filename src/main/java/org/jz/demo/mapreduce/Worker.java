package org.jz.demo.mapreduce;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author jz
 * @date 2020/12/10
 */
public class Worker {

    public Worker() {
        // 开启一个工作线程，监听指定的端口
    }

    public static void main(String[] args) {
        Bootstrap bootstrap =
                new Bootstrap().group(new NioEventLoopGroup()).channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) {
                        System.out.println("激活");
                        ctx.writeAndFlush("asda".getBytes());
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                        System.out.println(msg.toString());
                    }
                });
                ch.pipeline().addLast();
            }
        });

        try {
            ChannelFuture sync = bootstrap.connect(new InetSocketAddress(9999)).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
