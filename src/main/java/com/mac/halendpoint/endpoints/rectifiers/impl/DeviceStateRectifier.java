/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.halendpoint.endpoints.rectifiers.impl;

import com.mac.eiscp.devices.interfaces.CommandStateUpdatable;
import com.mac.eiscp.devices.interfaces.InitializableState;
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Device;
import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.impl.SingleDevice;
import com.mac.eiscp.services.protocols.Protocol;
import com.mac.eiscp.services.utilities.JsonConverter;
import com.mac.halendpoint.endpoints.listeners.UpdatedStateListener;
import com.mac.halendpoint.endpoints.rectifiers.StateRectifier;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mac
 */
public class DeviceStateRectifier implements StateRectifier {

    private static final String UPDATE_RESPONSE = "device";
    private UpdatedStateListener stateListener;
    private Device device;

    public void manageDevice(Device singleDevice) {
        if (Objects.nonNull(singleDevice)) {
            device = singleDevice;
            initDeviceState();
        }
    }

    @Override
    public void setUpdatedStateListener(UpdatedStateListener stateListener) {
        this.stateListener = stateListener;
    }

    @Override
    public void updateState(Protocol protocol) {
        Protocol newProto = null;
        if (Objects.nonNull(protocol)) {
            if (protocol.getRespondTo().equalsIgnoreCase("update")) {
                try {
                    String response = protocol.getResponse();
                    if (response.contains("message")) {
                        Message message = (Message) JsonConverter.fromJsonString(response, Message.class);

                        Command cmd = message.getCommand();
                        if (device instanceof SingleDevice) {
                            SingleDevice sd = (SingleDevice) device;

                            Field[] fields = sd.getClass().getDeclaredFields();
                            if (Objects.nonNull(fields)) {
                                for (Field field : fields) {
                                    field.setAccessible(true);
                                    Object value = field.get(sd);

                                    if (value instanceof CommandStateUpdatable) {
                                        CommandStateUpdatable csu = (CommandStateUpdatable) value;
                                        csu.updateState(cmd);
                                        newProto = new Protocol();
                                    }
                                    field.setAccessible(false);
                                }
                            }
                        }
                    }
                } catch (IOException | IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(DeviceStateRectifier.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (Objects.nonNull(newProto) && Objects.nonNull(this.stateListener)) {
            try {
                newProto.setIsModified(true);
                newProto.setParameters(protocol.getParameters());
                newProto.setRespondTo(UPDATE_RESPONSE);
                newProto.setRoutedTo(UPDATE_RESPONSE);
                newProto.setResponse(JsonConverter.toJsonString(device));
                this.stateListener.stateUpdated(newProto);
            } catch (IOException ex) {
                Logger.getLogger(DeviceStateRectifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void initDeviceState() {
        if (device instanceof SingleDevice) {
            SingleDevice sd = (SingleDevice) device;

            Field[] fields = sd.getClass().getDeclaredFields();
            if (Objects.nonNull(fields)) {
                for (Field field : fields) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(sd);

                        if (value instanceof InitializableState) {
                            InitializableState is = (InitializableState) value;
                            String cmd = is.getStateQstn();

                            Protocol protocol = new Protocol();
                            protocol.setIsModified(false);
                            protocol.setRoutedTo("command");
                            protocol.setRespondTo("status");
                            protocol.setParameters(device.getDeviceName(), cmd);
                            
                            this.stateListener.queryForState(protocol);
                            Thread.sleep(60);
                        }
                        field.setAccessible(false);
                    } catch (IllegalArgumentException | IllegalAccessException 
                            | InterruptedException ex) {
                        Logger.getLogger(DeviceStateRectifier.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }
        }
    }
}
