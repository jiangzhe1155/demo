package org.jz.demo.mapreduce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;

/**
 * @author 江哲
 * @date 2020/12/12
 */
public class RequestHandler extends SimpleChannelInboundHandler<byte[]> {
    private Master master;
    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    public RequestHandler(Master master) {
        super();
        this.master = master;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws IOException {
        System.out.println("客户端接收到请求");
        Request request = objectMapper.readValue(msg, Request.class);
        Response response = new Response();
        TaskObject taskObject = null;

        if (master.isDone()) {
            response.setDone(true);
        } else if (!master.mapTaskFinished()) {
            if ((taskObject = master.pollMapTask()) != null) {
                response.setTaskType(Response.MAP_TASK);
                response.setTaskObject(taskObject);
            }
        } else if (!master.reduceTaskFinished()) {
            if ((taskObject = master.pollMapTask()) != null) {
                response.setTaskType(Response.REDUCE_TASK);
                response.setTaskObject(taskObject);
            }
        }
        System.out.println("服务端返回请求" + response);
        ctx.writeAndFlush(objectMapper.writeValueAsBytes(response));
    }
}
