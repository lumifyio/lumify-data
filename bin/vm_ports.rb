#!/usr/bin/env ruby

class Vagrant
  def self.configure(version)
  end
end
load 'Vagrantfile'

def get_vm_id(vm_name)
  File.read(".vagrant/machines/#{vm_name}/virtualbox/id")
end

def get_portmap(vm_id)
  portmap = Hash.new
  `VBoxManage showvminfo #{vm_id} --machinereadable`.each_line do |line|
    # Forwarding(0)="ssh,tcp,127.0.0.1,2222,,22"
    match_data = /^Forwarding.*=".*,.*,.*,(\d+),.*,(\d+)"$/.match(line)
    next unless match_data
    portmap[match_data[2].to_i] = match_data[1].to_i
  end
  portmap
end

def puts_heading(heading)
  puts heading
  puts '-' * heading.length
end

def display_portmap(portmap, verbose=true)
  FORWARD_PORTS.each do |vm_ports, description|
    heading = false
    Array(vm_ports).each do |vm_port|
      host_port = portmap[vm_port]
      warning = host_port != vm_port ? ' [WARNING: host port has been auto-corrected]' : nil
      if warning || verbose
        unless heading
          puts_heading description
          heading = true
        end
        puts '%5d <-- %5d%s' % [vm_port, host_port, warning]
      end
    end
    puts if heading
  end
end

def check_properties(portmap, verbose=true)
  Dir['/opt/lumify/config/*.properties'].each do |properties_filename|
    heading = false
    File.read(properties_filename).each_line do |line|
      line.scan(/\d+/).each do |match|
        i = match.to_i
        if FORWARD_PORTS.keys.flatten.include?(i) && portmap[i] != i
          unless heading
            puts_heading properties_filename
            heading = true
          end
          puts line.chomp + ' --> ' + line.gsub(i.to_s, portmap[i].to_s).chomp
        end
      end
    end
    puts if heading
  end
end

vm_id = get_vm_id(ARGV[0])
portmap = get_portmap(vm_id)
display_portmap(portmap, false)
check_properties(portmap)
