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
import org.apache.commons.math3.random.RandomDataGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.ldviews.query.Keywords;
import edu.isi.ldviews.query.QueryExecutor;
import edu.isi.ldviews.query.QueryExecutorFactory;
import edu.isi.ldviews.query.QueryFactory;
import edu.isi.ldviews.query.QueryFactoryFactory;
import edu.isi.ldviews.query.RunResultSummary;
import edu.isi.ldviews.query.RunTimestampedStatisticsSummary;
import edu.isi.ldviews.query.Worker;
import edu.isi.ldviews.query.WorkerResultSummary;
import edu.isi.ldviews.query.WorkerTimestampedStatisticsSummary;

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
	private double arrivalrate;
	
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
		
		RandomDataGenerator rdg = new RandomDataGenerator();
		rdg.reSeed(randomseed);
		
		ExecutorService executor = Executors.newFixedThreadPool(concurrentnumberofworkers);
		List<QueryExecutor> queryExecutors = new LinkedList<QueryExecutor>();
		List<Future<WorkerResultSummary>> workerResults = new LinkedList<Future<WorkerResultSummary>>();
		List<WorkerResultSummary> workerResultSummaries = new LinkedList<WorkerResultSummary>();
		List<WorkerTimestampedStatisticsSummary> workerTimestampedStatisticsSummaries = new LinkedList<WorkerTimestampedStatisticsSummary>();
		List<Worker> workers = new LinkedList<Worker>();
		try
		{
 		for(int i =0; i < numberofworkers; i++)
		{
			double waitTime = rdg.nextExponential(1.0/ arrivalrate);
			Thread.sleep((long) (waitTime *1000));
			
			long workerSeed = rand.nextLong();
			
			{
			QueryExecutor queryExecutor = QueryExecutorFactory.getQueryExecutor(databasetype, hostname, portnumber, indexname);
			queryExecutors.add(queryExecutor);
			Worker worker = new Worker(queryExecutor, queryFactory, querySpec, keywords, workerSeed, 0.3, numberoftraces);
			workers.add(worker);
			workerResults.add(executor.submit(worker));
			}
			boolean ready = true;
			while(workerResults.size() > 0 && ready)
			{
				if(workerResults.get(0).isDone())
				{
					
					Future<WorkerResultSummary> summaryFuture = workerResults.remove(0);
					Worker worker = workers.remove(0);
					logger.info("Driver has collected " + worker.getSeed());
					try{
						WorkerResultSummary summary = summaryFuture.get();
						if(null != summary.getException() && !(summary.getException() instanceof java.util.concurrent.TimeoutException))
						{
							logger.error("Cancelling execution because of exception from worker " + summary.getSeed(),summary.getException());
							while(!workerResults.isEmpty())
							{
								workerResults.remove(0).cancel(true);
							}
							while(!workers.isEmpty())
							{
								Worker cancelledWorker = workers.remove(0);
								workerResultSummaries.add(cancelledWorker.getSummary());
								workerTimestampedStatisticsSummaries.add(cancelledWorker.getTimestampedStatisticsSummary());
							}
							
							i = numberofworkers;
						}
						else
						{
							workerResultSummaries.add(summary);
							workerTimestampedStatisticsSummaries.add(worker.getTimestampedStatisticsSummary());
						}
					}
					catch (Exception e)
					{
						logger.error("Unable to complete worker", e);
					}
						
				}
				else
				{
					ready = false;
				}
			}
		}
		
		
		try
		{
		for(Future<WorkerResultSummary>workerResult : workerResults)
		{
			WorkerResultSummary summary = workerResult.get(1, TimeUnit.HOURS);
			workerResultSummaries.add(summary);
			logger.info("Driver has collected " + summary.getSeed());
			workerTimestampedStatisticsSummaries.add(workers.remove(0).getTimestampedStatisticsSummary());
			
		}
		}
		catch(Exception e)
		{
			logger.error("Unable to collect remaining worker results ", e);
		}
		}
		finally{
			executor.shutdownNow();
			
			for(QueryExecutor queryExecutor : queryExecutors)
			{
				try{
				queryExecutor.shutdown();
				}
				catch (Exception e)
				{
					
				}
			}
		}
		for(WorkerResultSummary summary : workerResultSummaries)
		{
			System.out.println(summary.toJSONObject().toString());
		}
		
		
		RunResultSummary runResultSummary = new RunResultSummary(randomseed, workerResultSummaries);
		runResultSummary.setArrivalRate(arrivalrate);
		runResultSummary.setDatabaseType(databasetype);
		runResultSummary.setMaxConcurrency(concurrentnumberofworkers);
		runResultSummary.setNumWorkers(numberofworkers);
		runResultSummary.setNumTraces(numberoftraces);
		System.out.println(runResultSummary.toJSONObject().toString());
		System.out.println(runResultSummary.toCSV());
		
		RunTimestampedStatisticsSummary runTimestampedStatisticsSummary = new RunTimestampedStatisticsSummary(randomseed, workerTimestampedStatisticsSummaries);
		runTimestampedStatisticsSummary.setArrivalRate(arrivalrate);
		runTimestampedStatisticsSummary.setDatabaseType(databasetype);
		runTimestampedStatisticsSummary.setMaxConcurrency(concurrentnumberofworkers);
		runTimestampedStatisticsSummary.setNumWorkers(numberofworkers);
		runTimestampedStatisticsSummary.setNumTraces(numberoftraces);
		System.out.println(runTimestampedStatisticsSummary.toCSV());
		
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
		this.arrivalrate = Double.parseDouble((String) cl.getOptionValue("arrivalrate", "0.3"));
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
		options.addOption(new Option("arrivalrate", "arrivalrate", true, "worker arrival rate"));
		options.addOption(new Option("help", "help", false, "print this message"));
		return options;
	}
}
