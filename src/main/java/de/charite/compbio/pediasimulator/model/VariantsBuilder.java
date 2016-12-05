package de.charite.compbio.pediasimulator.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.AnnotationMessage;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.Chromosome;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.htsjdk.InvalidCoordinatesException;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import de.charite.compbio.jannovar.vardbs.base.VariantDescription;
import de.charite.compbio.jannovar.vardbs.base.VariantNormalizer;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;

public class VariantsBuilder {

	private JannovarData jannovarData;
	private ReferenceDictionary refDict;
	private ImmutableMap<Integer, Chromosome> chromosomeMap;
	private VariantAnnotator annotator;
	private VariantNormalizer variantNormalizer;

	public VariantsBuilder(JannovarData jannovarData, ReferenceDictionary refDict,
			ImmutableMap<Integer, Chromosome> chromosomeMap, VariantAnnotator annotator,
			VariantNormalizer variantNormalizer) {
		this.jannovarData = jannovarData;
		this.refDict = refDict;
		this.chromosomeMap = chromosomeMap;
		this.annotator = annotator;
		this.variantNormalizer = variantNormalizer;
	}

	public static final class Builder {

		private String jannovarDB;
		private String genomeFasta;

		public Builder jannovarDB(File jannovarDB) {
			this.jannovarDB = jannovarDB.getAbsolutePath();
			return this;
		}

		public VariantsBuilder build() {

			if (jannovarDB == null || genomeFasta == null)
				throw new RuntimeException("Jannovar database and Genome fasta file have to be set");

			try {
				JannovarData jannovarData = new JannovarDataSerializer(jannovarDB).load();
				ReferenceDictionary refDict = jannovarData.getRefDict();
				ImmutableMap<Integer, Chromosome> chromosomeMap = jannovarData.getChromosomes();
				VariantAnnotator annotator = new VariantAnnotator(refDict, chromosomeMap,
						new AnnotationBuilderOptions());
				VariantNormalizer variantNormalizer = new VariantNormalizer(genomeFasta);
				return new VariantsBuilder(jannovarData, refDict, chromosomeMap, annotator, variantNormalizer);
			} catch (SerializationException e) {
				throw new RuntimeException("Cannot deserialize Jannovar database", e);
			} catch (JannovarVarDBException e) {
				throw new RuntimeException("Cannot initialize allele matcher", e);
			}

		}

		public Builder genomeFile(File genomeFastaFile) {
			genomeFasta = genomeFastaFile.getAbsolutePath();
			return this;
		}

	}

	public List<Variant> get(VariantContext vc) throws InvalidCoordinatesException, AnnotationException {

		List<Variant> outputs = new ArrayList<>();
		Integer boxedInt = refDict.getContigNameToID().get(vc.getContig());
		if (boxedInt == null)
			throw new InvalidCoordinatesException("Unknown reference " + vc.getContig(),
					AnnotationMessage.ERROR_CHROMOSOME_NOT_FOUND);
		int chr = boxedInt.intValue();
		final String ref = vc.getReference().getBaseString();
		final int pos = vc.getStart();
		for (int i = 0; i < vc.getAlternateAlleles().size(); i++) {
			final Allele altAllele = vc.getAlternateAllele(i);
			final String alt = altAllele.getBaseString();

			VariantDescription vd = new VariantDescription(vc.getContig(), pos - 1, ref, alt);
			VariantDescription nd = variantNormalizer.normalizeVariant(vd);
			if (nd.getRef().isEmpty() || nd.getAlt().isEmpty()) // is insertion or deletion
				nd = variantNormalizer.normalizeInsertion(vd);

			GenomeVariant g = new GenomeVariant(
					new GenomePosition(refDict, Strand.FWD, chr, nd.getPos(), PositionType.ZERO_BASED), nd.getRef(),
					nd.getAlt());
			VariantAnnotations annotatons = annotator.buildAnnotations(g);

			outputs.add(new Variant(vc.getContig(), nd.getPos() + 1, nd.getEnd(), nd.getRef(), nd.getAlt(),
					annotatons.getAnnotations()));

		}
		return outputs;
	}

}
