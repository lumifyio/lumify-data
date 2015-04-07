#!/usr/bin/env ruby

require 'yaml'
require 'securerandom'

cluster = Hash.new
File.read(ARGV[0]).each_line do |line|
  ip, name, *aliases_and_comment = line.split(/\s+/)
  aliases = aliases_and_comment.reject {|a| a.match(/#|i-[0-9a-f]{8}/)}

  aliases.each do |a|
    cluster[a] = Hash.new
    cluster[a][:ip] = ip
    cluster[a][:name] = name
  end
end

def get(cluster, regex, type)
  array = cluster.select{|k,v| k.match(regex)}.collect{|k,v| v[type]}.flatten
  if array.size == 0
    nil
  elsif array.size == 1
    array.first
  else
    array
  end
end

def secret
  "#{File.basename(__FILE__)}/#{Time.now.strftime('%Y%m%dT%H%M%S')}/#{SecureRandom.hex(16)}"
end

hiera = Hash.new

if ARGV[1] == '--secrets'
  hiera['accumulo_instance_secret'] = secret
  hiera['rabbitmq_erlang_cookie'] = secret
else
  proxy = get(cluster, /proxy/, :ip)
  hiera['proxy_url'] = 'http://' + proxy + ':8080' if proxy

  syslog = get(cluster, /syslog/, :ip)
  hiera['syslog_server'] = syslog if syslog

  logstash_server = get(cluster, /logstash/, :ip)
  hiera['logstash_server'] = logstash_server if logstash_server

  hiera['hadoop_masters'] = Array(cluster['namenode'][:ip])
  hiera['hadoop_slaves'] = get(cluster, /node\d{2}/, :ip)
  hiera['historyserver_hostname'] = Array(cluster['namenode'][:name])

  hiera['accumulo_example_config'] = '3GB/native-standalone'
  hiera['accumulo_masters'] = Array(cluster['accumulomaster'][:name])
  hiera['accumulo_slaves'] = get(cluster, /node\d{2}/, :name)

  zk_port = 2181
  zk_nodes = Hash.new
  cluster.select{|k,v| k.match(/zk\d/)}.each do |k,v|
    n = k.match(/zk(\d{2})/).captures[0].to_i
    zk_nodes[n] = "#{v[:ip]}:#{zk_port}"
  end
  hiera['zookeeper_port'] = zk_port
  hiera['zookeeper_nodes'] = zk_nodes

  hiera['namenode_ipaddress'] = cluster['namenode'][:ip]
  hiera['namenode_hostname'] = 'namenode'
  hiera['namenode_rpc_address'] = cluster['namenode'][:ip] + ':8020'

  hiera['elasticsearch_locations'] = Array(get(cluster, /es\d{2}/, :ip)).collect{|ip| "#{ip}:9300"}
  hiera['elasticsearch_index_shards'] = hiera['elasticsearch_locations'].count * 5
  hiera['elasticsearch_heapsize'] = 4096

  hiera['ganglia_cluster_name'] = File.basename(ARGV[0], '_hosts')
  hiera['ganglia_server_ip'] = get(cluster, /puppet/, :ip)

  hiera['rabbitmq_nodes'] = get(cluster, /rabbitmq\d{2}/, :name)

  hiera['spark_master'] = get(cluster, 'sparkmaster', :ip)
  hiera['spark_workers'] = get(cluster, /node\d{2}/, :ip)
  hiera['spark_driver_memory'] = '1g'

  hiera['jetty_confidential_redirect_port'] = 443
  hiera['jetty_key_store_path'] = '/opt/lumify/config/lumify.io.jks'
  hiera['jetty_key_store_password'] = 'OBF:1j0u1pxx1nzh1oks1x1d1jg81kfx1w8j1f2s1v981vv11tb01nid1kfr1u9t1hh21vni1ta01nyp1yeq1jdg1ai31rwf1aj71jfu1yfg1nxp1tae1vnk1he01u9p1kcn1nll1t9e1vu91v9e1f1m1w971kch1jd21x191oia1nwx1pwp1j00'
  hiera['jetty_trust_store_path'] = '/opt/lumify/config/lumify.io.jks'
  hiera['jetty_trust_store_password'] = 'OBF:1j0u1pxx1nzh1oks1x1d1jg81kfx1w8j1f2s1v981vv11tb01nid1kfr1u9t1hh21vni1ta01nyp1yeq1jdg1ai31rwf1aj71jfu1yfg1nxp1tae1vnk1he01u9p1kcn1nll1t9e1vu91v9e1f1m1w971kch1jd21x191oia1nwx1pwp1j00'
  hiera['jetty_client_auth'] = 'false'
end

puts YAML.dump(hiera)
