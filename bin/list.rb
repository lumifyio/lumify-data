#!/usr/bin/env ruby

# with the expected value of an AWS tag or a "*_instances.txt" file
# list the matching AWS instances including attached and unattached
# EBS volumes, show the size of the volumes with "-v"

reddisk_tag_value_or_filename = ARGV[0]
if ['-v', '--volume-size'].include?(ARGV[1])
  volume_size = true
end

if File.readable?(reddisk_tag_value_or_filename)
  instance_ids = File.read(reddisk_tag_value_or_filename).lines.collect {|line| line.match(/# (i-.*)$/).captures[0]}
else
  states = ['pending', 'running', 'shutting-down', 'terminated', 'stopping', 'stopped'] - ['terminated']
  state_filters = states.collect {|state| "--filter \"instance-state-name=#{state}\""}.join(' ')
  output = `ec2-describe-instances --filter "tag:reddisk=#{reddisk_tag_value_or_filename}" #{state_filters}`
  instance_ids = output.lines.select {|line| line.match(/^INSTANCE/)}.collect {|line| line.split[1]}

  other_volumes = {}
  statuses = ['creating', 'available', 'in-use', 'deleting', 'deleted', 'error'] - ['in-use']
  status_filters = statuses.collect {|status| "--filter \"status=#{status}\""}.join(' ')
  output = `ec2-describe-volumes --filter "tag:reddisk=#{reddisk_tag_value_or_filename}" #{status_filters}`
  output.lines.each do |line|
    case line.split[0]
    when 'VOLUME'
      volume_id, size, possible_snap, zone_or_status, status_without_snap = line.split[1..5]
      if statuses.include?(zone_or_status)
        status = zone_or_status
      else
        status = status_without_snap
      end
      other_volumes[volume_id] = {:size => size, :status => status}
    when 'TAG'
      volume_id, key, value = line.split[2..4]
      other_volumes[volume_id][:Name] = value if key == 'Name'
    end
  end
end

instances = {}
instance_ids.each do |instance_id|
  output = `ec2-describe-instances #{instance_id}`
  instances[instance_id] = {}

  output.lines.each do |line|
    case line.split[0]
    when 'BLOCKDEVICE'
      device, volume_id = line.split[1..2]
      instances[instance_id][:volumes] ||= {}
      instances[instance_id][:volumes][volume_id] = {:device => device}
    when 'NICASSOCIATION'
      public_ip = line.split[1]
      instances[instance_id][:public_ip] = public_ip
    when 'TAG'
      key, value = line.split[3..4]
      case key
      when 'Name'
        instances[instance_id][:Name] = value
      when 'aliases'
        instances[instance_id][:aliases] = value
      end
    end
    # TODO: collect ami, instance_type?
  end
end

instances.each do |instance_id, data|
  puts instance_id
  names = data[:Name]
  names << ',' + data[:aliases] if data[:aliases]
  puts names
  puts data[:public_ip] if data[:public_ip]
  if volume_size
    output = `ec2-describe-volumes #{data[:volumes].keys.join(' ')}`
    output.lines.each do |line|
      if line.split[0] == 'VOLUME'
        volume_id, size = line.split[1..2]
        data[:volumes][volume_id][:size] = size
      end
    end
  end
  data[:volumes].sort_by {|k,v| v[:device]}.each do |volume_id, h|
    volume_line = '%s %-9s %6s' % [volume_id, h[:device], h[:size] ? h[:size] + 'GB' : nil]
    puts volume_line.strip
  end
  puts
end

if other_volumes && other_volumes.size > 0
  puts
  other_volumes.each do |volume_id, h|
    volume_line = '%s %-9s %6s %-9s %s' % [volume_id, nil, h[:size] + 'GB', h[:status], h[:Name]]
    puts volume_line.strip
  end
end

puts '# ' + instance_ids.join(' ') if instance_ids.size > 0
puts '# for v in ' + other_volumes.keys.join(' ') + '; do echo $v; done' if other_volumes && other_volumes.size > 0
