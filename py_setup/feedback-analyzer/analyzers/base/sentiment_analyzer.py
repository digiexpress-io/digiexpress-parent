from abc import ABC, abstractmethod
from typing import Dict, Optional, List, Literal
from dataclasses import dataclass

@dataclass
class SentimentRequest:
    id: str
    text: str
    language: Optional[str]

@dataclass
class SentenceSentiment:
    text: str
    sentiment: Literal["positive", "negative", "neutral", "mixed", "unknown"]
    scores: Dict[str, float]


@dataclass
class SentimentResponse:
    id: str
    sentiment: Literal["positive", "negative", "neutral", "mixed", "unknown"]
    confidence: float
    sentences: List[SentenceSentiment]
    timestamp: str
    modelVersion: str
    modelId: str

class BaseSentimentAnalyzer(ABC):
    @abstractmethod
    def analyze_sentiment(self, request: SentimentRequest) -> SentimentResponse:
        pass
    
    @abstractmethod
    def detect_language(self, text: str) -> str:
        pass