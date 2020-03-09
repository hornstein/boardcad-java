package boardcad.gui.jdk;

import java.util.Vector;

import javax.media.j3d.*;
import javax.vecmath.*;

import board.BezierBoard;

public class FasterBrd3DModelGenerator {

	boolean mCancelExecuting = false;
	Vector<Thread> mThreads = new Vector<Thread>();
	boolean mInitialModelRun = true;

	public void update3DModel(BezierBoard brd, Shape3D model, int numTasks) {
		if (brd.isEmpty())
			return;

		mCancelExecuting = true;

		System.out
				.println("BezierBoard.update3DModel() cancel execution, wait for threads");

		for (Thread thread : mThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out
						.println("BezierBoard.update3DModel() InterruptedException");
			}
		}

		mThreads.clear();

		System.out.println("BezierBoard.update3DModel() Wait finished");

		if (model.numGeometries() != numTasks) {
			System.out
					.printf("BezierBoard.update3DModel() Need initial run geom:%d tasks:%d",
							model.numGeometries(), numTasks);

			model.removeAllGeometries();
			mInitialModelRun = true;
		} else {
			mInitialModelRun = false;
		}

		mCancelExecuting = false;
		double length = brd.getLength();

		for (int i = 0; i < numTasks; i++) {
			final double sx = (length / numTasks) * i;
			final double ex = (length / numTasks) * (i + 1);
			final int index = i;
			Runnable task = () -> {
				update3DModel((BezierBoard) brd.clone(), model, sx, ex, index);
			};

			Thread thread = new Thread(task);
			mThreads.add(thread);
			thread.start();
		}

	}

	public void update3DModel(BezierBoard brd, Shape3D model, double startX,
			double endX, int index) {
		double lengthAccuracy = 1.0;
		double deckWidthAccuracy = 1.0;
		double bottomWidthAccuracy = 1.0;

		double spanLength = endX - startX;
		double width = brd.getCenterWidth();

		int lengthSteps = (int) (spanLength / lengthAccuracy) + 1;
		// int lengthSteps = 200;
		int deckSteps = (int) ((width / 2.0) / deckWidthAccuracy) + 1;
		int railSteps = 0;
		int bottomSteps = (int) (width / 2.0 / bottomWidthAccuracy) + 1;

		double lengthStep = spanLength / lengthSteps;

		int nrOfCoords = (lengthSteps) * (deckSteps + railSteps + bottomSteps)
				* 4 * 2;

		QuadArray quads;
		quads = new QuadArray(nrOfCoords, IndexedQuadArray.COORDINATES
				| IndexedQuadArray.NORMALS);

		Point3d[][] vertices = new Point3d[deckSteps+1][lengthSteps+1];
		Vector3f[][] normals = new Vector3f[deckSteps+1][lengthSteps+1];
		Point3d[] quadCoords = new Point3d[lengthSteps*4];
		Vector3f[] quadNormals = new Vector3f[lengthSteps*4];

		int nrOfQuads = 0;
		double xPos = 0.0;

		// Deck
		double minAngle = -45.0;
		double maxAngle = 150.0;
		
		//Generate deck coordinates
		for (int i = 0; i <= deckSteps; i++) {
			if (mCancelExecuting)
				return;

			xPos = startX;

			for (int j = 0; j <= lengthSteps; j++) {

				vertices[i][j] = new Point3d(brd.getSurfacePoint(xPos, minAngle, maxAngle, i, deckSteps));
				normals[i][j] = new Vector3f(brd.getSurfaceNormal(xPos, minAngle, maxAngle,i, deckSteps));

				xPos += lengthStep;

			}
		}
		
		//Generate quads
		for (int i = 0; i < deckSteps; i++) {
			if (mCancelExecuting)
				return;

			int q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				quadCoords[q] = vertices[i][j];
				quadNormals[q] = normals[i][j];
				++q;
				quadCoords[q] = vertices[i][j+1];
				quadNormals[q] = normals[i][j+1];
				++q;
				quadCoords[q] = vertices[i+1][j+1];
				quadNormals[q] = normals[i+1][j+1];
				++q;
				quadCoords[q] = vertices[i+1][j];
				quadNormals[q] = normals[i+1][j];
				++q;				
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += lengthSteps;
		}
		
		//Mirror deck coordinates
		for (int i = 0; i <= deckSteps; i++) {
			if (mCancelExecuting)
				return;

			for (int j = 0; j <= lengthSteps; j++) {

				vertices[i][j].setY(-vertices[i][j].getY());
				normals[i][j].setY(-normals[i][j].getY());
			}
		}
		
		//Generate mirrored quads
		for (int i = 0; i < deckSteps; i++) {
			if (mCancelExecuting)
				return;

			int q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				quadCoords[q] = vertices[i+1][j];
				quadNormals[q] = normals[i+1][j];
				++q;
				quadCoords[q] = vertices[i+1][j+1];
				quadNormals[q] = normals[i+1][j+1];
				++q;
				quadCoords[q] = vertices[i][j+1];
				quadNormals[q] = normals[i][j+1];
				++q;
				quadCoords[q] = vertices[i][j];
				quadNormals[q] = normals[i][j];
				++q;				
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += lengthSteps;
		}
		
		//Generate bottom
		minAngle = maxAngle;
		maxAngle = 360.0;
		for (int i = 0; i <= bottomSteps; i++) {
			if (mCancelExecuting)
				return;

			xPos = startX;

			for (int j = 0; j <= lengthSteps; j++) {

				vertices[i][j] = brd.getSurfacePoint(xPos, minAngle, maxAngle, i, bottomSteps);
				normals[i][j] = new Vector3f(brd.getSurfaceNormal(xPos, minAngle, maxAngle,i, bottomSteps));

				xPos += lengthStep;

			}
		}
		
		//Generate quads
		for (int i = 0; i < bottomSteps; i++) {
			if (mCancelExecuting)
				return;

			int q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				quadCoords[q] = vertices[i][j];
				quadNormals[q] = normals[i][j];
				++q;
				quadCoords[q] = vertices[i][j+1];
				quadNormals[q] = normals[i][j+1];
				++q;
				quadCoords[q] = vertices[i+1][j+1];
				quadNormals[q] = normals[i+1][j+1];
				++q;
				quadCoords[q] = vertices[i+1][j];
				quadNormals[q] = normals[i+1][j];
				++q;				
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += lengthSteps;
		}
		
		//Mirror bottom coordinates
		for (int i = 0; i <= bottomSteps; i++) {
			if (mCancelExecuting)
				return;

			for (int j = 0; j <= lengthSteps; j++) {

				vertices[i][j].setY(-vertices[i][j].getY());
				normals[i][j].setY(-normals[i][j].getY());
			}
		}
		
		//Generate mirrored quads
		for (int i = 0; i < bottomSteps; i++) {
			if (mCancelExecuting)
				return;

			int q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				quadCoords[q] = vertices[i+1][j];
				quadNormals[q] = normals[i+1][j];
				++q;
				quadCoords[q] = vertices[i+1][j+1];
				quadNormals[q] = normals[i+1][j+1];
				++q;
				quadCoords[q] = vertices[i][j+1];
				quadNormals[q] = normals[i][j+1];
				++q;
				quadCoords[q] = vertices[i][j];
				quadNormals[q] = normals[i][j];
				++q;				
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += lengthSteps;
		}

		if (mInitialModelRun) {
			model.addGeometry(quads);
		} else {
			model.setGeometry(quads, index);
		}

	}
}
