/usr/lib/spark/bin/spark-submit \
     --master yarn-client \
	--executor-memory 80g  --executor-cores 5  --num-executors 40 \
    --py-files merger.zip \
    framer.py \
   $@
