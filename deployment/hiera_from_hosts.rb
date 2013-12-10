#!/usr/bin/env ruby

require 'yaml'

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
  array.size == 1 ? array.first : array
end

hiera = Hash.new
hiera['proxy_url'] = 'http://' + get(cluster, /puppet/, :ip) + ':8080'
hiera['hadoop_masters'] = cluster['namenode'][:ip].to_a
hiera['hadoop_slaves'] = get(cluster, /node\d{2}/, :ip)
hiera['accumulo_example_config'] = '3GB/native-standalone'
hiera['accumulo_masters'] = cluster['accumulomaster'][:name].to_a
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
hiera['elasticsearch_locations'] = get(cluster, /node\d{2}/, :ip).collect{|ip| "#{ip}:9300"}
hiera['kafka_host_ipaddress'] = get(cluster, /kafka\d{2}/, :ip).first
hiera['storm_nimbus_host'] = get(cluster, /stormmaster/, :ip)
hiera['storm_nimbus_thrift_port'] = 6627
hiera['storm_supervisor_slots_ports'] = [6700, 6701, 6702, 6703]
hiera['storm_ui_port'] = 8081
hiera['jetty_confidential_port'] = 443
hiera['jetty_key_store_path'] = '/opt/lumify/config/jetty.jks'
hiera['jetty_key_store_password'] = 'OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v'
hiera['jetty_trust_store_path'] = '/opt/lumify/config/jetty.jks'
hiera['jetty_trust_store_password'] = 'OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v'
hiera['jetty_client_auth'] = 'false'
hiera['authentication_provider'] = 'com.altamiracorp.lumify.web.DevBasicAuthenticationProvider'

puts YAML.dump(hiera)
