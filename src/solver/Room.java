package solver;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static solver.Utils.*;

public class Room {
	public int id;
	public String name;
	
	public int capacity;
	public TimeBlock[] timeBlock;
	
	public Room(int _id, int _capacity, TimeBlock[] _timeBlock) {
		this(_id, "", _capacity, _timeBlock);
	}
	
	public Room(int _id, String _name, int _capacity, TimeBlock[] _timeBlock) {
		this.id = _id;
		this.name = _name;
		this.capacity = _capacity;
		this.timeBlock = _timeBlock;
	}
	
	public Room(int _id, JSONObject jsonObj) throws JSONException {
		this.id = _id;
		this.name = jsonObj.getString("name");
		this.capacity = jsonObj.getInt("capacity");
		this.timeBlock = TimeBlock.parseTimeBlock(jsonObj.getJSONArray("time"));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		formatter.format("Room: %s", this.name);
		formatter.format("\n\tID: %d, capacity: %d", this.id, this.capacity);
		for (TimeBlock block : timeBlock) {
			formatter.format("\n\t%s: %s - %s", block.getDay(),
					block.getTime(block.startTime), block.getTime(block.endTime));
		}
		formatter.close();
		return sb.toString();
	}
	
	public static Room[] parseRooms(List<JSONObject> jsonObj) throws JSONException {
		// TODO: Fill up parseRooms method
		Room[] rooms = new Room[jsonObj.size()];
		for (int i = 0; i < rooms.length; i++) {
			rooms[i] = new Room(i, jsonObj.get(i));
		}
		return rooms;
	}

	/**
	 * @param args
	 * @throws JSONException
	 */
	public static void main(String[] args) throws JSONException {
		// TODO Auto-generated method stub
		JSONObject jsonObj = new JSONObject(readInput(System.in));
		JSONArray jsonResources = jsonObj.getJSONArray("resources");
		ArrayList<JSONObject> jsonRooms = new ArrayList<JSONObject>();
		int n = jsonResources.length();
		for (int i = 0; i < n; i++) {
			jsonObj = jsonResources.getJSONObject(i);
			if (jsonObj.getString("type").equals("room")) {
				jsonRooms.add(jsonObj);
			}
		}
		Room[] rooms = parseRooms(jsonRooms);
		
		for (Room room : rooms) {
			System.out.println(room);
		}
	}

}
