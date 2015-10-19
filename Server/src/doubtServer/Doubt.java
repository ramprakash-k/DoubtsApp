package doubtServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Doubt {
	public int DoubtId;
	public int parentId;
	public int childCount;
    public int lines;
    public int linesReceived;
    public String rollNo;
    public String name;
    public String time;
    public int upVotesCount;
    private Set<String> upVoters;
    private Map<Integer, String> doubt;

    Doubt() {
    	parentId = -1;
    	childCount = 0;
        lines = 1;
        linesReceived = 0;
        rollNo = null;
        name = null;
        upVotesCount = 0;
        doubt = new HashMap<>();
        upVoters = new HashSet<>();
    }

    public String getDoubt() {
        String doubt = this.doubt.get(1);
        for (int i = 2; i <= lines; i++) {
            doubt = doubt + "\n" + this.doubt.get(i);
        }
        return doubt;
    }

    public void setDoubtLine(int line, String dLine) {
        doubt.put(line, dLine);
    }
    
    public String getFirstLine() {
    	return doubt.get(1);
    }
    
    public int upVote(String roll) {
    	upVoters.add(roll);
    	return upVoters.size();
    }
    
    public int nupVote(String roll) {
    	upVoters.remove(roll);
    	return upVoters.size();
    }
    
    public List<String> getInstr() {
    	List<String> instr = new ArrayList<>();
    	String m = "Add|" + Integer.toString(lines) + "|" +
    			name + "|" + rollNo + "|" + doubt.get(1) + "|" +
    			time + "|" + Integer.toString(DoubtId);
    	instr.add(m);
    	for (int i = 2; i <= lines; i++) {
    		m = "App|" + Integer.toString(i) + "|" +
    			doubt.get(i) + "|" + Integer.toString(DoubtId);
    		instr.add(m);
    	}
    	return instr;
    }
    
    public boolean hasUpvoted(String roll) {
    	return upVoters.contains(roll);
    }
}
