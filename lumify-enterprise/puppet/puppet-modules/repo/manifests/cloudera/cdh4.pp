class repo::cloudera::cdh4 {
    yumrepo { 'cloudera-cdh4':
        descr    => "Cloudera's Distribution for Hadoop, Version 4",
        baseurl  => 'http://archive.cloudera.com/cdh4/redhat/6/x86_64/cdh/4.4.0',
        gpgcheck => 1,
        gpgkey   => 'http://archive.cloudera.com/cdh4/redhat/6/x86_64/cdh/RPM-GPG-KEY-cloudera',
        enabled  => 1,
    }
}