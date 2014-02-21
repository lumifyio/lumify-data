class repo::cloudera::cdh3 {
    yumrepo { 'cloudera-cdh3-$basearch':
        descr    => "Cloudera's Distribution for Hadoop, Version 3",
        baseurl  => 'http://archive.cloudera.com/redhat/6/$basearch/cdh/3u6',
        gpgcheck => 1,
        gpgkey   => 'http://archive.cloudera.com/redhat/6/$basearch/cdh/RPM-GPG-KEY-cloudera',
        enabled  => 1,
    }
}
