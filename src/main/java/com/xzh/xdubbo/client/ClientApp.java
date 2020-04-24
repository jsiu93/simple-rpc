package com.xzh.xdubbo.client;

import com.xzh.xdubbo.api.GreetingsService;
import com.xzh.xdubbo.lib.XDubboClient;

import java.io.IOException;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        GreetingsService service = new XDubboClient<>(GreetingsService.class).getRef();
        String returnValue = service.sayHi("666");
        System.err.println(returnValue);
    }
}
