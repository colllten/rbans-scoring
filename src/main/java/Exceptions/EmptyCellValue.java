package Exceptions;

public class EmptyCellValue extends Exception {
	public EmptyCellValue() {
		super();
	}
	
	public EmptyCellValue(String message) {
		super(message);
	}
}
