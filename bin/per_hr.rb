#!/usr/bin/env ruby

# read a "*_instances.txt" file and based on the rates documented
# in comments at the top of the file, report approximately how
# much it will cost to run the instances and EBS storage listed

ARGV.each do |filename|
  prefix = filename.match(/^([0-9a-z_]+)_instances/i).captures[0]

  File.open(filename, 'r') do |file|
    rates = {}
    ebs_rate = 0.0
    instance_counts = {}
    ebs_gbs = 0

    file.each_line do |line|
      if line.match(/^\s*#\s+(.+?)\s+\(.+\)\s+=\s+\$(\d+\.\d+)\/hr\s*$/)
        instance_type = $1
        rate = $2.to_f
        rates[instance_type] = rate
        next
      end
      if line.match(/^\s*#\s+EBS\s+=\s+\$(\d+\.\d+)\/GB-month\s*$/)
        ebs_rate = $1.to_f
        next
      end
      break if line.match(/^\s*#STOP\s*$/)
      next if line.match(/^\s*#|^\s*$/)

      instance_type, storage, ip, name, field5, field6 = line.split(/\s+/)
      if field6
        aliases = field5.split(/,/)
        placement_group = field6.match(/\[(.*)\]/).captures[0]
      elsif field5
        if field5.match(/\[(.*)\]/)
          placement_group = $1
        else
          aliases = field5.split(/,/)
        end
      end

      ebs, instance_storage = storage.split(',')
      size_int, size_units = ebs.match(/(\d+)([GT])/).captures
      size_gb = case size_units
      when 'G'
        size_int.to_i
      when 'T'
        size_int.to_i * 1024
      else
        raise "unexpected volume size: #{size}"
      end
      ebs_gbs += size_gb

      instance_counts[instance_type] ||= 0
      instance_counts[instance_type] += 1
    end # line

    w1 = rates.keys.map(&:length).max
    grand_total = 0.0
    rates.sort.each do |instance_type, rate|
      if instance_counts[instance_type]
        total = rate * instance_counts[instance_type]
        grand_total += total
        total_s = '%02.2f' % [total]
        puts '%*s %5d @ $%02.2f = $%5s' % [-1 * w1, instance_type, instance_counts[instance_type], rate, total_s]
      end
    end

    ebs_total = (ebs_rate / (30 * 24)) * ebs_gbs
    grand_total += ebs_total
    ebs_s = '%02.2f' % [ebs_total]
    puts '%*s %5d @ $%02.2f = $%5s' % [-1 * w1, 'EBS', ebs_gbs, ebs_rate, ebs_s]

    puts '%*s $%04.2f' % [w1 + 16, ' ', grand_total]
  end # file
end # filename
