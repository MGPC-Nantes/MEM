/*Copyright (C) 2019  Guillaume Herbreteau

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU LesserGeneral Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

package com.clcbio.sdk.msianalysis.power;

import java.io.Serializable;

import com.clcbio.api.base.algorithm.Algo;
import com.clcbio.api.base.algorithm.AlgoException;
import com.clcbio.api.base.algorithm.AlgoHistoryTools;
import com.clcbio.api.base.algorithm.CallableExecutor;
import com.clcbio.api.base.algorithm.ChannelDescription;
import com.clcbio.api.base.algorithm.Multiplicity;
import com.clcbio.api.base.algorithm.OutputChannel;
import com.clcbio.api.base.algorithm.OutputHandler;
import com.clcbio.api.base.algorithm.parameter.AlgoParameters;
import com.clcbio.api.base.algorithm.parameter.AlgoParametersInterpreter;
import com.clcbio.api.base.session.ApplicationContext;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.GeneralClcTabular;
import com.clcbio.api.free.datatypes.framework.history.HistoryEntry;
import com.clcbio.sdk.msianalysis.PList;

public class MsiPowerAlgo extends Algo {
    public static final ChannelDescription<GeneralClcTabular> INPUT =
            new ChannelDescription<GeneralClcTabular>("MSI table", "MSI table", GeneralClcTabular.class , "input_reads", Multiplicity.ONE);
    public static final ChannelDescription<GeneralClcTabular> OUTPUT =
            new ChannelDescription<GeneralClcTabular>("MSI power", "MSI power", GeneralClcTabular.class , "output_table", Multiplicity.ONE);
   
    public MsiPowerAlgo(ApplicationContext applicationContext) {
        super(applicationContext);
        addInputChannel(INPUT.createDefaultInputChannel());
        addOutputChannel(OUTPUT.createDefaultOutputChannel());
    }

    @Override
    protected AlgoParametersInterpreter getInterpreter(AlgoParameters parameters) {
        return new MsiPowerParameters(parameters);
    }
        
    @Override
    public String getName() {
        return "MEM statistical power calculation";
    }
    
    @Override
    public String getClassKey() {
        return "MEM statistical power calculation";
    }
    
    @Override
    public double getVersion() {
        return 3.0;
    }
    
    @Override
    public void calculate(OutputHandler handler, CallableExecutor objectModificationExecutor) throws AlgoException,
    InterruptedException {
		MsiPowerParameters p = new MsiPowerParameters(getParameters());
		
		int stdRange = 60;   
		int nbSeq = 0;
		Serializable newRow[] =  new Serializable[9];
		GeneralClcTabular.Builder tableBuilder = GeneralClcTabular.createBuilder("MSI Table", "", new String[] {"Name","NbSeq","NbRepet","NbPos","pPos","pInstable","TheoSize","Shiftmin","SizeInstable"});
		
		for(ClcObject o : getInputObjectsIteratorProvider()) {
			String mSatName = (String) o.getName();
			double pInstable = 0;
			newRow[0] = mSatName;
		 	double threshold = p.threshold.get();
			
		 	GeneralClcTabular table = (GeneralClcTabular) o;
			
			int sum = 0;
			double theoSize = 0;
			boolean tableExiste = false;
			PList stdP = new PList(stdRange);
			if(table.getColumnCount() == 2 && table.getColumnName(0).contentEquals("size") && table.getColumnName(1).contentEquals("p")) {
				tableExiste = true;
				for(int x = 0; x < table.getRowCount(); x++) {
					stdP.set(x, (double) table.getValueAt(x, 1));
					theoSize += x * stdP.get(x);	    				
				}
			}
			
			if(tableExiste) {
			    newRow[6] = theoSize;
			    
			    double shiftMin = 0;
			    double shiftMax = 0;
			    shiftMax = theoSize / 10;
			    shiftMin = -theoSize / 10;
			    newRow[7] = shiftMin;
					
			    for(int j = 0 ; j < 3 ; j++) {
			    	if(j == 0) pInstable = 0.05;
			    	if(j == 1) pInstable = 0.1;
			    	if(j == 2) pInstable = 0.25;
	
					newRow[5] = pInstable;
			    	
					//Définition du modèle de mélange
					PList modele = new PList(stdRange);
					modele = stdP.mixtureModel(theoSize, theoSize, theoSize + 2* shiftMin, 1-pInstable);
				    newRow[8] = theoSize + 2 * shiftMin;
					
					nbSeq = 0;
					do {
			    		//Incrément de taille
			    		if(nbSeq <= 100) nbSeq +=10;
			    		else if (nbSeq <= 500) nbSeq +=50;
			    		else if (nbSeq <= 1000) nbSeq +=100;
			    		else if (nbSeq <= 5000) nbSeq +=500;
			    		newRow[1] = nbSeq;
			    		
			    		int nbPos = 0;
			    		int nbRepet = 100;
			    		newRow[2] = nbRepet;
			    		for(int repet = 0 ; repet < nbRepet; repet++) {
				    		//Randomiser
			    			sum = 0;
				    		int mSatN[] = new int[stdRange];
				    		for(int i = 0; i < nbSeq; i++) {
				    			mSatN[modele.invert(Math.random())]++; 
				    			sum++;
				    		}
			
			    			double mSatP[] = new double[stdRange];
				    		for(int x = 0; x < stdRange; x++) {
				    			mSatP[x] = (double) mSatN[x] / (double) sum;
				    		}
				
				    		//Algorithme EM
				    		double size1 = theoSize + 2;
				    		double size2 = theoSize - 3;
				    		double size3 = theoSize - 10;
				    		double proportion1 = 0.34;
				    		double proportion2 = 0.33;
				    		double proportion3 = 0.33;
				    		double p1 = 0;
				    		double p2 = 0;
				    		double p3 = 0;
				    		double w1 = 0;
				    		double w2 = 0;
				    		double w3 = 0;
				    		double sum1 = 0;
				    		double sum2 = 0;
				    		double sum3 = 0;
				    		double sumX1 = 0;
				    		double sumX2 = 0;
				    		double sumX3 = 0;
				    		double logLikelihood = -9999999;
				    		int compteur = 0;
				    			    		
				    		do {
					    		sum1 = 0;
					    		sum2 = 0;
					    		sum3 = 0;
					    		sumX1 = 0;
					    		sumX2 = 0;
					    		sumX3 = 0;
					    		logLikelihood = stdP.mixtureModel(theoSize, size1, size2, size3, proportion1, proportion2).logLikelihood(mSatN);
				    			
					    		for(int x = 0 ; x < stdRange ; x++) {
					    			p1 = stdP.shift(theoSize, size1).get(x) * proportion1;
					    			p2 = stdP.shift(theoSize, size2).get(x) * proportion2;
					    			p3 = stdP.shift(theoSize, size3).get(x) * proportion3;
					    			
					    			w1 = p1 / (p1 + p2 + p3);
					    			w2 = p2 / (p1 + p2 + p3);
					    			w3 = p3 / (p1 + p2 + p3);
					    			
					    			sum1 += w1 * (double) mSatN[x];
					    			sum2 += w2 * (double) mSatN[x];
					    			sum3 += w3 * (double) mSatN[x];
	
					    			sumX1 += w1 * (double) mSatN[x] * (double) x;
					    			sumX2 += w2 * (double) mSatN[x] * (double) x;
					    			sumX3 += w3 * (double) mSatN[x] * (double) x;	    			
					    		}
					    				    		
					    		proportion1 = sum1 / (double) sum;
					    		proportion2 = sum2 / (double) sum;
					    		proportion3 = sum3 / (double) sum;
					    
					    		size1 = sumX1 / sum1;
					    		size2 = sumX2 / sum2;
					    		size3 = sumX3 / sum3;
					    		
					    		compteur++;
				    				    				    		
				    		} while(((stdP.mixtureModel(theoSize, size1, size2, size3, proportion1, proportion2).logLikelihood(mSatN) - logLikelihood) > 0.0001) || (compteur < 100));
			
					    	double stableProportion = 0;
					    	double stableProportion1 = 0;
					    	double stableProportion2 = 0;
					    	double stableProportion3 = 0;
					    	boolean fraction1Instable = true;
					    	boolean fraction2Instable = true;
					    	boolean fraction3Instable = true;
					    					
					    	if((size1 - theoSize) > shiftMin && (size1 - theoSize) < shiftMax) {
					    		stableProportion1 = proportion1;
					    		fraction1Instable = false;
					    	}
					    	if((size2 - theoSize) > shiftMin && (size2 - theoSize) < shiftMax) {
					    		stableProportion2 = proportion2;
					    		fraction2Instable = false;
					    	}
					    	if((size3 - theoSize) > shiftMin && (size3 - theoSize) < shiftMax) {
					    		stableProportion3 = proportion3;
					    		fraction3Instable = false;
					    	}
							
					    	stableProportion = stableProportion1 + stableProportion2 + stableProportion3;
					    	
							PList fraction1 = stdP.shift(theoSize, size1);
							PList fraction2 = stdP.shift(theoSize, size2);
							PList fraction3 = stdP.shift(theoSize, size3);
							PList mixSsInstable = new PList(stdRange);
							PList mixP = new PList(stdRange);
							for(int x = 0; x < stdRange; x++) {
								mixSsInstable.set(x, fraction1.get(x) * stableProportion1 / stableProportion 
										+ fraction2.get(x) * stableProportion2 / stableProportion
										+ fraction3.get(x) * stableProportion3/ stableProportion);
								mixP.set(x, fraction1.get(x) * proportion1 + fraction2.get(x) * proportion2 + fraction3.get(x) * proportion3);
				    		}
							
							double logLikelihoodRatio = 0;
					        double vafMin = 0.05;
					        boolean instable = false;
					        
							logLikelihoodRatio = Math.max(0, -2 * (mixSsInstable.logLikelihood(mSatN) - mixP.logLikelihood(mSatN)));
			
							if(!fraction1Instable && !fraction2Instable && !fraction3Instable) {
								//Si aucune fraction en dehors de du range stable
									instable = false;
								}
								else if(1 - stableProportion < vafMin) {
								//Si fractions en dehors de du range stable représentent < 5%
									instable = false;
								}
								else if(logLikelihoodRatio < threshold) {
								//Sinon loglikelihood ratio test
									instable = false;
								}
								else {
									instable = true;
								}
					    	
							if(instable) nbPos++;
			    		}
			    		newRow[3] = nbPos;
			    		newRow[4] = (double) nbPos / (double) nbRepet;
			    		tableBuilder.addRow(newRow);
					}while(nbSeq < 2000);
			    }
			}
		}
		
		final GeneralClcTabular tableOut = tableBuilder.finish();
		
		tableOut.getUndoManager().discardAllEdits();
		tableOut.startNoUndoBlock();
		HistoryEntry entry2 = AlgoHistoryTools.createEnrichedEntry(tableOut, this);
		tableOut.addHistory(entry2);
		tableOut.setName("MEM statistical power calculation");
		tableOut.endNoUndoBlock();
		
		handler.postOutputObjects(tableOut, this);
		
		OutputChannel<GeneralClcTabular> channel2 = getOutputChannel(OUTPUT);
		channel2.startPosting();
		channel2.postOutputObject(tableOut);
		channel2.endPosting();
		}
}
