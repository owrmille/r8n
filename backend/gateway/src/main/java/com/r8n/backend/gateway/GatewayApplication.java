package com.r8n.backend.gateway;

public class GatewayApplication {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new GatewayApplication().getGreeting());
    }
}
