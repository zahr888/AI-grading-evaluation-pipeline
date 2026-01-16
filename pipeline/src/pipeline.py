#!/usr/bin/env python3
"""
==============================
AI Grading Evaluation Pipeline
==============================
"""

import json
import logging
import os
import sys
from datetime import datetime, timezone
from pathlib import Path

import yaml

# Setup imports
MLOPS_DIR = Path(__file__).parent.parent
sys.path.insert(0, str(MLOPS_DIR))

from src.data.prepare import prepare_data
from src.grading.ollama_grader import run_grading
from src.evaluation.normalization import normalize_human_grades
from src.evaluation.metrics import compute_metrics
from src.visualization.plots import generate_plots


def load_config(config_path: str = "config.yaml") -> dict:
    """Load config from YAML file."""
    if not os.path.isabs(config_path):
        config_path = os.path.join(MLOPS_DIR, config_path)
    with open(config_path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)


def setup_logging(log_file: str) -> logging.Logger:
    """Setup simple logging to console and file."""
    os.makedirs(os.path.dirname(log_file), exist_ok=True)

    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s | %(levelname)-8s | %(message)s',
        datefmt='%H:%M:%S',
        handlers=[
            logging.StreamHandler(),
            logging.FileHandler(log_file, encoding='utf-8')
        ]
    )
    return logging.getLogger("pipeline")


def run_pipeline(config_path: str = "config.yaml"):
    """Run the complete evaluation pipeline."""

    # Setup
    run_id = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    config = load_config(config_path)
    logger = setup_logging(config["logging"]["log_file"])

    logger.info("=" * 50)
    logger.info("AI GRADING EVALUATION PIPELINE")
    logger.info(f"Run ID: {run_id} | Model: {config['model']['name']}")
    logger.info("=" * 50)

    # Artifact paths
    artifacts = config["artifacts"]
    paths = {
        "data": f"{artifacts['data_dir']}/{run_id}_samples.json",
        "predictions": f"{artifacts['predictions_dir']}/{run_id}_predictions.json",
        "human_grades": f"{artifacts['metrics_dir']}/{run_id}_human_grades.json",
        "metrics": f"{artifacts['metrics_dir']}/{run_id}_metrics.json",
    }

    # STEP 1: Prepare Data
    logger.info("\n[1/5] Preparing data...")
    samples = prepare_data(
        config["paths"]["samples_csv"],
        config["paths"]["submissions_dir"],
        paths["data"]
    )

    # STEP 2: Run Ollama Grading
    logger.info("\n[2/5] Running Ollama grading...")
    predictions = run_grading(
        samples,
        config["paths"]["prompt_template"],
        config["model"]["name"],
        config["model"]["timeout"],
        config["model"]["retries"],
        paths["predictions"]
    )

    # STEP 3: Normalize Human Grades
    logger.info("\n[3/5] Normalizing human grades...")
    human_grades = normalize_human_grades(
        samples,
        config["evaluation"]["grade_map"],
        paths["human_grades"]
    )

    # STEP 4: Compute Metrics
    logger.info("\n[4/5] Computing metrics...")
    metrics = compute_metrics(
        predictions,
        human_grades,
        config["evaluation"]["dimensions"],
        config["evaluation"]["grade_map"],
        config["evaluation"]["accuracy_threshold"],
        paths["metrics"]
    )

    # STEP 5: Generate Plots
    logger.info("\n[5/5] Generating plots...")
    generate_plots(metrics, artifacts["plots_dir"], run_id)

    # Done
    logger.info("\n" + "=" * 50)
    logger.info("PIPELINE COMPLETE")
    logger.info(f"Artifacts saved to: {artifacts['base_dir']}")
    logger.info("=" * 50)

    return {"run_id": run_id, "metrics": metrics}


if __name__ == "__main__":
    run_pipeline()
