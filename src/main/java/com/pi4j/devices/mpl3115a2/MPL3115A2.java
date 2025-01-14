/*
 *
 *
 *     *
 *     * -
 *     * #%L
 *     * **********************************************************************
 *     * ORGANIZATION  :  Pi4J
 *     * PROJECT       :  Pi4J :: EXTENSION
 *     * FILENAME      :  MPL3115A2.java
 *     *
 *     * This file is part of the Pi4J project. More information about
 *     * this project can be found here:  https://pi4j.com/
 *     * **********************************************************************
 *     * %%
 *     *   * Copyright (C) 2012 - 2022 Pi4J
 *      * %%
 *     *
 *     * Licensed under the Apache License, Version 2.0 (the "License");
 *     * you may not use this file except in compliance with the License.
 *     * You may obtain a copy of the License at
 *     *
 *     *      http://www.apache.org/licenses/LICENSE-2.0
 *     *
 *     * Unless required by applicable law or agreed to in writing, software
 *     * distributed under the License is distributed on an "AS IS" BASIS,
 *     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     * See the License for the specific language governing permissions and
 *     * limitations under the License.
 *     * #L%
 *     *
 *
 *
 *
 */

package com.pi4j.devices.mpl3115a2;

import com.pi4j.context.Context;

import com.pi4j.io.gpio.digital.*;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.util.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *    Implementation for the MPL3115A2 sensor. Chip is altitude, pressure and
 *    temperature capable.
 *
 *    Code uses the basic sampling procedures using GPIO to indicate drdy, data ready.
 *    The code does not support setting limits  that signal interrupts when surpassed.
 *
 *    Java docs are sparse as the method names describe the intended
 *    function to be performed.
 *
 *    https://www.nxp.com/docs/en/data-sheet/MPL3115A2.pdf
 *
 *
 */
public class MPL3115A2 {

    public static final String NAME = "MPL3115A2";
    /**
     * Constant <code>ID="MPL3115A2"</code>
     */
    public static final String ID = "MPL3115A2";


    // I2C Provider name and unique ID
    /**
     * Constant <code>I2C_PROVIDER_NAME="NAME +  I2C Provider"</code>
     */
    public static final String I2C_PROVIDER_NAME = NAME + " MPL3115A2 I2C Provider";
    /**
     * Constant <code>I2C_PROVIDER_ID="ID + -i2c"</code>
     */
    public static final String I2C_PROVIDER_ID = ID + "-i2c";



    private  Logger logger = null;
    private  String traceLevel = null;


    // local/internal I2C reference for communication with hardware chip
    protected I2C i2c = null;

    protected I2CConfig config = null;

    protected Context pi4j = null;

    protected Console console = null;

    protected int busNum = MPL3115A2_Declares.DEFAULT_BUS;
    protected int address = MPL3115A2_Declares.DEFAULT_ADDRESS;

    protected int int1_gpio = 0;
    DigitalInput int1 = null;
    protected int int2_gpio = 0;
    DigitalInput int2 = null;


    /**
     * @param pi4j Context instance used acccross application
     * @param console
     * @param bus
     * @param address
     * @param int1_gpio
     * @param int2_gpio
     * @param traceLevel
     */
    public MPL3115A2(Context pi4j, Console console, int bus, int address,int int1_gpio, int int2_gpio, String traceLevel) {
        super();
        this.pi4j = pi4j;
        this.address = address;
        this.busNum = bus;
        this.console = console;
        this.traceLevel = traceLevel;
        this.int1_gpio = int1_gpio;
        this.int2_gpio = int2_gpio;
        // "trace", "debug", "info", "warn", "error" or "off"). If not specified, defaults to "info"
        //  must fully qualify logger as others exist and the slf4 code will use the first it
        //  encounters if using the defaultLogLevel
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "TRACE");
         
        System.setProperty("org.slf4j.simpleLogger.log." + MPL3115A2.class.getName(), this.traceLevel);

        this.logger = LoggerFactory.getLogger(MPL3115A2.class);
        this.createI2cDevice(); // will set start this.i2c
        this.init();

    }



    /**
     * @return i2c state
     */
    public I2C getI2c() {
        this.logger.trace(">>> Enter: GetI2c ");
        this.logger.trace("<<< Exit: getI2c  I2C device   " + this.i2c);
        return (this.i2c);
    }


    /**
     * Use the state from the Sensor config object and the state pi4j to create
     * a MPL3115A2 device instance
     */
    private void createI2cDevice() {
        this.logger.trace(">>> Enter:createI2cDevice   bus  " + this.busNum + "  address " + this.address);

        var address = this.address;
        var bus = this.busNum;

        String id = String.format("0X%02x: ", bus);
        String name = String.format("0X%02x: ", address);
        var i2cDeviceConfig = I2C.newConfigBuilder(this.pi4j)
                .bus(bus)
                .device(address)
                .id(id + " " + name)
                .name(name)
                .provider("linuxfs-i2c")
                .build();
        this.config = i2cDeviceConfig;
        this.i2c = this.pi4j.create(i2cDeviceConfig);
        this.logger.trace("<<< Exit:createI2cDevice  ");
    }

private void init(){
    this.logger.trace(">>> Enter: init");

            var ledConfigIntr = DigitalInput.newConfigBuilder(pi4j)
                    .id("Interrupt_1")
                    .name("Interrupt_1_TW_PW")
                    .address(this.int1_gpio)
                    .pull(PullResistance.OFF)
                    .debounce(4000L)
                    .provider("gpiod-digital-input");
    try {
        this.int1 = pi4j.create(ledConfigIntr);
        this.int1.addListener(new MonitorInterrupt1(this));
    } catch (Exception e) {
        e.printStackTrace();
        console.println("create Digital 1 failed");
        System.exit(200);
    }

    var ledConfigIntr2 = DigitalInput.newConfigBuilder(pi4j)
            .id("Interrupt_2")
            .name("Interrupt_2_DRDY")
            .address(this.int2_gpio)
            .pull(PullResistance.OFF)
            .debounce(4000L)
            .provider("gpiod-digital-input");
    try {
        this.int2 = pi4j.create(ledConfigIntr2);
    } catch (Exception e) {
        e.printStackTrace();
        console.println("create Digital 2 failed");
        System.exit(201);
    }
    this.validateWhoAmI();
    this.logger.trace("<<< Exit: init");
}

    /**
     * @return string containing a description of the attached I2C path
     */
    public String i2cDetail() {
        this.logger.trace(">>> Enter: i2cDetail");
        this.logger.trace("<<< Exit: i2cDetail  " + (this.i2c.toString() + " bus : " + this.config.bus() + "  address : " + String.format("0X%02x: ", this.config.device())));
        return (this.i2c.toString() + " bus : " + String.format("0X%02x: ", this.config.bus()) + "  address : " + String.format("0X%02x: ", this.config.device()));
    }



    private boolean validateWhoAmI(){
        this.logger.trace(">>> Enter: validateWhoAmI");
        boolean rval = false;
       // this.i2c.read();
        int who =  this.i2c.readRegisterByte(MPL3115A2_Declares.REG_WHO_AM_I) & 0xff;
        if(who == MPL3115A2_Declares.WHO_AM_I){
            rval = true;
        }else{
            this.logger.error("validateWhoAmI failure");
            System.exit(300);
        }
        int config = 0xff;
        
        this.logger.trace("<<< Exit: validateWhoAmI :  "+ rval);
        return(rval);
    }


    /**
     * Transition from standby to active
     * Note: The reset operation disables the I2C interface so the associated I2C write
     * give the appearance of failing.
     */
    public void reset(){
        this.logger.trace(">>> Enter: reset");
        byte reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL1);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL1,  reg & MPL3115A2_Declares.CTL1_SBYB_STBY_MASK);
        this.busyWaitMS(10);  // ensure chip quiet
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL1,  reg | MPL3115A2_Declares.CTL1_SBYB_SFT_RESET);
        this.busyWaitMS(10000);  // allow time fir the reboot/POR
        this.logger.trace("<<< Exit : reset");
    }




    /**
     *  Configure chip to calculate Altimeter
     *  Configure interrupt pins to match the Pi GPIO pin configurations
     *  Enter standby and then active to initiate calculation
     */
    private boolean forceAltCalc(){
        this.logger.trace(">>> Enter: forceAltCalc : ");
        boolean rval = false;
        this.busyWaitMS(1000);  // ensure chip quiet
        byte reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL1);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL1, reg & MPL3115A2_Declares.CTL1_SBYB_STBY_MASK | MPL3115A2_Declares.CTL1_ALT_ALTIM | MPL3115A2_Declares.CTL1_OVR_SAMPL_MAX);
        this.busyWaitMS(10);  // ensure chip quiet

        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_PT_DATA_CFG);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_PT_DATA_CFG, reg | MPL3115A2_Declares.PT_DATA_CFG_EVNT_ENBL | MPL3115A2_Declares.PT_DATA_CFG_EVNT_PA);

        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL4);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL4, reg | MPL3115A2_Declares.CTL4_INT_EN_DRDY);

        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL1);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL1, MPL3115A2_Declares.CTL1_ALT_ALTIM | MPL3115A2_Declares.CTL1_OVR_SAMPL_MAX | MPL3115A2_Declares.CTL1_SBYB_ACT);

        // wait INT2
        int i = 0;
        for(i=0;i<400;i++){
            if(this.int2.state()  == DigitalState.LOW) {
                break;
            }else{
                this.logger.trace("Not desired State counter "  + i);
                this.busyWaitMS(40); // depending upon 'sample rate' possible delay 512MS
            }
        }
        if(i >= 400){
            this.logger.trace("int2 Time Out ...\r\n");
        }

        byte source = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_INT_SOURCE);
        if((source & MPL3115A2_Declares.REG_INT_SOURCE_DRDY) > 0){
            rval = true;
        }
        this.busyWaitMS(500);  // ensure chip quiet
        this.logger.trace("<<< Exit: forceAltCalc rval : " + rval);
        return (rval);
    }


    /**
     *  Configure chip to calculate Pressure
     *  Configure interrupt pins to match the Pi GPIO pin configurations
     *  Enter standby and then active to initiate calculation
     */
    private boolean forcePressCalc(){
        this.logger.trace(">>> Enter: forcePressCalc : ");
        boolean rval = false;
        this.busyWaitMS(1000);  // ensure chip quiet
        byte reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL1);
        this.busyWaitMS(1000);  // ensure chip quiet
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL1, reg & MPL3115A2_Declares.CTL1_SBYB_STBY_MASK & MPL3115A2_Declares.CTL1_ALT_PRESS_MASK | MPL3115A2_Declares.CTL1_OVR_SAMPL_MAX);
        this.busyWaitMS(10);  // ensure chip quiet

        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_PT_DATA_CFG);
        this.busyWaitMS(1000);  // ensure chip quiet
        this.i2c.writeRegister(MPL3115A2_Declares.REG_PT_DATA_CFG, reg | MPL3115A2_Declares.PT_DATA_CFG_EVNT_ENBL | MPL3115A2_Declares.PT_DATA_CFG_EVNT_PA);

        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL4);
        this.busyWaitMS(1000);  // ensure chip quiet
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL4, reg | MPL3115A2_Declares.CTL4_INT_EN_DRDY);

        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL1);
        this.busyWaitMS(1000);  // ensure chip quiet
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL1, reg & MPL3115A2_Declares.CTL1_ALT_PRESS_MASK | MPL3115A2_Declares.CTL1_OVR_SAMPL_MAX | MPL3115A2_Declares.CTL1_SBYB_ACT);

        // wait INT2
        int i = 0;
        for(i=0;i<400;i++){
            if(this.int2.state()  == DigitalState.LOW) {
                break;
            }else{
                this.logger.trace("Not desired State counter "  + i);
                this.busyWaitMS(40); // depending upon 'sample rate' possible delay 512MS
            }
        }
        if(i >= 400){
            this.logger.trace("int2 Time Out ...\r\n");
        }

        byte source = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_INT_SOURCE);
        if((source & MPL3115A2_Declares.REG_INT_SOURCE_DRDY) > 0){
            rval = true;
        }
        this.busyWaitMS(500);  // ensure chip quiet
        this.logger.trace("<<< Exit: forcePressCalc rval : " + rval);
        return (rval);
    }


//
    /**
     *  Configure chip to calculate temperature
     *  Configure interrupt pins to match the Pi GPIO pin configurations
     *  Enter standby and then active to initiate calculation
     */
    private boolean forceTempCalc(){
        this.logger.trace(">>> Enter: forceTempCalc : ");
        boolean rval = false;
        this.busyWaitMS(1000);  // ensure chip quiet
        byte reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL1);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL1, reg & MPL3115A2_Declares.CTL1_SBYB_STBY_MASK & MPL3115A2_Declares.CTL1_ALT_PRESS_MASK | MPL3115A2_Declares.CTL1_OVR_SAMPL_MAX);
        this.busyWaitMS(10);  // ensure chip quiet

        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_PT_DATA_CFG);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_PT_DATA_CFG, reg | MPL3115A2_Declares.PT_DATA_CFG_EVNT_ENBL | MPL3115A2_Declares.PT_DATA_CFG_EVNT_T);

          reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL4);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL4, reg | MPL3115A2_Declares.CTL4_INT_EN_DRDY);

        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL1);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL1, reg & MPL3115A2_Declares.CTL1_ALT_PRESS_MASK | MPL3115A2_Declares.CTL1_OVR_SAMPL_MAX | MPL3115A2_Declares.CTL1_SBYB_ACT);

        // wait INT2
        int i = 0;
        for(i=0;i<400;i++){
            if(this.int2.state()  == DigitalState.LOW) {
                break;
            }else{
                this.logger.trace("Not desired State counter "  + i);
                this.busyWaitMS(20); // depending upon 'sample rate' possible delay 512MS
            }
        }
        if(i >= 400){
            this.logger.trace("int2 Time Out ...\r\n");
        }

        byte source = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_INT_SOURCE);
        if((source & MPL3115A2_Declares.REG_INT_SOURCE_DRDY) > 0){
            rval = true;
        }
        this.busyWaitMS(500);  // ensure chip quiet
        this.logger.trace("<<< Exit: forceTempCalc rval : " + rval);
        return (rval);
    }





    private double readAltimeter(){
        this.logger.trace(">>> Enter: readAltimeter");
        double rval = 0;
        if(this.forceAltCalc()) {

            byte[] compVal = new byte[2];

            byte dig_a1 = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_P_MSB);

            byte dig_a2 = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_P_CSB);

            byte dig_a3 = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_P_LSB);

            // fill in rval double
            // 16 bits are integer value  4 bits frac
            Double alt = Double.valueOf((dig_a1  << 8) | (dig_a2 & 0xff) );
             // mask the bits, shift right for proper LSB as first bit.
            // The shift right 4 to make the fraction
            // see
            // https://indepth.dev/posts/1019/the-simple-math-behind-decimal-binary-conversion-algorithms
            Double lsbFrac = ((dig_a3 >> 4) & 0xF)/16.0;

            rval = alt + lsbFrac;

        }else{
            this.logger.error("forceAltCalc failure");
        }

        this.logger.trace("<<< Exit : readAltimeter  :" + rval);
        return(rval);
    }

    private double readPressure(){
        this.logger.trace(">>> Enter: readPressure");
        double rval = 0;
        if(this.forcePressCalc()) {

            byte[] compVal = new byte[2];

            byte dig_p1 = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_P_MSB);

            byte dig_p2 = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_P_CSB);

            byte dig_p3 = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_P_LSB);


            // fill in rval double
            // 18 bits are integer value  2 bits frac
            // see
            // https://indepth.dev/posts/1019/the-simple-math-behind-decimal-binary-conversion-algorithms
            Double lsbFrac = ((dig_p3 >> 4) & 0x3)/4.0;
            Double prs = Double.valueOf(((dig_p1 & 0xff) << 10) | ((dig_p2 & 0xff)<<2)  | ((dig_p3 &0xC0) >>6) );

             rval = prs + lsbFrac;

        }else{
            this.logger.error("forcePressCalc failure");
        }

        this.logger.trace("<<< Exit : readPressure  :" + rval);
        return(rval);
    }

    private double readTemperature(){
        this.logger.trace(">>> Enter: readTemperature");
        double rval = 0;
        if(this.forceTempCalc()) {

            byte[] compVal = new byte[2];

            int dig_t1 =  this.i2c.readRegisterByte(MPL3115A2_Declares.REG_T_MSB);

            int dig_t2 = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_T_LSB);

            // fill in rval double
            // 18 bits are integer value  2 bits frac
            // see
            // https://indepth.dev/posts/1019/the-simple-math-behind-decimal-binary-conversion-algorithms

            Double temp = Double.valueOf(dig_t1);   // sign bit still attached
            Double tempFrac = (((dig_t2 & 0xF0) >> 4)/16.0);
            rval = temp + tempFrac;
        }else{
            this.logger.error("forceTempCalc failure");
        }

        this.logger.trace("<<< Exit : readTemperature  :" + rval);
        return(rval);
    }

    public double readAltimeterM(){
        this.logger.trace(">>> Enter: readAltimeterM");
        double rval =  this.readAltimeter();
        this.logger.trace("<<< Exit : readAltimeterM  :" + rval);
        return(rval);
    }

    public double readAltimeterF(){
        this.logger.trace(">>> Enter: readAltimeterF");
        double meter = this.readAltimeterM();
        double rval = (meter*39.37)/12;
        this.logger.trace("<<< Exit : readAltimeterF  :" + rval);
        return(rval);
    }

    public double readTemperatureC(){
        this.logger.trace(">>> Enter: readTemperatureC");
        double rval = 0.0;
        rval = this.readTemperature();
        this.logger.trace("<<< Exit : readTemperatureC  :" + rval);
        return(rval);
    }

     public double readTemperatureF() {
        this.logger.trace("enter: temperatureF");
        double fTemp = this.readTemperatureC() * 1.8 + 32;
        this.logger.trace("exit: temperatureF  " + fTemp);
        return fTemp;
    }


    public double readPresurePa(){
        this.logger.trace(">>> Enter: readPresurePa");
        double rval = this.readPressure();
        this.logger.trace("<<< Exit : readPresurePa  :" + rval);
        return(rval);
    }

    public double readPresureMb(){
        this.logger.trace(">>> Enter: readPresureMb");
        double rval = this.readPresurePa() /100;
        this.logger.trace("<<< Exit : readPresureMb  :" + rval);
        return(rval);
    }

    private void busyWaitMS(long ms) {
        long waitUntil = System.nanoTime() + (ms * 1000000);
        while (waitUntil > System.nanoTime()) {
            ;
        }
    }

    // SRC_PTH  alt/press limit. When exceeded
    // interrupt thru int2
    public void set_P_PGT(long target){
        this.logger.trace(">>> Enter: set_P_TGT : " + target);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_P_TGT_MSB, (byte) ((target ) >> 8));
        this.i2c.writeRegister(MPL3115A2_Declares.REG_P_TGT_LSB, (byte) (target ));
        byte reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_PT_DATA_CFG);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_PT_DATA_CFG, reg | MPL3115A2_Declares.PT_DATA_CFG_EVNT_ENBL | MPL3115A2_Declares.PT_DATA_CFG_EVNT_PA | MPL3115A2_Declares.PT_DATA_CFG_EVNT_T);
        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL4);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL4, reg | MPL3115A2_Declares.CTL4_INT_EN_PTH |  MPL3115A2_Declares.CTL4_INT_EN_PW);
        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL5);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL5, reg | MPL3115A2_Declares.CTL5_INT_CFG_PW  | MPL3115A2_Declares.CTL5_INT_CFG_PTH);

        this.logger.trace("<<< Exit: set_P_TGT  ");
    }


    // SRC_PW  alt/press window limit. When exceeded
    // interrupt thru int2
    public void set_P_WND(long window){
        this.logger.trace(">>> Enter: set_P_WND : " + window);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_P_WND_MSB, (byte) ((window & 0xff00) >> 8));
        this.i2c.writeRegister(MPL3115A2_Declares.REG_P_WND_LSB, (byte) (window & 0xff));
        byte reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_PT_DATA_CFG);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_PT_DATA_CFG, reg | MPL3115A2_Declares.PT_DATA_CFG_EVNT_ENBL | MPL3115A2_Declares.PT_DATA_CFG_EVNT_PA | MPL3115A2_Declares.PT_DATA_CFG_EVNT_T);
        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL4);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL4, reg | MPL3115A2_Declares.CTL4_INT_EN_PW |  MPL3115A2_Declares.CTL4_INT_EN_PTH);
        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL5);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL5, reg | MPL3115A2_Declares.CTL5_INT_CFG_PW  | MPL3115A2_Declares.CTL5_INT_CFG_PTH);

        this.logger.trace("<<< Exit: set_P_WND  ");
    }


    // SRC_TTH  temp limit. When exceeded
    // interrupt thru int1
    public void set_T_TGT(long target){
        this.logger.trace(">>> Enter: set_T_TGT : " + target);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_T_TGT, (byte) (target & 0x00ff));
        byte reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_PT_DATA_CFG);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_PT_DATA_CFG, reg | MPL3115A2_Declares.PT_DATA_CFG_EVNT_ENBL | MPL3115A2_Declares.PT_DATA_CFG_EVNT_PA | MPL3115A2_Declares.PT_DATA_CFG_EVNT_T);
        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL4);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL4, reg | MPL3115A2_Declares.CTL4_INT_EN_TTH  | MPL3115A2_Declares.CTL4_INT_EN_PW);
        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL5);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL5, reg | MPL3115A2_Declares.CTL5_INT_CFG_TW  | MPL3115A2_Declares.CTL5_INT_CFG_TTH);

        this.logger.trace("<<< Exit: set_T_TGT  ");
    }


    // SRC_TW  temp window limit. When exceeded
    // interrupt thru int1
    public void set_T_WND(long window){
        this.logger.trace(">>> Enter: set_T_WND : " + window);
        boolean rval = false;
        this.i2c.writeRegister(MPL3115A2_Declares.REG_T_WND, (byte) (window & 0xff));
        byte reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_PT_DATA_CFG);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_PT_DATA_CFG, reg | MPL3115A2_Declares.PT_DATA_CFG_EVNT_ENBL | MPL3115A2_Declares.PT_DATA_CFG_EVNT_PA | MPL3115A2_Declares.PT_DATA_CFG_EVNT_T);
        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL4);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL4, reg | MPL3115A2_Declares.CTL4_INT_EN_TW  | MPL3115A2_Declares.CTL4_INT_EN_TTH);
        reg = this.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL5);
        this.i2c.writeRegister(MPL3115A2_Declares.REG_CTRL5, reg | MPL3115A2_Declares.CTL5_INT_CFG_TW  | MPL3115A2_Declares.CTL5_INT_CFG_TTH);


        this.logger.trace("<<< Exit: set_T_WND " );
    }



    // Chip perform altitude calc using 101,326 Pa. If local Pa is different
    // the Altitude calculation will be incorrect.
    public void set_local_Pa_correction(long pa){
        this.logger.trace(">>> Enter: set_local_Pa_correction : " + pa);
        pa = pa >> 1;
        this.i2c.writeRegister(MPL3115A2_Declares.REG_BAR_IN_MSB, (byte) ((pa & 0xff00) >> 8));
        this.i2c.writeRegister(MPL3115A2_Declares.REG_BAR_IN_LSB, (byte) (pa & 0xff));

        this.logger.trace("<<< Exit: set_local_Pa_correction  ");
    }





    //  Interrupt handler


    /**
     * The code at present just prints the present value for what
     * caused the interrupt.  You could change the code to take some action
     * based on what occurred.
      */
    public static class MonitorInterrupt1 implements DigitalStateChangeListener {
        MPL3115A2 mplObj;
        public MonitorInterrupt1( MPL3115A2 mplObj) {
            this.mplObj =  mplObj;

        }

        @Override
        public void onDigitalStateChange(DigitalStateChangeEvent event) {
            // display pin state on console
            // system.out.println(" Matrix -->Utility : GPIO PIN STATE CHANGE: "
            // + event.getPin() + " = " + event.getState());
            if (event.state() == DigitalState.LOW) {
                // determine which condition occured
                this.mplObj.logger.trace("onDigitalStateChange Pin " + event.toString() + " went low, see who caused it" + "\n");
                byte source = this.mplObj.i2c.readRegisterByte(MPL3115A2_Declares.REG_INT_SOURCE);
                if ((source & MPL3115A2_Declares.REG_INT_SOURCE_PW) > 0) {
                    byte reg = this.mplObj.i2c.readRegisterByte(MPL3115A2_Declares.REG_CTRL1);
                    if((reg & MPL3115A2_Declares.CTL1_ALT_PRESS_MASK ) > 0){
                        double rval = this.mplObj.readPresureMb();
                        // User add meaningful code here
                        System.out.println("Pressure limit exceeded  " + rval + "\n");
                    }else{
                        double rval = this.mplObj.readAltimeterM();
                        // User add meaningful code here
                        System.out.println("Altitude limit exceeded  " + rval + "\n");
                    }
                } else if ((source & MPL3115A2_Declares.REG_INT_SOURCE_TW) > 0) {
                    double rval = this.mplObj.readTemperatureF();
                    // User add meaningful code here
                    System.out.println("Temperature limit exceeded " + rval   + "\n");
                } else {
                    this.mplObj.logger.trace("onDigitalStateChange " + event.toString() + " unexpected source" + "\n");
                }
            } else if (event.state() == DigitalState.HIGH) {
                this.mplObj.logger.trace("onDigitalStateChange " + event.toString() + " Pin went high  NOP"  + "\n");
            } else {
                this.mplObj.logger.trace("Pin " + event.toString() + " Strange event state  " + event.state() + "\n");
            }
        }
    }
}
