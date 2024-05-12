import Exceptions.EmptyCellValue;
import Exceptions.FileIsDirectoryException;
import Exceptions.FileReadPermissionException;
import Exceptions.WrongFileExtension;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
	private final static String[] EXPECTED_HEADERS = {"Subject ID", "Group", "Age", "Timepoint", "List Learning",
			"Figure Copy", "Line Orientation", "Picture", "Semantic Fluency", "Digit Span", "Coding", "List Recall",
			"List Recognition", "Story Recall", "Figure Recall", "Immediate Memory", "Visuospatial/Constructional",
			"Language", "Attention", "Delayed Memory", "Sum Index", "Total Scaled Score", "List Learning SS",
			"Story Memory SS", "Figure Copy SS", "Line Orientation PG", "Picture Naming PG", "Semantic Fluency SS",
			"Digit Span SS", "Coding SS", "List Recall PG", "List Recognition PG", "Story Recall SS",
			"Figure Recall SS"};
	
	private final static String[] TESTS = {"attention", "language", "visuospatial", "delayed-memory", "immediate-memory"};
	private final static String[] AGE_RANGES = {"20-39", "40-49", "50-59", "60-69"};
	private final static String[] TEST_STAGES = {"baseline", "mid", "post"};
	
	private final static String WELCOME_MESSAGE = "Welcome to RBANS Crosschecker! Please select the file used to hold subjects' test scores.";
	private final static String FILE_PATH_PROMPT = "Enter the file path of your RBANS scores";
	
	private final static String WRONG_FILE_PATH = "The file does not exist!";
	private final static String FILE_NOT_READABLE = "Cannot read file -- check its permissions!";
	private final static String FILE_IS_DIRECTORY = "A folder was selected!";
	private final static String WRONG_FILE_EXTENSION = "Make sure to select a file that ends with \".csv\"!";
	private final static String INCORRECT_HEADERS = "Some headers are not present!";
	
	public static void main(String[] args) {
		/*
		(1) Welcome
		(2) Prompt for file path
		(3) Print each issue
		 */
		
		// Setup the filechooser to only accept .csv files
		JFileChooser fileChooser = new JFileChooser(getLastFileLocation());
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"CSV", "csv");
		fileChooser.setFileFilter(filter);
		do {
			int optionPressed; // the int returned by the following windows
			optionPressed = JOptionPane.showConfirmDialog(null, WELCOME_MESSAGE, null, JOptionPane.OK_CANCEL_OPTION);
			if (optionPressed == JOptionPane.CANCEL_OPTION || optionPressed == JOptionPane.CLOSED_OPTION) {
				return;
			}
			
			optionPressed = fileChooser.showOpenDialog(null);
			if (optionPressed == JFileChooser.CANCEL_OPTION) {
				continue;
			}
			
			File rbansScoresFile = fileChooser.getSelectedFile(); // File that has all participants' scores
			// Loop until the file is accessible
			while (true) {
				try {
					// Check that the file exists, is readable, etc.
					validateFile(rbansScoresFile, "csv");
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					continue;
				}
				// File passed all checks
				saveFileLocation(rbansScoresFile);
				break;
			}
			
			// Get scores table
			Table table = Table.read().csv(rbansScoresFile);
			// Get all column names
			ArrayList<String> columnNames = new ArrayList<>();
			for (Column<?> column : table.columnArray()) {
				columnNames.add(column.name());
			}
			
			// Check that all expected headers are present
			ArrayList<String> missingColumns = hasCorrectCsvHeaders(EXPECTED_HEADERS, columnNames);
			if (!missingColumns.isEmpty()) {
				// TODO: Change to getting back a list of missing columns
				System.out.println(INCORRECT_HEADERS);
				System.out.println(Arrays.toString(missingColumns.toArray()));
				continue; // Loop back to beginning to select file
			}
			
			// File and table passed all checks
			// For each row, calculate their scores and crosscheck
			ArrayList<String> mismatches = new ArrayList<>();
			ProgressBar progressBar = new ProgressBar(table.rowCount());
			System.out.printf("Row Count = %d\n", table.rowCount());
			int completions = 0;
			for (Row row : table) {
				completions++;
				progressBar.updateProgress();
				System.out.printf("Subject %d\n", row.getInt("Subject ID"));
				String testStage = row.getString("Timepoint");
				String ageRange = calculateRBANSAgeRange(row.getInt("Age"));
				
				try {
					int immediateMemoryScore = calculateImmediateMemoryScore(testStage, ageRange, row.getInt("Story Memory"), row.getInt("List Learning"));
					int visuospatialScore = calculateVisuospatialScore(testStage, ageRange, row.getInt("Line Orientation"), row.getInt("Figure Copy"));
					int languageScore = calculateLanguageScore(testStage, ageRange, row.getInt("Picture"), row.getInt("Semantic Fluency"));
					int attentionScore = calculateAttentionScore(testStage, ageRange, row.getInt("Digit Span"), row.getInt("Coding"));
					int delayedMemoryScore = calculateDelayedMemoryScore(testStage, ageRange, row.getInt("List Recognition"),
							row.getInt("List Recall"),
							row.getInt("Story Recall"), // TODO: Verify
							row.getInt("Figure Recall"));
					// Check if scores match
					if (immediateMemoryScore != row.getInt("Immediate Memory")) {
						String errorMessage = String.format("Subject %d - Immediate Memory - Given: %d - Calculated: %d\n",
								row.getInt("Subject ID"), row.getInt("Immediate Memory"), immediateMemoryScore);
						mismatches.add(errorMessage);
					}
					if (visuospatialScore != row.getInt("Visuospatial/Constructional")) {
						String errorMessage = String.format("Subject %d - Visuospatial - Given: %d - Calculated: %d\n",
								row.getInt("Subject ID"), row.getInt("Visuospatial/Constructional"), visuospatialScore);
						mismatches.add(errorMessage);
					}
					if (languageScore != row.getInt("Language")) {
						String errorMessage = String.format("Subject %d - Language - Given: %d - Calculated: %d\n",
								row.getInt("Subject ID"), row.getInt("Language"), languageScore);
						mismatches.add(errorMessage);
					}
					if (attentionScore != row.getInt("Attention")) {
						String errorMessage = String.format("Subject %d - Attention - Given: %d - Calculated: %d\n",
								row.getInt("Subject ID"), row.getInt("Attention"), attentionScore);
						mismatches.add(errorMessage);
					}
					if (delayedMemoryScore != row.getInt("Delayed Memory")) {
						String errorMessage = String.format("Subject %d - Delayed Memory - Given: %d - Calculated: %d\n",
								row.getInt("Subject ID"), row.getInt("Delayed Memory"), delayedMemoryScore);
						mismatches.add(errorMessage);
					}
				} catch (EmptyCellValue e) {
					System.err.printf("Subject %d has an empty cell value\n", row.getInt("Subject ID"));
				} catch (IndexOutOfBoundsException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
			System.out.printf("Completions: %d\n", completions);
			JOptionPane.showMessageDialog(null, "Complete!");
			
			// Display each error message to the user
			String[] options = {"Given is correct", "Fixed in CSV"};
			for (int i = 0; i < mismatches.size(); i++) {
				JOptionPane.showOptionDialog(null,
						mismatches.get(i),
						String.format("Mismatch (%d/%d)", i + 1, mismatches.size()),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						options,
						options[0]);
			}
			options = new String[] {"Revalidate", "Quit"};
			optionPressed = JOptionPane.showOptionDialog(null,
					"Make sure to save your changes!",
					"Revalidate?",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE,
					null,
					options,
					options[0]);
			if (optionPressed == 0) { // Revalidate?
				continue;
			}
			JOptionPane.showMessageDialog(null, "Goodbye!");
			return;
		} while (true);
	}
	
	/**
	 * Ensures the file is a file that can be read, is not a directory, and has the correct extension
	 * @param file A file to be read
	 * @throws IOException
	 */
	private static void validateFile(File file, String expectedExtension) throws IOException {
		if (!file.exists()) {
			// File does not exist
			throw new FileNotFoundException(WRONG_FILE_PATH);
		} else if (!file.canRead()) {
			// Cannot read file
			throw new FileReadPermissionException(FILE_NOT_READABLE);
		} else if (file.isDirectory()) {
			// Can't read a directory
			throw new FileIsDirectoryException(FILE_IS_DIRECTORY);
		} else if (!file.getName().endsWith(expectedExtension)) {
			// Can't read .xlsx or other types of files... yet
			throw new WrongFileExtension(WRONG_FILE_EXTENSION);
		}
	}
	
	/**
	 * Ensures that all the expected headers are in a given formatted file
	 * @param expectedHeaders Headers needed in order to us scoring tables
	 * @param givenHeaders Headers in a character-delimited file
	 * @return A List<String> that contains the names of all missing files
	 */
	private static ArrayList<String> hasCorrectCsvHeaders(String[] expectedHeaders, List<String> givenHeaders) {
		ArrayList<String> missingColumns = new ArrayList<>();
		for (int i = 0; i < expectedHeaders.length; i++) {
			if (!givenHeaders.contains(expectedHeaders[i])) {
				missingColumns.add(expectedHeaders[i]);
			}
		}
		return missingColumns;
	}
	
	private static String calculateRBANSAgeRange(int age) {
		for (String ageRange : AGE_RANGES) {
			// Find the dash
			int dashIndex = ageRange.indexOf('-');
			// Get lower and upper bounds of age range
			int lowerBound = Integer.parseInt(ageRange.substring(0, dashIndex));
			int upperBound = Integer.parseInt(ageRange.substring(dashIndex + 1));
			
			if (age >= lowerBound && age <= upperBound) {
				return ageRange;
			}
		}
		throw new IllegalArgumentException("Age is not within RBANS age range");
	}
	
	private static int calculateImmediateMemoryScore(String testStage, String ageRange, int storyMemoryScore, int listLearningScore) throws EmptyCellValue {
		System.out.printf("Story Memory: %d | List Learning: %d\n", storyMemoryScore, listLearningScore);
		if (storyMemoryScore < 0 || listLearningScore < 0) {
			throw new EmptyCellValue();
		}
		String fileName = String.format("src/main/resources/%s/immediate-memory/%s-%s-immediate-memory.csv",
				testStage, ageRange, testStage);
		Table immediateMemoryTable = Table.read().csv(fileName);
		System.out.printf("Returning: %d\n", (int) immediateMemoryTable.column(String.valueOf(storyMemoryScore)).get(listLearningScore));
		return (int) immediateMemoryTable.column(String.valueOf(storyMemoryScore)).get(listLearningScore);
	}
	
	private static int calculateVisuospatialScore(String testStage, String ageRange, int lineOrientationScore, int figureCopyScore) throws EmptyCellValue {
		System.out.printf("Line Orientation: %d | Figure Copy: %d\n", lineOrientationScore, figureCopyScore);
		if (lineOrientationScore < 0 || figureCopyScore < 0) {
			throw new EmptyCellValue();
		}
		String fileName = String.format("src/main/resources/%s/visuospatial/%s-%s-visuospatial.csv",
				testStage, ageRange, testStage);
		Table visuospatialTable = Table.read().csv(fileName);
		System.out.printf("Returning: %d\n", (int) visuospatialTable.column(String.valueOf(lineOrientationScore)).get(figureCopyScore));
		return (int) visuospatialTable.column(String.valueOf(lineOrientationScore)).get(figureCopyScore);
	}
	
	private static int calculateLanguageScore(String testStage, String ageRange, int pictureNamingScore, int semanticFluencyScore) throws EmptyCellValue {
		System.out.printf("Picture: %d | Semantic Fluency: %d\n", pictureNamingScore, semanticFluencyScore);
		if (pictureNamingScore < 0 || semanticFluencyScore < 0) {
			throw new EmptyCellValue();
		}
		String fileName = String.format("src/main/resources/%s/language/%s-%s-language.csv",
				testStage, ageRange, testStage);
		Table languageTable = Table.read().csv(fileName);
		// TODO: Convert last column "9-10" to two columns
		IntColumn column = languageTable.intColumn((pictureNamingScore >= 9) ? "9-10" : String.valueOf(pictureNamingScore));
		System.out.printf("Returning: %d\n", column.getInt((semanticFluencyScore >= 36) ? 36 : semanticFluencyScore));
		return column.getInt((semanticFluencyScore >= 36) ? 36 : semanticFluencyScore);
	}
	
	private static int calculateAttentionScore(String testStage, String ageRange, int digitSpanScore, int codingScore) throws EmptyCellValue {
		System.out.printf("ATTENTION: Digit Span: %d | Coding: %d\n", digitSpanScore, codingScore);
		if (digitSpanScore < 0 || codingScore < 0) {
			throw new EmptyCellValue();
		}
		String fileName = String.format("src/main/resources/%s/attention/%s-%s-attention.csv",
				testStage, ageRange, testStage);
		Table attentionTable = Table.read().csv(fileName);
		System.out.printf("Returning: %d\n", (int) attentionTable.column(String.valueOf(digitSpanScore)).get(codingScore));
		return (int) attentionTable.column(String.valueOf(digitSpanScore)).get(codingScore);
	}
	
	private static int calculateDelayedMemoryScore(String testStage, String ageRange, int listRecognitionScore, int listRecallScore, int storyRecallScore, int figureRecallScore) throws EmptyCellValue {
		System.out.printf("DELAYED MEMORY: List Recognition: %d | List Recall: %d | Story Recall: %d | Figure Recall: %d\n", listRecognitionScore, listRecallScore, storyRecallScore, figureRecallScore);
		if (listRecognitionScore < 0 || listRecallScore < 0 || storyRecallScore < 0 || figureRecallScore < 0) {
			throw new EmptyCellValue();
		}
		String fileName = String.format("src/main/resources/%s/delayed-memory/%s-%s-delayed-memory.csv",
				testStage, ageRange, testStage);
		Table delayedMemoryTable = Table.read().csv(fileName);
		// TODO: Convert 19-20 column into two
		if (listRecognitionScore >= 19) {
			System.out.printf("Returning: %d\n", (int) delayedMemoryTable.column("19-20").get(listRecallScore + storyRecallScore + figureRecallScore));
			return (int) delayedMemoryTable.column("19-20").get(listRecallScore + storyRecallScore + figureRecallScore);
		}
		System.out.printf("Returning: %d\n", (int) delayedMemoryTable.column(String.valueOf(listRecognitionScore)).get(listRecallScore + storyRecallScore + figureRecallScore));
		return (int) delayedMemoryTable.column(String.valueOf(listRecognitionScore)).get(listRecallScore + storyRecallScore + figureRecallScore);
	}
	
	private static String getLastFileLocation() {
		try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/lastFileLocation.txt"))) {
			return br.readLine();
		} catch (Exception e) {
			// TODO: Better handling
		}
		return null;
	}
	
	private static void saveFileLocation(File file) {
		try (PrintWriter pw = new PrintWriter("src/main/resources/lastFileLocation.txt")) {
			String filePathToSave = file.getParentFile().getAbsolutePath();
			pw.println(filePathToSave);
			pw.flush();
		} catch (Exception e) {
			// TODO: Change how this is handled
		}
	}
}
