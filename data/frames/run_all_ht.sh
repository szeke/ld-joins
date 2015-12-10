#echo webpage
#time ./run.sh ../ht-data-to-frame/webpage.json-ld ../ht-data-to-frame/types-rdd.json hdfs://memex/user/worker/framed/ht/california-ads/trial01/webpage text
#echo offer
#time ./run.sh ../ht-data-to-frame/offer.json-ld ../ht-data-to-frame/types-rdd.json hdfs://memex/user/worker/framed/ht/california-ads/trial01/offer text
echo adultservice
time ./run.sh ../ht-data-to-frame/adultservice.json-ld ../ht-data-to-frame/types-rdd.json hdfs://memex/user/worker/framed/ht/california-ads/trial01/adultservice text
echo email
time ./run.sh ../ht-data-to-frame/email.json-ld ../ht-data-to-frame/types-rdd.json hdfs://memex/user/worker/framed/ht/california-ads/trial01/email text
echo phone
time ./run.sh ../ht-data-to-frame/phone.json-ld ../ht-data-to-frame/types-rdd.json hdfs://memex/user/worker/framed/ht/california-ads/trial01/phone text
echo seller
time ./run.sh ../ht-data-to-frame/seller.json-ld ../ht-data-to-frame/types-rdd.json hdfs://memex/user/worker/framed/ht/california-ads/trial01/seller text
