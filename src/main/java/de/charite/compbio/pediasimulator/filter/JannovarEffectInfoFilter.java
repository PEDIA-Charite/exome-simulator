package de.charite.compbio.pediasimulator.filter;

import java.util.ArrayList;
import java.util.List;

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

		if (annotation.length == 0 || annotation[0].equals("."))
			return false;
		String[] impacts = annotation[1].split("&");

		List<VariantEffect> effects = new ArrayList<>();
		for (VariantEffect e : VariantEffect.values()) {
			String so = e.getSequenceOntologyTerm();
			if (so != null) {
				for (String impact : impacts) {
					if (so.equals(impact)) {
						effects.add(e);
					}
				}
			}
		}
		for (VariantEffect effect : effects) {
			if (effect.ordinal() <= shouldEffect.ordinal())
				return true;
		}

		return false;

	}

}
