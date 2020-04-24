package com.xzh.xdubbo.provider;

import com.xzh.xdubbo.api.GreetingsServiceImpl;
import com.xzh.xdubbo.lib.XDubboProvider;

import java.io.IOException;

public class ProviderApp {
    public static void main(String[] args) throws IOException {
        new XDubboProvider<>(new GreetingsServiceImpl()).start();
        System.in.read();
    }
}
