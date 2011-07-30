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

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.jmol.api.JmolViewer;
import org.jmol.script.Token;
import org.jmol.shape.Shape;
import org.jmol.shapesurface.Isosurface;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.Viewer;
import org.jmol.util.*;

*//**
 * 
 *//*
public class Slicer {

  boolean useGUI;
  protected JmolViewer viewer;
  private float angleXY;
  private float anglefromZ;
  private float position;
  private float thickness;
  private Point3f negCorner;
  private Point3f posCorner;
  private SliceBox slicebox = new SliceBox();
  private boolean lefton = false;
  private boolean righton = false;

  public Slicer(JmolViewer viewer, boolean useGUI) {
    this.viewer = viewer;
    if (useGUI) {
      initGUI();
    } else {
      useGUI = false;
    }
    this.useGUI = useGUI;
    //initialize to match the boundbox
    initSliceBox(viewer.getBoundBoxCenter(), viewer.getBoundBoxCornerVector());

  }

  private void initGUI() {
    //TODO start up the GUI
  }

  private void initSliceBox(Point3f center, Vector3f cornervector) {
    //default to boundbox
    this.angleXY = 0;
    this.anglefromZ = (float) (Math.PI / 2);
    this.position = 50;
    this.thickness = 100;
    this.negCorner = SliceBox.vectoPoint(SliceBox.vecAdd(
        SliceBox.pointtoVec(center), SliceBox.vecScale(-1, cornervector)));
    this.posCorner = SliceBox.vectoPoint(SliceBox.vecAdd(
        SliceBox.pointtoVec(center), cornervector));
    this.slicebox.setSlice(angleXY, anglefromZ, position, thickness, negCorner,
        posCorner);
  }

  public Point3f[] getSliceVert() {
    return (slicebox.vertices);
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
   *//*
  public void setSlice(float angleXY, float anglefromZ, float position,
                       float thickness) {
    this.angleXY = angleXY;
    this.anglefromZ = anglefromZ;
    this.position = position;
    this.thickness = thickness;
    this.slicebox.setSlice(angleXY, anglefromZ, position, thickness, negCorner,
        posCorner);
  }

  *//**
   * 
   * @param radians
   *        (float) angle from X-axis of projection on XY plane in radians.
   *//*
  public void setSliceAngleXY(float radians) {
    if (this.angleXY != radians) {
      this.angleXY = radians;
      this.slicebox.setSlice(angleXY, anglefromZ, position, thickness,
          negCorner, posCorner);
      //update planes
      if (lefton) {
        drawSlicePlane(Token.left, true);
      }
      if (righton) {
        drawSlicePlane(Token.right, true);
      }
    }
  }

  public float getSliceAngleXY() {
    return (this.angleXY);
  }

  *//**
   * 
   * @param radians
   *        (float) angle of vector from Z axis in radians.
   *//*
  public void setSliceAnglefromZ(float radians) {
    if (this.anglefromZ != radians) {
      this.anglefromZ = radians;
      this.slicebox.setSlice(angleXY, anglefromZ, position, thickness,
          negCorner, posCorner);
      //update planes
      if (lefton) {
        drawSlicePlane(Token.left, true);
      }
      if (righton) {
        drawSlicePlane(Token.right, true);
      }
    }
  }

  public float getAnglefromZ() {
    return (this.anglefromZ);
  }

  *//**
   * 
   * @param percent
   *        (float) position of slice center along direction vector as percent
   *        of boundbox diagonal (50% is origin).
   *//*
  public void setSlicePosition(float percent) {
    if (this.position != percent) {
      this.position = percent;
      this.slicebox.setSlice(angleXY, anglefromZ, position, thickness,
          negCorner, posCorner);
      //update planes
      if (lefton) {
        drawSlicePlane(Token.left, true);
      }
      if (righton) {
        drawSlicePlane(Token.right, true);
      }
    }
  }

  public float getSlicePosition() {
    return (this.position);
  }

  *//**
   * 
   * @param percent
   *        (float) thickness of slice as percent of boundbox diagonal.
   *//*
  public void setSliceThickness(float percent) {
    if (this.thickness != percent) {
      this.thickness = percent;
      this.slicebox.setSlice(angleXY, anglefromZ, position, thickness,
          negCorner, posCorner);
      //update planes
      if (lefton) {
        drawSlicePlane(Token.left, true);
      }
      if (righton) {
        drawSlicePlane(Token.right, true);
      }
    }
  }

  public float getSliceThickness() {
    return (this.thickness);
  }

  public void sliceObject(String objectName) {
    int objectID = ((Viewer) viewer).getShapeManager()
        .getShapeIdFromObjectName(objectName);
    if (objectID == JmolConstants.SHAPE_ISOSURFACE) {//valid shape
      //Now need to get the command for the proper isosurface
      Shape shape = ((Viewer) viewer).getShapeManager().getShape(objectID);
      int surfaceIndex = shape.getIndexFromName(objectName);
      String cmd = ((Isosurface) shape).getCmd(surfaceIndex);
      //Make sure the size of the slices will include the whole volume of the isosurface
      negCorner = ((Isosurface) shape).getXYZMin(surfaceIndex);
      posCorner = ((Isosurface) shape).getXYZMax(surfaceIndex);
      this.slicebox.setSlice(angleXY, anglefromZ, position, thickness,
          negCorner, posCorner);
      String str1 = cmd.substring(0,
          (cmd.indexOf(objectName, 0) + objectName.length() + 1));
      String str2 = cmd.substring((cmd.indexOf(objectName, 0)
          + objectName.length() + 1));
      String newName = "_sliced" + objectName;
      str1 = TextFormat.simpleReplace(str1, objectName, newName);
      str2 = TextFormat.simpleReplace(str2, objectName, newName);
      int index = str2.indexOf("translucent");
      do {
        if (index > -1) {
          int startnext = index + 12;
          int endnext = -1;
          if ((endnext = str2.indexOf(" ", startnext)) > startnext) {
            try {
              int trans = Integer.parseInt(str2.substring(startnext, endnext));//necessary for try...catch
              str2 = str2.substring(0, index) + " opaque "
                  + str2.substring(endnext);
            } catch (NumberFormatException e) {
              str2 = str2.substring(0, index) + " opaque "
                  + str2.substring(startnext);
            }
          }
        }
        index = str2.indexOf("translucent");
      } while (index > -1);
      str2 = TextFormat.simpleReplace(str2, "translucent", "opaque");//just in case
      cmd = TextFormat.simpleReplace(cmd, "opaque", "translucent 200");
      cmd += ";\n" + str1 + " slab slicebox " + str2 + ";";
      //and because some stored commands don't contain the opacity...
      cmd += "\n" + "isosurface " + objectName + " color translucent 200;";
      cmd = "!" + cmd;
      if (lefton) {
        cmd+="slice left on;\n";
      }
      if (righton) {
        cmd+="slice right on;\n";
      } 
      viewer.evalStringQuiet(cmd);
    }//TODO shouldn't fail silently as it does now.
    return;
  }

  public void drawSlicePlane(int side, boolean on) {
    switch (side) {
    case Token.left:
      if (on) {
        String vertStr = "{" + slicebox.vertices[0].x + " "
            + slicebox.vertices[0].y + " " + slicebox.vertices[0].z + "}";
        vertStr += "{" + slicebox.vertices[1].x + " " + slicebox.vertices[1].y
            + " " + slicebox.vertices[1].z + "}";
        vertStr += "{" + slicebox.vertices[3].x + " " + slicebox.vertices[3].y
            + " " + slicebox.vertices[3].z + "}";
        vertStr += "{" + slicebox.vertices[2].x + " " + slicebox.vertices[2].y
            + " " + slicebox.vertices[2].z + "}";
        viewer.evalStringQuiet("draw _slicerLeft plane " + vertStr
            + " color translucent 200 magenta;");
        lefton = true;
      } else {
        viewer.evalStringQuiet("draw _slicerLeft off");
        lefton = false;
      }
      break;
    case Token.right:
      if (on) {
        String vertStr = "{" + slicebox.vertices[4].x + " "
            + slicebox.vertices[4].y + " " + slicebox.vertices[4].z + "}";
        vertStr += "{" + slicebox.vertices[5].x + " " + slicebox.vertices[5].y
            + " " + slicebox.vertices[5].z + "}";
        vertStr += "{" + slicebox.vertices[7].x + " " + slicebox.vertices[7].y
            + " " + slicebox.vertices[7].z + "}";
        vertStr += "{" + slicebox.vertices[6].x + " " + slicebox.vertices[6].y
            + " " + slicebox.vertices[6].z + "}";
        viewer.evalStringQuiet("draw _slicerRight plane " + vertStr
            + " color translucent 200 cyan;");
        righton = true;
      } else {
        viewer.evalStringQuiet("draw _slicerRight off");
        righton = false;
      }
      break;
    }
  }
*/

//}
