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

import com.clcbio.api.base.algorithm.Algo;
import com.clcbio.api.base.algorithm.parameter.AlgoParameters;
import com.clcbio.api.base.algorithm.selection.MinElementCountDoneConstraint;
import com.clcbio.api.clc.gui.wizard.WizardFacade;
import com.clcbio.api.free.actions.framework.StaticActionGroupDefinitions;
import com.clcbio.api.free.algorithm.AlgoAction;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.list.NucleotideSequenceList;
import com.clcbio.api.free.gui.components.MultiSelectClassRestrictor;
import com.clcbio.api.free.gui.components.MultiSelectRestrictor;
import com.clcbio.api.free.wizard.dynamic.ClcWizardStepModel;

public class MsiAnalysisAction extends AlgoAction {
    private static final long serialVersionUID = 3L;
    public static final String PLUGIN_GROUP = "free";

    @Override
    public String getName() {
        return "MEM Algorithm";
    }

    @Override
    public boolean isInToolBar() {
        return false;
    }
    
    @Override
    public boolean isInMenu() {
        return true;
    }
    
    @Override
    public String getClassKey() {
        return "com.clcbio.sdk.msianalysis";
    }

    @Override
    public double getVersion() {
        return 3.0;
    }

    @Override
    protected void addToActionGroup() {
    	StaticActionGroupDefinitions.TOOLBOX_TOP_GROUP.addAction(this);
    }
    
    @Override
    public int getPreferredMenuLocation() {
        return 6;
    }

    @Override
    public Algo createAlgo() {
        return new MsiAnalysisAlgo(getManager());
    }

    @Override
    public String getHelpID() {
        return null;
    }
    
    @Override
    public MultiSelectRestrictor createRestrictor(final WarningReceptor warningReceptor) {
    	MultiSelectClassRestrictor restrictor = new MultiSelectClassRestrictor(new Class<?>[]{NucleotideSequenceList.class}, "Select unmapped annotated reads");
    	restrictor.allowDoneWhen(new MinElementCountDoneConstraint(1));
    	return restrictor;
    }
    
    @Override
    public ClcWizardStepModel getFirstStep(AlgoParameters parameters, ClcWizardStepModel nextStep) {
        WizardFacade facade = WizardFacade.getInstance();
        MsiAnalysisParameters p = new MsiAnalysisParameters(parameters);
        return facade.createDefaultParameterSteps(p.createKeyChecker(getManager()), p.getKeyObjects(), nextStep);
    }
}
