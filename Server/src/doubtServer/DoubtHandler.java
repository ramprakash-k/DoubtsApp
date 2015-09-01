package doubtServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DoubtHandler {
	Map<Integer, Doubt> doubts;
	int totalCount;
	
	DoubtHandler() {
		doubts = new HashMap<Integer, Doubt>();
		totalCount = 0;
	}
	
	public int getNewId() {
		totalCount++;
		return totalCount;
	}
	
	public void addDoubt(Doubt tDoubt) {
		if (!doubts.containsKey(tDoubt.DoubtId)) {
            doubts.put(tDoubt.DoubtId, tDoubt);
        }
        else {
            Doubt doubt = doubts.get(tDoubt.DoubtId);
            doubt.lines = tDoubt.lines;
            doubt.linesReceived++;
            doubt.name = tDoubt.name;
            doubt.rollNo = tDoubt.rollNo;
            doubt.setDoubtLine(1, tDoubt.getFirstLine());
        }
	}
	
	public void appendDoubt(int doubtId, int line, String dLine) {
		if (doubts.containsKey(doubtId)) {
            Doubt doubt = doubts.get(doubtId);
            doubt.setDoubtLine(line, dLine);
            doubt.linesReceived++;
        } else {
            Doubt doubt = new Doubt();
            doubt.DoubtId = doubtId;
            doubt.linesReceived++;
            doubt.setDoubtLine(line, dLine);
            doubts.put(doubtId, doubt);
        }
	}
	
	public int upVoteDoubt(int doubtId, String roll) {
		return doubts.get(doubtId).upVote(roll);
	}
	
	public int nupVoteDoubt(int doubtId, String roll) {
		return doubts.get(doubtId).nupVote(roll);
	}
	
	public List<String> genAll(String roll) {
		List<String> instr = new ArrayList<>();
		for (Entry<Integer,Doubt> i : doubts.entrySet()) {
			Doubt doubt = i.getValue();
			if (doubt != null) {
				instr.addAll(doubt.getInstr());
				instr.add("Up|" + (doubt.hasUpvoted(roll) ? roll : "-1") + "|" +
					Integer.toString(doubt.DoubtId) + "|" +
					Integer.toString(doubt.upVotesCount));
			}
		}
		return instr;
	}
	
	public void deleteDoubt(int doubtId) {
		doubts.remove(doubtId);
	}
}
