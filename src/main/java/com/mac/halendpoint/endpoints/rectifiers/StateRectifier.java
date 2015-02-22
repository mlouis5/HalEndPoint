/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints.rectifiers;

import com.mac.eiscp.services.protocols.Protocol;
import com.mac.halendpoint.endpoints.listeners.UpdatedStateListener;

/**
 *
 * @author Mac
 */
public interface StateRectifier {
    
    void setUpdatedStateListener(UpdatedStateListener stateListener);
    
    void updateState(Protocol protocol);
}
