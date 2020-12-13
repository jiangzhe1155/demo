package org.jz.demo.mapreduce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

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
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                ch.pipeline().addLast(new LengthFieldPrepender(4));
                ch.pipeline().addLast(new ByteArrayDecoder());
                ch.pipeline().addLast(new ByteArrayEncoder());
                ch.pipeline().addLast(new SimpleChannelInboundHandler<byte[]>() {
                    ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws JsonProcessingException {
                        System.out.println("激活");
                        ctx.writeAndFlush(objectMapper.writeValueAsBytes(new Request()));
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
                        Response response = objectMapper.readValue(msg, Response.class);
                        System.out.println("客户端接收响应" + response);

                    }
                });
            }
        });

        try {
            ChannelFuture sync = bootstrap.connect(new InetSocketAddress(9999)).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
