package com.lightmsg.service;

import android.util.Log;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.handler.stream.StreamIoHandler;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientStream {
    private static final String TAG = ClientStream.class.getSimpleName();

    NioSocketConnector connector;

    public enum ClientState {
        Initiate,
        Negotiate,
        Start;
    }

    public ClientStream() {
        connector = new NioSocketConnector();

        //ObjectSerializationCodecFactory factory = new ObjectSerializationCodecFactory(); 
        //factory.setDecoderMaxObjectSize(Integer.MAX_VALUE);
        //factory.setEncoderMaxObjectSize(Integer.MAX_VALUE);
        //connector.getFilterChain().addLast("codec",new ProtocolCodecFilter(factory));

        // Configure the service.
        connector.setConnectTimeoutMillis(5000);
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.setHandler(new ClientStreamIoHandler());
    }

    public void connect() {
        new Thread() {
            public void run() {
                connect0();
            }
        }.start();
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
                System.err.println("Failed to connect.");
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }

        // wait until the summation is done
        session.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
        dispose();
        Log.d(TAG, "connect0(), NioSocketConnector stopped...");
    }

    public void dispose() {
    }

    public void send(final String tid, final String fileName) {
        send(tid, new File(fileName));
    }

    public void send(final String tid, final File file) {
        Log.d(TAG, "Client.send("+file+")");

        new Thread() {
            public void run() {
                Log.i(TAG, "Client.send(), Thread.start...");
                StreamRequest sr = new StreamRequest();
                sr.TransactId = tid;
                sr.sendFile = file;
                sr.recvFile = null;

                synchronized(mReqs) {
                    mTids.add(tid);
                    mReqs.put(tid, sr);
                }

                if (!connector.isActive()) {
                    connect();
                }
            }
        }.start();
        Log.d(TAG, "Client.send(), request return synchronism..");
    }

    class StreamRequest {
        public String TransactId;
        public File sendFile;
        public File recvFile;
    }

    private final HashMap<String, StreamRequest> mReqs = new HashMap<String, StreamRequest>();
    private final ArrayList<String> mTids = new ArrayList<String>();
    private final BlockingQueue<StreamRequest> mQue = new LinkedBlockingQueue<StreamRequest>();

    public class ClientStreamIoHandler extends StreamIoHandler {

        public ClientStreamIoHandler() {
        }

        @Override
        protected void processStreamIo(IoSession session, InputStream in,
                OutputStream out) {
            Log.d(TAG, "ClientStreamIoHandler.processStreamIo()...");

            Log.d(TAG, "ClientStreamIoHandler start ClientWorker...");
            new ClientOutWorker(session, in, out).start();
            //new ClientOutWorker(session, in, out).start();
            return;
        }

        private class ClientOutWorker extends Thread {
            private FileInputStream fis;

            private BufferedInputStream fbis;
            /*private BufferedInputStream bis;
            private BufferedOutputStream bos;*/

            private InputStream bis;
            private OutputStream bos;

            IoSession mSession;

            byte[] byteBuf = new byte[BUFFER_SIZE];
            public static final int BUFFER_SIZE = 1024*2;

            private int tick = 0;
            
            private ClientState state = ClientState.Initiate;

            public ClientOutWorker(IoSession session, InputStream in, OutputStream out) {
                mSession = session;

                bis = in;//new BufferedInputStream(in);
                bos = out;//new BufferedOutputStream(out);
            }

            public void run() {
                Log.d(TAG, "ClientOutWorker.run()...");

                for (;;) {
                    if (mTids.isEmpty()) {
                        try {
                            Log.d(TAG, "ClientOutWorker.run(), Idle to sleep 1000ms...");
                            if (tick > 60) {
                                mSession.close(true).awaitUninterruptibly();
                                return;
                            }
                            tick++;
                            sleep(2000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        continue;
                    }
                    
                    tick = 0;
                    Iterator<String> it = null;
                    synchronized(mTids) {
                        it = mTids.iterator();
                    }

                    while (it != null && it.hasNext()) {
                        String tid = it.next();
                        StreamRequest sr = null;
                        
                        synchronized(mTids) {
                            sr = mReqs.get(tid);
                            mReqs.remove(tid);
                            mTids.remove(tid);
                        }

                        switch (state) {
                        case Initiate:
                            sr.TransactId = "HELO";
                            int i = 0;
                            for (Byte b : sr.TransactId.getBytes()) {
                                byteBuf[i++] = b;
                            }
                            try {
                                Log.d(TAG, "ClientOutWorker.run(), before write");
                                bos.write(byteBuf, 0, i);
                                Log.d(TAG, "ClientOutWorker.run(), after write");

                                bos.flush();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                state = ClientState.Initiate;
                                break;
                            }
                            state = ClientState.Negotiate;

                        case Negotiate:
                            try {
                                Log.d(TAG, "ClientWorker.run(), wait negotiation result..");
                                if (bis.read(byteBuf, 0, 3) != -1) {
                                    String ret = new String(byteBuf, 0, 3);
                                    Log.d(TAG, "ClientWorker.run(), negotiation result:"+ret);
                                    if (byteBuf[0] == 'S'
                                            && byteBuf[1] == 'Y'
                                            && byteBuf[2] == 'N') {
                                        state = ClientState.Start; //Start now
                                    } else if (byteBuf[0] == 'F'
                                            && byteBuf[1] == 'I'
                                            && byteBuf[2] == 'N') {
                                        state = ClientState.Initiate; //Failed
                                        Log.d(TAG, "ClientWorker.run(), negotiation failed, wrong TID?"+byteBuf.toString());
                                        break;
                                    } else {
                                        state = ClientState.Initiate; //Failed
                                        Log.d(TAG, "ClientWorker.run(), negotiation failed!!!");
                                        break;
                                    }
                                }
                                Log.d(TAG, "ClientWorker.run(), negotiation OK!!!");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();

                                state = ClientState.Initiate; //Failed
                                //mSession.close();
                                break;
                            }

                        case Start:
                            Log.d(TAG, "ClientWorker.run(), start to write..");

                            int dataLen = 0;
                            try {
                                fis = new FileInputStream(sr.sendFile);
                                fbis = new BufferedInputStream(fis);

                                while((dataLen = fbis.read(byteBuf)) != -1 ){
                                    Log.i(TAG, "ClientOutWorker.run(), read[FILE] > BUF["+dataLen+"] > write[IO]");
                                    bos.write(byteBuf, 0, dataLen);
                                }

                                bos.flush();

                                Log.d(TAG, "ClientWorker.run(), wait write result..");
                                if (bis.read(byteBuf, 0, 3) != -1) {
                                    String ret = new String(byteBuf, 0, 3);
                                    Log.d(TAG, "ClientWorker.run(), write result:"+ret);
                                    if (byteBuf[0] == 'A'
                                            && byteBuf[1] == 'C'
                                            && byteBuf[2] == 'K') {
                                        state = ClientState.Start; //Start now
                                    } else {
                                        state = ClientState.Initiate; //Failed
                                        Log.d(TAG, "ClientWorker.run(), write failed!!!"+byteBuf.toString());
                                        break;
                                    }
                                }
                                Log.d(TAG, "ClientWorker.run(), write OK!!!");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                state = ClientState.Initiate;
                            }

                        default:
                            break;
                        }

                        try {
                            if (fbis != null) {
                                fbis.close();
                            }
                            if (fis != null) {
                                fis.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                /*try {
                    bis.close();
                    bos.close();
                    fbis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        }

        /*private class ClientWorker extends Thread {
            private InputStream is;
            private FileInputStream fis;
            private OutputStream os;
            private BufferedInputStream bis;
            private BufferedInputStream fbis;
            private BufferedOutputStream bos;

            IoSession mSession;

            byte[] byteBuf = new byte[BUFFER_SIZE];
            public static final int BUFFER_SIZE = 1024*2;

            private ClientState state = ClientState.Initiate;

            public ClientWorker(IoSession session, InputStream in, OutputStream out) {
                mSession = session;
                is = in;
                os = out;
                bis = new BufferedInputStream(in);
                bos = new BufferedOutputStream(out);

                try {
                    fis = new FileInputStream(sendFile);
                    fbis = new BufferedInputStream(fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            public void run() {
                Log.d(TAG, "ClientWorker.run()...");

                try {
                    sleep(3000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                //TransactId = "HELO";
                int i = 0;
                for (Byte b : TransactId.getBytes()) {
                    byteBuf[i++] = b;
                }

                if (state == ClientState.Initiate) {
                    try {
                        Log.d(TAG, "ClientWorker.run(), before write, "+TransactId.getBytes()[0]);
                        bos.write(byteBuf, 0, i);
                        Log.d(TAG, "ClientWorker.run(), after write");

                        try {
                            bos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    state = ClientState.Negotiate;
                }
                if (state == ClientState.Negotiate) {
                    //<message id="8EE0p-7"
                    //byte[] TidByte = new byte[64];
                    try {
                        Log.d(TAG, "ClientWorker.run(), before read");
                        if (bis.read(byteBuf) != -1) {
                            if (byteBuf[0] == 'T') {
                                state = ClientState.Start; //Start now
                            } else if (byteBuf[0] == 'F') {
                                state = ClientState.Initiate; //Failed
                                mSession.close();
                                return;
                            } else {
                                state = ClientState.Initiate; //Failed
                                mSession.close();
                                return;
                            }
                        }
                        Log.d(TAG, "ClientWorker.run(), after read");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                        state = ClientState.Initiate; //Failed
                        mSession.close();
                        return;
                    }
                }

                int dataLen = 0; 
                try {
                    while((dataLen = fbis.read(byteBuf)) != -1 ){
                        Log.i(TAG, "ClientWorker.run(), read > BUF["+dataLen+"] > write");
                        bos.write(byteBuf, 0, dataLen);
                    }

                    try {
                        bos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bos.close();
                        bis.close();
                        fbis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    state = ClientState.Initiate;
                }
            }
        }*/
    }
}