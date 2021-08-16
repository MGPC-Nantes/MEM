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

import java.util.Collection;
import java.util.Set;

import com.clcbio.api.base.algorithm.parameter.AlgoParameters;
import com.clcbio.api.base.algorithm.parameter.AlgoParametersInterpreter;
import com.clcbio.api.base.algorithm.parameter.ParameterGroup;
import com.clcbio.api.base.algorithm.parameter.keys.DoubleKey;
import com.clcbio.api.base.algorithm.parameter.keys.Key;
import com.clcbio.api.base.algorithm.parameter.keys.KeyContainer;
import com.clcbio.api.base.algorithm.parameter.keys.Keys;

public class MsiPowerParameters extends AlgoParametersInterpreter {
	public final ParameterGroup REF_STEP = ParameterGroup.topLevel("MSI table", "Define reference length distribution table");
	public final ParameterGroup THRESHOLD_STEP = ParameterGroup.childOf(REF_STEP, "Define likelihood ratio threshold");
	public final DoubleKey threshold = Keys.newDoubleKey(this, "threshold").inGroup(THRESHOLD_STEP)
			.mandatory()
			.labelled("Likelihood ratio test threshold")
			.defaultsToNull()
			.minMax(0.0, true, null, true)
			.unlockedInWorkflowsByDefault(false)
			.done();
	private final KeyContainer keys;
	
	public MsiPowerParameters(AlgoParameters parameters) {
		super(parameters);
		this.keys = new KeyContainer(threshold);
	}

	@Override
	public String getClassKey() {
		return "MEM statistical power calculation";
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