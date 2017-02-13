package com.skr.nettyclient;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * @author hyw
 * @since 2017/2/13
 */
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {
    private Context context;
    private Handler handler;
    public MyClientInitializer(Context ctx,Handler handler){
        this.context = ctx;
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        /**
         * 这个地方的必须和服务端对应上。否则无法正常解码和编码
         */
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        //客户端的逻辑
        pipeline.addLast("handler",new MyClientHandler(context,handler));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("---channelRead--- msg="+msg);
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("---channelReadComplete---");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i("MyClientInitializer","---channelActive---");
        super.channelActive(ctx);
    }

}
