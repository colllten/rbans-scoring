package Exceptions;

import java.io.IOException;

public class FileIsDirectoryException extends IOException {
	public FileIsDirectoryException() {
		super();
	}
	
	public FileIsDirectoryException(String message) {
		super(message);
	}
}
