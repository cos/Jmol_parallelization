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

import java.util.Random;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.jmol.jvxl.data.JvxlCoder;
import org.jmol.util.Logger;
import org.jmol.util.Measure;

class IsoShapeReader extends VolumeDataReader {

  private int psi_n = 2;
  private int psi_l = 1;
  private int psi_m = 1;
  private float psi_Znuc = 1; // hydrogen
  private float sphere_radiusAngstroms;
  private int monteCarloCount;
  private Random random;
  IsoShapeReader(SurfaceGenerator sg, float radius) {
    super(sg);
    sphere_radiusAngstroms = radius;    
  }
  
  IsoShapeReader(SurfaceGenerator sg, int n, int l, int m, float z_eff, int monteCarloCount) {
    super(sg);
    psi_n = n;
    psi_l = l;
    psi_m = m;
    psi_Znuc = z_eff;    
    sphere_radiusAngstroms = 0;
    this.monteCarloCount = monteCarloCount;
  }

  private boolean allowNegative = true;
  
  private double[] rfactor = new double[10];
  private double[] pfactor = new double[10];

  private final static double A0 = 0.52918; //x10^-10 meters
  private final static double ROOT2 = 1.414214;

  
  private float radius;
  private final Point3f ptPsi = new Point3f();


  @Override
  protected void setup() {
    volumeData.sr = this; // we will provide point data for mapping
    precalculateVoxelData = false;
    if (center.x == Float.MAX_VALUE)
      center.set(0, 0, 0);
    String type = "sphere";
    switch (dataType) {
    case Parameters.SURFACE_ATOMICORBITAL:
      calcFactors(psi_n, psi_l, psi_m);
      autoScaleOrbital();
      ptsPerAngstrom = 5f;
      maxGrid = 40;
      type = "hydrogen-like orbital";
      if (monteCarloCount > 0) {
        vertexDataOnly = true;
        //params.colorDensity = true;
        random = new Random(params.randomSeed);
      }
      break;
    case Parameters.SURFACE_LONEPAIR:
    case Parameters.SURFACE_RADICAL:
      type = "lp";
      vertexDataOnly = true;
      radius = 0;
      ptsPerAngstrom = 1;
      maxGrid = 1;
      break;
    case Parameters.SURFACE_LOBE:
      allowNegative = false;
      calcFactors(psi_n, psi_l, psi_m);
      radius = 1.1f * eccentricityRatio * eccentricityScale;
      if (eccentricityScale > 0 && eccentricityScale < 1)
        radius /= eccentricityScale;
      ptsPerAngstrom = 10f;
      maxGrid = 21;
      type = "lobe";
      break;
    case Parameters.SURFACE_ELLIPSOID3:
      type = "ellipsoid(thermal)";
      radius = 3.0f * sphere_radiusAngstroms;
      ptsPerAngstrom = 10f;
      maxGrid = 22;
      break;
    case Parameters.SURFACE_ELLIPSOID2:
      type = "ellipsoid";
      // fall through
    case Parameters.SURFACE_SPHERE:
    default:
      radius = 1.2f * sphere_radiusAngstroms * eccentricityScale;
      ptsPerAngstrom = 10f;
      maxGrid = 22;
      break;
    }
    setVolumeData();
    setHeader(type + "\n");
  }

  @Override
  protected void setVolumeData() {
    setVoxelRange(0, -radius, radius, ptsPerAngstrom, maxGrid);
    setVoxelRange(1, -radius, radius, ptsPerAngstrom, maxGrid);
    if (allowNegative)
      setVoxelRange(2, -radius, radius, ptsPerAngstrom, maxGrid);
    else
      setVoxelRange(2, 0, radius / eccentricityRatio, ptsPerAngstrom, maxGrid);
  }

  @Override
  public float getValue(int x, int y, int z, int ptyz) {
    volumeData.voxelPtToXYZ(x, y, z, ptPsi);
    return getValueAtPoint(ptPsi);
  }
  
  @Override
  public float getValueAtPoint(Point3f pt) {
    ptTemp.set(pt);
    ptTemp.sub(center);
    if (isEccentric)
      eccentricityMatrixInverse.transform(ptTemp);
    if (isAnisotropic) {
      ptTemp.x /= anisotropy[0];
      ptTemp.y /= anisotropy[1];
      ptTemp.z /= anisotropy[2];
    }
    if (sphere_radiusAngstroms > 0) {
      if (params.anisoB != null) {
        
        return sphere_radiusAngstroms - 
        
        (float) Math.sqrt(ptTemp.x * ptTemp.x + ptTemp.y * ptTemp.y + ptTemp.z
            * ptTemp.z) /
        (float) (Math.sqrt(
            params.anisoB[0] * ptTemp.x * ptTemp.x +
            params.anisoB[1] * ptTemp.y * ptTemp.y +
            params.anisoB[2] * ptTemp.z * ptTemp.z +
            params.anisoB[3] * ptTemp.x * ptTemp.y +
            params.anisoB[4] * ptTemp.x * ptTemp.z +
            params.anisoB[5] * ptTemp.y * ptTemp.z));
      }
      return sphere_radiusAngstroms
          - (float) Math.sqrt(ptTemp.x * ptTemp.x + ptTemp.y * ptTemp.y + ptTemp.z
              * ptTemp.z);
    }
    float value = (float) hydrogenAtomPsi(ptTemp);
    return (allowNegative || value >= 0 ? value : 0);
  }
  
  private void setHeader(String line1) {
    jvxlFileHeaderBuffer = new StringBuffer(line1);
    if(sphere_radiusAngstroms > 0) {
    jvxlFileHeaderBuffer.append(" rad=").append(sphere_radiusAngstroms);
    }else{
      jvxlFileHeaderBuffer
      .append(" n=").append(psi_n)
      .append(", l=").append(psi_l)
      .append(", m=").append(psi_m)
      .append(" Znuc=").append(psi_Znuc)
      .append(" res=").append(ptsPerAngstrom)
      .append(" rad=").append(radius);
    }
    jvxlFileHeaderBuffer.append(
            isAnisotropic ? " anisotropy=(" + anisotropy[0] + ","
                + anisotropy[1] + "," + anisotropy[2] + ")\n" : "\n");
    JvxlCoder.jvxlCreateHeaderWithoutTitleOrAtoms(volumeData, jvxlFileHeaderBuffer);
  }
  
  private final static float[] fact = new float[20];
  static {
    fact[0] = 1;
    for (int i = 1; i < 20; i++)
      fact[i] = fact[i - 1] * i;
  }

  private void calcFactors(int n, int el, int m) {
    int abm = Math.abs(m);
    double Nnl = Math.pow(2 * psi_Znuc / n / A0, 1.5)
        * Math.sqrt(fact[n - el - 1] / 2 / n / Math.pow(fact[n + el], 3));
    double Lnl = fact[n + el] * fact[n + el];
    double Plm = Math.pow(2, -el) * fact[el] * fact[el + abm]
        * Math.sqrt((2 * el + 1) * fact[el - abm] / 2 / fact[el + abm]);

    for (int p = 0; p <= n - el - 1; p++)
      rfactor[p] = Nnl * Lnl / fact[p] / fact[n - el - p - 1]
          / fact[2 * el + p + 1];
    for (int p = abm; p <= el; p++)
      pfactor[p] = Math.pow(-1, el - p) * Plm / fact[p] / fact[el + abm - p]
          / fact[el - p] / fact[p - abm];
  }

  private void autoScaleOrbital() {
    double min;
    if (params.cutoff == 0) {
      min = 0.01f;
    } else {
      min = Math.abs(params.cutoff / 2);
      // ISSQUARED means cutoff is in terms of psi*2, not psi
      if (params.isSquared)
        min = Math.sqrt(min / 2);
    }
    float r0 = 0;
    for (radius = 100; radius > 0; radius--) {
      double d = radialPart(radius);
      if (Math.abs(d) >= min && r0 == 0)
        r0 = radius;
    }
    radius = r0 + 1;
    if (isAnisotropic) {
      float aMax = 0;
      for (int i = 3; --i >= 0;)
        if (anisotropy[i] > aMax)
          aMax = anisotropy[i];
      radius *= aMax;
    }
    Logger.info("radial extent set to " + radius + " for cutoff "
        + params.cutoff);
  }

  private double radialPart(double r) {
    double rho = 2d * psi_Znuc * r / psi_n / A0;
    double sum = 0;
    for (int p = 0; p <= psi_n - psi_l - 1; p++)
      sum += Math.pow(-rho, p) * rfactor[p];
    return Math.exp(-rho / 2) * Math.pow(rho, psi_l) * sum;    
  }
  
  private double hydrogenAtomPsi(Point3f pt) {
    // ref: http://www.stolaf.edu/people/hansonr/imt/concept/schroed.pdf
    double x2y2 = pt.x * pt.x + pt.y * pt.y;
    double rnl = radialPart(Math.sqrt(x2y2 + pt.z * pt.z));    
    double ph = Math.atan2(pt.y, pt.x);
    double th = Math.atan2(Math.sqrt(x2y2), pt.z);
    double cth = Math.cos(th);
    double sth = Math.sin(th);
    int abm = Math.abs(psi_m);
    double sum = 0;
    for (int p = abm; p <= psi_l; p++)
      sum += Math.pow(1 + cth, p - abm) * Math.pow(1 - cth, psi_l - p)
          * pfactor[p];
    double theta_lm = Math.abs(Math.pow(sth, abm)) * sum;
    double phi_m;
    if (psi_m == 0)
      phi_m = 1;
    else if (psi_m > 0)
      phi_m = Math.cos(psi_m * ph) * ROOT2;
    else
      phi_m = Math.sin(-psi_m * ph) * ROOT2;
    if (Math.abs(phi_m) < 0.0000000001)
      phi_m = 0;
    return rnl * theta_lm * phi_m;
  }

  private boolean monteCarloDone;
  
  private void createMonteCarloOrbital() {
    if (monteCarloDone)
      return;
    monteCarloDone = true;
    float value;
    float f = 0;
    float d = radius * 2;
    if (params.thePlane != null)
      vTemp = new Vector3f();

    for (int i = 0; i < 1000; i++) {
      setRandomPoint(d);
      value = (float) Math.abs(hydrogenAtomPsi(ptPsi));
      if (value > f)
        f = value;
    }
    if (f < 0.01f) // must be a node
      return;
    for (int i = 0; i < monteCarloCount;) {
      setRandomPoint(d);
      ptPsi.add(center);
      value = getValueAtPoint(ptPsi);
      if (Math.abs(value) <= f * random.nextFloat())
        continue;
      addVertexCopy(ptPsi, value, 0);
      i++;
    }
  }

  private Vector3f vTemp;
  
  private void setRandomPoint(float x) {
    ptPsi.x = (random.nextFloat() - 0.5f) * x;
    ptPsi.y = (random.nextFloat() - 0.5f) * x;
    ptPsi.z = (random.nextFloat() - 0.5f) * x;
    if (params.thePlane != null)
      Measure.getPlaneProjection(ptPsi, params.thePlane, ptPsi, vTemp);
  }

  @Override
  protected void readSurfaceData(boolean isMapData) throws Exception {
    switch (params.dataType) {
    case Parameters.SURFACE_ATOMICORBITAL:
      if (monteCarloCount <= 0)
        break;
      createMonteCarloOrbital();
      return;
    case Parameters.SURFACE_LONEPAIR:
    case Parameters.SURFACE_RADICAL:
      ptPsi.set(0, 0, eccentricityScale / 2);
      eccentricityMatrixInverse.transform(ptPsi);
      ptPsi.add(center);
      addVertexCopy(center, 0, 0);
      addVertexCopy(ptPsi, 0, 0);
      addTriangleCheck(0, 0, 0, 0, 0, false, 0);
      return;
    }
    super.readSurfaceData(isMapData);
  }

}
