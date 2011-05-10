
package com.codeminders.ardroneui;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.hidapi.*;

import com.codeminders.ardroneui.controllers.*;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: bird Date: 5/6/11 Time: 4:50 PM To change
 * this template use File | Settings | File Templates.
 */
public class PS3Flight
{
    private static final long READ_UPDATE_DELAY_MS = 50L;

    static
    {
        System.loadLibrary("hidapi-jni");
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        readDevice();
    }

    private static void readDevice()
    {
        ARDrone drone;
        PS3Controller dev;
        try
        {
            drone = new ARDrone();
            drone.connect();
            try
            {
                dev = findController();
                if(dev == null)
                {
                    System.err.println("No suitable controller found!");
                    listDevices();
                    return;
                }
                try
                {
                    while(true)
                    {
                        PS3ControllerState pad = dev.read();
                        if(pad == null)
                            continue;

                        // TODO: perhaps save old PS3ControllerState object
                        // And do diff, detecting button press or release.
                        // otherwise multiple drone commands for send/relase
                        // will be sent while button is pressed.
                        if(pad.isStart())
                            drone.takeOff();
                        else if(pad.isSelect())
                            drone.land();

                        try
                        {
                            Thread.sleep(READ_UPDATE_DELAY_MS);
                        } catch(InterruptedException e)
                        {
                            // Ignore
                        }
                    }
                } finally
                {
                    dev.close();
                }
            } finally
            {
                drone.disconnect();
            }
        } catch(HIDDeviceNotFoundException hex)
        {
            hex.printStackTrace();
            listDevices();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }

    private static PS3Controller findController() throws IOException
    {
        HIDDeviceInfo[] devs = HIDManager.listDevices();
        for(int i = 0; i < devs.length; i++)
        {
            if(AfterGlowController.isA(devs[i]))
                return new AfterGlowController(devs[i]);
            if(SonyPS3Controller.isA(devs[i]))
                return new SonyPS3Controller(devs[i]);
        }
        return null;
    }

    private static void listDevices()
    {
        String property = System.getProperty("java.library.path");
        System.err.println(property);

        try
        {
            HIDDeviceInfo[] devs = HIDManager.listDevices();
            System.err.println("Devices:\n\n");
            for(int i = 0; i < devs.length; i++)
            {
                System.err.println("" + i + ".\t" + devs[i]);
                System.err.println("---------------------------------------------\n");
            }
        } catch(IOException e)
        {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
