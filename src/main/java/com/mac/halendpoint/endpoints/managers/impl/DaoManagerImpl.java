/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints.managers.impl;

import com.mac.eiscp.services.protocols.Protocol;
import com.mac.haldao.daos.AbstractDao;
import com.mac.halendpoint.endpoints.managers.DaoManager;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Mac
 */
public class DaoManagerImpl implements DaoManager {

    List<AbstractDao> repos;

    @Override
    public void setRepositories(AbstractDao... repos) {
        if (Objects.nonNull(repos) && repos.length > 0) {
            this.repos = Arrays.asList(repos);
        }
    }

    @Override
    public void save(Protocol protocol) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Protocol find(Protocol protocol) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Protocol> findAll(Protocol protocol) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
