package de.charite.compbio.pediasimulator.filter;

import com.google.common.collect.ImmutableSet;

import de.charite.compbio.pediasimulator.model.Gene;
import de.charite.compbio.simdrom.filter.AInfoFieldFilter;

public class JannovarGeneInfoFilter extends AInfoFieldFilter {

	public JannovarGeneInfoFilter(Object type) {
		super("ANN", type);
	}

	@Override
	protected boolean compareInfoType(Object should, Object is) {
		ImmutableSet<Gene> genes = (ImmutableSet<Gene>) should;
		String annotations = ((String) is);
		if (annotations.equals("."))
			return false;
		String[] annotation = annotations.split("\\|");
		Gene gene = new Gene(annotation[3], Integer.parseInt(annotation[4]));
		

		// FIXME what about empty genes: !annotation[3].isEmpty() &&
		return genes.contains(gene);
	}

}
