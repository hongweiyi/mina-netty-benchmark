jmeter-mina-netty-benchmark
===
Benchmark test between [Mina](https://github.com/nadarei/mina) and [Netty](https://github.com/netty/netty).

Mina / Netty version
---
The lastest version of mina3, netty3 and netty4. your can choose the version you prefer.

Usage
---
- `cd project_path`
- `mvn clean package -Dmaven.test.skip=true`
- `mv target/jmeter-mina-netty-benchmark-1.2.jar $JMETER_HOME/lib/ext`
- `mv your_netty_or_mina_lib $JMETER_HOME/lib/ext`
- enjoy the jmeter journey 

