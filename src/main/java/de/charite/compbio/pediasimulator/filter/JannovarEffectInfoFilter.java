package de.charite.compbio.pediasimulator.filter;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.simdrom.filter.AInfoFieldFilter;

public class JannovarEffectInfoFilter extends AInfoFieldFilter {

	public JannovarEffectInfoFilter(Object type) {
		super("ANN", type);
	}

	@Override
	protected boolean compareInfoType(Object should, Object is) {
		VariantEffect shouldEffect = (VariantEffect) should;
		String[] annotation = ((String) is).split("\\|");

		VariantEffect effect = VariantEffect._SMALLEST_LOW_IMPACT;
		for (VariantEffect e : VariantEffect.values()) {
			String so = e.getSequenceOntologyTerm();
			if (so != null && so.equals(annotation[1])) {
				effect = e;
				break;
			}

		}

		return effect.ordinal() <= shouldEffect.ordinal();
	}

}
