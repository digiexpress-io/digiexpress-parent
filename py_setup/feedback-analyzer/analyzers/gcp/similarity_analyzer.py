import re
import logging
from gensim.utils import simple_preprocess
from gensim.models import TfidfModel
from gensim import corpora
from gensim.similarities import SparseMatrixSimilarity
import nltk
from nltk.stem.snowball import SnowballStemmer
from analyzers.base.similarity_analyzer import BaseSimilarityAnalyzer, SimilarityRequest, SimilarityResponse, ProcessedEntry, EntrySimilarityResult
from utils.text_utils import detect_language, preprocess_text
from typing import List, Dict, Any, Optional
from datetime import datetime, timezone
from analyzers.gcp.versions import similarity_version
import traceback

class GCPSimilarityAnalyzer(BaseSimilarityAnalyzer):
    
    def __init__(self, debug_mode=True):
        logging.basicConfig(level=logging.INFO if debug_mode else logging.WARNING,
                           format='%(asctime)s - %(levelname)s - %(message)s')
        self.logger = logging.getLogger(__name__)
        self.debug_mode = debug_mode
        
        # Download NLTK resources if needed
        try:
            nltk.data.find('corpora/stopwords')
        except LookupError:
            nltk.download('stopwords', quiet=True)
        
        # Stemmers for different languages
        self.stemmers = {
            'fi': SnowballStemmer('finnish'),
            'sv': SnowballStemmer('swedish'),
            'en': SnowballStemmer('english')
        }
        
        # Language-specific stopwords
        self.stopwords = {
            'fi': list(nltk.corpus.stopwords.words('finnish')),
            'sv': list(nltk.corpus.stopwords.words('swedish')),
            'en': list(nltk.corpus.stopwords.words('english'))
        }
    
    def extract_text_features(self, text: str, language: str) -> Dict[str, Any]:
        features = {}
        
        # Preprocess text
        processed_text = preprocess_text(text, language)
        
        # Tokenize and stem
        stemmer = self.stemmers.get(language, self.stemmers['en'])
        stopwords_list = self.stopwords.get(language, self.stopwords['en'])
        
        # Simple preprocessing with Gensim
        tokens = simple_preprocess(processed_text, deacc=True, min_len=2, max_len=15)
        
        # Remove stopwords and stem
        stemmed_tokens = []
        for token in tokens:
            if token not in stopwords_list:
                try:
                    stemmed_token = stemmer.stem(token)
                    stemmed_tokens.append(stemmed_token)
                except:
                    stemmed_tokens.append(token)
        
        features["tokens"] = stemmed_tokens
        features["processed_text"] = processed_text
        features["original_text"] = text
        
        return features
    
    def find_similar_batch(self, request: SimilarityRequest, threshold: float = 0.1) -> SimilarityResponse:
        try:
            processed_entries: List[ProcessedEntry] = []
            all_features = []

            for entry in request.entries:
                language = entry.language or detect_language(entry.text)
                features = self.extract_text_features(entry.text, language)
                
                processed_entry = ProcessedEntry(
                    id=entry.id,
                    language=language,
                    text=entry.text,
                    similarities=[]
                )
                
                processed_entries.append(processed_entry)
                all_features.append(features["tokens"])
            
            dictionary = corpora.Dictionary(all_features)
            corpus = [dictionary.doc2bow(tokens) for tokens in all_features]
            tfidf_model = TfidfModel(corpus)
            tfidf_corpus = tfidf_model[corpus]
            
            similarity_index = SparseMatrixSimilarity(tfidf_corpus, num_features=len(dictionary))
            
            for i, entry in enumerate(processed_entries):
                query_tfidf = tfidf_model[corpus[i]]
                similarities = similarity_index[query_tfidf]

                for j in range(i + 1, len(processed_entries)):
                    score = float(similarities[j])
                    if score >= threshold:
                        similarity_result_for_other = EntrySimilarityResult(
                            id=processed_entries[j].id,
                            similarityScore=score,
                            text=processed_entries[j].text,
                            language=processed_entries[j].language
                        )
                        entry.similarities.append(similarity_result_for_other)

                        similarity_result_for_entry = EntrySimilarityResult(
                            id=entry.id,
                            similarityScore=score,
                            text=entry.text,
                            language=entry.language
                        )
                        processed_entries[j].similarities.append(similarity_result_for_entry)

            for entry in processed_entries:
                entry.similarities.sort(key=lambda x: x.similarityScore, reverse=True)

            
            response = SimilarityResponse(
                id=request.id,
                timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                modelVersion=similarity_version,
                modelId="gcp_similarity_analyzer",
                entries=processed_entries
            )
                        
            return response
        
        except Exception as e:
            self.logger.error(f"Error in similarity analysis: {str(e)} for request with id: {request.id}")
            self.logger.error(traceback.format_exc())
            return SimilarityResponse(
                id=request.id,
                timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                modelVersion=similarity_version,
                modelId="gcp_similarity_analyzer",
                entries=[]
            )
    
    def find_similar(self, text: str, feedback_database: List[str], threshold: float = 0.35, language: Optional[str] = None) -> List[Dict[str, Any]]:
        try:
            if language is None:
                language = detect_language(text)
            
            # Prepare all texts for processing
            all_texts = [text] + feedback_database
            all_features = []
            
            # Extract features from all texts
            for text_item in all_texts:
                features = self.extract_text_features(text_item, language)
                all_features.append(features["tokens"])
            
            dictionary = corpora.Dictionary(all_features)
            corpus = [dictionary.doc2bow(tokens) for tokens in all_features]
            tfidf_model = TfidfModel(corpus)
            tfidf_corpus = tfidf_model[corpus]
 
            similarity_index = SparseMatrixSimilarity(tfidf_corpus, num_features=len(dictionary))
            
            query_tfidf = tfidf_model[corpus[0]]
            similarities = similarity_index[query_tfidf]
            
            similar_items = []
            for i, similarity_score in enumerate(similarities[1:], 1):  # Skip first item
                if similarity_score >= threshold:
                    similar_items.append({
                        "text": feedback_database[i-1],
                        "similarity": float(similarity_score),
                        "method": "tfidf_cosine"
                    })
            
            similar_items.sort(key=lambda x: x["similarity"], reverse=True)
            
            return similar_items
            
        except Exception as e:
            self.logger.error(f"Error in similarity analysis: {str(e)} for request with text: {text}")
            self.logger.error(traceback.format_exc())
            return []