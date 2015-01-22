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

def to_id(name)
  name.gsub('.', '').gsub(/[^a-z0-9]+/i, '_').strip
end

def rp_id(yaml_filename)
  'RP_' + File.basename(yaml_filename, '.pdf.yaml')
end

def fi_id(name)
  'FI_' + to_id(name)
end

def image_id(filename)
  'FILE_' + filename
end

def person_id(name)
  'PERSON_' + to_id(name)
end

def poa_id(name, financial_institution)
  'POA_' + to_id(name) + '_' + to_id(financial_institution)
end

def parse_fi_name(name)
  quotes = %{"'“”}
  match_data = name.match(/^\s*(.+?)\s*(?:\([#{quotes}]?([^#{quotes}]*?)[#{quotes}]?\))?\s*$/)
  return match_data[1], match_data[2]
rescue
  log "exception parsing financial institution name: #{name}"
  name
end

yaml = YAML.load(File.read(yaml_filename))
yaml[:pdf_filename] = pdf_filename
yaml[:resolution_plan][:id] = rp_id(yaml_filename)

material_entitiy_hash = Hash.new
yaml[:resolution_plan][:material_entities].each do |yaml_me|
  if yaml_me.kind_of? String
    name, aka = parse_fi_name(yaml_me)
    properties = Hash.new
  else
    name = yaml_me.keys.first
    properties = yaml_me[name]
    name, aka = parse_fi_name(name)
    properties[:resolution_plan_id] = rp_id(yaml_filename)
  end
  properties[:id] = fi_id(name)
  if aka && properties[:alias] == nil
    properties[:alias] = aka
  end
  material_entitiy_hash[name] = properties
end
yaml[:resolution_plan][:material_entities] = material_entitiy_hash

financial_market_utility_hash = Hash.new
yaml[:resolution_plan][:financial_market_utilities].each do |yaml_fmu|
  if yaml_fmu.kind_of? String
    name, aka = parse_fi_name(yaml_fmu)
    properties = Hash.new
  else
    name = yaml_fmu.keys.first
    properties = yaml_fmu[name]
    name, aka = parse_fi_name(name)
  end
  properties[:id] = fi_id(name)
  if aka && properties[:alias] == nil
    properties[:alias] = aka
  end
  financial_market_utility_hash[name] = properties
end
yaml[:resolution_plan][:financial_market_utilities] = financial_market_utility_hash

template_filename = File.join(File.dirname(__FILE__), File.basename(__FILE__, '.rb')) + '.erb'
template = File.read(template_filename)
renderer = ERB.new(template, 0, '-')
os_binding = OpenStruct.new(yaml).instance_eval { binding }
File.open(rdf_filename, 'w') do |file|
  file.write renderer.result(os_binding)
end
