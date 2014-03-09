功能
===



QUSTION
===
* 如何让cpu尽可能的跑满？

* netty4内存溢出问题
	> Mar 09, 2014 5:00:05 PM io.netty.util.ResourceLeakDetector reportLeak
SEVERE: LEAK: ByteBuf.release() was not called before it's garbage-collected.
Recent access records: 1
\#1:
	io.netty.buffer.AdvancedLeakAwareByteBuf.writeBytes(AdvancedLeakAwareByteBuf.java:589)
	io.netty.channel.socket.nio.NioSocketChannel.doReadBytes(NioSocketChannel.java:208)
	io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:108)
	io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:494)
	io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:461)
	io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:378)
	io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:350)
	io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
	java.lang.Thread.run(Thread.java:744)
Created at:
	io.netty.buffer.UnpooledByteBufAllocator.newDirectBuffer(UnpooledByteBufAllocator.java:55)
	io.netty.buffer.AbstractByteBufAllocator.directBuffer(AbstractByteBufAllocator.java:155)
	io.netty.buffer.AbstractByteBufAllocator.directBuffer(AbstractByteBufAllocator.java:146)
	io.netty.buffer.AbstractByteBufAllocator.ioBuffer(AbstractByteBufAllocator.java:107)
	io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:106)
	io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:494)
	io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:461)
	io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:378)
	io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:350)
	io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
	java.lang.Thread.run(Thread.java:744)
	
* 为毛netty3的性能比netty4的好？？


