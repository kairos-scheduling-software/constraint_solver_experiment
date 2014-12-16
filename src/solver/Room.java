package solver;

import java.util.Formatter;
import java.util.Locale;

public class Room {
	public int id;
	public int capacity;
	public TimeBlock[] timeBlock;
	public Room(int _id, int _capacity, TimeBlock[] _timeBlock) {
		id = _id;
		capacity = _capacity;
		timeBlock = _timeBlock;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		formatter.format("Room ID: %d, capacity: %d", id, capacity);
		for (TimeBlock block : timeBlock) {
			formatter.format("\n\t%s: %s - %s", block.getDay(),
					block.getTime(block.startTime), block.getTime(block.endTime));
		}
		formatter.close();
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
