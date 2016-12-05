package de.charite.compbio.pediasimulator.score;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import de.charite.compbio.pediasimulator.model.ScoreType;
import de.charite.compbio.pediasimulator.model.Variant;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.FeatureReader;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.variant.variantcontext.Allele;

public class CADDScoreExtractor implements Closeable {

	private FeatureReader<CADDScoreFeature> readerInDel;
	private FeatureReader<CADDScoreFeature> readerSNV;

	private CADDScoreExtractor(FeatureReader<CADDScoreFeature> caddReaderSNV,
			FeatureReader<CADDScoreFeature> caddReaderInDel) {
		this.readerSNV = caddReaderSNV;
		this.readerInDel = caddReaderInDel;
	}

	public void annotate(Variant variant) {
		int end = variant.getEnd();
		if (variant.getRef().length() < variant.getAlt().length())
			end += 1;
		try {
			double score = getCaddScore(variant.getContig(), variant.getStart(), end, variant.getRef(), variant.getAlt(),
					variant.isIndel());
			variant.setScore(ScoreType.CADD,score);
		} catch (IOException e) {
			throw new RuntimeException("Cannot query variant " + variant.toString(), e);
		}
	}

	private double getCaddScore(String chr, int start, int end, Allele ref, Allele alt, boolean indel)
			throws IOException {

		CloseableTribbleIterator<CADDScoreFeature> iterator;
		if (indel)
			iterator = readerInDel.query(chr, start, end);
		else
			iterator = readerSNV.query(chr, start, end);
		double max = Double.MIN_VALUE;
		for (CADDScoreFeature caddScoreFeature : iterator) {
			max = Math.max(max, caddScoreFeature.getRawScore());
			if (caddScoreFeature.getAlt().equals(alt) && caddScoreFeature.getRef().equals(ref)) {
				double score = caddScoreFeature.getRawScore();
				iterator.close();
				return score;
			}
		}
		iterator.close();
		return max;

	}

	public static final class Builder {

		private String indels;
		private String snvs;

		public Builder() {
		}

		public Builder indels(File caddIndel) {
			indels = caddIndel.getAbsolutePath();
			return this;
		}

		public Builder snvs(File caddsnv) {
			snvs = caddsnv.getAbsolutePath();
			return this;
		}

		public CADDScoreExtractor build() {

			if (indels == null || snvs == null)
				throw new RuntimeException("Indels Or SNVs not set!");
			try {
				FeatureReader<CADDScoreFeature> caddReaderSNV = new TabixFeatureReader<>(snvs, new CADDcodec());
				FeatureReader<CADDScoreFeature> caddReaderInDel = new TabixFeatureReader<>(indels, new CADDcodec());

				return new CADDScoreExtractor(caddReaderSNV, caddReaderInDel);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot read CaddScore database", e);
			}

		}

	}

	@Override
	public void close() throws IOException {
		this.readerInDel.close();
		this.readerSNV.close();

	}

}
