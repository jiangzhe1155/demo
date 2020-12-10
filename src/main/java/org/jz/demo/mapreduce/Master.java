package org.jz.demo.mapreduce;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author jz
 * @date 2020/12/10
 */
public class Master {

    public Master() throws InterruptedException {
        server();
    }


    public void server() throws InterruptedException {
        int port = 9999;

        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            try {
                new Master().server();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();

        Thread thread1 = new Thread(() -> {
            try {
                java.nio.channels.SocketChannel open = java.nio.channels.SocketChannel.open();
                open.connect(new InetSocketAddress(9999));
                Socket socket = open.socket();

                OutputStream outputStream = socket.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                writer.write("AA");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        Thread thread2 = new Thread(() -> {
            try {
                java.nio.channels.SocketChannel open = java.nio.channels.SocketChannel.open();
                open.connect(new InetSocketAddress(9999));
                Socket socket = open.socket();

                OutputStream outputStream = socket.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                writer.write("BBBB");

            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        thread1.start();
        thread2.start();
    }

}
