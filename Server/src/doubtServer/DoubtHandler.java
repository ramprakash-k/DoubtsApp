package doubtServer;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;

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
            if (tDoubt.linesReceived == tDoubt.lines) {
            	addDoubtToGrid(tDoubt);
            }
        }
        else {
            Doubt doubt = doubts.get(tDoubt.DoubtId);
            doubt.lines = tDoubt.lines;
            doubt.linesReceived++;
            doubt.name = tDoubt.name;
            doubt.rollNo = tDoubt.rollNo;
            doubt.setDoubtLine(1, tDoubt.getFirstLine());
            if (doubt.linesReceived == doubt.lines) {
            	addDoubtToGrid(doubt);
            }
        }
	}
	
	public void appendDoubt(int doubtId, int line, String dLine) {
		if (doubts.containsKey(doubtId)) {
            Doubt doubt = doubts.get(doubtId);
            doubt.setDoubtLine(line, dLine);
            doubt.linesReceived++;
            if (doubt.linesReceived == doubt.lines) {
            	addDoubtToGrid(doubt);
            }
        } else {
            Doubt doubt = new Doubt();
            doubt.DoubtId = doubtId;
            doubt.linesReceived++;
            doubt.setDoubtLine(line, dLine);
            doubts.put(doubtId, doubt);
        }
	}
	
	public int upVoteDoubt(int doubtId, String roll) {
		int nc = doubts.get(doubtId).upVote(roll);
		JFormattedTextField comp = (JFormattedTextField) Server.pane.getComponent(5*doubtId + 4);
		comp.setText(Integer.toString(nc));
		Server.pane.updateUI();
		return nc;
	}
	
	public int nupVoteDoubt(int doubtId, String roll) {
		int nc = doubts.get(doubtId).nupVote(roll);
		JFormattedTextField comp = (JFormattedTextField) Server.pane.getComponent(5*doubtId + 4);
		comp.setText(Integer.toString(nc));
		Server.pane.updateUI();
		return nc;
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
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void deleteDoubt(int doubtId) {
		doubts.remove(doubtId);
		for (int i = 0; i < 5; i++) {
			JFormattedTextField comp = (JFormattedTextField) Server.pane.getComponent(5*doubtId + i);
			Map attr = comp.getFont().getAttributes();
			attr.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			comp.setFont(new Font(attr));
		}
		Server.pane.updateUI();
	}
	
	private void addDoubtToGrid(Doubt doubt) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		JFormattedTextField label1 = new JFormattedTextField(doubt.name);
		label1.setEditable(false);
		label1.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		gbc.weightx = 1;gbc.gridx = 0;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label1, gbc);
		
		JFormattedTextField label2 = new JFormattedTextField(doubt.rollNo);
		label2.setEditable(false);
		label2.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		gbc.weightx = 1;gbc.gridx = 1;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label2, gbc);
		
		JFormattedTextField label3 = new JFormattedTextField(doubt.time);
		label3.setEditable(false);
		label3.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		gbc.weightx = 1;gbc.gridx = 2;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label3, gbc);
		
		JFormattedTextField label4 = new JFormattedTextField(doubt.getDoubt());
		label4.setEditable(false);
		label4.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		gbc.weightx = 5;gbc.gridx = 3;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label4, gbc);
		
		JFormattedTextField label5 = new JFormattedTextField(Integer.toString(doubt.upVotesCount));
		label5.setEditable(false);
		label5.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		gbc.weightx = 1;gbc.gridx = 4;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label5, gbc);
		Server.pane.updateUI();
		System.out.println("Row Added");
	}
}
