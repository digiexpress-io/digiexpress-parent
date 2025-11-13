from google.cloud import language_v1
from analyzers.base.sentiment_analyzer import BaseSentimentAnalyzer, SentimentRequest, SentimentResponse, SentenceSentiment
from utils.text_utils import detect_language, fix_encoding
import config
from typing import Dict, Any, List
import logging
from datetime import datetime, timezone
from analyzers.gcp.versions import sentiment_version

class GCPSentimentAnalyzer(BaseSentimentAnalyzer):
    
    def __init__(self, debug_mode=True):
        self.client = language_v1.LanguageServiceClient()
        self.positive_threshold = config.POSITIVE_THRESHOLD
        self.negative_threshold = config.NEGATIVE_THRESHOLD
        
        # Setup logging
        logging.basicConfig(level=logging.INFO if debug_mode else logging.WARNING,
                           format='%(asctime)s - %(levelname)s - %(message)s')
        self.logger = logging.getLogger(__name__)
        
        # Languages supported by Google Cloud NL API for sentiment analysis
        self.supported_sentiment_languages = ["en", "zh", "fr", "de", "it", "ja", "ko", "pt", "es"]
    
    def detect_language(self, text: str) -> str:
        return detect_language(text)
    
    def analyze_sentiment(self, request: SentimentRequest) -> SentimentResponse:
        text = request.text
        language = request.language
        id = request.id

        text = fix_encoding(text)
        
        if language is None:
            language = self.detect_language(text)
        
        # Check if language is supported by Google Cloud for sentiment analysis
        if language not in self.supported_sentiment_languages:
            return self._keyword_sentiment_analysis(text, language, id)
        
        try:
            document = language_v1.Document(
                content=text,
                type_=language_v1.Document.Type.PLAIN_TEXT,
                language=language
            )
            
            response = self.client.analyze_sentiment(
                request={"document": document}
            )
            
            score = response.document_sentiment.score
            magnitude = response.document_sentiment.magnitude
            
            # Determine sentiment based on thresholds
            if score >= self.positive_threshold:
                sentiment = "positive"
            elif score <= self.negative_threshold:
                sentiment = "negative"
            else:
                sentiment = "neutral"
            
            # Extract sentence-level analysis
            sentences: List[SentenceSentiment] = []
            for sentence in response.sentences:
                sent_score = sentence.sentiment.score
                if sent_score >= self.positive_threshold:
                    sent_sentiment = "positive"
                elif sent_score <= self.negative_threshold:
                    sent_sentiment = "negative"
                else:
                    sent_sentiment = "neutral"
                
                sentences.append(SentenceSentiment(
                    text=sentence.text.content,
                    sentiment=sent_sentiment,
                    scores={"score": round(sent_score, 3), "magnitude": round(sentence.sentiment.magnitude, 3)}
                ))
            
            return SentimentResponse(
                id=id,
                sentiment=sentiment,
                confidence=magnitude,
                sentences=sentences,
                timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                modelVersion=sentiment_version,
                modelId="gcp_sentiment_analyzer"
            )
            
        except Exception as e:
            self.logger.error(f"GCP sentiment analysis error: {str(e)} for request with id: {request.id}")
            return self._keyword_sentiment_analysis(text, language, id)
    
    def _keyword_sentiment_analysis(self, text: str, language: str, id: str) -> Dict[str, Any]:
        """Enhanced keyword-based sentiment analysis for unsupported languages."""
        positive_keywords = {
            "en": ["good", "great", "excellent", "amazing", "wonderful", "fantastic", "perfect", "love", "best", "awesome"],
            "fi": ["hyvä", "loistava", "mahtava", "upea", "täydellinen", "rakasta", "paras", "ihana", "erinomainen"],
            "sv": ["bra", "utmärkt", "fantastisk", "underbar", "perfekt", "älska", "bäst", "härlig", "fantastisk"]
        }
        
        negative_keywords = {
            "en": ["bad", "terrible", "awful", "horrible", "worst", "hate", "disgusting", "poor", "disappointing"],
            "fi": ["huono", "kauhea", "hirveä", "pahin", "vihainen", "huonoin", "inhottava", "surkeä"],
            "sv": ["dålig", "fruktansvärd", "hemsk", "värst", "hata", "sämst", "äcklig", "besviken"]
        }
        
        text_lower = text.lower()
        positive_count = sum(1 for word in positive_keywords.get(language, []) if word in text_lower)
        negative_count = sum(1 for word in negative_keywords.get(language, []) if word in text_lower)
        
        total_words = len(text_lower.split())
        positive_ratio = positive_count / total_words if total_words > 0 else 0
        negative_ratio = negative_count / total_words if total_words > 0 else 0
        
        score = positive_ratio - negative_ratio
        
        if score > 0.05:
            sentiment = "positive"
        elif score < -0.05:
            sentiment = "negative"
        else:
            sentiment = "neutral"

        return SentimentResponse(
            id = id,
            sentiment = sentiment,
            confidence = abs(score),
            sentences=[{
                "text": text,
                "sentiment": sentiment,
                "scores": { "score": score }
            }],
            timestamp = datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
            modelVersion = sentiment_version,
            modelId = "gcp_keyword_fallback"
        )    