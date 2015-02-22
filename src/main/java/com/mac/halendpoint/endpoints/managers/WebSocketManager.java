/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints.managers;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 *
 * @author MacDerson
 */
public interface WebSocketManager {
    
    void addWebSocket(WebSocket peer, ClientHandshake ch);

    void clearAllWebSockets();

    boolean hasLiveSockets();

    boolean isEmpty();

    void removeClosedSockets();

    void removeSocket(WebSocket peer);

    void sendMessageToSocket(WebSocket socket, String message);
    
    void sendMessageToAllSockets(String message);
    
    void sendMessageToAllBrowserSockets(String message);

    boolean isSocketExists(WebSocket peer);
    
    int size();
    
}
