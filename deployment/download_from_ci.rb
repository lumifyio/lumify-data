#!/usr/bin/env ruby

artifacts = """
io/lumify/lumify-ontology-dev                     with-dependencies.jar
io/lumify/lumify-ccextractor                      with-dependencies.jar
io/lumify/lumify-clavin                           with-dependencies.jar
io/lumify/lumify-email-extractor                  with-dependencies.jar
io/lumify/lumify-enterprise-tools                 with-dependencies.jar
io/lumify/lumify-java-code-ingest                 with-dependencies.jar
io/lumify/lumify-known-entity-extractor           with-dependencies.jar
io/lumify/lumify-mapped-ingest                    with-dependencies.jar
io/lumify/lumify-opencv-object-detector           with-dependencies.jar
io/lumify/lumify-opennlp-dictionary-extractor     with-dependencies.jar
io/lumify/lumify-opennlp-me-extractor             with-dependencies.jar
io/lumify/lumify-phone-number-extractor           with-dependencies.jar
io/lumify/lumify-sphinx                           with-dependencies.jar
io/lumify/lumify-subrip-parser                    with-dependencies.jar
io/lumify/lumify-subrip-transcript                with-dependencies.jar
io/lumify/lumify-tesseract                        with-dependencies.jar
io/lumify/lumify-tika-mime-type                   with-dependencies.jar
io/lumify/lumify-tika-text-extractor              with-dependencies.jar
io/lumify/lumify-youtube-transcript               with-dependencies.jar
io/lumify/lumify-zipcode-extractor                with-dependencies.jar
io/lumify/lumify-wikipedia-mr                     with-dependencies.jar
io/lumify/lumify-import                           with-dependencies.jar
io/lumify/lumify-storm                            with-dependencies.jar
io/lumify/lumify-ldap-auth                        with-dependencies.jar
io/lumify/lumify-web-dev-tools                    .jar
io/lumify/lumify-opennlp-dictionary-extractor-web with-dependencies.jar
io/lumify/lumify-web-war                          .war lumify.war
#org/securegraph/securegraph-tools                 with-dependencies.jar
#com/altamiracorp/bigtable/bigtable-ui-war         .war bigtable.war
#com/altamiracorp/jmxui/jmx-ui-webapp              .war jmx.war
"""

def download(module_name, artifact_name, local_filename)
  result = {}

  print '?'; STDOUT.flush
  ssh_output = `ssh root@ci.lumify.io find /var/www/maven/snapshots/#{module_name} -name \"*#{artifact_name}\" 2>&1`
  if $?.to_i == 0
    result[:remote_filename] = ssh_output.lines.sort.last.chomp
    print "\b"; STDOUT.flush

    print '<'; STDOUT.flush
    scp_output = `scp root@ci.lumify.io:#{result[:remote_filename]} #{local_filename}`
    if $?.to_i == 0
      result[:local_filename] = local_filename
      print "\b."; STDOUT.flush
    else
      result[:error] = scp_output
      print "\b*"; STDOUT.flush
    end
  else
    result[:error] = ssh_output
    print "\b*"; STDOUT.flush
  end

  result
end

def md5(filename)
  if `uname`.chomp == 'Linux'
    `md5sum #{filename}`.split[0]
  else
    `md5 #{filename}`.split[3]
  end
end

def maven_time(filename)
  require 'time'
  y, mon, d, h, min, s = filename.match(/(\d{4})(\d{2})(\d{2})\.(\d{2})(\d{2})(\d{2})/).captures
  Time.parse("#{y}-#{mon}-#{d} #{h}:#{min}:#{s}")
end

def ago(t)
  age = Time.now - t
  if age < 60
    'less than 1 minute ago'
  elsif age < 60 * 60
    sprintf('%.2f minutes ago', age / 60)
  elsif age < 60 * 60 * 24
    sprintf('%.2f hours ago', age / 60 / 60)
  else
    sprintf('%.2f days ago', age / 60 / 60 / 24)
  end
end

artifacts = artifacts.lines
artifacts = artifacts.select {|line| line.match(ARGV[0])} if ARGV[0]

results = []
artifacts.each do |line|
   next if line.strip.length == 0
   next if line.match(/^\s*#/)
   module_name, artifact_name, local_filename = line.split
   if local_filename == nil
     local_filename = module_name.split('/').last + '.' + artifact_name.split('.').last
   end

   results << download(module_name, artifact_name, local_filename)
end
puts

results, errors = results.partition {|result| result[:error] == nil}

max_width = results.map {|result| result[:local_filename].length}.max
results.each do |result|
  sum = md5(result[:local_filename])
  t = maven_time(result[:remote_filename])
  puts "https://ci.lumify.io/fingerprint/%s/? %*s %s" % [sum, -1 * max_width, result[:local_filename], ago(t)]
end

errors.each do |result|
  puts result[:error]
end
