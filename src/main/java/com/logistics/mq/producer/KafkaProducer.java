package com.logistics.mq.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// kafka生产者
@Component
public class KafkaProducer {
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendTrack(String orderId, Integer node, String address) {
        kafkaTemplate.send("track-topic", orderId, orderId + "|" + node + "|" + address);
    }
}
