import boardcad.gui.jdk.BoardCAD
import boardcam.cutters.STLCutter
import boardcam.BoardMachine
import cadcore.NurbsPoint
from javax.swing import *
import math

###########################################################################
# Generate the g-code as a parallel task

machine=boardcam.BoardMachine()

machine.read_machine_data()
width_steps=machine.deckCuts
length_steps=50

filename=machine.deckFileName
zmax=machine.zMaxHeight

feedrate=machine.speed
feedrate_stringer=machine.stringerSpeed
feedrate_outline=machine.outlineSpeed

axis4_rail_start=machine.deckRailAngle
axis4_rail_stop=machine.bottomRailAngle
deck_cuts=machine.bottomCuts
rail_cuts=machine.bottomRailCuts

supportEnd=machine.endSupportPosition
supportEndX=supportEnd[0]
supportEndY=supportEnd[1]
supportEndZ=supportEnd[2]


cutter=boardcam.cutters.STLCutter()
cutter_name=machine.toolName
cutter.init(cutter_name)
axis4_offset_z=machine.axis4_offset.z	
offsetRotation=machine.axis4_offsetRotation
cutter.setRotationCenter(0.0, 0.0, axis4_offset_z);
cutter.scale(machine.toolScaleX,machine.toolScaleY,machine.toolScaleZ);


stringerOffsetZ=[10, 5, 0]
stringerCutoff=machine.stringerCutoff

outlineOffsetZ=[20, 0, -30]
outlineOffsetY=machine.outlineOffset

# get a copy of the deck surface from BoardCAD

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

# find parameter value for rail

t=deck.getMinT()
angle=180

while (angle>axis4_rail_start):
	t=t+0.01
	p = deck.getPoint((deck.getMaxS()-deck.getMinS())/2, t)
	n=deck.getNormal((deck.getMaxS()-deck.getMinS())/2, t)
	angle=math.fabs(180.0/math.pi*math.atan2(n.z, n.y))

t_rail=t	

#Find uniform distances for t and their angles

wval=[]
tval=[]
aval=[]

t=t_rail
n=deck.getNormal((deck.getMaxS()-deck.getMinS())/2, t)
p=cutter.calcOffset(p,n)
max_width = math.fabs(p.z)+.1
i=p.z
step=p.z/deck_cuts

while i>0.001:
	wval.append(p.z-i)
	i=i-step

print wval
print '\nmax width = %0.3f\n' % max_width

for w in wval:
	while (max_width - math.fabs(p.z) <= math.fabs(w)):
		t=t+0.01
		p=deck.getPoint((deck.getMaxS()-deck.getMinS())/2, t)	
	n=deck.getNormal((deck.getMaxS()-deck.getMinS())/2, t)
	angle=math.fabs(180.0/math.pi*math.atan2(n.z, n.y))
	aval.append(angle)
	tval.append(t)

print tval, aval

# add rail angles

rval=[]
for i in range(rail_cuts):
	rval.append(axis4_rail_stop-i*1.0/(rail_cuts-1)*(axis4_rail_stop-axis4_rail_start))
	
print rval

# Setup progress bar info...

# calculate tool path stringer
stringer_cut=[]
step=0.25

t=(deck.getMaxT()-deck.getMinT())/2
s=deck.getMaxS()
cutter.setRotation(0.0)
p=deck.getPoint(s,t)
#n=deck.getNormal(t,s)
p=cutter.calcOffset(cadcore.NurbsPoint(p.x,p.y,p.z),cadcore.NurbsPoint(1,0,0))
stringer_cut.append(cadcore.NurbsPoint(p.x,-p.z,p.y, 0.0))
while (s>deck.getMinS()):
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	p=cutter.calcOffset(cadcore.NurbsPoint(p.x,p.y,p.z),cadcore.NurbsPoint(n.x,n.y,n.z))
	stringer_cut.append(cadcore.NurbsPoint(p.x,-p.z,p.y, 0.0))
	s=s-step

s=deck.getMinS()
p=deck.getPoint(s,t)	
p=cutter.calcOffset(cadcore.NurbsPoint(p.x,p.y,p.z),cadcore.NurbsPoint(-1,0,0))
stringer_cut.append(cadcore.NurbsPoint(p.x,-p.z,p.y, 0.0))

# calculate tool path outline

outline_cut=[]
step=0.25

t=t_outline
s=deck.getMinS()
cutter.setRotation(0.0)
while (s<deck.getMaxS()):
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	p=cutter.calcOffset(cadcore.NurbsPoint(p.x,p.y,p.z),cadcore.NurbsPoint(n.x,n.y,n.z))
	outline_cut.append(cadcore.NurbsPoint(p.x,-p.z,p.y, 0.0))
	s=s+step

# calculate tool path deck

deck_right=[]
deck_left=[]
step=0.25
s=deck.getMinS()

while aval:
	a=aval.pop()
	print a
	cutter.setRotation(a)
	while (s<deck.getMaxS()):
		t=t_rail
		angle=180
		while (angle>a):
			t=t+0.01
			p=deck.getPoint(s,t)
			n=deck.getNormal(s,t)
			angle=math.fabs(180.0/math.pi*math.atan2(n.z, n.y))
		cutter.setRotation(angle)
		p=cutter.calcOffset(cadcore.NurbsPoint(p.x,p.y,p.z),cadcore.NurbsPoint(n.x,n.y,n.z))
		deck_right.append(cadcore.NurbsPoint(p.x,-p.z,p.y, angle))
		deck_left.append(cadcore.NurbsPoint(p.x,p.z,p.y, -angle))
		s=s+step	
	a=aval.pop()
	print a	
	cutter.setRotation(a)
	while (s>deck.getMinS()):
		t=t_rail
		angle=180
		while (angle>a):
			t=t+0.01
			p=deck.getPoint(s,t)
			n=deck.getNormal(s,t)
			angle=math.fabs(180.0/math.pi*math.atan2(n.z, n.y))
		cutter.setRotation(angle)
		p=cutter.calcOffset(cadcore.NurbsPoint(p.x,p.y,p.z),cadcore.NurbsPoint(n.x,n.y,n.z))
		deck_right.append(cadcore.NurbsPoint(p.x,-p.z,p.y, angle))
		deck_left.append(cadcore.NurbsPoint(p.x,p.z,p.y, -angle))
		s=s-step

# calculate tool path rail

rail_right=[]
rail_left=[]
step=0.25
s=deck.getMinS()
cutter.setRotationCenter(0.0, 0.0, axis4_offset_z-20)

while rval:
	a=rval.pop()
	print a
	cutter.setRotation(a-90)
	while (s<deck.getMaxS()):
		t=deck.getMinT()
		angle=180
		while (angle>a):
			t=t+0.01
			p=deck.getPoint(s,t)
			n=deck.getNormal(s,t)
			angle=math.fabs(180.0/math.pi*math.atan2(n.z, n.y))
		cutter.setRotation(angle-90)
		p=cutter.calcOffset(cadcore.NurbsPoint(p.x,p.y,p.z),cadcore.NurbsPoint(n.x,n.y,n.z))
		rail_right.append(cadcore.NurbsPoint(p.x,-p.z,p.y-20, angle-90))
		rail_left.append(cadcore.NurbsPoint(p.x,p.z,p.y-20, -angle+90))
		s=s+step	
	a=rval.pop()
	print a	
	cutter.setRotation(a-90)
	while (s>deck.getMinS()):
		t=deck.getMinT()
		angle=180
		while (angle>a):
			t=t+0.01
			p=deck.getPoint(s,t)
			n=deck.getNormal(s,t)
			angle=math.fabs(180.0/math.pi*math.atan2(n.z, n.y))
		cutter.setRotation(angle-90)
		p=cutter.calcOffset(cadcore.NurbsPoint(p.x,p.y,p.z),cadcore.NurbsPoint(n.x,n.y,n.z))
		rail_right.append(cadcore.NurbsPoint(p.x,-p.z,p.y-20, angle-90))
		rail_left.append(cadcore.NurbsPoint(p.x,p.z,p.y-20, -angle+90))
		s=s-step

# write g-code
f=open(filename, 'w')
zsafe='g0 z%d\n' %(zmax)
f.write('(cutting stringer)\n')
for offsetZ in stringerOffsetZ:
	f.write(zsafe)
	p=stringer_cut[0]
	f.write('g0 x%.3f y%.3f a%.3f\n' % (p.x+supportEndX, p.y+supportEndY, p.w+offsetRotation))
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY, p.z+supportEndZ+offsetZ-stringerCutoff, p.w+offsetRotation, feedrate_stringer))
	for p in stringer_cut:
		f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY, p.z+supportEndZ+offsetZ, p.w+offsetRotation, feedrate_stringer))
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY, p.z+supportEndZ+offsetZ-stringerCutoff, p.w+offsetRotation, feedrate_stringer))

for offsetZ in outlineOffsetZ:
	f.write('(cutting outline right)\n')
	f.write(zsafe)
	p=outline_cut[0]
	f.write('g0 x%.3f y%.3f a%.3f\n' % (p.x+supportEndX, p.y+supportEndY-outlineOffsetY-200, p.w+offsetRotation))
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY-outlineOffsetY-200, p.z+supportEndZ+offsetZ, p.w+offsetRotation, feedrate))
	mycount=0		
	for p in outline_cut:
		mycount=mycount+1
		f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY-outlineOffsetY, p.z+supportEndZ+offsetZ, p.w+offsetRotation, feedrate_outline))
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY-outlineOffsetY-200, p.z+supportEndZ+offsetZ, p.w+offsetRotation, feedrate))
	f.write('(cutting outline left)\n')
	f.write(zsafe)
	p=outline_cut[mycount-1]
	f.write('g0 x%.3f y%.3f a%.3f\n' % (p.x+supportEndX, -p.y+supportEndY+outlineOffsetY+200, p.w+offsetRotation))
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, -p.y+supportEndY+outlineOffsetY+200, p.z+supportEndZ+offsetZ, p.w+offsetRotation, feedrate))
	for i in range(mycount):
		p=outline_cut[mycount-i-1]
		f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, -p.y+supportEndY+outlineOffsetY, p.z+supportEndZ+offsetZ, p.w+offsetRotation, feedrate))
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, -p.y+supportEndY+outlineOffsetY+200, p.z+supportEndZ+offsetZ, p.w+offsetRotation, feedrate))

f.write(zsafe)
f.write('(cutting deck right)\n');
p=deck_right[0]
f.write('g0 x%.3f y%.3f a%.3f\n' % (p.x+supportEndX, p.y+supportEndY, p.w+offsetRotation))
for p in deck_right:
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY, p.z+supportEndZ, p.w+offsetRotation, feedrate))

f.write(zsafe)
f.write('(cutting rail right)\n');	
p=rail_right[0]
f.write('g0 x%.3f y%.3f a%.3f\n' % (p.x+supportEndX, p.y+supportEndY, p.w+offsetRotation))
for p in rail_right:
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY, p.z+supportEndZ, p.w+offsetRotation, feedrate))

f.write(zsafe)
f.write('(cutting deck left)\n');	
p=deck_left[0]
f.write('g0 x%.3f y%.3f a%.3f\n' % (p.x+supportEndX, p.y+supportEndY, p.w+offsetRotation))
for p in deck_left:
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY, p.z+supportEndZ, p.w+offsetRotation, feedrate))

f.write(zsafe)
f.write('(cutting rail left)\n');	
p=rail_left[0]
f.write('g0 x%.3f y%.3f a%.3f\n' % (p.x+supportEndX, p.y+supportEndY, p.w+offsetRotation))
for p in rail_left:
	f.write('g1 x%.3f y%.3f z%.3f a%.3f f%d\n' % (p.x+supportEndX, p.y+supportEndY, p.z+supportEndZ, p.w+offsetRotation, feedrate))

f.write(zsafe)
f.write("M2");
f.close()

JOptionPane.showMessageDialog(None, "Finished g-code generation")

