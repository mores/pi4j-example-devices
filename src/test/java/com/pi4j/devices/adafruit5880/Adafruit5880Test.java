package com.pi4j.devices.adafruit5880;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;

public class Adafruit5880Test {

        private static Logger log = LoggerFactory.getLogger( Adafruit5880Test.class );

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
        }
}
