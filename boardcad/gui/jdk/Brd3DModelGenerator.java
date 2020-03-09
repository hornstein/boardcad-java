package boardcad.gui.jdk;

import java.util.Vector;

import javax.media.j3d.*;
import javax.vecmath.*;

import board.BezierBoard;

public class Brd3DModelGenerator {

	boolean mCancelExecuting = false;
	Vector<Thread> mThreads = new Vector<Thread>();
	boolean mInitialModelRun = true;

	public void update3DModel(BezierBoard brd, Shape3D model, int numTasks) {
		if (brd.isEmpty())
			return;

		mCancelExecuting = true;

		System.out
				.println("BezierBoard.update3DModel() cancel execution, wait for futures");

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

		Point3d[] vertices = new Point3d[4];
		Vector3f[] normals = new Vector3f[4];
		Point3d[] mirrorVertices = new Point3d[4];
		Vector3f[] mirrorNormals = new Vector3f[4];
		for (int i = 0; i < 4; i++) {
			vertices[i] = new Point3d();
			normals[i] = new Vector3f();
			mirrorVertices[i] = new Point3d();
			mirrorNormals[i] = new Vector3f();
		}

		int nrOfQuads = 0;
		double xPos = 0.0;

		// Deck
		double minAngle = -45.0;
		double maxAngle = 45.0;
		for (int i = 0; i < deckSteps; i++) {
			if (mCancelExecuting)
				return;

			xPos = startX;

			// first coords in lengthwise strip
			vertices[0].set(brd.getSurfacePoint(xPos, minAngle, maxAngle, i,
					deckSteps));
			normals[0].set(brd.getSurfaceNormal(xPos, minAngle, maxAngle, i,
					deckSteps));

			vertices[3].set(brd.getSurfacePoint(xPos, minAngle, maxAngle,
					i + 1, deckSteps));
			normals[3].set(brd.getSurfaceNormal(xPos, minAngle, maxAngle,
					i + 1, deckSteps));

			xPos += lengthStep;

			for (int j = 1; j <= lengthSteps; j++) {

				vertices[1].set(brd.getSurfacePoint(xPos, minAngle, maxAngle,
						i, deckSteps));
				normals[1].set(brd.getSurfaceNormal(xPos, minAngle, maxAngle,
						i, deckSteps));

				vertices[2].set(brd.getSurfacePoint(xPos, minAngle, maxAngle,
						i + 1, deckSteps));
				normals[2].set(brd.getSurfaceNormal(xPos, minAngle, maxAngle,
						i + 1, deckSteps));

				// Build one quad
				quads.setCoordinates(nrOfQuads * 4, vertices);
				quads.setNormals(nrOfQuads * 4, normals);
				nrOfQuads++;

				// Mirror
				for (int n = 0; n < 4; n++) {
					mirrorVertices[n].set(vertices[3 - n]);
					mirrorNormals[n].set(normals[3 - n]);

					mirrorVertices[n].y = -mirrorVertices[n].y;
					mirrorNormals[n].y = -mirrorNormals[n].y;
				}

				// Build mirrored quad
				quads.setCoordinates(nrOfQuads * 4, mirrorVertices);
				quads.setNormals(nrOfQuads * 4, mirrorNormals);
				nrOfQuads++;

				// Get ready for next step
				vertices[0].set(vertices[1]);
				normals[0].set(normals[1]);

				vertices[3].set(vertices[2]);
				normals[3].set(normals[2]);

				xPos += lengthStep;

			}
		}

		// Bottom
		minAngle = maxAngle;
		maxAngle = 360.0;
		for (int i = 0; i < bottomSteps; i++) {
			if (mCancelExecuting)
				return;

			xPos = startX;

			// first coords in lengthwise strip
			vertices[0].set(brd.getSurfacePoint(xPos, minAngle, maxAngle, i,
					bottomSteps));
			normals[0].set(brd.getSurfaceNormal(xPos, minAngle, maxAngle, i,
					bottomSteps));

			vertices[3].set(brd.getSurfacePoint(xPos, minAngle, maxAngle,
					i + 1, bottomSteps));
			normals[3].set(brd.getSurfaceNormal(xPos, minAngle, maxAngle,
					i + 1, bottomSteps));

			xPos += lengthStep;

			for (int j = 1; j <= lengthSteps; j++) {
				// Two next coords
				vertices[1].set(brd.getSurfacePoint(xPos, minAngle, maxAngle,
						i, bottomSteps));
				normals[1].set(brd.getSurfaceNormal(xPos, minAngle, maxAngle,
						i, bottomSteps));

				vertices[2].set(brd.getSurfacePoint(xPos, minAngle, maxAngle,
						i + 1, bottomSteps));
				normals[2].set(brd.getSurfaceNormal(xPos, minAngle, maxAngle,
						i + 1, bottomSteps));

				// Build one quad
				quads.setCoordinates(nrOfQuads * 4, vertices);
				quads.setNormals(nrOfQuads * 4, normals);
				nrOfQuads++;

				// Mirror
				for (int n = 0; n < 4; n++) {
					mirrorVertices[n].set(vertices[3 - n]);
					mirrorNormals[n].set(normals[3 - n]);

					mirrorVertices[n].y = -mirrorVertices[n].y;
					mirrorNormals[n].y = -mirrorNormals[n].y;
				}

				// Build mirrored quad
				quads.setCoordinates(nrOfQuads * 4, mirrorVertices);
				quads.setNormals(nrOfQuads * 4, mirrorNormals);
				nrOfQuads++;

				// Get ready for next step
				vertices[0].set(vertices[1]);
				normals[0].set(normals[1]);

				vertices[3].set(vertices[2]);
				normals[3].set(normals[2]);

				xPos += lengthStep;

			}
		}

		if (mInitialModelRun) {
			model.addGeometry(quads);
		} else {
			model.setGeometry(quads, index);
		}

	}
}
