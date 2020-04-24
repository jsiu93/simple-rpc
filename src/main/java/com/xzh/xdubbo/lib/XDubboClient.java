package com.xzh.xdubbo.lib;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XDubboClient<T> {
    private Class<T> interfaceClass;
    private Socket socket;


    public XDubboClient(Class<T> interfaceClass) throws IOException {
        this.interfaceClass = interfaceClass;
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress("127.0.0.1", Integer.parseInt("8888")));
        System.err.println("connected");

    }

    public T getRef() {

        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                MethodInfo methodInfo = new MethodInfo(method.getName(), Stream.of(args).collect(Collectors.toList()));
                XDubboClient.this.socket.getOutputStream().write((JSON.toJSONString(methodInfo) + "\n").getBytes());
                XDubboClient.this.socket.getOutputStream().flush();

                String returnValueJson = new BufferedReader(new InputStreamReader(XDubboClient.this.socket.getInputStream())).readLine();
                System.err.println(returnValueJson);
                return JSON.parse(returnValueJson);
            }
        });
    }



}
