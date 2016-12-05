package de.charite.compbio.pediasimulator.model;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

public class AnnotationFilter {

	private VariantEffect lessOrEqual;
	private ImmutableSet<String> genes;

	private AnnotationFilter(VariantEffect lessOrEqual, ImmutableSet<String> genes) {
		this.lessOrEqual = lessOrEqual;
		this.genes = genes;
	}

	public static final class Builder {

		private VariantEffect lessOrEqual = VariantEffect._SMALLEST_LOW_IMPACT;
		private ImmutableSet<String> genes = new ImmutableSet.Builder<String>().build();

		public Builder lessOrEqualThan(VariantEffect smallestModerateImpact) {
			lessOrEqual = smallestModerateImpact;
			return this;
		}

		public Builder genes(ImmutableSet<String> genes) {
			this.genes = genes;
			return this;
		}

		public AnnotationFilter build() {
			return new AnnotationFilter(lessOrEqual, genes);
		}

	}

	public Optional<Variant> filter(Variant variant) {
		ImmutableList.Builder<Annotation> builder = new ImmutableList.Builder<>();
		for (Annotation annotation : variant.getAnnotations()) {

			boolean genePassed = false;
			boolean effectPassed = false;

			VariantEffect effect = annotation.getMostPathogenicVarType();

			// can be null if jannovar has a problem with the annotation
			if (effect == null)
				continue;
			// effect
			if (effect.ordinal() <= lessOrEqual.ordinal())
				effectPassed = true;

			// gene
			if (genes.isEmpty() || genes.contains(annotation.getGeneSymbol()))
				genePassed = true;

			if (genePassed && effectPassed)
				builder.add(annotation);
		}
		ImmutableList<Annotation> annotations = builder.build();
		if (annotations.isEmpty())
			return Optional.empty();
		else {
			variant.setAnnotations(annotations);
			return Optional.of(variant);
		}
	}

}
