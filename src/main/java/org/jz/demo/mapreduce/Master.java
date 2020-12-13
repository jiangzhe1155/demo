package org.jz.demo.mapreduce;

import cn.hutool.core.collection.ConcurrentHashSet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * @author jz
 * @date 2020/12/10
 */
public class Master {

    private LinkedBlockingDeque<TaskObject> mapTasks = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<TaskObject> reduceTasks = new LinkedBlockingDeque<>();

    private ConcurrentHashSet<TaskObject> unfinishedMapTasks = new ConcurrentHashSet<>();
    private ConcurrentHashSet<TaskObject> unfinishedReduceTasks = new ConcurrentHashSet<>();

    private boolean isDone;

    public boolean mapTaskFinished() {
        return isDone || (mapTasks.size() == 0 && unfinishedMapTasks.size() == 0);
    }

    public boolean reduceTaskFinished() {
        return isDone || (reduceTasks.size() == 0 && unfinishedReduceTasks.size() == 0);
    }

    public TaskObject pollMapTask() {
        return mapTasks.pollFirst();
    }

    public TaskObject pollReduceTask() {
        return reduceTasks.pollFirst();
    }

    public boolean isDone() {
        return isDone;
    }

    public Master(List<String> filePaths) {
        for (int i = 0; i < filePaths.size(); i++) {
            mapTasks.add(new TaskObject().setFilepath(filePaths.get(i)).setIdx(i));
        }
    }

    public void server() throws InterruptedException {
        int port = 9999;
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Master self = this;
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                            ch.pipeline().addLast(new LengthFieldPrepender(4));
                            ch.pipeline().addLast(new ByteArrayDecoder());
                            ch.pipeline().addLast(new ByteArrayEncoder());
                            ch.pipeline().addLast(new RequestHandler(self));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);


            ChannelFuture f = b.bind().sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> paths = Arrays.stream(args).map(filepath -> {
            String fullPath = FilenameUtils.getFullPath(filepath);
            String rex = FilenameUtils.getName(filepath);
            return Arrays.stream(Objects.requireNonNull(new File(fullPath).list(new WildcardFileFilter(rex))))
                    .map(name -> fullPath + name).collect(Collectors.toList());
        }).flatMap(Collection::stream).distinct().collect(Collectors.toList());
        for (String path : paths) {
            System.out.println(path);
        }
        Master master = new Master(paths);
        master.server();

    }

}
