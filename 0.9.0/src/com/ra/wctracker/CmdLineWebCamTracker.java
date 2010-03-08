package com.ra.wctracker;

import java.io.File;
import java.net.URL;

public class CmdLineWebCamTracker {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 1 || args.length > 4) {
			System.err.println("Usage: java CmdLineWebCamTracker " + "<Webcam URL> "
					+ "<Output Directory> " + "<?Max Duration in Millis?> "
					+ "<?Delay between queries?>");
		}

		URL webcamURL = new URL(args[0]);
		File directory = new File(args[1]);
		if (!directory.isDirectory()) {
			throw new Exception("Directory " + directory
					+ " must be a Directory!");
		}
		long duration = WebCamTracker.DEFAULT_DURATION;
		if (args.length > 2) {
			duration = Long.parseLong(args[2]);
		}
		long delay = WebCamTracker.DEFAULT_DELAY; // millis
		if (args.length > 3) {
			delay = Long.parseLong(args[3]);
		}
		WebCamTracker.retrieveImages(webcamURL, directory, duration, delay);
	}

}
