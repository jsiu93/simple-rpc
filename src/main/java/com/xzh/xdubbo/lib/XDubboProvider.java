package com.xzh.xdubbo.lib;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class XDubboProvider<T> {

    private T serviceImpl;

    private ServerSocket serverSocket;

    public XDubboProvider(T serviceImpl) throws IOException {
        this.serviceImpl = serviceImpl;
        this.serverSocket = new ServerSocket(8888);

    }

    public void start() throws IOException {

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("accepted");;

            new WorkerThread(socket).start();

        }
    }

    private class WorkerThread extends Thread{
        private Socket socket;

        public WorkerThread(Socket socket) throws IOException {
            this.socket = socket;

        }

        @Override
        public void run() {

            try {
                String line = new BufferedReader(new InputStreamReader((socket.getInputStream()))).readLine();

                MethodInfo methodInfo = JSON.parseObject(line, MethodInfo.class);
                Method method = serviceImpl.getClass().getMethod(methodInfo.getMethodName(),
                        methodInfo.getParams().stream().map(Object::getClass).toArray(Class[]::new));
                Object returnValue = method.invoke(serviceImpl, methodInfo.getParams().toArray());

                socket.getOutputStream().write((JSON.toJSONString(returnValue) + "\n").getBytes());
                socket.getOutputStream().flush();

            } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
