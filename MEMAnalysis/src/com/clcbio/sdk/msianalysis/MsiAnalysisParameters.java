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

import java.util.Collection;
import java.util.Set;

import com.clcbio.api.base.algorithm.parameter.AlgoParameters;
import com.clcbio.api.base.algorithm.parameter.AlgoParametersInterpreter;
import com.clcbio.api.base.algorithm.parameter.ParameterGroup;
import com.clcbio.api.base.algorithm.parameter.keys.ClcObjectKey;
import com.clcbio.api.base.algorithm.parameter.keys.Key;
import com.clcbio.api.base.algorithm.parameter.keys.KeyContainer;
import com.clcbio.api.base.algorithm.parameter.keys.Keys;
import com.clcbio.api.free.datatypes.GeneralClcTabular;

public class MsiAnalysisParameters extends AlgoParametersInterpreter {
	public final ParameterGroup UNMAP_READS_STEP = ParameterGroup.topLevel("Unmapped Reads", "Define unmapped annotated reads");
	public final ParameterGroup TABLE_STEP = ParameterGroup.childOf(UNMAP_READS_STEP, "Define reference distribution table");
	public final ClcObjectKey table = Keys.newClcObjectKey(this, "table").inGroup(TABLE_STEP)
			.allowedTypes(GeneralClcTabular.class)
			.labelled("Reference distribution tables")
			.defaultsToNull()
			.mandatory()
			.done();
	private final KeyContainer keys;
	
	public MsiAnalysisParameters(AlgoParameters parameters) {
		super(parameters);
		this.keys = new KeyContainer(table);
	}

	@Override
	public String getClassKey() {
		return "MEM Algorithm";
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
