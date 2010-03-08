package com.ra.wctracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

public class WebCamTracker {

	public static final long DEFAULT_DELAY = 1000;
	public static final long DEFAULT_DURATION = -1;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WebCamTrackerWindow window = new WebCamTrackerWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void retrieveImages(URL webcamURL, File directory,
			long duration, long delay) throws Exception {

		int i = findFirstFileNumberThatDoesntExist(directory);
		
		
		File currentFile = getNextFileName(directory, i);
		String lastChecksum = null;
		String currentChecksum = null;
		long startTime = System.currentTimeMillis();
		while (duration == DEFAULT_DURATION || System.currentTimeMillis() - startTime < duration
				) {
			System.out.println("Retrieving image " + i);
			try {
				writeDataFromUrl(webcamURL, currentFile);
				System.out.println("Retrieved Image: " + i + " at "
						+ currentFile.getAbsolutePath());
				i++;
				currentChecksum = getMD5Checksum(currentFile);
				if (currentChecksum.equals(lastChecksum)) {
					System.out
							.println("File retrieved is duplicate of previous file.  Deleting.");
					// file is the same, delete it and don't switch out the
					// filename so we use it again.
					if (currentFile.delete()) {
						System.err
								.println("Problem deleting dupe file.  "
										+ "Moving on to next filename even though previous 2 files are the same.");
						currentFile = getNextFileName(directory, i);
						if (duration == DEFAULT_DURATION) {
							break;
						}
					}
				} else {
					lastChecksum = currentChecksum;
					currentFile = getNextFileName(directory, i);
					if (duration == DEFAULT_DURATION) {
						break;
					}
				}
			} catch (Exception e) {
				// if we couldn't write it, just keep going.
				continue;
			} finally {
				System.out.println("sleeping for " + delay + " milliseconds.");
				Thread.sleep(delay);
			}

		}
		System.out.println("Retrieved " + (i - 1) + " Webcam Images.");

		RemoveDupes(directory);
	}

	private static int findFirstFileNumberThatDoesntExist(File directory) {
		int i = 0;
		File currentFile = getNextFileName(directory, i);
		while (currentFile.exists()) {
			i++;
			currentFile = getNextFileName(directory, i);
		}
		return i;
	}

	private static File getNextFileName(File directory, int i) {
		return new File(directory, "image-" + seed(i) + ".jpg");
	}

	public static int RemoveDupes(File directory) throws Exception {
		File[] files = directory.listFiles();
		String lastchecksum = null;
		String currentChecksum;
		System.out.println("Scanning for dupes.");
		int deleted = 0;
		for (File f : files) {
			if (!f.isDirectory()) {
				currentChecksum = getMD5Checksum(f);
				if (currentChecksum.equals(lastchecksum)) {
					f.delete();
					System.out.println("deleted: " + f.getName());
					deleted++;
				}
				lastchecksum = currentChecksum;
			}
		}
		System.out.println("Found & deleted " + deleted + " dupes.");
		return deleted;
	}

	public static byte[] createChecksum(File file) throws Exception {
		InputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	public static String getMD5Checksum(File file) throws Exception {
		byte[] b = createChecksum(file);
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	private static String seed(int i) {
		return ((100000 + i) + "").substring(1);
	}

	private static void writeDataFromUrl(URL url, File file)
			throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(file);
		IOUtils.copy(url.openStream(), out);
		out.flush();
		out.close();
	}

	public static Map<String, String> getSuggestedCams() {
		Properties props = new Properties();
		Map<String,String> namesToURLS = new HashMap<String,String>();
		try {
			props.load(WebCamTracker.class.getClassLoader().getResourceAsStream("suggestedCams.properties"));
			for(Entry e:props.entrySet()) {
				namesToURLS.put(e.getKey().toString(), e.getValue().toString());
			}
		} catch (IOException e) {
			System.err.println("Problem loading suggested cam properties file.  Loading defaults instead");
			namesToURLS.put("Hilo Hawaii","http://64.29.78.157/cgi-bin/fullsize.jpg");
			namesToURLS.put("Sprecks Hawaii", "http://174.37.229.10/mauiwindcam.com/sprecks-large.jpg");
			namesToURLS.put("Kanaha Hawaii","http://174.37.229.10/mauiwindcam.com/kanaha-large.jpg");
			namesToURLS.put("Lake McDonald", "http://ns.www.nps.gov.edgesuite.net/glac/photosmultimedia/webcams/mcdcam2.jpg");
			namesToURLS.put("Lassen National Park", "http://www.nps.gov/ns/lavo/photosmultimedia/webcams/kyvc_webcam1.jpg");
		}
		return namesToURLS;
	}

}
