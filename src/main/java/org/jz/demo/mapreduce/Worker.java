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
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jz
 * @date 2020/12/10
 */
public class Worker {

    public Worker() {
        // 开启一个工作线程，监听指定的端口
    }

    public static void main(String[] args) throws IOException {
        Bootstrap bootstrap =
                new Bootstrap().group(new NioEventLoopGroup()).channel(NioSocketChannel.class);

        WordCount wordCount = new WordCount();
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new LengthFieldPrepender(4));
                ch.pipeline().addLast(new ByteArrayDecoder());
                ch.pipeline().addLast(new ByteArrayEncoder());
                ch.pipeline().addLast(new SimpleChannelInboundHandler<byte[]>() {
                    ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS
                            , false);

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws JsonProcessingException {
                        System.out.println("激活");
                        ctx.writeAndFlush(objectMapper.writeValueAsBytes(new Request()));
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
                        Response response = objectMapper.readValue(msg, Response.class);
                        System.out.println("客户端接收响应" + response);

                        String base = "F:\\java project\\demo\\src\\main\\java\\org\\jz\\demo\\mapreduce\\data\\";

                        if (response.isDone()) {
                            ctx.close();
                        } else if (response.getTaskObject() == null) {
                            System.out.println("客户端未找到任务 休眠 2秒");
                            Thread.sleep(2000);
                        } else {
                            List<KeyValue<String, Integer>> map = wordCount.map(response.getTaskObject().getFilepath());

                            List<File> files = new ArrayList<>();
                            int nReduce = response.getNReduce();
                            for (int i = 0; i < nReduce; i++) {
                                File file = new File("mr-" + response.getTaskObject().getIdx() + "-" + i);
                                FileUtils.touch(file);
                                files.add(file);
                            }

                            Map<Integer, List<String>> collect =
                                    map.stream().collect(Collectors.groupingBy(a -> a.getKey().hashCode() % nReduce,
                                            Collectors.mapping(a -> {
                                                try {
                                                    return objectMapper.writeValueAsString(a);
                                                } catch (JsonProcessingException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }, Collectors.toList())));

                            for (Map.Entry<Integer, List<String>> integerListEntry : collect.entrySet()) {
                                File file = files.get(integerListEntry.getKey());
                                FileUtils.writeLines(file, integerListEntry.getValue());
                            }
                        }
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
