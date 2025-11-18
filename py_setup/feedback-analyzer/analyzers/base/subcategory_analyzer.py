from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import List, Dict, Optional

@dataclass
class SubcategoryRequest:
    id: str
    language: Optional[str]
    mainCategory: str
    text: str
    categories: Dict[str, Dict[str, Dict[str, List[str]]]]  # language -> main category -> subcategory -> keywords

@dataclass
class SubcategoryResponse:
    id: str
    subcategory: str        # "unclassified" if no subcategory is found
    confidence: float
    matches: List[str]
    scores: Dict[str, float]
    timestamp: str
    modelVersion: str
    modelId: str

class BaseSubcategoryAnalyzer(ABC):
    
    @abstractmethod
    def determine_subcategory(self, request: SubcategoryRequest) -> SubcategoryResponse:
        pass