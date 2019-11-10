package de.charite.compbio.pediasimulator.cli;

import java.io.File;
import java.util.function.BiFunction;

import de.charite.compbio.pediasimulator.cmd.JsonWithVCFExtenderCommand;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class JsonWithVCFExtenderOptions {

	private File vcfInputFile;
	private File omimFile;
	private File jsonFile;
	private File outputFile;
	private int sampleIndex;

	public static void setupParser(Subparsers subparsers) {
		BiFunction<String[], Namespace, JsonWithVCFExtenderCommand> handler = (argv, args) -> {
			try {
				return new JsonWithVCFExtenderCommand(argv, args);
			} catch (CommandLineParsingException e) {
				throw new RuntimeException("Could not parse command line", e);
			}
		};
		Subparser parser = subparsers.addParser("extend-json").aliases("extendjson").setDefault("cmd", handler);

		parser.addArgument("-v", "--variants").type(File.class).nargs(1).required(true)
				.help("VCF file of input variants  (annotated!)");
		parser.addArgument("-j", "--json-file").type(File.class).nargs(1).required(true).help("VCF file of mutations");
		parser.addArgument("-out", "--output-file").type(File.class).nargs(1).required(true)
				.help("VCF file of input variants");
		parser.addArgument("-o", "--omim").type(File.class).nargs(1).required(true).help("OMIM genemap2 file!");
		parser.addArgument("-s", "--sample-index").type(Integer.class).setDefault(0).help("Index of sample which would like to analyze in multi-vcf.");

		parser.defaultHelp(true);
	}

	public void setFromArgs(Namespace res) {
		this.vcfInputFile = (File) res.getList("variants").get(0);
		this.jsonFile = (File) res.getList("json_file").get(0);
		this.omimFile = (File) res.getList("omim").get(0);
		this.outputFile = (File) res.getList("output_file").get(0);
		this.sampleIndex = (Integer) res.get("sample_index");
	}

	public File getVcfInputFile() {
		return vcfInputFile;
	}

	public File getOMIMFile() {
		return omimFile;
	}

	public File getJsonFile() {
		return jsonFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public Integer getSampleIndex() {
		return sampleIndex;
	}
}
