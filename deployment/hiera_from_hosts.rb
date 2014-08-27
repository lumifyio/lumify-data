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

hiera = Hash.new

proxy = get(cluster, /proxy/, :ip)
hiera['proxy_url'] = 'http://' + proxy + ':8080' if proxy

syslog = get(cluster, /syslog/, :ip)
hiera['syslog_server'] = syslog if syslog

logstash_server = get(cluster, /logstash/, :ip)
hiera['logstash_server'] = logstash_server if logstash_server

hiera['hadoop_masters'] = Array(cluster['namenode'][:ip])
hiera['hadoop_slaves'] = get(cluster, /node\d{2}/, :ip)

hiera['accumulo_example_config'] = '3GB/native-standalone'
hiera['accumulo_masters'] = Array(cluster['accumulomaster'][:name])
hiera['accumulo_slaves'] = get(cluster, /node\d{2}/, :name)
hiera['accumulo_instance_secret'] = "#{File.basename(__FILE__)}/#{Time.now.strftime('%Y%m%dT%H%M%S')}/#{SecureRandom.hex(16)}"

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

hiera['rabbitmq_nodes'] = get(cluster, /rabbitmq\d{2}/, :name)
hiera['rabbitmq_erlang_cookie'] = "#{File.basename(__FILE__)}/#{Time.now.strftime('%Y%m%dT%H%M%S')}/#{SecureRandom.hex(16)}"

hiera['storm_nimbus_host'] = get(cluster, /stormmaster/, :ip)
hiera['storm_nimbus_thrift_port'] = 6627
storm_slots_per_supervisor = 4
storn_base_supervisor_slot_port = 6700
hiera['storm_supervisor_slots_ports'] = storm_slots_per_supervisor.times.reduce([]){|m,i| m.push(storn_base_supervisor_slot_port+i)}
hiera['storm_supervisor_jmx_registry_ports'] = storm_slots_per_supervisor.times.reduce([]){|m,i| m.push(('1'+(storn_base_supervisor_slot_port+i).to_s).to_i)}
hiera['storm_supervisor_jmx_objects_ports'] = storm_slots_per_supervisor.times.reduce([]){|m,i| m.push(('2'+(storn_base_supervisor_slot_port+i).to_s).to_i)}
hiera['storm_ui_port'] = 8081

hiera['jetty_confidential_port'] = 443
hiera['jetty_key_store_path'] = '/opt/lumify/config/jetty.jks'
hiera['jetty_key_store_password'] = 'OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v'
hiera['jetty_trust_store_path'] = '/opt/lumify/config/jetty.jks'
hiera['jetty_trust_store_password'] = 'OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v'
hiera['jetty_client_auth'] = 'false'

puts YAML.dump(hiera)
