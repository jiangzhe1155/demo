package org.jz.demo.mapreduce.netty;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.jz.demo.mapreduce.Response;

import java.net.InetSocketAddress;

/**
 * @author 江哲
 * @date 2020/12/11
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        int port = 9999;

        ObjectMapper objectMapper = new ObjectMapper();
        Thread server = new Thread(() -> {
            ServerBootstrap b = new ServerBootstrap();
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                            ch.pipeline().addLast(new LengthFieldPrepender(4));
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) throws JsonProcessingException {
                                    System.out.println(StrUtil.format("服务器{}接收到客户端的请求",
                                            Thread.currentThread().getName()));
                                    Response response = new Response();
                                    ctx.writeAndFlush(objectMapper.writeValueAsBytes(response));
                                }
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128);
            try {
                ChannelFuture f = b.bind().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        Thread client = new Thread(() -> {
            Bootstrap bootstrap =
                    new Bootstrap().group(new NioEventLoopGroup()).channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                    ch.pipeline().addLast(new LengthFieldPrepender(4));
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new StringEncoder());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws JsonProcessingException {
                            System.out.println(StrUtil.format("客户端接收到服务器的回复  {}", msg));
                            Response response = objectMapper.readValue(msg, Response.class);
                            System.out.println("客户端处理完成");
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            System.out.println("激活");
                            ctx.writeAndFlush("{\"asd\":\"sadasd\")");
                        }
                    });
                }
            });

            try {
                ChannelFuture sync = bootstrap.connect(new InetSocketAddress(port)).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        server.start();
        client.start();
    }
}