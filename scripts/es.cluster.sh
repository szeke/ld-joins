java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 31 --databasetype ES --numworkers 100 --concurrentnumworkers 1 --numberoftraces 5 --arrivalrate 10 > es.cluster.100w.1c.5t.10r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 20 --databasetype ES --numworkers 1 --concurrentnumworkers 10 --numberoftraces 5 --arrivalrate 10 > es.cluster.1w.10c.5t.10r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 21 --databasetype ES --numworkers 10 --concurrentnumworkers 10 --numberoftraces 5 --arrivalrate 0.1 > es.cluster.10w.10c.5t.01r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 22 --databasetype ES --numworkers 10 --concurrentnumworkers 10 --numberoftraces 5 --arrivalrate 1 > es.cluster.10w.10c.5t.1r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 23 --databasetype ES --numworkers 10 --concurrentnumworkers 10 --numberoftraces 5 --arrivalrate 10 > es.cluster.10w.10c.5t.10r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 24 --databasetype ES --numworkers 100 --concurrentnumworkers 10 --numberoftraces 5 --arrivalrate 0.1 > es.cluster.100w.10c.5t.01r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 25 --databasetype ES --numworkers 100 --concurrentnumworkers 10 --numberoftraces 5 --arrivalrate 1 > es.cluster.100w.10c.5t.1r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 26 --databasetype ES --numworkers 100 --concurrentnumworkers 10 --numberoftraces 5 --arrivalrate 10 > es.cluster.100w.10c.5t.10r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 27 --databasetype ES --numworkers 100 --concurrentnumworkers 100 --numberoftraces 5 --arrivalrate 0.1 > es.cluster.100w.100c.5t.01r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 28 --databasetype ES --numworkers 100 --concurrentnumworkers 100 --numberoftraces 5 --arrivalrate 1 > es.cluster.100w.100c.5t.1r.out
sleep 1m
java -cp ld-views-driver-0.0.1-SNAPSHOT-shaded.jar  edu.isi.ldviews.Driver --hostname $1 --portnumber 9200 --indexname dig-ht-california-ads-trial01-index-fixed --queryfile query-specs.json --resultsfile src/test/resources/output.json --keywordsfile  keywords.json  --randomseed 29 --databasetype ES --numworkers 100 --concurrentnumworkers 100 --numberoftraces 5 --arrivalrate 10 > es.cluster.100w.100c.5t.10r.out
sleep 1m