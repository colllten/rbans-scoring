package Exceptions;

import java.io.IOException;

public class FileReadPermissionException extends IOException {
	public FileReadPermissionException() {
		super();
	}
	
	public FileReadPermissionException(String message) {
		super(message);
	}
}
