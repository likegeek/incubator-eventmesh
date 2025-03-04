/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.grpc.sub;

import org.apache.eventmesh.client.grpc.config.EventMeshGrpcClientConfig;
import org.apache.eventmesh.client.grpc.consumer.EventMeshGrpcConsumer;
import org.apache.eventmesh.client.grpc.consumer.ReceiveMsgHook;
import org.apache.eventmesh.client.tcp.common.EventMeshCommon;
import org.apache.eventmesh.common.protocol.SubscriptionItem;
import org.apache.eventmesh.common.protocol.SubscriptionMode;
import org.apache.eventmesh.common.protocol.SubscriptionType;
import org.apache.eventmesh.util.Utils;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import io.cloudevents.CloudEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CloudEventsSubscribeReply implements ReceiveMsgHook<CloudEvent> {

    public static CloudEventsSubscribeReply handler = new CloudEventsSubscribeReply();

    public static void main(String[] args) throws InterruptedException {
        Properties properties = Utils.readPropertiesFile("application.properties");
        final String eventMeshIp = properties.getProperty("eventmesh.ip");
        final String eventMeshGrpcPort = properties.getProperty("eventmesh.grpc.port");

        final String topic = "TEST-TOPIC-GRPC-RR";

        EventMeshGrpcClientConfig eventMeshClientConfig = EventMeshGrpcClientConfig.builder()
            .serverAddr(eventMeshIp)
            .serverPort(Integer.parseInt(eventMeshGrpcPort))
            .consumerGroup("EventMeshTest-consumerGroup")
            .env("env").idc("idc")
            .sys("1234").build();

        SubscriptionItem subscriptionItem = new SubscriptionItem();
        subscriptionItem.setTopic(topic);
        subscriptionItem.setMode(SubscriptionMode.CLUSTERING);
        subscriptionItem.setType(SubscriptionType.SYNC);


        EventMeshGrpcConsumer eventMeshGrpcConsumer = new EventMeshGrpcConsumer(eventMeshClientConfig);

        eventMeshGrpcConsumer.init();

        eventMeshGrpcConsumer.registerListener(handler);

        eventMeshGrpcConsumer.subscribe(Collections.singletonList(subscriptionItem));

        Thread.sleep(60000);
        eventMeshGrpcConsumer.unsubscribe(Collections.singletonList(subscriptionItem));
    }

    @Override
    public Optional<CloudEvent> handle(CloudEvent msg) {
        log.info("receive request-reply msg====================={}", msg);
        if (msg != null) {
            return Optional.of(msg);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getProtocolType() {
        return EventMeshCommon.CLOUD_EVENTS_PROTOCOL_NAME;
    }
}
