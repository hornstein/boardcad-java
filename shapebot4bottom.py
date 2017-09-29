"""
G-code generation for the bottom
"""

import boardcad.gui.jdk.BoardCAD
import boardcam.cutters.FlatCutter
import boardcam.BoardMachine
import cadcore.NurbsPoint
from javax.swing import *
import math

# Read machine configuration from shapebot.properties

machine=boardcam.BoardMachine()

width_steps=machine.bottomCuts
rail_steps=machine.bottomRailCuts
length_steps=50

filename=machine.bottomFileName
zmax=machine.zMaxHeight

feedrate=machine.speed
feedrate_stringer=machine.stringerSpeed

supportEnd=machine.endSupportPosition
supportEndX=supportEnd[0]
supportEndY=supportEnd[1]
supportEndZ=supportEnd[2]

cut_stringer = machine.cutStringer
stringer_offset = machine.stringerOffset

stringerOffsetZ=[10, 5, 0]
stringerCutoff=machine.stringerCutoff

offsetRotation=machine.axis4_offsetRotation

# Create a cutter

#cutter=boardcam.cutters.FlatCutter()
#cutter.setRadius(10.0)
cutter=boardcam.cutters.STLCutter()
cutter.init(machine.toolName)
cutter.scale(1.0,1.0,1.0)

# Get the bottom and deck surface from BoardCAD

boardhandler=boardcad.gui.jdk.BoardCAD.getInstance().getBoardHandler()
board=boardhandler.getActiveBoard();
deck=board.getDeck()
bottom=board.getBottom()

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

length_step=(bottom.getMaxS()-bottom.getMinS())/length_steps
width_step=((bottom.getMaxT()-bottom.getMinT())/2)/width_steps
if rail_steps>0:
	rail_step=(t_outline-deck.getMinT())/rail_steps




#Find uniform distances for t and their angles

wval=[]
tval=[]

t=bottom.getMinT()
n=deck.getNormal((bottom.getMaxS()-bottom.getMinS())/2, t)
p=cutter.calcOffset(p,n)
max_width = math.fabs(p.z)+.1
i=math.fabs(p.z)
step=math.fabs(p.z)/width_steps

while i>0.001:
	wval.append(p.z+i)
	i=i-step

print wval
print '\nmax width = %0.3f\n' % max_width

for w in wval:
	while (max_width - math.fabs(p.z) <= math.fabs(w)):
		t=t+0.01
		p=bottom.getPoint((deck.getMaxS()-deck.getMinS())/2, t)
		n=bottom.getNormal((deck.getMaxS()-deck.getMinS())/2, t)
		p=cutter.calcOffset(p,n)
	tval.append(t)

print tval




# calculate tool path stringer
stringer_cut=[]

s=bottom.getMinS()
t=(bottom.getMaxT()-bottom.getMinT())/2
while (s<bottom.getMaxS()):
	p=bottom.getPoint(s,t)
	n=bottom.getNormal(s,t)
	p=cutter.calcOffset(p,n)
	stringer_cut.append(p)
	s=s+length_step


# calculate tool path bottom

bottom_cut=[]

s=bottom.getMinS();
t=(bottom.getMaxT()-bottom.getMinT())/2

while tval:
	t=tval.pop()
	while (s<bottom.getMaxS()):
		p=bottom.getPoint(s,t) 
		n=bottom.getNormal(s,t)
		p=cutter.calcOffset(p,n)
		bottom_cut.append(p)
		s=s+length_step
	t=tval.pop()
	while (s>bottom.getMinS()):
		p=bottom.getPoint(s,t)
		n=bottom.getNormal(s,t)
		p=cutter.calcOffset(p,n)		
		bottom_cut.append(p)
		s=s-length_step


# calculate tool path rail

deck_cut=[]

if(rail_steps>0):
	s=deck.getMinS();
	t=deck.getMinT()
	while (t<t_outline):
		while (s<deck.getMaxS()):
			p=deck.getPoint(s,t) 
			n=deck.getNormal(s,t)
			p=cutter.calcOffset(p,n)
			deck_cut.append(p)
			s=s+length_step
		t=t+rail_step
		while (s>deck.getMinS()):
			p=deck.getPoint(s,t)
			n=deck.getNormal(s,t)
			p=cutter.calcOffset(p,n)		
			deck_cut.append(p)
			s=s-length_step
		t=t+rail_step
else:
	s=deck.getMinS();
	t=deck.getMinT()
	while (s<deck.getMaxS()):
		p=deck.getPoint(s,t) 
		n=deck.getNormal(s,t)
		p=cutter.calcOffset(p,n)
		deck_cut.append(p)
		s=s+length_step
	while (s>deck.getMinS()):
		p=deck.getPoint(s,t)
		n=deck.getNormal(s,t)
		p=cutter.calcOffset(p,n)
		p.z=p.z-10		
		deck_cut.append(p)
		s=s-length_step



# Avoid stringer collision

if(cut_stringer==0):
	stringerOffsetZ=[]
	for p in bottom_cut:
		if(p.z<stringer_offset):
			p.z=stringer_offset
	for p in deck_cut:
		if(p.z<stringer_offset):
			p.z=stringer_offset
	

# Write g-code

toolpath=[]

f=open(filename, 'w')

zsafe='g0 z%d a%.3f\n' %(zmax, offsetRotation)

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


f.write(zsafe)
toolpath.append(cadcore.NurbsPoint(p.x,zmax-supportEndZ,0.0))
f.write('(cutting bottom right)\n');
p=bottom_cut[0]
f.write('g0 x%.3f y%.3f\n' % (p.x+supportEndX, -p.z+supportEndY))
toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, p.z))
f.write('g1 x%.3f y%.3f z%.3f f%d\n' % (p.x+supportEndX, -p.z+supportEndY, p.y+supportEndZ, feedrate))
toolpath.append(cadcore.NurbsPoint(p.x, p.y, -p.z))
for p in bottom_cut:
	f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, -p.z+supportEndY, p.y+supportEndZ))
	toolpath.append(cadcore.NurbsPoint(p.x, p.y, -p.z))


f.write(zsafe)
toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, -p.z))
f.write('(cutting bottom left)\n');
p=bottom_cut[0]
f.write('g0 x%.3f y%.3f\n' % (p.x+supportEndX, p.z+supportEndY))
toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, -p.z))
f.write('g1 x%.3f y%.3f z%.3f f%d\n' % (p.x+supportEndX, p.z+supportEndY, p.y+supportEndZ, feedrate))
toolpath.append(cadcore.NurbsPoint(p.x, p.y, p.z))
for p in bottom_cut:
	f.write('g1 x%.3f y%.3f z%.3f\n' % (p.x+supportEndX, p.z+supportEndY, p.y+supportEndZ))
	toolpath.append(cadcore.NurbsPoint(p.x, p.y, p.z))


f.write(zsafe)
toolpath.append(cadcore.NurbsPoint(p.x, zmax-supportEndZ, p.z))
f.write('(cutting rail right)\n');
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
f.write('(cutting rail left)\n');
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
	machine.bottom_cut[i]=p;
	i=i+1

machine.nr_of_cuts_bottom=i
	


JOptionPane.showMessageDialog(None, "Finished g-code generation")


