/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

package org.jmol.slicer;

/*
import javax.vecmath.*;
public class SliceBox {

  Point3f[] vertices = new Point3f[8]; //8 vertices (ordered to match boundbox)
  Plane leftPlane; //definition of the left plane
  Plane rightPlane; //definition of the left plane
  float angleXY; //0<=anglexy< 180 degrees
  float anglefromZ;//0<=anglefromZ < 180
  float position; //% of boundbox diagonal (50% centered)
  float thickness; //% of boundbox diagonal
  Point3f boundBoxNegCorner;
  Point3f boundBoxPosCorner;
  float diagonal;

  class Plane extends Vector3f {
    Vector3f[] basis;

    Plane(float angleXY, float anglefromZ, float length) {
      basis = new Vector3f[2];
      basis[0] = new Vector3f();
      basis[1] = new Vector3f();
      this.z = (float) (Math.cos(anglefromZ) * length);
      float projXY = (float) (Math.sin(anglefromZ) * length);
      this.y = (float) (projXY * Math.sin(angleXY));
      this.x = (float) (projXY * Math.cos(angleXY));
      basis[0].x = (float) (-Math.sin(angleXY));
      basis[0].y = (float) (Math.cos(angleXY));
      basis[0].z = 0;
      basis[1].x = (float) (Math.cos(anglefromZ) * Math.cos(angleXY));
      basis[1].y = (float) (Math.cos(anglefromZ) * Math.sin(angleXY));
      basis[1].z = (float) (-Math.sin(anglefromZ));
    }

        Plane(float x, float y, float z){
          this.x = x;
          this.y=y;
          this.z = z;
        }
  }

  *//**
   * Defines a parallelpiped within which isosurfaces (and in future? atoms) are
   * displayed.
   * 
   * @param angleXY
   *        (float)angle in radians from X-axis to projection in XY plane
   * @param anglefromZ
   *        (float)angle in radians from z-axis to vector
   * @param position
   *        (float) position along vector in % of boundbox diagonal
   * @param thickness
   *        (float) thickness of slice in % of boundbox diagonal
   * @param boundBoxNegCorner
   *        (Point3f) most negative boundbox corner
   * @param boundBoxPosCorner
   *        (Point3f) most positive boundbox corner
   *//*
  void setSlice(float angleXY, float anglefromZ, float position, float thickness,
                Point3f boundBoxNegCorner, Point3f boundBoxPosCorner){
    if (angleXY >= 0 && angleXY < Math.PI) {
      this.angleXY = angleXY;
    } else {
      float fix = (float) (Math.floor(angleXY / Math.PI));
      this.angleXY = (float)(angleXY - fix * Math.PI);
    }
    if (anglefromZ >= 0 && anglefromZ < Math.PI) {
      this.anglefromZ = anglefromZ;
    } else {
      double fix = Math.floor(anglefromZ / Math.PI);
      this.anglefromZ = (float) (anglefromZ - fix * Math.PI);
    }
    this.position = position;
    this.thickness = thickness;
    this.boundBoxNegCorner = boundBoxNegCorner;
    this.boundBoxPosCorner = boundBoxPosCorner;
    diagonal = boundBoxPosCorner.distance(boundBoxNegCorner);
    float length = diagonal * (position - 50 - thickness / 2)/100;
    leftPlane = new Plane(angleXY, anglefromZ, length);
    length = diagonal * (position - 50 + thickness / 2)/100;
    rightPlane = new Plane(angleXY, anglefromZ, length);
    //Build vertices that are outside the boundbox.
    //leftPlane
    Point3f[] tempVert = calcPlaneVert(leftPlane);
    vertices[0] = tempVert[0];
    vertices[1] = tempVert[1];
    vertices[2] = tempVert[3];//2 & 3 swapped to match boundbox ordering
    vertices[3] = tempVert[2];
    //rightPlane
    tempVert = calcPlaneVert(rightPlane);
    vertices[4] = tempVert[0];
    vertices[5] = tempVert[1];
    vertices[6] = tempVert[3];//2 & 3 swapped to match boundbox ordering
    vertices[7] = tempVert[2];
  }
*//**
 *   
 * @return returns this SliceBox
 *//*
  SliceBox getSlice(){
    return (this);
  }
  
*//**
 * @param plane (Plane) the plane
 * @param start (Point3f) start of line segment
 * @param end (Point3f) end of line segement
 * @return a Point3f if line segment intersects plane
 *//*
  private Point3f intersectionSegmentPlane(Plane plane, Point3f start,
                                           Point3f end) {
    Point3f intersection = new Point3f();
    Vector3f planeVec = new Vector3f(plane);
    Vector3f startVec = new Vector3f(start);
    Vector3f endVec = new Vector3f(end);
    float d = (planeVec.lengthSquared() - planeVec.dot(startVec))
        / (planeVec.dot(endVec) - planeVec.dot(startVec));
    if (d > 0 && d < 1) {
      intersection.x = start.x + d * (end.x - start.x);
      intersection.y = start.y + d * (end.y - start.y);
      intersection.z = start.z + d * (end.z - start.z);
    } else {
      intersection = null; // no intersection so don't return a value.
    }
    return (intersection);
  }

  public static Vector3f vecAdd(Vector3f v1, Vector3f v2) {
    Vector3f result = new Vector3f();
    result.x = v1.x + v2.x;
    result.y = v1.y + v2.y;
    result.z = v1.z + v2.z;
    return (result);
  }

  public static Vector3f vecScale(float scale, Vector3f v) {
    Vector3f result = new Vector3f();
    result.x = scale * v.x;
    result.y = scale * v.y;
    result.z = scale * v.z;
    return (result);
  }

  public static  Point3f vectoPoint(Vector3f v) {
    Point3f result = new Point3f();
    result.x = v.x;
    result.y = v.y;
    result.z = v.z;
    return (result);
  }

  public static Vector3f pointtoVec(Point3f p){
    Vector3f result = new Vector3f(p);
    return(result);
  }
   private Point3f[] calcPlaneVert(Plane plane) {
    Point3f[] result = new Point3f[4];
    float scale = (float) (0.5 * diagonal);
    Vector3f tempVec = new Vector3f();
    tempVec = vecScale(scale, vecAdd(plane.basis[0], plane.basis[1]));
    result[0] = vectoPoint(vecAdd(tempVec, plane));
    result[2] = vectoPoint(vecAdd(plane, vecScale(-1, tempVec)));
    tempVec = vecScale(scale,
        vecAdd(plane.basis[1], vecScale(-1, plane.basis[0])));
    result[1] = vectoPoint(vecAdd(plane, tempVec));
    result[3] = vectoPoint(vecAdd(plane, vecScale(-1, tempVec)));
    return (result);
  }
}
*/