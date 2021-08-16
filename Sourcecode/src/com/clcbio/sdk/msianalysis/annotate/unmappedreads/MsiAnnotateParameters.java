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

import java.util.Collection;
import java.util.Set;

import com.clcbio.api.base.algorithm.parameter.AlgoParameters;
import com.clcbio.api.base.algorithm.parameter.AlgoParametersInterpreter;
import com.clcbio.api.base.algorithm.parameter.ParameterGroup;
import com.clcbio.api.base.algorithm.parameter.keys.DoubleKey;
import com.clcbio.api.base.algorithm.parameter.keys.Key;
import com.clcbio.api.base.algorithm.parameter.keys.KeyContainer;
import com.clcbio.api.base.algorithm.parameter.keys.Keys;
import com.clcbio.api.base.algorithm.parameter.keys.StringKey;

public class MsiAnnotateParameters extends AlgoParametersInterpreter {
	public final ParameterGroup UNMAP_READS_STEP = ParameterGroup.topLevel("Unmapped Reads", "Define unmapped reads");
	public final ParameterGroup NAME_STEP = ParameterGroup.childOf(UNMAP_READS_STEP, "Define microsatellite's name");
	//public final ParameterGroup SHIFT_STEP = ParameterGroup.childOf(UNMAP_READS_STEP, "Define microsatellite's size range");
	public final ParameterGroup THRESHOLD_STEP = ParameterGroup.childOf(UNMAP_READS_STEP, "Define likelihood ratio threshold");
	public final ParameterGroup POW_STEP = ParameterGroup.childOf(UNMAP_READS_STEP, "Define power calculation parameters");
	public final StringKey name = Keys.newStringKey(this, "name").inGroup(NAME_STEP)
			.mandatory()
			.labelled("Microsatellite's name")
			.defaultsToNull()
			.unlockedInWorkflowsByDefault(false)
			.done();
	public final DoubleKey threshold = Keys.newDoubleKey(this, "threshold").inGroup(THRESHOLD_STEP)
			.mandatory()
			.labelled("Likelihood ratio test threshold")
			.defaultsToNull()
			.minMax(0.0, true, null, true)
			.unlockedInWorkflowsByDefault(false)
			.done();
/*	public final DoubleKey shiftMin = Keys.newDoubleKey(this, "shiftMin").inGroup(SHIFT_STEP)
			.mandatory()
			.labelled("Microsatellite's minimal bias vs mean")
			.defaultsToNull()
			.unlockedInWorkflowsByDefault(false)
			.done();
	public final DoubleKey shiftMax = Keys.newDoubleKey(this, "shiftMax").inGroup(SHIFT_STEP)
			.mandatory()
			.labelled("Microsatellite's maximal bias vs mean")
			.defaultsToNull()
			.unlockedInWorkflowsByDefault(false)
			.done();*/
	public final DoubleKey meanPow = Keys.newDoubleKey(this, "meanPow").inGroup(POW_STEP)
			.mandatory()
			.labelled("Power mean")
			.defaultsToNull()
			.unlockedInWorkflowsByDefault(false)
			.done();
	public final DoubleKey stdDevPow = Keys.newDoubleKey(this, "stdDevPow").inGroup(POW_STEP)
			.mandatory()
			.labelled("Power standard deviation")
			.minMax(0.0, true, null, true)
			.defaultsToNull()
			.unlockedInWorkflowsByDefault(false)
			.done();
	private final KeyContainer keys;
	
	public MsiAnnotateParameters(AlgoParameters parameters) {
		super(parameters);
		this.keys = new KeyContainer(name, /*shiftMin, shiftMax,*/ threshold, meanPow, stdDevPow);
	}

	@Override
	public String getClassKey() {
		return "MEM Annotate Microsatellite Reads";
	}

	@Override
	public void setToDefault() {
		keys.setToDefault();
	}
	
    @Override
    public Set<String> getKeys() {
        return keys.getKeySet();
    }

    @Override
    public Collection<Key<?>> getKeyObjects() {
        return keys.getKeys();
    }
}
