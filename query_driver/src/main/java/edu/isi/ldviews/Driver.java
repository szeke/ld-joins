package edu.isi.ldviews;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;

import edu.isi.ldviews.query.Keywords;
import edu.isi.ldviews.query.QueryExecutor;
import edu.isi.ldviews.query.QueryExecutorFactory;
import edu.isi.ldviews.query.QueryFactory;
import edu.isi.ldviews.query.QueryFactoryFactory;
import edu.isi.ldviews.query.RunResultSummary;
import edu.isi.ldviews.query.Worker;
import edu.isi.ldviews.query.WorkerResultSummary;

public class Driver {

	private static Logger logger = LoggerFactory.getLogger(Driver.class);

	private String hostname;
	private int portnumber;
	private String indexname;
	private String queryFile;
	private String resultsFile;
	private long randomseed;
	private String keywordFile;
	private String databasetype;
	private int numberofworkers;
	private int concurrentnumberofworkers;
	private int numberoftraces;
	
	public Driver(CommandLine cl)
	{
		parseCommandLineOptions(cl);	
	}

	public static void main(String[] args) {

		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, Driver.class.getSimpleName());
		if(cl == null)
		{
			return;
		}

		try {
			Driver driver = new Driver(cl);

			long start = System.currentTimeMillis();
			driver.drive();
			long end = System.currentTimeMillis();
			System.out.println("Driver took " + (end - start) + " milliseconds");
		} catch (Exception e) {
			logger.error("Error occured while running benchmark!", e);
		}
	}



	private void drive() throws Exception {
		JSONArray keywordsJSON = new JSONArray(IOUtils.toString(new File(keywordFile).toURI()));
		Keywords keywords = new Keywords(keywordsJSON);
		Random rand = new Random(randomseed);
		JSONObject querySpec = new JSONObject(IOUtils.toString(new File(queryFile).toURI()));
		QueryFactory queryFactory = QueryFactoryFactory.getQueryFactory(databasetype);
		
		ExecutorService executor = Executors.newFixedThreadPool(concurrentnumberofworkers);
		List<Future<WorkerResultSummary>> workerResults = new LinkedList<Future<WorkerResultSummary>>();
		for(int i =0; i < numberofworkers; i++)
		{
			long workerSeed = rand.nextLong();
			
			{
			QueryExecutor queryExecutor = QueryExecutorFactory.getQueryExecutor(databasetype, hostname, portnumber, indexname);
		
			workerResults.add(executor.submit(new Worker(queryExecutor, queryFactory, querySpec, keywords, workerSeed, 0.3, numberoftraces)));
			}
		
		}
		
		List<WorkerResultSummary> workerResultSummaries = new LinkedList<WorkerResultSummary>();
		
		for(Future<WorkerResultSummary>workerResult : workerResults)
		{
			workerResultSummaries.add(workerResult.get(10, TimeUnit.MINUTES));
		}
		
		for(WorkerResultSummary summary : workerResultSummaries)
		{
			System.out.println(summary.toJSONObject().toString());
		}
		RunResultSummary runResultSummary = new RunResultSummary(randomseed, workerResultSummaries);
		
		System.out.println(runResultSummary.toJSONObject().toString());
		executor.shutdown();
	}

	protected void parseCommandLineOptions(CommandLine cl) {
		queryFile = (String) cl.getOptionValue("queryfile");
		resultsFile = (String) cl.getOptionValue("resultsfile");
		keywordFile = (String) cl.getOptionValue("keywordsfile");
		randomseed = Long.parseLong((String) cl.getOptionValue("randomseed","0"));
		portnumber = Integer.parseInt((String) cl.getOptionValue("portnumber","9200"));
		
		hostname = (String) cl.getOptionValue("hostname");
		databasetype = (String) cl.getOptionValue("databasetype");
		if(databasetype.compareTo("ES") == 0)
		{
			if(cl.hasOption("indexname"))
			{
				indexname = (String) cl.getOptionValue("indexname");
			}
			else
			{
				logger.error("Need to specify --indexname if using --databasetype ES");
			}
		}
		this.numberofworkers = Integer.parseInt((String) cl.getOptionValue("numworkers","1"));
		this.concurrentnumberofworkers = Integer.parseInt((String) cl.getOptionValue("concurrentnumworkers","1"));
		this.numberoftraces = Integer.parseInt((String) cl.getOptionValue("numberoftraces","1"));
	}

	private static Options createCommandLineOptions() {

		Options options = new Options();
		options.addOption(new Option("hostname", "hostname", true, "hostname for database connection"));
		options.addOption(new Option("portnumber", "portnumber", true, "portnumber for database connection"));
		options.addOption(new Option("indexname", "indexname", true, "elasticsearch index name"));
		options.addOption(new Option("queryfile", "queryfile", true, "query file for loading data"));
		options.addOption(new Option("resultsfile", "resultsfile", true, "specifies where benchmark results are put"));
		options.addOption(new Option("keywordsfile", "keywordsfile", true, "specifies file containing keywords to sample"));
		options.addOption(new Option("randomseed", "randomseed", true, "random seed for benchmark driver"));
		options.addOption(new Option("databasetype", "databasetype", true, "ES or SPARQL"));
		options.addOption(new Option("numworkers", "numworkers", true, "number of workers to execute queries"));
		options.addOption(new Option("concurrentnumworkers", "concurrentnumworkers", true, "number of workers able to execute concurrently"));
		options.addOption(new Option("numberoftraces", "numberoftraces", true, "number of query traces each worker should execute"));
		options.addOption(new Option("help", "help", false, "print this message"));
		return options;
	}
}
