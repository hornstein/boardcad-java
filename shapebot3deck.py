"""
G-code generation for the deck
"""

import boardcad.gui.jdk.BoardCAD
import boardcam.cutters.FlatCutter
import boardcam.cutters.STLCutter
import boardcam.BoardMachine
import cadcore.NurbsPoint
from javax.swing import *
import math

# Read machine configuration from shapebot.properties

machine=boardcam.BoardMachine()
machine.read_machine_data()

width_steps=machine.deckCuts
length_steps=50

filename=machine.deckFileName
zmax=machine.zMaxHeight

feedrate=machine.speed
feedrate_stringer=machine.stringerSpeed
feedrate_outline=machine.outlineSpeed

supportEnd=machine.endSupportPosition
supportEndX=supportEnd[0]
supportEndY=supportEnd[1]
supportEndZ=supportEnd[2]

cut_stringer = machine.cutStringer
stringer_offset = machine.stringerOffset

stringerOffsetZ=[10, 5, 0]
stringerCutoff=machine.stringerCutoff
outlineOffsetZ=[]
outlineOffsetY=machine.outlineOffset


# Create a cutter

cutter=boardcam.cutters.FlatCutter()
cutter.setRadius(10.0)
#cutter=boardcam.cutters.STLCutter()
#cutter.init(machine.toolName)
#cutter.scale(1.0,1.0,1.0)

# Get the deck surface from BoardCAD

boardhandler=boardcad.gui.jdk.BoardCAD.getInstance().getBoardHandler()
board=boardhandler.getActiveBoard();
deck=board.getDeck()

# find parameter value for outline

max_width = 0.0
t=deck.getMinT()

p = deck.getPoint((deck.getMaxS()-deck.getMinS())/2, t)

while (max_width - math.fabs(p.z) <= 0):
	t=t+0.01
	p = deck.getPoint((deck.getMaxS()-deck.getMinS())/2, t)	
	if (math.fabs(p.z) > max_width):
		max_width = math.fabs(p.z)

t_outline=t


# Calculate step length

width_step=((deck.getMaxT()-deck.getMinT())/2-t_outline-0.01)/width_steps
length_step=(deck.getMaxS()-deck.getMinS())/length_steps


# calculate tool path stringer
stringer_cut=[]

s=deck.getMaxS()
t=(deck.getMaxT()-deck.getMinT())/2
p=deck.getPoint(s,t)
p=cutter.calcOffset(p,cadcore.NurbsPoint(1,0,0))
stringer_cut.append(p)
while (s>deck.getMinS()):
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	p=cutter.calcOffset(p,n)
	stringer_cut.append(p)
	s=s-length_step

s=deck.getMinS()
p=deck.getPoint(s,t)	
p=cutter.calcOffset(p,cadcore.NurbsPoint(-1,0,0))
stringer_cut.append(p)

# calculate tool path outline

outline_cut=[]

t=t_outline
s=deck.getMinS()
while (s<deck.getMaxS()):
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	p=cutter.calcOffset(p,n)
	outline_cut.append(p)
	s=s+length_step

# calculate tool path deck

deck_cut=[]

s=deck.getMinS();
t=(deck.getMaxT()-deck.getMinT())/2-00.1

while (t>t_outline):
	while (s<deck.getMaxS()):
		p=deck.getPoint(s,t) 
		n=deck.getNormal(s,t)
		p=cutter.calcOffset(p,n)
		deck_cut.append(p)
		s=s+length_step
	t=t-width_step
	while (s>deck.getMinS()):
		p=deck.getPoint(s,t)
		n=deck.getNormal(s,t)
		p=cutter.calcOffset(p,n)		
		deck_cut.append(p)
		s=s-length_step
	t=t-width_step


# Avoid stringer collision

if(cut_stringer==0):
	stringerOffsetZ=[]
	for p in outline_cut:
		if(p.z<stringer_offset):
			p.z=stringer_offset
	for p in deck_cut:
		if(p.z<stringer_offset):
			p.z=stringer_offset
	

# Write g-code

toolpath=[]

f=open(filename, 'w')

zsafe='g0 z%d\n' %(zmax)

f.write('(cutting stringer)\n')
p=stringer_cut[0]
for offsetZ in stringerOffsetZ:
	f.write(zsafe)
	toolpath.append(cadcore.NurbsPoint(p.x,zmax-supportEndZ,0.0))
	p=stringer_cut[0]
	f.write('g0 x%.3f y%.3f\n' % (p.x+supportEndX, supportEndY))
	toolpath.append(cadcore.NurbsPoint(p.x,zmax-supportEndZ,0.0))
	f.write('g1 x%.3f z%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndZ+offsetZ-stringerCutoff, feedrate_stringer))
	toolpath.append(cadcore.NurbsPoint(p.x,p.y+offsetZ-stringerCutoff,0.0))
	for p in stringer_cut:
		f.write('g1 x%.3f z%.3f\n' % (p.x+supportEndX, p.y+supportEndZ+offsetZ))
		toolpath.append(cadcore.NurbsPoint(p.x,p.y+offsetZ,0.0))
	f.write('g1 x%.3f z%.3f\n' % (p.x+supportEndX, p.y+supportEndZ+offsetZ-stringerCutoff))
	toolpath.append(cadcore.NurbsPoint(p.x,p.y+offsetZ-stringerCutoff,0.0))

for offsetZ in outlineOffsetZ:
	f.write('(cutting outline right)\n')
	f.write(zsafe)
	toolpath.append(cadcore.NurbsPoint(p.x,zmax-supportEndZ,0.0))
	p=outline_cut[0]
	f.write('g0 x%.3f y%.3f\n' % (p.x+supportEndX, -p.z+supportEndY-outlineOffsetY-200))
	toolpath.append(cadcore.NurbsPoint(p.x,zmax-supportEndZ,-p.z-outlineOffsetY-200))
	f.write('g1 x%.3f y%.3f z%.3f f%d\n' % (p.x+supportEndX, -p.z+supportEndY-outlineOffsetY-200, p.y+supportEndZ+offsetZ, feedrate_outline))
	toolpath.append(cadcore.NurbsPoint(p.x,p.y+offsetZ,-p.z-outlineOffsetY-200))
	mycount=0		
	for p in outline_cut:
		mycount=mycount+1
		f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, -p.z+supportEndY-outlineOffsetY, p.y+supportEndZ+offsetZ))
		toolpath.append(cadcore.NurbsPoint(p.x,p.y+offsetZ,-p.z-outlineOffsetY))
	f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, -p.z+supportEndY-outlineOffsetY-200, p.y+supportEndZ+offsetZ))
	toolpath.append(cadcore.NurbsPoint(p.x,p.y+offsetZ,-p.z-outlineOffsetY-200))
	f.write('(cutting outline left)\n')
	f.write(zsafe)
	toolpath.append(cadcore.NurbsPoint(p.x,zmax-supportEndZ,-p.z-outlineOffsetY-200))
	p=outline_cut[mycount-1]
	f.write('g0 x%.3f y%.3f\n' % (p.x+supportEndX, p.z+supportEndY+outlineOffsetY+200))
	toolpath.append(cadcore.NurbsPoint(p.x,zmax-supportEndZ, p.z-outlineOffsetY+200))
	f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, p.z+supportEndY+outlineOffsetY+200, p.y+supportEndZ+offsetZ))
	toolpath.append(cadcore.NurbsPoint(p.x, p.y+offsetZ, p.z-outlineOffsetY+200))
	for i in range(mycount):
		p=outline_cut[mycount-i-1]
		f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, p.z+supportEndY+outlineOffsetY, p.y+supportEndZ+offsetZ))
		toolpath.append(cadcore.NurbsPoint(p.x, p.y+offsetZ,p.z-outlineOffsetY))
	f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, p.z+supportEndY+outlineOffsetY+200, p.y+supportEndZ+offsetZ))
	toolpath.append(cadcore.NurbsPoint(p.x, p.y+offsetZ, p.z-outlineOffsetY+200))
	toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, p.z-outlineOffsetY+200))


f.write(zsafe)
f.write('(cutting deck right)\n');
p=deck_cut[0]
f.write('g0 x%.3f y%.3f\n' % (p.x+supportEndX, -p.z+supportEndY))
toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, p.z))
f.write('g1 x%.3f y%.3f z%.3f f%d\n' % (p.x+supportEndX, -p.z+supportEndY, p.y+supportEndZ, feedrate))
toolpath.append(cadcore.NurbsPoint(p.x, p.y, -p.z))
for p in deck_cut:
	f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, -p.z+supportEndY, p.y+supportEndZ))
	toolpath.append(cadcore.NurbsPoint(p.x, p.y, -p.z))
f.write(zsafe)
toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, -p.z))
f.write('(cutting deck left)\n');
p=deck_cut[0]
f.write('g0 x%.3f y%.3f\n' % (p.x+supportEndX, p.z+supportEndY))
toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, p.z))
f.write('g1 x%.3f y%.3f z%.3f f%d\n' % (p.x+supportEndX, p.z+supportEndY, p.y+supportEndZ, feedrate))
toolpath.append(cadcore.NurbsPoint(p.x, p.y, p.z))
for p in deck_cut:
	f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, p.z+supportEndY, p.y+supportEndZ))
	toolpath.append(cadcore.NurbsPoint(p.x, p.y, p.z))


f.write(zsafe)
toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, p.z))

f.write("M2");
f.close()

# Visualize toolpath in BoardCAD

i=0
for p in toolpath:
	machine.deck_cut[i]=p;
	i=i+1

machine.nr_of_cuts_deck=i
	


JOptionPane.showMessageDialog(None, "Finished g-code generation")


