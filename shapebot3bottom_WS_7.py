"""
PMC Code Generation for Wave Shapes Machine Gosford - BOTTOM
     Author: Peter Weatherhead
       Date: 09/02/2016
   Revision: 0.90
Description: Based on original BoardCAD 3-axis Code.
			 Machining pattern per conversation with Graeme.
				- Alternate sides cuts, up one side, down other side.
					- Safe Z for traversing = plus 115mm, nose/tail +20mm, 
					  Safety switches = plus 123mm
			Stringer Cuts
				1. 2 cuts per side at 2mm above the surface - up, down
				2. 2 mm deeper, final cut stringer so tool is cutting at 20deg
			Outline Cuts
				Around perimeter - avoiding stringer.
				Especially useful for boards with squared off noses or tails.
"""

import boardcad.gui.jdk.BoardCAD
import boardcam.cutters.FlatCutter
import boardcam.BoardMachine
import cadcore.NurbsPoint
from javax.swing import *
import math
import copy

#==========================================================================
# FUNCTION DEFINITIONS 
def mod_p_avoid_support(p):
#  	   Author: Peter Weatherhead
#        Date: 30/01/2016
#    Revision: 1.00
# Description: Modify point position around blank supports (+/-550mm)
# 				- point is elevated and moved outwards according to the 
#				  global parameters below in the main code.
#				- provides central routine for method used in multiple 
#				  locations.
#				Note direction of p.y inequality is modified for bottom cuts
# 		Input: 3D point p (cadcore.NurbsPoint)
#	   Output: A boolean is returned indicating if the point position was 
#			   modified or not.
	if(p.y > WS_Support_Pos_Y - WS_Support_Prox_Tol_Y):
		if((p.x > -(WS_Support_Pos_X + WS_Support_Prox_Tol_X)) and \
			(p.x < -(WS_Support_Pos_X - WS_Support_Prox_Tol_X))): 
			if(abs(p.z) > WS_Support_Z_Perc_Width * WS_Support_Minus_Width):
				p.y = WS_Support_Pos_Y - WS_Support_Prox_Tol_Y
				p.x = p.x + p.x/abs(p.x)*WS_Support_Z_Adjust
				return True
		elif(p.x > (WS_Support_Pos_X - WS_Support_Prox_Tol_X) and \
			(p.x < (WS_Support_Pos_X + WS_Support_Prox_Tol_X))):
			if(abs(p.z) > WS_Support_Z_Perc_Width * WS_Support_Plus_Width):
				p.y = WS_Support_Pos_Y - WS_Support_Prox_Tol_Y
				p.x = p.x + p.x/abs(p.x)*WS_Support_Z_Adjust
				return True
		else:
			return False
	else:
		return False

#==========================================================================
# MAIN CODE

# Read machine configuration from shapebot.properties
machine=boardcam.BoardMachine()
machine.read_machine_data()
filename=machine.bottomFileName

width_steps=55
rail_steps=5
length_steps=700
zmax=-115
zmax_offset_stringer = 30
min_plan_step_len = 10 	# minimum step length in plan - used to cut out short steps
min_z_step_length = 6   # a plan move can be less than 10mm if the Z step exceeds 6mm

cut_stringer = False    # determines whether outline cuts are allowed near 
						# the stringer

stringerOffsetZ=[10, 5, 0]
stringerCutoff=machine.stringerCutoff

WS_Support_Pos_X = 550
WS_Support_Pos_Y = -32.26
WS_Support_Prox_Tol_X = 42/2 + 12/2 + 3
WS_Support_Prox_Tol_Y = 12/2 + 3 + 2
WS_Support_Z_Adjust = 3
WS_Support_Z_Perc_Width = 0.95

WS_Stringer_Lat_Offsets = [-22,-17,-8]
WS_Stringer_Depth_Offsets = [-2,-2,0]

WS_Outline_Stringer_Offset = 25

WS_PMC_Precision = '%.2f'
# Note final stringer pass on bottom is only 20% speed.
WS_PMC_Stringer_Speeds = ['CMD"%10"\n', 'CMD"%20"\n', 'CMD"%30"\n']
WS_PMC_Stringer_Spd_Dists = [0,225,450]
WS_PMC_Speeds = ['CMD"%30"\n', 'CMD"%40"\n', 'CMD"%50"\n', 'CMD"%60"\n', 'CMD"%70"\n', 'CMD"%80"\n', 'CMD"%90"\n', 'CMD"%100"\n', 'CMD"%110"\n', 'CMD"%120"\n']
WS_PMC_Spd_Dists = [0,50,60,70,80,90,100,110,120,130]

# FeedRate hard coded to F180
#FeedRateString = JOptionPane.showInputDialog(None, "Input Motor Feed Rate: F{data}\n Default Value = 220", "220")

#try:
#    FeedRate = int(FeedRateString)
#except ValueError:
#	JOptionPane.showMessageDialog(None, "Inappropriate Feedrate - input cannot be converted to integer, Feedrate set to F220")
#	FeedRate = 220
#if not(isinstance(FeedRate,(int,long))):
#	FeedRate = 220
#	JOptionPane.showMessageDialog(None, "Inappropriate Feedrate - input is not an integer, Feedrate set to F220")
#else:
#	if ((FeedRate > 240) or (FeedRate < 100)):
#		FeedRate = 220
#		JOptionPane.showMessageDialog(None, "Feedrate too slow (<100) or fast (>240), Feedrate set to F220")

FeedRate = 180
	
print 'TA60\nF'
print FeedRate
print '\nCMD"%10"\n'  

# Create a cutter

cutter=boardcam.cutters.STLCutter()
#cutter.init(machine.toolName)
cutter.init('WS_BOTTOM_bullnose_2.stl')
cutter.scale(1.0,1.0,1.0)

# Get the bottom and deck surface from BoardCAD
boardhandler=boardcad.gui.jdk.BoardCAD.getInstance().getBoardHandler()
board=boardhandler.getActiveBoard();
deck=board.getDeck()
bottom=board.getBottom()

# Get board widths at supports
WS_Support_Minus_Width = 0.0

temp_s = deck.getMinS()
p = deck.getPoint(temp_s, 0)
while (p.x <= -WS_Support_Pos_X):
	temp_s=temp_s+0.01
	p = deck.getPoint(temp_s, 0)	

temp_t = deck.getMinT()
p = deck.getPoint(temp_s, temp_t)
while (WS_Support_Minus_Width - math.fabs(p.z) <= 0):
	temp_t=temp_t+0.01
	p = deck.getPoint(temp_s, temp_t)	
	if (math.fabs(p.z) > WS_Support_Minus_Width):
		WS_Support_Minus_Width = math.fabs(p.z)

WS_Support_Plus_Width = 0.0
print 'Support Width - = %0.3f' % WS_Support_Minus_Width

temp_s = deck.getMinS()
p = deck.getPoint(temp_s, 0)
while (p.x <= WS_Support_Pos_X):
	temp_s=temp_s+0.01
	p = deck.getPoint(temp_s, 0)	

temp_t = deck.getMinT()
p = deck.getPoint(WS_Support_Pos_X, temp_t)
while (WS_Support_Plus_Width - math.fabs(p.z) <= 0):
	temp_t=temp_t+0.01
	p = deck.getPoint(temp_s, temp_t)	
	if (math.fabs(p.z) > WS_Support_Plus_Width):
		WS_Support_Plus_Width = math.fabs(p.z)

print 'Support Width + = %0.3f' % WS_Support_Plus_Width

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

t=bottom.getMinT()
n=deck.getNormal((bottom.getMaxS()-bottom.getMinS())/2, t)
max_width = math.fabs(p.z)+.1
i=math.fabs(p.z)
step=math.fabs(p.z)/width_steps

# Calculate step length
length_step=(bottom.getMaxS()-bottom.getMinS())/length_steps
width_step=((bottom.getMaxT()-bottom.getMinT())/2)/width_steps
if rail_steps>0:
	rail_step=(t_outline-deck.getMinT())/rail_steps

print 'width step = %0.3f' % width_step
print 'Length step = %0.3f' % length_step
print 'Rail step = %0.3f' % rail_step

#Find uniform distances for t and their angles
wval=[]
tval=[]

while i>0.001:
	# Modified Right Side Up
	wval.append(-(p.z-i))
	i=i-step

#print wval
print '\nwidth steps = %0.3f' % width_steps
print 'steps = %0.3f' % step
print 'max width = %0.3f' % max_width
print 't = %0.3f\n' % t

for w in wval:
	#JOptionPane.showMessageDialog(None, w)
	while (max_width - math.fabs(p.z) <= math.fabs(w)):#<=
		t=t+0.01
		p=bottom.getPoint((deck.getMaxS()-deck.getMinS())/2, t)
		n=bottom.getNormal((deck.getMaxS()-deck.getMinS())/2, t)	
	tval.append(t)
	
#print tval

#==========================================================================
# STRINGER CUTS
# Obtain Tail and Nose Positions.
# Start at Tail, offset to negative Y first
# Z and X axes flipped from deck in PMC output

stringer_cut=[]
stringer_path=[]

# New (Tidier Method)
# Get Central Path (Tail to Nose)
t = (bottom.getMaxT()-bottom.getMinT())/2
s = bottom.getMinS()
p = bottom.getPoint(s,t)
n = bottom.getNormal(s,t)
TailX = p.x
p=cutter.calcOffset(p,n)
zmax_tail = p.y - zmax_offset_stringer  # hop low over cuts
p.y = zmax_tail
stringer_path.append(p)
last_p = cadcore.NurbsPoint(-3000,0,0)

while (s<=bottom.getMaxS()):
	p=bottom.getPoint(s,t)
	n=bottom.getNormal(s,t)
	p=cutter.calcOffset(p,n)
	if (((p.x-last_p.x)>= min_plan_step_len) or (s > bottom.getMaxS() - 0.001)):
		stringer_path.append(p)
		last_p = p
	s=s+length_step

s = bottom.getMaxS()
p = bottom.getPoint(s,t)
NoseX = p.x
n=bottom.getNormal(s,t)
p=cutter.calcOffset(p,n)
zmax_nose = p.y - zmax_offset_stringer  # hop low over cuts
p.y = zmax_nose
stringer_path.append(p)

# Create Tool Path for multiple stringer cuts according to the arrays at the top
# of the code.  Increase array dims for more cuts.
for Lat_Off in WS_Stringer_Lat_Offsets:
	for p in stringer_path:    # cut tail to nose
		temp_p = copy.copy(p)
		if ((temp_p.y <> zmax_tail) and (temp_p.y <> zmax_nose)): # do not modify traverse heights
			temp_p.y = temp_p.y + \
				WS_Stringer_Depth_Offsets[WS_Stringer_Lat_Offsets.index(Lat_Off)]
		temp_p.z = Lat_Off
		stringer_cut.append(temp_p)
		
	for p in reversed(stringer_path):  # cut nose to tail
		temp_p = copy.copy(p)
		if ((temp_p.y <> zmax_tail) and (temp_p.y <> zmax_nose)):         # do not modify traverse heights
			temp_p.y = temp_p.y + \
				WS_Stringer_Depth_Offsets[WS_Stringer_Lat_Offsets.index(Lat_Off)]
		temp_p.z = -Lat_Off
		stringer_cut.append(temp_p)
	

#==========================================================================
# BOTTOM CUTS
# calculate tool path bottom
bottom_cut=[]
bottom_cut_support=[]	# Array of booleans indicating whether this cut is modified around supports
width_step_multiplier = 1  # modifiable multiplier to even out lateral steps.

# Calculate safe travel heights and set the start point (at tail offset to negative side)
s=bottom.getMinS()
t=(bottom.getMaxT()-bottom.getMinT())/2
centre_t=(bottom.getMaxT()-bottom.getMinT())/2

# Determine Tail Travel Height
p=bottom.getPoint(s,t) 
n=bottom.getNormal(s,t)
p=cutter.calcOffset_WS_bottom(p,n)
WS_Safe_Trav_Tail = p.y - 20

# Determine Nose Travel Height
s=bottom.getMaxS()
p=bottom.getPoint(s,t) 
n=bottom.getNormal(s,t)
p=cutter.calcOffset_WS_bottom(p,n)
WS_Safe_Trav_Nose = p.y - 20

s=bottom.getMinS()

# NEEDS TO BE MODIFIED FOR DIFFERENT BOARDS 
#    -6 FOR NORMAL BOARDS - maybe -2 - depends on outline.
#    -1 FOR TOMO QUAD CONCAVE
t=centre_t -2*width_step  # Dont Trace Stringer Again
 

while (t>=bottom.getMinT()+2*width_step):
	last_p = cadcore.NurbsPoint(-3000,0,0)
	bottom_cut_moded = False
	#print 't = %0.3f' % t
	while (s<bottom.getMaxS()):
		p=bottom.getPoint(s,t)
		n=bottom.getNormal(s,t)
		p=cutter.calcOffset_WS_bottom(p,n)		
		#bottom_cut_moded =  mod_p_avoid_support(p) or bottom_cut_moded
							# note order is important to ensure mod_p is always
							# evaluated whether or not the cut has already been 
							# modified.
		if (((math.sqrt((p.x-last_p.x)*(p.x-last_p.x)+(p.z-last_p.z)*(p.z-last_p.z)) >= min_plan_step_len) \
		or (abs(p.y-last_p.y) > min_z_step_length) or (p.x > NoseX-0.01)) and ((p.x-last_p.x) >= 0)):
			#print '%0.3f, %0.3f, %0.3f' % (p.x,p.y,p.z)
			bottom_cut.append(p)
			last_p = p
		s=s+length_step
	
	temp_p = copy.copy(p)
	temp_p.y = WS_Safe_Trav_Nose
	bottom_cut.append(temp_p)
	t=centre_t-(t-centre_t)
	#print 't = %0.3f' % t
	p=bottom.getPoint(s,t)
	n=bottom.getNormal(s,t)
	p=cutter.calcOffset_WS_bottom(p,n)
	p.y = WS_Safe_Trav_Nose
	bottom_cut.append(p)
	
	if (bottom_cut_moded):
		bottom_cut_support.append(True)
	else:
		bottom_cut_support.append(False)
	bottom_cut_moded = False
	
	last_p = cadcore.NurbsPoint(3000,0,0)
	while (s>=bottom.getMinS()):
		p=bottom.getPoint(s,t) 
		n=bottom.getNormal(s,t)
		p=cutter.calcOffset_WS_bottom(p,n)
		#bottom_cut_moded =  mod_p_avoid_support(p) or bottom_cut_moded
							# note order is important to ensure mod_p is always
							# evaluated whether or not the cut has already been 
							# modified.
		if (((math.sqrt((p.x-last_p.x)*(p.x-last_p.x)+(p.z-last_p.z)*(p.z-last_p.z)) >= min_plan_step_len) \
		or (abs(p.y-last_p.y) > min_z_step_length) or (p.x < TailX+0.01)) and ((p.x-last_p.x) <= 0)):
			#print '%0.3f, %0.3f, %0.3f,       %0.3f, %0.3f, %0.3f' % (p.x,p.y,p.z,last_p.x,last_p.y,last_p.z)
			bottom_cut.append(p)
			last_p = p
		s=s-length_step
	temp_p = copy.copy(p)
	temp_p.y = WS_Safe_Trav_Tail
	bottom_cut.append(temp_p)
	t=centre_t-(t-centre_t)-width_step_multiplier*width_step
	width_step_multiplier = width_step_multiplier * 1.07
	p=bottom.getPoint(s,t)
	n=bottom.getNormal(s,t)
	p=cutter.calcOffset_WS_bottom(p,n)
	p.y = WS_Safe_Trav_Tail
	bottom_cut.append(p)

	if (bottom_cut_moded):
		bottom_cut_support.append(True)
	else:
		bottom_cut_support.append(False)

#==========================================================================
# BOTTOM RAIL CUTS
# calculate tool path bottom
deck_cut=[]

if(rail_steps>0):
	centre_t=(deck.getMaxT()-deck.getMinT())/2
	s=deck.getMinS()
	t=deck.getMinT()
	p=deck.getPoint(s,t) 
	n=deck.getNormal(s,t)
	p=cutter.calcOffset(p,n)
	p.y = WS_Safe_Trav_Tail
	deck_cut.append(p)
	
	while (t<t_outline):
		last_p = cadcore.NurbsPoint(-3000,0,0)
		while (s<=deck.getMaxS()):
			p=deck.getPoint(s,t) 
			n=deck.getNormal(s,t)
			p=cutter.calcOffset(p,n)
			# Modify Target point around supports (+/-550mm)
			mod_p_avoid_support(p)
			if (((math.sqrt((p.x-last_p.x)*(p.x-last_p.x)+(p.z-last_p.z)*(p.z-last_p.z)) >= min_plan_step_len) \
			or (abs(p.y-last_p.y) > min_z_step_length)) and ((p.x-last_p.x) >= 0)):
				deck_cut.append(p)
				last_p = p
			s=s+length_step
		
		p.y = WS_Safe_Trav_Nose
		deck_cut.append(p)
		t=centre_t-(t-centre_t)
		#print 't = %0.3f' % t
		p=deck.getPoint(s,t)
		n=deck.getNormal(s,t)
		p=cutter.calcOffset(p,n)
		p.y = WS_Safe_Trav_Nose
		deck_cut.append(p)
		
		last_p = cadcore.NurbsPoint(3000,0,0)
		while (s>=deck.getMinS()):
			p=deck.getPoint(s,t)
			n=deck.getNormal(s,t)
			p=cutter.calcOffset(p,n)		
			# Modify Target point around supports (+/-550mm)
			mod_p_avoid_support(p)
			if (((math.sqrt((p.x-last_p.x)*(p.x-last_p.x)+(p.z-last_p.z)*(p.z-last_p.z)) >= min_plan_step_len) \
			or (abs(p.y-last_p.y) > min_z_step_length)) and ((p.x-last_p.x) <= 0)):
				deck_cut.append(p)
				last_p = p
			s=s-length_step
		
		p.y = WS_Safe_Trav_Tail
		deck_cut.append(p)
		t=centre_t-(t-centre_t)
		#print 't = %0.3f' % t
		p=deck.getPoint(s,t)
		n=deck.getNormal(s,t)
		p=cutter.calcOffset(p,n)
		p.y = WS_Safe_Trav_Tail
		deck_cut.append(p)
		
		t=t+rail_step

# Avoid stringer collision
if(not cut_stringer):
	for p in deck_cut:
		if(abs(p.z)<WS_Outline_Stringer_Offset):
			p.z = (p.z / abs(p.z)) * WS_Outline_Stringer_Offset
	

#==========================================================================
# WRITE PMC CODE
# Note that Coordinates are reversed on bottom (longitudinal and vertical 
# axes relative to board).

toolpath=[]

f=open(filename, 'w')

# WRITE CONSTANT HEADER
f.write('CLOSE\nDEL GATHER\nOPEN ROT CLEAR\nI194=1700\nI313=146944\nI314=-146944\n')
f.write('CMD"%50"\nTA200\nZ115\nX0Y0\nZ123\n')

# WRITE GO TO TAIL HOME
p = stringer_cut[0]
tempString = 'X' + WS_PMC_Precision + 'Y0\n'
f.write(tempString % -p.x)
f.write(tempString % -p.x)
f.write(tempString % -p.x)
f.write(tempString % -p.x)

# WRITE ACCEL and STRINGER SPEED and SIDE STEP
f.write('TA60\n')
#f.write(str(FeedRate))  Hard Coded Speed to F180
f.write('F180\n')
f.write('CMD"%10"\n')
tempString = 'Y' + WS_PMC_Precision + '\n'
f.write(tempString % WS_Stringer_Lat_Offsets[0])

# WRITE STRINGER CUTS
#===============================================================
lastPoint = cadcore.NurbsPoint(-3000,0,0)
secondLastPoint = cadcore.NurbsPoint(-3000,0,0)
i = 1
accel = True
toNose = True
StringerCutNum = 1
print 'NoseX = %0.3f\n' % NoseX
print 'TailX = %0.3f\n' % TailX

for p in stringer_cut:
	# Skip First Point and last points.
	if (stringer_cut.index(p) <> 0): 
	#and (stringer_cut.index(p) <> len(stringer_cut)-1): 
		if (p.x <> lastPoint.x):
			f.write('X' + WS_PMC_Precision % (-p.x))
		if (p.z <> lastPoint.z):
			f.write('Y' + WS_PMC_Precision % (p.z))
		if (p.y <> lastPoint.y):
			f.write('Z' + WS_PMC_Precision % (-p.y))
		if ((p.x <> lastPoint.x) or (p.z <> lastPoint.z) or (p.y <> lastPoint.y)):
			f.write('\n')
		# INSERT SPEED COMMANDS
		if toNose:
			if accel:
				if (p.x > (WS_PMC_Stringer_Spd_Dists[i] + TailX)):
					f.write(WS_PMC_Stringer_Speeds[i])
					if (StringerCutNum < 5):
						if (i < 2):
							i = i + 1
						elif (i == 2):
							accel = False
					else:
						if (i < 1):
							i = i + 1
						elif (i == 1):
							accel = False
			else: # Decelerating
				if (p.x > (NoseX - WS_PMC_Stringer_Spd_Dists[i])):
					if (i > 0):
						f.write(WS_PMC_Stringer_Speeds[i-1])
						i = i - 1
		else: # towards Tail
			if accel:
				if (p.x < (NoseX - WS_PMC_Stringer_Spd_Dists[i])):
					f.write(WS_PMC_Stringer_Speeds[i])
					if (StringerCutNum < 5):
						if (i < 2):
							i = i + 1
						elif (i == 2):
							accel = False
					else:
						if (i < 1):
							i = i + 1
						elif (i == 1):
							accel = False
			else: # Decelerating
				if (p.x < (TailX + WS_PMC_Stringer_Spd_Dists[i])):
					if (i > 0):
						f.write(WS_PMC_Stringer_Speeds[i-1])
						i = i - 1
		# Turn Around Routine
		if (((lastPoint.y == zmax_tail) and (p.y == zmax_tail)) or 
			((lastPoint.y == zmax_nose) and (p.y == zmax_nose))):
			accel = True
			toNose = not(toNose)
			i = 1
			StringerCutNum = StringerCutNum + 1
		secondLastPoint = lastPoint
		lastPoint = p
		toolpath.append(cadcore.NurbsPoint(p.x,p.y,p.z))

# Add Centring Coordinate before beginning foam cuts as no lift off
toolpath.append(cadcore.NurbsPoint(lastPoint.x,lastPoint.y,0))
f.write('Y0\n')

# BOTTOM CUTS
#===============================================================
lastPoint = cadcore.NurbsPoint(-3000,0,0)
secondLastPoint = cadcore.NurbsPoint(-3000,0,0)
i = 1
accel = True
toNose = True
bottom_cut_ind = 0

f.write(WS_PMC_Speeds[0])

print 'TailX = %0.3f' % TailX
print 'NoseX = %0.3f' % NoseX
#print deck_cut_support

bottom_cut_ind = 0

for p in bottom_cut:
	# INSERT SPEED COMMANDS
	#print 'Acc = %0.3f' % accel
	#print 'i = %0.3f' % i
	#print 'bah = %0.3f\n' % WS_PMC_Stringer_Spd_Dists[i]
	# Skip last points.
	if (bottom_cut.index(p) <> len(bottom_cut)-1):
		# Speed commands modified to ensure one point between each.
		if (not bottom_cut_support[bottom_cut_ind]):
			if toNose:
				if accel:
					if (p.x > (WS_PMC_Spd_Dists[i] + TailX)):
						f.write(WS_PMC_Speeds[i])
						if (i < 9):
							i = i + 1
						elif (i == 9):
							accel = False
				else: # Decelerating
					if (p.x > (NoseX - WS_PMC_Spd_Dists[i])):
						if (i > 0):
							f.write(WS_PMC_Speeds[i-1])
							i = i - 1
			else: # towards Tail
				if accel:
					if (p.x < (NoseX - WS_PMC_Spd_Dists[i])):
						f.write(WS_PMC_Speeds[i])
						if (i < 9):
							i = i + 1
						elif (i == 9):
								accel = False
				else: # Decelerating
					if (p.x < (TailX + WS_PMC_Spd_Dists[i])):
						if (i > 0):
							f.write(WS_PMC_Speeds[i-1])
							i = i - 1
		# Turn Around Routine
		if (((lastPoint.y == WS_Safe_Trav_Tail) and (p.y == WS_Safe_Trav_Tail)) or \
			((lastPoint.y == WS_Safe_Trav_Nose) and (p.y == WS_Safe_Trav_Nose))):
			accel = True
			toNose = not(toNose)
			i = 1
			bottom_cut_ind = bottom_cut_ind + 1
			#print toNose
			#print bottom_cut_ind
		
		# WRITE COORDS
		if (p.x <> lastPoint.x):
			f.write('X' + WS_PMC_Precision % (-p.x))
		if (p.z <> lastPoint.z):
			f.write('Y' + WS_PMC_Precision % (p.z))
		if (p.y <> lastPoint.y):
			f.write('Z' + WS_PMC_Precision % (-p.y))
		if ((p.x <> lastPoint.x) or (p.z <> lastPoint.z) or (p.y <> lastPoint.y)):
			f.write('\n')
		secondLastPoint = lastPoint
		lastPoint = p
		toolpath.append(cadcore.NurbsPoint(p.x,p.y,p.z))


# RAIL CUTS
# Constant Speed for Rails at this stage.
f.write('CMD"%30"\n')
for p in deck_cut:
	if (deck_cut.index(p) <> len(deck_cut)-1):
		if (p.x <> lastPoint.x):
			f.write('X' + WS_PMC_Precision % (-p.x))
		if (p.z <> lastPoint.z):
			f.write('Y' + WS_PMC_Precision % (-p.z))
		if (p.y <> lastPoint.y):
			f.write('Z' + WS_PMC_Precision % (-p.y))
		if ((p.x <> lastPoint.x) or (p.z <> lastPoint.z) or (p.y <> lastPoint.y)):
			f.write('\n')
		toolpath.append(cadcore.NurbsPoint(p.x, p.y, p.z))
		lastPoint = p

# WRITE CONSTANT FOOTER
f.write('TA200\nCMD"%50"\nZ115\nX0Y270\nCLOSE\n')

# CLOSE AND SAVE FILE
f.close()

# Visualize toolpath in BoardCAD
i=0
for p in toolpath:
	machine.bottom_cut[i]=p;
	i=i+1

machine.nr_of_cuts_bottom=i
JOptionPane.showMessageDialog(None, "Finished g-code generation")


