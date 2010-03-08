package com.ra.wctracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;

public class WebCamTrackerWindow {

	protected Shell shell;
	private Text outputDirectoryTextBox;
	private Text durationTextBox;
	private Text delayTextBox;
	private Text output;
	private CCombo webCamURLTextBox;
	private Scale durationScale;
	private Scale delayScale;
	private Label lblMinutes;
	private Label lblSeconds;
	private Table table;
	private Display display;
	private Canvas canvas;
	private File settingsFile;

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

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		shell = new Shell();
		shell.setSize(642, 573);
		shell.setText("WebCam Tracker");
		createContents();
		settingsFile = null;
		try {
			;
			settingsFile = new File(File.createTempFile(
					"a-temp-file-for-nothing", "y").getParentFile(),
					"webcam-tracker.settings");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (settingsFile.exists()) {
			loadSettings();
		} else {
			try {
				settingsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		OutputStream out = new OutputStream() {
			public synchronized void write(int b) throws IOException {
				output.append(Character.toString((char) b));
			}
		};
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(out));
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void loadSettings() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(settingsFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (props.get("outputDirectory") != null) {
			outputDirectoryTextBox.setText(props.get("outputDirectory")
					.toString());
		}
		if (props.get("webcam.history") != null) {
			String[] history = props.get("webcam.history").toString().split(
					"\\],\\[");
			for (String hisItem : history) {
				webCamURLTextBox.add(hisItem);
			}

		}

	}

	private void saveSettings() {
		Properties props = new Properties();
		props.put("outputDirectory", outputDirectoryTextBox.getText());

		int i = 0;
		String history = "";
		boolean first = true;
		for (String item : webCamURLTextBox.getItems()) {
			if (item == null || item.length() == 0 || history.contains(item)) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				history += "],[";
			}
			history += item;

			i++;
			if (i >= 10) {
				break;
			}
		}
		props.put("webcam.history", history);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					settingsFile);
			props.store(fileOutputStream, "comment");
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {

		ScrolledComposite scrolledOutputPane = new ScrolledComposite(shell,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledOutputPane.setBounds(10, 300, 606, 204);
		scrolledOutputPane.setExpandHorizontal(true);
		scrolledOutputPane.setExpandVertical(true);

		output = new Text(scrolledOutputPane, SWT.BORDER | SWT.V_SCROLL
				| SWT.MULTI);
		scrolledOutputPane.setContent(output);
		scrolledOutputPane.setMinSize(output.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		Button directoryButton = new Button(shell, SWT.NONE);
		directoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shell);

				// Set the initial filter path according
				// to anything they've selected or typed in
				dlg.setFilterPath(outputDirectoryTextBox.getText());

				// Change the title bar text
				dlg.setText("SWT's DirectoryDialog");

				// Customizable message displayed in the dialog
				dlg.setMessage("Select a directory");

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dlg.open();
				if (dir != null) {
					// Set the text box to the new selection
					outputDirectoryTextBox.setText(dir);
					saveSettings();
				}
			}
		});
		directoryButton.setBounds(271, 8, 101, 25);
		directoryButton.setText("Output Directory");

		outputDirectoryTextBox = new Text(shell, SWT.BORDER);
		outputDirectoryTextBox.setEditable(false);
		outputDirectoryTextBox.setBounds(378, 10, 238, 22);

		Label lblDuration = new Label(shell, SWT.NONE);
		lblDuration.setBounds(271, 50, 48, 22);
		lblDuration.setText("Duration");

		durationTextBox = new Text(shell, SWT.BORDER);
		durationTextBox.setBounds(325, 50, 72, 21);
		durationTextBox.setText(WebCamTracker.DEFAULT_DURATION + "");

		lblMinutes = new Label(shell, SWT.NONE);
		lblMinutes.setBounds(403, 50, 48, 15);
		lblMinutes.setText("minutes");

		Label lblDelay = new Label(shell, SWT.NONE);
		lblDelay.setBounds(271, 95, 48, 21);
		lblDelay.setText("Delay");

		delayTextBox = new Text(shell, SWT.BORDER);
		delayTextBox.setBounds(325, 95, 72, 21);
		delayTextBox.setText(WebCamTracker.DEFAULT_DELAY / 1000 + "");

		lblSeconds = new Label(shell, SWT.NONE);
		lblSeconds.setBounds(403, 95, 48, 15);
		lblSeconds.setText("seconds");

		Button goButton = new Button(shell, SWT.NONE);
		goButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					final URL webcamURL = new URL(webCamURLTextBox.getText());
					final File directory = new File(outputDirectoryTextBox
							.getText());
					long duration = Long.parseLong(durationTextBox.getText());
					if (duration != WebCamTracker.DEFAULT_DURATION) {
						duration = duration * 60000;
					}
					final long delay = Long.parseLong(delayTextBox.getText()) * 1000;
					System.out.println(duration);

					// WebCamTrackerThread t = new
					// WebCamTrackerThread(webcamURL,
					// directory, duration, delay);
					// keep history
					webCamURLTextBox.add(webcamURL.toString());
					saveSettings();
					try {
						WebCamTracker.retrieveImages(webcamURL, directory,
								duration, delay);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					// t.start();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		goButton.setBounds(271, 269, 345, 25);
		goButton.setText("Retrieve Webcam Images");

		webCamURLTextBox = new CCombo(shell, SWT.BORDER);
		// TODO make this keep a history
		webCamURLTextBox.setBounds(10, 10, 255, 21);

		durationScale = new Scale(shell, SWT.NONE);
		durationScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				durationTextBox.setText(durationScale.getSelection() + "");
			}
		});
		durationScale.setMaximum(1440);// minutes = 24hrs
		durationScale.setBounds(446, 37, 170, 42);

		delayScale = new Scale(shell, SWT.NONE);
		delayScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				delayTextBox.setText(delayScale.getSelection() + "");
			}
		});
		delayScale.setMaximum(600); // seconds = 10minutes
		delayScale.setBounds(446, 83, 170, 42);

		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TableItem tItem = table.getSelection()[0];
				// Browser.
				Browser browser = new Browser(shell, SWT.NONE);
				browser.setUrl(tItem.getText(1));

			}
		});
		table.setBounds(10, 37, 255, 257);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem tItem = table.getSelection()[0];
				webCamURLTextBox.setText(tItem.getText(1));
				try {
					URL url = new URL(tItem.getText(1));
					Image webcamPreviewImage = new Image(display, url
							.openStream());

					// scale appropriately not just to the new height/width.
					Image rescaledImage = resizeImage(webcamPreviewImage);
					GC gc = new GC(canvas);
					gc.setForeground(gc.getBackground());
					gc.drawImage(rescaledImage, (canvas.getBounds().width / 2)
							- (rescaledImage.getBounds().width / 2), (canvas
							.getBounds().height / 2)
							- (rescaledImage.getBounds().height / 2));
					gc.dispose();
					webcamPreviewImage.dispose();
					rescaledImage.dispose();

				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}

			private Image resizeImage(Image webcamPreviewImage) {
				int maxHeight = canvas.getBounds().height;
				int maxWidth = canvas.getBounds().width;

				int origHeight = webcamPreviewImage.getBounds().height;
				int origWidth = webcamPreviewImage.getBounds().width;

				double ratio = (double) maxHeight / (double) origHeight;

				if (origWidth * ratio > maxWidth) {
					ratio = (double) maxWidth / (double) origWidth;
				}

				int newHeight = (int) (ratio * origHeight);
				int newWidth = (int) (ratio * origWidth);

				return new Image(display, webcamPreviewImage.getImageData()
						.scaledTo(newWidth, newHeight));
			}
		});

		TableColumn tblclmnWebcamName = new TableColumn(table, SWT.NONE);
		tblclmnWebcamName.setWidth(100);
		tblclmnWebcamName.setText("WebCam Name");

		TableColumn tblclmnUrl = new TableColumn(table, SWT.NONE);
		tblclmnUrl.setWidth(150);
		tblclmnUrl.setText("URL");

		setupSuggestedCams();

		ProgressBar progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setBounds(10, 510, 606, 17);

		canvas = new Canvas(shell, SWT.NONE);
		canvas.setBounds(271, 131, 345, 132);
		canvas.setLayout(new GridLayout(1, false));

	}

	private void setupSuggestedCams() {
		Map<String, String> namesToURLS = WebCamTracker.getSuggestedCams();

		TableItem ti;
		for (String name : namesToURLS.keySet()) {
			ti = new TableItem(table, SWT.NONE);
			ti.setText(new String[] { name, namesToURLS.get(name) });
		}
	}

	public Text getOutput() {
		return output;
	}
}
