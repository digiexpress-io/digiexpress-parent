from os import getenv
from dotenv import load_dotenv
import os

load_dotenv()

# Provider selection - 'azure' or 'gcp'
PROVIDER = getenv('FEEDBACK_PROVIDER', 'azure')

# Azure configuration
AZURE_ENDPOINT = getenv('AZURE_ENDPOINT')
AZURE_API_KEY = getenv('AZURE_API_KEY')

# GCP configuration  
GOOGLE_CREDENTIALS_PATH = getenv('GOOGLE_CREDENTIALS_PATH')
GOOGLE_PROJECT_ID = getenv('GOOGLE_PROJECT_ID')

if PROVIDER == 'gcp' and GOOGLE_CREDENTIALS_PATH:
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = GOOGLE_CREDENTIALS_PATH

# Sentiment thresholds 
POSITIVE_THRESHOLD = float(getenv('POSITIVE_THRESHOLD', 0.25))
NEGATIVE_THRESHOLD = float(getenv('NEGATIVE_THRESHOLD', -0.25))

# Supported languages
SUPPORTED_LANGUAGES = ["en", "fi", "sv"]