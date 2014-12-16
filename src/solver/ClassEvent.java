package solver;

import solver.constraints.*;
import solver.variables.*;

// The ones below are for choco-solver v3.3.0
//import org.chocosolver.solver.Solver;
//import org.chocosolver.solver.constraints.Constraint;
//import org.chocosolver.solver.constraints.IntConstraintFactory;
//import org.chocosolver.solver.constraints.LogicalConstraintFactory;
//import org.chocosolver.solver.variables.IntVar;
//import org.chocosolver.solver.variables.VariableFactory;

public class ClassEvent {
	Solver solver;
	IntVar capacity;
	String professor;
	int days_count;
	int duration;
	IntVar startTime, endTime;
	IntVar startDay;
	IntVar roomId;
	public Constraint eventConstraint;
	
	static int[] mwf = {450, 515, 580, 645, 710, 775, 840, 905, 970};
	static int[] mwf80 = {485, 710, 805, 900};
	static int[] th = {450, 550, 645, 745, 840, 940};

	
	// days_count and startDay together defines the days_of_week for a class
	
	
	public ClassEvent(Solver _solver, int room_count, int _capacity, String _professor,
			int _days, int _duration) {
		solver = _solver;
		professor = _professor;
		
		roomId = VariableFactory.bounded("room id", 0, room_count - 1, solver);
		capacity = VariableFactory.fixed(_capacity, solver);
		
		days_count = _days;
		duration = _duration;
		
		startTime = VariableFactory.enumerated("class start time",
				initStartTime(days_count, duration), solver);
		endTime = VariableFactory.offset(startTime, duration);
		
		if (duration == 50) {
			startDay = VariableFactory.fixed(0, solver);
			eventConstraint = IntConstraintFactory.member(startTime, mwf);
		} else {
			startDay = VariableFactory.bounded("start day", 0, 1, solver);
			if (duration == 80) {
				eventConstraint = LogicalConstraintFactory.or(
					LogicalConstraintFactory.and(
						IntConstraintFactory.arithm(startDay, "=", 0),
						IntConstraintFactory.member(startTime, mwf80)),
					LogicalConstraintFactory.and(
						IntConstraintFactory.arithm(startDay, "=", 1),
						IntConstraintFactory.member(startTime, th)));
			} else {
				eventConstraint = LogicalConstraintFactory.or(
					LogicalConstraintFactory.and(
						IntConstraintFactory.arithm(startDay, "=", 0),
						IntConstraintFactory.member(startTime, mwf)),
					LogicalConstraintFactory.and(
						IntConstraintFactory.arithm(startDay, "=", 1),
						IntConstraintFactory.member(startTime, th)));
			}
		}
	}
	
	static private int[] initStartTime(int daysCount, int duration) {
		int[] blocks;
		if (daysCount == 3) {
			if (duration != 80) blocks = mwf;
			else blocks = mwf80;
		} else {
			if (duration == 80) {
				blocks = new int[]{450, 485, 550, 645, 710, 745, 805, 840, 900, 940};
			}
			else if (duration == 170) {
				blocks = new int[]{450, 515, 550, 580, 645, 710, 745, 775, 840, 905, 940, 970};
			} else blocks = mwf;
		}
		
		return blocks;
	}
	
	public Constraint notOverlap(ClassEvent other) {
		Constraint _room = IntConstraintFactory.arithm(this.roomId, "!=", other.roomId);
		Constraint _day = IntConstraintFactory.arithm(this.startDay, "!=", other.startDay);
		Constraint _time1 = IntConstraintFactory.arithm(this.endTime, "<=", other.startTime);
		Constraint _time2 = IntConstraintFactory.arithm(other.endTime, "<=", this.startTime);
		return LogicalConstraintFactory.or(_room, _day, _time1, _time2);
	}
	
	public Constraint roomConstraint(Room room) {
		Constraint _id = IntConstraintFactory.arithm(this.roomId, "!=", room.id);
		Constraint _fit = IntConstraintFactory.arithm(this.capacity, "<=", room.capacity);
		int n = room.timeBlock.length;
		for (int i = 0; i < this.days_count; i++) {
			Constraint _time = IntConstraintFactory.FALSE(solver);
			for (int j = 0; j < n; j++) {
				TimeBlock timeBlock = room.timeBlock[j];
				Constraint tmp = LogicalConstraintFactory.and(
						IntConstraintFactory.arithm(VariableFactory.offset(this.startDay, i * 2), "=", timeBlock.day),
						IntConstraintFactory.arithm(this.startTime, ">=", timeBlock.startTime),
						IntConstraintFactory.arithm(this.endTime, "<=", timeBlock.endTime));
				_time = LogicalConstraintFactory.or(_time, tmp);
			}
			_fit = LogicalConstraintFactory.and(_fit, _time);
		}
		return LogicalConstraintFactory.or(_id, _fit);
	}
	
	public static Constraint buildConstraint(Solver solver, ClassEvent[] classes, Room[] rooms) {
		Constraint constraint = IntConstraintFactory.TRUE(solver);
		int n = classes.length;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				constraint = LogicalConstraintFactory.and(constraint,
						classes[i].notOverlap(classes[j]));
			}
			for (Room room : rooms) {
				constraint = LogicalConstraintFactory.and(constraint,
						classes[i].roomConstraint(room));
			}
			constraint = LogicalConstraintFactory.and(constraint, classes[i].eventConstraint);
		}
		
		return constraint;
	}
	
	private static Room[] initRooms() {
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
		
		Room[] rooms = initRooms();
		ClassEvent[] classes = initClass(solver);
		for (Room room : rooms) {
			System.out.println(room);
		}
		
		Constraint constraint = buildConstraint(solver, classes, rooms);
		
		solver.post(constraint);
		
		if(solver.findSolution()) {
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
				System.out.printf("Room: %d, capacity: %d, daysCount: %-12s, start time: %02d:%02d, duration: %d minutes\n",
						classes[i].roomId.getValue(), classes[i].capacity.getValue(), days, startHour, startMinute, classes[i].duration);
			}
			solver.isFeasible();
		}
		
	}

}
