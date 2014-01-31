#!/usr/bin/env ruby

# read a "*_instances.txt" file and launch the instances described
# creating and attaching the specifed EBS and instance-store volumes,
# disable entries as a comments ("#") and ignore the remainder of the
# file with "#STOP", produces ".log" and hosts files

INSTANCE_STORE_DEVICE_LETTERS = ('b'..'e').to_a
EBS_VOLUME_DEVICE_LETTERS     = ('f'..'z').to_a
REQUIRED_ENV_VARIABLES        = ['AWS_ACCESS_KEY',
                                 'AWS_SECRET_KEY',
                                 'EC2_URL'
                                ]
REQUIRED_CONFIG_KEYS          = ['default_availability_zone',
                                 'default_subnet',
                                 'default_security_groups',
                                 'default_keypair',
                                 'default_ami'
                                ]

# http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/placement-groups.html#concepts-placement-groups
PLACEMENT_GROUP_TYPES         = ['c3.large', 'c3.xlarge', 'c3.2xlarge', 'c3.4xlarge', 'c3.8xlarge', 'cc2.8xlarge',
                                 'cg1.4xlarge', 'g2.2xlarge',
                                 'cr1.8xlarge',
                                 'hi1.4xlarge', 'hs1.8xlarge', 'i2.xlarge', 'i2.2xlarge', 'i2.4xlarge', 'i2.8xlarge'
                                ]

def check_env
  missing_env_variables = REQUIRED_ENV_VARIABLES.select do |env_var|
    ENV[env_var] == nil || ENV[env_var].length == 0
  end

  if missing_env_variables.count > 0
    puts 'ERROR: the following required environment variables are not set:'
    missing_env_variables.each do |env_var|
      puts '  ' + env_var
    end
    false
  else
    true
  end
end

def load_config(config_file)
  require 'yaml'
  hash = YAML.load(File.read(config_file))

  missing_config_keys = REQUIRED_CONFIG_KEYS.select do |key|
    hash[key] == nil || hash[key].length == 0
  end

  if missing_config_keys.count > 0
    puts 'ERROR: the following required keys are not set in spinup.yml:'
    missing_config_keys.each do |key|
      put '  ' + key
    end
    false
  else
    hash
  end
end

def log(msg)
  @log ||= File.open(File.basename($0, '.rb') + '.log', 'w')
  @log.puts msg
  @log.flush
end

def run(cmd)
  puts cmd
  log(cmd)

  output = `#{cmd}`
  log(output)

  unless $?.to_i == 0
    puts output if output && output.length > 0
    raise $?.to_s
  end

  output
end

def tag(resource_id, hash)
  cmd = "ec2-create-tags #{resource_id}"
  hash.each do |k, v|
    cmd << " \\\n  --tag '#{k}=#{v}'"
  end

  run(cmd)
end

def create_volume(name, size_gb, snapshot=nil, availability_zone=nil)
  availability_zone ||= DEFAULT_AVAILABILITY_ZONE

  cmd = 'ec2-create-volume'
  cmd << " \\\n  --size #{size_gb}"
  cmd << " \\\n  --snapshot #{snapshot}" if snapshot
  cmd << " \\\n  --availability-zone #{availability_zone}" if availability_zone

  output = run(cmd)
  volume_id = output.split(/\s+/)[1]

  tag(volume_id, {:Name => name})

  volume_id
end

def attach_volume(volume_id, instance_id, device_name)
  cmd = "ec2-attach-volume #{volume_id}"
  cmd << " \\\n  --instance #{instance_id}"
  cmd << " \\\n  --device #{device_name}"

  run(cmd)
end

def ebs_volumes(name, size)
  size_int, size_units = size.match(/(\d+)([GT])/).captures
  size_gb = case size_units
  when 'G'
    size_int.to_i
  when 'T'
    size_int.to_i * 1024
  else
    raise "unexpected volume size: #{size}"
  end

  created_gb = 0
  volume_number = 1
  volume_ids = []
  while created_gb < size_gb
    gb = [size_gb - created_gb, 1024].min
    volume_ids << create_volume("#{name}_vol#{volume_number}", gb)
    created_gb += gb
    volume_number += 1
  end

  volume_ids
end

def block_device_mapping(instance_store_volume_count)
  options = []

  device_letters = INSTANCE_STORE_DEVICE_LETTERS.clone
  i = 0
  instance_store_volume_count.times do
    options <<  "--block-device-mapping '/dev/sd#{device_letters.shift}=ephemeral#{i}'"
    i += 1
  end

  options.join(' ')
end

def run_instance(instance_type, ip, name, options={})
  instance_store_volume_count = options[:instance_store_volume_count]
  user_data                   = options[:user_data]
  security_groups             = options[:security_groups] || DEFAULT_SECURITY_GROUPS
  keypair                     = options[:keypair] || DEFAULT_KEYPAIR
  subnet                      = options[:subnet] || DEFAULT_SUBNET
  availability_zone           = options[:availability_zone] || DEFAULT_AVAILABILITY_ZONE
  placement_group             = options[:placement_group]

  if user_data
    require 'tempfile'
    user_data_file = Tempfile.new(name + '_user_data_', '/tmp')
    user_data_file.write(user_data)
    user_data_file.flush
  end

  cmd = "ec2-run-instances #{DEFAULT_AMI}"
  cmd << " \\\n  #{block_device_mapping(instance_store_volume_count)}" if instance_store_volume_count
  cmd << " \\\n  --user-data-file #{user_data_file.path}" if user_data
  security_groups.each do |security_group|
    cmd << " \\\n  --group #{security_group}"
  end
  cmd << " \\\n  --key #{keypair}"
  cmd << " \\\n  --subnet #{subnet}"
  cmd << " \\\n  --instance-type #{instance_type}"
  cmd << " \\\n  --availability-zone #{availability_zone}"
  if placement_group
    if PLACEMENT_GROUP_TYPES.include?(instance_type)
      cmd << " \\\n  --placement-group #{placement_group}"
    else
      puts "WARNING: placement groups are not supported for instance type: #{instance_type}"
    end
  end
  cmd << " \\\n  --private-ip-address #{ip}"

  output = run(cmd)
  instance_id = output.lines.find {|line| line.match(/^INSTANCE/)}.split(/\s+/)[1]

  if user_data
    user_data_file.close!
  end

  tag(instance_id, {:Name => name})

  instance_id
end

def get_volume_id(instance_id, device_name='/dev/sda*')
  cmd = "ec2-describe-volumes --filter \"attachment.instance-id=#{instance_id}\" --filter \"attachment.device=#{device_name}\""
  output = run(cmd)
  volume_id = output.lines.find {|line| line.match(/^VOLUME/)}.split(/\s+/)[1]
end

def stop_instance(instance_id)
  cmd = "ec2-stop-instances #{instance_id}"
  run(cmd)
end

def start_instance(instance_id)
  cmd = "ec2-start-instances #{instance_id}"
  run(cmd)
end

def mime_multipart(hash)
  require 'rubygems'
  require 'mime'

  mixed = MIME::MultipartMedia::Mixed.new
  hash.each do |type, body|
    text_media = MIME::TextMedia.new(body, type)
    mixed.add_entity(text_media)
  end

  mixed.to_s
end


config_file = ARGV.first.match(/\.yml$/) ? ARGV.shift : 'spinup.yml' # TODO: search up if not in the current dir
check_env || exit
config = load_config(config_file) || exit
DEFAULT_AVAILABILITY_ZONE = config['default_availability_zone']
DEFAULT_SUBNET            = config['default_subnet']
DEFAULT_SECURITY_GROUPS   = config['default_security_groups']
DEFAULT_KEYPAIR           = config['default_keypair']
DEFAULT_AMI               = config['default_ami']

ARGV.each do |filename|
  prefix = filename.match(/^([0-9a-z_]+)_instances/).captures[0]
  host_entries = []

  File.open(filename, 'r') do |file|
    file.each_line do |line|
      break if line.match(/^\s*#STOP\s*$/)
      next if line.match(/^\s*#|^\s*$/)
      puts
      log("\n" + '# ' + Time.now.strftime('%Y-%m-%d %H:%M:%S'))
      log('# ' + line)

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
      label_prefix = name.match(/-(\d+)$/).captures[0]

      if ebs == '0'
        volume_ids = []
      else
        volume_ids = ebs_volumes(name, ebs)
        volume_ids.each do |volume_id|
          tag(volume_id, {:Project => prefix})
        end
      end

      cloud_config = """
        #cloud-config
        cloud_type: auto
        disable_root: false
        ssh_import_id: root
        cloud_config_modules:
        - ssh-import-id
        - ssh
      """.gsub(/^\n|^ +|\n$/, '')
      script = """
        #!/bin/bash
        sed -i'' -e 's/^PermitRootLogin.*$/PermitRootLogin without-password/' /etc/ssh/sshd_config
        service sshd restart
      """.gsub(/^\n|^ +|\n$/, '')
      user_data = mime_multipart('text/cloud-config' => cloud_config, 'text/x-shellscript' => script)

      instance_id = run_instance(instance_type, ip, name,
                                 :instance_store_volume_count => instance_storage ? instance_storage.match(/^(\d+)i/).captures[0].to_i : nil,
                                 :user_data => user_data,
                                 :placement_group => placement_group
                                )

      # TODO: poll for ready?
      sleep(5)

      tag(instance_id, {:Project => prefix})
      tag(instance_id, {:aliases => aliases.join(',')}) if aliases

      volume_id = get_volume_id(instance_id)
      tag(volume_id, {:Name => "#{name}_vol0", :Project => prefix})

      # TODO: poll for ready?
      sleep(10)

      device_prefix = '/dev/sd'
      device_letters = EBS_VOLUME_DEVICE_LETTERS.clone
      volume_ids.each do |volume_id|
        attach_volume(volume_id, instance_id, device_prefix + device_letters.shift)
      end

      host_entries << [ip, name, aliases ? aliases.join(' ') : nil, instance_id]
    end # line

    if host_entries.size > 0
      max_aliases_width = host_entries.map {|a| a[2] ? a[2].length : 0}.max
      File.open(prefix + '_hosts', 'w') do |hosts_file|
        host_entries.each do |host_entry|
          hosts_file.puts '%*s %*s %*s # %s' % [-15, host_entry[0], -18, host_entry[1], -1 * max_aliases_width, host_entry[2], host_entry[3]]
        end
      end # hosts_file
    end
  end # file
end

@log.close if @log
