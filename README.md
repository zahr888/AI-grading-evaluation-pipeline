# AI Grading Evaluation Pipeline

> This is an experimental/testing project to explore how well an AI can grade student code and to practice simple MLOps patterns.
> The goal is to create a small, reproducible evaluation pipeline you can run locally and extend later.

---

## What this project does 

It compares AI-generated grades (via a local Ollama model) with human instructor grades for Java assignments. The pipeline runs the evaluation end-to-end and saves predictions, metrics and simple charts so you can see where AI helps and where it fails.

High-level flow:

```
Student code → AI grades it → Compare to human grades → Metrics + charts
```

---

## Where the samples come from

Samples are taken from the public Menagerie dataset: [https://github.com/m-messer/Menagerie](https://github.com/m-messer/Menagerie)

The prepared CSV contains rows like:

```
Unnamed: 0,assignment_number,comments,skill,participant_id,batch,grade
0,18.0,"The code uses meaningful identifier names...",Readability,15,1,B+
0,18.0,"The core requirements ... seem met...",Correctness,15,1,A-
...
```

**What that means:** each row is one human comment/grade for a submission (participant_id). The CSV provides multi-dimension human evaluations (Correctness, Readability, Code Elegance, Documentation) plus a textual comment which is great for comparing AI grades and feedback.

---

## What I implemented

I made a lightweight pipeline that is:

* **Modular**: each step is a small, importable module (example: `from src.data.prepare import prepare_data`)
* **Configurable**: single `config.yaml` holds model name, paths and basic params
* **Reproducible**: artifacts are saved per run to `artifacts/` with a run id
* **Logged**: simple structured logs saved to `logs/pipeline.log`
* **Robust**: failures on a single submission are logged, pipeline continues

New tools/practices I experimented with:

* `Makefile` for convenient commands (`make run`, `make test`)
* `config.yaml` for centralized config
* Python `logging` for structured logs
* Prompt engineering: strict JSON schema and fallback behavior
* Organizing code as importable modules

---

Here is the **clean, concise replacement section** for the metrics part of your README:

---

## Implemented Metrics (current)

### Per grading dimension

For each dimension (Correctness, Readability, Code Elegance, Documentation), the pipeline computes:

* **MAE**
* **RMSE**
* **accuracy_pct**

### Overall run statistics

Across the whole evaluation run:

* **total_predictions** — total number of AI grading attempts
* **successful** — number of predictions without errors
* **threshold_used** — accuracy threshold (in points) used for accuracy calculation

> These metrics provide a first, interpretable view of grading accuracy and reliability.
> In future iterations, additional metrics and deeper analyses will be added.

---

## Project structure (short)

```
mlops_pipeline/
├─ src/
│  ├─ data/prepare.py
│  ├─ grading/ollama_grader.py
│  ├─ evaluation/normalization.py
│  ├─ evaluation/metrics.py
│  ├─ visualization/plots.py
│  └─ pipeline.py
├─ config.yaml
├─ artifacts/        # data, predictions, metrics, plots per run
├─ logs/
├─ tests/
├─ Makefile
└─ requirements.txt
```

---

## Quick start (WSL / Linux)

1. Ensure Python 3.10+, Ollama is installed and a model is pulled:

```bash
ollama pull llama3.1:8b
```

2. Create and activate a virtualenv, install:

```bash
# one-line (recommended)
make install

# or

python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

3. Run the pipeline:

```bash
# one-line (recommended)
make run

# or
python -m src.pipeline
```
---

## Expected Output

```
14:30:00 | INFO | ==================================================
14:30:00 | INFO | AI GRADING EVALUATION PIPELINE
14:30:00 | INFO | Run ID: 20260116T143000Z | Model: llama3.1:8b
14:30:00 | INFO | ==================================================

14:30:00 | INFO | [1/5] Preparing data...
14:30:00 | INFO | Found 3 participants

14:30:01 | INFO | [2/5] Running Ollama grading...
14:30:01 | INFO | [1/3] Grading 13...
14:30:15 | INFO | [2/3] Grading 15...
14:30:30 | INFO | [3/3] Grading 16...

14:30:31 | INFO | [3/5] Normalizing human grades...
14:30:31 | INFO | [4/5] Computing metrics...
14:30:31 | INFO | Metrics Summary:
14:30:31 | INFO |   Correctness: MAE=4.5, Accuracy=66.7%
14:30:31 | INFO |   Readability: MAE=3.2, Accuracy=75.0%

14:30:32 | INFO | [5/5] Generating plots...
14:30:32 | INFO | ==================================================
14:30:32 | INFO | PIPELINE COMPLETE
14:30:32 | INFO | Artifacts saved to: artifacts
14:30:32 | INFO | ==================================================
```

---
## Output Files (artifacts/)

After running, check these folders:

| Folder | Contents |
|--------|----------|
| `artifacts/data/` | Prepared samples JSON |
| `artifacts/predictions/` | AI grades for each submission |
| `artifacts/metrics/` | MAE, RMSE, accuracy scores |
| `artifacts/plots/` | PNG charts |

---

## Next steps (planned)

* Add more evaluation metrics (e.g., BERTScore for feedback, calibration metrics)
* Benchmark additional models and prompt variants
* Improve feedback similarity with sentence embeddings
* Add a simple CI step (GitHub Actions) to run unit tests on PRs
* Optionally add Dockerfile + Makefile target for containerized runs
* 
---
