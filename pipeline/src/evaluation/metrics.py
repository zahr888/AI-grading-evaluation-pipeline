"""
Evaluation Metrics - MAE, RMSE, and accuracy.
"""

import json
import math
import os
import logging
from collections import defaultdict

logger = logging.getLogger(__name__)


def compute_metrics(predictions: list, human_grades: dict, dimensions: list,
                    grade_map: dict, accuracy_threshold: int, output_path: str) -> dict:
    """
    Compute evaluation metrics comparing model predictions to human grades.

    Returns dict with per_dimension and overall metrics.
    """
    # Collectors
    mae = defaultdict(list)
    mse = defaultdict(list)
    within_thresh = defaultdict(int)
    counts = defaultdict(int)

    for pred in predictions:
        if "_error" in pred:
            continue

        sid = pred.get("_submission_id")
        model_grades = pred.get("grades", {})
        human = human_grades.get(sid, {})

        for dim in dimensions:
            human_score = human.get(dim)
            model_letter = model_grades.get(dim)
            model_score = grade_map.get(model_letter) if model_letter else None

            if human_score is not None and model_score is not None:
                diff = abs(model_score - human_score)
                mae[dim].append(diff)
                mse[dim].append((model_score - human_score) ** 2)
                if diff <= accuracy_threshold:
                    within_thresh[dim] += 1
                counts[dim] += 1

    # Compute final metrics
    per_dim = {}
    for dim in dimensions:
        n = counts[dim]
        per_dim[dim] = {
            "count": n,
            "MAE": round(sum(mae[dim]) / n, 2) if n else None,
            "RMSE": round(math.sqrt(sum(mse[dim]) / n), 2) if n else None,
            "accuracy_pct": round(within_thresh[dim] / n * 100, 1) if n else None
        }

    metrics = {
        "per_dimension": per_dim,
        "overall": {
            "total_predictions": len(predictions),
            "successful": sum(1 for p in predictions if "_error" not in p),
            "threshold_used": accuracy_threshold
        }
    }

    # Save
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(metrics, f, indent=2)

    # Log summary
    logger.info("Metrics Summary:")
    for dim, m in per_dim.items():
        if m["MAE"]:
            logger.info(f"  {dim}: MAE={m['MAE']}, Accuracy={m['accuracy_pct']}%")

    logger.info(f"Saved -> {output_path}")
    return metrics
