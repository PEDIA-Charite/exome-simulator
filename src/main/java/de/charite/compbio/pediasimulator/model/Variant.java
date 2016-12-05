package de.charite.compbio.pediasimulator.model;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import htsjdk.variant.variantcontext.Allele;

public class Variant {

	private String contig;
	private int start;
	private int end;

	private Allele ref;
	private Allele alt;

	private ImmutableList<Annotation> annotations;

	private Map<ScoreType, Double> scores;

	public Variant(String contig, int start, int end, String ref, String alt, ImmutableList<Annotation> annotations) {
		this.contig = contig;
		this.start = start;
		this.end = end;
		this.ref = Allele.create(ref, true);
		this.alt = Allele.create(alt, false);
		this.annotations = annotations;
	}

	public String getContig() {
		return contig;
	}

	public Allele getRef() {
		return ref;
	}

	public Allele getAlt() {
		return alt;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public boolean isIndel() {
		return getRef().length() != getAlt().length();
	}

	public Map<ScoreType, Double> getScores() {
		if (scores == null)
			scores = new HashMap<>();
		return scores;
	}
	
	public ImmutableList<Annotation> getAnnotations() {
		return annotations;
	}

	public void setScore(ScoreType type, double score) {
		getScores().put(type, score);
	}
	
	public void setAnnotations(ImmutableList<Annotation> annotations) {
		this.annotations = annotations;
	}
	
	@Override
	public String toString() {
		return contig + ":" + start + "-" +  end + ref.getBaseString()+ ">" + alt.getBaseString();
	}

}
