/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints.listeners;

import com.mac.eiscp.services.protocols.Protocol;

/**
 *
 * @author Mac
 */
public interface UpdatedStateListener {
    
    void stateUpdated(Protocol protocol);
    
    void queryForState(Protocol protocol);
}
