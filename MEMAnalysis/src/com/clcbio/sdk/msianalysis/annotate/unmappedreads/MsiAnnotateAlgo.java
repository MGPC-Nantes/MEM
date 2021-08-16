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

package com.clcbio.sdk.msianalysis.annotate.unmappedreads;

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
import com.clcbio.api.free.datatypes.bioinformatics.sequence.list.NucleotideSequenceList;
import com.clcbio.api.free.datatypes.framework.history.HistoryEntry;

public class MsiAnnotateAlgo extends Algo {
    public static final ChannelDescription<NucleotideSequenceList> INPUT =
            new ChannelDescription<NucleotideSequenceList>("Unmapped reads", "Unmapped reads", NucleotideSequenceList.class , "input_seqlist", Multiplicity.ONE);
    public static final ChannelDescription<NucleotideSequenceList> OUTPUT =
            new ChannelDescription<NucleotideSequenceList>("Microsatellite reads", "Microsatellite reads", NucleotideSequenceList.class , "ouput_seqlist", Multiplicity.ONE);
        
    public MsiAnnotateAlgo(ApplicationContext applicationContext) {
        super(applicationContext);
        addInputChannel(INPUT.createDefaultInputChannel());
        addOutputChannel(OUTPUT.createDefaultOutputChannel());
    }
    
    @Override
    protected AlgoParametersInterpreter getInterpreter(AlgoParameters parameters) {
        return new MsiAnnotateParameters(parameters);
    }
    
    @Override
    public String getName() {
        return "MEM Annotate Microsatellite Reads";
    }
    
    @Override
    public String getClassKey() {
        return "MEM Annotate Microsatellite Reads";
    }
    
    @Override
    public double getVersion() {
        return 2.4;
    }
    
    @Override
    public void calculate(OutputHandler handler, CallableExecutor objectModificationExecutor) throws AlgoException,
            InterruptedException {
    	MsiAnnotateParameters p = new MsiAnnotateParameters(getParameters());
    	for(ClcObject o : getInputObjectsIteratorProvider()) {
    		NucleotideSequenceList seqList = (NucleotideSequenceList) o;
    		seqList.addAnnotation("name", p.name.get());
    		//seqList.addAnnotation("shiftMin", p.shiftMin.get());
    		//seqList.addAnnotation("shiftMax", p.shiftMax.get());
    		seqList.addAnnotation("threshold", p.threshold.get());
    		seqList.addAnnotation("meanPow", p.meanPow.get());
    		seqList.addAnnotation("stdDevPow", p.stdDevPow.get());
    		
    		seqList.getUndoManager().discardAllEdits();
    		seqList.startNoUndoBlock();
	        final HistoryEntry entry = AlgoHistoryTools.createEnrichedEntry(seqList, this);
	        seqList.addHistory(entry);
	        seqList.endNoUndoBlock();
	        
	        handler.postOutputObjects(seqList, this);
	        
	        OutputChannel<NucleotideSequenceList> channel = getOutputChannel(OUTPUT);
	        channel.startPosting();
	        channel.postOutputObject((NucleotideSequenceList) seqList);
	        channel.endPosting();
    	}
    	
    }
}
