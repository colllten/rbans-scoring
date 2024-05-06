import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

public class ScoreTablesTester {
	private final static String[] TEST_STAGES = {"baseline", "mid", "post"};
	private final static String[] TESTS = {"attention", "language", "visuospatial", "delayed-memory", "immediate-memory"};
	private final static String[] AGE_RANGES = {"20-39", "40-49", "50-59", "60-69"};
	
	public static void main(String[] args) {
		for (String testStage : TEST_STAGES) {
			for (String test : TESTS) {
				for (String ageRange : AGE_RANGES) {
					String fileName = String.format("src/main/resources/%s/%s/%s-%s-%s.csv", testStage, test, ageRange, testStage, test);
					Table table = Table.read().csv(fileName);
					
					// Iterate through each row and check that each value is <= its right
					for (Row row : table) {
						// Iterate over each value
						for (int i = 1; i < row.columnCount() - 1; i++) {
							// If current value is not LTE next value...
							if (!(row.getInt(i) <= row.getInt(i + 1))) {
								System.out.println(fileName);
								System.out.println(row);
							}
						}
					}
					
					// Iterate through each column and check that values are in ascending order
					for (int i = 1; i < table.columnCount(); i++) {
						IntColumn column = table.intColumn(i);
						// Iterate over each value in column
						for (int j = 0; j < column.size() - 1; j++) {
							if (!(column.getInt(j) <= column.getInt(j + 1))) {
								System.out.printf("%s - %s - %s @ C%d R%d\n", testStage, test, ageRange, i - 1, j + 1);
							}
						}
					}
				}
			}
		}
	}
}
