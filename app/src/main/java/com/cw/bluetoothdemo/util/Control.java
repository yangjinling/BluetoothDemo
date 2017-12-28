package com.cw.bluetoothdemo.util;

import com.cw.bluetoothdemo.app.Contents;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 作者：杨金玲 on 2017/12/27 11:51
 * 邮箱：18363820101@163.com
 */

public class Control {
    public static void gpio_control(int gpio, int mode) {
        try {
            String file_name = new String("/sys/class/gpio/gpio" + gpio + "/value");
            File file = new File(file_name);
            if (file.exists() == false) {
                file_name = new String("/sys/class/gpiocontrol/gpiocontrol/gpiocontrol" + gpio);
                file = new File(file_name);
            }

            FileWriter localFileWriter = new FileWriter(file);
            if (mode == 1)
                localFileWriter.write("1");
            else if (mode == 0)
                localFileWriter.write("0");
            Contents.isControl = true;
            localFileWriter.close();
        } catch (IOException localIOException) {
            localIOException.printStackTrace();
        }
    }
}
