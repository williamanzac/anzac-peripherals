package anzac.docs.release.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class ReleaseNotesGUI extends JFrame {
	private final class BrowseAction extends AbstractAction {
		private static final long serialVersionUID = -8415463707501758249L;

		public BrowseAction() {
			super("Browse");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.setSelectedFile(new File(targetFile.getText()));
			chooser.showOpenDialog(ReleaseNotesGUI.this);
			targetFile.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private final class SaveReleaseNotes extends AbstractAction {
		private static final long serialVersionUID = 4241673182582626204L;

		private SaveReleaseNotes() {
			super("Save Release Notes");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String text = releaseNotes.getText();
			try {
				FileUtils.writeStringToFile(new File(targetFile.getText()), text);
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private final class ChangeLogAction extends AbstractAction {
		private static final long serialVersionUID = 3503523614764664475L;

		public ChangeLogAction() {
			super("Get Change Log");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat();
			final String startText = startDate.getText();
			final String endText = endDate.getText();
			try {
				final Date startDate = dateFormat.parse(startText);
				final Date endDate = dateFormat.parse(endText);

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						final String start = format.format(startDate);
						final String end = format.format(endDate);
						final String range = MessageFormat.format("\"'{'{0}'}':'{'{1}'}'\"", start, end);
						final ProcessBuilder builder = new ProcessBuilder("svn", "--non-interactive", "log", "-v",
								"-r", range);
						try {
							final Process process = builder.start();
							try (InputStream inputStream = process.getInputStream();
									InputStream errorStream = process.getErrorStream();
									OutputStream outputStream = new ByteArrayOutputStream();) {
								IOUtils.copy(inputStream, outputStream);
								IOUtils.copy(errorStream, outputStream);
								process.waitFor();
								changeLog.setText(outputStream.toString());
							}
						} catch (final IOException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (final ParseException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static final long serialVersionUID = 961002543472955209L;

	private final JTextPane releaseNotes;
	private final JTextPane changeLog;
	private final JTextField startDate;
	private final JTextField endDate;
	private final JButton changeButton;
	private final JSplitPane splitPane;
	private final JButton saveButton;
	private final JTextField targetFile;
	private final JButton targetButton;

	private final SimpleDateFormat format = new SimpleDateFormat("YYY-MM-dd HH:mm:ss Z");

	public ReleaseNotesGUI() {
		setTitle("Release Notes Generator");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		releaseNotes = new JTextPane();
		changeLog = new JTextPane();
		startDate = new JTextField(10);
		endDate = new JTextField(10);
		changeButton = new JButton(new ChangeLogAction());
		saveButton = new JButton(new SaveReleaseNotes());
		targetFile = new JTextField(50);
		targetButton = new JButton(new BrowseAction());
		final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);

		final JScrollPane changeLogScroller = new JScrollPane(changeLog);
		final JScrollPane releaseNotesScroller = new JScrollPane(releaseNotes);
		final JLabel startLabel = new JLabel("Start");
		startLabel.setLabelFor(startDate);
		final JLabel endLabel = new JLabel("End");
		endLabel.setLabelFor(endDate);
		final JLabel targetLabel = new JLabel("Target");
		targetLabel.setLabelFor(targetFile);

		targetFile.setText(new File("").getAbsolutePath());
		targetFile.setEditable(false);

		final SimpleDateFormat dateFormat = new SimpleDateFormat();
		final Calendar now = Calendar.getInstance();
		endDate.setText(dateFormat.format(now.getTime()));

		now.add(Calendar.DAY_OF_YEAR, -30);
		startDate.setText(dateFormat.format(now.getTime()));

		toolBar.add(startLabel);
		toolBar.add(startDate);
		toolBar.add(new JToolBar.Separator());
		toolBar.add(endLabel);
		toolBar.add(endDate);
		toolBar.add(new JToolBar.Separator());
		toolBar.add(targetLabel);
		toolBar.add(targetFile);
		toolBar.add(targetButton);
		toolBar.add(new JToolBar.Separator());
		toolBar.add(changeButton);
		toolBar.add(saveButton);
		toolBar.setFloatable(false);

		final JPanel releaseNotesPanel = new JPanel();
		releaseNotesPanel.setBorder(BorderFactory.createTitledBorder("Release Notes"));
		releaseNotesPanel.setLayout(new BorderLayout());
		releaseNotesPanel.add(releaseNotesScroller, BorderLayout.CENTER);

		final JPanel changeLogPanel = new JPanel();
		changeLogPanel.setBorder(BorderFactory.createTitledBorder("Change Log"));
		changeLogPanel.setLayout(new BorderLayout());
		changeLogPanel.add(changeLogScroller, BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, changeLogPanel, releaseNotesPanel);

		final JPanel main = new JPanel(new BorderLayout());
		main.add(toolBar, BorderLayout.NORTH);
		main.add(splitPane, BorderLayout.CENTER);
		setContentPane(main);

		pack();
		setSize(1024, 600);
	}

	@Override
	public void setVisible(final boolean b) {
		super.setVisible(b);
		splitPane.setDividerLocation(0.4d);
	}

	public static void main(final String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new ReleaseNotesGUI().setVisible(true);
	}
}
