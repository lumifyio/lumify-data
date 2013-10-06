#!/usr/bin/env ruby

MAP = {
  'puppet'         => ['include env::cluster::puppetmaster'],
  /^namenode/      => ['include env::cluster::hadoop_master', 'include env::cluster::oozie_server'],
  'accumulomaster' => ['include env::cluster::hadoop_secondary', 'include env::cluster::accumulo_master'],
  /node\d{2}/      => ['include env::cluster::node'],
  /zk\d{2}/        => ['include env::cluster::zookeeper_server'],
  'www'            => ['include env::cluster::webserver']
}


File.read(ARGV[0]).each_line do |line|
  break if line.match(/^\s*#STOP\s*$/)
  next if line.match(/^\s*#|^\s*$/)

  _, _, _, name, aliases, _ = line.split(/\s+/)
  aliases = aliases.split(/,/)

  puts "node '#{name}' {"
  aliases.each do |alais|
    MAP.keys.each do |regex|
      if alais.match(regex)
        puts MAP[regex].join("\n")
      end
    end
  end
  puts "}"
  puts
end
