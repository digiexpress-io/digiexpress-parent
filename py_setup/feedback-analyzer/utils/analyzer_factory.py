import config
import logging

logger = logging.getLogger(__name__)

class AnalyzerFactory:
    # Factory for creating analyzer instances based on provider
    
    @staticmethod
    def create_sentiment_analyzer():
        # Create sentiment analyzer based on provider
        try:
            if config.PROVIDER == 'azure':
                from analyzers.azure_loc.sentiment_analyzer import AzureSentimentAnalyzer
                return AzureSentimentAnalyzer()
            elif config.PROVIDER == 'gcp':
                from analyzers.gcp.sentiment_analyzer import GCPSentimentAnalyzer
                return GCPSentimentAnalyzer()
            else:
                raise ValueError(f"Unsupported provider: {config.PROVIDER}")
        except ImportError as e:
            logger.error(f"Failed to import {config.PROVIDER} sentiment analyzer: {e}")
            raise
    
    @staticmethod
    def create_similarity_analyzer():
        # Create similarity analyzer based on provider
        try:
            if config.PROVIDER == 'azure':
                from analyzers.azure_loc.similarity_analyzer import AzureSimilarityAnalyzer
                return AzureSimilarityAnalyzer()
            elif config.PROVIDER == 'gcp':
                from analyzers.gcp.similarity_analyzer import GCPSimilarityAnalyzer
                return GCPSimilarityAnalyzer()
            else:
                raise ValueError(f"Unsupported provider: {config.PROVIDER}")
        except ImportError as e:
            logger.error(f"Failed to import {config.PROVIDER} similarity analyzer: {e}")
            raise
    
    @staticmethod
    def create_subcategory_analyzer():
        # Create subcategory analyzer based on provider
        try:
            if config.PROVIDER == 'azure':
                from analyzers.azure_loc.subcategory_analyzer import AzureSubcategoryAnalyzer
                return AzureSubcategoryAnalyzer()
            elif config.PROVIDER == 'gcp':
                from analyzers.gcp.subcategory_analyzer import GCPSubcategoryAnalyzer
                return GCPSubcategoryAnalyzer()
            else:
                raise ValueError(f"Unsupported provider: {config.PROVIDER}")
        except ImportError as e:
            logger.error(f"Failed to import {config.PROVIDER} subcategory analyzer: {e}")
            raise