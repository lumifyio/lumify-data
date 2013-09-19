#!/bin/bash -eu

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

other_host=$1

ssh ${SSH_OPTS} ${other_host} 'puppet agent -t || true'
ssh ${SSH_OPTS} ${other_host} service puppet start
