jmeter-mina-netty-benchmark
===
Benchmark test between [Mina](https://github.com/nadarei/mina) and [Netty](https://github.com/netty/netty).

Usage
---
- ...

Mina / Netty version
---
The relatively new version of mina3(3.0.0-M2), netty3(3.8.0.Final) and netty4(4.0.17.Final). you can choose the version you prefer.

Release Note
===

1.2.1
---
* add async send mechanism
* remove unnecessary print

1.2.2
---
- fix some problems
    * optimize Label in Aggreate Report
    * extract some parameters
- reformat code

1.2.3
---
- optimize Label
    * server name | send mode | client name : id
- add Netty4 pooled option to Client
- change the way of netty3 write buffer creation

2.0.0
---
- change jmeter to [simperf](https://github.com/imbugs/simperf)

Blog
===
[hongweiyi.com](http://hongweiyi.com)

