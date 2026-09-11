FROM python:3.12-slim
RUN apt-get update && apt-get install -y --no-install-recommends libreoffice-writer poppler-utils fonts-liberation fonts-dejavu graphviz nodejs && rm -rf /var/lib/apt/lists/*
RUN pip install --no-cache-dir python-docx pypdf pillow pymupdf pdf2image lxml
WORKDIR /work
