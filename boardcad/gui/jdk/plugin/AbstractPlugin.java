package boardcad.gui.jdk.plugin;

import javax.swing.JComponent;
import javax.swing.JMenu;

import board.AbstractBoard;

abstract public interface AbstractPlugin {
	
	abstract public AbstractBoard getCurrentBoard();
	
	abstract public JMenu getMenu();
	abstract public JComponent getComponent();

}
