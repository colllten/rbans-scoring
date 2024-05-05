import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

public class Test {
	private final static String[] tests = {"attention", "language", "visuospatial", "delayed-memory", "immediate-memory"};
	private final static String[] ageRanges = {"20-39", "40-49", "50-59", "60-69"};
	private final static String[] testStages = {"baseline", "mid", "post"};
	
	public static void main(String[] args) {
		for (String ageRange : ageRanges) {
			for (String testStage : testStages) {
				for (String test : tests) {
					checkColumns(ageRange, testStage, test);
				}
			}
		}
	}
	
	public static void checkColumns(String ageRange, String testStage, String test) {
		//System.out.println(ageRange + testStage + test);
		String fileName = String.format("src/main/resources/%s/%s/%s-%s-%s.csv", testStage, test, ageRange, testStage, test);
		Table table = Table.read().csv(fileName);
		
		// Get the columns
		Column<?>[] columns = table.columnArray();
		// Iterate through each column
		for (int i = 0; i < columns.length; i++) {
			Column<?> column = columns[i];
			String columnClass = column.getClass().getName();
			if (!columnClass.equals("tech.tablesaw.api.IntColumn") && i != 0) {
				System.out.printf("%s-%s-%s\n", testStage, test, ageRange);
				System.out.printf("Column %d: %s\n", i, columnClass);
				System.out.println(columns[i]);
			}
		}
	}
}
