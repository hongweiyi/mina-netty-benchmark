jmeter-mina-netty-benchmark
===
Benchmark test between [Mina](https://github.com/nadarei/mina) and [Netty](https://github.com/netty/netty).

Usage
---
- preparation
 
		$ cd project_path
		$ mvn clean package -Dmaven.test.skip=true
		$ mv target/jmeter-mina-netty-benchmark-1.2.jar $JMETER_HOME/lib/ext
		$ mv your_netty_or_mina_lib $JMETER_HOME/lib/ext

- configuration
	* add a ThreadGroup, add a JavaRequest
	* select the Server type:
		* `Mina3ServerTcpBenchmarkTest`
		* `Netty3ServerTcpBenchmarkTest`
		* `Netty4ServerTcpBenchmarkTest`
	* change `Send Parameters` option `client_type`, you can type three option:
		* `mina3`
		* `netty3`
		* `netty4`
	* change `Thread Group's loop count` equals `Send Parameters` option `nubmer_of_msgs`
	* run it
	
- Send Parameters
	<table>
		<tr><th>Key</th><th>Default Value</th><th>Other</th></tr>
		<tr><td>numbers_of_msgs</td><td>100000</td><td></td></tr>
		<tr><td>size_of_single_msg</td><td>1024*1024</td><td>Unit: byte</td></tr>
		<tr><td>client_type</td><td>netty4</td><td>netty3, mina3</td></tr>
		<tr><td>netty4_alloc</td><td>unpooled</td><td>pooled</td></tr>
	</table> 

Mina / Netty version
---
The relatively new version of mina3(3.0.0-M2), netty3(3.8.0.Final) and netty4(4.0.17.Final). you can choose the version you prefer.

Version
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

Blog
===
[hongweiyi.com](http://hongweiyi.com)

