VENV = .venv
PYTHON = $(VENV)/bin/python
PIP = $(VENV)/bin/pip

.PHONY: run test clean help install

install:
	python3 -m venv $(VENV)
	$(PIP) install -r requirements.txt

run:
	$(PYTHON) -m src.pipeline

test:
	$(PYTHON) -m pytest tests/ -v

clean:
	rm -rf artifacts/data/* artifacts/predictions/* artifacts/metrics/* artifacts/plots/* logs/*
	rm -rf $(VENV)
	@echo "Cleaned artifacts and virtual environment!"

help:
	@echo "Commands:"
	@echo "  make install - Create .venv and install dependencies"
	@echo "  make run     - Run pipeline (using .venv)"
	@echo "  make test    - Run tests (using .venv)"
	@echo "  make clean   - Remove artifacts and .venv"