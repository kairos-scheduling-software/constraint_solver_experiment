package solver;

public class TimeBlock {
	public int day;
	public int startTime;
	public int endTime;
	private static String[] dayArr = {"Mon", "Tue", "Wed", "Thu", "Fri"};
	
	public TimeBlock(int _day, int _startTime, int _endTime) {
		day = _day;
		startTime = _startTime;
		endTime = _endTime;
	}
	
	public String getDay() {
		return dayArr[day];
	}
	
	public String getTime(int time) {
		int hour = time / 60;
		int min = time % 60;
		return String.format("%02d:%02d", hour, min);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
