import config

find_sentiment_and_subcategory_example = {
    "id": "1",
    "language": "en",
    "mainCategory": "business",
    "text": "Hi, I would really like to commend you for properly handling the workplace harassment report.",
    "categories": {
        "en": {
            "business": {
                "customer_service": ["service", "support", "helpful", "rude", "friendly"],
                "workplace": ["harassment", "environment", "colleagues", "management"]
            },
            "environment": {
                "pollution": ["waste", "emissions", "contamination"],
                "conservation": ["wildlife", "forests", "protection"]
            }
        }
    }
}

find_similar_example = {
  "id": "batch_1",
  "entries": [
    {
      "id": "1",
      "language": "en",
      "text": "Hi, I would like to make a complaint about the delay in renewing my identification card"
    },  
    {
      "id": "2",
      "language": "en",
      "text": "Hello, I just wanted to say that you have a great business incubator program that helped my startup and many others."
    },  
    {
      "id": "3",
      "language": "en",
      "text": "Hi, I was wondering why does it take so long to get my identification card renewed?"
    }
  ]
}