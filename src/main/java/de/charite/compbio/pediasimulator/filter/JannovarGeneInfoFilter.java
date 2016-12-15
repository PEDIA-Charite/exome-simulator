package de.charite.compbio.pediasimulator.filter;

import com.google.common.collect.ImmutableSet;

import de.charite.compbio.simdrom.filter.AInfoFieldFilter;

public class JannovarGeneInfoFilter extends AInfoFieldFilter {

	public JannovarGeneInfoFilter(Object type) {
		super("ANN", type);
	}

	@Override
	protected boolean compareInfoType(Object should, Object is) {
		ImmutableSet<String> genes = (ImmutableSet<String>) should;
		String annotations = ((String) is);
		if (annotations.equals("."))
				return false;
		
		String[] annotation = annotations.split("\\|");

		return !annotation[3].isEmpty() && genes.contains(annotation[3]);
	}

}
