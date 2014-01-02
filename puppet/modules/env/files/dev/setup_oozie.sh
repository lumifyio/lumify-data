oozie_home=/vagrant/oozie
oozie_libs=$oozie_home/target/oozie-libs

echo 'removing old oozie files'
sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -test -e oozie-libs
libs_exist=$?
sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -test -e oozie-libs
jobs_exist=$?

if [ ${libs_exist} -eq 0 ]; then
	sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -rmr oozie-libs
fi

if [ ${jobs_exist} -eq 0 ]; then
	sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -rmr oozie-jobs
fi

if [ ! -d ${oozie_libs} ]; then
	echo 'running maven to get the latest oozie-libs...' >&2
    mvn_output="$(cd ${oozie_home}/.. && mvn clean package -DskipTests)"
    mvn_exit=$?
    if [ ${mvn_exit} -ne 0 ]; then
      echo "${mvn_output}"
      exit ${mvn_exit}
    fi
    echo 'maven finished.' >&2
fi

echo 'pushing new oozie files/libs to hdfs'
sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -put $oozie_home/jobs oozie-jobs
sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -put $oozie_libs oozie-libs