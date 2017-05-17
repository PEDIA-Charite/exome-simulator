/**
 * 
 */
package de.charite.compbio.pediasimulator.score;

import java.util.regex.Pattern;

import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.readers.LineIterator;

/**
 * Codec for parsing a tabix CADD file, as described by Kircher et.al.
 * 
 * @author Max Schubach
 *
 */
public class CADDcodec extends AsciiFeatureCodec<CADDScoreFeature> {
	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\t|( +)");

	/**
	 * @param myClass
	 */
	public CADDcodec() {
		super(CADDScoreFeature.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see htsjdk.tribble.FeatureCodec#canDecode(java.lang.String)
	 */
	@Override
	public boolean canDecode(String path) {
		return path.toLowerCase().endsWith(".tsv") || path.toLowerCase().endsWith(".tsv.gz");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see htsjdk.tribble.AsciiFeatureCodec#decode(java.lang.String)
	 */
	@Override
	public CADDScoreFeature decode(String line) {
		if (line.trim().isEmpty()) {
			return null;
		}

		if (line.startsWith("#")) { // we do not read the header
			return null;
		}

		String[] tokens = SPLIT_PATTERN.split(line, -1);
		return decode(tokens);
	}

	/**
	 * Decode a line splitted by tabs (not a header line) of a CADD tabix file into a pathogenicity score variant
	 * 
	 * @param tokens
	 *            of a tabix cadd line
	 * @return the transformated tokens into a {@link PathogenicityScoreVariant} containing {@link ScoreType#CADD_RAW} and
	 *         {@link ScoreType#CADD_RAW}.
	 */
	public CADDScoreFeature decode(String[] tokens) {
		int tokenCount = tokens.length;

		// The first 3 columns are non optional for BED. We will relax this
		// and only require 2.

		if (tokenCount < 2) {
			return null;
		}

		String chr = tokens[0];
		int start = Integer.parseInt(tokens[1]);
		String ref = tokens[2];
		String alt = tokens[3];

		double rawScore = Double.parseDouble(tokens[4]);
		double phred_score = Double.parseDouble(tokens[5]);

		CADDScoreFeature output = new CADDScoreFeature(chr, start, ref, alt, rawScore, phred_score);

		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see htsjdk.tribble.AsciiFeatureCodec#readActualHeader(htsjdk.tribble.readers.LineIterator)
	 */
	@Override
	public Object readActualHeader(LineIterator reader) {
		return null;
	}

}
