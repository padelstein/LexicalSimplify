package simplify;

public class BadAlignmentException extends Exception{
	private String message;
	
	public BadAlignmentException(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String toString(){
		return "MisalignmentException:: " + message;
	}
}
