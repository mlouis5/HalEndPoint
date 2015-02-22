/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints;

import com.mac.config.DataSourceConfig;
import com.mac.eiscp.interfaces.Device;
import com.mac.eiscp.services.protocols.Protocol;
import com.mac.eiscp.services.utilities.JsonConverter;
import com.mac.haldao.daos.impl.PresetRepository;
import com.mac.haldao.daos.pojos.Preset;
import com.mac.halendpoint.endpoints.listeners.UpdatedStateListener;
import com.mac.halendpoint.endpoints.managers.WebSocketManager;
import com.mac.halendpoint.endpoints.managers.impl.WebSocketManagerImpl;
import com.mac.halendpoint.endpoints.rectifiers.StateRectifier;
import com.mac.halendpoint.endpoints.rectifiers.impl.DeviceStateRectifier;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author Mac
 */
@Component
public class HalSocket extends WebSocketServer implements UpdatedStateListener {

    private static final int PORT = 9092;

    private final WebSocketManager socketManager;
    private final ProtocolRouter router;
    private final StateRectifier deviceManager;
    private boolean isFirstConnection;
    private ApplicationContext ctx;
    private final PresetRepository presetRepo;

    public HalSocket() {
        super(new InetSocketAddress(PORT));
        
        ctx = new AnnotationConfigApplicationContext(DataSourceConfig.class);
        presetRepo = ctx.getBean(PresetRepository.class);
        
        isFirstConnection = true;
        socketManager = new WebSocketManagerImpl();
        router = new ProtocolRouter();
        deviceManager = new DeviceStateRectifier();
        deviceManager.setUpdatedStateListener(this);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        socketManager.addWebSocket(ws, ch);

        if (isFirstConnection) {
            isFirstConnection = false;
            Protocol protocol = new Protocol();
            protocol.setIsModified(false);
            protocol.setRoutedTo("listen");
            protocol.setRespondTo("socket");
            protocol.setParameters("ws", "local", String.valueOf(PORT));
            try {
                router.router(protocol);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException ex) {
                Logger.getLogger(HalSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void onClose(WebSocket ws, int i, String string, boolean bln) {
        if (Objects.nonNull(ws)) {
            socketManager.removeSocket(ws);
        }
    }

    @Override
    public void onMessage(WebSocket ws, String message) {
        Logger.getLogger(HalSocket.class.getName()).log(Level.INFO, message, message);
        if (Objects.nonNull(message) && !message.isEmpty()) {
            try {
                if(message.contains("save")){
                    
                }else if (message.contains("message")) {
                    Protocol protocol = new Protocol();
                    protocol.setIsModified(true);
                    protocol.setRespondTo("update");
                    protocol.setResponse(message);

                    deviceManager.updateState(protocol);
                } else {
                    System.out.println("MESSAGE BEFORE CONVERSION: " + message);
                    Protocol protocol = (Protocol) JsonConverter.fromJsonString(message, Protocol.class);

                    Protocol returnedProtocol = router.router(protocol);
                    
                    if(returnedProtocol.getRoutedTo().equalsIgnoreCase("device") 
                            && returnedProtocol.getRespondTo()
                                    .equalsIgnoreCase("device")){
                        String device = returnedProtocol.getResponse();
                        Device dev = (Device) JsonConverter.fromJsonString(device, Device.class);
                        ((DeviceStateRectifier) deviceManager).manageDevice(dev);
                    }

                    String stringProto = JsonConverter.toJsonString(returnedProtocol);
                    socketManager.sendMessageToAllBrowserSockets(stringProto);
                }
            } catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(HalSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void onError(WebSocket ws, Exception excptn) {
        if (Objects.nonNull(ws)) {
            if (ws.isOpen()) {
                ws.close();
            }
            socketManager.removeSocket(ws);
        }
    }

    @Override
    public void stateUpdated(Protocol protocol) {
        try {
            String stringProto = JsonConverter.toJsonString(protocol);            
            socketManager.sendMessageToAllBrowserSockets(stringProto);
        } catch (IOException ex) {
            Logger.getLogger(HalSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void queryForState(Protocol protocol) {
        try {
            router.router(protocol);
        } catch (IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException | IOException ex) {
            Logger.getLogger(HalSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

}
