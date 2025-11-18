import logging
import nltk
from nltk.stem.snowball import SnowballStemmer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from analyzers.base.subcategory_analyzer import BaseSubcategoryAnalyzer, SubcategoryRequest, SubcategoryResponse
from utils.text_utils import detect_language, fix_encoding, preprocess_text
import numpy as np
from datetime import datetime, timezone
from analyzers.gcp.versions import subcategory_version
import config

class GCPSubcategoryAnalyzer(BaseSubcategoryAnalyzer):
    
    def __init__(self):
        self.logger = logging.getLogger(__name__)
        try:
            nltk.data.find('tokenizers/punkt')
            nltk.data.find('corpora/stopwords')
        except LookupError:
            nltk.download('punkt', quiet=True)
            nltk.download('stopwords', quiet=True)

        self.stemmers = {
            'fi': SnowballStemmer('finnish'),
            'sv': SnowballStemmer('swedish'),
            'en': SnowballStemmer('english')
        }
    
    def determine_subcategory(self, request: SubcategoryRequest) -> SubcategoryResponse:
        try:
            id = request.id
            language = request.language
            main_category = request.mainCategory
            text = request.text
            categories = request.categories
            
            if language is None:
                language = detect_language(text)
            
            text = fix_encoding(text)
            processed_text = preprocess_text(text, language)
            
            # Get categories for language
            language_categories = categories.get(language, {})
            
            # Find matching main category
            matching_category = None
            for category_name in language_categories.keys():
                if (main_category.lower() in category_name.lower() or 
                    category_name.lower() in main_category.lower()):
                    matching_category = category_name
                    break
            
            if not matching_category:
                self.logger.error(f"No matching main category for '{main_category}' in language '{language}'")
                return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="gcp_subcategory_analyzer"
                )
            
            subcategories = language_categories[matching_category]
            if not subcategories:
                self.logger.error(f"No subcategories found for main category '{main_category}' in language '{language}'")
                return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="gcp_subcategory_analyzer"
                )
            
            # Prepare texts for TF-IDF
            subcategory_texts = []
            subcategory_names = []
            
            for subcat_name, keywords in subcategories.items():
                subcat_text = ' '.join(keywords)
                subcategory_texts.append(preprocess_text(subcat_text, language))
                subcategory_names.append(subcat_name)
            
            # Create TF-IDF vectors
            all_texts = [processed_text] + subcategory_texts
            vectorizer = TfidfVectorizer(
                lowercase=True,
                stop_words=None,
                ngram_range=(1, 2),
                max_features=1000
            )
            
            tfidf_matrix = vectorizer.fit_transform(all_texts)
            
            # Calculate similarities
            input_vector = tfidf_matrix[0]
            subcategory_vectors = tfidf_matrix[1:]
            similarities = cosine_similarity(input_vector, subcategory_vectors).flatten()
            
            if len(similarities) == 0:
                self.logger.error(f"No similarity scores calculated for request with id: {id}")
                return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="gcp_subcategory_analyzer"
                )
            
            # Find best match
            best_idx = np.argmax(similarities)
            best_similarity = similarities[best_idx]
            best_subcategory = subcategory_names[best_idx]
            
            # Minimum threshold
            if best_similarity < 0.1:
                self.logger.error(f"Best similarity {best_similarity} below threshold for reqeust with id: {id}")
                return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="gcp_subcategory_analyzer"
                )
            
            return SubcategoryResponse(
                id=id,
                subcategory=best_subcategory,
                confidence=float(best_similarity),
                matches=[name for name, sim in zip(subcategory_names, similarities) if sim > 0.1],
                scores={name: float(sim) for name, sim in zip(subcategory_names, similarities)},
                timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                modelVersion=subcategory_version,
                modelId="gcp_subcategory_analyzer"
            )
            
        except Exception as e:
            self.logger.error(f"Subcategory analysis error: {e} for request with id: {request.id}")
            return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="gcp_subcategory_analyzer"
                )
