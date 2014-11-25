Facter.add('data_directories') do
  setcode do
    Dir['/data[0-9]'].join(',')
  end
end
