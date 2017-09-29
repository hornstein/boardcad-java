import boardcad.gui.jdk.BoardCAD

import boardcam.BoardMachine


from javax.swing import JScrollPane

from java.awt import * 


from javax.swing import * 

from java.io import *

import java.awt.event.KeyEvent as KeyEvent


import console



def gcodeDeck(event):
	
	fc = JFileChooser()
	fc.setCurrentDirectory(File(boardcad.gui.jdk.BoardCAD.getInstance().defaultDirectory))
	returnVal = fc.showSaveDialog(boardcad.gui.jdk.BoardCAD.getInstance().getFrame())
	if (returnVal != JFileChooser.APPROVE_OPTION):
		return
	mfile = fc.getSelectedFile()
	filename = mfile.getPath()
	if(filename == None):
		return
	boardcad.gui.jdk.BoardCAD.getInstance().defaultDirectory = mfile.getPath()
	
	boardcam.BoardMachine.deckFileName=filename
	
	boardcam.BoardMachine.read_machine_data()
	
	filename=boardcam.BoardMachine.deckScript
	
	myconsole.insertText("execfile('" + filename + "')")
	
	myconsole.enter()
	
	board_draw=boardcad.gui.jdk.BoardCAD.getInstance().getBoardHandler().board_draw
	
	board_draw.deck_cut=boardcam.BoardMachine.deck_cut;
	board_draw.nr_of_cuts_deck=boardcam.BoardMachine.nr_of_cuts_deck;
	board_draw.deck_collision=boardcam.BoardMachine.deck_collision



def gcodeBottom(event):
	
	fc = JFileChooser()
	fc.setCurrentDirectory(File(boardcad.gui.jdk.BoardCAD.getInstance().defaultDirectory))
	returnVal = fc.showSaveDialog(boardcad.gui.jdk.BoardCAD.getInstance().getFrame())
	if (returnVal != JFileChooser.APPROVE_OPTION):
		return
	mfile = fc.getSelectedFile()
	filename = mfile.getPath()
	if(filename == None):
		return
	boardcad.gui.jdk.BoardCAD.getInstance().defaultDirectory = mfile.getPath()
	
	boardcam.BoardMachine.bottomFileName=filename
	
	boardcam.BoardMachine.read_machine_data()
	
	filename=boardcam.BoardMachine.bottomScript
	
	myconsole.insertText("execfile('" + filename + "')")
	
	myconsole.enter()
	
	board_draw=boardcad.gui.jdk.BoardCAD.getInstance().getBoardHandler().board_draw
	
	board_draw.bottom_cut=boardcam.BoardMachine.bottom_cut;
	board_draw.nr_of_cuts_bottom=boardcam.BoardMachine.nr_of_cuts_bottom;
	board_draw.bottom_collision=boardcam.BoardMachine.bottom_collision



def runScript(event):
	fc = JFileChooser()
	fc.setCurrentDirectory(File(boardcad.gui.jdk.BoardCAD.getInstance().defaultDirectory))
	returnVal = fc.showOpenDialog(boardcad.gui.jdk.BoardCAD.getInstance().getFrame())
	if (returnVal != JFileChooser.APPROVE_OPTION):
		return
	mfile = fc.getSelectedFile()
	filename = mfile.getPath()
	
	myconsole.insertText("execfile('" + filename + "')")
	
	myconsole.enter()
	
	#execfile(filename)




frame=boardcad.gui.jdk.BoardCAD.getInstance().getFrame()

mytab=boardcad.gui.jdk.BoardCAD.getInstance().mTabbedPane2

myconsole = console.Console()

mytab.addTab("Jython console", JScrollPane(myconsole.text_pane))


mbar=frame.getJMenuBar()


jythonmenu=boardcad.gui.jdk.BoardCAD.getInstance().scriptMenu

#jythonmenu=JMenu('Scripts')

#jythonmenu.setMnemonic(KeyEvent.VK_I)


mitem=JMenuItem('G-code deck', actionPerformed=gcodeDeck)

jythonmenu.add(mitem)

mitem=JMenuItem('G-code bottom', actionPerformed=gcodeBottom)

jythonmenu.add(mitem)

jythonmenu.addSeparator()


mitem=JMenuItem('Run script', actionPerformed=runScript)

jythonmenu.add(mitem)

mbar.revalidate()

#mbar.add(jythonmenu)

#frame.setJMenuBar(mbar)



 




