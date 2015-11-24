package doubtServer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.ScrollPane;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class DoubtHandler{
	Map<Integer, Doubt> doubts;
	Map<Integer, Integer> locks; // 0 - free; 1 - server; 2 - client;
	List<Integer> positionIdMap;
	int currentSort; // 0 - earliest; 1 - latest; 2 - maxUpvotes; 3 - minUpvotes;
	int totalCount;
	Broadcaster broadcaster;
	int selected;
	
	DoubtHandler(Broadcaster caster) {
		doubts = new HashMap<Integer, Doubt>();
		locks = new HashMap<Integer, Integer>();
		positionIdMap = new ArrayList<Integer>();
		currentSort = 0;
		totalCount = 0;
		broadcaster = caster;
		selected = -1;
	}
	
	public int getNewId() {
		totalCount++;
		return totalCount;
	}
	
	public void addDoubt(Doubt tDoubt) {
		tDoubt.position = tDoubt.DoubtId - 1;
		positionIdMap.add(tDoubt.DoubtId);
		doubts.put(tDoubt.DoubtId, tDoubt);
		locks.put(tDoubt.DoubtId, 0);
        if (tDoubt.linesReceived == tDoubt.lines) {
        	addDoubtToGrid(tDoubt);
        }
	}
	
	public boolean getLock(int doubtId, int who) {
		if (locks.get(doubtId) == 0) {
			locks.put(doubtId, who);
			return true;
		} else {
			return false;
		}
	}
	
	public void releaseLock(int doubtId) {
		locks.put(doubtId, 0);
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
		JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(doubtId).position + 5);
		comp.setText(Integer.toString(nc));
		Server.pane.updateUI();
		return nc;
	}
	
	public int nupVoteDoubt(int doubtId, String roll) {
		int nc = doubts.get(doubtId).nupVote(roll);
		JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(doubtId).position + 5);
		comp.setText(Integer.toString(nc));
		Server.pane.updateUI();
		return nc;
	}
	
	public List<String> genAll(String roll) {
		List<String> instr = new ArrayList<>();
		for (Entry<Integer,Doubt> i : doubts.entrySet()) {
			Doubt doubt = i.getValue();
			if (doubt != null && !doubt.deleted) {
				instr.addAll(doubt.getInstr());
				instr.add("Up|" + (doubt.hasUpvoted(roll) ? roll : "-1") + "|" +
					Integer.toString(doubt.DoubtId) + "|" +
					Integer.toString(doubt.getUpVotesCount()));
			}
		}
		return instr;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void deleteDoubt(int doubtId) {
		doubts.get(doubtId).deleted = true;
		for (int i = 0; i < 6; i++) {
			JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(doubtId).position + i);
			Map attr = comp.getFont().getAttributes();
			attr.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			comp.setFont(new Font(attr));
			for (MouseListener it : comp.getMouseListeners()) {
				comp.removeMouseListener(it);
			}
		}
		Server.pane.updateUI();
	}
	
	public void sortClickAction(boolean byTime) {
		if (byTime) {
			if  (currentSort < 2) currentSort = 1 - currentSort;
			else currentSort = 0;
		} else {
			if (currentSort > 1) currentSort = 5 - currentSort;
			else currentSort = 2;
		}
		((JLabel) Server.panel.getComponent(3)).setText("Time");
		((JLabel) Server.panel.getComponent(5)).setText("Upvotes");
		switch(currentSort) {
			case 0:
				((JLabel) Server.panel.getComponent(3)).setText("Time \u25bd");
				break;
			case 1:
				((JLabel) Server.panel.getComponent(3)).setText("Time \u25b3");
				break;
			case 2:
				((JLabel) Server.panel.getComponent(5)).setText("Upvotes \u25bd");
				break;
			case 3:
				((JLabel) Server.panel.getComponent(5)).setText("Upvotes \u25b3");
				break;
		}
		fixSort();
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
	
	private Timer getTimer(final JButton timeButton, final Box panel, final int doubtId) {
		final Timer timer = new Timer(1000, null);
		timer.addActionListener(new ActionListener() {
			int time = 45;
			@Override
			public void actionPerformed(ActionEvent e) {
				timeButton.setText("Extend (" + Integer.toString(time) + ")");
				time--;
				if (time == 0) {
					Window w = SwingUtilities.getWindowAncestor(panel);
					if (w != null) {
						w.setVisible(false);
						releaseLock(doubtId);
					}
					timer.stop();
				}
			}
		});
		return timer;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private Timer getMergeTimer() {
		final Timer timer = new Timer(1000, null);
		timer.addActionListener(new ActionListener() {
			int time = 15;
			@Override
			public void actionPerformed(ActionEvent e) {
				time--;
				if (time == 0 && selected != -1) {
					releaseLock(selected);
					for (int i = 0; i < 6; i++) {
						JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(selected).position + i);
						Map attr = comp.getFont().getAttributes();
						attr.put(TextAttribute.BACKGROUND, Color.WHITE);
						comp.setFont(new Font(attr));
					}
					selected = -1;
					Server.pane.updateUI();
					timer.stop();
				}
				if (time == 0) {
					System.out.println("Timer stopped manually");
					timer.stop();
				}
				if (time < 0) {
					System.out.println("Not Stopping " + time);
				}
			}
		});
		return timer;
	}
	
	private JOptionPane getOptionPane(JComponent parent) {
        JOptionPane pane = null;
        if (!(parent instanceof JOptionPane)) {
            pane = getOptionPane((JComponent)parent.getParent());
        } else {
            pane = (JOptionPane) parent;
        }
        return pane;
    }
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void rightClickAction(MouseEvent e, final int doubtId) {
		if (e.isPopupTrigger()){
			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent a) {
					String s = a.getActionCommand();
					final Timer mergeTimer = getMergeTimer();
					switch(s) {
					case "Delete": {
						System.out.println("Delete pressed " + Integer.toString(doubtId));
						if (selected == doubtId) {
							selected = -1;
							for (int i = 0; i < 6; i++) {
								JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(doubtId).position + i);
								Map attr = comp.getFont().getAttributes();
								attr.put(TextAttribute.BACKGROUND, Color.WHITE);
								comp.setFont(new Font(attr));
							}
							Server.pane.updateUI();
						}
						if (getLock(doubtId, 1)) {
							deleteDoubt(doubtId);
							releaseLock(doubtId);
							broadcaster.broadcastMessage("Del|"+Integer.toString(doubtId)+"|-1");
						}
						else {
							JOptionPane.showMessageDialog(null,"Can't delete doubt now");
						}
						break;
					} case "Edit": {
						System.out.println("Edit pressed " + Integer.toString(doubtId));
						if (!getLock(doubtId,1)) {
							JOptionPane.showMessageDialog(null,"Can't modify doubt now");
							break;
						} 
						final Box panel = Box.createVerticalBox();
						panel.setSize(400, 300);
						String dbt = doubts.get(doubtId).getDoubt();
						dbt.replaceAll("\\n","<br>");
						JTextArea text = new JTextArea(dbt);
						text.setSize(399, 200);
						ScrollPane scroll = new ScrollPane();
						scroll.setSize(399, 200);
						scroll.add(text);
						final JButton timeButton = new JButton("Extend");
						final List<Timer> timerList = new ArrayList<Timer>();
						Timer timer = getTimer(timeButton,panel,doubtId);
						timerList.add(timer);
						timeButton.addActionListener(new ActionListener() {
							int count = 2;
							@Override
							public void actionPerformed(ActionEvent e) {
								if (count > 0) {
									timerList.get(0).stop();
									timerList.remove(0);
									timerList.add(getTimer(timeButton,panel,doubtId));
									timerList.get(0).start();
									count--;
									if (count == 0) {
										timeButton.setEnabled(false);
									}
								}	
							}
						});
						panel.add(scroll);
						timerList.get(0).start();
						final JButton okay = new JButton("Confirm");
						okay.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								getOptionPane((JComponent)e.getSource()).setValue(okay);
							}
						});
						final JButton cancel = new JButton("Cancel");
						cancel.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								getOptionPane((JComponent)e.getSource()).setValue(cancel);
							}
						});
						int result = JOptionPane.showOptionDialog(
								null,
								panel,
								"Edit the doubt",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE,
								null,
								new Object[]{timeButton,okay,cancel},
								okay);
						if (result == 1) {
							String newDoubt = text.getText();
							if (newDoubt.equals("") ||
								newDoubt.contains("|") ||
								newDoubt.split("[ \n]").length == 0) {
								JOptionPane.showMessageDialog(null,"Invalid Doubt");
							} else {
								System.out.println("Edited : " + newDoubt);
								editDoubt(doubtId, newDoubt);
								String doubt[] = newDoubt.split("[\n]");
								broadcaster.broadcastMessage("Edit|"
										+ Integer.toString(doubt.length) + "|"
										+ doubt[0] + "|" + Integer.toString(doubtId));
								for (int i = 2; i <= doubt.length; i++) {
									broadcaster.broadcastMessage("Epp|"
											+ Integer.toString(i) + "|"
											+ doubt[i-1] + "|" + Integer.toString(doubtId));
								}
							}
						} else {
							System.out.println("Editing cancelled");
						}
						releaseLock(doubtId);
						break;
					} case "Merge With": {
						if (!getLock(doubtId, 1)) {
							JOptionPane.showMessageDialog(null, "Can't merge this doubt now");
							break;
						}
						selected = doubtId;
						for (int i = 0; i < 6; i++) {
							JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(doubtId).position + i);
							Map attr = comp.getFont().getAttributes();
							attr.put(TextAttribute.BACKGROUND, Color.YELLOW);
							comp.setFont(new Font(attr));
						}
						Server.pane.updateUI();
						mergeTimer.start();
						break;
					} case "Cancel Merge": {
						mergeTimer.stop();
						releaseLock(doubtId);
						selected = -1;
						for (int i = 0; i < 6; i++) {
							JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(doubtId).position + i);
							Map attr = comp.getFont().getAttributes();
							attr.put(TextAttribute.BACKGROUND, Color.WHITE);
							comp.setFont(new Font(attr));
						}
						Server.pane.updateUI();
						break;
					} case "Merge Into": {
						if (!getLock(doubtId,1)) {
							JOptionPane.showMessageDialog(null, "Can't merge into this doubt now");
							break;
						}
						mergeTimer.stop();
						Doubt child = doubts.get(selected);
						Doubt parent = doubts.get(doubtId);
						child.parentId = doubtId;
						parent.childCount = parent.childCount + 1 + child.childCount;
						int newUp = parent.mergeUpvoters(child);
						for (int i = 0; i < 6; i++) {
							JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(selected).position + i);
							Map attr = comp.getFont().getAttributes();
							attr.put(TextAttribute.BACKGROUND, Color.LIGHT_GRAY);
							comp.setFont(new Font(attr));
							for (MouseListener it : comp.getMouseListeners()) {
								comp.removeMouseListener(it);
							}
						}
						JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(selected).position);
						comp.setText(selected + "/" + doubtId);
						JTextArea comp1 = (JTextArea) Server.pane.getComponent(6*doubts.get(doubtId).position + 5);
						comp1.setText(Integer.toString(newUp));
						Server.pane.updateUI();
						broadcaster.broadcastMessage("Merge|" + selected + "|" + doubtId);
						broadcaster.broadcastMessage("Up|-1|" + doubtId + "|" + newUp);
						releaseLock(selected);
						releaseLock(doubtId);
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
	
	private void editDoubt(int doubtId, String dbt) {
		Doubt doubt = doubts.get(doubtId);
		String dbts[] = dbt.split("[\n]");
		for (int i = 1; i <= dbts.length; i++) {
			doubt.setDoubtLine(i, dbts[i-1]);
		}
		doubt.linesReceived = doubt.lines = dbts.length;
		JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubts.get(doubtId).position + 4);
		dbt.replaceAll("\\n", "<br>");
		comp.setText(dbt);
		Server.pane.updateUI();
	}
	
	private void addDoubtToGrid(Doubt doubt) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		MouseAdapter adapter = getAdapter(doubt.DoubtId);
		
		JTextArea label0 = new JTextArea(doubt.DoubtId + (doubt.parentId == -1 ? "" : "/"+doubt.parentId));
		label0.setEditable(false);
		label0.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label0.addMouseListener(adapter);
		label0.setLineWrap(true);
		label0.setWrapStyleWord(true);
		label0.setPreferredSize(new Dimension(90,16*doubt.lines+9));
		gbc.gridx = 0;gbc.gridy = doubt.position;
		Server.pane.add(label0, gbc);
		
		JTextArea label1 = new JTextArea(doubt.name);
		label1.setEditable(false);
		label1.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label1.addMouseListener(adapter);
		label1.setLineWrap(true);
		label1.setWrapStyleWord(true);
		label1.setPreferredSize(new Dimension(140,16*doubt.lines+9));
		gbc.gridx = 1;gbc.gridy = doubt.position;
		Server.pane.add(label1, gbc);
		
		JTextArea label2 = new JTextArea(doubt.rollNo);
		label2.setEditable(false);
		label2.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label2.addMouseListener(adapter);
		label2.setLineWrap(true);
		label2.setWrapStyleWord(true);
		label2.setPreferredSize(new Dimension(140,16*doubt.lines+9));
		gbc.gridx = 2;gbc.gridy = doubt.position;
		Server.pane.add(label2, gbc);
		
		JTextArea label3 = new JTextArea(doubt.time);
		label3.setEditable(false);
		label3.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label3.addMouseListener(adapter);
		label3.setLineWrap(true);
		label3.setWrapStyleWord(true);
		label3.setPreferredSize(new Dimension(130,16*doubt.lines+9));
		gbc.gridx = 3;gbc.gridy = doubt.position;
		Server.pane.add(label3, gbc);
		
		String dbt = doubt.getDoubt();
		dbt.replaceAll("\\n","<br>");
		JTextArea label4 = new JTextArea(dbt);
		label4.setEditable(false);
		label4.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label4.addMouseListener(adapter);
		label4.setLineWrap(true);
		label4.setWrapStyleWord(true);
		label4.setPreferredSize(new Dimension(400,16*doubt.lines+9));
		gbc.gridx = 4;gbc.gridy = doubt.position;
		Server.pane.add(label4, gbc);
		
		JTextArea label5 = new JTextArea(Integer.toString(doubt.getUpVotesCount()));
		label5.setEditable(false);
		label5.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label5.addMouseListener(adapter);
		label5.setLineWrap(true);
		label5.setWrapStyleWord(true);
		label5.setPreferredSize(new Dimension(90,16*doubt.lines+9));
		gbc.gridx = 5;gbc.gridy = doubt.position;
		Server.pane.add(label5, gbc);
		Server.pane.updateUI();
		System.out.println("Row Added");
		fixSort();
	}
	
	private class comp implements Comparator<Integer> {
		@Override
		public int compare(Integer a, Integer b) {
			Doubt lhs = doubts.get(a),rhs = doubts.get(b);
			if (lhs.DoubtId == rhs.DoubtId) return 0;
			else {
				switch(currentSort) {
					case 0:
						return lhs.DoubtId > rhs.DoubtId ? 1 : -1;
					case 1:
						return lhs.DoubtId < rhs.DoubtId ? 1 : -1;
					case 2:
						if (lhs.getUpVotesCount() == rhs.getUpVotesCount()) {
							return lhs.DoubtId > rhs.DoubtId ? 1 : -1;
						} else {
							return lhs.getUpVotesCount() > rhs.getUpVotesCount() ? -1 : 1;
						}
					case 3:
						if (lhs.getUpVotesCount() == rhs.getUpVotesCount()) {
							return lhs.DoubtId > rhs.DoubtId ? 1 : -1;
						} else {
							return lhs.getUpVotesCount() > rhs.getUpVotesCount() ? 1 : -1;
						}
				}
			}
			return 0;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void fixSort() {
		System.out.println("Sorting");
		Collections.sort(positionIdMap, new comp());
		for (int i = 0; i < positionIdMap.size(); i++) {
			int doubtId = positionIdMap.get(i);
			Doubt doubt = doubts.get(doubtId);
			doubt.position = i;
			JTextArea c0,c1,c2,c3,c4,c5;
			
			c0 = (JTextArea) Server.pane.getComponent(6*i);
			c0.setText(doubt.DoubtId + (doubt.parentId == -1 ? "" : "/"+doubt.parentId));
			c1 = (JTextArea) Server.pane.getComponent(6*i+1);
			c1.setText(doubt.name);
			c2 = (JTextArea) Server.pane.getComponent(6*i+2);
			c2.setText(doubt.rollNo);
			c3 = (JTextArea) Server.pane.getComponent(6*i+3);
			c3.setText(doubt.time);
			c4 = (JTextArea) Server.pane.getComponent(6*i+4);
			String dbt = doubt.getDoubt();
			dbt.replaceAll("\\n", "<br>");
			c4.setText(dbt);
			c5 = (JTextArea) Server.pane.getComponent(6*i+5);
			c5.setText(Integer.toString(doubt.getUpVotesCount()));
			
			for (int j = 0; j < 6; j++) {
				JTextArea c = (JTextArea) Server.pane.getComponent(6*i+j);
				GridBagConstraints gbc = Server.layout.getConstraints(c);
				gbc.weighty = (i == positionIdMap.size() - 1 ? 1 : 0);
				Map attr = c.getFont().getAttributes();
				for (MouseListener it : c.getMouseListeners()) {
					c.removeMouseListener(it);
				}
				if (!doubt.deleted && doubt.parentId == -1)
					c.addMouseListener(getAdapter(doubtId));
				if (doubt.deleted) {
					attr.put(TextAttribute.STRIKETHROUGH, true);
				} else {
					attr.put(TextAttribute.STRIKETHROUGH, false);
				}
				if (doubtId == selected) {
					attr.put(TextAttribute.BACKGROUND, Color.YELLOW);
				} else if (doubt.parentId != -1) {
					attr.put(TextAttribute.BACKGROUND, Color.LIGHT_GRAY);
				} else {
					attr.put(TextAttribute.BACKGROUND, Color.WHITE);
				}
				c.setFont(new Font(attr));
				Server.pane.remove(6*i+j);
				Server.pane.add(c, gbc, 6*i+j);
			}
		}
		Server.pane.updateUI();
	}
}
