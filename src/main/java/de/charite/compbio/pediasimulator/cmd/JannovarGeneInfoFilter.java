package de.charite.compbio.pediasimulator.cmd;

import com.google.common.collect.ImmutableSet;

import de.charite.compbio.simdrom.filter.AInfoFieldFilter;

public class JannovarGeneInfoFilter extends AInfoFieldFilter {

	public JannovarGeneInfoFilter(Object type) {
		super("ANN", type);
	}

	@Override
	protected boolean compareInfoType(Object should, Object is) {
		ImmutableSet<String> genes = (ImmutableSet<String>) should;
		String[] annotation = ((String) is).split("\\|");

		return !annotation[3].isEmpty() && genes.contains(annotation[3]);
	}

}
