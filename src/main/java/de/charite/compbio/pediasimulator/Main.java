package de.charite.compbio.pediasimulator;

import java.util.function.BiFunction;

import de.charite.compbio.pediasimulator.cli.BuildSampleDBOptions;
import de.charite.compbio.pediasimulator.cli.SpikeInOptions;
import de.charite.compbio.pediasimulator.cmd.ICommand;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Main class to run the pedia simulator more at {@link #main(String[])}
 * 
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 *
 */
public class Main {

	public static void main(String[] args) {

		// 1. Initialize
		// 1.1. Initialize parser and parse arguments
		ArgumentParser parser = ArgumentParsers.newArgumentParser("").description("PEDIA simulator.");
		Subparsers subparsers = parser.addSubparsers().title("subcommands").description("valid subcommands")
				.help("additional help");
		BuildSampleDBOptions.setupParser(subparsers);
		SpikeInOptions.setupParser(subparsers);

		// Parse command line arguments
		Namespace res = null;
		try {
			res = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		BiFunction<String[], Namespace, ICommand> factory = res.get("cmd");
		ICommand cmd = factory.apply(args, res);
		if (cmd == null)
			System.exit(1);

		// Execute the command.
		cmd.run();

		System.exit(0);

	}

}
