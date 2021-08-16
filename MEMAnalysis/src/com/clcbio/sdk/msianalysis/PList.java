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

public class PList {
	private int stdRange;
	private double pValue[];
	
	public PList(int range) {
		stdRange = range;
		pValue = new double[stdRange];
	}
	
	int length() {
		return stdRange;
	}
	
	public double get(int index) {
		if(index >= 0 && index < stdRange) {
			return pValue[index];
		}
		else if(index < 0) {
			return pValue[0];
		}
		else {
			return pValue[stdRange-1];
		}
	}
	
	public void set(int index, double newValue) {
		if(index >= 0 && index < stdRange) {
			pValue[index] = newValue;
		}
		else if(index < 0) {
			pValue[0] = newValue;
		}
		else {
			pValue[stdRange-1] = newValue;
		}
	}
	
	public PList shift(double theoSize, double size) {
		double shiftedY[] = new double[stdRange];
		double shiftedX = 0;
		double shiftedXCR = 0;
		double kernelGate = 0.5;
		double kernel = 0;
		double kernelSum = 0;
		double sum = 0;
		PList newPList = new PList(stdRange);
		
		for(int x = 0; x < stdRange; x++) {
			shiftedX = (double) x * theoSize / size;
			kernelSum = 0;
					
			for(int referenceX = 0; referenceX < stdRange; referenceX++) {
				shiftedXCR = (shiftedX - (double) referenceX) / kernelGate;
				kernel = 1 / Math.sqrt(2 * Math.PI) * Math.exp(-Math.pow(shiftedXCR, 2) / 2);
				kernelSum += get(referenceX) * kernel;
			}
			
			shiftedY[x] = kernelSum / kernelGate;
		}
		for(double y : shiftedY) {
			sum += y;
		}
		for(int x = 0; x < stdRange ; x++) {
			newPList.set(x, shiftedY[x] / sum);
		}
		return newPList;
	}
	
	public PList mixtureModel(double theoSize, double size1, double size2, double proportion1) {
		PList newPList = new PList(stdRange);

		PList mixPList1 = this.shift(theoSize, size1);
		PList mixPList2 = this.shift(theoSize, size2);

		if(proportion1 <0) {
			proportion1 = 0;
		}
		else if(proportion1 > 1) {
			proportion1 = 1;
		}
		
		for(int x = 0; x < stdRange; x++) {
			newPList.set(x, mixPList1.get(x) * proportion1 + mixPList2.get(x) * (1 - proportion1));
		}
		
		return newPList;
	}

	public PList mixtureModel(double theoSize, double size1, double size2, double size3, double proportion1 , double proportion2) {
		PList newPList = new PList(stdRange);

		PList mixPList1 = this.shift(theoSize, size1);
		PList mixPList2 = this.shift(theoSize, size2);
		PList mixPList3 = this.shift(theoSize, size3);

		if(proportion1 <0) {
			proportion1 = 0;
		}
		else if(proportion1 > 1) {
			proportion1 = 1;
		}

		if(proportion2 <0) {
			proportion2 = 0;
		}
		else if(proportion2 > (1 - proportion1)) {
			proportion2 = (1 - proportion1);
		}
		
		for(int x = 0; x < stdRange; x++) {
			newPList.set(x, mixPList1.get(x) * proportion1 + mixPList2.get(x) * proportion2 + mixPList3.get(x) * (1 - proportion1 - proportion2));
		}
		
		return newPList;
	}
	
	public double logLikelihood(int nValues[]) {
		double logLike = 0;
		
		for(int i = 0; i < stdRange; i++) {
			if(this.get(i) > 0) {
				logLike += Math.log10(this.get(i)) * nValues[i];
			}
			else {
				logLike += -308 * nValues[i];
			}
			
		}
		
		return logLike;
	}
	
	public int invert(double p) {
		double sum = 0;
		int result = 0;
		for(int x = 0; x < stdRange; x++) {
			if(p >= sum && p < sum + this.get(x)) {
				result = x;
			}
			sum += this.get(x);
		}
		return result;
	}
}
