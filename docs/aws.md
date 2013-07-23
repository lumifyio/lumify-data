# AWS Cluster Configuration

## Namenode Hardware Considerations
- dual quad-core 2.6 Ghz CPU, 24 GB of DDR3 RAM, dual 1 Gb Ethernet NICs, a SAS drive controller, and at
least two SATA II drives in a JBOD configuration in addition to the host OS device
- As a base rule of thumb, the namenode consumes roughly 1 GB for every 1 million blocks.
- Namenode disk requirements are modest in terms of storage. Since all metadata must fit in memory, by definition, it
can’t take roughly more than that on disk. Either way, the amount of disk this really requires is minimal—less than 1 TB.

## Secondary Namenode Hardware Considerations
- The secondary namenode is almost always identical to the namenode.

## Jobtracker Hardware Considerations
- Similar to the namenode and secondary namenode, the jobtracker is also memory-hungry

## Worker Hardware Considerations
- A ballpark estimate is that 20-30% of the machine’s raw disk capacity needs to be reserved for temporary data.
- 2 × 6 core 2.9 Ghz/15 MB cache
- 64 GB DDR3-1600 ECC
- 12 × 3 TB LFF SATA II 7200 RPM

## Software Considerations
- must use 64-bit JVM due to need for large heap sizes

## Servers
1. namenode, job tracker
2. secondary namenode, Accumulo
3. task tracker, data node, zoo keeper
4. task tracker, data node, zoo keeper
5. task tracker, data node, zoo keeper