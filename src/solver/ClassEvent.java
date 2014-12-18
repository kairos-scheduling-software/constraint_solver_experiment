package solver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	int id;
	String name;
	String professor;

	int days_count;
	int duration;
	
	Solver solver;
	IntVar capacity;
	IntVar roomId;
	
	IntVar startTime, endTime;
	IntVar startDay;
	public Constraint eventConstraint;
	
	static int[] mwf = {450, 515, 580, 645, 710, 775, 840, 905, 970};
	static int[] mwf80 = {485, 710, 805, 900};
	static int[] th = {450, 550, 645, 745, 840, 940};

	
	// days_count and startDay together defines the days_of_week for a class
	
	public ClassEvent(Solver _solver, int[] _roomIds, JSONObject jsonClass) throws JSONException {
		this.solver = _solver;
		
		this.id = jsonClass.getInt("id");
		this.name = jsonClass.getString("name");
		this.professor = jsonClass.getString("professor");
		
		this.days_count = jsonClass.getInt("days");
		this.duration = jsonClass.getInt("duration");
		
		this.capacity = VariableFactory.fixed(jsonClass.getInt("capacity"), this.solver);
		this.roomId = VariableFactory.enumerated("room id", _roomIds, this.solver);
		
		this.initStartTime();
	}
	
	public ClassEvent(Solver _solver, int room_count, int _capacity, String _professor,
			int _days, int _duration) {
		solver = _solver;
		professor = _professor;
		
		roomId = VariableFactory.bounded("room id", 0, room_count - 1, solver);
		capacity = VariableFactory.fixed(_capacity, solver);
		
		days_count = _days;
		duration = _duration;
		
		this.initStartTime();
	}
	
	private void initStartTime() {
		int[] blocks;
		if (days_count == 3) {
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
		
		startTime = VariableFactory.enumerated("class start time",
				blocks, solver);
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
	
	public static ClassEvent[] parseClasses(Solver _solver,
			int[] _roomIds, JSONArray jsonClasses) throws JSONException {
		ClassEvent[] classes = new ClassEvent[jsonClasses.length()];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = new ClassEvent(_solver, _roomIds, jsonClasses.getJSONObject(i));
		}
		
		return classes;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
