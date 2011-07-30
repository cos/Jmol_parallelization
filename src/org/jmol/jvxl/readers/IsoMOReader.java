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

import java.util.Map;
import java.util.Random;

import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.jmol.util.Logger;
import org.jmol.util.Measure;
import org.jmol.util.TextFormat;
import org.jmol.viewer.JmolConstants;
import org.jmol.api.Interface;
import org.jmol.api.QuantumCalculationInterface;
import org.jmol.api.QuantumPlaneCalculationInterface;

class IsoMOReader extends AtomDataReader {

  private Random random;
  
  IsoMOReader(SurfaceGenerator sg) {
    super(sg);
    isNci = (params.qmOrbitalType == Parameters.QM_TYPE_NCI_PRO);
    if (isNci) {
      // NCI analysis org.jmol.quantum.NciCalculation
      // allows for progressive plane reading, which requires
      // isXLowToHigh to be TRUE
      isXLowToHigh = hasColorData = true;
      precalculateVoxelData = false; // will process as planes
      params.insideOut = !params.insideOut;
    }
  }
  
  /////// ab initio/semiempirical quantum mechanical orbitals ///////

  @SuppressWarnings("unchecked")
  private void setup(boolean isMapData) {
    setup();
    doAddHydrogens = false;
    getAtoms(params.qm_marginAngstroms, true, false, params.bsSelected);
    if (isNci)
      setHeader("NCI (promolecular)", "see NCIPLOT: A Program for Plotting Noncovalent Interaction Regions, Julia Contreras-Garcia, et al., J. of Chemical Theory and Computation, 2011, 7, 625-632");
    else
      setHeader("MO", "calculation type: " + params.moData.get("calculationType"));
    setRangesAndAddAtoms(params.qm_ptsPerAngstrom, params.qm_gridMax, myAtomCount);
    String className = (isNci ? "quantum.NciCalculation" : "quantum.MOCalculation");
    q = (QuantumCalculationInterface) Interface.getOptionInterface(className);
    moData = params.moData;
    mos = (List<Map<String, Object>>) moData.get("mos");
    linearCombination = params.qm_moLinearCombination;
    if (isNci) {
      qpc = (QuantumPlaneCalculationInterface) q;
    } else if (linearCombination == null) {
      Map<String, Object> mo = mos.get(params.qm_moNumber - 1);
      for (int i = params.title.length; --i >= 0;)
        fixTitleLine(i, mo);
      coef = (float[]) mo.get("coefficients"); 
      dfCoefMaps = (int[][]) mo.get("dfCoefMaps");
    } else {
      coefs = new float[mos.size()][];
      for (int i = 1; i < linearCombination.length; i += 2) {
        int j = (int) linearCombination[i];
        if (j > mos.size() || j < 1)
          return;
        coefs[j - 1] = (float[]) mos.get(j - 1).get("coefficients");
      }
    }
    isElectronDensityCalc = (coef == null && linearCombination == null && !isNci);
    volumeData.sr = null;
    if (isMapData && !isElectronDensityCalc) {
      volumeData.sr = this;
      volumeData.doIterate = false;
      volumeData.voxelData = voxelData = new float[1][1][1];
      points = new Point3f[1];
      points[0] = new Point3f();
      if (!setupCalculation())
        q = null;
    } else if (params.psi_monteCarloCount > 0) {
      vertexDataOnly = true;
      random = new Random(params.randomSeed);
    }
    //if (isNci && params.thePlane == null)
      //  params.insideOut = !params.insideOut;
  }
  
  @Override
  protected boolean readVolumeParameters(boolean isMapData) {
    setup(isMapData);
    if (volumeData.sr == null)
      initializeVolumetricData();
    return true;
  }
  private void fixTitleLine(int iLine, Map<String, Object> mo) {
    // see Parameters.Java for defaults here. 
    if (!fixTitleLine(iLine))
       return;
    String line = params.title[iLine];
    int pt = line.indexOf("%");
    if (line.length() == 0 || pt < 0)
      return;
    int rep = 0;
    if (line.indexOf("%F") >= 0)
      line = TextFormat.formatString(line, "F", params.fileName);
    if (line.indexOf("%I") >= 0)
      line = TextFormat.formatString(line, "I", params.qm_moLinearCombination == null ? "" + params.qm_moNumber : JmolConstants.getMOString(params.qm_moLinearCombination));
    if (line.indexOf("%N") >= 0)
      line = TextFormat.formatString(line, "N", "" + params.qmOrbitalCount);
    if (line.indexOf("%E") >= 0)
      line = TextFormat.formatString(line, "E", mo.containsKey("energy") && ++rep != 0 ? "" + mo.get("energy") : "");
    if (line.indexOf("%U") >= 0)
      line = TextFormat.formatString(line, "U", params.moData.containsKey("energyUnits") && ++rep != 0 ? (String) params.moData.get("energyUnits") : "");
    if (line.indexOf("%S") >= 0)
      line = TextFormat.formatString(line, "S", mo.containsKey("symmetry") && ++rep != 0 ? "" + mo.get("symmetry") : "");
    if (line.indexOf("%O") >= 0)
      line = TextFormat.formatString(line, "O", mo.containsKey("occupancy") && ++rep != 0  ? "" + mo.get("occupancy") : "");
    if (line.indexOf("%T") >= 0)
      line = TextFormat.formatString(line, "T", mo.containsKey("type") && ++rep != 0  ? "" + mo.get("type") : "");
    boolean isOptional = (line.indexOf("?") == 0);
    params.title[iLine] = (!isOptional ? line : rep > 0 && !line.trim().endsWith("=") ? line.substring(1) : "");
  }
  
  private final float[] vDist = new float[3];
  
  @Override
  protected void readSurfaceData(boolean isMapData) throws Exception {
    if (volumeData.sr != null)
      return;
    if (params.psi_monteCarloCount <= 0) {
      super.readSurfaceData(isMapData);
      return;
    }
    if (points != null)
      return; // already done
    points = new Point3f[1000];
    for (int j = 0; j < 1000; j++)
      points[j] = new Point3f();
    if (params.thePlane != null)
      vTemp = new Vector3f();
    // presumes orthogonal
    for (int i = 0; i < 3; i++)
      vDist[i] = volumeData.volumetricVectorLengths[i]
          * volumeData.voxelCounts[i];
    volumeData.voxelData = voxelData = new float[1000][1][1];
    getValues();
    float value;
    float f = 0;
    for (int j = 0; j < 1000; j++)
      if ((value = Math.abs(voxelData[j][0][0])) > f)
        f = value;
    if (f < 0.0001f)
      return;
    //minMax = new float[] {(params.mappedDataMin  = -f / 2), 
    //(params.mappedDataMax = f / 2)};
    for (int i = 0; i < params.psi_monteCarloCount;) {
      getValues();
      for (int j = 0; j < 1000; j++) {
        value = voxelData[j][0][0];
        float absValue = Math.abs(value);
        if (absValue <= getRnd(f))
          continue;
        addVertexCopy(points[j], value, 0);
        if (++i == params.psi_monteCarloCount)
          break;
      }
    }
  }

  private void getValues() {        
    for (int j = 0; j < 1000; j++) {
      voxelData[j][0][0] = 0;
      points[j].set(volumeData.volumetricOrigin.x + getRnd(vDist[0]), 
          volumeData.volumetricOrigin.y + getRnd(vDist[1]), 
          volumeData.volumetricOrigin.z + getRnd(vDist[2]));
      if (params.thePlane != null)
        Measure.getPlaneProjection(points[j], params.thePlane, points[j], vTemp);
    }
    createOrbital();
  }

  @Override
  public float getValueAtPoint(Point3f pt) {
    return (q == null ? 0 : q.process(pt));
  }
  

  private float getRnd(float f) {
    return random.nextFloat() * f;
  }

  //mapping mos fails
  
  @Override
  protected void generateCube() {
    volumeData.voxelData = voxelData = new float[nPointsX][nPointsY][nPointsZ];
    createOrbital();
  }

  private Point3f[] points;
  private Vector3f vTemp;
  QuantumCalculationInterface q;
  Map<String, Object> moData;
  List<Map<String, Object>> mos;
  boolean isNci;
  float[] coef; 
  int[][] dfCoefMaps;
  float[] linearCombination;
  float[][] coefs;
  private boolean isElectronDensityCalc;
  
  protected void createOrbital() {
    boolean isMonteCarlo = (params.psi_monteCarloCount > 0);
    if (isElectronDensityCalc) {
      // electron density calc
      if (mos == null || isMonteCarlo)
        return;
      for (int i = params.qm_moNumber; --i >= 0; ) {
        Logger.info(" generating isosurface data for MO " + (i + 1));
        Map<String, Object> mo = mos.get(i);
        coef = (float[]) mo.get("coefficients");
        dfCoefMaps = (int[][]) mo.get("dfCoefMaps");
        if (!setupCalculation())
          return;
        q.createCube();
      }
    } else {
      if (!isMonteCarlo)
        Logger.info("generating isosurface data for MO using cutoff " + params.cutoff);
      if (!setupCalculation())
        return;
      q.createCube();
    }
  }
  
  @Override
  public float[] getPlane(int x) {
    if (!qSetupDone) 
      setupCalculation();
    return super.getPlane(x); 
  }

  private boolean qSetupDone;
  
  @SuppressWarnings("unchecked")
  private boolean setupCalculation() {
    qSetupDone = true;
    switch (params.qmOrbitalType) {
    case Parameters.QM_TYPE_GAUSSIAN:
      return q.setupCalculation(volumeData, bsMySelected, (String) moData
          .get("calculationType"), atomData.atomXyz, atomData.firstAtomIndex,
          (List<int[]>) moData.get("shells"), (float[][]) moData
              .get("gaussians"), dfCoefMaps, null, coef, linearCombination,
          coefs, params.theProperty, moData.get("isNormalized") == null, points,
          params.parameters);
    case Parameters.QM_TYPE_SLATER:
      return q.setupCalculation(volumeData, bsMySelected, (String) moData
          .get("calculationType"), atomData.atomXyz, atomData.firstAtomIndex,
          null, null, null, moData.get("slaters"), coef, linearCombination,
          coefs, params.theProperty, true, points, params.parameters);
    case Parameters.QM_TYPE_NCI_PRO:
      return q.setupCalculation(volumeData, bsMySelected, null,
          atomData.atomXyz, atomData.firstAtomIndex, null, null, null, null,
          null, null, null, params.theProperty, true, points,
          params.parameters);
    }
    return false;
  }
  
  @Override
  protected float getSurfacePointAndFraction(float cutoff,
                                             boolean isCutoffAbsolute,
                                             float valueA, float valueB,
                                             Point3f pointA,
                                             Vector3f edgeVector, int x, int y,
                                             int z, int vA, int vB,
                                             float[] fReturn, Point3f ptReturn) {
      
      float zero = super.getSurfacePointAndFraction(cutoff, isCutoffAbsolute, valueA,
          valueB, pointA, edgeVector, x, y, z, vA, vB, fReturn, ptReturn);
      return (q == null || Float.isNaN(zero) ? zero : q.process(ptReturn)); 
  }
}
