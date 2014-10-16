package com.digi.xbee.api.connection.android;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digi.xbee.api.utils.HexUtils;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

/**
 * This class acts as a wrapper to write data to the USB Interface in Android and
 * behaves like a {@code OutputStream} class.
 */
public class AndroidUSBOutputStream extends OutputStream {

	// Constants.
	private static final int WRITE_TIMEOUT = 2000;
	
	// Variables.
	private UsbDeviceConnection usbConnection;

	private UsbEndpoint sendEndPoint;
	
	private Logger logger;

	/**
	 * Class constructor. Instances a new {@code AndroidUSBOutputStream} object with the given
	 * parameters.
	 * 
	 * @param writeEndpoint The USB end point to use to write data to.
	 * @param connection The USB connection to use to write data to.
	 */
	public AndroidUSBOutputStream(UsbEndpoint writeEndpoint, UsbDeviceConnection connection) {
		this.usbConnection = connection;
		this.sendEndPoint = writeEndpoint;
		this.logger = LoggerFactory.getLogger(AndroidUSBOutputStream.class);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int oneByte) throws IOException {
		write(new byte[] {(byte)oneByte});
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		final byte[] finalData = new byte[count + offset];
		System.arraycopy(buffer, offset, finalData, 0, count);
		Thread sendThread = new Thread() {
			public void run() {
				usbConnection.bulkTransfer(sendEndPoint, finalData, finalData.length, WRITE_TIMEOUT);
				logger.debug("Message sent: " + HexUtils.byteArrayToHexString(finalData));
			}
		};
		sendThread.start();
	}
}