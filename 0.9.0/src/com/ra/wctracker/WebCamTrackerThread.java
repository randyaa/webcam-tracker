package com.ra.wctracker;

import java.io.File;
import java.net.URL;

public class WebCamTrackerThread extends Thread {
	private URL webcamURL;
	private File directory;
	private long duration;
	private long delay;

	public WebCamTrackerThread(URL webcamURL, File directory, long duration,
			long delay) {
		this.webcamURL = webcamURL;
		this.directory = directory;
		this.duration = duration;
		this.delay = delay;
	}

	@Override
	public synchronized void start() {
		super.start();
		System.out.println("thread started");
	}

	@Override
	public void run() {
		System.out.println("thread running");
		
		super.run();
	}
}
