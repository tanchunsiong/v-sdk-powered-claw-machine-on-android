package us.zoom.sdksample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 This class allows you to control GPIO pins form android, by abstracting the command line excecution into a java class.
 IMPORTANT NOTE: Requires root to run, since it is required to write to GPIO ports in /sys/class/gpio/
 */
//https://gist.github.com/lpbas/bca095e23627c7d53addba0a01d6cc09

public class GPIO {

    static String TAG = GPIO.class.getSimpleName();

    public static String DIRECTION_IN = "in";
    public static String DIRECTION_OUT = "out";

    public static int VALUE_ON = 1;
    public static int VALUE_OFF = 0;

    private String port;
    private int pin;

    //Constructors
    public GPIO(int pin) {
        this.port = "gpio" + pin;
        this.pin = pin;
    }

    public GPIO(int pin, String direction) {
        this.port = "gpio" + pin;
        this.pin = pin;

        initPin(direction);
    }

    /// Export GPIO pin
    private boolean activatePin() {
        String command = String.format("echo %d > /sys/class/gpio/export", this.pin);
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            return true;
        } catch (IOException e) {
            //log.e(TAG, "Failed to export " + port, e);
            return false;
        }
    }

    /// Unexport GPIO pin
    public boolean deactivatePin() {
        String command = String.format("echo %d > /sys/class/gpio/unexport", this.pin);
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            return true;
        } catch (IOException e) {
            //log.e(TAG, "Failed to unexport " + port, e);
            return false;
        }
    }

    /// Initialise the GPIO pin
    public int initPin(String direction) {
        int retour = 0;
        boolean ret = true;

        // see if gpio is already set
        retour = getValue();
        if (retour == -1) {
            // unexport the gpio
            ret = deactivatePin();
            if (!ret) {
                retour = -1;
            }

            //export the gpio
            ret = activatePin();
            if (!ret) {
                retour = -2;
            }
        }

        // check if gpio direction is defined
        String ret2 = getDirection();
        if (!ret2.contains(direction)) {
            // set the direction (in or out)
            ret = setDirection(direction);
            if (!ret) {
                retour = -3;
            }
        }

        return retour;
    }

    /// Get direction of GPIO
    public String getDirection() {
        String command = String.format("cat /sys/class/gpio/%s/direction", this.port);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            String retour = text.toString();
            return retour;
        } catch (IOException e) {
            //log.e(TAG, "Failed to get direction of " + port, e);
            return "";
        }
    }

    /// Get state of GPIO for input and output and test if it is configurate
    public int getValue() {
        String command = String.format("cat /sys/class/gpio/%s/value", this.port);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            try {
                String retour = text.toString();
                if (retour.equals("")) {
                    return -1;
                } else {
                    return Integer.parseInt(retour.substring(0, 1));
                }
            } catch (NumberFormatException nfe) {
                return -1;
            }
        } catch (IOException e) {
            //log.e(TAG, "Failed to get value of " + port, e);
            return -1;
        }
    }

    /// Set the value of the GPIO output
    public boolean setValue(int value) {
        String command = String.format("echo %d > /sys/class/gpio/%s/value", value, this.port);
        try {
            String[] test = new String[]{"su", "-c", command};
            Runtime.getRuntime().exec(test);
            return true;
        } catch (IOException e) {
            //log.e(TAG, "Failed to set value of " + port, e);
            return false;
        }
    }

    /// Set the direction of the GPIO pin
    public boolean setDirection(String direction) {
        String command = String.format("echo %s > /sys/class/gpio/%s/direction", direction, this.port);
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            return true;
        } catch (IOException e) {
            //log.e(TAG, "Failed to set direction of " + port, e);
            return false;
        }
    }

}