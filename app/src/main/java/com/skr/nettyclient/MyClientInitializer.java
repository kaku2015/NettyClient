package com.skr.nettyclient;

import android.content.Context;
import android.os.Handler;

import com.socks.library.KLog;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

/**
 * @author hyw
 * @since 2017/2/13
 */
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {
    private static final String LOG_TAG = "MyClientInitializer";
    private Context mContext;
    private Handler mHandler;

    private static final String KEY_PASSWORD = "123456";    //密钥库密码
    private static final String KEYSTORE_CLIENT = "kclient.bks";    //本地密钥库
    private static final String KEYSTORE_TRUST = "tclient.bks";        //信任密钥库

    public MyClientInitializer(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 在大多数情况下,SslHandler 将成为 ChannelPipeline 中的第一个 ChannelHandler，所以调用了pipeline.addFirst() 。这将确保所有其他 ChannelHandler 应用他们的逻辑到数据后加密后才发生,从而确保他们的变化是安全的。
        SSLEngine sslEngine = getClientSSLContext().createSSLEngine();
        sslEngine.setUseClientMode(true);
        pipeline.addFirst("ssl", new SslHandler(sslEngine));

        // 这个地方必须和服务端对应上。否则无法正常解码和编码
        // DelimiterBasedFrameDecoder 消息分割
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        //客户端的逻辑
        pipeline.addLast("mHandler", new MyClientHandler(mContext, mHandler));
    }

    private SSLContext getClientSSLContext() {
        SSLContext sslContext = null;
        try {
            //取得TLS协议的SSLContext实例
            sslContext = SSLContext.getInstance("TLS");
            //取得BKS类型的本地密钥库实例，这里特别注意：手机只支持BKS密钥库，不支持Java默认的JKS密钥库
            KeyStore clientkeyStore = KeyStore.getInstance("BKS");
            //初始化
            clientkeyStore.load(
                    mContext.getResources().getAssets().open(KEYSTORE_CLIENT),
                    KEY_PASSWORD.toCharArray());
            KeyStore trustkeyStore = KeyStore.getInstance("BKS");
            trustkeyStore.load(mContext.getResources().getAssets()
                    .open(KEYSTORE_TRUST), KEY_PASSWORD.toCharArray());

            //获得X509密钥库管理实例
            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance("X509");
            keyManagerFactory.init(clientkeyStore, KEY_PASSWORD.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance("X509");
            trustManagerFactory.init(trustkeyStore);

            //初始化SSLContext实例
            sslContext.init(keyManagerFactory.getKeyManagers(),
                    trustManagerFactory.getTrustManagers(), null);

        } catch (Exception e) {
            KLog.e(LOG_TAG, e.toString());
        }
        return sslContext;
    }
}
