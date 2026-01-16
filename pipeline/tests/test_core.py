"""
Simple tests for core functions.
Run with: python -m pytest tests/
"""

import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent))

from src.evaluation.normalization import normalize_human_grades
from src.evaluation.metrics import compute_metrics


GRADE_MAP = {"A": 93, "A-": 90, "B+": 87, "B": 83, "C": 73, "F": 40}


def test_normalize_grades(tmp_path):
    """Test grade normalization."""
    samples = [{
        "submission_id": "1",
        "human_rows": [
            {"skill": "Correctness", "grade": "A"},
            {"skill": "Readability", "grade": "B"}
        ]
    }]

    output = tmp_path / "grades.json"
    result = normalize_human_grades(samples, GRADE_MAP, str(output))

    assert result["1"]["Correctness"] == 93
    assert result["1"]["Readability"] == 83


def test_compute_metrics(tmp_path):
    """Test metric computation."""
    predictions = [{
        "_submission_id": "1",
        "grades": {"Correctness": "A-", "Readability": "B"}
    }]
    human = {"1": {"Correctness": 93, "Readability": 83}}
    dims = ["Correctness", "Readability"]

    output = tmp_path / "metrics.json"
    result = compute_metrics(predictions, human, dims, GRADE_MAP, 5, str(output))

    assert result["per_dimension"]["Correctness"]["MAE"] == 3  # 93-90
    assert result["per_dimension"]["Readability"]["MAE"] == 0  # exact match
