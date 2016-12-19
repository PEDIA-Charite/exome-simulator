package de.charite.compbio.pediasimulator.cmd;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.htsjdk.InvalidCoordinatesException;
import de.charite.compbio.pediasimulator.cli.BuildSampleDBOptions;
import de.charite.compbio.pediasimulator.cli.CommandLineParsingException;
import de.charite.compbio.pediasimulator.filter.JannovarEffectInfoFilter;
import de.charite.compbio.pediasimulator.filter.JannovarGeneInfoFilter;
import de.charite.compbio.pediasimulator.io.OMIMGeneLoader;
import de.charite.compbio.pediasimulator.model.Sample;
import de.charite.compbio.pediasimulator.model.Variant;
import de.charite.compbio.pediasimulator.model.VariantsBuilder;
import de.charite.compbio.simdrom.filter.IFilter;
import de.charite.compbio.simdrom.filter.LessOrEqualInfoFieldFilter;
import de.charite.compbio.simdrom.sampler.vcf.VCFSampler;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import net.sourceforge.argparse4j.inf.Namespace;

public class BuildSampleDBCommand implements ICommand {

	/** Configuration */
	private BuildSampleDBOptions options;
	private VariantsBuilder variantsBuilder;
//	private CADDScoreExtractor caddScoreExtractor;
//	private AnnotationFilter annotationFilter;

	private List<Sample> samples = new ArrayList<>();

	public BuildSampleDBCommand(String argv[], Namespace args) throws CommandLineParsingException {
		this.options = new BuildSampleDBOptions();
		this.options.setFromArgs(args);
	}

	/**
	 * 
	 * The main method of the exomiser-benchmark. It should put everything together.
	 * 
	 * 1. Initialize
	 * 
	 * 1.1 Initialize Arguments and parse arguments (done) TODO JSON
	 * 
	 * 2. Config parsers
	 * 
	 * 2.1 Init 1000G VCF file, get samples to parse and validate. (done)
	 * 
	 * 2.2 Init JSON files (TODO). 3. Benchmark 3.1 Iterate over pathogenic ClinVar variants (done)
	 * 
	 * 3.2 Iterate over OMIM IDs (TODO)
	 * 
	 * 3.2.1 Create Genotype of ClinVarVariant (hom_alt or het) if the syndorme is dominant/recessive (TODO)
	 * 
	 * 3.3 Iterate over Samples (done)
	 * 
	 * 3.4 Report result and store it (TODO)
	 * 
	 * @param args
	 *            Arguments of the command line
	 */
	@Override
	public void run() {

		// 1. Config parsers
		// 1.1 Init 1000G VCF file
		VCFFileReader reader = new VCFFileReader(options.getVcfInputFile());
		
		// 1.6 load gene files from omim used for filtering
		OMIMGeneLoader omimGeneLoader = new OMIMGeneLoader(options.getOMIMFile());
		ImmutableSet<String> genes = omimGeneLoader.load();
		
		// 1.2 init filter
		ImmutableSet<IFilter> filters = new ImmutableSet.Builder<IFilter>()
				.add(new LessOrEqualInfoFieldFilter("EXAC_BEST_AF", 0.01))
				.add(new LessOrEqualInfoFieldFilter("UK10K_AF", 0.01))
				.add(new LessOrEqualInfoFieldFilter("AF", 0.01)).add(new LessOrEqualInfoFieldFilter("AFR_AF", 0.01))
				.add(new LessOrEqualInfoFieldFilter("AMR_AF", 0.01)).add(new LessOrEqualInfoFieldFilter("EAS_AF", 0.01))
				.add(new LessOrEqualInfoFieldFilter("EUR_AF", 0.01)).add(new LessOrEqualInfoFieldFilter("SAS_AF", 0.01))
				.add(new LessOrEqualInfoFieldFilter("SAS_AF", 0.01))
				.add(new JannovarEffectInfoFilter(VariantEffect._SMALLEST_MODERATE_IMPACT))
				.add(new JannovarGeneInfoFilter(genes))
				.build();
		// 1.3 initial sampler
		VCFSampler.Builder builder = new VCFSampler.Builder().vcfReader(reader).seed(42).filters(filters);

		// 1.4 initialize the variants builder for normalization and annotation of variants;
		this.variantsBuilder = new VariantsBuilder.Builder().build();

		// 1.5 CADD score extractor
//		this.caddScoreExtractor = new CADDScoreExtractor.Builder().indels(options.getCADDIndel())
//				.snvs(options.getCADDSNV()).build();



		// 1.7 variants filter of effect
//		this.annotationFilter = new AnnotationFilter.Builder().lessOrEqualThan(VariantEffect._SMALLEST_MODERATE_IMPACT)
//				.genes(genes).build();

		// 2 Get the samples to iterate
		List<String> samples = reader.getFileHeader().getSampleNamesInOrder();

		// 3 iterate
		// 3.1 iterate over samples
		int i = 0;
		for (String sampleName : samples) {
			i++;
			System.out.printf("Process %d of %d samples (%s)", i, samples.size(), sampleName);

			VCFSampler sampler = builder.sample(sampleName).build();
			Sample sample = new Sample(sampleName);
			// 3.2 iterate over variants
			while (sampler.hasNext()) {
				VariantContext vc = sampler.next();
				if (vc.isSymbolic()) //skip symbolic variants
					continue;

				List<Variant> variants;
				try {
					variants = this.variantsBuilder.get(vc);

				} catch (AnnotationException | InvalidCoordinatesException e) {
					throw new RuntimeException("Cannot convert " + vc + " int variants", e);
				}

				sample.addAll(variants);
			}
			System.out.printf(" --- found %d genes.\n", +sample.getScoresPerGene().keySet().size());
			this.samples.add(sample);
		}

		// 4 serialize file
		System.out.println("Serialize the samples");

		try {
			FileOutputStream fos = new FileOutputStream(options.getSerializedFile());
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(samples);
			oos.close();
			fos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

}
