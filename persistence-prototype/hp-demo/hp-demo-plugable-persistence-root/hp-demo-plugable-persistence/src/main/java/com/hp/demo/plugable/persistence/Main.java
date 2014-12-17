/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence;

import com.hp.demo.plugable.persistence.common.model.Location;
import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.model.CoreModule;
import com.hp.demo.plugable.persistence.model.NetworkDeviceService;
import com.hp.demo.plugable.persistence.model.UserService;
import com.hp.demo.plugable.persistence.model.impl.CoreModuleProvider;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.auth.Password;
import com.hp.util.common.type.auth.Username;
import com.hp.util.common.type.net.Email;
import com.hp.util.common.type.net.IpAddress;

/**
 * Plugable persistence demo.
 * 
 * @author Fabiel Zuniga
 */
public class Main {

    /**
     * Main method.
     * 
     * @param args arguments
     */
    public static void main(String[] args) {
        System.out.println("Plugable persistence demo");

        CoreModule coreModule = CoreModuleProvider.getCoreModule();

        NetworkDeviceService networkDeviceService = coreModule.getModel().getNetworkDeviceService();

        NetworkDevice device1 = networkDeviceService.discover(IpAddress.valueOf("192.168.1.1"));
        NetworkDevice device2 = networkDeviceService.discover(IpAddress.valueOf("192.168.1.2"));

        networkDeviceService.setFriendlyName(device1.getId(), "Device 1");
        networkDeviceService.setFriendlyName(device2.getId(), "Device 2");

        networkDeviceService.setLocation(device1.getId(), Location.BUILDING_1_FIRST_FLOOR);
        networkDeviceService.setLocation(device2.getId(), Location.BUILDING_1_SECOND_FLOOR);

        System.out.println();

        System.out.println("Reachable devices:");
        for (NetworkDevice device : networkDeviceService.getReachable()) {
            System.out.println(device);
        }

        Location location = Location.BUILDING_1_FIRST_FLOOR;
        System.out.println("Devices at " + location + ":");
        for (NetworkDevice device : networkDeviceService.getByLocation(location)) {
            System.out.println(device);
        }

        System.out.println();

        UserService userService = coreModule.getModel().getUserService();

        Username username1 = Username.valueOf("user-1");
        Password password1 = Password.valueOf("user-1-password");

        Username username2 = Username.valueOf("user-2");
        Password password2 = null;

        userService.signUp(username1, password1, Email.valueOf("user-1@persistence-demo.com"));
        userService.signUp(username2, password2, Email.valueOf("user-2@persistence-demo.com"));

        System.out.println("Valid authentication: " + userService.signIn(username1, password1));
        System.out.println("Valid authentication: " + userService.signIn(username2, password2));
        System.out.println("Invalid authentication: "
                + userService.signIn(username1, Password.valueOf("invalid-password")));

        System.out.println("Enabled users:");
        for (User user : userService.getEnabled()) {
            System.out.println(user);
        }

        Id<User, Username> userToDisable = Id.valueOf(username1);
        userService.disable(userToDisable);

        System.out.println("Enabled users:");
        for (User user : userService.getEnabled()) {
            System.out.println(user);
        }
        System.out.println("Disabled users:");
        for (User user : userService.getDisabled()) {
            System.out.println(user);
        }

        System.exit(0);
    }
}
