# -*- mode: ruby -*-
# vi: set ft=ruby :

HOSTNAME="lumify-vm.nearinfinity.com"

def configure_puppet(puppet, manifest_file)
  puppet.manifests_path = 'puppet/manifests'
  puppet.module_path    = [ 'puppet/modules', 'puppet/puppet-modules' ]
  puppet.manifest_file  = manifest_file
  puppet.facter         = { 'fqdn' => HOSTNAME }
  puppet.options        = '--hiera_config /vagrant/puppet/hiera-vm.yaml'
end

Vagrant.configure('2') do |config|
  config.vm.box = 'centos6.4'
  config.vm.box_url = 'http://developer.nrel.gov/downloads/vagrant-boxes/CentOS-6.4-x86_64-v20130427.box'

  config.vm.hostname = HOSTNAME

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  config.vm.network :forwarded_port, :guest => 8080, :host => 8080
  config.vm.network :forwarded_port, :guest => 8443, :host => 8443

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  config.vm.network :private_network, :ip => '192.168.33.10'

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

  # used for development including closed source enterprise features
  config.vm.define "dev", :primary => true do |dev|
    dev.vm.provision :shell, :inline => "mkdir -p /data0 /opt/lumify /opt/lumify/logs"
    dev.vm.provision :puppet do |puppet|
      configure_puppet(puppet, 'dev_vm.pp')
    end
  end

  # used to create the downloadable open source demo VM
  config.vm.define "demo-opensource" do |demo|
    demo.vm.provision :shell, :inline => "mkdir -p /data0 /opt/lumify /opt/lumify/logs"
    demo.vm.provision :puppet do |puppet|
      configure_puppet(puppet, 'demo_opensource_vm.pp')
    end
    demo.vm.provision :shell, :path => "demo-vm/configure-vm.sh", :args => "sample-data-html.tgz" 
  end

  # used to create the downloadable enterprise demo VM
  config.vm.define "demo-enterprise" do |demo|
    demo.vm.provision :shell, :inline => "mkdir -p /data0 /opt/lumify /opt/lumify/logs"
    demo.vm.provision :puppet do |puppet|
      configure_puppet(puppet, 'demo_enterprise_vm.pp')
    end
    demo.vm.provision :shell, :path => "demo-vm/configure-vm.sh", :args => "chechen-terrorists.tgz"
  end
end
