package de.charite.compbio.pediasimulator.score;

import htsjdk.tribble.Feature;
import htsjdk.variant.variantcontext.Allele;

public class CADDScoreFeature implements Feature {

	private String contig;
	private int position;
	private Allele ref;
	private Allele alt;
	private double rawScore;
	private double phredScore;

	public CADDScoreFeature(String contig, int position, String ref, String alt, double raw_score, double phred_score) {
		this.ref = Allele.create(ref, true);
		this.alt = Allele.create(alt, false);
		this.position = position;
		this.rawScore = raw_score;
		this.phredScore = phred_score;
	}

	public double getRawScore() {
		return rawScore;
	}

	public double getPhredScore() {
		return phredScore;
	}

	public Allele getRef() {
		return ref;
	}

	public Allele getAlt() {
		return alt;
	}

	@Override
	public String getContig() {
		return contig;
	}

	@Override
	public int getStart() {
		return position;
	}

	@Override
	public int getEnd() {
		return position;
	}

	@Override
	public String getChr() {
		return contig;
	}

}
