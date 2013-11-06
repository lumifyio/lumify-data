#!/usr/bin/expect

set zookeeper [lindex $argv 0]
set topic [lindex $argv 1]

spawn /opt/kafka/bin/kafka-console-producer.sh --zookeeper $zookeeper --topic $topic

expect "Creating async producer for broker id" { send "\n"; sleep 1 }
expect "for producing" { sleep 1; send \003 }

send_user "topic \"$topic\" created\n"
