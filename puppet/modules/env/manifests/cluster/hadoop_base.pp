class env::cluster::hadoop_base inherits env::cluster::base {
  include env::common::logstash_config
  include logstash::client
}