package com.pi4j.devices.adafruit5880;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class Adafruit5880Test {

        private static Logger log = LoggerFactory.getLogger( Adafruit5880Test.class );

        private static Context pi4j;
	private I2C dev;

        public static void setUp() {
                log.info( "setUp" );

                pi4j =  Pi4J.newAutoContext();
        }

        public static void tearDown() {
                log.info( "tearDown" );

                pi4j.shutdown();
        }

        public void testOne() {
                log.info( "testOne" );

		I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j).id("Adafruit5880").bus(1).device(0x36).build();

		I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");

		try
		{
			dev = i2CProvider.create(i2cConfig);

			byte STATUS_BASE = 0x00;
			byte STATUS_HW_ID = 0x01;
			byte STATUS_VERSION = 0x02;

			byte chipId = read8( STATUS_BASE, STATUS_HW_ID );
			log.info( "chipId: " + chipId );

			int version = read( STATUS_BASE, STATUS_VERSION, 4 ).getInt();
			log.info( "version: " + version ); 

			int pid = version >> 16;
			log.info( "pid: " + pid );
		}
		catch( Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			dev.close();
		}
        }

	private java.nio.ByteBuffer read( byte register, byte addr, int length ) throws Exception
        {
                dev.writeRegister( register, addr );
                Thread.sleep(8);

                return dev.readRegisterByteBuffer( register, length );
        }

	private byte read8( byte register, byte addr ) throws Exception
	{
		dev.writeRegister( register, addr );
		Thread.sleep(8);

		byte returnValue = (byte)dev.readRegister( register );

		return returnValue;
	}
}
