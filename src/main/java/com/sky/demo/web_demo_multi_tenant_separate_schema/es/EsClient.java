package com.sky.demo.web_demo_multi_tenant_separate_schema.es;

import com.sky.demo.web_demo_multi_tenant_separate_schema.util.AppConfig;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by user on 16/11/28.
 */
@Component
public class EsClient {

    private static final Logger logger = LoggerFactory.getLogger(EsClient.class);

    private TransportClient transportClient;

    public TransportClient getTransportClient() {
        return transportClient;
    }

    public void setTransportClient(TransportClient transportClient) {
        this.transportClient = transportClient;
    }

    @PostConstruct
    public void init() {
        Settings settings = Settings.builder()
                .put("cluster.name", AppConfig.getItem("es.cluster.name"))
                .put("client.transport.sniff", true)
                .build();

        try {
            transportClient = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName(AppConfig.getItem("es.server.host")),
                            Integer.parseInt(AppConfig.getItem("es.server.port"))));
        } catch (UnknownHostException e) {
            logger.error("build TransportClient error", e);
        }

    }

    @PreDestroy
    public void destroy() {
        if (transportClient != null) {
            transportClient.close();
        }
    }






}
