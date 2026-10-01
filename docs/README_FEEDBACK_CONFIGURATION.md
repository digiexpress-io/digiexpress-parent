# Feedback overview
Feedback improves standard Eveli form/task capability with possibility to extract from form feedback request information (such as category, title and content), creation of answer to feedback request and publication of answer in portal page.


# Requirements for feedback form

For implementing feedback feedback form should be created first. For feedback support form should provide following information:

* field containing main category name
* field(s) containing subcategory name, this is optional
* field containing title for feedback request
* field containing content of feedback
* optional field containing yes/no value for user agreement to publish feedback on portal page. If this is not configured or user has answered `no` then feedback can't be published.
* field or context variable containing user name


# Feedback configuration

## Flow configuration

### Setting task features

To enable feedback handling (i.e. creating response to user feedback and publishing it in task management UI) task should have feature `feedback` set. Generally for this dialob questionnaire collector should check field for user agreement to publish feedback and if this field contains true value then task's feature `feedback` can be set.

E.g. in questionnaire collector following code assuming that field name for such question is "publicAnswerAllowed":
```
...
    if(dialob.bool "publicAnswerAllowed"){
      output.taskFeature = "feedback"
    }
...
@ServiceData
  public static class Output implements Serializable {
...
    String taskFeature; 
... 
```

This parameter should be passed to task builder by flow:
``` 
  - Create task:
      id: "createTask"
      then: end
      service:
        ref: FeedBackTaskBuilder
        restful: false
        collection: false
        inputs:
          questionnaireId: questionnaireId
...
          taskFeatures: collectQuestionnaireData.taskFeature
...
```

And setting feature in task builder:
``` 
public class FeedBackTaskBuilder {
  
  public Output execute(Input input, ProgramContext ctx) {
... 
    final ImmutableCreateTaskCommand.Builder command = ImmutableCreateTaskCommand.builder()
      .subject("${input.label},${input.category}")
      .description("Asiakas ${input.firstName} ${input.lastName}, SSN ${input.ssn}, ${input.email}, ${input.address}")
...
      .featuresAsCsv(input.taskFeatures)
...


  @ServiceData
  public static class Input implements Serializable {
    String firstName;
    String lastName;
    String ssn;
...
    @Nullable String taskFeatures; 
...    
  }
```

### Handling additional information

Additionally task can be populated with feedback specific data, e.g. feedback title can be added to additional info field. For this fetch feedback title from questionnaire (assuming this field name is `feedbackTitle`):

```
...
  output.title = dialob.text "feedbackTitle"
...
@ServiceData
  public static class Output implements Serializable {
...
    String title; 
... 
```

pass this into task builder in flow:

This parameter should be passed to task builder by flow:
``` 
  - Create task:
      id: "createTask"
      then: end
      service:
        ref: FeedBackTaskBuilder
        restful: false
        collection: false
        inputs:
          questionnaireId: questionnaireId
...
          infoText: collectQuestionnaireData.title
...
```

And setting this additional info field in task builder:
``` 
public class FeedBackTaskBuilder {
  
  public Output execute(Input input, ProgramContext ctx) {
    final ImmutableCreateTaskCommand.Builder command = ImmutableCreateTaskCommand.builder()
      .subject("${input.label},${input.category}")
      .description("Asiakas ${input.firstName} ${input.lastName}, SSN ${input.ssn}, ${input.email}, ${input.address}")
...
      .additionalInfo(input.infoText)
...


  @ServiceData
  public static class Input implements Serializable {
    String firstName;
    String lastName;
    String ssn;
...
    @Nullable String infoText; 
...    
  }
```

## Configuration for applications

## Backend application configuration

For feedback application properties see file `README_CONFIG_PROPERTIES.md`
To fill these values fields from form are needed (see above) along with form name.


## Portal UI configuration

Feedback component is shown conditionally in component configuration. By default its configuration is like this ( file `components-g.tsx`):

```
  GArticleFeedback: {
    defaultProps: {
      enabled(view: { id: string }) { 
        return view.id.toLowerCase().endsWith('palaute');
      },
    }
  },
```

To customize this conditional for `enabled` function should be adjusted. 