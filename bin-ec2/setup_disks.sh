#!/bin/bash

INSTANCE_STORE_LETTERS='bcde'
EBS_LETTERS='fghijklmnopqrstuvwxyz'
VG_NAME=ebs
LV_NAME=data


# create a single full size partition on the device
# assumes the disk is unpartitioned
function _partition {
  local device=$1
  local partition_type='83'
  if [ "$2" != '' ]; then
    partition_type="$2"
  fi

  echo "partitioning ${device}..."
  echo "p
n
p
1


t
${partition_type}
p
w" | fdisk ${device}
}

# create an Ext4 filesystem on the partition with no reserved space
function _format {
  local partition=$1
  local label=$2

  echo "formatting ${partition}..."
  mkfs.ext4 -L ${label} -m 0 ${partition}
}

# add an Ext4 partition to /etc/fstab and mount it
function _mount {
  local label=$1
  local mountpoint=$2
  local options='defaults'
  if [ "$3" != '' ]; then
    options="defaults,$3"
  fi

  echo "mounting LABEL='${label}' at ${mountpoint}..."
  mkdir ${mountpoint}
  echo "LABEL=${label} ${mountpoint} ext4 ${options} 0 0" >> /etc/fstab
  mount ${mountpoint}
}

# return the 4th octet of the IP address assigned to eth0
function _octet4 {
  echo $(ifconfig eth0 | grep 'inet addr' | awk '{print $2}' | sed -e 's/.*\.//')
}

# prepare and use any not previously configured Instance Storage devices as /data[1-4]
# assumes use for Hadoop with 'noatime' mount option
function _instance {
  local label_prefix=$(_octet4)
  local n=$(awk '/-i[1234]/ {print $2}' /etc/fstab | sort | tail -1 | sed -e 's/\/data//')
  [ "${n}" != '' ] || n=0
  local instance_store_disk

  for instance_store_disk in $(fdisk -l | grep "Disk /dev/xvd[${INSTANCE_STORE_LETTERS}]" | awk '{print $2}' | sed -e 's/://'); do
    fdisk -l | grep "${instance_store_disk}1" &>/dev/null
    if [ $? -ne 0 ]; then
      n=$((${n} + 1))

      _partition ${instance_store_disk}
      _format ${instance_store_disk}1 ${label_prefix}-i${n}
      _mount ${label_prefix}-i${n} /data${n} noatime
    fi
  done
}

# prepare and use any not previously configure EBS volumes
# adding them to a single /data logical volume backed filesystem
function _ebs {
  for ebs_disk in $(fdisk -l | grep "Disk /dev/xvd[${EBS_LETTERS}]" | awk '{print $2}' | sed -e 's/://'); do
    fdisk -l | grep "${ebs_disk}1" &>/dev/null
    if [ $? -ne 0 ]; then
      _partition ${ebs_disk} 8e

      vgs ${VG_NAME} &>/dev/null
      if [ $? -ne 0 ]; then
        echo "creating volume group '${VG_NAME}' with partition: ${ebs_disk}1..."
        vgcreate --clustered n ${VG_NAME} ${ebs_disk}1
        echo "creating logical volume '${LV_NAME}'..."
        lvcreate --extents 100%FREE --name ${LV_NAME} ${VG_NAME}
        _format /dev/mapper/${VG_NAME}-${LV_NAME} ${label_prefix}-ebs
        _mount ${label_prefix}-ebs /data
      else
        echo "adding partition to volume group '${VG_NAME}': ${ebs_disk}1..."
        vgextend ${VG_NAME} ${ebs_disk}1
        echo "extending logical volume '${LV_NAME}'..."
        lvextend --extents +100%FREE ${VG_NAME}/${LV_NAME}
        echo "resizing the /dev/mapper/${VG_NAME}-${LV_NAME} filesystem..."
        resize2fs /dev/mapper/${VG_NAME}-${LV_NAME}
      fi
    fi
  done
}


case "$1" in
  instance)
    _instance
    ;;
  ebs)
    _ebs
    ;;
  *)
    echo 'you must specify the type of disks to configure, one of:'
    awk '/[a-z]+\)/ {print $1}' $0 | sed -e 's/)//' -e 's/^/  /'
    exit -1
    ;;
esac
