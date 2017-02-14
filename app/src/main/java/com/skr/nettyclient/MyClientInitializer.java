package com.skr.nettyclient;

import android.content.Context;
import android.os.Handler;

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
    private static final String LOG_TAG = "MyClientInitializer";
    private Context mContext;
    private Handler mHandler;

    public MyClientInitializer(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 这个地方必须和服务端对应上。否则无法正常解码和编码
        // DelimiterBasedFrameDecoder 消息分割
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        //客户端的逻辑
        pipeline.addLast("mHandler", new MyClientHandler(mContext, mHandler));
    }

}
