/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-03-30 11:40:16 -0500 (Fri, 30 Mar 2007) $
 * $Revision: 7273 $
 *
 * Copyright (C) 2007 Miguel, Bob, Jmol Development
 *
 * Contact: hansonr@stolaf.edu
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.jvxl.readers;

import javax.vecmath.Point3f;
import javax.vecmath.Point4f;

import org.jmol.jvxl.data.JvxlCoder;

class IsoFxyReader extends VolumeDataReader {
  
  IsoFxyReader(SurfaceGenerator sg) {
    super(sg);
    precalculateVoxelData = false;
    atomDataServer = sg.getAtomDataServer();
    params.fullyLit = true;
  }

  private float[][] data;
  private boolean isPlanarMapping;
  
  @Override
  protected void setup() {
    isPlanarMapping = (params.thePlane != null || params.state == Parameters.STATE_DATA_COLORED);
    if (params.functionInfo.size() > 5)
      data = (float[][]) params.functionInfo.get(5);
    setup("functionXY");
  }

  protected void setup(String type) {
    String functionName = (String) params.functionInfo.get(0);
    jvxlFileHeaderBuffer = new StringBuffer();
    jvxlFileHeaderBuffer.append(type).append("\n").append(functionName).append("\n");
    setVolumeData();
    JvxlCoder.jvxlCreateHeaderWithoutTitleOrAtoms(volumeData, jvxlFileHeaderBuffer);
  }

  @Override
  protected void setVolumeData() {
    if (data == null) {
      setVolumeDataParams();
      return;
    }
    volumetricOrigin.set((Point3f) params.functionInfo.get(1));
    for (int i = 0; i < 3; i++) {
      Point4f info = (Point4f) params.functionInfo.get(i + 2);
      voxelCounts[i] = Math.abs((int) info.x);
      volumetricVectors[i].set(info.y, info.z, info.w);      
    }
    if (isAnisotropic)
      setVolumetricAnisotropy();
  }

  @Override
  public float getValue(int x, int y, int z, int ptyz) {
    float value;
    if (data == null) {
      value = evaluateValue(x, y, z);
    } else {
      volumeData.voxelPtToXYZ(x, y, z, ptTemp);
      value = data[x][y]; 
    }
    return (isPlanarMapping ? value : value - ptTemp.z);
  }
  
  private final float[] values = new float[3];
  protected float evaluateValue(int x, int y, int z) {
    if (params.func == null)
      return 0;
    volumeData.voxelPtToXYZ(x, y, z, ptTemp);
    values[0] = ptTemp.x;
    values[1] = ptTemp.y;
    values[2] = ptTemp.z;
    return atomDataServer.evalFunctionFloat(params.func[0], params.func[1], values);
  }
}
