package de.charite.compbio.pediasimulator.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.charite.compbio.jannovar.annotation.Annotation;

public class Sample {

	private String name;

	Map<String, ListMultimap<ScoreType, Double>> scoresPerGene;

	public Sample(String sampleName) {
		this.name = sampleName;
		this.scoresPerGene = new HashMap<>();
	}

	public void add(Variant variant) {
		Set<String> genes = new HashSet<>();
		genes.addAll(variant.getGenes());
		for (String gene : genes) {
			if (!scoresPerGene.containsKey(gene))
				scoresPerGene.put(gene, ArrayListMultimap.create());
			for (ScoreType type : variant.getScores().keySet())
				scoresPerGene.get(gene).put(type, variant.getScores().get(type));
		}

	}

	public Map<String, ListMultimap<ScoreType, Double>> getScoresPerGene() {
		return scoresPerGene;
	}

	public void addAll(List<Variant> variants) {
		for (Variant variant : variants) {
			add(variant);
		}
	}

	@Override
	public String toString() {
		return "Sample [name=" + name + ", scoresPerGene=" + scoresPerGene + "]";
	}
	
	

}
