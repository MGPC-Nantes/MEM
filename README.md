# Description
*Motivation:*
Microsatellites are DNA sequences formed by a continuous repetition of patterns
from 1 to 6 nucleotides. Deficient mismatch repair system (dMMR) induces a
variation in length of microsatellites called microsatellite instability (MSI). 
However, MSI assessment by Next-Generation Sequencing (NGS) is difficult 
because replication errors occurring during the amplification steps of the 
sequencing process themselves induce variation in microsatellite sequence length.  

*Results:* 
The MSI assessment by Expectation-Maximization (MEM) analysis attempts to closely 
replicate the reference PCR interpretation method for 5 microsatellites validated 
by the Bethesda and ESMO international guidelines (BAT-25, BAT-26, NR-21, NR-24, 
and NR-27). MEM identifies the stable or unstable nature of each microsatellite 
i- by determining the length distribution of microsatellite sequences from unmapped 
and quality unfiltered paired-end reads using the Smith-Waterman alignment, and 
ii- by determining whether the observed distribution is comparable to a reference 
distribution (stable) or corresponds to a mixture model, i.e. the mixture of several 
sub-distributions of different mean lengths (unstable) using Expectation-Maximization 
algorithm.

# Availability

MEM combines a workflow and a plugin for CLC Genomics Workbench (QIAGEN Aarhus A/S). The workflow is necessary for the overall operation of the analysis, and in particular the determination of the length distribution of microsatellite sequences (step i). The plugin adds the necessary tools for the Expectation-Maximisation algorithm (step ii).

## Installation

The ready-to-use files are available in the "CLC-Ready-to-use" folder.

1- Install the plugin on an adapted version of the CLC Genomics Workbench with the "MEMAnalysis_CLC_vX-X.X.cpa" file
2- Install the workflow on an adapted version of the CLC Genomics Workbench with the "MEMworkflow_CLC_vX-X.X.cpw" file.

For more information on installing plugins and workflows on the CLC Genomics Workbench, see the [User Manual](http://resources.qiagenbioinformatics.com/manuals/clcgenomicsworkbench/current/User_Manual.pdf) for your current version.

## Sourcecode

The sourcecode of the MEM plugin is available in the "Sourcecode" folder.

# More Information

The MEM algorithm is described in details in our paper:

# License

`MEM` is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version.

`MEM` is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
A PARTICULAR PURPOSE.  See the GNU General Public License for more
details.
