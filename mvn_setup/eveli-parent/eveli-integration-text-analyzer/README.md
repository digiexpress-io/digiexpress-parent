
# About
Wrench service or java code adapter for text analyzer service. 
Service provides 2 endpoints:
* analyzes given text and category and returns text sentiment (positive-negative) and provides optional subcategory for text.
* capability to match given set of texts against each other and find out similar ones.

# Usage
Adapter contains `api` package defining interface. There is reference implementation which uses REST service API to connect to remote service. Interface and implementation of this RI are in `adapter.api` and `adapter.spi` packages. Usage of this RI is configured in `config` and `spi` packages.

Here is example code for theWrench service, using this analyzer:

```
package io.resys.wrench.assets.bundle.groovy;
import java.io.Serializable;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.ServiceProgram;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionResult;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionLog;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResultLog;
import io.resys.hdes.client.api.programs.ServiceProgram.ServiceResult;
import io.resys.hdes.client.api.programs.Program.ProgramContext;
import io.resys.hdes.client.api.programs.ServiceData;
import io.digiexpress.eveli.textanalyzer.api.Sentiment;
import io.digiexpress.eveli.textanalyzer.api.ImmutableTextCategoryItem;
import io.digiexpress.eveli.textanalyzer.api.TextCategoryItem;
import io.digiexpress.eveli.textanalyzer.api.TextAnalyzerService;
import io.digiexpress.eveli.textanalyzer.api.TextSentimentAndSubcategory;

public class FeedbackAnalyzer {
  public Output execute(Input input, ProgramContext ctx) {
    TextCategoryItem request = ImmutableTextCategoryItem.builder()
    .id(input.id)
    .language(input.language)
    .text(input.text)
    .mainCategory(input.mainCategory)
    .build();
    TextSentimentAndSubcategory result = ctx.getBean(TextAnalyzerService.class).findSentimentAndSubcategory(request);
    Output output = new Output();
    output.sentiment = result.sentiment;
    output.confidence = result.sentimentConfidence;
    output.subcategory = result.subcategory;
    return output;
  }
  @ServiceData
  public static class Input implements Serializable {
    String id
    String language
    String text
    String mainCategory
  }
  @ServiceData
  public static class Output implements Serializable {
    Sentiment sentiment
    Float confidence
    String subcategory
  }
}
```

# Configuration

See io.digiexpress.eveli.analyzer.properties.AnalyzerServerProps for configurable properties of RI.
