package com.skr.nettyclient;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.socks.library.KLog;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author hyw
 * @since 2017/2/13
 */
public class MyClientHandler extends SimpleChannelInboundHandler<String> {
    private static final String LOG_TAG = "MyClientHandler";
    private Context mContext;
    private Handler mHandler;

    public MyClientHandler(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg)
            throws Exception {
        KLog.i(LOG_TAG, "channelRead0->msg= " + msg);
        Message m = new Message();
        m.what = ClientActivity.MSG_REC;
        m.obj = msg;
        mHandler.sendMessage(m);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        KLog.i(LOG_TAG, "Client active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        KLog.i(LOG_TAG, "Client close ");
    }

}
