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

hiera = Hash.new
hiera['proxy_url'] = 'http://10.0.3.10:8080'
hiera['hadoop_masters'] = cluster['namenode'][:ip].to_a
hiera['hadoop_slaves'] = cluster.select{|k,v| k.match(/node\d{2}/)}.collect{|k,v| v[:ip]}.flatten
hiera['accumulo_example_config'] = '3GB/native-standalone'
hiera['accumulo_masters'] = cluster['accumulomaster'][:name].to_a
hiera['accumulo_slaves'] = cluster.select{|k,v| k.match(/node\d{2}/)}.collect{|k,v| v[:name]}.flatten
zk_nodes = Hash.new
cluster.select{|k,v| k.match(/node\d/)}.each do |k,v|
  n = k.match(/node(\d{2})/).captures[0].to_i
  zk_nodes[n] = "#{v[:ip]}:2181"
end
hiera['zookeeper_nodes'] = zk_nodes
hiera['namenode_ipaddress'] = cluster['namenode'][:ip].first
hiera['elasticsearch_locations'] = cluster.select{|k,v| k.match(/node\d{2}/)}.collect{|k,v| v[:ip]}.flatten.collect{|ip| "#{ip}:9300"}
hiera['jetty_confidential_port'] = 443
hiera['jetty_key_store_path'] = '/opt/lumify/config/lumify_demo.jks'
hiera['jetty_key_store_password'] = 'OBF:1wfw1xtz1uo71tok1s3q20zj1s3c1toa1unr1xtj1wg2'
hiera['authentication_provider'] = 'com.altamiracorp.lumify.web.DevBasicAuthenticationProvider'

puts YAML.dump(hiera)
