package de.charite.compbio.pediasimulator.model;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.charite.compbio.jannovar.annotation.Annotation;

public class Sample {

	private String name;

	Map<String,ListMultimap<ScoreType,Double>> scoresPerGene;

	public Sample(String sampleName) {
		this.name = sampleName;
		scoresPerGene = new HashMap<>();
	}

	public void add(Variant variant) {
		Set<String> genes = new HashSet<>();
		for (Annotation annotation : variant.getAnnotations()) {
			genes.add(annotation.getGeneSymbol());
		}
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

}
