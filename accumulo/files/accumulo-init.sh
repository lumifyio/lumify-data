#!/usr/bin/expect

set instancename [lindex $argv 0]
set password [lindex $argv 1]

spawn /usr/lib/accumulo/bin/accumulo init

expect {
  "Instance name :" { send "$instancename\r"; exp_continue; }
  "Delete existing entry from zookeeper?" { send "Y\n"; exp_continue; }
  "Enter initial password" { send "$password\r"; exp_continue; }
  "Confirm initial password" { send "$password\r"; exp_continue; }
}
