package de.charite.compbio.pediasimulator.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import de.charite.compbio.pediasimulator.cmd.SpikeInCommand;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class SpikeInOptions {

	private File vcfInputFile;
	private File vcfMutationFile;
	private File omimFile;
	private File outputFile;
	private Integer seed;
	private List<String> samples;

	public static void setupParser(Subparsers subparsers) {
		BiFunction<String[], Namespace, SpikeInCommand> handler = (argv, args) -> {
			try {
				return new SpikeInCommand(argv, args);
			} catch (CommandLineParsingException e) {
				throw new RuntimeException("Could not parse command line", e);
			}
		};
		Subparser parser = subparsers.addParser("spike-in").aliases("spikein").setDefault("cmd", handler);

		parser.addArgument("-v", "--variants").type(File.class).nargs(1).required(true)
				.help("VCF file of input variants (background)");
		parser.addArgument("-m", "--mutations").type(File.class).nargs(1).required(true).help("VCF file of mutations");
		parser.addArgument("-out", "--output").type(File.class).nargs(1).required(true)
				.help("Putput VCF file of spikein variants");
		parser.addArgument("-o", "--omim").type(File.class).nargs(1).required(true).help("OMIM genemap2 file!");
		parser.addArgument("-s", "--seed").type(Integer.class).nargs("?").help("Seed for picking pick random sample.");
		parser.addArgument("--sample").type(String.class).setDefault(new ArrayList<>()).nargs("*").help(
				"Set one or multiple sample names of the mutation file to generate spike in only these samples instead of all.");

		parser.defaultHelp(true);
	}

	public void setFromArgs(Namespace res) {
		this.vcfInputFile = (File) res.getList("variants").get(0);
		this.vcfMutationFile = (File) res.getList("mutations").get(0);
		this.omimFile = (File) res.getList("omim").get(0);
		this.outputFile = (File) res.getList("output").get(0);
		this.seed = res.getInt("seed");
		this.samples = res.getList("sample");
	}

	public File getVcfInputFile() {
		return vcfInputFile;
	}

	public File getOMIMFile() {
		return omimFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public File getVcfMutationFile() {
		return vcfMutationFile;
	}
	
	public List<String> getSamples() {
		return samples;
	}

	public Integer getSeed() {
		return seed;
	}

}
