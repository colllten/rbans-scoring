package Exceptions;

import java.io.IOException;

public class WrongFileExtension extends IOException {
	public WrongFileExtension() {
		super();
	}
	
	public WrongFileExtension(String message) {
		super(message);
	}
}
