FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt .

# Install build tools + curl + unzip for downloading corpora
RUN apt-get update && apt-get install -y --no-install-recommends \
        build-essential \
        curl \
        unzip \
        && rm -rf /var/lib/apt/lists/*

RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# Download NLTK corpora (WordNet + omw-1.4) without triggering nltk import
RUN mkdir -p /usr/local/share/nltk_data/corpora && \
    curl -L https://raw.githubusercontent.com/nltk/nltk_data/gh-pages/packages/corpora/wordnet.zip -o /tmp/wordnet.zip && \
    curl -L https://raw.githubusercontent.com/nltk/nltk_data/gh-pages/packages/corpora/omw-1.4.zip -o /tmp/omw-1.4.zip && \
    unzip /tmp/wordnet.zip -d /usr/local/share/nltk_data/corpora && \
    unzip /tmp/omw-1.4.zip -d /usr/local/share/nltk_data/corpora && \
    rm /tmp/wordnet.zip /tmp/omw-1.4.zip

COPY . .

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]