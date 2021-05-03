package com.example.controller;

import org.springframework.web.bind.annotation.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/amqplib/callback_api")
public class MessagingController {
    private final static String QUEUE_NAME = "pedidos_node";
    private String message = "";

    @PostMapping
    public static String Producer() throws Exception{
        String message;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            message =  "{"                                                       +
                       "data: 01/01/2021,"                                       +
                       "nome: Gian Eric,"                                        +
                       "servicos: [{descricao: Instalação do motor de arranque},"+
                       "{descricao: Instalação do amortecedor},"                 +
                       "{descricao: Troca da bomba de gasolina}]"                +
                       "}";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            return " [x] Sent. '" + message + "'";
        }
    }

    @GetMapping
    public String Consumer() throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println("[*] Waiting for messages.");

        channel.basicQos(1);

        //Wait messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            message = new String(delivery.getBody(), "UTF-8");

            System.out.println(" [x] Received '" + message + "'");
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        return " [x] Received '" + message + "'";
    }
}
