import config
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def validate_environment():
    errors = []
    
    if config.PROVIDER not in ['azure', 'gcp']:
        errors.append(f"Invalid FEEDBACK_PROVIDER: {config.PROVIDER}. Must be 'azure' or 'gcp'")
    
    if config.PROVIDER == 'azure':
        if not config.AZURE_ENDPOINT:
            errors.append("AZURE_ENDPOINT is required when using Azure provider")
        if not config.AZURE_API_KEY:
            errors.append("AZURE_API_KEY is required when using Azure provider")
    
    elif config.PROVIDER == 'gcp':
        if not config.GOOGLE_PROJECT_ID:
            errors.append("GOOGLE_PROJECT_ID is required when using GCP provider")
        if not config.GOOGLE_CREDENTIALS_PATH:
            errors.append("GOOGLE_CREDENTIALS_PATH is required when using GCP provider")
    
    if errors:
        for error in errors:
            logger.error(error)
        raise EnvironmentError("Invalid environment configuration. See logs for details.")