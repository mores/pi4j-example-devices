package com.pi4j.devices.st7789;

public class ST7789_Defines {

        private static final short CMD_NOP = 0x00;
        private static final short CMD_SWRESET = 0x01;
        private static final short CMD_RDDID = 0x04;
        private static final short CMD_RDDST = 0x09;

        private static final short CMD_SLPIN = 0x10;
        private static final short CMD_SLPOUT = 0x11;
        private static final short CMD_PTLON = 0x12;
        private static final short CMD_NORON = 0x13;

        private static final short CMD_INVOFF = 0x20;
        private static final short CMD_INVON = 0x21;
        private static final short CMD_DISPOFF = 0x28;
        private static final short CMD_DISPON = 0x29;
        private static final short CMD_CASET = 0x2A;
        private static final short CMD_RASET = 0x2B;
        private static final short CMD_RAMWR = 0x2C;
        private static final short CMD_RAMRD = 0x2E;

        private static final short CMD_PTLAR = 0x30;
        private static final short CMD_COLMOD = 0x3A;
        private static final short CMD_MADCTL = 0x36;

        private static final short CMD_FRMCTR1 = 0xB1;
        private static final short CMD_FRMCTR2 = 0xB2;
        private static final short CMD_FRMCTR3 = 0xB3;
        private static final short CMD_INVCTR = 0xB4;
        private static final short CMD_DISSET5 = 0xB6;

        private static final short CMD_PWCTR1 = 0xC0;
        private static final short CMD_PWCTR2 = 0xC1;
        private static final short CMD_PWCTR3 = 0xC2;
        private static final short CMD_PWCTR4 = 0xC3;
        private static final short CMD_PWCTR5 = 0xC4;
        private static final short CMD_VMCTR1 = 0xC5;

        private static final short CMD_RDID1 = 0xDA;
        private static final short CMD_RDID2 = 0xDB;
        private static final short CMD_RDID3 = 0xDC;
        private static final short CMD_RDID4 = 0xDD;

        private static final short CMD_PWCTR6 = 0xFC;

        private static final short CMD_GMCTRP1 = 0xE0;
        private static final short CMD_GMCTRN1 = 0xE1;
}
