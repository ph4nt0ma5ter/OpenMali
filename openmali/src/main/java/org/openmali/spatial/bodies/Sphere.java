/**
 * Copyright (c) 2007-2009, OpenMaLi Project Group all rights reserved.
 * 
 * Portions based on the Sun's javax.vecmath interface, Copyright by Sun
 * Microsystems or Kenji Hiranabe's alternative GC-cheap implementation.
 * Many thanks to the developers.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of the 'OpenMaLi Project Group' nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 */
/*
 * Copyright (c) 2002 Shaven Puppy Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'Shaven Puppy' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openmali.spatial.bodies;

import org.openmali.FastMath;
import org.openmali.vecmath2.Point3f;
import org.openmali.vecmath2.Tuple3f;

/**
 * An efficient Sphere.
 * 
 * Creation date: (06/02/2001 00:08:09)
 * 
 * @author Jack Ritter from "Graphics Gems", Academic Press, 1990
 * @author cas
 * @author Marvin Froehlich (aka Qudus)
 */
public class Sphere extends Body implements java.io.Serializable {
	private static final long serialVersionUID = 5096988865873236385L;

	private final Point3f center2 = new Point3f();

	/**
	 * @return this Sphere's center point.
	 */
	public final Point3f getCenter() {
		center2.set(centerX, centerY, centerZ);

		return (center2);
	}

	/**
	 * Sets the Sphere's radius.
	 * 
	 * @param radius
	 */
	public void setRadius(float radius) {
		maxCenterDist = radius;
		maxCenterDistSquared = radius * radius;
		distComputed = true;
	}

	/**
	 * @return the Sphere's radius.
	 */
	public final float getRadius() {
		return (getMaxCenterDistance());
	}

	/**
	 * @return the square of the Sphere's radius.
	 */
	public final float getRadiusSquared() {
		return (getMaxCenterDistanceSquared());
	}

	/**
	 * Checks, if the 2D-point is in the XY-projected circle.
	 * 
	 * @param px
	 * @param py
	 * @param distance the distance to (virtually) expand the radius by for this check
	 */
	public boolean containsXYPlus(float px, float py, float distance) {
		final float dx = px - getCenterX();
		final float dy = py - getCenterY();

		final float dist = FastMath.sqrt((dx * dx) + (dy * dy));

		if (dist < (getRadius() + distance)) {
			return (true);
		}

		return (false);
	}

	/**
	 * Checks, if the 2D-point is in the XY-projected circle.
	 * 
	 * @param px
	 * @param py
	 * @param distance the distance to (virtually) expand the radius by for this check
	 */
	public boolean containsPlus(float px, float py, float pz, float distance) {
		final float dx = px - getCenterX();
		final float dy = py - getCenterY();
		final float dz = pz - getCenterZ();

		final float dist = FastMath.sqrt((dx * dx) + (dy * dy) + (dz * dz));

		if (dist < (getRadius() + distance)) {
			return (true);
		}

		return (false);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(float px, float py, float pz) {
		return (Classifier.classifySpherePoint(this, px, py, pz) == Classifier.Classification.INSIDE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Point3f point) {
		return (contains(point.getX(), point.getY(), point.getZ()));
	}

	private void combineWithSphere(float xx, float yy, float zz, float rr) {
		final float dx = xx - getCenterX();
		final float dy = yy - getCenterY();
		final float dz = zz - getCenterZ();
		final float d = FastMath.sqrt((dx * dx) + (dy * dy) + (dz * dz));

		if (getRadius() >= d + rr) {
			// we enclose other sphere
			return;
		}

		if (rr >= d + getRadius()) {
			// other sphere encloses us
			this.setCenter(xx, yy, zz);
			this.setRadius(rr);

			return;
		}

		final float dl = FastMath.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		final float d2 = ((d + getRadius() + rr) / 2.0f) - getRadius();
		final float dx2 = dx / dl * d2;
		final float dy2 = dy / dl * d2;
		final float dz2 = dz / dl * d2;

		setCenter(getCenterX() + dx2, getCenterY() + dy2, getCenterZ() + dz2);

		final float dia = getRadius() + d + rr;

		setRadius(dia / 2.0f);
	}

	private void combineWithSphere(Sphere sphere) {
		combineWithSphere(sphere.getCenterX(), sphere.getCenterY(), sphere.getCenterZ(), sphere.getRadius());
	}

	private void combineWithPoint(float px, float py, float pz) {
		combineWithSphere(px, py, pz, 0.0f);
	}

	private void combineWithPoint(Tuple3f p) {
		combineWithPoint(p.getX(), p.getY(), p.getZ());
	}

	private void combineWithBox(Box box) {
		combineWithPoint(box.getLower());
		combineWithPoint(box.getUpper());
	}

	/**
	 * {@inheritDoc}
	 */
	public void combine(BodyInterface body) {
		if (body instanceof Sphere)
			combineWithSphere((Sphere) body);
		else if (body instanceof Box)
			combineWithBox((Box) body);
		else if (body instanceof ConvexHull)
			throw new Error("ConvexHull not supported yet");
		else
			throw new Error("unknown Body type");
	}

	/**
	 * {@inheritDoc}
	 */
	public void combine(BodyInterface[] bos) {
		for (int i = 0; i < bos.length; i++)
			combine(bos[i]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void combine(float px, float py, float pz) {
		combineWithPoint(px, py, pz);
	}

	/**
	 * {@inheritDoc}
	 */
	public void combine(Point3f point) {
		combine(point.getX(), point.getY(), point.getZ());
	}

	/**
	 * {@inheritDoc}
	 */
	public void combine(Point3f[] points) {
		for (int i = 0; i < points.length; i++) {
			combine(points[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return (this.getClass().getSimpleName() + " { center: " + getCenter() + ", radius: " + getRadius() + " }");
	}

	/**
	 * Creates a BoundingSphere.
	 */
	public Sphere(float x, float y, float z, float radius) {
		super();

		this.setCenter(x, y, z);
		this.maxCenterDist = radius;
		this.maxCenterDistSquared = radius * radius;
		this.distComputed = true;
	}

	/**
	 * Creates a BoundingSphere.
	 */
	public Sphere(Tuple3f center, float radius) {
		this(center.getX(), center.getY(), center.getZ(), radius);
	}

	/**
	 * Creates a BoundingSphere.
	 */
	public Sphere() {
		this(0f, 0f, 0f, 0f);
	}
}
