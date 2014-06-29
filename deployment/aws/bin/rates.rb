#!/usr/bin/env ruby

require 'open3'

BASE_URL = 'https://www.kimonolabs.com/api/csv'
API_ID = 'a591sr78'
API_KEY = '7e27ec7260e80b244896e108e788a1ae'

url = "#{BASE_URL}/#{API_ID}?kimnoheaders=2&apikey=#{API_KEY}"

def parse_is(raw)
  if raw =~ /\d+/
    if raw =~ /x/
      count, size = raw.match(/(\d+)\s+x\s+(\d+)/).captures
    else
      count = 1
      size = raw.match(/(\d+)/).captures[0]
    end
    return count.to_i, size.to_i, raw =~ /ssd/i
  else
    return 0, 0, false
  end
end

def fmt_is(raw)
  count, size, ssd = parse_is(raw)
  if count > 0
    instance_storage = '%2i x %4iGB' % [count, size]
    instance_storage += " SSD" if ssd
    return instance_storage
  else
    return 'N/A'
  end
end

def sort_instances(instances_hash)
  case ARGV[0]
  when /type/
    instances_hash.sort_by {|type, i| type}
  when /cpu|cores/
    instances_hash.sort_by {|type, i| i[:vcpu]}
  when /ecu/
    instances_hash.sort_by {|type, i| i[:ecu]}
  when /ram|memory/
    instances_hash.sort_by {|type, i| i[:memory]}
  when /disk|storage/
    instances_hash.sort_by {|type, i| c, s = parse_is(i[:instance_storage]); c * s}
  else
    instances_hash.sort_by {|type, i| i[:rate]}
  end
end

stdin, stdout, stderr = Open3.popen3("curl \"#{url}\"")

instances = Hash.new
while (line = stdout.gets) do
  next if line.strip.length == 0
  line.gsub!('"', '')
  line.chomp!

  type, _, vcpu, ecu, memory, instance_storage, rate = line.split(',')

  instance = Hash.new
  instance[:vcpu] = vcpu.to_i
  instance[:ecu] = ecu.to_i
  instance[:memory] = memory.to_f
  instance[:instance_storage] = instance_storage
  instance[:rate] = rate.match(/(\d+\.\d+)/).captures[0].to_f

  if instance[type]
    puts "WARNING: duplicate type: #{type}"
  else
    instances[type] = instance
  end
end

sort_instances(instances).each do |type, i|
  puts '# %-11s (%2i cores, %3i ECU, %5.1fGB RAM, %-15s) = $%5.3f/hr' % [type, i[:vcpu], i[:ecu], i[:memory], fmt_is(i[:instance_storage]), i[:rate]]
end
