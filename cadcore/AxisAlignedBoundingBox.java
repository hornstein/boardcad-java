package cadcore;

import javax.vecmath.Point3d;

public class AxisAlignedBoundingBox 
{
		private Point3d mMin;
		private Point3d mMax;

		public AxisAlignedBoundingBox(double minX, double maxX, double minY, double maxY, double minZ,
				double maxZ) {
			mMin = new Point3d(minX, minY, minZ);
			mMax = new Point3d(minX, minY, minZ);
		}

		public AxisAlignedBoundingBox(Point3d aMin, Point3d aMax) 
		{
			mMin = aMin;
			mMax = aMax;
		}
		
		public double getWidth()
		{
			return mMax.y - mMin.y;
		}

		public double getLength()
		{
			return mMax.x - mMin.x;
		}
		
		public double getHeight()
		{
			return mMax.z - mMin.z;
		}
		
		public double getTop()
		{
			return mMax.z;
		}

		public double getBottom()
		{
			return mMin.z;
		}
		
		public double getMinX()
		{
			return mMin.x;
		}
		
		public double getMinY()
		{
			return mMin.y;
		}
		
		public double getMinZ()
		{
			return mMin.z;
		}

		public double getMaxX()
		{
			return mMax.x;
		}
		
		public double getMaxY()
		{
			return mMax.y;
		}
		
		public double getMaxZ()
		{
			return mMax.z;
		}

		public boolean isIntersectingVerticalDisc(Point3d pos, double r) {
			if (pos.x < mMin.x || pos.x > mMax.x)
			{
				return false;
			}

			// find the square of the distance
			// from the disc to the box
			double s = 0, d = 0;
			if (pos.y < mMin.y) {
				s = pos.y - mMin.y;
			} else if (pos.y > mMax.y) {
				s = pos.y - mMax.y;
			}
			d += s * s;

			s = 0;
			if (pos.z < mMin.z) {
				s = pos.z - mMin.z;
			} else if (pos.z > mMax.z) {
				s = pos.z - mMax.z;
			}
			d += s * s;

			return d <= r * r;

		}
		
		public boolean isIntersectingHorizontalDisc(Point3d pos, double r) 
		{
//			System.out.printf("isIntersectingHorizontalDisc() pos: %f, %f, %f radius: %f\n", pos.x, pos.y, pos.z, r);
//
//			System.out.printf("isIntersectingHorizontalDisc() min: %f, %f, %f max: %f, %f, %f\n", mMin.x, mMin.y, mMin.z, mMax.x, mMax.y, mMax.z);

			if (pos.z < mMin.z || pos.z > mMax.z)
			{
//				System.out.printf("isIntersectingHorizontalDisc() z %f outside of range %f to %f\n", pos.z, mMin.z, mMax.z);
				return false;
			}

			// find the square of the distance
			// from the disc to the box
			double s = 0, d = 0;
			if (pos.x < mMin.x) {
				s = pos.x - mMin.x;
			} else if (pos.x > mMax.x) {
				s = pos.x - mMax.x;
			}
			d += s * s;
			
			s = 0;
			if (pos.y < mMin.y) {
				s = pos.y - mMin.y;
			} else if (pos.y > mMax.y) {
				s = pos.y - mMax.y;
			}
			d += s * s;

			return d <= r * r;
		}

		public boolean isIntersectingSphere(Point3d pos, double r)
		{
			// find the square of the distance
			// from the disc to the box
			double s = 0, d = 0;
			if (pos.x < mMin.x) {
				s = pos.x - mMin.x;
			} else if (pos.x > mMax.x) {
				s = pos.x - mMax.x;
			}
			d += s * s;

			s = 0;
			if (pos.y < mMin.y) {
				s = pos.y - mMin.y;
			} else if (pos.y > mMax.y) {
				s = pos.y - mMax.y;
			}
			d += s * s;

			s = 0;
			if (pos.z < mMin.z) {
				s = pos.z - mMin.z;
			} else if (pos.z > mMax.z) {
				s = pos.z - mMax.z;
			}
			d += s * s;

			return d <= r * r;

		}
		
		public boolean isIntersectingVerticalCylinder(Point3d pos, double r, double height) {
			//Check if it overlaps height
			if (pos.z + height < mMin.z || pos.z > mMax.z)
			{
				return false;
			}
			
			return isIntersectingHorizontalDisc(new Point3d(pos.x, pos.y, (mMin.z+mMax.z)/2), r);
		}
		
		public boolean isIntersectingHorizontalCylinder(Point3d pos, double r, double length) 
		{
			//Check if it overlaps lengthwise
			if (pos.x + length < mMin.x || pos.x > mMax.x)
			{
				return false;
			}
			
			return isIntersectingVerticalDisc(new Point3d((mMin.x+mMax.x)/2, pos.y, pos.z), r);
		}

}
