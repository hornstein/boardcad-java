"""
PMC Code Generation for Wave Shapes Machine Gosford - DECK
     Author: Peter Weatherhead
       Date: 20/01/2016
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
import boardcam.cutters.STLCutter
import boardcam.BoardMachine
import cadcore.NurbsPoint
from javax.swing import *
import math
import copy


#==========================================================================
# FUNCTION DEFINITIONS 
def mod_p_avoid_support(p):
#  	   Author: Peter Weatherhead
#        Date: 16/01/2016
#    Revision: 1.00
# Description: Modify point position around blank supports (+/-550mm)
# 				- point is elevated and moved outwards according to the 
#				  global parameters below in the main code.
#				- provides central routine for method used in multiple 
#				  locations.
# 		Input: 3D point p (cadcore.NurbsPoint)
#	   Output: A boolean is returned indicating if the point position was 
#			   modified or not.
	if(p.y < WS_Support_Pos_Y + WS_Support_Prox_Tol_Y):
		if((p.x > -(WS_Support_Pos_X + WS_Support_Prox_Tol_X)) and \
			(p.x < -(WS_Support_Pos_X - WS_Support_Prox_Tol_X))): 
			if(abs(p.z) > WS_Support_Z_Perc_Width * WS_Support_Minus_Width):
				p.y = WS_Support_Pos_Y + WS_Support_Prox_Tol_Y
				p.x = p.x + p.x/abs(p.x)*WS_Support_Z_Adjust
				return True
		elif(p.x > (WS_Support_Pos_X - WS_Support_Prox_Tol_X) and \
			(p.x < (WS_Support_Pos_X + WS_Support_Prox_Tol_X))):
			if(abs(p.z) > WS_Support_Z_Perc_Width * WS_Support_Plus_Width):
				p.y = WS_Support_Pos_Y + WS_Support_Prox_Tol_Y
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
filename=machine.deckFileName

width_steps=40
length_steps=400
zmax=123
zmax_offset = 20
min_plan_step_len = 10 	# minimum step length in plan - used to cut out short steps
min_z_step_length = 6   # a plan move can be less than 10mm if the Z step exceeds 6mm

cut_stringer = False    # determines whether outline cuts are allowed near 
						# the stringer
					
WS_Support_Pos_X = 550
WS_Support_Pos_Y = -32.26
WS_Support_Prox_Tol_X = 42/2 + 12/2 + 3
WS_Support_Prox_Tol_Y = 12/2 + 3 + 2
WS_Support_Z_Adjust = 3
WS_Support_Z_Perc_Width = 0.95

WS_Stringer_Lat_Offsets = [17,8]
WS_Stringer_Depth_Offsets = [2,0]

WS_Outline_Stringer_Offset = 25

WS_PMC_Precision = '%.2f'
# 5% jumps
#WS_PMC_Stringer_Speeds = ['CMD"%10"\n','CMD"%15"\n','CMD"%20"\n','CMD"%25"\n','CMD"%30"\n']
#WS_PMC_Stringer_Spd_Dists = [0,112.5,225,337.5,450]
# 10% jumps
WS_PMC_Stringer_Speeds = ['CMD"%10"\n','CMD"%20"\n','CMD"%30"\n']
WS_PMC_Stringer_Spd_Dists = [0,225,450]

WS_PMC_Speeds = ['CMD"%30"\n', 'CMD"%40"\n', 'CMD"%50"\n', 'CMD"%60"\n', 'CMD"%70"\n', 'CMD"%80"\n', 'CMD"%90"\n', 'CMD"%100"\n', 'CMD"%110"\n', 'CMD"%120"\n']
WS_PMC_Spd_Dists = [0,60,80,100,120,140,160,180,200,220]

# Create a cutter
cutter=boardcam.cutters.STLCutter()
cutter.init('WS_DECK_bullnose_2.stl')
cutter.scale(machine.toolScaleX,machine.toolScaleY,machine.toolScaleZ);
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
#DEBUG
#print 'max width = %0.3f' % max_width
#print 't = %0.3f\n' % t

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

temp_s = deck.getMinS()
p = deck.getPoint(temp_s, 0)

while (p.x <= WS_Support_Pos_X):
	temp_s=temp_s+0.01
	p = deck.getPoint(temp_s, 0)	

temp_t = deck.getMinT()
p = deck.getPoint(WS_Support_Pos_X, t)

while (WS_Support_Plus_Width - math.fabs(p.z) <= 0):
	temp_t=temp_t+0.01
	p = deck.getPoint(temp_s, temp_t)	
	if (math.fabs(p.z) > WS_Support_Plus_Width):
		WS_Support_Plus_Width = math.fabs(p.z)

# Calculate step length
width_step=((deck.getMaxT()-deck.getMinT())/2-t_outline-0.01)/width_steps
length_step=(deck.getMaxS()-deck.getMinS())/length_steps

#==========================================================================
# STRINGER CUTS
# Obtain Tail and Nose Positions.
# Start at Tail, offset to positive Y first
# Cuts past nose and tail for clean up.
stringer_cut=[]
stringer_path=[]

# New (Tidier Method)

# Get Central Path (Tail to Nose)
t = (deck.getMaxT()-deck.getMinT())/2
s = deck.getMinS()
p = deck.getPoint(s,t)
TailX = p.x #- 21     # accounts for tool offset on normal runs.
zmax_tail = p.y + zmax_offset  # hop low over cuts
p.x = p.x - 21
p.y = p.y + 3
temp_p = copy.copy(p)
temp_p.x = temp_p.x - 10
temp_p1 = copy.copy(temp_p)
temp_p1.y = zmax_tail
stringer_path.append(temp_p1)
stringer_path.append(temp_p)
stringer_path.append(p)
last_p = cadcore.NurbsPoint(-3000,0,0)

while (s<=deck.getMaxS()):
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	p=cutter.calcOffset(p,n)
	if ((p.x-last_p.x)>= min_plan_step_len):
		stringer_path.append(p)
		last_p = p
	s=s+length_step
s = deck.getMaxS()
p = deck.getPoint(s,t)
NoseX = p.x
zmax_nose = p.y + zmax_offset  # hop low over cuts
p.x = p.x + 0
p.y = p.y + 3
temp_p = copy.copy(p)
temp_p.x = temp_p.x + 21
temp_p1 = copy.copy(temp_p)
temp_p1.y = zmax_nose
stringer_path.append(p)
stringer_path.append(temp_p)
stringer_path.append(temp_p1)

# Create Tool Path for multiple stringer cuts according to the arrays at the top
# of the code.  Increase array dims for more cuts.
for Lat_Off in WS_Stringer_Lat_Offsets:
	for p in stringer_path:    # cut tail to nose
		temp_p = copy.copy(p)
		if ((temp_p.y <> zmax_nose) and (temp_p.y <> zmax_tail)): # do not modify traverse heights
			temp_p.y = temp_p.y + \
				WS_Stringer_Depth_Offsets[WS_Stringer_Lat_Offsets.index(Lat_Off)]
		temp_p.z = Lat_Off
		stringer_cut.append(temp_p)
		
	for p in reversed(stringer_path):  # cut nose to tail
		temp_p = copy.copy(p)
		if ((temp_p.y <> zmax_nose) and (temp_p.y <> zmax_tail)):         # do not modify traverse heights
			temp_p.y = temp_p.y + \
				WS_Stringer_Depth_Offsets[WS_Stringer_Lat_Offsets.index(Lat_Off)]
		temp_p.z = -Lat_Off
		stringer_cut.append(temp_p)
		

# DEBUG - prints stringer cut path coordinates to console.
"""
for p in stringer_cut:
	print str(round(p.x,1)) + ", " + str(round(p.y,1)) + ", " + str(round(p.z,1))
"""

#==========================================================================
# OUTLINE CUTS
# Requires symmetric model
# calculate tool path outline
outline_cut=[]

centre_t=(deck.getMaxT()-deck.getMinT())/2
S_Min_Stop = deck.getMinS()
S_Max_Stop = deck.getMaxS()
S_Min_Stop_Set = False
S_Max_Stop_Set = False
S_Min_WS_Stop = deck.getMinS()
S_Max_WS_Stop = deck.getMaxS()
S_Min_WS_Stop_Set = False
S_Max_WS_Stop_Set = False


p=deck.getPoint(deck.getMinS(),centre_t)
n=cadcore.NurbsPoint(-1,0,0)
p=cutter.calcOffset(p,n)
p.z = WS_Outline_Stringer_Offset
# This is necessary because p is a pointer, not a parameter, so modifying p
# later also effects the value appended to the array
outline_cut.append(cadcore.NurbsPoint(p.x,zmax_tail,p.z))
outline_cut.append(p)

t=t_outline
s=deck.getMinS()

p=deck.getPoint(s,t)
n=cadcore.NurbsPoint(-1,0,0)
p=cutter.calcOffset(p,n)
outline_cut.append(p)
last_p = cadcore.NurbsPoint(-3000,0,0)

while (s<deck.getMaxS()):
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	# Determine if Deck Cuts need to be stopped short at nose and tail
	if ((p.z < 30) and (not S_Min_Stop_Set)):
		S_Min_Stop = s
	elif ((p.z >= 30) and (not S_Min_Stop_Set)):
		S_Min_Stop_Set = True
	if ((p.z < 20) and (S_Min_Stop_Set) and (not S_Max_Stop_Set)):
		S_Max_Stop = s
		S_Max_Stop_Set = True
	p=cutter.calcOffset(p,n)
	mod_p_avoid_support(p)
	if (((math.sqrt((p.x-last_p.x)*(p.x-last_p.x)+(p.z-last_p.z)*(p.z-last_p.z)) >= min_plan_step_len/2) \
		or (abs(p.y-last_p.y) > min_z_step_length)) and (p.x-last_p.x) >= 0) :
		outline_cut.append(p)
		last_p = p
	s=s+length_step

s=deck.getMaxS()

p=deck.getPoint(s,t)
n=cadcore.NurbsPoint(1,0,0)
p=cutter.calcOffset(p,n)
outline_cut.append(p)
	
p=deck.getPoint(deck.getMaxS(),centre_t)
n=cadcore.NurbsPoint(1,0,0)
p=cutter.calcOffset(p,n)
p.z = WS_Outline_Stringer_Offset
outline_cut.append(p)
outline_cut.append(cadcore.NurbsPoint(p.x,zmax_nose,p.z))

# Get stops for WS Cutter 
s=deck.getMinS()
while (s<deck.getMaxS()):
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	# Determine if Deck Cuts need to be stopped short at nose and tail
	if ((p.z < 30+21) and (not S_Min_WS_Stop_Set)):
		S_Min_WS_Stop = s
	elif ((p.z >= 30+21) and (not S_Min_WS_Stop_Set)):
		S_Min_WS_Stop_Set = True
	if ((p.z < 20+21) and (S_Min_WS_Stop_Set) and (not S_Max_WS_Stop_Set)):
		S_Max_WS_Stop = s
		S_Max_WS_Stop_Set = True
	s=s+length_step
	
#==========================================================================
# DECK CUTS
# calculate tool path deck starting at tail.
deck_cut=[]
deck_cut_support=[]
loop_num = 0
loop_num_offset = width_steps/2
HasStepLenHalved = False	
# Array of booleans indicating whether this cut is modified around supports

# Determine Nose Travel Height
s=deck.getMaxS();
t=(deck.getMaxT()-deck.getMinT())/2 
p=deck.getPoint(s,t) 
n=deck.getNormal(s,t)
p=cutter.calcOffset(p,n)
WS_Safe_Trav_Nose = p.y + 20

# Determine Tail Travel Height
s=deck.getMinS();
p=deck.getPoint(s,t) 
n=deck.getNormal(s,t)
p=cutter.calcOffset(p,n)
WS_Safe_Trav_Tail = p.y + 20

t = centre_t-width_step*1 # Dont Trace Stringer Again
s = S_Min_Stop			  # Dont Start at tail if this area is to be skipped
p=deck.getPoint(s,t) 
n=deck.getNormal(s,t)
p=cutter.calcOffset_WS(p,n)
p.y = WS_Safe_Trav_Tail
#deck_cut.append(p)		 # Swing cut doesnt need to step up

S_Max_Stop_Var = S_Max_WS_Stop
S_Min_Stop_Var = S_Min_WS_Stop
step_Multiplier = 1

while (t>t_outline):
	last_p = cadcore.NurbsPoint(-3000,0,0)
	deck_cut_moded = False
	while (s<=S_Max_Stop_Var): # Cut towards Nose
		p=deck.getPoint(s,t)
		n=deck.getNormal(s,t)
		if (loop_num < loop_num_offset):
			#dummy = "dummy"
			p=cutter.calcOffset_WS(p,n)
		else:
			p=cutter.calcOffset(p,n)		
		deck_cut_moded =  mod_p_avoid_support(p) or deck_cut_moded
							# note order is important to ensure mod_p is always
							# evaluated whether or not the cut has already been 
							# modified.
		if (((math.sqrt((p.x-last_p.x)*(p.x-last_p.x)+(p.z-last_p.z)*(p.z-last_p.z)) >= min_plan_step_len) \
		or (abs(p.y-last_p.y) > min_z_step_length)) and ((p.x-last_p.x) >= 0)):
			deck_cut.append(p)
			last_p = p
		s=s+length_step
	
	temp_p = copy.copy(last_p) # Step up for traverse, ensure not modifying last p by 
						  # copying variable reference
	temp_p.y = WS_Safe_Trav_Nose
	deck_cut.append(temp_p)

	if (deck_cut_moded):  # Determine if cut was modified to avoid supports
		deck_cut_support.append(True)
		if (not HasStepLenHalved):
			#min_plan_step_len = min_plan_step_len/2-t_outline
			HasStepLenHalved = True
			loop_num = loop_num_offset
			step_Multiplier = 2
	else:
		deck_cut_support.append(False)

	# Step to other side of board
	deck_cut_moded = False	
	t=centre_t-(t-centre_t)
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	if (loop_num < loop_num_offset):
		p=cutter.calcOffset_WS(p,n)
	else:
			p=cutter.calcOffset(p,n)
	p.y = WS_Safe_Trav_Nose
	deck_cut.append(p)
	last_p = cadcore.NurbsPoint(3000,0,0)
	
	while (s>=S_Min_Stop_Var): # Cut towards Tail
		p=deck.getPoint(s,t) 
		n=deck.getNormal(s,t)
		if (loop_num < loop_num_offset):
			p=cutter.calcOffset_WS(p,n)
		else:
			p=cutter.calcOffset(p,n)
		deck_cut_moded =  mod_p_avoid_support(p) or deck_cut_moded
		if (((math.sqrt((p.x-last_p.x)*(p.x-last_p.x)+(p.z-last_p.z)*(p.z-last_p.z)) >= min_plan_step_len) \
		or (abs(p.y-last_p.y) > min_z_step_length)) and ((p.x-last_p.x) <= 0)):
			deck_cut.append(p)
			last_p = p
		s=s-length_step
	#
	# Swing Cut does not step across at tail.
	#
	#temp_p = copy.copy(last_p) # Step up for traverse, ensure not modifying last p by 
						  # copying variable reference
	#temp_p.y = WS_Safe_Trav_Tail
	#deck_cut.append(temp_p)
	if (deck_cut_moded):
		deck_cut_support.append(True)
	else:
		deck_cut_support.append(False)
	
	# Step to other side of board (and increase cut width)
	if(loop_num <> loop_num_offset-1):
		#t=centre_t-(t-centre_t)-width_step
		t = t + abs(t-centre_t)/(t-centre_t)*width_step*step_Multiplier # Swing Cut - step over at nose only.
	else:
		#t=centre_t-(t-centre_t)
		t = t # Swing Cut - step over at nose.
		S_Max_Stop_Var = S_Max_Stop
		S_Min_Stop_Var = S_Min_Stop
		step_Multiplier = 1.2
	p=deck.getPoint(s,t)
	n=deck.getNormal(s,t)
	if (loop_num < loop_num_offset):
		p=cutter.calcOffset_WS(p,n)
	else:
		p=cutter.calcOffset(p,n)
	#
	# Swing Cut does not step across at tail.
	#
	#p.y = WS_Safe_Trav_Tail
	#deck_cut.append(p)
	loop_num = loop_num + 1

#==========================================================================
# MODIFY OUTLINE PATH TO AVOID STRINGER
# Avoid stringer collision - only at nose and tail
if(not cut_stringer):
	for p in outline_cut:
		if(p.z<WS_Outline_Stringer_Offset):
			p.z=WS_Outline_Stringer_Offset

#==========================================================================
# WRITE PMC CODE
# 
toolpath=[]

f=open(filename, 'w')

# WRITE CONSTANT HEADER
f.write('CLOSE\nDEL GATHER\nOPEN ROT CLEAR\nI194=1700\nI313=146944\nI314=-146944\n')
f.write('CMD"%50"\nTA200\nZ115\nX0Y0\nZ123\n')

# WRITE GO TO TAIL HOME
p = stringer_cut[0]
tempString = 'X' + WS_PMC_Precision + 'Y0\n'
f.write(tempString % p.x)
f.write(tempString % p.x)
f.write(tempString % p.x)
f.write(tempString % p.x)

# WRITE ACCEL and STRINGER SPEED
f.write('TA60\nF180\nCMD"%10"\n')

# WRITE STRINGER CUTS
lastPoint = cadcore.NurbsPoint(-3000,0,0)
secondLastPoint = cadcore.NurbsPoint(-3000,0,0)
i = 1
accel = True

for p in stringer_cut:
	# Skip First Point and last.
	if (stringer_cut.index(p) <> 0) and (stringer_cut.index(p) <> len(stringer_cut)-1):
		if (p.x <> lastPoint.x):
			f.write('X' + WS_PMC_Precision % (p.x))
		if (p.z <> lastPoint.z):
			f.write('Y' + WS_PMC_Precision % (p.z))
		if (p.y <> lastPoint.y):
			f.write('Z' + WS_PMC_Precision % (p.y))
		if ((p.x <> lastPoint.x) or (p.z <> lastPoint.z) or (p.y <> lastPoint.y)):
			f.write('\n')
		# Quadruple plunge coordinate for machine speed compensation.
		if (stringer_cut.index(p) == 1):
			for dummy_i in range(3):
				f.write('X' + WS_PMC_Precision % (p.x))
				f.write('Y' + WS_PMC_Precision % (p.z))
				f.write('Z' + WS_PMC_Precision % (p.y))
				f.write('\n')
		# INSERT SPEED COMMANDS
		if (accel and ((p.x - lastPoint.x) > 0) and (p.x > (WS_PMC_Stringer_Spd_Dists[i] + TailX))):
			f.write(WS_PMC_Stringer_Speeds[i])
			if (i < len(WS_PMC_Stringer_Speeds)-1):
				i = i + 1
			elif (i == len(WS_PMC_Stringer_Speeds)-1):
				accel = False
		if ((not accel) and ((p.x - lastPoint.x) > 0) and (p.x > (NoseX - WS_PMC_Stringer_Spd_Dists[i]))):
			if (i > 0):
				f.write(WS_PMC_Stringer_Speeds[i-1])
				i = i - 1
		if (accel and ((p.x - lastPoint.x) < 0) and (p.x < (NoseX - WS_PMC_Stringer_Spd_Dists[i]))):
			f.write(WS_PMC_Stringer_Speeds[i])
			if (i < len(WS_PMC_Stringer_Speeds)-1):
				i = i + 1
			elif (i == len(WS_PMC_Stringer_Speeds)-1):
				accel = False
		if ((not accel) and ((p.x - lastPoint.x) < 0) and (p.x < (TailX + WS_PMC_Stringer_Spd_Dists[i]))):
			if (i > 0):
				f.write(WS_PMC_Stringer_Speeds[i-1])
				i = i - 1
		if (((not accel) and (lastPoint.y == zmax_tail) and (secondLastPoint.y == zmax_tail)) \
			or ((not accel) and (lastPoint.y == zmax_nose) and (secondLastPoint.y == zmax_nose))):
			accel = True
			i = 1
		secondLastPoint = lastPoint
		lastPoint = p
		toolpath.append(cadcore.NurbsPoint(p.x,p.y,p.z))

# DECK CUTS
lastPoint = cadcore.NurbsPoint(3000,0,0)
secondLastPoint = cadcore.NurbsPoint(3000,0,0)
i = 1
accel = True
f.write(WS_PMC_Speeds[0])

print 'TailX = %0.3f' % TailX
print 'NoseX = %0.3f' % NoseX
print deck_cut_support

deck_cut_ind = 0

for p in deck_cut:
	# INSERT SPEED COMMANDS - Modified for One Step
	if (not deck_cut_support[deck_cut_ind]):
		if (accel and ((p.x - lastPoint.x) > 0) and (p.x > (WS_PMC_Spd_Dists[1] + TailX)) and (p.x < (WS_PMC_Spd_Dists[9] + TailX + 50))):
			f.write(WS_PMC_Speeds[i])
			if (i < 9):
				i = i + 1
			elif (i == 9):
				accel = False
		if ((not accel) and ((p.x - lastPoint.x) > 0) and (p.x > (NoseX - WS_PMC_Spd_Dists[9])) and (p.x > (NoseX - WS_PMC_Spd_Dists[9] - 50))):
			if (i > 0):
				f.write(WS_PMC_Speeds[i-1])
				i = i - 1
			elif (i == 0):
				accel = True
				i = 1
		if (accel and ((p.x - lastPoint.x) < 0) and (p.x < (NoseX - WS_PMC_Spd_Dists[1])) and (p.x > (NoseX - WS_PMC_Spd_Dists[9] - 50))):
			f.write(WS_PMC_Speeds[i])
			if (i < 9):
				i = i + 1
			elif (i == 9):
				accel = False
		if ((not accel) and ((p.x - lastPoint.x) < 0) and (p.x < (TailX + WS_PMC_Spd_Dists[9])) and (p.x < (WS_PMC_Spd_Dists[9] + TailX + 50))):
			if (i > 0):
				f.write(WS_PMC_Speeds[i-1])
				i = i - 1
			elif (i == 0):
				accel = True
				i = 1
		if ((not accel) and ((lastPoint.y == WS_Safe_Trav_Nose) or (lastPoint.y == WS_Safe_Trav_Tail))):
			accel = True
			i = 1
		if (((lastPoint.y == WS_Safe_Trav_Nose) and (secondLastPoint.y == WS_Safe_Trav_Nose)) or \
			(((p.x - lastPoint.x) > 0) and ((lastPoint.x - secondLastPoint.x) <= 0))):	
			deck_cut_ind = deck_cut_ind + 1
	# WRITE COORDS
	if (p.x <> lastPoint.x):
		f.write('X' + WS_PMC_Precision % (p.x))
	if (p.z <> lastPoint.z):
		f.write('Y' + WS_PMC_Precision % (-p.z))	# Reverse Direction for Swing Cutting
	if (p.y <> lastPoint.y):
		f.write('Z' + WS_PMC_Precision % (p.y))
	if ((p.x <> lastPoint.x) or (p.z <> lastPoint.z) or (p.y <> lastPoint.y)):
		f.write('\n')
	if ((abs(p.z) > (abs(lastPoint.z) + 11)) or (abs(p.z) < (abs(lastPoint.z) - 11))):
		JOptionPane.showMessageDialog(None, "Large Lateral Step Detected, Check Tool Path Carefully! (X,Y,Z = [%0.3f, %0.3f, %0.3f]  [%0.3f, %0.3f, %0.3f])" \
			% (p.x,p.y,p.z,lastPoint.x,lastPoint.y,lastPoint.z))

	secondLastPoint = lastPoint
	lastPoint = p
	toolpath.append(cadcore.NurbsPoint(p.x, p.y, -p.z))

# OUTLINE CUTS
f.write(WS_PMC_Speeds[0])	# Cut outline at minimum speed
f.write('Z' + WS_PMC_Precision % (WS_Safe_Trav_Tail))   # Jump up to avoid wiping tail
f.write('\n')
toolpath.append(cadcore.NurbsPoint(p.x, WS_Safe_Trav_Tail, -p.z))

for p in outline_cut:
	if (p.x <> lastPoint.x):
		f.write('X' + WS_PMC_Precision % (p.x))
	if (p.z <> lastPoint.z):
		f.write('Y' + WS_PMC_Precision % (-p.z))
	if (p.y <> lastPoint.y):
		f.write('Z' + WS_PMC_Precision % (p.y))
	if ((p.x <> lastPoint.x) or (p.z <> lastPoint.z) or (p.y <> lastPoint.y)):
		f.write('\n')
	lastPoint = p
	toolpath.append(cadcore.NurbsPoint(p.x,p.y,-p.z))

lastPoint = cadcore.NurbsPoint(p.x,p.y,-p.z)
	
for p in reversed(outline_cut):
	if (p.x <> lastPoint.x):
		f.write('X' + WS_PMC_Precision % (p.x))
	if (p.z <> lastPoint.z):
		f.write('Y' + WS_PMC_Precision % (p.z))
	if (p.y <> lastPoint.y):
		f.write('Z' + WS_PMC_Precision % (p.y))
	if ((p.x <> lastPoint.x) or (p.z <> lastPoint.z) or (p.y <> lastPoint.y)):
		f.write('\n')
	lastPoint = cadcore.NurbsPoint(p.x,p.y,p.z)
	toolpath.append(cadcore.NurbsPoint(p.x,p.y,p.z))

# WRITE CONSTANT FOOTER
f.write('TA200\nCMD"%50"\nZ115\nX0Y270\nCLOSE\n')

# CLOSE AND SAVE FILE
f.close()

# Visualize toolpath in BoardCAD
i=0
for p in toolpath:
	machine.deck_cut[i]=p;
	i=i+1

machine.nr_of_cuts_deck=i

JOptionPane.showMessageDialog(None, "Finished g-code generation")


