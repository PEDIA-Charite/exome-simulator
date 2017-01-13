package de.charite.compbio.pediasimulator.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import de.charite.compbio.pediasimulator.model.Gene;

public class OMIMGeneLoader {

	File omimFile;

	public OMIMGeneLoader(File omimFile) {
		this.omimFile = omimFile;
	}

	public ImmutableSet<Gene> load() {
		ImmutableSet.Builder<Gene> output = new ImmutableSet.Builder<>();
		try {
			CSVParser parser = CSVFormat.TDF.parse(new FileReader(omimFile));

			for (CSVRecord csvRecord : parser) {
				if (csvRecord.get(0).startsWith("#")) // header
					continue;
				if (csvRecord.get(9).isEmpty()) // no entrez geneid
					continue;
				if (csvRecord.get(12).isEmpty()) // no phenotype or successability
					continue;

				boolean use = false;
				Iterable<String> phenotypes = Splitter.on(';').trimResults().split(csvRecord.get(12));
				for (String phenotype : phenotypes) {
					if (!(phenotype.startsWith("{") || phenotype.startsWith("[") || phenotype.startsWith("?")))
						use = true;

				}
				if (use)
					output.add(new Gene(csvRecord.get(8), Integer.parseInt(csvRecord.get(9))));
			}

		} catch (IOException e) {
			throw new RuntimeException("Cannot parse OMIM file!", e);
		}

		return output.build();
	}

}
