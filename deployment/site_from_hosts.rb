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
  ip, name, *aliases_and_comment = line.split(/\s+/)
  aliases = aliases_and_comment.reject {|a| a.match(/#|i-[0-9a-f]{8}/)}

  puts "node '#{name}' {"
  aliases.each do |a|
    MAP.keys.each do |regex|
      if a.match(regex)
        puts MAP[regex].join("\n")
      end
    end
  end
  puts "}"
  puts
end
