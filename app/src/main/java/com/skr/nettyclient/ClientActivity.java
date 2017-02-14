package com.skr.nettyclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.socks.library.KLog;

import java.net.InetSocketAddress;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author hyw
 * @since 2017/2/13
 */
public class ClientActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ClientActivity";
    public static int MSG_REC = 0xabc;
    public static int PORT = 7891;
    public static final String HOST = "192.168.1.115";
    private NioEventLoopGroup mNioEventLoopGroup;
    private Channel mChannel;
    private ChannelFuture mChannelFuture;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_REC) {
                Toast.makeText(ClientActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @OnClick(R.id.netty_send_btn)
    public void onClick() {
        sendMessage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        ButterKnife.bind(this);
        attemptConnect();
    }

    private void attemptConnect() {
        new Thread() {
            @Override
            public void run() {
                mNioEventLoopGroup = new NioEventLoopGroup();
                try {
                    // Client服务启动器
                    Bootstrap bootstrap = new Bootstrap();
                    // 指定EventLoopGroup
                    // 相比于服务端，客户端只需要创建一个EventLoopGroup，因为它不需要独立的线程去监听客户端连接
                    bootstrap.group(mNioEventLoopGroup);
                    // 指定channel类型
                    bootstrap.channel(NioSocketChannel.class);
                    // 指定Handler
                    bootstrap.handler(new MyClientInitializer(ClientActivity.this, mHandler));
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                    bootstrap.option(ChannelOption.TCP_NODELAY, true);
                    bootstrap.option(ChannelOption.SO_TIMEOUT, 5000);
                    // 连接到本地的7878端口的服务端
                    mChannelFuture = bootstrap.connect(new InetSocketAddress(
                            HOST, PORT));
                    mChannel = mChannelFuture.sync().channel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //发送数据
    private void sendMessage() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    KLog.i(LOG_TAG, "mChannel.write sth & mChannel.isOpen(): " + mChannel.isOpen());
                    // 写入Buffer并刷入
                    // 向服务端发送在控制台输入的文本 并用"\r\n"结尾之所以用\r\n结尾 是因为我们在handler中添加了 DelimiterBasedFrameDecoder 帧解码。 这个解码器是一个根据\n符号位分隔符的解码器。所以每条消息的最后必须加上\n否则无法识别和解码
                    mChannel.writeAndFlush("你好,我是客户端.\r\n");
                    mChannel.read();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNioEventLoopGroup != null) {
            mNioEventLoopGroup.shutdownGracefully();
        }
    }
}
