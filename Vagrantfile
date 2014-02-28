# -*- mode: ruby -*-
# vi: set ft=ruby :

HOSTNAME = "lumify-vm.lumify.io"

FORWARD_PORTS = {
  [8020, 40400, 50070]         => 'hadoop namenode',
  [50090, 56456]               => 'hadoop secondarynamenode',
  [8021, 37567, 50030]         => 'hadoop jobtracker',
  [50010, 50020, 50075, 51244] => 'hadoop datanaode',
  [34081, 50060]               => 'hadoop tasktracker',
  [2181, 2888, 3888]           => 'zookeepr',
  9997                         => 'accumulo tserver',
  9999                         => 'accumulo master',
  [4560, 50095]                => 'accumulo monitor',
  [9200, 9300]                 => 'elasticsearch',
  9092                         => 'kafka',
  8081                         => 'storm ui',
  6627                         => 'storm nimbus',
  [6700, 6701, 6702, 6703]     => 'storm supervisor',
  [8080, 8443]                 => 'jetty',
}

def forward_ports(config, port_hash)
  port_hash.keys.flatten.each do |port|
    config.vm.network :forwarded_port, :guest => port, :host => port, :auto_correct => true
  end
end

def provision_proxy(config, proxy_url)
  if proxy_url
    config.vm.provision :shell, :inline => "echo 'proxy=#{proxy_url}' >> /etc/yum.conf"
    config.vm.provision :shell, :inline => 'npm config set registry http://registry.npmjs.org/', :privileged => false
    config.vm.provision :shell, :inline => "npm config set proxy #{proxy_url}", :privileged => false
  else
    config.vm.provision :shell, :inline => "sed -i -e '/^proxy=/d' /etc/yum.conf"
    config.vm.provision :shell, :inline => 'npm config set registry https://registry.npmjs.org/', :privileged => false
    config.vm.provision :shell, :inline => 'npm config delete proxy', :privileged => false
  end
end

def configure_puppet(puppet, manifest_file, hiera_file)
  puppet.manifests_path = 'puppet/manifests'
  puppet.module_path    = [ 'puppet/modules', 'puppet/puppet-modules' ]
  puppet.manifest_file  = manifest_file
  puppet.facter         = { 'fqdn' => HOSTNAME }
  puppet.options        = "--hiera_config /vagrant/puppet/#{hiera_file}"
end

Vagrant.configure('2') do |config|
  config.vm.box = 'centos6.4'
  config.vm.box_url = 'http://developer.nrel.gov/downloads/vagrant-boxes/CentOS-6.4-x86_64-v20130427.box'
  #config.vm.box = 'centos6.4-i386'
  #config.vm.box_url = 'http://developer.nrel.gov/downloads/vagrant-boxes/CentOS-6.4-i386-v20130731.box'

  config.vm.hostname = HOSTNAME

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network :forwarded_port, :guest => 8080, :host => 8080
  # config.vm.network :forwarded_port, :guest => 8443, :host => 8443

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network :private_network, :ip => '192.168.33.10'

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network :public_network

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id, '--memory', '4096']
    vb.customize ["modifyvm", :id, '--cpus', '2']
  end
  #
  # View the documentation for the provider you're using for more
  # information on available options.

  # used to compile our dependencies
  config.vm.define "rpm" do |rpm|
    rpm.vm.provision :shell, :path => "lumify-rpms/configure-vm.sh"
  end

  # used to manage the local SMMC cluster
  config.vm.define "puppet" do |puppet|
    puppet.vm.hostname = 'puppet'
    puppet.vm.network :public_network, :ip => '10.0.1.200'
    puppet.vm.provision :shell, :inline => "yum install -y git"
    puppet.vm.provision :shell, :inline => "cd /vagrant/deployment && ./push.sh - physical/smmc_hosts"
    puppet.vm.provision :shell, :inline => "cd && ./init.sh smmc_hosts local"
  end

  # used for development including closed source enterprise features
  config.vm.define "dev", :primary => true do |dev|
    forward_ports(dev, FORWARD_PORTS)
    dev.vm.provision :shell, :inline => "mkdir -p /data0 /opt/lumify /opt/lumify/logs"
    dev.vm.provision :puppet do |puppet|
      configure_puppet(puppet, 'dev_vm.pp', 'hiera-vm.yaml')
    end
  end

  # used for automated integration testing
  config.vm.define "test" do |test|
    forward_ports(test, FORWARD_PORTS)
    provision_proxy(test, ENV['PROXY_URL'])
    test.vm.provision :shell, :inline => "mkdir -p /data0"
    test.vm.provision :puppet do |puppet|
      configure_puppet(puppet, 'dev_vm.pp', 'hiera-test.yaml')
    end
    test.vm.provision :shell, :path => "bin/test/clone.sh", :args => '/tmp/lumify-all', :privileged => false
    test.vm.provision :shell, :path => "bin/test/ingest.sh", :args => '/tmp/lumify-all', :privileged => false
  end

  # used to create the downloadable open source demo VM
  config.vm.define "demo-opensource" do |demo|
    forward_ports(demo, FORWARD_PORTS)
    demo.vm.provision :shell, :inline => "mkdir -p /data0 /opt/lumify /opt/lumify/logs"
    demo.vm.provision :puppet do |puppet|
      configure_puppet(puppet, 'demo_opensource_vm.pp', 'hiera-vm.yaml')
    end
    demo.vm.provision :shell, :path => "demo-vm/configure-vm.sh", :args => "opensource sample-data-html.tgz"
  end

  # used to create an enterprise demo VM
  config.vm.define "demo-enterprise" do |demo|
    forward_ports(demo, FORWARD_PORTS)
    demo.vm.provision :shell, :inline => "mkdir -p /data0 /opt/lumify /opt/lumify/logs"
    demo.vm.provision :puppet do |puppet|
      configure_puppet(puppet, 'demo_enterprise_vm.pp', 'hiera-vm.yaml')
    end
    demo.vm.provision :shell, :path => "demo-vm/configure-vm.sh", :args => "enterprise chechen-terrorists.tgz"
  end
end
