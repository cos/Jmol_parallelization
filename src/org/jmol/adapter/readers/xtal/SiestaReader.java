package org.jmol.adapter.readers.xtal;

import org.jmol.adapter.smarter.Atom;
import org.jmol.adapter.smarter.AtomSetCollectionReader;


/**
 * SIESTA
 * http://www.icmab.es/siesta/
 * @author Pieremanuele Canepa, Room 104, FM Group School of Physical Sciences,
 *         Ingram Building, University of Kent, Canterbury, Kent, CT2 7NH United
 *         Kingdom, pc229@kent.ac.uk
 * 
 * @version 1.0
 */

public class SiestaReader extends AtomSetCollectionReader {

  //private boolean geomMod = false;
  private int noAtoms;

  @Override
  protected void initializeReader() {
    setSpaceGroupName("P1");
    setFractionalCoordinates(false);
  }

  @Override
  protected boolean checkLine() throws Exception {

    if (line.contains("%block LatticeVectors")) {
      //geomMod = false;
      setCell();
      return true;
    } else if (line.contains("AtomicCoordinatesFormat Ang")) {
      readAtomsCartesian();
      return true;
    } else if (line.contains("NumberOfAtoms")){
      readNoAtoms();
      //geomMod = true;
      return true;
    } else if (line.contains("Begin CG move =")) {
      setNewmodel();
      return true;
    } else if (line.contains("outcell: Unit cell vectors")) {
      setCell();
      return true;
    } else if (line.contains("siesta: E_KS(eV) = ")) {
      setModelName();
      return true;
    }
    return true;
  }

  private void setNewmodel() throws Exception {
    discardLinesUntilContains("outcoor: Atomic coordinates");
      readAtomsCartGeom();
  }

  private float[] unitCellData = new float[9];

  private void setCell() throws Exception {
    setSymmetry();
    fillFloatArray(unitCellData, null, 0);
    readUnitCellVectors();
  }

  /*  
    LatticeConstant    1.00000 Ang
    %block LatticeVectors
      14.662154    0.084061    0.000000
       0.074910   14.590447    0.000000
       0.000000    0.000000   40.000000
    %endblock LatticeVectors
   */
  private void readUnitCellVectors() throws Exception {
    addPrimitiveLatticeVector(0, unitCellData, 0);
    addPrimitiveLatticeVector(1, unitCellData, 3);
    addPrimitiveLatticeVector(2, unitCellData, 6);
  }

  private void setSymmetry() throws Exception {
    applySymmetryAndSetTrajectory();
    setSpaceGroupName("P1");
    setFractionalCoordinates(false);
  }
  
  
  private void readNoAtoms() {
    String[] tokens = getTokens();
    noAtoms = parseInt(tokens[1]);
  }

  /*  
    AtomicCoordinatesFormat Ang
    %block AtomicCoordinatesAndAtomicSpecies
         6.15000000     7.17000000    15.47800000    2    C    1
         5.92900000     8.49200000    15.02300000    2    C    2
         5.89900000     9.54900000    15.94800000    2    C    3
         6.08300000     9.29200000    17.31300000    2    C    4
         6.34400000     7.98900000    17.76600100    2    C    5
         6.38200000     6.93200000    16.84400000    2    C    6
         5.72400000     8.78800000    13.70100000    3    O    7
         6.10300000     6.10500000    14.59100000    3    O    8
         5.98800000     8.04300000    13.09100000    1    H    9
   */
  private void readAtomsCartesian() throws Exception {
    discardLines(1);
    setFractionalCoordinates(false);
    atomSetCollection.newAtomSet();
    while (readLine() != null
        && line.indexOf("%endblock Atomic") < 0) {
      String[] tokens = getTokens();
      Atom atom = atomSetCollection.addNewAtom();
      atom.atomName = tokens[4];
      float x = parseFloat(tokens[0]);
      float y = parseFloat(tokens[1]);
      float z = parseFloat(tokens[2]);
      setAtomCoord(atom, x, y, z);
    }
    applySymmetryAndSetTrajectory();
  }
  
  private void readAtomsCartGeom() throws Exception {
    discardLines(1);
    setFractionalCoordinates(false);
   // atomSetCollection.newAtomSet();
    for (int i = 0; i < noAtoms; i++) {
      String[] tokens = getTokens();
      Atom atom = atomSetCollection.addNewAtom();
      atom.atomName = tokens[4];
      float x = parseFloat(tokens[0]);
      float y = parseFloat(tokens[1]);
      float z = parseFloat(tokens[2]);
      setAtomCoord(atom, x, y, z);
      readLine();
    }
    applySymmetryAndSetTrajectory();
  }
  

  private Double energy;

  // siesta: Etot    =    -66760.952146
  private void setModelName() throws Exception {
    String[] tokens = getTokens();
    energy = Double.valueOf(Double.parseDouble(tokens[3]));
    setEnergy();
  }

  private void setEnergy() {
    atomSetCollection.setAtomSetEnergy("" + energy, energy.floatValue());
    atomSetCollection.setAtomSetAuxiliaryInfo("Energy", energy);
    atomSetCollection.setAtomSetCollectionAuxiliaryInfo("Energy", energy);
    atomSetCollection.setAtomSetName("Energy = " + energy + " eV");
  }
}
