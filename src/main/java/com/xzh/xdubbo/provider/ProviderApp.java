package com.xzh.xdubbo.provider;

import com.xzh.xdubbo.api.GreetingsServiceImpl;
import com.xzh.xdubbo.lib.XDubboNioProvider;
import com.xzh.xdubbo.lib.XDubboProvider;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ProviderApp {
    public static void main(String[] args) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        new XDubboNioProvider<>(new GreetingsServiceImpl()).start();
        System.in.read();
    }
}
