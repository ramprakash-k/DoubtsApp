package doubtServer;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

public class DoubtHandler{
	Map<Integer, Doubt> doubts;
	int totalCount;
	Broadcaster broadcaster;
	int selected;
	
	DoubtHandler(Broadcaster caster) {
		doubts = new HashMap<Integer, Doubt>();
		totalCount = 0;
		broadcaster = caster;
		selected = -1;
	}
	
	public int getNewId() {
		totalCount++;
		return totalCount;
	}
	
	public void addDoubt(Doubt tDoubt) {
		doubts.put(tDoubt.DoubtId, tDoubt);
        if (tDoubt.linesReceived == tDoubt.lines) {
        	addDoubtToGrid(tDoubt);
        }
	}
	
	public void editDoubt(int doubtId, String dbt) {
		Doubt doubt = doubts.get(doubtId);
		String dbts[] = dbt.split("[\n]");
		for (int i = 1; i <= dbts.length; i++) {
			doubt.setDoubtLine(i, dbts[i-1]);
		}
		doubt.linesReceived = doubt.lines = dbts.length;
		JTextArea comp = (JTextArea) Server.pane.getComponent(5*doubtId + 3);
		dbt.replaceAll("\\n", "<br>");
		comp.setText(dbt);
		Server.pane.updateUI();
	}
	
	public void appendDoubt(int doubtId, int line, String dLine) {
		Doubt doubt = doubts.get(doubtId);
        doubt.setDoubtLine(line, dLine);
        doubt.linesReceived++;
        if (doubt.linesReceived == doubt.lines) {
        	addDoubtToGrid(doubt);
        }
	}
	
	public int upVoteDoubt(int doubtId, String roll) {
		int nc = doubts.get(doubtId).upVote(roll);
		JTextArea comp = (JTextArea) Server.pane.getComponent(5*doubtId + 4);
		comp.setText(Integer.toString(nc));
		Server.pane.updateUI();
		return nc;
	}
	
	public int nupVoteDoubt(int doubtId, String roll) {
		int nc = doubts.get(doubtId).nupVote(roll);
		JTextArea comp = (JTextArea) Server.pane.getComponent(5*doubtId + 4);
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
			JTextArea comp = (JTextArea) Server.pane.getComponent(5*doubtId + i);
			Map attr = comp.getFont().getAttributes();
			attr.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			comp.setFont(new Font(attr));
			for (MouseListener it : comp.getMouseListeners()) {
				comp.removeMouseListener(it);
			}
		}
		Server.pane.updateUI();
	}
	
	private MouseAdapter getAdapter(final int doubtId) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				rightClickAction(e,doubtId);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				rightClickAction(e,doubtId);
			}
		};
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void rightClickAction(MouseEvent e, final int doubtId) {
		if (e.isPopupTrigger()){
			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent a) {
					String s = a.getActionCommand();
					switch(s) {
					case "Delete": {
						System.out.println("Delete pressed " + Integer.toString(doubtId));
						if (selected == doubtId) {
							selected = -1;
							for (int i = 0; i < 5; i++) {
								JTextArea comp = (JTextArea) Server.pane.getComponent(5*doubtId + i);
								Map attr = comp.getFont().getAttributes();
								attr.put(TextAttribute.BACKGROUND, Color.WHITE);
								comp.setFont(new Font(attr));
							}
							Server.pane.updateUI();
						}
						deleteDoubt(doubtId);
						broadcaster.broadcastMessage("Del|"+Integer.toString(doubtId)+"|-1");
						break;
					} case "Edit": {
						System.out.println("Edit pressed " + Integer.toString(doubtId));
						JPanel panel = new JPanel();
						panel.setSize(400, 300);
						String dbt = doubts.get(doubtId).getDoubt();
						dbt.replaceAll("\\n","<br>");
						JTextArea text = new JTextArea(dbt);
						text.setSize(399, 200);
						ScrollPane scroll = new ScrollPane();
						scroll.setSize(399, 200);
						scroll.add(text);
						panel.add(scroll);
						int result = JOptionPane.showConfirmDialog(
								null,
								panel,
								"Edit the doubt?",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE);
						if (result == JOptionPane.YES_OPTION) {
							System.out.println("Edited : " + text.getText());
							editDoubt(doubtId, text.getText());
							String doubt[] = text.getText().split("[\n]");
							broadcaster.broadcastMessage("Edit|"
									+ Integer.toString(doubt.length) + "|"
									+ doubt[0] + "|" + Integer.toString(doubtId));
							for (int i = 2; i <= doubt.length; i++) {
								broadcaster.broadcastMessage("Epp|"
										+ Integer.toString(i) + "|"
										+ doubt[i-1] + "|" + Integer.toString(doubtId));
							}
						} else {
							System.out.println("Editing cancelled");
						}
						break;
					} case "Merge With": {
						selected = doubtId;
						for (int i = 0; i < 5; i++) {
							JTextArea comp = (JTextArea) Server.pane.getComponent(5*doubtId + i);
							Map attr = comp.getFont().getAttributes();
							attr.put(TextAttribute.BACKGROUND, Color.YELLOW);
							comp.setFont(new Font(attr));
						}
						Server.pane.updateUI();
						break;
					} case "Cancel Merge": {
						selected = -1;
						for (int i = 0; i < 5; i++) {
							JTextArea comp = (JTextArea) Server.pane.getComponent(5*doubtId + i);
							Map attr = comp.getFont().getAttributes();
							attr.put(TextAttribute.BACKGROUND, Color.WHITE);
							comp.setFont(new Font(attr));
						}
						Server.pane.updateUI();
						break;
					} case "Merge Into": {
						Doubt child = doubts.get(selected);
						Doubt parent = doubts.get(doubtId);
						child.parentId = doubtId;
						parent.childCount = parent.childCount + 1 + child.childCount;
						for (int i = 0; i < 5; i++) {
							JTextArea comp = (JTextArea) Server.pane.getComponent(5*selected + i);
							Map attr = comp.getFont().getAttributes();
							attr.put(TextAttribute.BACKGROUND, Color.LIGHT_GRAY);
							comp.setFont(new Font(attr));
							for (MouseListener it : comp.getMouseListeners()) {
								comp.removeMouseListener(it);
							}
						}
						Server.pane.updateUI();
						broadcaster.broadcastMessage("Merge|"
								+ Integer.toString(selected) + "|"
								+ Integer.toString(doubtId));
						selected = -1;
						break;
					}
					}
				}
			};
			JPopupMenu menu = new JPopupMenu();
			JMenuItem merge;
			if (selected == -1) {
				merge = new JMenuItem("Merge With");
			} else if (selected != doubtId) {
				merge = new JMenuItem("Merge Into");
			} else {
				merge = new JMenuItem("Cancel Merge");
			}
			merge.addActionListener(listener);
			menu.add(merge);
			JMenuItem del = new JMenuItem("Delete");
			del.addActionListener(listener);
			JMenuItem edit = new JMenuItem("Edit");
			edit.addActionListener(listener);
			menu.add(del);
			menu.add(edit);
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	private void addDoubtToGrid(Doubt doubt) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		MouseAdapter adapter = getAdapter(doubt.DoubtId);
		
		JTextArea label1 = new JTextArea(doubt.name);
		label1.setEditable(false);
		label1.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label1.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 0;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label1, gbc);
		
		JTextArea label2 = new JTextArea(doubt.rollNo);
		label2.setEditable(false);
		label2.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label2.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 1;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label2, gbc);
		
		JTextArea label3 = new JTextArea(doubt.time);
		label3.setEditable(false);
		label3.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label3.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 2;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label3, gbc);
		
		String dbt = doubt.getDoubt();
		dbt.replaceAll("\\n","<br>");
		JTextArea label4 = new JTextArea(dbt);
		label4.setEditable(false);
		label4.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label4.addMouseListener(adapter);
		gbc.weightx = 5;gbc.gridx = 3;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label4, gbc);
		
		JTextArea label5 = new JTextArea(Integer.toString(doubt.upVotesCount));
		label5.setEditable(false);
		label5.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label5.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 4;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label5, gbc);
		Server.pane.updateUI();
		System.out.println("Row Added");
	}
}
