package com.xzh.xdubbo.lib;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class XDubboNioProvider<T> {
    private T serviceImpl;

    private ServerSocketChannel serverSocketChannel;

    private static class SocketData {
        StringBuilder sb = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        boolean isReading = true;

        public void append(int bytesRead) {
            byte[] tmp = new byte[bytesRead];
            buffer.get(tmp);
            sb.append(new String(tmp));
        }
    }

    public XDubboNioProvider(T serviceImpl) throws IOException {
        this.serviceImpl = serviceImpl;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.bind(new InetSocketAddress(8888));
    }

    public void start() throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel client = channel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new SocketData());

                }

                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    SocketData socketData = (SocketData) key.attachment();

                    int bytesRead = client.read(socketData.buffer);
                    if (bytesRead > 0) {
                        socketData.buffer.flip();
                        socketData.append(bytesRead);

                        if (socketData.sb.toString().contains("\n")) {

                            MethodInfo methodInfo = JSON.parseObject(socketData.sb.toString(), MethodInfo.class);
                            Method method = serviceImpl.getClass().getMethod(methodInfo.getMethodName(),
                                    methodInfo.getParams().stream().map(Object::getClass).toArray(Class[]::new));
                            Object returnValue = method.invoke(serviceImpl, methodInfo.getParams().toArray());

                            byte[] returnValueBytes = ((JSON.toJSONString(returnValue) + "\n").getBytes());
                            socketData.buffer.flip();
                            socketData.isReading = false;

                            socketData.buffer.put(returnValueBytes);
                        }
                    }
                }

                if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    SocketData socketData = (SocketData) key.attachment();
                    if (!socketData.isReading) {
                        socketData.isReading = true;

                        socketData.buffer.flip();

                        while (socketData.buffer.hasRemaining()) {
                            client.write(socketData.buffer);
                        }
                        client.close();

                    }
                }
            }
        }

    }
}
