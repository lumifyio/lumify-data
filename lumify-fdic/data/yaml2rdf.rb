#!/usr/bin/env ruby

require 'yaml'
#require 'datetime'
require 'ostruct'
require 'erb'

yaml_filename = ARGV[0]
pdf_filename = File.basename(yaml_filename, '.yaml')
rdf_filename = File.join(File.dirname(yaml_filename), pdf_filename) + '.rdf.xml'

def log(message)
  STDERR.puts message
end

def to_date(string)
  DateTime.parse(string).strftime('%Y-%m-%d')
rescue
  log "exception parsing date: #{string}"
  string
end

yaml = YAML.load(File.read(yaml_filename))
yaml[:pdf_filename] = pdf_filename
yaml[:resolution_plan][:id] = 'RP_' + File.basename(yaml_filename, '.pdf.yaml')

template_filename = File.join(File.dirname(__FILE__), File.basename(__FILE__, '.rb')) + '.erb'
template = File.read(template_filename)
renderer = ERB.new(template, 0, '-')
os_binding = OpenStruct.new(yaml).instance_eval { binding }
File.open(rdf_filename, 'w') do |file|
  file.write renderer.result(os_binding)
end
