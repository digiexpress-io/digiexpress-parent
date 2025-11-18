Feedback Analyzer
=========================

A feedback analysis system that combines Azure Text Analytics and Google Cloud Natural Language APIs with advanced machine learning techniques for sentiment analysis, similarity detection, and subcategory classification.

# Features

Azure Text Analytics: Cloud-based analysis with key phrase extraction and entity recognition \
Google Cloud Natural Language: Advanced ML with TF-IDF similarity and multi-language stemming \
Switching between providers via configuration 

## Multi-Language Support

English, Finnish, and Swedish with advanced language detection \
Provider-specific optimizations for each language \
Fallback mechanisms for unsupported languages 

# Analytics 

Sentiment Analysis: Document and sentence-level with confidence scores \
Similarity Detection: TF-IDF, cosine similarity, and Azure feature matching \
Subcategory Classification: Rule-based and ML-powered categorization 

# Architecture

Abstract base classes for consistent interfaces \
Factory pattern for provider abstraction \
Separation of concerns \
Provider-specific categories optimized for each service 

# Prerequisites

Python 3.8 or higher \
Azure Text Analytics resource (for Azure provider) \
Google Cloud Natural Language API access (for GCP provider) 

# Versioning

Each of the analyzers for each provider has its own specific version, kept in `versions.py` \
These versions are returned as part of every response, so it is important that they are kept up to date \
Version changes and history are recorded in `CHANGELOG.md` 

# Local setup

### 1. Installation

Navigate into project directory
```shell
cd unified-feedback-analyzer
```

### 2. Install dependencies

#### Check Python version and make sure it's up to 3.12.5, if not install it using [pyenv](https://github.com/pyenv/pyenv)

```shell
python -V
```

```shell
pyenv install 3.12.5
pyenv local 3.12.5
```


#### Create a virtual environment
```shell
python -m venv venv
source venv/bin/activate
```

#### Install dependencies from file
```shell
pip install -r requirements.txt
```

### 3. Configuration

Create a `.env` file based on `.env.example`.
 
```shell
cp .env.example .env
```

#### For Azure:
```shell
FEEDBACK_PROVIDER=azure
AZURE_ENDPOINT=https://your-resource.cognitiveservices.azure.com/
AZURE_API_KEY=your_azure_api_key_here
```

#### For GCP:
```shell
FEEDBACK_PROVIDER=gcp
GOOGLE_PROJECT_ID=your-gcp-project-id
GOOGLE_CREDENTIALS_PATH=path/to/your/service-account-key.json
```

### 4. Run the Application

```shell
python -m uvicorn main:app --reload
```

#### or using Docker

```shell
docker build -t feedback-analyzer .
docker run -p 8000:8000 --name feedback-analyzer -d feedback-analyzer
```

### 4. Access the API

Open your browser and navigate to: http://localhost:8000/docs \
This will open the interactive Swagger UI for testing the endpoints


# Project Structure

```
unified-feedback-analyzer/
├── config.py                         # Unified configuration
├── main.py                           # Main application running FastAPI
├── Dockerfile                        # Docker configuration
├── requirements.txt                  # Dependencies
├── .env                              # Credentials (create from .env.example)
├── .env.example                      # Configuration template
├── .gitignore                        # Git ignore file
├── utils/
│   ├── text_utils.py                 # Text processing utilities
│   └── analyzer_factory.py           # Provider factory
├── analyzers/
│   ├── base/                         # Abstract base classes
│   ├── azure/                        # Azure implementations
│   └── gcp/                          # GCP implementations
└── README.md                  
