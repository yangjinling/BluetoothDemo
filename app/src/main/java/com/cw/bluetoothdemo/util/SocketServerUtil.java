package com.cw.bluetoothdemo.util;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.EventLog;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016/12/5.
 */

public class SocketServerUtil {

    private ServerSocket server;
    private InputStream in;
    private String str = null;
    public static boolean isClint = false;
    private Handler mHandler;
    private int port;

    /**
     * @param port 端口号
     * @steps bind();绑定端口号
     * @effect 初始化服务端
     */
    public SocketServerUtil(final int port) {
        this.port = port;
        if (!isClint) {
            try {
                server = new ServerSocket(port);
                Log.e("YJL", "创建server");
            } catch (IOException e) {
                e.printStackTrace();
            }
            isClint = true;
        }
    }

    /**
     * @steps listen();
     * @effect socket监听数据
     */
    public void beginListen() {
        if (listenThread == null) {
            if (null != server)
                listenThread = new ListenThread(server);
            listenThread.start();
        }
    }

    public void registerHandler(Handler handler) {
        mHandler = handler;
    }

    public void unregisterHandler() {
        mHandler = null;
    }

    private ListenThread listenThread;

    /**
     * @steps write();
     * @effect socket服务端发送信息
     */
    public void sendMessage(final byte[] buffer) {
        //创建临时对象
        ConnectedThread r;
        // 同步副本的connectedthread
        synchronized (this) {
//            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // 执行写同步
        r.write(buffer);
    }

    private ConnectedThread mConnectedThread;

    public synchronized void connected(Socket socket) {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // 启动线程管理连接和传输
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

    }

    private class ConnectedThread extends Thread {
        private final Socket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(Socket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // 获得bluetoothsocket输入输出流
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e("YJL", "没有创建临时sockets", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            try {
                /**得到输入流*/
                byte[] buffer = new byte[1024];
                int bytes = 0;
                String strReadDate = new String();
                /**
                 * 实现数据循环接收
                 * */
                while (!mmSocket.isClosed()) {
                    Thread.sleep(1);
//                    Log.e("YJLstate", "" + mmSocket.isClosed());
                    try {
                        if ((bytes = mmInStream.read(buffer)) > 0) {
                            byte[] buf_data = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                buf_data[i] = buffer[i];
                            }
                            strReadDate = BJCWUtil.HexTostr(buf_data, buf_data.length);
                            Log.e("YJL", "wifi到wifi的数据：：：" + strReadDate);
                            returnMessage(strReadDate);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("YJL", "一直在读异常了1");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 向外发送。
         *
         * @param buffer 发送的数据
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer, 0, buffer.length);
                mmOutStream.flush();
            } catch (IOException e) {
                Log.e("YJL", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("YJL", "close() of connect socket failed", e);
            }
        }
    }

    private class ListenThread extends Thread {
        private Socket mSocket;
        private ServerSocket mServer;

        public ListenThread(ServerSocket server) {
            mServer = server;
        }

        public void run() {
            try {
                /**
                 * accept();
                 * 接受请求
                 * */
                while (true) {
                    Thread.sleep(1);
                    mSocket = mServer.accept();
                    if (null != mSocket) {
                        connected(mSocket);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("YJL", "断开1");
                if (null != mServer) {
                    try {
                        if (null != mSocket) {
                            mSocket.close();
                        }
                        mServer.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                if (null != mServer) {
                    mServer.close();
                    if (null != mSocket)
                        mSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static final int MESSAGE_SOCKET_READ = 1;

    /**
     * @steps read();
     * @effect socket服务端得到返回数据并发送到主界面
     */
    public void returnMessage(String chat) {
        Message msg = mHandler.obtainMessage(MESSAGE_SOCKET_READ);
        msg.obj = chat;
        mHandler.sendMessage(msg);
    }

    /**
     * 停止所有的线程
     */
    public synchronized void disconnect() {
        Log.e("YJL", "wifi---disconnect");
        if (listenThread != null) {
            listenThread.cancel();
            listenThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


    }

}
