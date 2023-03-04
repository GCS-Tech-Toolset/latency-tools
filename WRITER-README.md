# Latency-Writer
A utility for writing latency files.
There are 2 kinds of writers:

1. SimpleLatencyRecorder
2. StandardLatencyRecorder

You can have an unlimited number of recorders in your system, however, you must take care that they are not writing to the same output file, or using the same core's (as other writers, or as your programs as well).

## Instantiation
There are 3 steps to generating a latency-recorder:

1. Create Properties form Factory
2. Modify props as per your needs
3. Generate latency-recorder from your properties.

These are discussed in further detail later. For now, here is what that code looks like:

```java
    LatencyRecorderProperties props = LatencyRecorderFactory.createInstanceProperties(val, 10_000);
    props.setOutfileName("test");
    ILatencyRecorder latRec = LatencyRecorderFactory.createLatencyWriter(props);
    .
    .
    .
    latRec.recordLatency(val)
```

In the above example:
* the 10_000 indicates the expected MPS (this autoconfigures for you)
* Generates a StandardLatencyRecorder
* writer to "./test.bin"


## SimpleLatencyRecorder
A bear bones recorder that keeps all values on the heap.
Allows you to specify output-dir, file, etc.


## StandardLatencyRecorder
A more complete solution, you can log indefinite amounts of latency values (tested at 500B values)


## Configuration, System
The following are the available entries:

```java
LatencyWriter.XXX.ThreadControl.ProcessingCoreId
LatencyWriter.XXX.ThreadControl.WriterCoreId
LatencyWriter.XXX.Procesor.Capacity
LatencyWriter.XXX.Type.SimpleRecorder
LatencyWriter.XXX.Writer.BufferCount
LatencyWriter.XXX.Writer.BufferRatio
LatencyWriter.XXX.Writer.OutfileName
LatencyWriter.XXX.Writer.OutfilePath
```

All of these values can be passed in via the `-D` options from the java command invocation, or they can be set programmatically.


## Naming
The `XXX` in the above values refers to the *name of the recorder* you are trying to configure.
So, for example, your system could have a latency-recorder to measure each side of serialization; how long it takes to serialize, and how long to deserialize. This means you would have 2 recorders, probably named: "serialize" & "deserialize".
<br/>
With this setup, a possible configuration set you would see is:

```java
    LatencyRecorderProperties serializerLatenciesProps = LatencyRecorderFactory.createInstanceProperties("serializer", 10_000);
    serializerLatenciesProps.setFpath("/tmp/latencies");   
    serializerLatenciesProps.setProcessorCoreId(1);
    serializerLatenciesProps.setWriterCoreId(3)

    LatencyRecorderProperties deserializerLatenciesProps = LatencyRecorderFactory.createInstanceProperties("deserializer", 10_000);
    deserializerLatenciesProps.setOutfilePath("/tmp/latencies");
    deserializerLatenciesProps.setProcessorCoreId(2);
    deserializerLatenciesProps.setWriterCoreId(3)
    
    ILatencyRecorder serializerLatencoes = LatencyRecorderFactory.createLatencyWriter(serializerLatenciesProps);
    ILatencyRecorder deserializerLatencies = LatencyRecorderFactory.createLatencyWriter(deserializerLatenciesProps);
```
the above snippet would configure two latency recorders:
* both expect an MPS of around 10K
* both would write tp /tmp/latencies
* one output file would be called /tmp/latencies/serializer.bin
* one output file would be called /tmp/latencies/deserializer.bin 
* They would be processing on core's 1 and 2 respectively
* both are using core 3 to write data from memory to disk (this is OK since the expected MPS is so low)



## Defaults
In general, the reporter defaults to the following settings:
1. expectation of MPS 


### Simple

### Full

Automatically sets the following values for MPS ranges:
```java
MPS<=100K 
  WaitStrategy=BlockingWaitStrategy
  Capacity=512
  BufferCount=512
MPS>500K && MPS <= 1M
  WaitStrategy=YieldingWaitStrategy
  Capacity=1024
  BufferCount=1024
MPS>1M
  WaitStrategy=BusySpinWaitStrategy
  Capacity=65_536
  BufferCount=1024
```