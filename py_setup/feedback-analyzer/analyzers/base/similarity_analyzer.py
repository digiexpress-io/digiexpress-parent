from abc import ABC, abstractmethod
from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field

@dataclass
class Entry:
    id: str
    language: Optional[str]
    text: str

@dataclass
class SimilarityRequest:
    id: str
    entries: List[Entry]
    
@dataclass
class EntrySimilarityResult:
    id: str
    similarityScore: float
    text: str
    language: str

@dataclass
class ProcessedEntry:
    id: str
    language: str
    text: str
    similarities: List[EntrySimilarityResult] = field(default_factory=list)

@dataclass
class SimilarityResponse:
    id: str
    timestamp: str
    modelVersion: str
    modelId: str
    entries: List[ProcessedEntry]

class BaseSimilarityAnalyzer(ABC):
    
    @abstractmethod
    def find_similar_batch(self, request: SimilarityRequest, threshold: float) -> SimilarityResponse:
        pass
    
    @abstractmethod
    def find_similar(self, text: str, feedback_database: List[str], threshold: float, language: Optional[str]) -> List[Dict[str, Any]]:
        pass
    
    @abstractmethod
    def extract_text_features(self, text: str, language: str) -> Dict[str, Any]:
        pass