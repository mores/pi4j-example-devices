package com.pi4j.devices.st7789;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBus;
import com.pi4j.io.spi.SpiConfig;

public class ST7789Test {

	private static Logger log = LoggerFactory.getLogger( ST7789Test.class );

	private static Context pi4j;

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

		SpiConfig spiConfig = Spi.newConfigBuilder(pi4j)
			.id("SPI" + 1)
			.name("ST7789")
			.baud(24000000)
			.build();

		Spi spi = pi4j.create(spiConfig);

	}
}
