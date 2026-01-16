# AI Grading Evaluation Pipeline

> Can an AI grade student code as well as a human? This pipeline finds out.

---

## What Does This Project Do?

This pipeline compares **AI grading** (using Ollama LLM) vs **human grading** for student code submissions.

```
Student Code → AI grades it → Compare with human grades → How accurate was AI?
```

**Example Output:**
- "AI was within 5 points of human grades 72% of the time"
- "MAE (error) = 4.8 points on Readability"

---

## Project Structure

```
Gradini/
├── samples10.csv              # Human grades (CSV)
├── submissions/               # Student Java code
│   ├── 18~19_Submission_13/
│   ├── 18~19_Submission_15/
│   └── 18~19_Submission_16/
├── prompts/                   # LLM prompt template
│   └── grading_prompt_template.txt
│
└── mlops_pipeline/            # ← This pipeline
    ├── src/
    │   ├── data/prepare.py           # Step 1: Load data
    │   ├── grading/ollama_grader.py  # Step 2: AI grading
    │   ├── evaluation/
    │   │   ├── normalization.py      # Step 3: Convert grades
    │   │   └── metrics.py            # Step 4: Calculate accuracy
    │   ├── visualization/plots.py    # Step 5: Generate charts
    │   └── pipeline.py               # Main entry point
    ├── config.yaml            # All settings (model, paths, etc.)
    ├── artifacts/             # Output files saved here
    ├── logs/                  # Log files
    ├── tests/                 # Unit tests
    ├── Makefile               # Shortcut commands
    └── requirements.txt       # Python dependencies
```

---

## How the Pipeline Works (5 Steps)

```
┌─────────────────────────────────────────────────────────────────┐
│                        pipeline.py                              │
│                                                                 │
│  STEP 1: prepare.py                                             │
│  ─────────────────                                              │
│  • Read samples10.csv (human grades)                            │
│  • Read Java files from submissions/                            │
│  • Combine into one JSON file                                   │
│                          ↓                                      │
│  STEP 2: ollama_grader.py                                       │
│  ────────────────────────                                       │
│  • Send each submission to Ollama LLM                           │
│  • Get AI grades: Correctness, Readability, etc.                │
│  • Save predictions to JSON                                     │
│                          ↓                                      │
│  STEP 3: normalization.py                                       │
│  ────────────────────────                                       │
│  • Convert letter grades (A, B+, C) to numbers (93, 87, 73)     │
│  • Average multiple human grades per dimension                  │
│                          ↓                                      │
│  STEP 4: metrics.py                                             │
│  ──────────────────                                             │
│  • Compare AI grades vs human grades                            │
│  • Calculate: MAE, RMSE, accuracy %                             │
│                          ↓                                      │
│  STEP 5: plots.py                                               │
│  ────────────────                                               │
│  • Generate bar charts (MAE per dimension)                      │
│  • Save as PNG files                                            │
│                                                                 │
│  ✅ Done! Check artifacts/ for results                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Prerequisites

### 1. Python 3.10+
Check your version:
```bash
python --version
```

### 2. Ollama (Local LLM)
Download from: https://ollama.ai

Then pull a model:
```bash
ollama pull llama3.1:8b
```

Start Ollama (keep this running):
```bash
ollama serve
```

---

## Installation

### Option A: Windows (PowerShell)

```powershell
cd mlops_pipeline
pip install -r requirements.txt
```

### Option B: Linux/WSL (with virtual environment)

```bash
cd mlops_pipeline

# Create virtual environment
python3 -m venv .venv

# Activate it
source .venv/bin/activate

# Install packages
pip install -r requirements.txt
```

---

## Running the Pipeline

### Windows (PowerShell)

```powershell
cd mlops_pipeline
python -m src.pipeline
```

### Linux/WSL (using Makefile)

```bash
cd mlops_pipeline
make install   # First time only
make run       # Run pipeline
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

## Configuration (config.yaml)

All settings in one file:

```yaml
model:
  name: "llama3.1:8b"    # Which Ollama model to use
  timeout: 60            # Seconds before timeout
  retries: 2             # Retry on failure

paths:
  samples_csv: "../samples10.csv"      # Human grades
  submissions_dir: "../submissions"    # Student code
  prompt_template: "../prompts/grading_prompt_template.txt"

evaluation:
  dimensions:            # What to grade
    - "Correctness"
    - "Readability"
    - "Code Elegance"
    - "Documentation"
  accuracy_threshold: 5  # "Within X points" counts as accurate
```

---

## Commands Reference

| Command | What it does |
|---------|--------------|
| `python -m src.pipeline` | Run the full pipeline |
| `python -m pytest tests/ -v` | Run unit tests |
| `make run` | (Linux) Run pipeline |
| `make test` | (Linux) Run tests |
| `make clean` | (Linux) Delete artifacts |

---

## Troubleshooting

### "Timeout after 60s"
- Make sure Ollama is running: `ollama serve`
- Try a faster model: change `model.name` to `llama3.2:3b`

### "No such file: samples10.csv"
- Paths in `config.yaml` should start with `../` (go up one folder)

### "Module not found: matplotlib"
- Install dependencies: `pip install -r requirements.txt`

### Tests fail
- Run from `mlops_pipeline/` folder
- Check Python path with: `python -c "import src.pipeline"`

---

## Grade Scale Used

| Grade | Points |
|-------|--------|
| A++ | 100 |
| A+ | 97 |
| A | 93 |
| A- | 90 |
| B+ | 87 |
| B | 83 |
| B- | 80 |
| C+ | 77 |
| C | 73 |
| C- | 70 |
| D | 60 |
| F | 40 |

---

## Why This Design? (MLOps Best Practices)

| Practice | How we do it |
|----------|--------------|
| **No hardcoded values** | Everything in `config.yaml` |
| **Reproducible** | Same config = same results |
| **Modular** | Each step is a separate file |
| **Logged** | Check `logs/pipeline.log` |
| **Tested** | Unit tests in `tests/` |
| **One command** | `make run` or `python -m src.pipeline` |

---

## License

MIT - Use freely!
