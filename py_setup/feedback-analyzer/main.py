from fastapi import FastAPI, HTTPException, Body, Query
from analyzers.base.similarity_analyzer import SimilarityRequest, SimilarityResponse
from analyzers.base.sentiment_analyzer import SentimentRequest
from analyzers.base.subcategory_analyzer  import SubcategoryRequest
from analyzers.base.sentiment_subcategory import SentimentSubcategoryRequest, SentimentSubcategoryResponse
from utils.analyzer_factory import AnalyzerFactory
from utils.validate_env import validate_environment
from utils.payload_examples import find_similar_example, find_sentiment_and_subcategory_example
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Feedback Analyzer API",
    description="API for analyzing customer feedback using sentiment analysis, subcategory classification, and similarity search.",
    version="1.0.0"
)
    
validate_environment()

@app.post(
    "/find-sentiment-and-subcategory",
    response_model=SentimentSubcategoryResponse,
    summary="Analyze Sentiment and Determine Subcategory",
    description="Analyze sentiment and determine subcategory of the provided feedback text.",
    responses={
        200: {"description": "Sentiment and subcategory analysis successful"},
        400: {"description": "Invalid request"},
        500: {"description": "Internal server error"}
    }
)
async def find_sentiment_and_subcategory(
    sentiment: bool = Query(True, description="Set to false to skip sentiment analysis"),
    subcategory: bool = Query(True, description="Set to false to skip subcategory analysis"),
    request: SentimentSubcategoryRequest = Body(..., example=find_sentiment_and_subcategory_example)
):
    if not sentiment and not subcategory:
        raise HTTPException(status_code=400, detail="At least one of sentiment or subcategory analysis must be requested")

    sentiment_analyzer = AnalyzerFactory.create_sentiment_analyzer()
    subcategory_analyzer = AnalyzerFactory.create_subcategory_analyzer()
    sentiment_response = None
    subcategory_response = None

    try:
        if sentiment:
          sentiment_request = SentimentRequest(
              id=request.id,
              language=request.language,
              text=request.text
          )
          sentiment_response = sentiment_analyzer.analyze_sentiment(sentiment_request)

        if subcategory:
          subcategory_request = SubcategoryRequest(
              id=request.id,
              language=request.language,
              mainCategory=request.mainCategory,
              text=request.text,
              categories=request.categories
          )
          subcategory_response = subcategory_analyzer.determine_subcategory(subcategory_request)

        return SentimentSubcategoryResponse(
            sentiment=sentiment_response,
            subcategory=subcategory_response
        )
    except Exception as e:
        logger.error(f"Error processing sentiment and subcategory analysis: {e}, for request with id: {request.id}")
        raise HTTPException(status_code=500, detail="Internal Server Error")
    
@app.post(
    "/find-similar", 
    response_model=SimilarityResponse,
    summary="Find Similar Feedback",
    description="Find similar feedback tasks based on the provided feedback text.",
    responses={
        200: {"description": "Finding similar feedback successful"},
        400: {"description": "Invalid request"},
        500: {"description": "Internal server error"}
    }
)
async def find_similar(request: SimilarityRequest = Body(..., example=find_similar_example)):
    analyzer = AnalyzerFactory.create_similarity_analyzer()
    try:
        response = analyzer.find_similar_batch(request)
        return response
    except Exception as e:
        logger.error(f"Error processing similarity search: {e}, for request with id: {request.id}")
        raise HTTPException(status_code=500, detail="Internal Server Error")