/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints.managers.impl;

import com.google.common.collect.Maps;
import com.mac.halendpoint.endpoints.managers.WebSocketManager;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 *
 * @author Mac
 */
public class WebSocketManagerImpl implements WebSocketManager {

    Map<ClientHandshake, WebSocket> sockets;

    public WebSocketManagerImpl() {
        sockets = Maps.newConcurrentMap();
    }

    @Override
    public void addWebSocket(WebSocket peer, ClientHandshake ch) {
        if (Objects.nonNull(peer)) {
            sockets.put(ch, peer);
        }
    }

    @Override
    public void clearAllWebSockets() {
        sockets.clear();
    }

    @Override
    public boolean hasLiveSockets() {
        return sockets.entrySet().stream().anyMatch((entry) -> (entry.getValue().isOpen()));
    }

    @Override
    public boolean isEmpty() {
        return sockets.isEmpty();
    }

    @Override
    public void removeClosedSockets() {
        sockets.entrySet().stream().filter((entry) -> (entry.getValue().isClosed())).forEach((entry) -> {
            sockets.remove(entry.getKey());
        });
    }

    @Override
    public void removeSocket(WebSocket peer) {
        if (Objects.nonNull(peer)) {
            Optional<Entry<ClientHandshake, WebSocket>> toBeRemoved
                    = sockets.entrySet().stream().filter((entry)
                            -> (entry.getValue() == peer)).findFirst();
            if (toBeRemoved.isPresent()) {
                Entry<ClientHandshake, WebSocket> socket = toBeRemoved.get();
                sockets.remove(socket.getKey());
            }
        }
    }

    @Override
    public void sendMessageToSocket(WebSocket peer, String message) {
        if (Objects.nonNull(peer)) {
            if (peer.isOpen()) {
                peer.send(message);
            } else {
                removeSocket(peer);
            }
        }
    }

    @Override
    public void sendMessageToAllSockets(String message) {
        sockets.entrySet().stream().forEach((entry) -> {
            sendMessageToSocket(entry.getValue(), message);
        });
    }

    public void sendMessageToAllBrowserSockets(String message) {
        sockets.entrySet().stream().forEach((entry) -> {
            if(isBrowserSocket(entry)){
                sendMessageToSocket(entry.getValue(), message);
            }
        });
    }

    @Override
    public boolean isSocketExists(WebSocket peer) {
        return sockets.entrySet().stream().anyMatch((entry) -> (entry.getValue().equals(peer)));
    }

    @Override
    public int size() {
        return sockets.size();
    }

    private boolean isBrowserSocket(Entry<ClientHandshake, WebSocket> entry) {
        int fieldCount = 0;
        boolean containsOrigin = false;
        boolean hasUserAgent = false;
        if (Objects.nonNull(entry)) {
            ClientHandshake ch = entry.getKey();
            if (Objects.nonNull(ch)) {
                Iterator<String> iter = ch.iterateHttpFields();
                while (iter.hasNext()) {
                    String field = iter.next();
                    if (field.equalsIgnoreCase("Origin")) {
                        containsOrigin = true;
                    } else if (field.equalsIgnoreCase("User-Agent")) {
                        hasUserAgent = true;
                    }
                    fieldCount++;
                }
            }
        }
        return fieldCount > 5 && containsOrigin && hasUserAgent;
    }
}
