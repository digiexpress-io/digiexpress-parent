import logging
import re
from azure.core.credentials import AzureKeyCredential
from azure.ai.textanalytics import TextAnalyticsClient
from azure.identity import DefaultAzureCredential
from analyzers.base.sentiment_analyzer import BaseSentimentAnalyzer, SentimentRequest, SentimentResponse
from utils.text_utils import detect_language, normalize_text
import config
from datetime import datetime, timezone
from analyzers.azure_loc.versions import sentiment_version

class AzureSentimentAnalyzer(BaseSentimentAnalyzer):
    
    def __init__(self, credential=None, debug_mode=True):
        if credential is None:
            credential = AzureKeyCredential(config.AZURE_API_KEY) if config.AZURE_API_KEY else DefaultAzureCredential()
        
        self.client = TextAnalyticsClient(
            endpoint=config.AZURE_ENDPOINT,
            credential=credential
        )
        
        # Setup logging
        logging.basicConfig(level=logging.INFO if debug_mode else logging.WARNING,
                           format='%(asctime)s - %(levelname)s - %(message)s')
        self.logger = logging.getLogger(__name__)
        
        # Finnish-specific detection patterns
        self.finnish_chars = 'äöå'
        self.finnish_words = [
            'koulu', 'koulun', 'järjestelmä', 'ilmoittautuminen',
            'rekisteröinti', 'parantaa', 'tyytyväinen'
        ]
    
    def _is_likely_finnish(self, text):
        text_lower = text.lower()
        finnish_char_count = sum(1 for char in text_lower if char in self.finnish_chars)
        finnish_word_count = sum(1 for word in self.finnish_words if word in text_lower)
        return finnish_char_count > 0 or finnish_word_count > 0
    
    def detect_language(self, text: str) -> str:
        try:
            detected = detect_language(text)
            
            try:
                clean_text = normalize_text(text)
                response = self.client.detect_language(
                    documents=[{"id": "1", "text": clean_text}], 
                    country_hint="FI"
                )
                azure_detected = response[0].primary_language.iso6391_name
                confidence = response[0].primary_language.confidence_score
                
                # Override Azure detection if strong indicators for Finnish
                if azure_detected == 'en' and self._is_likely_finnish(text):
                    return 'fi'
                    
                # Use Azure result if confidence is high
                if confidence > 0.8 and azure_detected in config.SUPPORTED_LANGUAGES:
                    return azure_detected
                    
            except Exception as e:
                self.logger.warning(f"Azure language detection failed: {str(e)}")
            
            return detected
            
        except Exception as e:
            self.logger.error(f"Language detection error: {str(e)}, text: {text}")
            return 'en'  # Default fallback
    
    def analyze_sentiment(self, request: SentimentRequest) -> SentimentResponse:
        try:
            language = request.language
            text = request.text
            id = request.id

            if language is None:
                language = self.detect_language(text)
            
            clean_text = normalize_text(text)
            
            response = self.client.analyze_sentiment(
                documents=[{"id": "1", "language": language, "text": clean_text}])[0]
            
            sentiment = response.sentiment

            scores = {
                "positive": round(response.confidence_scores.positive, 3),
                "neutral": round(response.confidence_scores.neutral, 3),
                "negative": round(response.confidence_scores.negative, 3)
            }
            
            sentences = []
            for sentence in response.sentences:
                sentence_sentiment = sentence.sentiment
                
                sentences.append({
                    "text": sentence.text,
                    "sentiment": sentence_sentiment,
                    "scores": {
                        "positive": round(sentence.confidence_scores.positive, 3),
                        "neutral": round(sentence.confidence_scores.neutral, 3),
                        "negative": round(sentence.confidence_scores.negative, 3)
                    }
                })
            
            return SentimentResponse(
                id = id,
                sentiment = sentiment,
                confidence = scores[sentiment] if sentiment in scores else 0.0,
                sentences = sentences,
                timestamp = datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                modelVersion = sentiment_version,
                modelId = "azure_sentiment_analyzer"
            )
            
        except Exception as e:
            self.logger.error(f"Sentiment analysis error: {str(e)} for request with id: {request.id}")
            response = SentimentResponse(
                id = request.id,
                sentiment = "unknown",
                confidence = 0.0,
                sentences=[],
                timestamp = datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                modelVersion = sentiment_version,
                modelId = "azure_sentiment_analyzer"
            )



