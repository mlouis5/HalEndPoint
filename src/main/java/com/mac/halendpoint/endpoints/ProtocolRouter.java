/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints;

import com.mac.eiscp.interfaces.Device;
import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.impl.SingleDevice;
import com.mac.eiscp.services.controllers.status.Status;
import com.mac.eiscp.services.protocols.FailedProtocol;
import com.mac.eiscp.services.protocols.Protocol;
import com.mac.eiscp.services.protocols.annotations.ProtocolMapping;
import com.mac.eiscp.services.utilities.JsonConverter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Mac
 */
public class ProtocolRouter {

    private static final Logger log = LoggerFactory.getLogger(ProtocolRouter.class);

    private static final String BASE_RESOURCE_PATH = "http://localhost:9090/";

    public Protocol router(Protocol protocol) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException {

        log.info("incoming protocol: " + protocol);
        if (Objects.nonNull(protocol)) {
            Method[] methods = this.getClass().getDeclaredMethods();
            log.info("protocol NOT null");
            for (Method method : methods) {
                if (!method.getName().equals("router")) {
                    log.info("current method: " + method.getName());
                    method.setAccessible(true);
                    ProtocolMapping protoMapping = method.getAnnotation(ProtocolMapping.class);
                    log.info("ProtocolMapping: " + protoMapping);
                    if (Objects.nonNull(protoMapping)) {
                        log.info("ProtocolMapping not null");
                        if (protoMapping.routeTo().equalsIgnoreCase(protocol.getRoutedTo())) {
                            log.info("routing to: " + protoMapping.routeTo());
                            int numParams = protoMapping.numParams();
                            String[] parameters = protocol.getParameters();
                            if (Objects.nonNull(parameters) && parameters.length == numParams) {
                                log.info("parameter lengths match");
                                Object response = method.invoke(this, (Object[]) parameters);
                                log.info("response after invocation");
                                protocol.setRespondTo(protoMapping.respondTo());
                                if (!(response instanceof String)) {
                                    protocol.setResponse(JsonConverter.toJsonString(response));
                                } else {
                                    protocol.setResponse((String) response);
                                }
                                protocol.setIsModified(true);
                                log.info(response.toString());
                                log.info("final protocol: " + protocol);
                                log.info("final protocol: " + protocol.getRespondTo());
                                log.info("final protocol: " + protocol.getResponse());
                                log.info("final protocol: " + protocol.getRoutedTo());
                                log.info("final protocol: " + Arrays.toString(protocol.getParameters()));
                            }
                        }
                    }
                    method.setAccessible(false);
                }
            }
            return protocol;
        }
        return new FailedProtocol();
    }

    @ProtocolMapping(routeTo = "device", respondTo = "device", numParams = 1)
    private Device device(String deviceName) {
        log.info("device method called, with deviceName as: " + deviceName);
        String uri = formUri(BASE_RESOURCE_PATH, deviceName);
        RestTemplate template = new RestTemplate();
        return template.getForObject(BASE_RESOURCE_PATH + deviceName, SingleDevice.class);
    }

    @ProtocolMapping(routeTo = "command", respondTo = "status", numParams = 2)
    private Status command(String deviceName, String command) throws Exception {
        Status stat = Status.FAILED;
        if (Objects.nonNull(deviceName) && Objects.nonNull(command)) {
            String uri = formUri(BASE_RESOURCE_PATH, deviceName, command);
            RestTemplate template = new RestTemplate();
            stat = template.getForObject(uri, Status.class);
        }
        return stat;
    }

    @ProtocolMapping(routeTo = "query", respondTo = "query", numParams = 2)
    private Message query(String deviceName, String command) throws Exception {
        Message msg = null;
        if (Objects.nonNull(deviceName) && Objects.nonNull(command)) {
            String uri = formUri(BASE_RESOURCE_PATH, "query", deviceName, command);
            RestTemplate template = new RestTemplate();
            msg = template.getForObject(uri, Message.class);
        }
        return msg;
    }

    @ProtocolMapping(routeTo = "listen", respondTo = "socket", numParams = 3)
    private String listen(String protocol, String host, String port) throws Exception {
        if(Objects.nonNull(protocol) && Objects.nonNull(host) && Objects.nonNull(port)){
            String uri = formUri(BASE_RESOURCE_PATH, "listener", protocol, host, port);
            System.out.println("URI: " + uri);
            RestTemplate template = new RestTemplate();
            return template.getForObject(uri, String.class);
        }
        return Status.NOT_MODIFIED.name();
    }

    @ProtocolMapping(routeTo = "view", respondTo = "route", numParams = 1)
    private String router(String viewName) throws Exception {
        if (Objects.nonNull(viewName)) {
            return viewName;
        }
        return Status.NOT_MODIFIED.name();
    }

    private String formUri(String base, String... paths) {
        if (Objects.nonNull(base)) {
            StringBuilder sb = new StringBuilder(base);
            if (Objects.nonNull(paths)) {
                int count = paths.length - 1;
                for (String path : paths) {
                    sb.append(path);
                    if(count > 0){
                        sb.append("/");
                        count--;
                    }
                }
                return sb.toString();
            }
        }
        return base;
    }
}
