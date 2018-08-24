package de.charite.compbio.pediasimulator.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import com.google.common.base.Splitter;
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
	private final double framshift_cadd_raw = 5.0;
	private final double framshift_cadd_phred = 25.0;

	private VariantsBuilder(JannovarData jannovarData, ReferenceDictionary refDict,
			ImmutableMap<Integer, Chromosome> chromosomeMap, VariantAnnotator annotator,
			VariantNormalizer variantNormalizer) {
		this.jannovarData = jannovarData;
		this.refDict = refDict;
		this.chromosomeMap = chromosomeMap;
		this.annotator = annotator;
		this.variantNormalizer = variantNormalizer;
	}

	private VariantsBuilder() {
	}

	private boolean checkFrameshift(String effect) {
		String effects[] = effect.split("&");
		boolean result = false;
		for (int i = 0; i < effects.length; i++) {
			switch (effects[i]) {
				case "frameshift_variant":
				case "frameshift_elongation":
				case "frameshift_truncation":
					result = result | true;
					break;
				default:
					// result = result | false;
			}
		}
		return result;
	}

	public static final class Builder {

		private String jannovarDB;
		private String genomeFasta;

		public Builder jannovarDB(File jannovarDB) {
			this.jannovarDB = jannovarDB.getAbsolutePath();
			return this;
		}

		public VariantsBuilder build() {

			if (jannovarDB == null && genomeFasta != null)
				throw new RuntimeException("Jannovar database file have to be set");
			if (jannovarDB != null && genomeFasta == null)
				throw new RuntimeException("Reference fasta file have to be set");
			if (jannovarDB != null)
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
			else {
				return new VariantsBuilder();
			}

		}

		public Builder genomeFile(File genomeFastaFile) {
			genomeFasta = genomeFastaFile.getAbsolutePath();
			return this;
		}

	}

	public List<Variant> get(VariantContext vc) throws InvalidCoordinatesException, AnnotationException {

		List<Variant> outputs = new ArrayList<>();

		if (vc.isSymbolicOrSV())
			return outputs;
		if (vc.getContig().equals("MT"))
			return outputs;

		final String ref = vc.getReference().getBaseString();
		final int pos = vc.getStart();
		if (jannovarData != null) {

			Integer boxedInt = refDict.getContigNameToID().get(vc.getContig());
			if (boxedInt == null)
				throw new InvalidCoordinatesException("Unknown reference " + vc.getContig(),
						AnnotationMessage.ERROR_CHROMOSOME_NOT_FOUND);
			int chr = boxedInt.intValue();
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
		} else {
			List<Object> phred_snv = vc.getCommonInfo().getAttributeAsList("CADD_SNV_PHRED");
			List<Object> raw_snv = vc.getCommonInfo().getAttributeAsList("CADD_SNV_RawScore");
			List<Object> raw_snv_ovl = vc.getCommonInfo().getAttributeAsList("CADD_SNV_OVL_RawScore");
			List<Object> phred_snv_ovl = vc.getCommonInfo().getAttributeAsList("CADD_SNV_OVL_PHRED");

			List<Object> phred_indel = vc.getCommonInfo().getAttributeAsList("CADD_INDEL_PHRED");
			List<Object> raw_indel = vc.getCommonInfo().getAttributeAsList("CADD_INDEL_RawScore");
			List<Object> raw_indel_ovl = vc.getCommonInfo().getAttributeAsList("CADD_INDEL_OVL_RawScore");
			List<Object> phred_indel_ovl = vc.getCommonInfo().getAttributeAsList("CADD_INDEL_OVL_PHRED");

			List<Object> annotation = vc.getCommonInfo().getAttributeAsList("ANN");
			for (int i = 0; i < vc.getAlternateAlleles().size(); i++) {
				boolean isFrameshift = checkFrameshift(((String) annotation.get(i)).split("\\|")[1]);
				final String alt = vc.getAlternateAllele(i).getBaseString();
				Variant variant = new Variant(vc.getContig(), pos, vc.getEnd(), ref, alt);
				if (alt.length() == ref.length()) {
					if (vc.isMNP() || raw_snv.isEmpty() || ((String) raw_snv.get(i)).equals(".")) { // MSNP are problematic
						variant.setScore(ScoreType.CADD_RAW, Splitter.on("|").splitToList((String) raw_snv_ovl.get(0))
								.stream().mapToDouble(s -> Double.parseDouble(s)).max().getAsDouble());
						variant.setScore(ScoreType.CADD_PHRED,
								Splitter.on("|").splitToList((String) phred_snv_ovl.get(0)).stream()
										.mapToDouble(s -> Double.parseDouble(s)).max().getAsDouble());
					} else {
						variant.setScore(ScoreType.CADD_RAW, Double.parseDouble((String) raw_snv.get(i)));
						variant.setScore(ScoreType.CADD_PHRED, Double.parseDouble((String) phred_snv.get(i)));
					}
				} else {
					OptionalDouble value_raw;
					OptionalDouble value_phred;
					if (raw_indel.isEmpty()) {
						// use SNVs if no indel variant is present...
						if (raw_indel_ovl.isEmpty()) {
							value_raw = Splitter.on("|").splitToList((String) raw_snv_ovl.get(0)).stream()
									.mapToDouble(s -> Double.parseDouble(s)).max();
							value_phred = Splitter.on("|").splitToList((String) phred_snv_ovl.get(0)).stream()
									.mapToDouble(s -> Double.parseDouble(s)).max();
						} else {
							value_raw = Splitter.on("|").splitToList((String) raw_indel_ovl.get(0)).stream()
									.mapToDouble(s -> Double.parseDouble(s)).max();
							value_phred = Splitter.on("|").splitToList((String) phred_indel_ovl.get(0)).stream()
									.mapToDouble(s -> Double.parseDouble(s)).max();
						}
					} else if (((String) raw_indel.get(i)).equals(".")) {
						value_raw = Splitter.on("|").splitToList((String) raw_indel_ovl.get(0)).stream()
								.mapToDouble(s -> Double.parseDouble(s)).max();
						value_phred = Splitter.on("|").splitToList((String) phred_indel_ovl.get(0)).stream()
								.mapToDouble(s -> Double.parseDouble(s)).max();
					} else {
						value_raw = Splitter.on("|").splitToList((String) raw_indel.get(i)).stream()
								.mapToDouble(s -> Double.parseDouble(s)).max();
						value_phred = Splitter.on("|").splitToList((String) phred_indel.get(i)).stream()
								.mapToDouble(s -> Double.parseDouble(s)).max();
					}
					if (isFrameshift){
						variant.setScore(ScoreType.CADD_RAW, Double.max(framshift_cadd_raw, value_raw.getAsDouble()));
						variant.setScore(ScoreType.CADD_PHRED, Double.max(framshift_cadd_phred, value_phred.getAsDouble()));
					} else {
						variant.setScore(ScoreType.CADD_RAW, value_raw.getAsDouble());
						variant.setScore(ScoreType.CADD_PHRED, value_phred.getAsDouble());
					}
				}

				variant.setGene(new Gene(((String) annotation.get(i)).split("\\|")[3],
						Integer.parseInt(((String) annotation.get(i)).split("\\|")[4])));
				outputs.add(variant);
			}
		}
		return outputs;
	}

}
