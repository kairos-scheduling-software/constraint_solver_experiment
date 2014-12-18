package solver;

import org.json.JSONArray;
import org.json.JSONException;

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
	
	public static TimeBlock[] parseTimeBlock(JSONArray jsonArr) throws JSONException {
		TimeBlock[] blocks = new TimeBlock[jsonArr.length()];
		for (int i = 0; i < jsonArr.length(); i++) {
			JSONArray item = jsonArr.getJSONArray(i);
			blocks[i] = new TimeBlock(item.getInt(0), item.getInt(1), item.getInt(2));
		}
		return blocks;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
