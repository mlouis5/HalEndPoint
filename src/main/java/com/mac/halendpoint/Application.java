/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint;

import com.mac.halendpoint.endpoints.HalSocket;

/**
 *
 * @author Mac
 */
public class Application {
    
    public static void main(String[] args){
        HalSocket server = new HalSocket();
        server.start();
    }
}
