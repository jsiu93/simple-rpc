package com.xzh.xdubbo.api;

public class GreetingsServiceImpl implements GreetingsService {
    @Override
    public String sayHi(String name) {
        return "hi " + name;
    }
}
