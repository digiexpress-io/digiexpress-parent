# AI tools integration API

*WIP*

**Note:** This is a proposal for the API, all naming, endpoints, and data structures are subject to change based on the implementation details and requirements.

## Feedback analysis tools

### Configuration data provided by DE

Following configuration data is provided by DE as a JSON file, stored in a file storage system depending on the cloud environment used. 
#### Feedback category / subcategory mapping data

This configuration can be customer specific and should be part of the publication, deployed into the file storage on publication time. It is required that this data includes identifiers for the categories used that match the option list identifiers on the feedback forms of the given DE setup to match the results of the analysis with the feedback forms.


#### Feedback similarity search history data

This data should contain the historic data as JSON file, stored in a file storage system depending on the cloud environment used. This data should be updated periodically, e.g. once a day before running the feedback similarity analysis job.

Similarity search history data should contain the following fields:

- Task identifier
- User language
- Feedback text

Example json:

```json
[
    {
        "taskId": "12345",
        "language": "en",
        "feedbackText": "This is a sample feedback text."
    },
    {
        "taskId": "67890",
        "language": "fi",
        "feedbackText": "Tämä on esimerkkipalauteteksti."
    }
]
```

### Analysis services

Here are proposed services provided by the feedback analysis tool. 

Expected result codes:

- **200: OK** - Successful result
- **400: Bad Request** - Input data is not valid (mandatory fields are missing, etc.)
- **500: Internal Server Error** - A technical error occurred while processing the request

Text encoding header should be set to UTF-8 for all requests and responses.

- **Model version number** - A version number of the analysis model. This should be incremented whenever the model is updated. It indicates when the analysis algorithm gets updated and gives indication for the DE user / tester if the analysis results are based on the latest model. It also serves as a flag to for potential batch processing to re-analyze the data with the latest model. The format of the model version number needs to be agreed upon, options: semver or a simple incrementing number.
- **Model identifier** - An identifier of the analysis model used, For example `gcp-nlp`, `azure-text-analytics` etc. This is used to identify the model used for the analysis and can be used for testing and debugging purposes.

#### Subcategory and sentiment detection

This service analyzes the feedback text and detects the subcategory and sentiment. It is working in a scope of a single feedback task and provides analysis results to be stored in the task data. 
Its input should have the following attributes:

- Task identifier
- User language
- Main category 
- Feedback text

Example json:

```json
{
    "taskId": "12345",
    "language": "en",
    "mainCategory": "serviceQuality",
    "feedbackText": "The service was excellent and very fast."
}
```

The service responds with following results:

- Task identifier (corresponding to the input)
- Subcategory identifier or "unknown"
- Subcategory text *TODO: Is this needed?*
- Subcategory confidence score (0-1)
- Sentiment identifier (positive, negative, neutral, unknown)
- Sentiment confidence score (0-1)
- Timestamp of the analysis
- Version number of the analysis model used
- Identifier of the analysis model used

If subcategory or sentiment cannot be detected, the service should return "unknown" for subcategory and "unknown" for sentiment. Also, respective confidence scores should be 0.

Example response json:

```json
{
    "taskId": "12345",
    "subcategoryId": "excellentService",
    "subcategoryText": "Excellent Service",
    "subcategoryConfidence": 0.95,
    "sentimentId": "positive",
    "sentimentConfidence": 0.98,
    "timestamp": "2023-10-01T12:00:00Z",
    "modelVersion": "1.0.0",
    "modelId": "model-12345"
}
```

Proposed service endpoint:

**POST** `/subcategory-sentiment`

#### Feedback similarity detection

This service searches for similar feedback texts in the similarity search history data. It is working in a scope of a list of feedback tasks and provides search results to be stored in the task data. It is intended to be used in batch processing.

Its input is an array of feedback tasks, each with the following attributes:

- Task identifier
- User language
- Feedback text

Example request json:

```json
[
    {
        "taskId": "12345",
        "language": "en",
        "feedbackText": "The service was excellent and very fast."
    },
    {
        "taskId": "67890",
        "language": "fi",
        "feedbackText": "Tämä on esimerkkipalauteteksti."
    }
]
```

The service responds with a result containing analysis metadata and an array of similarity search results for each task.

- Timestamp of the analysis
- Version number of the analysis model used
- Identifier of the analysis model used
- Array of tasks that were processed, each with the following attributes:
  - Task identifier (corresponding to the input)
  - Language of the feedback text of the input task
  - Feedback text of the input task *TODO: Is this needed?*
  - Array of similarity search results, each with the following attributes (can be empty if no similar feedback was found):
    - Task identifier of the similar feedback task (corresponding to the history data)
    - Similarity score (0-1)
    - Feedback text (from history data) *TODO: Is this needed?*
    - Language of the similarity text

Example response json:

```json
{
    "timestamp": "2023-10-01T12:00:00Z",
    "modelVersion": "1.0.0",
    "modelId": "model-12345",
    "tasks": [
        {
            "taskId": "12345",
            "language": "en",
            "feedbackText": "The service was excellent and very fast.",
            "similarities": [
                {
                    "taskId": "54321",
                    "similarityScore": 0.85,
                    "similarityText": "The service was great and quick.",
                    "language": "en"
                }
            ]
        },
        {
            "taskId": "67890",
            "language": "fi",
            "feedbackText": "Tämä on esimerkkipalauteteksti.",
            "similarities": []
        }
    ]
}
```

Proposed service endpoint:

**POST** `/similarity-search`