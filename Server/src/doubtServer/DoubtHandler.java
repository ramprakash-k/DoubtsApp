package doubtServer;

import java.awt.Color;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class DoubtHandler{
	Map<Integer, Doubt> doubts;
	Map<Integer, Integer> locks; // 0 - free; 1 - server; 2 - client;
	int totalCount;
	Broadcaster broadcaster;
	int selected;
	
	DoubtHandler(Broadcaster caster) {
		doubts = new HashMap<Integer, Doubt>();
		locks = new HashMap<Integer, Integer>();
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
	
	public void editDoubt(int doubtId, String dbt) {
		Doubt doubt = doubts.get(doubtId);
		String dbts[] = dbt.split("[\n]");
		for (int i = 1; i <= dbts.length; i++) {
			doubt.setDoubtLine(i, dbts[i-1]);
		}
		doubt.linesReceived = doubt.lines = dbts.length;
		JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubtId + 4);
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
		JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubtId + 5);
		comp.setText(Integer.toString(nc));
		Server.pane.updateUI();
		return nc;
	}
	
	public int nupVoteDoubt(int doubtId, String roll) {
		int nc = doubts.get(doubtId).nupVote(roll);
		JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubtId + 5);
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
					Integer.toString(doubt.getUpVotesCount()));
			}
		}
		return instr;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void deleteDoubt(int doubtId) {
		doubts.remove(doubtId);
		for (int i = 0; i < 6; i++) {
			JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubtId + i);
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
						JTextArea comp = (JTextArea) Server.pane.getComponent(6*selected + i);
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
								JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubtId + i);
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
							JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubtId + i);
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
							JTextArea comp = (JTextArea) Server.pane.getComponent(6*doubtId + i);
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
							JTextArea comp = (JTextArea) Server.pane.getComponent(6*selected + i);
							Map attr = comp.getFont().getAttributes();
							attr.put(TextAttribute.BACKGROUND, Color.LIGHT_GRAY);
							comp.setFont(new Font(attr));
							for (MouseListener it : comp.getMouseListeners()) {
								comp.removeMouseListener(it);
							}
						}
						JTextArea comp = (JTextArea) Server.pane.getComponent(6*selected);
						comp.setText(selected + "/" + doubtId);
						JTextArea comp1 = (JTextArea) Server.pane.getComponent(6*doubtId + 5);
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
	
	private void addDoubtToGrid(Doubt doubt) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		MouseAdapter adapter = getAdapter(doubt.DoubtId);
		
		JTextArea label0 = new JTextArea(doubt.DoubtId + (doubt.parentId == -1 ? "" : "/"+doubt.parentId));
		label0.setEditable(false);
		label0.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label0.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 0;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label0, gbc);
		
		JTextArea label1 = new JTextArea(doubt.name);
		label1.setEditable(false);
		label1.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label1.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 1;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label1, gbc);
		
		JTextArea label2 = new JTextArea(doubt.rollNo);
		label2.setEditable(false);
		label2.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label2.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 2;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label2, gbc);
		
		JTextArea label3 = new JTextArea(doubt.time);
		label3.setEditable(false);
		label3.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label3.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 3;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label3, gbc);
		
		String dbt = doubt.getDoubt();
		dbt.replaceAll("\\n","<br>");
		JTextArea label4 = new JTextArea(dbt);
		label4.setEditable(false);
		label4.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label4.addMouseListener(adapter);
		gbc.weightx = 5;gbc.gridx = 4;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label4, gbc);
		
		JTextArea label5 = new JTextArea(Integer.toString(doubt.getUpVotesCount()));
		label5.setEditable(false);
		label5.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		label5.addMouseListener(adapter);
		gbc.weightx = 1;gbc.gridx = 5;gbc.gridy = doubt.DoubtId;
		Server.pane.add(label5, gbc);
		Server.pane.updateUI();
		System.out.println("Row Added");
	}
}
