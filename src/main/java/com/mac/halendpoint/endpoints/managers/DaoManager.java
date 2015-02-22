/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints.managers;

import com.mac.eiscp.services.protocols.Protocol;
import com.mac.haldao.daos.AbstractDao;
import java.util.List;

/**
 *
 * @author Mac
 */
public interface DaoManager {
    
    void setRepositories(AbstractDao... repos);
    
    void save(Protocol protocol);
    
    Protocol find(Protocol protocol);
    
    List<Protocol> findAll(Protocol protocol);
}
