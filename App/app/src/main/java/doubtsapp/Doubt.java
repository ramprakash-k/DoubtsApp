package doubtsapp;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to store data of a doubt
 */
public class Doubt {
    public int DoubtId;
    public int lines;
    public int linesReceived;
    public String rollNo;
    public String name;
    public String time;
    public int upVotesCount;
    public boolean hasUserUpVoted;
    public boolean canUserUpVote;
    public boolean isOwnDoubt;
    private Map<Integer, String> doubt;

    Doubt() {
        lines = 1;
        linesReceived = 0;
        rollNo = null;
        name = null;
        upVotesCount = 0;
        canUserUpVote = false;
        isOwnDoubt = false;
        hasUserUpVoted = false;
        doubt = new HashMap<>();
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
}
