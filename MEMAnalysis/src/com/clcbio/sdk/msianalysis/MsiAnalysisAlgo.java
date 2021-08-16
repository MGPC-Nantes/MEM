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

package com.clcbio.sdk.msianalysis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;

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
import com.clcbio.api.clc.datatypes.graph.AssociativeDataSeries;
import com.clcbio.api.clc.datatypes.graph.FunctionPlotImpl;
import com.clcbio.api.clc.datatypes.report.ReportCompositeElement;
import com.clcbio.api.clc.datatypes.report.ReportGraphicsElement;
import com.clcbio.api.clc.datatypes.report.ReportTextElement;
import com.clcbio.api.clc.datatypes.report.SimpleReport;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.GeneralClcTabular;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.list.NucleotideSequenceList;
import com.clcbio.api.free.datatypes.framework.history.HistoryEntry;
import com.clcbio.api.free.datatypes.report.Report;

public class MsiAnalysisAlgo extends Algo {
    public static final ChannelDescription<NucleotideSequenceList> INPUT =
            new ChannelDescription<NucleotideSequenceList>("Unmapped reads", "Unmapped annotated reads", NucleotideSequenceList.class , "input_reads", Multiplicity.AT_LEAST_ONE);
    public static final ChannelDescription<Report> REPORTOUTPUT =
            new ChannelDescription<Report>("MSI report", "MSI report", Report.class , "output_report", Multiplicity.ONE);
        
    public MsiAnalysisAlgo(ApplicationContext applicationContext) {
        super(applicationContext);
        addInputChannel(INPUT.createDefaultInputChannel());
        addOutputChannel(REPORTOUTPUT.createDefaultOutputChannel());
    }
    
    @Override
    protected AlgoParametersInterpreter getInterpreter(AlgoParameters parameters) {
        return new MsiAnalysisParameters(parameters);
    }
    
    @Override
    public String getName() {
        return "MEM Algorithm";
    }
    
    @Override
    public String getClassKey() {
        return "MEM Algorithm";
    }
    
    @Override
    public double getVersion() {
        return 3.0;
    }
    
    @Override
    public void calculate(OutputHandler handler, CallableExecutor objectModificationExecutor) throws AlgoException,
            InterruptedException {
    	MsiAnalysisParameters p = new MsiAnalysisParameters(getParameters());
        
		int stdRange = 60;    	
    	int nbMSat = 0;
    	int nbMSatInst = 0;
    	int nbMSatNC = 0;
    	String titre = new String();
    	String resume = new String();
    	String newLine = System.getProperty("line.separator");
    	ArrayList<ReportCompositeElement> reportList = new ArrayList<ReportCompositeElement>();

    	for(ClcObject o : getInputObjectsIteratorProvider()) {
    		nbMSat++;
    		boolean instable = false;
    		boolean nonContributif = false;
    		NucleotideSequenceList mSatSeqList = (NucleotideSequenceList) o;
    		String mSatName = (String) mSatSeqList.getAnnotation("name");
    	 	double threshold = (double) mSatSeqList.getAnnotation("threshold");   		
    		boolean tableExiste = false;
    		GeneralClcTabular table = null;
    		List<ClcObject> tableList = (List<ClcObject>) p.table.getClcObjects(getApplicationContext());
    		for(ClcObject obj : tableList) {
    			if(obj.getName().indexOf(mSatName) != -1) {
    				table = (GeneralClcTabular) obj;
    				tableExiste = true;
    			}
    		}
    		    		
    		if(tableExiste) {
	    		Iterator<Sequence> itr = mSatSeqList.asListOfSequences().iterator();
	    		int mSatN[] = new int[stdRange];
	    		int sum = 0;
	    		double mSatP[] = new double[stdRange];
	    		while(itr.hasNext()) {
	    			Sequence seq = (Sequence) itr.next();
	    			if (seq.getLength() >= 0 && seq.getLength() < mSatN.length) {
	    				mSatN[seq.getLength()]++; 
	    				sum++;
	    			}
	    		}
	    		for(int x = 0; x < stdRange; x++) {
	    			mSatP[x] = (double) mSatN[x] / (double) sum;
	    		}

	    		double theoSize = 0;
				PList stdP = new PList(stdRange);
	    		if(table.getColumnCount() == 2 && table.getColumnName(0).contentEquals("size") && table.getColumnName(1).contentEquals("p")) {
	    			for(int x = 0; x < table.getRowCount(); x++) {
	    				stdP.set(x, (double) table.getValueAt(x, 1));
	    				theoSize += x * stdP.get(x);	    				
	    			}
	    		}
	    		
	    		double power = 0;
	    		double meanPow = (double) mSatSeqList.getAnnotation("meanPow");
	    		double stdDevPow = (double) mSatSeqList.getAnnotation("stdDevPow");
	    		NormalDistribution norm = new NormalDistribution(meanPow, stdDevPow);
	    		power = norm.cumulativeProbability(sum);
	
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
		    	
		    	double shiftMin = (double) -theoSize / 10;//(double) mSatSeqList.getAnnotation("shiftMin");
		    	double shiftMax = (double) theoSize / 10;//(double) mSatSeqList.getAnnotation("shiftMax");
				
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
				PList stableP = new PList(stdRange);
				PList mixSsInstable = new PList(stdRange);
				PList mixP = new PList(stdRange);
		        String xValues[] = new String[stdRange];
		        double yValues[] = new double[stdRange];
		        double stableYValues[] = new double[stdRange];
		        double mixYValues[] = new double[stdRange];
				for(int x = 0; x < stdRange; x++) {
					xValues[x] = x+"";
					yValues[x] = mSatP[x];
					stableP.set(x, fraction1.get(x) * stableProportion1 + fraction2.get(x) * stableProportion2 + fraction3.get(x) * stableProportion3);
					stableYValues[x] = stableP.get(x);
					mixSsInstable.set(x, fraction1.get(x) * stableProportion1 / stableProportion 
							+ fraction2.get(x) * stableProportion2 / stableProportion
							+ fraction3.get(x) * stableProportion3/ stableProportion);
					mixP.set(x, fraction1.get(x) * proportion1 + fraction2.get(x) * proportion2 + fraction3.get(x) * proportion3);
					mixYValues[x] = mixP.get(x);
	    		}
				
				double logLikelihoodRatio = 0;
				String mSatText = new String();
		        DecimalFormat df = new DecimalFormat("#.#");
		        int nbMinimalReads = 100;
		        double vafMin = 0.02;

		        logLikelihoodRatio = Math.max(0, -2 * (mixSsInstable.logLikelihood(mSatN) - mixP.logLikelihood(mSatN)));
		        
				mSatText = "Coverage : " + sum + " reads (power: " + df.format(power * 100) + " %)"  + newLine + newLine + "Log-likelihood ratio = " + df.format(logLikelihoodRatio) + " (threshold : " + threshold + ")" + newLine + "Sub-dsitributions : " + df.format(size1 - theoSize) + "bp (" + df.format((double) proportion1*100) + "%), "+ df.format(size2 - theoSize) + "bp (" + df.format((double) proportion2*100) + "%), "+ df.format(size3 - theoSize) + "bp (" + df.format((double) proportion3*100) + "%)" + newLine + "Estimated unstable allele fraction : " + df.format((1 - (double) stableProportion)*100) + " %";
								
				if(sum < nbMinimalReads) {
					nonContributif = true;
					nbMSat--;
					nbMSatNC++;
					mSatText = mSatText + newLine + newLine + "Less than " + nbMinimalReads + " reads";
				}
				else {
					if(!fraction1Instable && !fraction2Instable && !fraction3Instable) {
					//Si aucune fraction en dehors de du range stable
						instable = false;
						if(power < 0.8) {
							nonContributif = true;
							nbMSat--;
							nbMSatNC++;
							mSatText = mSatText + newLine + newLine + "Mean of all sub-distributions between " + df.format(shiftMin) + "bp and " + df.format(shiftMax) + "bp relative to reference mean" + newLine + "Unsufficient statistical power (<80%)";
						}
						else {
							nonContributif = false;
							mSatText = mSatText + newLine + newLine + "Mean of all sub-distributions between " + df.format(shiftMin) + "bp and " + df.format(shiftMax) + "bp relative to reference mean";
						}
					}
					else if(1 - stableProportion < vafMin) {
					//Si fractions en dehors de du range stable représentent < 5%
						instable = false;
						if(power < 0.8) {
							nonContributif = true;
							nbMSat--;
							nbMSatNC++;
							mSatText = mSatText + newLine + newLine + "Fraction of potentially unstable alleles <" + df.format((double) vafMin*100) + "%" + newLine + "Unsufficient statistical power (<80%)";
						}
						else {
							nonContributif = false;
							mSatText = mSatText + newLine + newLine + "Fraction of potentially unstable alleles <" + df.format((double) vafMin*100) + "%";
						}
					}
					else if(logLikelihoodRatio < threshold) {
					//Sinon loglikelihood ratio test
						instable = false;
						if(power < 0.8) {
							nonContributif = true;
							nbMSat--;
							nbMSatNC++;
							mSatText = mSatText + newLine + newLine + "Negative log-likelihood ratio test" + newLine + "Unsufficient statistical power (<80%)";
						}
						else {
							nonContributif = false;
							mSatText = mSatText + newLine + newLine + "Negative log-likelihood ratio test";
						}
					}
					else {
						instable = true;
						nonContributif = false;
						nbMSatInst++;
						if(resume.isEmpty()) resume = "Unstable microsatellites: " + mSatName;
						else resume = resume + ", " + mSatName;
					}
				}
  
				ReportCompositeElement reportSection = new ReportCompositeElement() ;
				ReportTextElement reportMSatText = new ReportTextElement(mSatText) ;
				
				if(instable) {
					reportSection.setCaption(mSatName + ": UNSTABLE");
				}
				else if(nonContributif){
					reportSection.setCaption(mSatName + ": NON-CONTRIBUTORY");
				}
				else {
					reportSection.setCaption(mSatName + ": stable");
				}
				
				reportSection.addReportElement(reportMSatText);

		        FunctionPlotImpl histogram = new FunctionPlotImpl("Observed distribution for " + mSatName);
		        histogram.setHistogram(true);
		        FunctionPlotImpl curve = new FunctionPlotImpl(mSatName + "Sub-distributions");
		        curve.setHistogram(false);

    	        histogram.addData(new AssociativeDataSeries("Observed distribution for " + mSatName, "Length", "Allelic fraction", 0, 1, yValues, xValues, null));
    	        curve.addData(new AssociativeDataSeries("Observed distribution for " + mSatName, "Length", "Allelic fraction", 0, 1, mixYValues, xValues, null));
    	        if(instable) {
    	        	curve.addData(new AssociativeDataSeries("Stable sub-distribution of " + mSatName, "Length", "Allelic fraction", 0, 1, stableYValues, xValues, null));
    	        }
    	        else {
    	        	curve.addData(new AssociativeDataSeries("Stable sub-distribution of " + mSatName, "Length", "Allelic fraction", 0, 1, mixYValues, xValues, null));
    	        }

		        ReportGraphicsElement reportHistogram = new ReportGraphicsElement(histogram);
		        reportSection.addReportElement(reportHistogram);
		        if(!nonContributif) {
			        ReportGraphicsElement reportCurve = new ReportGraphicsElement(curve);
			        reportSection.addReportElement(reportCurve);
		        }

		        reportList.add(reportSection);
    		}
    		else {
		        ReportCompositeElement reportSection = new ReportCompositeElement() ;
		        reportSection.setCaption("."+mSatName+ ": no reference table");

		        reportList.add(reportSection);
    		}
    	}

        ReportCompositeElement reportSection = new ReportCompositeElement() ;
        ReportTextElement textResume = new ReportTextElement();
        
    	if((double) nbMSatInst / (double) nbMSat >= 0.4 && nbMSatInst >= ((double) nbMSat + (double) nbMSatNC) * 0.4) {
			titre = "MICROSATELLITE INSTABILITY : MSI phenoype (" + nbMSatInst + "/" + nbMSat + ")";
			textResume.setContent(resume);
			reportSection.addReportElement(textResume);
    	}
    	else if(nbMSatNC > 0){
    		if(((double) nbMSatInst + nbMSatNC) / ((double) nbMSat + nbMSatNC) >= 0.4) {
    			titre = "NON-CONTRIBUTORY ANALYSIS (" + nbMSatNC + " NC/" + (nbMSat + nbMSatNC) + ", MSI cannot be excluded)";
    		}
    		else {
    			titre = "MSS phenotype (" + nbMSatInst + "/" + nbMSat + ", " + nbMSatNC + " NC, without impact on the result)";
    		}
    	}
    	else {	
			titre = "MSS phenotype (" + nbMSatInst + "/" + nbMSat + ")";
    	}
        reportSection.setCaption(titre);
        reportList.add(0, reportSection);
    	
		Report report = new SimpleReport(reportList, "MSI Report");
		
		report.getUndoManager().discardAllEdits();
		report.startNoUndoBlock();
		HistoryEntry entry = AlgoHistoryTools.createEnrichedEntry(report, this);
		report.addHistory(entry);
		report.setName("MSI report");
		report.endNoUndoBlock();

        handler.postOutputObjects(report, this);    	
        
        OutputChannel<Report> channel = getOutputChannel(REPORTOUTPUT);
        channel.startPosting();
        channel.postOutputObject(report);
        channel.endPosting();
    }
}
