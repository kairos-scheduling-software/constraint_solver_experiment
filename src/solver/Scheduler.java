package solver;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;

public class Scheduler {
	Solver solver;
	
	ClassEvent[] classes;
	
	// TODO: Change these to Resource class (instead of Room, Professor...)
	Room[] rooms;
	Professor[] professors;
	
	private boolean modelBuilt = false;
	
	public Scheduler(JSONObject jsonObj) throws JSONException {
		JSONArray jsonClasses, jsonResources;
		ArrayList<JSONObject> jsonRooms = new ArrayList<JSONObject>();
		// ArrayList<JSONObject> jsonProfs;
		
		jsonClasses = jsonObj.getJSONArray("events");
		jsonResources = jsonObj.getJSONArray("resources");
		
		//jsonProfs = new ArrayList<JSONObject>();
		for (int i = 0; i < jsonResources.length(); i++) {
			JSONObject obj = jsonResources.getJSONObject(i);
			if (obj.getString("type").equals("room")) {
				jsonRooms.add(obj);
			}
		}
		
		this.solver = new Solver();
		this.rooms = Room.parseRooms(jsonRooms);
		int[] roomIds = new int[this.rooms.length];
		for (int i = 0; i < roomIds.length; i++) {
			roomIds[i] = this.rooms[i].id;
		}
		this.classes = ClassEvent.parseClasses(this.solver, roomIds, jsonClasses);
	}
	
	private Scheduler(Solver solver, ClassEvent[] classes, Room[] rooms) {
		this.solver = solver;
		this.classes = classes;
		this.rooms = rooms;
	}
	
	private void buildModel() {
		Constraint constraint = IntConstraintFactory.TRUE(this.solver);
		int n = this.classes.length;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				constraint = LogicalConstraintFactory.and(constraint,
						this.classes[i].notOverlap(this.classes[j]));
			}
			for (Room room : this.rooms) {
				constraint = LogicalConstraintFactory.and(constraint,
						this.classes[i].roomConstraint(room));
			}
			constraint = LogicalConstraintFactory.and(constraint, this.classes[i].eventConstraint);
		}
		
		this.solver.post(constraint);
		
		modelBuilt = true;
	}
	
	public boolean findSolution() {
		if (!modelBuilt) buildModel();
		return solver.findSolution();
	}
	
	
	// Functions for testing the Scheduler
	private static Room[] initRooms(Solver solver) {
		Room[] rooms = new Room[2];
		
		TimeBlock[] blocks = new TimeBlock[5];
		for (int i = 0; i < 5; i++) {
			blocks[i] = new TimeBlock(i, 600, 960);
		}
		rooms[0] = new Room(0, 80, blocks);
		
		blocks = new TimeBlock[3];
		for (int i = 0; i < 3; i++) {
			blocks[i] = new TimeBlock(i+1, 700, 1100);
		}
		rooms[1] = new Room(1, 50, blocks);
		
		return rooms;
	}
	
	private static ClassEvent[] initClass(Solver solver) {
		ClassEvent[] classes = new ClassEvent[4];
		
		classes[0] = new ClassEvent(solver, 2, 60, "prof1", 2, 170);
		classes[1] = new ClassEvent(solver, 2, 50, "prof2", 3, 50);
		classes[2] = new ClassEvent(solver, 2, 50, "prof3", 2, 170);
		classes[3] = new ClassEvent(solver, 2, 40, "prof4", 2, 50);
		
		return classes;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Solver solver = new Solver();
		Room[] rooms = initRooms(solver);
		ClassEvent[] classes = initClass(solver);
		for (Room room : rooms) {
			System.out.println(room);
		}
		
		Scheduler scheduler = new Scheduler(solver, classes, rooms);
		
		if(scheduler.findSolution()) {
			for (int i = 0; i < 4; i++) {
				int startTime = classes[i].startTime.getValue();
				int startHour = startTime / 60;
				int startMinute = startTime % 60;
				int daysCount = classes[i].days_count;
				int startDay = classes[i].startDay.getValue();
				String days = "";
				if (startDay == 0) {
					if (daysCount == 3) days = "Mon-Wed-Fri";
					else days = "Mon-Wed";
				} else days = "Tue-Thu";
				System.out.printf("Room: %d, capacity: %d, days: %-12s, start time: %02d:%02d, duration: %d minutes\n",
						classes[i].roomId.getValue(), classes[i].capacity.getValue(), days, startHour, startMinute, classes[i].duration);
			}
			//solver.isFeasible();
		}
	}

}
