import javax.swing.*;
import java.awt.*;

/**
 * A class that represents a progress bar to show the user a GUI and give them a sense of where their task is currently
 * at.
 *
 * @author Colten Glover
 *
 * @version 05/07/2024
 */
public class ProgressBar extends JFrame {
	
	private JProgressBar progressBar;
	private int completed = 0;
	private int goal;
	
	public ProgressBar(int goal) {
		setTitle("Progress Bar Example");
		setSize(300, 150);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		
		// Create progress bar
		this.goal = goal;
		progressBar = new JProgressBar(0, goal);
		progressBar.setStringPainted(true); // Display progress as text
		progressBar.setValue(0);
		add(progressBar, BorderLayout.CENTER);
		setVisible(true);
	}
	
	public void updateProgress() {
		completed++;
		double percentCompleted = (((double) completed) / goal) * 100;
		progressBar.setValue((int) percentCompleted);
		
		// Check if progress is complete
		if (completed == goal) {
			progressBar.setValue(100);
			dispose();
		}
	}
}
