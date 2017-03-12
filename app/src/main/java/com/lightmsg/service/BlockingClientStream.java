package com.lightmsg.service;

import android.util.Log;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.handler.stream.StreamIoHandler;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingClientStream {
    private static final String TAG = BlockingClientStream.class.getSimpleName();
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    private static BlockingClientStream bcs = null;// = new BlockingClientStream();
    private boolean requestedConnect;
    private NioSocketConnector connector;

    public enum ClientState {
        Initiate,
        Connect,
        Negotiate,
        Start;
    }

    class StreamRequest {
        String TransactId;

        boolean isSend;
        File sendFile;

        File recvFile;
        int recvSize;
        private BlockingClientStreamCallback cb;
    }

    private final BlockingQueue<StreamRequest> mSendQueue = new LinkedBlockingQueue<StreamRequest>();
    private final BlockingQueue<StreamRequest> mRecvQueue = new LinkedBlockingQueue<StreamRequest>();
    private final BlockingQueue<StreamRequest> mQueues = new LinkedBlockingQueue<StreamRequest>();


    private BlockingClientStream() {
        connector = new NioSocketConnector();

        //ObjectSerializationCodecFactory factory = new ObjectSerializationCodecFactory(); 
        //factory.setDecoderMaxObjectSize(Integer.MAX_VALUE);
        //factory.setEncoderMaxObjectSize(Integer.MAX_VALUE);
        //connector.getFilterChain().addLast("codec",new ProtocolCodecFilter(factory));

        // Configure the service.
        connector.setConnectTimeoutMillis(5000);
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.setHandler(new ClientStreamIoHandler());

        requestedConnect = false;
    }

    public static BlockingClientStream getInstance() {
        if (bcs == null) {
            bcs = new BlockingClientStream();
        }
        return bcs;
    }

    public void connect() {
        if (!requestedConnect) {
            requestedConnect = true;

            pool.execute(new Runnable() {
                public void run() {
                    connect0();
                }
            });
        }
    }

    public void connect0() {
        Log.d(TAG, "connect0(), NioSocketConnector start...");
        IoSession session;
        for (;;) {
            try {
                ConnectFuture future = connector.connect(
                        new InetSocketAddress(
                                CoreService.getStreamServer(),
                                CoreService.getStreamPort()));
                future.awaitUninterruptibly();
                session = future.getSession();
                break;
            } catch (RuntimeIoException e) {
                Log.e(TAG, "Failed to connect.");
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (java.nio.channels.UnresolvedAddressException e) {
                Log.e(TAG, "Failed to connect.");
                e.printStackTrace();
            }
        }

        // wait until the summation is done
        session.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
        dispose();
        Log.d(TAG, "connect0(), NioSocketConnector stopped...");
    }

    public void dispose() {
        Log.d(TAG, "Client.dispose()!!!");
        pool.shutdownNow();
        requestedConnect = false;
    }

    class SendHandler implements Runnable {
        private final StreamRequest sr = new StreamRequest();
        public SendHandler(String tid, File file, BlockingClientStreamCallback cb) {
            sr.TransactId = tid;
            sr.isSend = true;
            sr.sendFile = file;
            sr.recvFile = null;
            sr.cb = cb;
        }

        public void run() {
            Log.i(TAG, "SendHandler.run()...");

            connect();

            try {
                //mSendQueue.put(sr);
                mQueues.put(sr);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void send(final String tid, final String fileName, BlockingClientStreamCallback cb) {
        send(tid, new File(fileName), cb);
    }

    public void send(final String tid, final File file, BlockingClientStreamCallback cb) {
        Log.d(TAG, "Client.send(" + file + ")");

        pool.execute(new SendHandler(tid, file, cb));

        Log.d(TAG, "Client.send(), request return synchronism..");
    }

    class RecvHandler implements Runnable {
        private final StreamRequest sr = new StreamRequest();
        public RecvHandler(String tid, int size, File file, BlockingClientStreamCallback cb) {
            sr.TransactId = tid;
            sr.isSend = false;

            sr.sendFile = null;

            sr.recvFile = file;
            sr.recvSize = size;

            sr.cb = cb;
        }

        public void run() {
            Log.i(TAG, "RecvHandler.run()...");

            connect();

            try {
                //mRecvQueue.put(sr);
                mQueues.put(sr);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void recv(final String tid, final int size, final String fileName, BlockingClientStreamCallback cb) {
        recv(tid, size, new File(fileName), cb);
    }

    public void recv(final String tid, final int size, final File file, BlockingClientStreamCallback cb) {
        Log.d(TAG, "Client.recv(" + tid + "," + size + "," + file + "(" +file.getAbsolutePath() +")" + ")");

        pool.execute(new RecvHandler(tid, size, file, cb));

        Log.d(TAG, "Client.recv(), request return synchronism..");
    }

    public class ClientStreamIoHandler extends StreamIoHandler {

        public ClientStreamIoHandler() {
        }

        @Override
        protected void processStreamIo(IoSession session, InputStream in,
                                       OutputStream out) {
            Log.d(TAG, "ClientStreamIoHandler.processStreamIo()...");
            pool.execute(new ClientWorker(session, in, out));
            return;
        }

        private class ClientWorker implements Runnable {
            private InputStream bis;
            private OutputStream bos;
            private IoSession mSession;

            public ClientWorker(IoSession session, InputStream in, OutputStream out) {
                Log.d(TAG, "ClientStreamIoHandler.ClientWorker()");
                mSession = session;
                bis = in;
                bos = out;
            }

            @Override
            public void run() {
                for (;;) {
                    StreamRequest sr = null;
                    try {
                        sr = mQueues.take();
                        Log.d(TAG, "ClientStreamIoHandler.ClientWorker.run(), tid="+sr.TransactId);
                        if (sr.isSend) {
                            Future send = pool.submit(new ClientSendWorker(mSession, bis, bos, sr));
                            send.get();
                        } else {
                            Future recv = pool.submit(new ClientRecvWorker(mSession, bis, bos, sr));
                            recv.get();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private class ClientSendWorker implements Runnable {
            private FileInputStream fis;
            private BufferedInputStream fbis;

            private InputStream bis;
            private OutputStream bos;
            private IoSession mSession;
            private StreamRequest sr;
            private BlockingClientStreamCallback cb;

            byte[] byteBuf = new byte[BUFFER_SIZE];
            public static final int BUFFER_SIZE = 1024*2;

            private int tick = 0;

            private ClientState state = ClientState.Initiate;

            public ClientSendWorker(IoSession session, InputStream in, OutputStream out, StreamRequest sr) {
                mSession = session;

                bis = in;
                bos = out;
                this.sr = sr;
                this.cb = sr.cb;
            }

            public void run() {
                Log.d(TAG, "ClientSendWorker.run()...");

                switch (state) {
                    case Initiate:
                        byteBuf[0] = 'S';
                        byteBuf[1] = 'N';
                        byteBuf[2] = 'D';
                        try {
                            Log.d(TAG, "ClientSendWorker.run(), before write, "+"\"SND\"");
                            cb.start();
                            bos.write(byteBuf, 0, 3);
                            Log.d(TAG, "ClientSendWorker.run(), after write");

                            bos.flush();

                            Log.d(TAG, "ClientSendWorker.run(), wait Connect result..");
                            if (bis.read(byteBuf, 0, 3) != -1) {
                                String ret = new String(byteBuf, 0, 3);
                                Log.d(TAG, "ClientSendWorker.run(), Connect result:"+ret);
                                if (byteBuf[0] == 'S'
                                        && byteBuf[1] == 'Y'
                                        && byteBuf[2] == 'N') {
                                    state = ClientState.Negotiate; //Start now
                                    cb.update(5);
                                } else if (byteBuf[0] == 'F'
                                        && byteBuf[1] == 'I'
                                        && byteBuf[2] == 'N') {
                                    state = ClientState.Initiate; //Failed
                                    cb.finish(-1, sr.TransactId, sr.sendFile);
                                    Log.d(TAG, "ClientSendWorker.run(), Connect failed, "+byteBuf.toString());
                                    break;
                                } else {
                                    state = ClientState.Initiate; //Failed
                                    cb.finish(-1, sr.TransactId, sr.sendFile);
                                    Log.d(TAG, "ClientSendWorker.run(), Connect failed!!!");
                                    break;
                                }
                            }
                            Log.d(TAG, "ClientSendWorker.run(), Connect OK!!!");
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            state = ClientState.Initiate;
                            cb.finish(-1, sr.TransactId, sr.sendFile);
                            break;
                        }
                        state = ClientState.Connect;

                    case Connect:
                        int i = 0;
                        for (Byte b : sr.TransactId.getBytes()) {
                            byteBuf[i++] = b;
                        }
                        try {
                            Log.d(TAG, "ClientSendWorker.run(), before write, "+sr.TransactId);
                            bos.write(byteBuf, 0, i);
                            Log.d(TAG, "ClientSendWorker.run(), after write");

                            bos.flush();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            state = ClientState.Initiate;
                            cb.finish(-2,  sr.TransactId, sr.sendFile);
                            break;
                        }
                        state = ClientState.Negotiate;

                    case Negotiate:
                        try {
                            Log.d(TAG, "ClientSendWorker.run(), wait Negotiate result..");
                            if (bis.read(byteBuf, 0, 3) != -1) {
                                String ret = new String(byteBuf, 0, 3);
                                Log.d(TAG, "ClientSendWorker.run(), Negotiate result:"+ret);
                                if (byteBuf[0] == 'A'
                                        && byteBuf[1] == 'C'
                                        && byteBuf[2] == 'K') {
                                    state = ClientState.Start; //Start now
                                    cb.update(10);
                                } else if (byteBuf[0] == 'F'
                                        && byteBuf[1] == 'I'
                                        && byteBuf[2] == 'N') {
                                    state = ClientState.Initiate; //Failed
                                    cb.finish(-3, sr.TransactId, sr.sendFile);
                                    Log.d(TAG, "ClientSendWorker.run(), Negotiate failed, wrong TID?"+byteBuf.toString());
                                    break;
                                } else {
                                    state = ClientState.Initiate; //Failed
                                    cb.finish(-3, sr.TransactId, sr.sendFile);
                                    Log.d(TAG, "ClientSendWorker.run(), Negotiate failed!!!");
                                    break;
                                }
                            }
                            Log.d(TAG, "ClientSendWorker.run(), Negotiate OK!!!");
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();

                            state = ClientState.Initiate; //Failed
                            cb.finish(-3, sr.TransactId, sr.sendFile);
                            //mSession.close();
                            break;
                        }

                    case Start:
                        Log.d(TAG, "ClientSendWorker.run(), start to write..");

                        int dataLen;
                        try {
                            fis = new FileInputStream(sr.sendFile);
                            fbis = new BufferedInputStream(fis);

                            long reqSize = sr.sendFile.length();
                            long total = 0;
                            while((dataLen = fbis.read(byteBuf)) != -1 ){
                                total += dataLen;
                                Log.i(TAG, "ClientSendWorker.run(), read[FILE] > BUF[" + dataLen + "] > write[IO]");
                                bos.write(byteBuf, 0, dataLen);
                                cb.update(10 + 90 * (total / reqSize));
                            }

                            bos.flush();

                            if (fbis != null) {
                                fbis.close();
                            }
                            if (fis != null) {
                                fis.close();
                            }

                            Log.d(TAG, "ClientSendWorker.run(), wait write result..");
                            if (bis.read(byteBuf, 0, 3) != -1) {
                                String ret = new String(byteBuf, 0, 3);
                                Log.d(TAG, "ClientSendWorker.run(), write result:"+ret);
                                if (byteBuf[0] == 'S'
                                        && byteBuf[1] == 'U'
                                        && byteBuf[2] == 'C') {
                                    state = ClientState.Initiate; //Finish successful
                                    cb.finish(0, sr.TransactId, sr.sendFile);
                                } else {
                                    state = ClientState.Initiate; //Finish failed
                                    cb.finish(-4, sr.TransactId, sr.sendFile);
                                    Log.d(TAG, "ClientSendWorker.run(), write failed!!!"+byteBuf.toString());
                                }
                            }
                            Log.d(TAG, "ClientSendWorker.run(), write OK!!!");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            state = ClientState.Initiate;
                        }
                        break;

                    default:
                        break;
                }
            }
        }

        private class ClientRecvWorker implements Runnable {
            private FileOutputStream fos;
            private BufferedOutputStream fbos;

            private InputStream bis;
            private OutputStream bos;
            private IoSession mSession;
            private StreamRequest sr;
            private BlockingClientStreamCallback cb;

            byte[] byteBuf = new byte[BUFFER_SIZE];
            public static final int BUFFER_SIZE = 1024*2;

            private int tick = 0;

            private ClientState state = ClientState.Initiate;

            public ClientRecvWorker(IoSession session, InputStream in, OutputStream out,
                                    StreamRequest sr) {
                mSession = session;

                bis = in;
                bos = out;
                this.sr = sr;
                this.cb = sr.cb;
            }

            public void run() {
                Log.d(TAG, "ClientRecvWorker.run()...");

                switch (state) {
                    case Initiate:
                        byteBuf[0] = 'R';
                        byteBuf[1] = 'C';
                        byteBuf[2] = 'V';
                        try {
                            Log.d(TAG, "ClientRecvWorker.run(), before write, "+"\"RCV\"");
                            cb.start();
                            bos.write(byteBuf, 0, 3);
                            Log.d(TAG, "ClientRecvWorker.run(), after write");

                            bos.flush();

                            Log.d(TAG, "ClientRecvWorker.run(), wait Connect result..");
                            if (bis.read(byteBuf, 0, 3) != -1) {
                                String ret = new String(byteBuf, 0, 3);
                                Log.d(TAG, "ClientRecvWorker.run(), Connect result:"+ret);
                                if (byteBuf[0] == 'S'
                                        && byteBuf[1] == 'Y'
                                        && byteBuf[2] == 'N') {
                                    state = ClientState.Negotiate; //Start now
                                    cb.update(5);
                                } else if (byteBuf[0] == 'F'
                                        && byteBuf[1] == 'I'
                                        && byteBuf[2] == 'N') {
                                    state = ClientState.Initiate; //Failed
                                    cb.finish(-1,  sr.TransactId, sr.recvFile);
                                    Log.d(TAG, "ClientRecvWorker.run(), Connect failed, "+byteBuf.toString());
                                    break;
                                } else {
                                    state = ClientState.Initiate; //Failed
                                    cb.finish(-1, sr.TransactId, sr.recvFile);
                                    Log.d(TAG, "ClientRecvWorker.run(), Connect failed!!!");
                                    break;
                                }
                            }
                            Log.d(TAG, "ClientRecvWorker.run(), Connect OK!!!");
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            state = ClientState.Initiate;
                            cb.finish(-1, sr.TransactId, sr.recvFile);
                            break;
                        }
                        state = ClientState.Connect;

                    case Connect:
                        int i = 0;
                        for (Byte b : sr.TransactId.getBytes()) {
                            byteBuf[i++] = b;
                        }
                        try {
                            Log.d(TAG, "ClientRecvWorker.run(), before write, "+sr.TransactId);
                            bos.write(byteBuf, 0, i);
                            Log.d(TAG, "ClientRecvWorker.run(), after write");

                            bos.flush();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            state = ClientState.Initiate;
                            cb.finish(-2,  sr.TransactId, sr.recvFile);
                            break;
                        }
                        state = ClientState.Negotiate;

                    case Negotiate:
                        try {
                            Log.d(TAG, "ClientRecvWorker.run(), wait Negotiate result..");
                            if (bis.read(byteBuf, 0, 3) != -1) {
                                String ret = new String(byteBuf, 0, 3);
                                Log.d(TAG, "ClientRecvWorker.run(), Negotiate result:"+ret);
                                if (byteBuf[0] == 'A'
                                        && byteBuf[1] == 'C'
                                        && byteBuf[2] == 'K') {
                                    state = ClientState.Start; //Start now
                                    cb.update(10);
                                } else if (byteBuf[0] == 'F'
                                        && byteBuf[1] == 'I'
                                        && byteBuf[2] == 'N') {
                                    state = ClientState.Initiate; //Failed
                                    cb.finish(-3, sr.TransactId, sr.recvFile);
                                    Log.d(TAG, "ClientRecvWorker.run(), Negotiate failed, wrong TID?"+byteBuf.toString());
                                    break;
                                } else {
                                    state = ClientState.Initiate; //Failed
                                    cb.finish(-3, sr.TransactId, sr.recvFile);
                                    Log.d(TAG, "ClientRecvWorker.run(), Negotiate failed!!!");
                                    break;
                                }
                            }
                            Log.d(TAG, "ClientRecvWorker.run(), Negotiate OK!!!");
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();

                            state = ClientState.Initiate; //Failed
                            cb.finish(-3, sr.TransactId, sr.recvFile);
                            //mSession.close();
                            break;
                        }

                    case Start:
                        Log.d(TAG, "ClientRecvWorker.run(), start to write..");

                        int dataLen = 0;
                        try {
                            fos = new FileOutputStream(sr.recvFile);
                            fbos = new BufferedOutputStream(fos);

                            long reqSize = sr.recvSize;
                            long total = 0;
                            while((dataLen = bis.read(byteBuf)) != -1){
                                total += dataLen;
                                Log.d(TAG, "ClientRecvWorker.run(), RECV TOTAL:"+total+", read[IO] > BUF["+dataLen+"] > write[FILE]");
                                fbos.write(byteBuf, 0, dataLen);
                                cb.update(10 + 90 * (total / reqSize));
                                if (total >= reqSize) {
                                    Log.d(TAG, "ClientRecvWorker.run(), RECV Finished:"+total);
                                    break;
                                }
                            }

                            fbos.flush();

                            byteBuf[0] = "SUC".getBytes()[0];
                            byteBuf[1] = "SUC".getBytes()[1];
                            byteBuf[2] = "SUC".getBytes()[2];
                            bos.write(byteBuf, 0, 3);
                            //log.debug("ServerWorker.run(), write ack");
                            bos.flush();

                            //cb.update(100);
                            if (fbos != null) {
                                fbos.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                            cb.finish(0, sr.TransactId, sr.recvFile);
                            Log.d(TAG, "ClientRecvWorker.run(), write OK!!!");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            state = ClientState.Initiate;
                        }
                        break;

                    default:
                        break;
                }

            }
        }
    }
}