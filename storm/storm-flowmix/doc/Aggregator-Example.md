
## Example

#### AggregatorExample

1.Count

```
INPUT: source: flowLoaderStream:30, stream: flowLoaderStream, id: {}, [[Flow{id='flow1', name='null', description='null', streams={stream1=StreamDef{name='stream1', flowOps=[org.calrissian.flowmix.core.model.op.SelectOp@5844e298, PartitionOp{fields=[key3]}, org.calrissian.flowmix.core.model.op.AggregateOp@568307e0]}}}]]

INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='ae9874de-c704-4647-9b2f-01df30fd5f29', timestamp=1441855786234, tuples=[Tuple{key='key3', value=val3, metadata={}}, Tuple{key='count', value=1972, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='73c4e443-96a2-4fd0-8c53-7426a95a3602', timestamp=1441855791251, tuples=[Tuple{key='key3', value=val3, metadata={}}, Tuple{key='count', value=2613, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='34477064-dc7a-4678-ae60-1ea3a3704a88', timestamp=1441855796270, tuples=[Tuple{key='key3', value=val3, metadata={}}, Tuple{key='count', value=2507, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='d03860ff-fe80-4181-bc0b-839d0641803f', timestamp=1441855801284, tuples=[Tuple{key='key3', value=val3, metadata={}}, Tuple{key='count', value=2578, metadata={}}]}, 2, stream1, stream1]
```

截取上面的`Input:source`, 下面的字符串表示的是`FlowInfo: flowId,Event,index,streamName,previousStreamName`

```
[
  flow1,
  BaseEvent{type='', id='d03860ff-fe80-4181-bc0b-839d0641803f', timestamp=1441855801284,
    tuples=[
      Tuple{key='key3', value=val3, metadata={}},
      Tuple{key='count', value=2578, metadata={}}
    ]
  },
  2,
  stream1,
  stream1
]
```

`Event`继承`BaseEvent`(上面第二个元素), 指定uuid和timestamp即可. event可以放入多个Tuple,组成tuples.  
这里因为source是aggregate, 所以tuples中包含了count聚合结果: `Tuple{key='count', value=2578, metadata={}}` 
而Tuple{key='key3', value=val3, metadata={}},可以看做是group by要保留的结果.  
比如select key3,count(*) from tbl group by key3的结果为:  

```
key3 | count
-----|-----
val3 | 2578
```

其中: groupByField=key3, operatedField=key3. groupedValues=val3, aggregateResult=2578.

2.替换为DistCountAggregator后: 因为每次发射的key3,val3都是一样的,所以distinct count总是1.

```
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='675c9bc2-8e65-4821-a015-673888d4b002', timestamp=1441869612156, tuples=[Tuple{key='distCount', value=1, metadata={}}, Tuple{key='key3', value=val3, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='0396d0b1-2f6b-45f8-bcb1-0356902b820d', timestamp=1441869617175, tuples=[Tuple{key='distCount', value=1, metadata={}}, Tuple{key='key3', value=val3, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='73d5cf30-78e8-4a85-a0af-11c1cf757819', timestamp=1441869622189, tuples=[Tuple{key='distCount', value=1, metadata={}}, Tuple{key='key3', value=val3, metadata={}}]}, 2, stream1, stream1]
```

3.替换为ExampleRunner2随机产生事件并使用Count. 为什么每次聚合时,没有包含所有的value.而是分开的?? --> 实际上多个value作为多个聚合计算.所以下面两条记录是在一次计算出来的(可以看到它们的timestamp是一样的).

```
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='c5e3496c-e191-4dfc-a3c7-bd356aaa6d28', timestamp=1441879356388, tuples=[Tuple{key='key3', value=val3, metadata={}}, Tuple{key='count', value=1008, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='5bb1ba08-9c30-4136-b98f-f2636bc05b81', timestamp=1441879356388, tuples=[Tuple{key='key3', value=val-3, metadata={}}, Tuple{key='count', value=1578, metadata={}}]}, 2, stream1, stream1]
```

为了验证key3的两个value是在一次聚合中计算的, 在AggregatorBolt中打印event: "events:" + flowInfo.getEvent(),
可以看到事件现在可以随机生成相同key不同value. 并且两条aggregate是紧跟着一起输出的,说明是在同一个时间窗口内计算两个value的count的.

```
events:BaseEvent{type='', id='70b44213-a3ea-4c7a-b946-c992b66a8dcc', timestamp=1441892571592, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
events:BaseEvent{type='', id='05efb831-d76c-49bf-8ac8-bd2cf801a6af', timestamp=1441892571825, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
events:BaseEvent{type='', id='ba459811-cba0-4bcb-b3d9-1f8ca17a27b3', timestamp=1441892572072, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='deb316ed-33d0-45ee-8421-35e9d8f177fe', timestamp=1441892572079, tuples=[Tuple{key='key3', value=val-3, metadata={}}, Tuple{key='count', value=16, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='d3287989-2e3f-4006-8d74-cf7554f5f117', timestamp=1441892572080, tuples=[Tuple{key='key3', value=val3, metadata={}}, Tuple{key='count', value=12, metadata={}}]}, 2, stream1, stream1]
events:BaseEvent{type='', id='43343c3f-7280-4177-b25b-6db1a708ce4d', timestamp=1441892572087, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
events:BaseEvent{type='', id='5785c15f-2e30-4c2d-acc6-f9762df50e96', timestamp=1441892572315, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
....
events:BaseEvent{type='', id='1122ccca-ba6a-4ec9-a7ec-0113e5e73124', timestamp=1441892576826, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
events:BaseEvent{type='', id='161a83d6-6e6a-4b80-8ea5-7d78cfdffec3', timestamp=1441892577074, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='90239faf-bdd1-4d13-890a-f4f39fd5fdf1', timestamp=1441892577080, tuples=[Tuple{key='key3', value=val-3, metadata={}}, Tuple{key='count', value=19, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='5cb96eeb-1c24-4b5d-9f23-e380433248bf', timestamp=1441892577082, tuples=[Tuple{key='key3', value=val3, metadata={}}, Tuple{key='count', value=11, metadata={}}]}, 2, stream1, stream1]
```

4.替换为GroupCount: 并在GroupCountAggregator.added方法中添加打印maps: -->为了更方便地看每次add后的结果

```
events:BaseEvent{type='', id='645bc883-28c8-47fe-80f6-ca5db2042be3', timestamp=1441886287869, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
{val3=1}
events:BaseEvent{type='', id='1afddd95-2117-4792-a632-0ef4c6cbf4bd', timestamp=1441886287969, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
{val3=2}
events:BaseEvent{type='', id='aba447e8-b34d-4b86-895e-b299858015b6', timestamp=1441886287969, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
{val3=3}
events:BaseEvent{type='', id='3208e7af-07aa-4f14-bf4e-33e5a57abe32', timestamp=1441886286945, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
{val-3=1}
events:BaseEvent{type='', id='50baaa27-4ab1-4e94-9c3b-1fd531c3a042', timestamp=1441886287815, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
{val-3=2}
events:BaseEvent{type='', id='0d5e1573-2b55-444c-969c-ec89a73e8f73', timestamp=1441886287868, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
{val-3=3}
events:BaseEvent{type='', id='263b66da-029a-4df8-845d-633b597ccde5', timestamp=1441886288418, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
{val3=4}
events:BaseEvent{type='', id='d315d081-79b5-4cb7-99d9-55115f2e64c6', timestamp=1441886288815, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
{val-3=4}
```

可以看到val-3时, maps并没有把val3的都打印出来.但是这个maps在下一个val3时,是接着前面的计数,即val3=4.
那么我们是不是可以认为对于不同的key3的值,都用不同的maps来保存. 所以在val-3时看不到val3.在val3时看不到val-3.
不过没关系的,在聚合时, 对于不同的值都会聚合一遍: 所以我们之前看到的也是对的,我们以为5秒钟一次只统计一个,实际上统计了两个.

```
Begin Aggregate....
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='6bf42e0d-2855-4c77-9db4-c6c87532a844', timestamp=1441892040371, tuples=[Tuple{key='groupCount', value={val3=6}, metadata={}}, Tuple{key='key3', value=val3, metadata={}}]}, 2, stream1, stream1]
Begin Aggregate....
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='7e7cd87d-8b8e-426f-b5ec-fe932255d872', timestamp=1441892040378, tuples=[Tuple{key='groupCount', value={val-3=11}, metadata={}}, Tuple{key='key3', value=val-3, metadata={}}]}, 2, stream1, stream1]
```

接着count从1开始.

```
events:BaseEvent{type='', id='585ea2ee-8541-448e-82cf-9e3a4585b5f4', timestamp=1441892040368, tuples=[Tuple{key='key3', value=val-3, metadata={}}]}
{val-3=1}
events:BaseEvent{type='', id='96f64942-676f-43a6-828f-0d685f8a30c2', timestamp=1441892040400, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
{val3=1}
events:BaseEvent{type='', id='640cc01a-402f-4ac4-b537-3a9cef66ce18', timestamp=1441892040714, tuples=[Tuple{key='key3', value=val3, metadata={}}]}
{val3=2}
```

实际上Count和GroupCount的结果是一样的(所以GroupCount是不需要的!). 对比下两个输出:

```
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='90239faf-bdd1-4d13-890a-f4f39fd5fdf1', timestamp=1441892577080, tuples=[Tuple{key='key3', value=val-3, metadata={}}, Tuple{key='count', value=19, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='5cb96eeb-1c24-4b5d-9f23-e380433248bf', timestamp=1441892577082, tuples=[Tuple{key='key3', value=val3, metadata={}}, Tuple{key='count', value=11, metadata={}}]}, 2, stream1, stream1]

INPUT: source: aggregate:7, stream: output, id: {}, [flow1, BaseEvent{type='', id='6bf42e0d-2855-4c77-9db4-c6c87532a844', timestamp=1441892040371, tuples=[Tuple{key='groupCount', value={val3=6}, metadata={}}, Tuple{key='key3', value=val3, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='7e7cd87d-8b8e-426f-b5ec-fe932255d872', timestamp=1441892040378, tuples=[Tuple{key='groupCount', value={val-3=11}, metadata={}}, Tuple{key='key3', value=val-3, metadata={}}]}, 2, stream1, stream1]
可以看到count直接输出value=19,而GroupCount的value={val3=19},而val3是可以由另外一个tuple确定的. 所以Count实际上就有GroupCount的意义在内.
```


### IP关联账户(AssocCountExample)

```
INPUT: source: aggregate:9, stream: output, id: {}, [flow1, BaseEvent{type='', id='385f5d1e-0305-4757-a0c3-6ac50f7ab31c', timestamp=1441871416038,
tuples=[
  Tuple{key='key1', value=val1, metadata={}},
  Tuple{key='key3', value=val3, metadata={}},
  Tuple{key='assocCount', value={val1=1}, metadata={}}]}, 2, stream1, stream1]
```

最后一个Tuple的key是outputField=assocCount. 前面两个是groupedValues:因为partition字段为key1,key3

相同key添加不同value: 在一条Event中!

```
INPUT: source: aggregate:9, stream: output, id: {}, [flow1, BaseEvent{type='', id='9291c396-6051-4a48-91d7-b8d6c6ce296c', timestamp=1441871638999, tuples=[
  Tuple{key='key1', value=val1, metadata={}},
  Tuple{key='key1', value=val-1, metadata={}},
  Tuple{key='key3', value=val3, metadata={}},
  Tuple{key='key3', value=val-3, metadata={}},
  Tuple{key='assocCount', value={val1=1}, metadata={}}]}, 2, stream1, stream1]
```

我们要的效果应该是在不同的Event中.相同key有不同value.

```
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='77073837-df65-4fee-8174-f4315beb4441', timestamp=1441872467326, tuples=[
  Tuple{key='key1', value=val1, metadata={}},
  Tuple{key='key3', value=val-3, metadata={}},
  Tuple{key='assocCount', value={val1=1}, metadata={}}]}, 2, stream1, stream1]
```

问题出在assocCount的value没有反应所有的value.
在AssocCountAggregator中添加打印left,right,一直都是val1--val-3

问题出在: ExampleRunner的getMockEvents是第一次就决定了.后面都是用第一次的mockEvents.不会说每一次都重新计算.

在AggregatorBolt中打印event: "events:" + flowInfo.getEvent(), 可以看到事件现在可以随机生成相同key不同value.

```
events:BaseEvent{type='', id='408a2429-e79a-4ea6-990d-e2bcdef79fa6', timestamp=1441875542164, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val-3, metadata={}}]}
events:BaseEvent{type='', id='3a77926b-30e7-4195-8878-efddfc95ddfe', timestamp=1441875542166, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val3, metadata={}}]}
InputEvent       AssocResult
key1, key3       key1, assocCount
val1  val3       val1, 1
val1  val_3  =>  val1, 2
val1  val3       val1, 2
```

两个分区: .partition().fields("key1","key3").end()的输出日志:

```
INPUT: source: aggregate:9, stream: output, id: {}, [flow1, BaseEvent{type='', id='5412b237-f540-4794-99ff-41c153bc3ea2', timestamp=1441893159764, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val3, metadata={}}, Tuple{key='assocCount', value={val1=1}, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='cc959f91-ba95-416b-a1a0-aeafa8af11e1', timestamp=1441893159765, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val-3, metadata={}}, Tuple{key='assocCount', value={val1=1}, metadata={}}]}, 2, stream1, stream1]

INPUT: source: aggregate:9, stream: output, id: {}, [flow1, BaseEvent{type='', id='556673a1-fa94-4f47-ae51-a5ec4e58a7a8', timestamp=1441893169767, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val3, metadata={}}, Tuple{key='assocCount', value={val1=1}, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='30219f0f-866f-4e6a-93bc-625972beca7d', timestamp=1441893169767, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val-3, metadata={}}, Tuple{key='assocCount', value={val1=1}, metadata={}}]}, 2, stream1, stream1]
```

把AssocCountAggregator的aggregateResult换成maps:

```
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='33c58c4d-d778-4ef7-919a-b8d5437eb9ee', timestamp=1441894334996, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val-3, metadata={}}, Tuple{key='assocCount', value={val1={val-3=35}}, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:9, stream: output, id: {}, [flow1, BaseEvent{type='', id='a1d2ee4a-a5db-4bc8-a402-2bea9a251ee8', timestamp=1441894334996, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val3, metadata={}}, Tuple{key='assocCount', value={val1={val3=24}}, metadata={}}]}, 2, stream1, stream1]

INPUT: source: aggregate:9, stream: output, id: {}, [flow1, BaseEvent{type='', id='fb064650-f09f-4b04-b128-c99a4b04ac11', timestamp=1441894344997, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val3, metadata={}}, Tuple{key='assocCount', value={val1={val3=53}}, metadata={}}]}, 2, stream1, stream1]
INPUT: source: aggregate:6, stream: output, id: {}, [flow1, BaseEvent{type='', id='e6792ed4-ce70-4c13-b92f-353ecb8c6fca', timestamp=1441894344999, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='key3', value=val-3, metadata={}}, Tuple{key='assocCount', value={val1={val-3=66}}, metadata={}}]}, 2, stream1, stream1]
```

问题: 为什么在一次聚合中, 会打印多次Begin Aggregate....@AggregatorBolt.emitAggregate. 有几个value就打印几次.
在AggregatorBolt中有两个顶地啊用了emitAggrgate,而tick中:

```
for(AggregatorWindow window : windowCache.asMap().values()) {
    if(window.getTriggerTicks() == op.getTriggerThreshold())
        emitAggregate(flow, op, curStream.getName(), idx, window);
}
```

可以看到不同的value是在不同的AggregatorWindow中.放入windowCache中的是在非tick中:

```
windowCache.put(flowInfo.getPartition(), window);
```

其中partition实际上是value,不同的value在不同的分区中. 因为我们设置的partition是key1,key3.
所以<val1,val3>和<val1,val_3>会被作两个分区的. 如果partiton为key1,则只有一个分区. 修改下:.partition().fields("key1").end()
可以看到下面一个窗口内只有一次聚合操作. 下面是两个时间窗口,而且value包含两个value: value={val1={val3=22, val-3=34}}

```
Begin Aggregate....
INPUT: source: aggregate:9, stream: output, id: {}, [flow1, BaseEvent{type='', id='1633bbc7-f85b-4ce4-a939-aed77bc28025', timestamp=1441896652308, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='assocCount', value={val1={val3=22, val-3=34}}, metadata={}}]}, 2, stream1, stream1]

Begin Aggregate....
INPUT: source: aggregate:9, stream: output, id: {}, [flow1, BaseEvent{type='', id='7c51fb56-4ad3-4230-8f79-d53753d6ddc7', timestamp=1441896662311, tuples=[Tuple{key='key1', value=val1, metadata={}}, Tuple{key='assocCount', value={val1={val3=50, val-3=66}}, metadata={}}]}, 2, stream1, stream1]
```