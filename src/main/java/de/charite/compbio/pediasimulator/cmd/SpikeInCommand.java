package de.charite.compbio.pediasimulator.cmd;

import java.util.List;
import java.util.Random;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

import de.charite.compbio.pediasimulator.cli.CommandLineParsingException;
import de.charite.compbio.pediasimulator.cli.SpikeInOptions;
import de.charite.compbio.pediasimulator.filter.JannovarGeneInfoFilter;
import de.charite.compbio.pediasimulator.io.OMIMGeneLoader;
import de.charite.compbio.simdrom.filter.IFilter;
import de.charite.compbio.simdrom.sampler.SpikeIn;
import de.charite.compbio.simdrom.sampler.vcf.VCFSampler;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import net.sourceforge.argparse4j.inf.Namespace;

public class SpikeInCommand implements ICommand {

	/** Configuration */
	private final SpikeInOptions options;
	private int variantCount;
	private final String command;

	public SpikeInCommand(String argv[], Namespace args) throws CommandLineParsingException {
		this.options = new SpikeInOptions();
		this.options.setFromArgs(args);
		this.command = "java -jar pedia-simulator.jar " + Joiner.on(" ").join(argv);
	}

	/**
	 * 
	 * The main method of the . It should put everything together.
	 * 
	 * @param args
	 *            Arguments of the command line
	 */
	@Override
	public void run() {
		Random random;
		if (options.getSeed() == null)
			random = new Random();
		else
			random = new Random(options.getSeed());

		// 1. Config parsers
		// 1.1 Init 1000G VCF file/
		VCFFileReader backgroundReader = new VCFFileReader(options.getVcfInputFile());
		VCFFileReader mutationReader = new VCFFileReader(options.getVcfMutationFile());

		// 1.2 load gene files from omim used for filtering
		OMIMGeneLoader omimGeneLoader = new OMIMGeneLoader(options.getOMIMFile());
		ImmutableSet<String> genes = omimGeneLoader.load();

		// 1.3 init filter
		ImmutableSet<IFilter> filters = new ImmutableSet.Builder<IFilter>().add(new JannovarGeneInfoFilter(genes))
				.build();
		// 1.4 initial background sampler
		VCFSampler.Builder backgroundBuilder = new VCFSampler.Builder().vcfReader(backgroundReader)
				.seed(random.nextLong()).filters(filters);

		// 1.5 initialize mutation sampler
		VCFSampler.Builder mutationBuilder = new VCFSampler.Builder().vcfReader(mutationReader).seed(random.nextLong());

		// 2 Get the samples to iterate
		List<String> backgroundSamples = backgroundReader.getFileHeader().getSampleNamesInOrder();
		List<String> mutationSamples = mutationReader.getFileHeader().getSampleNamesInOrder();

		// 3 iterate
		// 3.1 iterate over mutation samples
		int i = 0;
		for (String sampleName : mutationSamples) {
			i++;

			// random background sample
			String backgroundName = backgroundSamples.get(random.nextInt(backgroundSamples.size()));

			System.out.printf("Process %d of %d samples (Merge sample %s with %s)", i, mutationSamples.size(),
					sampleName, backgroundName);

			VCFSampler backgroundSampler = backgroundBuilder.sample(backgroundName).build();
			VCFSampler mutationSampler = mutationBuilder.sample(sampleName).build();

			SpikeIn spikeIn = new SpikeIn(backgroundSampler, mutationSampler, true);
			
			
			// 3.2 iterate over variants and write VCF file
			VariantContextWriter writer = new VariantContextWriterBuilder()
					.setOutputFile(options.getFolder() + "/" + sampleName + "_" + backgroundName + ".vcf.gz").build();

			// Header
			VCFHeader header = spikeIn.getVCFHeader();
			header.addMetaDataLine(new VCFHeaderLine("PediaSimulatorSpikeInSample", sampleName));
			header.addMetaDataLine(new VCFHeaderLine("PediaSimulatorBackgroundSample", sampleName));
			header.addMetaDataLine(new VCFHeaderLine("PediaSimulatorCommand", command));
			writer.writeHeader(header);
			
			
			// iterate and write
			variantCount = 0;
			spikeIn.stream().peek(vc -> count()).forEach(vc -> writer.add(vc));
			
			
			// close
			writer.close();
			spikeIn.close();

			System.out.printf(" --- created %s_%s.vcf.gz with %d variants.\n", sampleName, backgroundName, variantCount);
		}

		System.exit(0);

	}

	private void count() {
		variantCount++;
	}

}
