package de.charite.compbio.pediasimulator.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

public class OMIMGeneLoader {
	
	File omimFile;

	public OMIMGeneLoader(File omimFile) {
		this.omimFile = omimFile;
	}

	public ImmutableSet<String> load() {
		ImmutableSet.Builder<String> output = new ImmutableSet.Builder<>();
		try {
			CSVParser parser = CSVFormat.TDF.parse(new FileReader(omimFile));
			
			for (CSVRecord csvRecord : parser) {
				if (csvRecord.get(0).startsWith("#"))
					continue;
				output.addAll(Splitter.on(',').trimResults().omitEmptyStrings().splitToList(csvRecord.get(6)));
			}
			
		} catch (IOException e) {
			throw new RuntimeException("Cannot parse OMIM file!",e);
		}
		
		return output.build();
	}

}
