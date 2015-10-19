/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package storm.kafka;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;

public class StringKeyValueScheme extends StringScheme implements KeyValueScheme {

    @Override
    public List<Object> deserializeKeyAndValue(byte[] key, byte[] value) {
        if ( key == null ) {
            return deserialize(value);
        }
        String keyString = StringScheme.deserializeString(key);
        String valueString = StringScheme.deserializeString(value);
        return new Values(ImmutableMap.of(keyString, valueString));
    }

//    @Override
//    public List<Object> deserializeKeyAndValue(byte[] key, byte[] value){
//        ArrayList tuple = new ArrayList();
//        tuple.add(key);
//        tuple.add(value);
//        return tuple;
//    }
//
//    @Override
//    public List<Object> deserialize(byte[] bytes) {
//        ArrayList tuple = new ArrayList();
//        tuple.add(bytes);
//        return tuple;
//    }
//
//    @Override
//    public Fields getOutputFields() {
//        return new Fields("key","value");
//    }
}