#!/usr/bin/env ruby

require 'yaml'
require 'ostruct'
require 'erb'

yaml_filename = ARGV[0]
pdf_filename = File.basename(yaml_filename, '.yaml')
rdf_filename = File.join(File.dirname(yaml_filename), pdf_filename) + '.rdf.xml'

def log(message)
  STDERR.puts message
end

def to_date(string)
  if string.kind_of? Date
    return string
  end

  DateTime.parse(string).strftime('%Y-%m-%d')
rescue => e
  log "exception parsing date: #{string} [#{e}]"
  string
end

def to_dollars(string)
  if string.kind_of? Fixnum
    return string
  end

  dollars = string.gsub(/[^0-9TBM]/i, '')
  case dollars
  when /T/i
    dollars.gsub(/[^0-9]/, '').to_i * 10**12
  when /B/i
    dollars.sub(/[^0-9]/, '').to_i * 10**9
  when /M/i
    dollars.sub(/[^0-9]/, '').to_i * 10**6
  else
    raise 'unexpected multiplier'
  end
rescue => e
  log "exception parsing dollars: #{string} [#{e}]"
  string
end

def encode(string)
  string.encode(:xml => :text)
end

def to_id(name)
  name.gsub('.', '').gsub(/[^a-z0-9]+/i, '_').strip
end

def rp_id(yaml_filename)
  'RP_' + File.basename(yaml_filename, '.pdf.yaml')
end

def bs_id(yaml_filename, financial_institution)
  'BS_' + File.basename(yaml_filename, '.pdf.yaml') + '_' + to_id(financial_institution)
end

def fi_id(name)
  'FI_' + to_id(name)
end

def me_id(name)
  'ME_' + to_id(name)
end

def fmu_id(name)
  'FMU_' + to_id(name)
end

def image_id(filename)
  'IMAGE_' + filename
end

def person_id(name)
  'PERSON_' + to_id(name)
end

def poa_id(name, financial_institution)
  'POA_' + to_id(name) + '_' + to_id(financial_institution)
end

def parse_fi_name(name)
  match_data = name.match(/^\s*(.+?)\s*(?:\((.*)\))?\s*$/)
  fi_name = match_data[1].strip
  fi_alias = match_data[2] ? match_data[2].gsub(/^[^[0-9A-Za-z]]/, '').gsub(/[^[0-9A-Za-z]]$/, '') : nil
  return fi_name, fi_alias
rescue => e
  log "exception parsing financial institution name: #{name} [#{e}]"
  name
end

yaml = YAML.load(File.read(yaml_filename))
yaml[:pdf_filename] = pdf_filename
yaml[:resolution_plan][:id] = rp_id(yaml_filename)

material_entity_hash = Hash.new
principal_officers_hash = Hash.new
image_hash = Hash.new
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

  if properties[:balance_sheet]
    properties[:balance_sheet][:id] = bs_id(yaml_filename, name)
  end

  if properties[:principal_officers]
    properties[:principal_officers].each do |yaml_po|
      po_name = yaml_po.keys.first
      po_properties = yaml_po[po_name]
      principal_officers_hash[po_name] ||= Hash.new
      principal_officers_hash[po_name][:orgs] ||= Hash.new
      po_properties[:id] = poa_id(po_name, name)
      principal_officers_hash[po_name][:orgs][name] = po_properties
      if po_properties[:positions].kind_of? String
        po_properties[:positions] = [po_properties[:positions]]
      end
      if po_properties[:image]
        principal_officers_hash[po_name][:image] = po_properties[:image]
        image_hash[image_id(po_properties[:image])] = {:title => "Picture of #{po_name}", :filename => File.join('people', po_properties[:image])}
      end
      properties[:principal_officer_names] ||= Array.new
      properties[:principal_officer_names] << po_name
    end
  end

  if properties[:image]
    image_hash[image_id(properties[:image])] = {:title => "#{name} Logo", :filename => properties[:image]}
  end

  material_entity_hash[name] = properties
end
yaml[:resolution_plan][:material_entities] = material_entity_hash
yaml[:resolution_plan][:principal_officers] = principal_officers_hash

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

  if properties[:image]
    image_hash[image_id(properties[:image])] = {:title => "#{name} Logo", :filename => properties[:image]}
  end
end
yaml[:resolution_plan][:financial_market_utilities] = financial_market_utility_hash
yaml[:images] = image_hash

template_filename = File.join(File.dirname(__FILE__), File.basename(__FILE__, '.rb')) + '.erb'
template = File.read(template_filename)
renderer = ERB.new(template, 0, '-')
os_binding = OpenStruct.new(yaml).instance_eval { binding }
File.open(rdf_filename, 'w') do |file|
  file.write renderer.result(os_binding)
end
