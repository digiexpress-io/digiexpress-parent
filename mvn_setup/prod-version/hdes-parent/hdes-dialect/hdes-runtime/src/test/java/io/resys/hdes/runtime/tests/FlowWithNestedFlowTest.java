//package io.resys.hdes.runtime.tests;
//
//import java.io.Serializable;
//import java.util.HashMap;
//
///*-
// * #%L
// * hdes-runtime
// * %%
// * Copyright (C) 2020 Copyright 2020 ReSys OÜ
// * %%
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *      http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * #L%
// */
//
//import java.util.List;
//import java.util.Map;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import io.resys.hdes.compiler.api.HdesCompiler;
//import io.resys.hdes.compiler.api.HdesCompiler.Resource;
//import io.resys.hdes.compiler.spi.spec.JavaHdesCompiler;
//import io.resys.hdes.executor.api.FlowMetaValue;
//import io.resys.hdes.executor.api.HdesExecutable;
//import io.resys.hdes.executor.api.HdesExecutable.Flow;
//import io.resys.hdes.executor.api.HdesExecutable.HdesExecution;
//import io.resys.hdes.executor.api.HdesExecutable.HdesInputValue;
//import io.resys.hdes.executor.api.HdesExecutable.HdesOutputValue;
//import io.resys.hdes.runtime.api.HdesRuntime.RuntimeEnvir;
//import io.resys.hdes.runtime.api.HdesRuntime.RuntimeTask;
//import io.resys.hdes.runtime.spi.ImmutableHdesRuntime;
//
//public class FlowWithNestedFlowTest {
//  private static final HdesCompiler compiler = JavaHdesCompiler.config().build();
//  private static final ObjectMapper objectMapper = new ObjectMapper();
//  
//  @Test
//  public void simpleFlow() {
//    String src = "flow CascoPricingFlow\n" +
//        "headers: {\n" + 
//        "  factor1 INTEGER required IN,\n" + 
//        "  factor2 INTEGER required IN,\n" +
//        "  total INTEGER optional OUT\n" +
//        "}\n" + 
//        "tasks: {\n" + 
//        "  CalculateFactors: {\n" + 
//        "    then: end-as: { total: CalculateFactors.result }\n" +
//        "    flow: CalculateFactorFlow uses: { factor1: factor1, factor2: factor2 }\n"
//        + "}\n" + 
//        "}";
//    
//    Map<String, Serializable> data = new HashMap<>();
//    data.put("factor1", 11);
//    data.put("factor2", 20);
//
//    HdesExecution<? extends HdesInputValue, FlowMetaValue, ? extends HdesOutputValue> output = runFlow("CascoPricingFlow", src, data);
//    Assertions.assertEquals(output.getOutputValue().toString(), "OutputValue{total=31}");
//  }
//  
//  
//  @SuppressWarnings({ "rawtypes", "unchecked" })
//  private static HdesExecution<? extends HdesInputValue, FlowMetaValue, ? extends HdesOutputValue> runFlow(String name, String src, Map<String, Serializable> data) {
//    
//    String calculateFactorFlow = "flow CalculateFactorFlow\n" + 
//            "headers: {\n" + 
//            "  factor1 INTEGER required IN,\n" + 
//            "  factor2 INTEGER required IN,\n" +
//            "  result INTEGER required OUT\n" +
//            "}\n" + 
//            "tasks: {\n" + 
//            "  SumFactors: {\n" + 
//            "    then: end-as: { result: factor1 + factor2 }\n" +
//            " }" +
//            "}";
//    try {
//      List<Resource> resources = compiler.parser()
//          .add(name, src)
//          .add("CalculateFactorFlow", calculateFactorFlow)
//          .build();
//      
//      RuntimeEnvir runtime = ImmutableHdesRuntime.builder().from(resources).build();
//      RuntimeTask task = runtime.get(name);
//      HdesExecutable.HdesInputValue input = objectMapper.convertValue(data, task.getAccepts());
//      Flow fl = (Flow) task.getValue();
//      HdesExecution<? extends HdesInputValue, FlowMetaValue, ? extends HdesOutputValue> output = fl.apply(input);
//      
//      return output;
//    } catch(ClassNotFoundException e) {
//      throw new RuntimeException(e.getMessage(), e);
//    }
//  }
//}